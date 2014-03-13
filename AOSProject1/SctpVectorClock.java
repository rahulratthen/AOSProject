import java.util.ArrayList;
import java.util.List;

public class SctpVectorClock {
	public static int MESSAGE_SIZE = 128;

	private static int mSelfNodeID = 0;
	private static String mConfigFile = null;
	private static ConfigReader mConfigReader = null;
	private static SctpServer mServer = null;
	private static Thread mServerThread = null;
	private static VectorClock mSelfClock = null;

	private static List<SctpClient> mClients = new ArrayList<SctpClient>();
	private static List<Thread> mClientThreads = new ArrayList<Thread>();

	public static void main(String[] args) {
		try {
			if (2 != args.length) {
				System.out.println("SctpVectorClock : Incomplete Invokation, Need NodeID & Configuration File Name");
				return;
			} else {
				int mCurrentClock[];

				// System.out.println("SctpVectorClock : Start");

				/* parse the input arguments */
				mSelfNodeID = Integer.parseInt(args[0]);
				mConfigFile = args[1];
				// System.out.println("SctpVectorClock : Self Node ID : "+mSelfNodeID);
				// System.out.println("SctpVectorClock : ConfigFile : "+mConfigFile);

				/* read the configuration file */
				mConfigReader = new ConfigReader(mConfigFile);

				// System.out.println("SctpVectorClock : Number of Nodes : "+mConfigReader.getNodeCount());
				// System.out.println("SctpVectorClock : Self Port Number : "+mConfigReader.getNodeConfig(mSelfNodeID)[2]);

				/* create vector clock */
				mSelfClock = new VectorClock(args[0], mConfigReader.getNodeCount());

				/* print initial clock */
				System.out.print("SctpVectorClock : Initial Clock : ");
				mCurrentClock = mSelfClock.getCurrentClock();
				for (int i = 0; i < mCurrentClock.length; i++) {
					System.out.print(mCurrentClock[i]);					
					System.out.print(" ");					
				}
				System.out.print("\n");

				/* create server */
				mServer = new SctpServer(mSelfClock, mConfigReader.getNodeConfig(mSelfNodeID)[0], mConfigReader.getNodeConfig(mSelfNodeID)[1], mConfigReader.getNodeConfig(mSelfNodeID)[2],mConfigReader.getNodeCount() - 1);
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
						mClients.add(new SctpClient(mSelfClock, args[0], mConfigReader.getNodeConfig(i)[0], mConfigReader.getNodeConfig(i)[1], mConfigReader.getNodeConfig(i)[2]));
						mClientThreads.add(new Thread(mClients.get(i - skipped)));
						mClientThreads.get(i - skipped).start();
					}
				}

				/* wait for the server to finish*/
				mServerThread.join();
				System.out.println("SctpVectorClock : Server Exited");
				
				/* wait for clients to finish */
				for (Thread thread: mClientThreads) {
					thread.join();
				}
				System.out.println("SctpVectorClock : All Clients Exited");

				System.out.print("SctpVectorClock : Final Clock : ");
				mCurrentClock = mSelfClock.getCurrentClock();
				for (int i = 0; i < mCurrentClock.length; i++) {
					System.out.print(mCurrentClock[i]);					
					System.out.print(" ");					
				}
				System.out.print("\n");
				// System.out.println("SctpVectorClock : Finish");
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}
}
