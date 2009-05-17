import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class VoteClient 
{

	public static void main(String[] args) 
	{
		// Variables needed are defined here
		String userInput;
		String stateInput;
		String candidateInput;
		Hashtable<String, VoteServer> serverList;
		ArrayList<String> stateList = new ArrayList<String>();
		VoteServer clientServer = null;

		try 
		{
			// Create new server list
			serverList = new Hashtable<String, VoteServer>();

			// Get a reader to get client input from console
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// Start up 3 initial servers
			VoteServer server1 = new VoteServer("NJ");
			VoteServer server2 = new VoteServer("PA");
			VoteServer server3 = new VoteServer("NY");

			serverList.put("NJ", server1);
			serverList.put("PA", server2);
			serverList.put("NY", server3);

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

					// Add state to global state list
					if (! stateList.contains(stateInput))
						stateList.add(stateInput);

					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();					

					//if the server group exists, use that group and vote
					if (serverList.containsKey(stateInput))
					{
						clientServer = serverList.get(stateInput);
						clientServer.vote(candidateInput);
					}
					else		//create the server and vote
					{
						clientServer = new VoteServer(stateInput);
						clientServer.vote(candidateInput);
						serverList.put(stateInput, clientServer);						
					}
				}
				else if (userInput.equals("2"))
				{

					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();	

					int total = 0;

					// Spawn a channel for each state and grab the candidate
					for (int i = 0; i < stateList.size(); i++)
					{
						//if the server group exists, use that group
						if (serverList.containsKey(stateList.get(i)))
						{
							clientServer = serverList.get(stateList.get(i));
							total = total + clientServer.getResultsByCandidate(candidateInput);
						}
					}

					System.out.println("National Results For Candidate " + candidateInput + ":  " + total + ".");

				}
				else if (userInput.equals("3"))
				{
					String tally = "";
					
					System.out.print("Select A State:  ");
					stateInput = br.readLine();				

					// Get candidates for state
					//clientServer = new VoteServer(stateInput);
					
					if (serverList.containsKey(stateInput))
					{
						clientServer = serverList.get(stateInput);
						tally = clientServer.getCandidatesByState();					
					}
					else
						tally = "No votes";
					
					//serverList.add(clientServer);

					System.out.println("All Candidate Results In " + stateInput + ":  " + tally + ".");

				}
				else if (userInput.equals("4"))
				{

					Hashtable<String, Integer> nationalResults = new Hashtable<String, Integer>();

					// Spawn a channel for each state and grab the candidate
					for (int i = 0; i < stateList.size(); i++)
					{
						
						//if the server group exists, use that group
						if (serverList.containsKey(stateList.get(i)))
						{
							clientServer = serverList.get(stateList.get(i));
							Hashtable<String, Integer> tempHT = clientServer.getResultsByStateHT();
							
							// Iterate through our temp hash table and add to national results

							Iterator<String> iter = tempHT.keySet().iterator();

							while (iter.hasNext())
							{
								String cand = iter.next();
	
								if (nationalResults.containsKey(cand))
								{
									int candidateVoteCount = nationalResults.get(cand);
									candidateVoteCount = candidateVoteCount + tempHT.get(cand);
									nationalResults.put(cand, candidateVoteCount);   			
								}
								else
								{
									nationalResults.put(cand, 1);
								}
							}	
						}
					}

					System.out.println("National Results:  " + nationalResults.toString());
				}			
				else if (userInput.equals("5"))
				{
					// Randomly choose a server in the list to crash
					int random = (int) ( 0 + Math.random() * serverList.size());
					serverList.get(random).stop();
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