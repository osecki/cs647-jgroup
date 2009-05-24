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
				Thread.sleep(1000);
				server.ping();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			} 
			catch (ChannelNotConnectedException e) 
			{
				e.printStackTrace();
			} 
			catch (ChannelClosedException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
