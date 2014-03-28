
import java.nio.*;
import com.sun.nio.sctp.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class SctpServer implements Runnable {
	public static int MESSAGE_SIZE = 128;

	private String mAddress;
	private String mPort;
	private String mSelfNodeID;
	private int mNeighbourCount;
	Application parentThread;
	//private 
	//private VectorClock mSelfClock;

	public SctpServer(Application p, String mSelfNodeID, String mAddress, String mPort, int mNeighbourCount) {
		super();
		parentThread = p;
		this.mAddress = mAddress;
		this.mPort = mPort;
		this.mNeighbourCount = mNeighbourCount;
		this.mSelfNodeID = mSelfNodeID;
		//this.mSelfClock = mClock;
		// System.out.println("SctpServer "+mSelfNodeID+" : Server Created with Port : "+mPort);
	}

	@Override
	public void run() {
		int mMessageCount = 0;
		SocketAddress mServerAddress = new InetSocketAddress(mAddress, Integer.parseInt(mPort));

		// System.out.println("SctpServer "+mSelfNodeID+" : Server Started");
		// System.out.println("SctpServer "+mSelfNodeID+" : Socket : "+mServerAddress.toString());

		try {
			// System.out.println("SctpServer "+mSelfNodeID+" : Open Server Channel");
			SctpServerChannel mServerChannel = SctpServerChannel.open();

			// System.out.println("SctpServer "+mSelfNodeID+" : Bind Server to Port : "+Integer.parseInt(mPort));
			mServerChannel.bind(mServerAddress);
/*
			while (mMessageCount < mNeighbourCount) {
				String mMessage;
				String mMessageParts[];
				//String mClockInMessage[];
				//int mReceivedClock[];
				//int mUpdatedClock[];

				SctpChannel mClientChannel = mServerChannel.accept();

				mClientChannel.receive(mBuffer,null,null);

				mMessage = bufferToString(mBuffer);

				//mMessageParts = mMessage.split("/");
//				mClockInMessage = mMessageParts[2].trim().split(" ");
				//System.out.println("SctpServer "+mSelfNodeID+" : Received from "+mMessageParts[1]+ " : "+mMessage);
				System.out.println("Sctp Server: " + mMessage);
				mBuffer.flip();

//				/* update self clock from received clock */
//				mReceivedClock = new int[mClockInMessage.length];
//				for (int i = 0; i < mClockInMessage.length; i++) {
//					mReceivedClock[i] = Integer.parseInt(mClockInMessage[i].trim());
//				}

//				System.out.print("SctpServer "+mSelfNodeID+" : Updated Clock : ");
//				mUpdatedClock = mSelfClock.getClockOnReceive(mReceivedClock);
//				for (int i = 0; i < mUpdatedClock.length; i++) {
//					System.out.print(mUpdatedClock[i]);					
//					System.out.print(" ");					
//				}
//				System.out.print("\n");

//				mMessageCount++;
				
//			}
			
			
			while(true){
				ByteBuffer mBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
				String mMessage;
				String mMessageParts[];
				//String mClockInMessage[];
				//int mReceivedClock[];
				//int mUpdatedClock[];

				SctpChannel mClientChannel = mServerChannel.accept();

				mClientChannel.receive(mBuffer,null,null);

				mMessage = bufferToString(mBuffer);

				//mMessageParts = mMessage.split("/");
//				mClockInMessage = mMessageParts[2].trim().split(" ");
				//System.out.println("SctpServer "+mSelfNodeID+" : Received from "+mMessageParts[1]+ " : "+mMessage);
				System.out.println("Sctp Server: " + mMessage);
				
				// while(!parentThread.finishedCS)
				// {
					// System.out.print("a");
				// }

				//Adding the mutex part - Rahul
				if(mMessage.startsWith("p"))
				{
					parentThread.updateLocal(mMessage);
					
				}
				else if(mMessage.startsWith("r"))
				{
					System.out.println("Starts with r");
					parentThread.processRequestMessage(mMessage);
				}
				
				mBuffer.flip();
				
			}
			// System.out.println("SctpServer "+mSelfNodeID+" : Messages Received from All Neighbours");

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private String bufferToString(ByteBuffer mBuffer) {
		mBuffer.position(0);
		mBuffer.limit(MESSAGE_SIZE);
		byte[] mBufArr = new byte[mBuffer.remaining()];
		mBuffer.get(mBufArr);
		return new String(mBufArr);
	}
}

