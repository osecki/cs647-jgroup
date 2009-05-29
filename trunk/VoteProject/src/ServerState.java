import java.util.Hashtable;
import java.util.Vector;


public class ServerState implements java.io.Serializable
{
	public Vector<Integer> voteIDs;
	public Hashtable<String, Hashtable<String, Integer>> globalTally;
	
	public ServerState()
	{
		voteIDs = new Vector<Integer>();
		globalTally = new Hashtable<String, Hashtable<String, Integer>>();		
	}
}
