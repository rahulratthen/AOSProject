import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.*;

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
	
	////////Added by Viswam///////////
	private static int[] RN;
	/////////////////////////////////
	
	private static List<SctpClient> mClients = new ArrayList<SctpClient>();
	private static List<Thread> mClientThreads = new ArrayList<Thread>();
	
	public Application()
	{
		haveToken = false;
		isRequesting = false;
	}
	
	////////Added by Viswam/////////////////////////////////////////////////////////////////
	public String encodePrivelege(Queue<Integer> q, int[] arr)
	{
		String privelege = "p";
		
		while(!q.isEmpty())
		{
			privelege += q.remove() + ",";
		}
		
		privelege += "!";
		
		for(int i=0; i<arr.length; i++)
		{
			privelege += Integer.toString(arr[i]) + ",";
		}
		
		return privelege;
	}
	
	public Queue<Integer> getPrivelegeQueue(String message)
	{
		Queue<Integer> q = new LinkedList<Integer>();
		message = message.substring(1); //Remove the first letter 'p'
		
		String temp1[] = message.split("!"); //Queue and array separated by ! mark
		String temp2[] = temp1[0].split(",");//Take the queue alone
		
		for(int i=0; i<temp2.length; i++)
		{
			q.add(Integer.parseInt(temp2[i]));
		}
		
		return q;
	}
	
	public int[] getPrivelegeArray(String message)
	{
		message = message.substring(1); //Remove the first letter 'p'
		
		String temp1[] = message.split("!"); //Queue and array separated by ! mark
		String temp2[] = temp1[1].split(","); //Take the array alone
		
		int arrLength = temp2.length;
		int[] arr = new int[arrLength];
		
		for(int i=0; i<temp2.length; i++)
		{
			arr[i] = Integer.parseInt(temp2[i]);
		}
		
		return arr;
	}
	
	public void sendPrivelege(String message, int destID)
	{
		SocketAddress mSocketAddress = new InetSocketAddress(mConfigReader.getNodeConfig(destID)[1],Integer.parseInt(mConfigReader.getNodeConfig(destID)[2]));
		MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);
		
		try {

			SctpChannel mSctpChannel = SctpChannel.open();
			mSctpChannel.connect(mSocketAddress);
			ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			mByteBuffer.put(message.getBytes());
			mByteBuffer.flip();
			mSctpChannel.send(mByteBuffer,mMessageInfo);
			System.out.println("Sending token to "+ destID);
		} catch (Exception e) {
			System.out.println("Exception: " +  e);
				
		}
	}
	
	public void broadcastRequest(String message)
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
					mByteBuffer.put(message.getBytes());
					mByteBuffer.flip();
					mSctpChannel.send(mByteBuffer,mMessageInfo);
					System.out.println("Sending request to "+ i);
				} catch (Exception e) {
					System.out.println("Exception: " +  e);
						
				}
			}
			
			
		}
	}
	
	public void algorithmSendRequest()
	{
		isRequesting = true;
		
		if(!haveToken)
		{
			RN[mSelfNodeID] = RN[mSelfNodeID] + 1;
			broadcastRequest("encodedRequest");
			while(!haveToken){};
		}
		//enter CS after this
	}
	
	///////////////////End of edits by Viswam/////////////////////////////////
	
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
		
		/////////Debug code to test privelege encoding added by Viswam////////////////////
		Queue<Integer> testQueue = new LinkedList<Integer>();
		int[] LN = {10,9,8,7,6};
		for(int i=0; i<5; i++)
		{
			testQueue.add(i);
		}
		
		String privelege = app.encodePrivelege(testQueue,LN);
		Queue<Integer> decodedQueue = app.getPrivelegeQueue(privelege);
		System.out.println(decodedQueue);
		int[] decodedArray = app.getPrivelegeArray(privelege);
		System.out.println();
		for(int i=0; i<decodedArray.length; i++) System.out.print(Integer.toString(decodedArray[i]));
		
		/////////////////End of edits by Viswam////////////////////////////////////
		/********Commented by Viswam***********/
		// /* create server */
		// mServer = new SctpServer(mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
		// mServerThread = new Thread(mServer);
		// System.out.println("SctpVectorClock : Starting Server : "+args[0]+" at "+mConfigReader.getNodeConfig(mSelfNodeID)[1]+":"+mConfigReader.getNodeConfig(mSelfNodeID)[2]);
		// mServerThread.start();

		// app.applicationModule();
		/**************************************/
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
