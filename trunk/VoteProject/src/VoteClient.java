import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class VoteClient 
{
	//Data structure to keep track of servers
	public static Hashtable<String, ArrayList<VoteServer>> servers;
	
	public static void main(String[] args) 
	{		
		// Variables needed are defined here
		String userInput;
		String stateInput;
		String candidateInput;
		VoteServer clientServer = null;

		try 
		{
			//Create master server list
			servers = new Hashtable<String, ArrayList<VoteServer>>();

			// Get a reader to get client input from console
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// Start up 3 initial servers andadd initial servers to list
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
				System.out.println("(1)  Vote For Candidate");
				System.out.println("(2)  Results By Candidate");
				System.out.println("(3)  All Candidate Results By State");
				System.out.println("(4)  National Tally For All Candidates");
				System.out.println("(5)  Kill Random Server");
				System.out.println("(6)  Quit");
				System.out.println("---------------------------------");

				System.out.println();
				System.out.print("Select An Option: ");
				userInput = br.readLine();

				// Process based on user selection
				if (userInput.equals("1"))
				{
					System.out.print("Select A State:  ");
					stateInput = br.readLine();

					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();					

					//if the server group exists, use that group and vote
					if (servers.containsKey(stateInput))
					{
						//grab the oldest server
						if (servers.get(stateInput).size() > 0)
							servers.get(stateInput).get(0).vote(candidateInput);
					}
					else		//create the server and vote
					{
						VoteServer server = new VoteServer(stateInput);
						servers.put(stateInput, new ArrayList<VoteServer>());
						servers.get(stateInput).add(server);
						server.vote(candidateInput);						
					}
				}
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
				else if (userInput.equals("3"))
				{
					String tally = "No votes";
					
					System.out.print("Select A State:  ");
					stateInput = br.readLine();				

					if (servers.containsKey(stateInput))
						if (servers.get(stateInput).size() > 0)
							tally = servers.get(stateInput).get(0).getCandidatesByState();
					
					System.out.println("All Candidate Results In " + stateInput + ":  " + tally + ".");

				}
				else if (userInput.equals("4"))
				{
					Hashtable<String, Integer> nationalResults = new Hashtable<String, Integer>();					
					Iterator<ArrayList<VoteServer>> iter = servers.values().iterator();

					while(iter.hasNext())
					{
						ArrayList<VoteServer> stateServers = iter.next();
						
						if (stateServers.size() > 0)
						{
							Hashtable<String, Integer> tempHT = stateServers.get(0).getResultsByStateHT();
							
							// Iterate through our temp hash table and add to national results

							Iterator<String> iter2 = tempHT.keySet().iterator();

							while (iter2.hasNext())
							{
								String cand = iter2.next();
	
								if (nationalResults.containsKey(cand))
								{
									int candidateVoteCount = nationalResults.get(cand);
									candidateVoteCount = candidateVoteCount + tempHT.get(cand);
									nationalResults.put(cand, candidateVoteCount);   			
								}
								else
								{
									nationalResults.put(cand, tempHT.get(cand));
								}
							}								
						}
					}
					
					System.out.println("National Results:  " + nationalResults.toString());
				}			
				else if (userInput.equals("5"))
				{
					//Randomly choose a server state group to crash
					int random = (int) ( 0 + Math.random() * servers.size());
					ArrayList<VoteServer> stateServers = (ArrayList<VoteServer>)servers.values().toArray()[random];
					
					int randomServer = (int) ( 0 + Math.random() * stateServers.size());
					stateServers.get(randomServer).stop();
				}
			} while(!userInput.equals("6"));
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			System.out.println("Error during initial server initiation or during menu loop.");
		}
	}
	
	private static void DumpServers(Hashtable<String, ArrayList<VoteServer>> serverList)
	{
		Iterator<String> iter = serverList.keySet().iterator();
		
		while (iter.hasNext())
		{
			String state = iter.next();
			
			ArrayList<VoteServer> stateServers = serverList.get(state);
			
			for (int i = 0; i < stateServers.size(); i++)
			{
				VoteServer server = stateServers.get(i);
				System.out.println("State: " + state + " (" + i + ") : " + server.voteTally);
			}
		}
	}
}