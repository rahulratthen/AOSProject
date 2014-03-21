import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class Application 
{
	//int nodeID;
	boolean haveToken;
	boolean isRequesting;
	
	public static int MESSAGE_SIZE = 128;

	private static int mSelfNodeID = 0;
	private static String mConfigFile = null;
	private static ConfigReader mConfigReader = null;
	private static SctpServer mServer = null;
	private static Thread mServerThread = null;
	//private static VectorClock mSelfClock = null;

	private static List<SctpClient> mClients = new ArrayList<SctpClient>();
	private static List<Thread> mClientThreads = new ArrayList<Thread>();
	
	public Application()
	{
		haveToken = false;
		isRequesting = false;
	}
	
	public void applicationModule()
	{
		
		Random random_delay = new Random();
		while(true)
		{
			try 
			{
				Thread.sleep(random_delay.nextInt(3000));
				csEnter();
				
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
	}

	private void csEnter() 
	{
		for(int i=0; i< mConfigReader.getNodeCount();i++)
		{
			if(i!= mSelfNodeID)
			{
				SocketAddress mSocketAddress = new InetSocketAddress(mConfigReader.getNodeConfig(i)[1],Integer.parseInt(mConfigReader.getNodeConfig(i)[2]));
				MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);
				
				try {

					SctpChannel mSctpChannel = SctpChannel.open();
					mSctpChannel.connect(mSocketAddress);
					ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
					mByteBuffer.put("test".getBytes());
					mByteBuffer.flip();
					mSctpChannel.send(mByteBuffer,mMessageInfo);
					//System.out.println("SctpClient "+mServerNodeID+" : Send : "+mMessage.toString());
					System.out.println("SctpClient sending to "+ i);
					//break;
				} catch (Exception e) {
					System.out.println("Exception: " +  e);
						
				}
			}
			
			
		}
		
	}
	
	public static void main(String[] args) 
	{
		Application app = new Application();
		mSelfNodeID = Integer.parseInt(args[0]);
		mConfigFile = args[1];
		mConfigReader = new ConfigReader(mConfigFile);
		
		/* create server */
		mServer = new SctpServer(mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
		mServerThread = new Thread(mServer);
		System.out.println("SctpVectorClock : Starting Server : "+args[0]+" at "+mConfigReader.getNodeConfig(mSelfNodeID)[1]+":"+mConfigReader.getNodeConfig(mSelfNodeID)[2]);
		mServerThread.start();

		app.applicationModule();
		
		/* create clients */
//		int skipped = 0;
//		for (int i = 0; i < mConfigReader.getNodeCount(); i++) {
//			if (i == mSelfNodeID) {
//				skipped = 1;
//				continue;
//			} else {
//				System.out.println("SctpVectorClock : Starting Client : "+mConfigReader.getNodeConfig(i)[0]+" at "+mConfigReader.getNodeConfig(i)[1]+":"+mConfigReader.getNodeConfig(i)[2]);
//				mClients.add(new SctpClient(args[0], mConfigReader.getNodeConfig(i)[0], mConfigReader.getNodeConfig(i)[1], mConfigReader.getNodeConfig(i)[2]));
//				mClientThreads.add(new Thread(mClients.get(i - skipped)));
//				mClientThreads.get(i - skipped).start();
//			}
//		}

	}


	



}
