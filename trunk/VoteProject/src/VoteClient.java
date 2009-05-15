import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class VoteClient 
{

	public static void main(String[] args) 
	{
		String userInput;
		String stateInput;
		String candidateInput;
		ArrayList<VoteServer> serverList;
		ArrayList<String> stateList = new ArrayList<String>();
		VoteServer clientServer = null;
		
		try 
		{
			//Create new server list
			serverList = new ArrayList<VoteServer>();
			
			//Get a reader to get client input from console
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						
			//Start up 3 initial servers
			VoteServer server1 = new VoteServer("NJ");
			VoteServer server2 = new VoteServer("NJ");
			VoteServer server3 = new VoteServer("NJ");
			
			server1.start();
			server2.start();
			server3.start();
			
			serverList.add(server1);
			serverList.add(server2);
			serverList.add(server3);
			
    		do
    		{
				System.out.println("\n\n----------- Menu ------------");
				System.out.println("(1)  Vote For Candidate");
				System.out.println("(2)  Results By Candidate");
				System.out.println("(3)  All Candidate Results By State");
				System.out.println("(4)  National Tally For All Candidates");
				System.out.println("(5)  Kill Random Server");
				System.out.println("(6)  Quit");
		
				System.out.println();
				System.out.print("Select An Option: ");
				userInput = br.readLine();

				//Process based on user selection
				if (userInput.equals("1"))
				{
					System.out.print("Select A State: ");
					stateInput = br.readLine();
		
					//add state to global state list
					if (!stateList.contains(stateInput))
						stateList.add(stateInput);
					
					System.out.print("Select A Candidate: ");
					candidateInput = br.readLine();					
					
					//Vote
					clientServer = new VoteServer(stateInput);
					clientServer.vote(candidateInput);
					serverList.add(clientServer);
				}
				else if (userInput.equals("2"))
				{
					System.out.print("Select A Candidate: ");
					candidateInput = br.readLine();	
					
					int total = 0;
					
					//spawn a channel for each state and grab the candidate
					for (int i = 0; i < stateList.size(); i++)
					{
						clientServer = new VoteServer(stateList.get(i));
						total = total + clientServer.getResultsByCandidate(candidateInput);
						serverList.add(clientServer);
					}
					
					System.out.println("National Results For Candidate " + candidateInput + " : " + total);
				}
				else if (userInput.equals("3"))
				{
					System.out.print("Select A State: ");
					stateInput = br.readLine();				
					
					//Get candidates for state
					clientServer = new VoteServer(stateInput);
					String tally = clientServer.getCandidatesByState();					
					serverList.add(clientServer);
					
					System.out.println("All Candidate Results In " + stateInput + " : " + tally);
					
				}
				else if (userInput.equals("4"))
				{
					Hashtable<String, Integer> nationalResults = new Hashtable<String, Integer>();
					
					//spawn a channel for each state and grab the candidate
					for (int i = 0; i < stateList.size(); i++)
					{
						clientServer = new VoteServer(stateList.get(i));
						Hashtable tempHT = clientServer.getResultsByStateHT();
						serverList.add(clientServer);
						
						
						//iterate through our temp hash table and add to national results
						
						Iterator<String> iter = tempHT.keySet().iterator();
						
						while (iter.hasNext())
						{
							String cand = iter.next();
							
							if (nationalResults.containsKey(cand))
							{
								int candidateVoteCount = nationalResults.get(cand);
								candidateVoteCount = candidateVoteCount + 1;
								nationalResults.put(cand, candidateVoteCount);   			
							}
							else
							{
								nationalResults.put(cand, 1);
							}
						}	
					}
					
					System.out.println("National Results : " + nationalResults.toString());
				}			
				else if (userInput.equals("5"))
				{
					int random = (int) ( 0 + Math.random() * serverList.size());
					serverList.get(random).stop();
				}
    		} while(!userInput.equals("6"));
		
    		//stop servers
    		
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
