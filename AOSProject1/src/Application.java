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
	boolean havePrivilege;
	boolean isRequesting;
	boolean finishedCS;
	boolean timerExpired;
	boolean requestReceived;
	
	public static int MESSAGE_SIZE = 128;

	private static int mSelfNodeID = 0;
	private static String mConfigFile = null;
	private static ConfigReader mConfigReader = null;
	private static SctpServer mServer = null;
	private static Thread mServerThread = null;

	private static List<SctpClient> mClients = new ArrayList<SctpClient>();
	private static List<Thread> mClientThreads = new ArrayList<Thread>();


	ArrayList<Integer> RN = new ArrayList<Integer>();
	ArrayList<Integer> LN = new ArrayList<Integer>();
	Queue<Integer> requestQueue = new LinkedList<Integer>();
	int seqNum = -1;
	int lastRequest = -1;
	int csCount = 0;

	public Application()
	{
		havePrivilege = false;
		isRequesting = false;
		timerExpired = false;
		//requestReceived = false;
		//finishedCS = true;
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
		}

	}
	
	public void updateTimer()
	{
		timerExpired = true;
	}

	public void applicationModule()
	{
		while(csCount<10)
		{
			int test = 0;
			if(timerExpired)
			{
				try 
				{
					timerExpired = false;
					csEnter();
					CriticalSection();
					csLeave();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			
			else if(requestReceived)
			{
				respondRequest();
				requestReceived = false;
			}
			
			
		}
		while(havePrivilege)
		{
			int asd = 0;
			if(requestReceived)
			{
				respondRequest();
				requestReceived = false;
			}
			if(csCount<11)
				break;
		}
	}
	
	//Request(j,n)
	//j - Node number
	//n - Sequence number
	public void processRequestMessage(String msg)
	{
		int j,n;
		j = getRequestID(msg);
		n = getRequestSequence(msg);

		RN.set(j,new Integer(Math.max(RN.get(j), n)));
	}
	
	public void respondRequest()
	{
		
		if((havePrivilege)&&(!isRequesting)&&(RN.get(lastRequest)== LN.get(lastRequest) + 1))
		{
			havePrivilege = false;

			String encodedPrivilegeMsg = encodePrivelege(requestQueue,LN);
			sendPrivelege(encodedPrivilegeMsg, lastRequest);
			SocketAddress mSocketAddress = new InetSocketAddress(mConfigReader.getNodeConfig(lastRequest)[1],Integer.parseInt(mConfigReader.getNodeConfig(lastRequest)[2]));
			MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);

			try {

				SctpChannel mSctpChannel = SctpChannel.open();
				mSctpChannel.connect(mSocketAddress);
				ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
				mByteBuffer.put(encodedPrivilegeMsg.getBytes());
				mByteBuffer.flip();
				mSctpChannel.send(mByteBuffer,mMessageInfo);
				System.out.println("Privilege Message sending to "+ lastRequest);
			} catch (Exception e) {
				System.out.println("Exception: " +  e);

			}
		}

	}

	private void CriticalSection()
	{
		csCount++;
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
			e.printStackTrace();
		}

		
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
			havePrivilege = false;
			int headElement = (Integer)requestQueue.remove();

			String msg = encodePrivelege(requestQueue,LN);
			sendPrivelege(msg, headElement);
		}

		isRequesting = false;
		finishedCS = true;
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
				} catch (Exception e) {
					System.out.println("Exception: " +  e);

				}
			}


		}

		app.applicationModule();
		
		app.testCorrectness();
		
		System.exit(0);

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
