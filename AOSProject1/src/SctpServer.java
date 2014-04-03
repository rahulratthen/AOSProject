
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

	public SctpServer(Application p, String mSelfNodeID, String mAddress, String mPort, int mNeighbourCount) {
		super();
		parentThread = p;
		this.mAddress = mAddress;
		this.mPort = mPort;
		this.mNeighbourCount = mNeighbourCount;
		this.mSelfNodeID = mSelfNodeID;
	}

	@Override
	public void run() {
		int mMessageCount = 0;
		SocketAddress mServerAddress = new InetSocketAddress(mAddress, Integer.parseInt(mPort));


		try {
			SctpServerChannel mServerChannel = SctpServerChannel.open();

			mServerChannel.bind(mServerAddress);
			
			while(true){
				ByteBuffer mBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
				String mMessage;
				String mMessageParts[];

				SctpChannel mClientChannel = mServerChannel.accept();

				mClientChannel.receive(mBuffer,null,null);

				mMessage = bufferToString(mBuffer);
				
				if(mMessage.startsWith("p"))
				{
					parentThread.updateLocal(mMessage);
					
				}
				else if(mMessage.startsWith("r"))
				{
					parentThread.processRequestMessage(mMessage);
					
					if(parentThread.finishedCS)
					{
						parentThread.lastRequest = parentThread.getRequestID(mMessage);
						parentThread.requestReceived = true;
					}
				}
				
				mBuffer.flip();
				
			}

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

