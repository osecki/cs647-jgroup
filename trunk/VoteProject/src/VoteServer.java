import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelListener;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
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
	private boolean isAlive;
	
	// Global state ;  Key=state;  Value = <Key = candidate; Value = count>
	public Hashtable<String, Hashtable<String, Integer>> globalTally;
	
	// Group membership object
	private View groupMembership;
	
	// Constructor
	public VoteServer(String st)
	{
		try
		{
			globalTally = new Hashtable<String, Hashtable<String, Integer>>();
			state = st;				
			
			// Add an entry in the global tally for our state
			globalTally.put(state, new Hashtable<String, Integer>());
			
			start();			
		}
		catch(Exception ex)
		{		
		}
	}
	
	public String getStateName()
	{
		return state;
	}
	
	public Address getAddress()
	{
		return channel.getLocalAddress();
	}
	
	public void stopHealthCheck()
	{
		channel.shutdown();
	}
	
	public void startHealthCheck()
	{

	}

	public void start() throws Exception
	{
		try
		{
			channel = new JChannel();
			channel.setReceiver(this);
			channel.addChannelListener(this);
			channel.connect("vote"); 				// Join the channel for the state we want
			channel.getState(null, 10000);
		
			// Start the health check
			isAlive = true;
		}
		catch(Exception ex)
		{
		}
	}	

	public void vote(String state, String cand)
	{
		try
		{
			if (globalTally.containsKey(state))					// If the state exists
			{
				if (globalTally.get(state).containsKey(cand))  	// If the candidate exists
				{
					// Increment the vote count
					int candidateVoteCount = globalTally.get(state).get(cand);
					candidateVoteCount = candidateVoteCount + 1;
					globalTally.get(state).put(cand, candidateVoteCount); 
				}
				else											// Candidate does not exist
				{
					// Set an initial vote count
					globalTally.get(state).put(cand, 1);
				}
			}
			else												// State does not exist
			{
				// Create entry for the state
				globalTally.put(state, new Hashtable<String, Integer>());
				
				// Add an entry for the candidate
				globalTally.get(state).put(cand, 1);
			}			
			
			//Send state to all members in the group
			Message message = new Message(null, null, globalTally);
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
			if (globalTally.containsKey(state))
				ret = globalTally.get(state).toString();
			
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
			Iterator<String> iter = globalTally.keySet().iterator();
			
			while(iter.hasNext())
			{
				String st = iter.next();
				
				Hashtable<String, Integer> stateResults = globalTally.get(st);
				
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
		Iterator<String> iter = globalTally.keySet().iterator();
		
		while (iter.hasNext())
		{
			String state = iter.next();
			
			Hashtable<String, Integer> stateResults = globalTally.get(state);
			
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
		String rcvdMsg = "";
		
		try
		{
			if (msg.getObject() instanceof Hashtable)
			{
				// Our state
				Hashtable<String, Hashtable<String, Integer>> rcvdGlobalTally = (Hashtable<String, Hashtable<String,Integer>>)msg.getObject();

				synchronized(globalTally)
				{
					globalTally = (Hashtable<String, Hashtable<String, Integer>>) rcvdGlobalTally.clone();
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
		synchronized(globalTally) 
		{
			try 
			{
				return Util.objectToByteBuffer(globalTally);
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
			Hashtable<String, Hashtable<String, Integer>> tempGlobalTally = (Hashtable<String, Hashtable<String, Integer>>)(Util.objectFromByteBuffer(new_state));

			synchronized(globalTally) 
			{
				globalTally = tempGlobalTally;
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
		
		System.out.println(this.getAddress().toString() + " has been alerted that " + address.toString() + " is suspect!");
		this.isAlive = false;
	}

	public void channelShunned() 
	{
		System.out.println("************** CHANNEL SHUNNED!");
	}
}