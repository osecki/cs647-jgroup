import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.util.List;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Hashtable;

public class VoteServer extends ReceiverAdapter
{
	private String state;
	private String candidate;
	private JChannel channel;
	
	public java.util.Hashtable<String, Integer> voteTally;
	
	public VoteServer()
	{
		voteTally = new Hashtable<String, Integer>();
	}

	public VoteServer(String st)
	{
		voteTally = new Hashtable<String, Integer>();	//create data structure
		state = st;										//save state for channel connection
	}
	
	public void start() throws Exception
	{
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect(state);							//join the channel for the state we want
		channel.getState(null, 10000);
	}	
	
	public void vote(String cand) throws Exception
	{
		candidate = cand;

		//add candidate vote to data structure and propogate
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
	
	public Hashtable getResultsByCandidate(String cand) throws Exception
	{
		channel = new JChannel();
		channel.setReceiver(this);
		channel.connect(state);					//join the channel for the state we want
		channel.getState(null, 10000);
		
		return voteTally;
	}
	
    public void receive(Message msg) 
    {
    	Hashtable rcvdVoteTally = (Hashtable)msg.getObject();
        
        synchronized(voteTally)
        {
        	voteTally = rcvdVoteTally;
        }
        
        //debug
        //this.dumpVotes();
    }	
	
	public void viewAccepted(View new_view)
	{
		//debug
		//System.out.println("viewAccepted - " + new_view);
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
	
    public void setState(byte[] new_state) 
    {
        try 
        {        	
            Hashtable tempVoteTally = (Hashtable)(Util.objectFromByteBuffer(new_state));
            
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
    
    private void dumpVotes()
    {
    	System.out.println("Dump of Vote Tally: " + this.voteTally.toString());
    }
}
