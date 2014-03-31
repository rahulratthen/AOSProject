import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

import com.sun.nio.sctp.MessageInfo;
import com.sun.nio.sctp.SctpChannel;

public class Application 
{
	//int nodeID;
	boolean havePrivilege;
	boolean isRequesting;
	boolean finishedCS;
	boolean timerExpired;

	public static int MESSAGE_SIZE = 128;

	private static int mSelfNodeID = 0;
	private static String mConfigFile = null;
	private static ConfigReader mConfigReader = null;
	private static SctpServer mServer = null;
	private static Thread mServerThread = null;
	//private static VectorClock mSelfClock = null;

	private static List<SctpClient> mClients = new ArrayList<SctpClient>();
	private static List<Thread> mClientThreads = new ArrayList<Thread>();


	ArrayList<Integer> RN = new ArrayList<Integer>();
	ArrayList<Integer> LN = new ArrayList<Integer>();
	Queue<Integer> requestQueue = new LinkedList<Integer>();
	int seqNum = -1;

	public Application()
	{
		havePrivilege = false;
		isRequesting = false;
		timerExpired = false;

	}

	public void initializeArrays()
	{
		for(int i=0;i<mConfigReader.getNodeCount();i++)
		{
			RN.add(new Integer(-1));
			LN.add(new Integer(-1));
		}
		if(mSelfNodeID == 0)
		{
			havePrivilege = true;
			havePrivilege = true;
		}

	}
	
	public void updateTimer()
	{
		System.out.println("Timer updated");
		timerExpired = true;
	}

	public void applicationModule()
	{
		System.out.println("Entered app");
		Random random_delay = new Random();
		while(seqNum<5)
		{
			if(timerExpired) System.out.println("Allowed to enter");
			while(timerExpired && seqNum<5)
			{
				System.out.println("Allowed to enter "+seqNum);
				try 
				{
					timerExpired = false;
					//Thread.sleep(2000);
					csEnter();
					csLeave();
	
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}

	}


	//Rahul
	public void processRequestMessage(String msg)
	{
		//Request(j,n)
		//j - Node number
		//n - Sequence number
		//call function to get values of j and n
		System.out.println("Entered processRequestMessage");
		int j,n;
		j = getRequestID(msg);
		n = getRequestSequence(msg);

		RN.set(j,new Integer(Math.max(RN.get(j), n)));
		//RN[j] = Math.max(RN[j], n);
		// System.out.println("Privilege : "+havePrivilege+ " Requesting: "+isRequesting + "Rn[j] : "+RN.get(j)+ "Ln[j]: "+LN.get(j));
//		if((havePrivilege)&&(!isRequesting)&&(RN.get(j)== LN.get(j) + 1))
//		{
//			havePrivilege = false;
//
//			String encodedPrivilegeMsg = encodePrivelege(requestQueue,LN);
//			sendPrivelege(encodedPrivilegeMsg, j);
//			SocketAddress mSocketAddress = new InetSocketAddress(mConfigReader.getNodeConfig(j)[1],Integer.parseInt(mConfigReader.getNodeConfig(j)[2]));
//			MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);
//
//			try {
//
//				SctpChannel mSctpChannel = SctpChannel.open();
//				mSctpChannel.connect(mSocketAddress);
//				ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
//				mByteBuffer.put(encodedPrivilegeMsg.getBytes());
//				mByteBuffer.flip();
//				mSctpChannel.send(mByteBuffer,mMessageInfo);
//				//System.out.println("SctpClient "+mServerNodeID+" : Send : "+mMessage.toString());
//				System.out.println("Privilege Message sending to "+ j);
//				//break;
//			} catch (Exception e) {
//				System.out.println("Exception: " +  e);
//
//			}
//		}

	}

	//Rahul
	private void CriticalSection()
	{
		//do some activity in Cs. Write to Log file
		Date d = new Date();
		System.out.println("Node "+ mSelfNodeID + "entering Cs at "+d.getTime());
		try {
			File file  = new File("cstest.txt");
			FileWriter fw = new FileWriter(file,true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(mSelfNodeID + "e");
			bw.close();
			Thread.sleep(1000);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		finishedCS = true;
	}

	private boolean elementInQueue(Queue que , int element)
	{
		Iterator iterator = que.iterator();
		while(iterator.hasNext())
		{
			int e = (int) iterator.next();
			if(e == element)
			{
				return true;
			}
		}
		return false;

	}

	//Rahul
	private void csLeave()
	{
		ExitCS();
	}

	public int testCorrectness()
	{
		
	    try {
	    	BufferedReader br = new BufferedReader(new FileReader("cstest.txt"));
	        String line1 = br.readLine();
	        while(line1!=null)
	        {
	        	String line2 = br.readLine();
	        	if(line2!=null)
	        	{
	        		if(!((line1.charAt(0) == line2.charAt(0)) && (line1.charAt(1) != line2.charAt(1)) ))
	        		{
	        			System.out.println("Oops!!");
	        			return 0;
	        		}
	        	}
	        	line1 = br.readLine();
	        }
	        
	        System.out.println("Works!!");
	        
	    }
	    catch(Exception e)
	    {
	    	System.out.println(e);
	    }
	    return 1;
	}

	private void csEnter() 
	{
		algorithmSendRequest();

	}

	private void ExitCS()
	{
		finishedCS = true;
		try{
		File file  = new File("cstest.txt");
		FileWriter fw = new FileWriter(file,true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("\n"+mSelfNodeID + "x\n");
		bw.close();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
		System.out.println("Node "+mSelfNodeID+" exiting CS");
		LN.set(mSelfNodeID, RN.get(mSelfNodeID));
		for(int i=0; i< mConfigReader.getNodeCount();i++)
		{
			if(i!= mSelfNodeID)
			{
				if(!elementInQueue(requestQueue,i) && (RN.get(i)== LN.get(i) + 1))
				{
					requestQueue.add(i);
				}

			}
		}

		if(!requestQueue.isEmpty())
		{
			System.out.println("asd");
			havePrivilege = false;
			havePrivilege = false;
			int headElement = (Integer)requestQueue.remove();

			String msg = encodePrivelege(requestQueue,LN);
			sendPrivelege(msg, headElement);
			//			SocketAddress mSocketAddress = new InetSocketAddress(mConfigReader.getNodeConfig(headElement)[1],Integer.parseInt(mConfigReader.getNodeConfig(headElement)[2]));
			//			MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);
			//			
			//			try {
			//
			//				SctpChannel mSctpChannel = SctpChannel.open();
			//				mSctpChannel.connect(mSocketAddress);
			//				ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			//				mByteBuffer.put(msg.getBytes());
			//				mByteBuffer.flip();
			//				mSctpChannel.send(mByteBuffer,mMessageInfo);
			//				//System.out.println("SctpClient "+mServerNodeID+" : Send : "+mMessage.toString());
			//				System.out.println("SctpClient sending Privilege msg to "+ headElement);
			//				
			//			} catch (Exception e) {
			//				System.out.println("Exception: " +  e);
			//					
			//			}
		}

		isRequesting = false;


	}

	public static void main(String[] args) 
	{
		Application app = new Application();

		mSelfNodeID = Integer.parseInt(args[0]);
		mConfigFile = args[1];
		mConfigReader = new ConfigReader(mConfigFile);
		app.initializeArrays();
		/* create server */
		mServer = new SctpServer(app,mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
		mServerThread = new Thread(mServer);
		System.out.println("SctpVectorClock : Starting Server : "+args[0]+" at "+mConfigReader.getNodeConfig(mSelfNodeID)[1]+":"+mConfigReader.getNodeConfig(mSelfNodeID)[2]);
		mServerThread.start();

		TimerThread timer = new TimerThread(app);
		new Thread(timer).start();
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

		app.applicationModule();
		
		app.testCorrectness();

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
	public String encodePrivelege(Queue<Integer> q, ArrayList<Integer> arr)
	{
		String privelege = "p";

		while(!q.isEmpty())
		{
			privelege += q.remove() + ",";
		}

		privelege += "!";

		for(int i=0; i<arr.size(); i++)
		{
			privelege += Integer.toString(arr.get(i)) + ",";
		}

		return privelege;
	}

	public Queue<Integer> getPrivelegeQueue(String message)
	{
		Queue<Integer> q = new LinkedList<Integer>();
		message = message.substring(1); //Remove the first letter 'p'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark
		//System.out.println(temp1[0].length());

		if(temp1[0].length() > 0)
		{
			String temp2[] = temp1[0].split(",");//Take the queue alone

			for(int i=0; i<temp2.length; i++)
			{
				q.add(Integer.parseInt(temp2[i].trim()));
			}
		}


		return q;
	}

	public ArrayList<Integer> getPrivelegeArray(String message)
	{
		message = message.substring(1); //Remove the first letter 'p'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark
		String temp2[] = temp1[1].split(","); //Take the array alone

		int arrLength = temp2.length;
		//System.out.println(arrLength);
		ArrayList<Integer> arr = new ArrayList<Integer>();

		for(int i=0; i<temp2.length-1; i++)
		{
			arr.add(Integer.parseInt(temp2[i].trim()));
		}

		return arr;
	}

	public void sendPrivelege(String message, int destID)
	{
		isRequesting = false;
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

	public void updateLocal(String prevMsg)
	{
		havePrivilege = true;
		requestQueue = getPrivelegeQueue(prevMsg);
		LN = getPrivelegeArray(prevMsg);
		System.out.println("Updated local");
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
					System.out.println("Sending request "+ message + "to " + i);
				} catch (Exception e) {
					System.out.println("Exception: " +  e);

				}
			}


		}
	}

	public void algorithmSendRequest()
	{
		isRequesting = true;
		finishedCS = false;
		if(!havePrivilege)
		{
			//System.out.println(RN.size()+ LN.size());
			int temp = RN.get(mSelfNodeID);
			temp+= 1;
			RN.set(mSelfNodeID, temp);
			broadcastRequest(encodeRequest(mSelfNodeID,++seqNum));
			while(!havePrivilege){System.out.print(".");};
		}
		CriticalSection();
	}


	public String encodeRequest(int j, int n)
	{
		String request = "r";

		request += Integer.toString(j);

		request += "!";

		request += Integer.toString(n);

		return request;
	}

	public int getRequestID(String message)
	{
		message = message.substring(1); //Remove the first letter 'r'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark

		int id = Integer.parseInt(temp1[0]);
		return id;
	}

	public int getRequestSequence(String message)
	{
		message = message.substring(1); //Remove the first letter 'r'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark

		int seq = Integer.parseInt(temp1[1].trim());
		return seq;
	}



}
