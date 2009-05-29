import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelException;
import org.jgroups.ChannelListener;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.jmx.protocols.pbcast.NAKACK;
import org.jgroups.protocols.DISCARD;
import org.jgroups.protocols.FD;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;


public class VoteServer extends ReceiverAdapter implements ChannelListener
{
	// Variables used in this class
	private String state;
	private JChannel channel;
	private boolean active;
	
	ServerState serverState = null;
	
	// Global state ;  Key=state;  Value = <Key = candidate; Value = count>
	
	
	// Group membership object
	private View groupMembership;
	
	// Constructor
	public VoteServer(String st)
	{
		try
		{
			serverState = new ServerState();
			
			state = st;				
			
			// Add an entry in the global tally for our state
			serverState.globalTally.put(state, new Hashtable<String, Integer>());
			
			start();			
		}
		catch(Exception ex)
		{		
		}
	}

	public boolean isActive()
	{
		return active;
	}
	
	public String getStateName()
	{
		return state;
	}
	
	public Address getAddress()
	{
		return channel.getLocalAddress();
	}
	
	public void stopHealthCheck() throws Exception
	{
		//old code here...... doesn't quite work
//		channel.shutdown();
		
		//BS TEST
		DISCARD discard = (DISCARD) channel.getProtocolStack().findProtocol("DISCARD");
		
		if (discard != null)
		{
			discard.setDownDiscardRate(1);
			discard.setUpDiscardRate(1);
			System.out.println("Set discard rate");
		}
		//BS TEST
	}

	public void start() throws Exception
	{
		try
		{
			channel = new JChannel();
			channel.setOpt(JChannel.AUTO_RECONNECT, true);
			channel.setOpt(JChannel.AUTO_GETSTATE, true);
			channel.setReceiver(this);
			channel.addChannelListener(this);

		
			//BS TEST
			//Add protocol for DISCARD			
/*			
			DISCARD discard = new DISCARD();
			discard.setExcludeItself(false);
			discard.setLocalAddress(channel.getLocalAddress());
			ProtocolStack stack = channel.getProtocolStack();
			stack.insertProtocol(discard, ProtocolStack.ABOVE, stack.getTransport().getName());			
*/			
			channel.connect("vote"); 				// Join the channel for the state we want
			channel.getState(null, 10000);			
			
			// Start the health check
			active = true;
		}
		catch(Exception ex)
		{
		}
	}	

	public void vote(int voterID, String state, String cand)
	{
		try
		{
			//Check for unique voter ID, and if so, continue, else quit
			if (serverState.voteIDs.contains(voterID))
			{
				System.out.println("**** VoterID: " + voterID + " has already voted!");
				return;
			}
			
			//Add voter ID to table
			serverState.voteIDs.addElement(voterID);
			
			
			if (serverState.globalTally.containsKey(state))					// If the state exists
			{
				if (serverState.globalTally.get(state).containsKey(cand))  	// If the candidate exists
				{
					// Increment the vote count
					int candidateVoteCount = serverState.globalTally.get(state).get(cand);
					candidateVoteCount = candidateVoteCount + 1;
					serverState.globalTally.get(state).put(cand, candidateVoteCount); 
				}
				else											// Candidate does not exist
				{
					// Set an initial vote count
					serverState.globalTally.get(state).put(cand, 1);
				}
			}
			else												// State does not exist
			{
				// Create entry for the state
				serverState.globalTally.put(state, new Hashtable<String, Integer>());
				
				// Add an entry for the candidate
				serverState.globalTally.get(state).put(cand, 1);
			}			
			
			//Send state to all members in the group
			Message message = new Message(null, null, serverState);
			channel.send(message);
		}
		catch(Exception ex)
		{
		}
	}

	public String getCandidatesByState()
	{
		String ret = "{}";
	
		try
		{
			if (serverState.globalTally.containsKey(state))
				ret = serverState.globalTally.get(state).toString();
			
			// If we have no votes, return user friendly message
			if (ret.equals("{}"))
				ret = "No votes for this state yet.";			
		}
		catch(Exception ex)
		{
		}
		
		return ret;
	}

	public int getResultsByCandidate(String cand)
	{	
		int ret = 0;
	
		try
		{
			Iterator<String> iter = serverState.globalTally.keySet().iterator();
			
			while(iter.hasNext())
			{
				String st = iter.next();
				
				Hashtable<String, Integer> stateResults = serverState.globalTally.get(st);
				
				if (stateResults.containsKey(cand))
					ret = ret + stateResults.get(cand);
			}	
		}
		catch(Exception ex)
		{
		}
		
		return ret;
	}

	public String getNationalResults() throws Exception
	{
		Hashtable<String, Integer> results = new Hashtable<String, Integer>();
		
		// Iterate through states
		Iterator<String> iter = serverState.globalTally.keySet().iterator();
		
		while (iter.hasNext())
		{
			String state = iter.next();
			
			Hashtable<String, Integer> stateResults = serverState.globalTally.get(state);
			
			// Iterate state results
			Iterator<String> iterState = stateResults.keySet().iterator();
			
			while (iterState.hasNext())
			{
				String candidate = iterState.next();
				int stateCount = stateResults.get(candidate);
				
				// If candidate is in table, increment count
				if (results.containsKey(candidate))
				{
					int natlCount = results.get(candidate);
					results.put(candidate, natlCount + stateCount);	
				}
				else
				{
					results.put(candidate, stateCount);
				}
			}
		}
		
		return results.toString();
	}

	@SuppressWarnings("unchecked")
	public void receive(Message msg) 
	{
		//debug
		//System.out.println("*** RCVD " + this.getAddress() + " " + msg.getObject().getClass());
		
		
		String rcvdMsg = "";
		
		try
		{
			if (msg.getObject() instanceof ServerState)
			{
				// Our state
				ServerState rcvdGlobalTally = (ServerState)msg.getObject();

				synchronized(serverState)
				{
					//fix me
					serverState = (ServerState) rcvdGlobalTally;
				}
			}	
			else if (msg.getObject() instanceof String)
			{
				// Our health check has been received.
				rcvdMsg = (String)msg.getObject();
				Address sourceAddress = msg.getSrc();
			}			
		}
		catch(Exception ex)
		{
		}
	}	

	// This is called whenever someone joins or leaves the group	
	public void viewAccepted(View new_view)
	{		
		//System.out.println("view accept: " + new_view.printDetails());
		
		// Save the group membership view
		groupMembership = new_view;
	}

	public byte[] getState()				
	{		
		synchronized(serverState) 
		{
			try 
			{
				return Util.objectToByteBuffer(serverState);
			}
			catch(Exception e) 
			{
				e.printStackTrace();
				return null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void setState(byte[] new_state) 
	{		
		try 
		{        	
			ServerState tempGlobalTally = (ServerState)(Util.objectFromByteBuffer(new_state));

			synchronized(tempGlobalTally) 
			{
				serverState = tempGlobalTally;
			}
		}
		catch(Exception e) 
		{
		}	
	}

	public void channelClosed(Channel arg0) 
	{
	}

	public void channelConnected(Channel arg0) 
	{
	}

	public void channelDisconnected(Channel arg0) 
	{	
	}

	public void channelReconnected(Address arg0) 
	{	
	}
	
	public void suspect(Address address)
	{
		//this function gets called when we stop the heartbeat
		//then, it will call viewAccepted with the new view (excluding the suspect)
		//this suspect server will no longer receive any messages sent to the group
		
		System.out.println(this.getAddress().toString() + " has been alerted that " + address.toString() + " is suspect!");
		this.active = false;
	}

	public void channelShunned() 
	{
		System.out.println("************** CHANNEL SHUNNED!");
	}
	
	public void readmit()
	{
		try 
		{
			channel.open();
			channel.connect("vote");
			channel.getState(null, 10000);			
		} 
		catch (ChannelException e) 
		{
		}
	}
}