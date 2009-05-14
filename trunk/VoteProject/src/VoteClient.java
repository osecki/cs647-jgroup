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
		ArrayList<String> stateList = new ArrayList<String>();
		
		try 
		{
			//Get a reader to get client input from console
    		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						
			//Start up 3 initial servers
			VoteServer server1 = new VoteServer("NJ");
			VoteServer server2= new VoteServer("NJ");
			VoteServer server3= new VoteServer("NJ");
			
			server1.start();
			server2.start();
			server3.start();
			
    		do
    		{
				System.out.println("\n\n----------- Menu ------------");
				System.out.println("(1)  Vote For Candidate");
				System.out.println("(2)  Results By Candidate");
				System.out.println("(3)  All Candidate Results By State");
				System.out.println("(4)  Results By State");
				System.out.println("(5)  Results");
		
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
					new VoteServer(stateInput).vote(candidateInput);
				}
				else if (userInput.equals("2"))
				{
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
					String tally = new VoteServer(stateInput).getCandidatesByState();					
					System.out.println("All Candidate Results In " + stateInput + " : " + tally);
				}
				else if (userInput.equals("4"))
				{
					
				}
    		} while(!userInput.equals("5"));
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
