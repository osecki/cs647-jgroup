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



