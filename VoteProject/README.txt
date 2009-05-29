Omar Badran, Jordan Osecki, Bill Shaya
CS 647 JGroups Assignment


This program has been tested on Windows Vista, using Java JDK 1.6.0_11


File Structure:
			
			VoteProject
			-----bin
			----------(binary output)
			-----lib
			----------commons-logging.jar
			----------jgroups-all.jar
			----------log4j.jar
			-----src
			----------VoteServer.java  (implementation of voting server)
			----------VoteClient.java  (implementation of voting client)
			----------ServerState.java (representation of server state)



To Compile:  (Navigate to VoteProject directory)

			javac -classpath .;lib\jgroups-all.jar;lib\log4j.jar;lib\commons-logging.jar src\*.java


To Run:      (Navigate to VoteProject directory)

			java -cp .;lib\jgroups-all.jar;lib\log4j.jar;lib\commons-logging.jar;bin\ VoteClient



Instructions:

Upon running the application, the user will be presented with a menu style console
application.  The user will be presented with the following options:

(1)  Vote For Candidate

			Upon selection, the user must enter:
			
				Voter ID:  represents an individual voter (ex:  1)
				State:     represents the state we are voting in (ex:  PA)
				Candidate: represents the candidate we are voting for (ex:  Obama)
				
			The application could have provided a configuration file which maps
			voter IDs to States, such that the only parameters into the vote call are
			State and Candidate, however, we provide the input of Vote ID for 
			flexibility.  Upon casting a vote, the receiving server will check the
			global state to ensure that the vote is from a unique voter ID, else
			the vote will be ignored.  In addition, the server will add the vote
			details to a global tally state object.  If a voter from a state votes
			where a state server has not been spawned yet, a random server will accept
			the vote, and the new state server will be spawned.

(2)  Get Results For A Particular Candidate

			Upon selection, the user must enter:
			
				Candidate: represents the candidate we are voting for (ex:  Obama)
				
			The application will tally votes from every state for a given candidate.

(3)  Get All Candidate Results For A State

			Upon selection, the user must enter:
			
				State:     represents the state we are voting in (ex:  PA)

			The application will display all candidates totals from a given state.

(4)  Get National Tally For All Candidates

			The application will display the global vote talley for all candidates 
			from every state.

(5)  Kill A Random Server

(6)  Restart Failed Server

(7)  Exit

