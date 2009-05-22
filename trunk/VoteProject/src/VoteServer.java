import org.jgroups.Address;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;

public class VoteServer extends ReceiverAdapter
{
	// Variables used in this class
	private String state;
	private JChannel channel;

	// Health check thread
	private HealthCheck healthThread;
	private boolean isAlive;
	
	// global state ;   key=state;   value = <key = candidate; value = count>
	public Hashtable<String, Hashtable<String, Integer>> globalTally;
	
	
	// Constructor
	public VoteServer(String st) throws Exception
	{
		healthThread = new HealthCheck();
		healthThread.setServer(this);
		
		globalTally = new Hashtable<String, Hashtable<String, Integer>>();
		state = st;				
		
		//add an entry in the global tally for our state
		globalTally.put(state, new Hashtable<String, Integer>());
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
		channel.connect(state);							// Join the channel for the state we want
		channel.getState(null, 10000);
	
		//start the health check
		isAlive = true;
		healthThread.start();
	}	

	public void stop()
	{
		isAlive = false;
		System.out.println("State Server: " + state + " : " + channel.getLocalAddressAsString() + " has stopped");		
		channel.disconnect();
	}

	public void vote(String state, String cand) throws Exception
	{
		if (globalTally.containsKey(state))		//if the state exists
		{
			if (globalTally.get(state).containsKey(cand))  //if the candidate exists
			{
				//increment the vote count
				int candidateVoteCount = globalTally.get(state).get(cand);
				candidateVoteCount = candidateVoteCount + 1;
				globalTally.get(state).put(cand, candidateVoteCount); 
			}
			else											//candidate does not exist
			{
				//set an initial vote count
				globalTally.get(state).put(cand, 1);
			}
		}
		else									//state does not exist
		{
			//create entry for the state
			globalTally.put(state, new Hashtable<String, Integer>());
			
			//add an entry for the candidate
			globalTally.get(state).put(cand, 1);
		}
	}

	public String getCandidatesByState() throws Exception
	{
		String ret = "{}";
		
		if (globalTally.containsKey(state))
			ret = globalTally.get(state).toString();
		
		//if we have no votes, return user friendly message
		if (ret.equals("{}"))
			ret = "No votes";
		
		return ret;
	}

	public int getResultsByCandidate(String cand) throws Exception
	{	
		int ret = 0;
		
		if (globalTally.containsKey(state))
			if (globalTally.get(state).containsKey(cand))
				ret = globalTally.get(state).get(cand);		
		
		return ret;
	}

	public String getNationalResults() throws Exception
	{
		Hashtable<String, Integer> results = new Hashtable<String, Integer>();
		
		//iterate through states
		Iterator<String> iter = globalTally.keySet().iterator();
		
		while (iter.hasNext())
		{
			String state = iter.next();
			
			Hashtable<String, Integer> stateResults = globalTally.get(state);
			
			//iterate state results
			Iterator<String> iterState = stateResults.keySet().iterator();
			
			while (iterState.hasNext())
			{
				String candidate = iterState.next();
				int stateCount = stateResults.get(candidate);
				
				//if candidate is in table, increment count
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

		if (msg.getObject() instanceof Hashtable)
		{
			//our state
			
			Hashtable<String, Hashtable<String, Integer>> rcvdGlobalTally = (Hashtable<String, Hashtable<String,Integer>>)msg.getObject();

			synchronized(globalTally)
			{
				globalTally = rcvdGlobalTally;
			}
		}	
		else if (msg.getObject() instanceof String)
		{
			//our healthcheck
			
		}
	}	

	public void viewAccepted(View new_view)
	{
		// This is called whenever someone joins or leaves the group
		//System.out.println("View Changed:  " + channel.getLocalAddress().toString());
		//System.out.println("Servers In Cluster " + this.state + ":   " + new_view.printDetails());
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
	
	public void ping() throws ChannelNotConnectedException, ChannelClosedException
	{
		//send a healthcheck to all members in our group
		Message message = new Message(null, null, "ping");
		channel.send(message);
	}
}