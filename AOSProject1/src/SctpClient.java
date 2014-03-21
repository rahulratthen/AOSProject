import java.net.*;
import java.nio.*;
import java.util.Random;

import com.sun.nio.sctp.*;

public class SctpClient implements Runnable {
	public static int MESSAGE_SIZE = 128;
	private String mSelfNodeID;
	private String mServerAddress;
	private String mServerPort;
	private String mServerNodeID;
	StringBuffer mMessage = new StringBuffer("Hi"); /* change the message to be sent here */
	ByteBuffer mByteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
	int sequenceNumber = 1;
	
	//private VectorClock mSelfClock;

	public SctpClient(String mSelfNodeID, String mServerNodeID, String mServerAddress, String mServerPort) {
		super();
		this.mServerAddress = mServerAddress;
		this.mServerPort = mServerPort;
		this.mServerNodeID = mServerNodeID;
		//this.mSelfClock = mClock;
		this.mSelfNodeID = mSelfNodeID;
		mMessage.append("/");
		mMessage.append(mSelfNodeID.trim());
		// System.out.println("SctpClient "+mServerNodeID+" : Client for "+mServerAddress+":"+mServerPort+" Created");
	}

	@Override
	public void run() {
		SocketAddress mSocketAddress = new InetSocketAddress(mServerAddress,Integer.parseInt(mServerPort));
		MessageInfo mMessageInfo = MessageInfo.createOutgoing(null,0);

		// System.out.println("SctpClient "+mServerNodeID+" : Client for "+mServerAddress+":"+mServerPort+" Started");
		while (true) {
			try {
				//int mClockToSend[];

				SctpChannel mSctpChannel = SctpChannel.open();
				mSctpChannel.connect(mSocketAddress);

				// System.out.println("SctpClient "+mServerNodeID+" : Socket : "+mSocketAddress.toString());

				/* get clock to send and append it to message */
				//mClockToSend = mSelfClock.getClockToSend();
//
//				mMessage.append("/");
//				for (int i = 0; i < mClockToSend.length; i++) {
//					mMessage.append(mClockToSend[i]);
//					mMessage.append(" ");
//				}

				mByteBuffer.put(mMessage.toString().getBytes());
				mByteBuffer.flip();
				mSctpChannel.send(mByteBuffer,mMessageInfo);
				System.out.println("SctpClient "+mServerNodeID+" : Send : "+mMessage.toString());
				// System.out.println("SctpClient "+mServerNodeID+" : Exiting");
				break;
			} catch (Exception e) {
					// System.out.println("SctpClient "+mServerNodeID+" : Couldn't Send Message, Retrying after 1 Second...");
					try {
						Thread.sleep(1000);
					} catch (Exception e2) {
						System.out.println("SctpClient "+mServerNodeID+" : Couldn't Sleep...");
					}
			}
			
			applicationModule();
		}
	}

	private void applicationModule() 
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
		
		
	}
}
