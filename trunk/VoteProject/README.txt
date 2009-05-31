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
			----------log4j.properties (log4j configuration file)
			----------VoteServer.java  (implementation of voting server)
			----------VoteClient.java  (implementation of voting client)
			----------ServerState.java (representation of server state)



To Compile:  (Navigate to VoteProject directory)

			javac -classpath .;lib\jgroups-all.jar;lib\log4j.jar;lib\commons-logging.jar src\*.java


To Run:      (Navigate to VoteProject directory)

			java -cp .;lib\jgroups-all.jar;lib\log4j.jar;lib\commons-logging.jar;bin\ VoteClient




Log4J is configured in a way to show the WARN level.  This shows various pieces of information
about the JGroups configuration.  Modify the logging level appropriately in log4j.properties
should you want to see other types of information (DEBUG, ERROR, INFO, etc).

Note:   on our testing, the WARN level showed various messages such as:

[WARN] UDP: - failed to join /224.0.75.75:7500 on net2: java.net.SocketException: Unrecognized Windows Sockets error: 0: no Inet4Address associated with interface

We believe that this does not cause any impact on the application runtime.

In addition, since logging is enabled, it is causing log output to be interwoven with the console input prompts.  
This does not impact application runtime.  Simply type in the console your criteria and hit enter as though
the logging output was never there.



Instructions:

Upon running the application, the user will be presented with a menu style console
application.  Our architecture dictates that the voting client can access
servers directly.  All nodes are simulated on the same local PC, however, are 
differentiated by different port numbers.  The following options are available upon
running.

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

			To kill a random server, select this option, and the console will display (for example):
			
			Killing Server: 192.168.1.2:61087
			
			We implemented this using the DISCARD protocol.  Upon selection of this option,
			we modify the protocol stack such that the channel DISCARDS all traffic.  Upon detection
			that the heartbeat is dead, this channel will become suspect, and the following output will
			be displayed (for example):
			
			192.168.1.2:61089 has been alerted that 192.168.1.2:61087 is suspect!

			Typically, now, you should select option (6) to restart the failed server

(6)  Restart Failed Server

			This option will restart the failed server from option (5).  When the server group
			detects that the failed server is alive, the server will be shunned and automatically
			initiate the rejoin protocol.  Typical output from this option is shown below:
			
			23811 [WARN] FD: - I was suspected by 192.168.1.2:61091; ignoring the SUSPECT message and sending back a HEARTBEAT_ACK	
			192.168.1.2:61091 has been shunned.  Initiating rejoin protocol

(7)  Exit



Requirements Analysis (which is refernced in the java files as Requirement x)

1) Startup
	
	The application spawns three initial servers (NJ, PA, NY) implemented as JChannel objects
	extending ReceiverAdapter and implementing ChannelListener classes from the JGroups API

2) Dynamic Membership 1

	
	When a vote is cast, the application checks the cluster to determine if any do not represent
	the desired state, the vote is accepted by the oldest server, and a new server is spawned
	and joins the cluster, through the JGroups connect API call

3) State Transfer

	As a new server joins the cluster, the server calls the JGroups methods getState and
	setState.  These implementations serialize the state session objects which are
	broadcast to all members of the cluster through the JGroups framework

4) Dynamic Membership 2

	After a vote is collected, the server that was voted on will serialize the global state
	and initiate a state transfer as described in (3)

5) Dynamic Membership 3

	The JChannel object allows customization of the protocol stack in the constructor
	of the object.  We specify the PING and FD protocols which are implemented
	in the JGroups stack and protocol framework.  The framework will initiate
	heartbeats and responses.

6) Simulation of Server Failure

	Simulation of a failure is achieved by modifying the DIsCARD protocol such that
	all packets are dropped.  The JGroups API detects the failure and marks
	the server as SUSPECT and the server is expelled from the group.

7) Dynamic Membership 4

	After the server has become SUSPECT, we can bring it back to life by
	modifying the DISCARD protocol to allow all traffic to be seen on the
	failed node.  The JGroups framework will detect that the node is alive
	and he will be shunned.  We configured the JChannel objects to have
	an AUTO_RECONNECT and AUTO_GETSTATE property.  Upon being shunned
	the recovered server will initiate the rejoin protocol and 
	return to the cluster and sync up his state.
	
	
Experience

	The JGroups API was found to be extremely useful in creating robust,
	distributed and redundant applications.  The API provided a rich
	framework which aided the software development by implementing
	a seamless GMS interface, which allows complete customization
	by a software developer.  It drastically reduced development time
	since we did not need to write any of the implementation of the
	protocols.  
	
	I think that the JGroups API would be extremely useful for 
	applications demanding high availability and reliability.  After
	using the API for this home assignment, we appreciate the benefits
	of such systems.  
	
	I would have thought that JGroups would be more popular than it
	seems to be.  Several technical roadbloacks, such as the heartbeat
	requirements, were encountered during development.  The only
	real technical leads found were directly from the JGroups
	manual and tutorial.  There were not many stand alone
	forums found, and the newsgroup was not too lively.  However,
	upon trial and error, the API was found to be very detailed
	and easy to use.  I would recommend it to anyone needing to perform
	tasks such as this one.