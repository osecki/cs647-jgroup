import org.jgroups.ChannelClosedException;
import org.jgroups.ChannelNotConnectedException;
//import org.jgroups.JChannel;

public class HealthCheck extends Thread
{
	private VoteServer server;
	public boolean isActive;
	
	public void setServer(VoteServer serv)
	{
		server = serv;
	}
	
	public void run()
	{
		while(true)
		{
			try 
			{
				Thread.sleep(5000);			//sleep
				server.ping();				//send ping
				Thread.sleep(5000);			//sleep
				server.checkPongResponses();
			} 
			catch (Exception e) 
			{
			}
		}
	}
}
