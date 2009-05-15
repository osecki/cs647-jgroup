import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;
import java.util.Hashtable;

public class VoteServer extends ReceiverAdapter
{
	// Variables used in this class
	private String state;
	private JChannel channel;
	public java.util.Hashtable<String, Integer> voteTally;

	// Constructor 1
	public VoteServer()
	{
		voteTally = new Hashtable<String, Integer>();	// Create data structure
	}

	// Constructor 2
	public VoteServer(String st)
	{
		voteTally = new Hashtable<String, Integer>();	// Create data structure
		state = st;										// Save state for channel connection
	}

	public void start() throws Exception
	{
		System.out.println("\nStarting New Channel - Joining Cluster");

		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect(state);							// Join the channel for the state we want
		channel.getState(null, 10000);	

		// TODO start a new thread for a heartbeat?
	}	

	public void stop()
	{
		channel.disconnect();
	}

	public void vote(String cand) throws Exception
	{
		// Add candidate vote to data structure and propagate
		start();

		if (voteTally.containsKey(cand))
		{
			int candidateVoteCount = voteTally.get(cand);
			candidateVoteCount = candidateVoteCount + 1;
			voteTally.put(cand, candidateVoteCount);   			
		}
		else
		{
			voteTally.put(cand, 1);
		}

		Message msg = new Message(null, null, voteTally);
		channel.send(msg);			
	}

	public String getCandidatesByState() throws Exception
	{
		start();
		return voteTally.toString();
	}

	public int getResultsByCandidate(String cand) throws Exception
	{	
		int ret = 0;

		start();

		if (voteTally.containsKey(cand))
			ret = voteTally.get(cand);

		return ret;
	}

	public Hashtable<String, Integer> getResultsByStateHT() throws Exception
	{
		start();
		return voteTally;
	}

	@SuppressWarnings("unchecked")
	public void receive(Message msg) 
	{

		if (msg.getObject() instanceof Hashtable)
		{
			Hashtable<String, Integer> rcvdVoteTally = (Hashtable<String, Integer>)msg.getObject();

			synchronized(voteTally)
			{
				voteTally = rcvdVoteTally;
			}
		}
		else
		{
			// TODO Process a string message for a heartbeat?
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
		synchronized(this.voteTally) 
		{
			try 
			{
				return Util.objectToByteBuffer(this.voteTally);
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
			Hashtable<String, Integer> tempVoteTally = (Hashtable<String, Integer>)(Util.objectFromByteBuffer(new_state));

			synchronized(this.voteTally) 
			{
				this.voteTally = tempVoteTally;
			}
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
}