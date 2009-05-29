import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;

public class VoteClient 
{
	// Data structure to keep track of servers
	public static LinkedList<VoteServer> servers;
	private static VoteServer failedServer = null;
	
	
	
	public static boolean stateServerExists(String state)
	{
		boolean ret = false;
		
		Iterator<VoteServer> iter = servers.iterator();
		
		while(iter.hasNext())
		{
			VoteServer server = iter.next();
			
			if (server.getStateName().equals(state))
			{
				ret = true;
				break;
			}
		}
		
		return ret;
	}
	
	public static VoteServer getOldestStateServer(String state)
	{
		VoteServer voteServer = null;
		
		Iterator<VoteServer> iter = servers.iterator();
		
		while(iter.hasNext())
		{
			VoteServer server = iter.next();
			
			if (server.getStateName().equals(state))
			{
				voteServer = server;
				break;
			}
		}
		
		return voteServer;		
	}
	
	public static VoteServer getOldestStateServer()
	{
		VoteServer voteServer = null;
		
		if (servers.size() > 0)
			voteServer = servers.getFirst();
		
		return voteServer;
	}
	
	public static VoteServer getRandomServer()
	{
		VoteServer voteServer = null;
		
		int random = (int) ( 0 + Math.random() * servers.size());
		voteServer = servers.get(random);
		
		return voteServer;
	}
	
	
	public static void main(String[] args) 
	{		
		// Variables needed are defined here
		String userInput;
		String stateInput;
		String candidateInput;
		int voterID;
		
		try
		{
			//Create master server list
			servers = new LinkedList<VoteServer>();
			
			// Get a reader to get client input from console
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// Start up 3 initial servers and add initial servers to list
			servers.add(new VoteServer("NJ"));
			servers.add(new VoteServer("PA"));
			servers.add(new VoteServer("NY"));

			// Loop which runs the menu for the client until quit is chosen
			do
			{
				System.out.println("\n\n----------- Menu ------------");
				System.out.println("(1) Vote For Candidate");
				System.out.println("(2) Get Results For A Particular Candidate");
				System.out.println("(3) Get All Candidate Results For A State");
				System.out.println("(4) Get National Tally For All Candidates");
				System.out.println("(5) Kill A Random Server");
				System.out.println("(6) Restart Failed Server");
				System.out.println("(7) Exit");
				System.out.println("---------------------------------");
				System.out.println();
				System.out.print("Select An Option: ");
				userInput = br.readLine();

				// Vote
				if (userInput.equals("1"))
				{
					System.out.print("Enter Voter ID: ");
					voterID = Integer.parseInt(br.readLine());
					System.out.print("Select A State:  ");
					stateInput = br.readLine();
					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();					

					// If the server group exists
					if (stateServerExists(stateInput))
					{
						// Grab the oldest server in that state and vote
						getOldestStateServer(stateInput).vote(voterID, stateInput, candidateInput);
					}
					else
					{
						// Vote on a random server
						getOldestStateServer().vote(voterID, stateInput, candidateInput);
						
						// Spawn a new server for the state that doesn't exist
						servers.add(new VoteServer(stateInput));
					}
				}				
				else if (userInput.equals("2"))
				{
					System.out.print("Select A Candidate:  ");
					candidateInput = br.readLine();	

					int results = getOldestStateServer().getResultsByCandidate(candidateInput);
					System.out.println("National Results For Candidate " + candidateInput + ":  " + results + ".");
				}
				else if (userInput.equals("3"))
				{
					String tally = "No votes.";
					
					System.out.print("Select A State:  ");
					stateInput = br.readLine();				

					if (stateServerExists(stateInput))
						tally = getOldestStateServer(stateInput).getCandidatesByState();					
					
					System.out.println("All Candidate Results In " + stateInput + ":  " + tally + ".");					
				}
				else if (userInput.equals("4"))
				{
					System.out.println("National Results:  " + getOldestStateServer().getNationalResults());					
				}
				else if (userInput.equals("5"))
				{
					failedServer = getRandomServer();
					
					System.out.println("Killing Server: " + failedServer.getAddress().toString());
					
					failedServer.stopHealthCheck();
				}
				else if (userInput.equals("6"))
				{
					failedServer.readmit();
				}
			} while(!userInput.equals("7"));	
		}
		catch(Exception ex)
		{
			
		}
	}
}