import java.util.ArrayList;
import java.util.List;

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
	
	
	public static void main(String[] args) 
	{
		Application app = new Application();
		
		/* create server */
		mServer = new SctpServer(mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
		mServerThread = new Thread(mServer);
		System.out.println("SctpVectorClock : Starting Server : "+args[0]+" at "+mConfigReader.getNodeConfig(mSelfNodeID)[1]+":"+mConfigReader.getNodeConfig(mSelfNodeID)[2]);
		mServerThread.start();

		/* create clients */
		int skipped = 0;
		for (int i = 0; i < mConfigReader.getNodeCount(); i++) {
			if (i == mSelfNodeID) {
				skipped = 1;
				continue;
			} else {
				System.out.println("SctpVectorClock : Starting Client : "+mConfigReader.getNodeConfig(i)[0]+" at "+mConfigReader.getNodeConfig(i)[1]+":"+mConfigReader.getNodeConfig(i)[2]);
				mClients.add(new SctpClient(args[0], mConfigReader.getNodeConfig(i)[0], mConfigReader.getNodeConfig(i)[1], mConfigReader.getNodeConfig(i)[2]));
				mClientThreads.add(new Thread(mClients.get(i - skipped)));
				mClientThreads.get(i - skipped).start();
			}
		}

	}



}
