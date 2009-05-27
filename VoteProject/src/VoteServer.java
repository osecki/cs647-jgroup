import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
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
//import java.io.Serializable;

public class VoteServer extends ReceiverAdapter
{
	// Variables used in this class
	private String state;
	private JChannel channel;

	// Health check thread
	private HealthCheck healthThread;
	public boolean isAlive;
	private Vector<Address> healthVector;
	
	
	// Global state ;  Key=state;  Value = <Key = candidate; Value = count>
	public Hashtable<String, Hashtable<String, Integer>> globalTally;
	
	// Group membership object
	private View groupMembership;
	
	// Constructor
	public VoteServer(String st) throws Exception
	{
		healthVector = new Vector<Address>();
		healthThread = new HealthCheck();
		healthThread.setServer(this);
		
		globalTally = new Hashtable<String, Hashtable<String, Integer>>();
		state = st;				
		
		// Add an entry in the global tally for our state
		globalTally.put(state, new Hashtable<String, Integer>());
		
		start();
	}
	
	public Address getAddress()
	{
		return channel.getLocalAddress();
	}

	public void sendStateToCluster() throws ChannelNotConnectedException, ChannelClosedException
	{
		Message message = new Message(null, null, globalTally);
		channel.send(message);
	}
	
	public void start() throws Exception
	{
		System.out.println("\nStarting New Channel - Joining Cluster");

		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect("vote"); 				// Join the channel for the state we want
		channel.getState(null, 10000);
	
		// Start the health check
		isAlive = true;
		healthThread.start();
	}	

	public void stop()
	{
		isAlive = false;
		System.out.println("State Server: " + state + ":  " + channel.getLocalAddressAsString() + " has stopped");		
	}
	
	public void disconn()
	{
		channel.disconnect();
	}

	public void vote(String state, String cand) throws Exception
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
	}

	public String getCandidatesByState() throws Exception
	{
		String ret = "{}";
		
		if (globalTally.containsKey(state))
			ret = globalTally.get(state).toString();
		
		// If we have no votes, return user friendly message
		if (ret.equals("{}"))
			ret = "No votes for this state yet.";
		
		return ret;
	}

	public int getResultsByCandidate(String cand) throws Exception
	{	
		int ret = 0;
		
		if (globalTally.containsKey(state) && globalTally.get(state).containsKey(cand))
			ret = globalTally.get(state).get(cand);		
		
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
					globalTally = rcvdGlobalTally;
				}
			}	
			else if (msg.getObject() instanceof String)
			{
				// Our health check has been received.
				rcvdMsg = (String)msg.getObject();
				Address sourceAddress = msg.getSrc();
				
				if (rcvdMsg.equals("ping"))
				{
					if (!sourceAddress.equals(channel.getLocalAddress()))
					{
						//if i am alive, send response back to sender
						if (this.isAlive)
						{

							Message message = new Message(sourceAddress, null, "pong");
							channel.send(message);
						}
					}
				}				
				else if (rcvdMsg.equals("pong"))
				{	
					//look through vector and remove address we have received ping from
					for (int i = healthVector.size() - 1; i >= 0; i--)
						if (healthVector.get(i).equals(sourceAddress))
							healthVector.removeElementAt(i);
					
					//check if healthVector is empty
					if (healthVector.isEmpty())
					{
						
					}
				}
			}			
		}
		catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}


	}	

	// This is called whenever someone joins or leaves the group	
	public void viewAccepted(View new_view)
	{
		System.out.println("*****************************");
		
		
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
			e.printStackTrace();
		}	
	}
	
	public void ping()
	{		
		try
		{
			//if i am the oldest guy in the group, send out the pings to all members
			if (channel.getLocalAddress().equals(groupMembership.getCreator()))
			{			
				healthVector.clear();
				
				//Set health vector to our group members (except me)
				for (int i = 0; i < groupMembership.getMembers().size(); i++)
					if (!groupMembership.getMembers().elementAt(i).equals(channel.getLocalAddress()))
						healthVector.add(groupMembership.getMembers().elementAt(i));
				
				// Send a health check to all members in our group
				Message message = new Message(null, null, "ping");
				channel.send(message);	
			}
		}
		catch(Exception ex)
		{		
		}
	}
	
	public void checkPongResponses()
	{
		try
		{
			//if i am the oldest guy in the group (who sent out the pings)
			if (channel.getLocalAddress().equals(groupMembership.getCreator()))
			{
				//we have not received a response from some cluster server
				if (!healthVector.isEmpty())
				{
					System.out.println("Server: " + healthVector.toString() + " has not responded to heartbeat - failed!");
					removeFailedServer(healthVector.elementAt(0));
				}
			}			
		}
		catch(Exception ex)
		{
		}
	}
	
	private void removeFailedServer(Address failedAddress)
	{
		//find the failed server by IP address from our servers list
		
		Iterator<String> iter = VoteClient.servers.keySet().iterator();
		
		while(iter.hasNext())
		{
			String state = iter.next();
			
			ArrayList<VoteServer> stateServers = VoteClient.servers.get(state);
			
			for (int i = 0 ; i < stateServers.size(); i++)
			{
				if (stateServers.get(i).getAddress().equals(failedAddress))
				{
					stateServers.get(i).disconn();
					return;
				}
			}
		}
	}
}