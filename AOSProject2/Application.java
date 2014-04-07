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
	private static ConfigReader mConfigReader = null; //Class used to read the config text file
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

	/**
	 * constructor
	 */
	public Application()
	{
		havePrivilege = false;
		isRequesting = false;
		timerExpired = false;
		//requestReceived = false;
		//finishedCS = true;
	}

	/**
	 * initializes the arrays RN and LN to -1
	 */
	public void initializeArrays()
	{
		for(int i=0;i<mConfigReader.getNodeCount();i++)
		{
			RN.add(new Integer(-1));
			LN.add(new Integer(-1));
		}
		
		//Node 0 starts with the token
		if(mSelfNodeID == 0)
		{
			havePrivilege = true;
			try {
				File file  = new File("cstest.txt");
				FileWriter fw = new FileWriter(file,false);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.close();
			}
			catch(Exception e)
			{
				
			}
		}

	}
	
	/**
	 * Method to indicate that it is time to request entry into critical section
	 */
	public void updateTimer()
	{
		timerExpired = true;
	}
	
	/**
	 * Application module where node enters and exits CS
	 */
	public void applicationModule()
	{
		while(csCount<50) //Loop until n requests are satisfied
		{
			//if(mSelfNodeID == 1 && csCount == 20)
				//break;
			int test = 0;
			if(timerExpired)
			{
				try 
				{
					timerExpired = false;
					csEnter(); //Algorithm module to request entry into CS
					CriticalSection(); //Enter CS
					csLeave(); //Algorithm module to handle token upon exit
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			
			//If request for token was received
			else if(requestReceived)
			{
				respondRequest();
				requestReceived = false;
			}
			
			
		}
		
		//After node is done, ensure token is passed on before terminating
		while(havePrivilege)
		{
			int asd = 0;
			if(requestReceived)
			{
				respondRequest();
				requestReceived = false;
				break;
			}
			//if(csCount<51)
				
		}
	}
	
	/**
	 * Process received Request(j,n) message where j - Node number, n - Sequence number
	 * @param msg
	 */
	public void processRequestMessage(String msg)
	{
		int j,n;
		j = getRequestID(msg);
		n = getRequestSequence(msg);
		
		//Update RN array according to algorithm
		RN.set(j,new Integer(Math.max(RN.get(j), n)));
	}
	
	/**
	 * responds to the pending requests in the queue
	 */
	public void respondRequest()
	{
		//Follow algorithm procedure to send token if necessary
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

	/**
	 * Critical Section
	 */
	private void CriticalSection()
	{
		csCount++; //Count number of satisfied CS entry requests
		
		//Do some activity in Cs. Write to Log file
		Date d = new Date();
		System.out.println("Node "+ mSelfNodeID + "entering Cs at "+d.getTime());
		try {
			File file  = new File("cstest.txt");
			FileWriter fw = new FileWriter(file,true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(mSelfNodeID + "e");
			bw.close();
			Thread.sleep(500);
			
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
	
	/**
	 * Check if given node ID is in request queue
	 * @param que
	 * @param element
	 * @return
	 */
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

	/**
	 * exits from the critical section
	 */
	private void csLeave()
	{
		ExitCS();
	}
	
	/**
	 * Function to test log file for mutual exclusion errors
	 * @return
	 */
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
	        		if(line1.length()>2 || line2.length()>2)
	        		{
	        			System.out.println("Mutual exclusion not satisfied. Line too long");
	        			return 0;
	        		}
	        		if(!((line1.charAt(0) == line2.charAt(0)) && (line1.charAt(1) != line2.charAt(1)) ))
	        		{
	        			System.out.println("Mutual exclusion not satisfied");
	        			return 0;
	        		}
	        	}
	        	line1 = br.readLine();
	        }
	        
	        System.out.println("Algorithm works");
	        
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
	
	/**
	 * Perform required algorithm procedures after exiting CS
	 */
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

	/**
	 * Starts the application
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Application app = new Application();

		mSelfNodeID = Integer.parseInt(args[0]);
		mConfigFile = args[1];
		mConfigReader = new ConfigReader(mConfigFile);
		app.initializeArrays();
		
		/* create server to receive messages*/
		mServer = new SctpServer(app,mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
		mServerThread = new Thread(mServer);
		mServerThread.start();
		
		//Timer to indicate when to request CS entry
		TimerThread timer = new TimerThread(app);
		new Thread(timer).start();
		
		//Create a communication channel to every other node
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
	
	/**
	 * Encode privilege message contents into String for transmission
	 * @param q
	 * @param arr
	 * @return
	 */
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
	
	/**
	 * Get queue from received privilege message string
	 * @param message
	 * @return
	 */
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
	
	/**
	 * Get array from received privilege message string
	 * @param message
	 * @return
	 */
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
	
	/**
	 * Send token to designated node
	 * @param message
	 * @param destID
	 */
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
	
	/**
	 * Server thread calls this function when token is received
	 * @param prevMsg
	 */
	public void updateLocal(String prevMsg)
	{
		havePrivilege = true;
		requestQueue = getPrivelegeQueue(prevMsg);
		LN = getPrivelegeArray(prevMsg);
	}
	
	/**
	 * Send request message to all other nodes in system
	 * @param message
	 */
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
	
	/**
	 * Required algorithm procedure before generating CS request
	 */
	public void algorithmSendRequest()
	{
		isRequesting = true;
		finishedCS = false;
		if(!havePrivilege) //If this node does not have token
		{
			int temp = RN.get(mSelfNodeID);
			temp+= 1;
			RN.set(mSelfNodeID, temp);
			broadcastRequest(encodeRequest(mSelfNodeID,++seqNum));
			while(!havePrivilege){System.out.print(".");}; //Blocking call to wait until token is received
		}
	}

	/**
	 * Encode request message contents into a string
	 * @param j
	 * @param n
	 * @return
	 */
	public String encodeRequest(int j, int n)
	{
		String request = "r";

		request += Integer.toString(j);

		request += "!";

		request += Integer.toString(n);

		return request;
	}
	
	/**
	 * Get Node ID from received request message string
	 * @param message
	 * @return
	 */
	public int getRequestID(String message)
	{
		message = message.substring(1); //Remove the first letter 'r'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark

		int id = Integer.parseInt(temp1[0]);
		return id;
	}
	
	/**
	 * Get sequence number from received request message string
	 * @param message
	 * @return
	 */
	public int getRequestSequence(String message)
	{
		message = message.substring(1); //Remove the first letter 'r'

		String temp1[] = message.split("!"); //Queue and array separated by ! mark

		int seq = Integer.parseInt(temp1[1].trim());
		return seq;
	}



}
