import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
public class ConfigReader 
{

	private int mNumOfNodes = 0;
	private List<String[]> mNodeConfig = new ArrayList<>();

	ConfigReader (String mConfigFile) {
		BufferedReader mFileReader;


		try {
			String mLine;
			int index = 0;
			mFileReader =  new BufferedReader(new FileReader(mConfigFile));
			while((mLine = mFileReader.readLine()) != null) {
				index++;

				String[] mColomns = mLine.trim().split(" ");
				if (mColomns.length == 1) {
					mNumOfNodes = Integer.parseInt(mColomns[0]);
				} else {
					mNodeConfig.add(mColomns);
				}
			}
			mLine = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		mFileReader = null;
	}

	public int getNodeCount() {
		return mNumOfNodes;
	}

	public String[] getNodeConfig(int mNodeIndex) {
		return mNodeConfig.get(mNodeIndex);
	}
}


