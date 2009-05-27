import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;

public class VoteClient 
{
	// Data structure to keep track of servers
	// Key:  state;
	// Value:  Arraylist of VoteServers in that cluster
	public static Hashtable<String, ArrayList<VoteServer>> servers;
	
	@SuppressWarnings("unchecked")
	private static VoteServer getRandomServer()
	{
		// Randomly choose a server state cluster
		int random = (int) ( 0 + Math.random() * servers.size());
		ArrayList<VoteServer> stateServers = (ArrayList<VoteServer>)servers.values().toArray()[random];
		
		// Randomly choose a server in that cluster
		int randomServer = (int) ( 0 + Math.random() * stateServers.size());
		return stateServers.get(randomServer);		
	}
	
	private static void broadcastState(byte[] globalState) throws ChannelNotConnectedException, ChannelClosedException
	{
		Iterator<String> iter = servers.keySet().iterator();
	
		// Loop through each of our states
		while(iter.hasNext())
		{
			String state = iter.next();
			
			if (servers.get(state).size() > 0)
			{
				// Grab the oldest state server and set its state and propagate to all servers in the cluster
				servers.get(state).get(0).setState(globalState);
				servers.get(state).get(0).sendStateToCluster();
			}
		}
	}
	
	public static void main(String[] args) 
	{		
		// Variables needed are defined here
		String userInput;
		String stateInput;
		String candidateInput;

		try 
		{
			//Create master server list
			servers = new Hashtable<String, ArrayList<VoteServer>>();

			// Get a reader to get client input from console
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// Start up 3 initial servers and add initial servers to list
			servers.put("NJ", new ArrayList<VoteServer>());
			servers.get("NJ").add(new VoteServer("NJ"));
			
			servers.put("PA", new ArrayList<VoteServer>());
			servers.get("PA").add(new VoteServer("PA"));
			
			servers.put("NY", new ArrayList<VoteServer>());
			servers.get("NY").add(new VoteServer("NY"));
			
			// Loop which runs the menu for the client until quit is chosen
			do
			{
				System.out.println("\n\n----------- Menu ------------");
				System.out.println("(1) Vote For Candidate");
				System.out.println("(2) Get Results For A Particular Candidate");
				System.out.println("(3) Get All Candidate Results For A State");
				System.out.println("(4) Get National Tally For All Candidates");
				System.out.println("(5) Kill A Random Server");				
				System.out.println("(6) Exit");
				System.out.println("---------------------------------");
				System.out.println();
				System.out.print("Select An Option: ");
				userInput = br.readLine();

				// Process based on user selection
				
				// Vote
				if (userInput.equals("1"))
				{
					System.out.print("Select A State:  ");
					stateInput = br.readLine();
					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();					

					// If the server group exists, use that group and vote
					if (servers.containsKey(stateInput) && servers.get(stateInput).size() > 0)
					{
						// Grab the oldest server and vote on it
						VoteServer server = servers.get(stateInput).get(0);
						server.vote(stateInput, candidateInput);
							
						// Broadcast new state
						broadcastState(server.getState());
					}
					else
					{
						// Vote on a random server
						VoteServer randomServer = getRandomServer();
						randomServer.vote(stateInput, candidateInput);

						// Spawn a new server for the state that doesn't exist
						servers.put(stateInput, new ArrayList<VoteServer>());
						servers.get(stateInput).add(new VoteServer(stateInput));
						
						// Broadcast new state
						broadcastState(randomServer.getState());
					}
				}
				// Get candidate results
				else if (userInput.equals("2"))
				{
					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();	

					int total = 0;

					// Spawn a channel for each state and grab the candidate
					Iterator<String> iter =  servers.keySet().iterator();
					
					while(iter.hasNext())
					{
						String state = iter.next();
						
						ArrayList<VoteServer> stateServers = servers.get(state);
						
						if (stateServers.size() > 0)
							total = total + stateServers.get(0).getResultsByCandidate(candidateInput);
					}
					
					System.out.println("National Results For Candidate " + candidateInput + ":  " + total + ".");

				}
				// Get results by state
				else if (userInput.equals("3"))
				{
					String tally = "No votes.";
					
					System.out.print("Select A State:  ");
					stateInput = br.readLine();				

					if (servers.containsKey(stateInput) && servers.get(stateInput).size() > 0)
						tally = servers.get(stateInput).get(0).getCandidatesByState();
					
					System.out.println("All Candidate Results In " + stateInput + ":  " + tally + ".");
				}
				// National results for all
				else if (userInput.equals("4"))
				{
					// Select a random server and get national results
					System.out.println("National Results:  " + getRandomServer().getNationalResults());
				}			
				else if (userInput.equals("5"))
				{
					// Randomly choose a server state group to crash
					getRandomServer().stop();
				}
			} while(!userInput.equals("6"));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Error during initial server initiation or during menu loop.");
		}
	}
}