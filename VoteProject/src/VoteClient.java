import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;

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
				}
				else if (userInput.equals("2"))
				{
					//implement
					//is this a national tally for a particular candidate?
					
/*
					System.out.println();
					System.out.print("Select A Candidate:");
					candidateInput = br.readLine();
					System.out.println(new VoteServer().getResultsByCandidate(candidateInput));
*/
				}
				else if (userInput.equals("3"))
				{
					System.out.print("Select A State: ");
					stateInput = br.readLine();				
					
					//Get candidates for state
					clientServer = new VoteServer(stateInput);
					String tally = clientServer.getCandidatesByState();					
					System.out.println("All Candidate Results In " + stateInput + " : " + tally);
				}
				else if (userInput.equals("4"))
				{
					//implement
					//should I iterate a list of all states and open channels and 
					//aggregate here?
				}			
				else if (userInput.equals("5"))
				{
					//implement
				}
    		} while(!userInput.equals("6"));
		
    		//stop
    		clientServer.stop();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
