public class VectorClock {
	private int mNodeCount;
	private int mSelfClockID;
	private int mClock[] = null;

	VectorClock(String mSelfNodeID, int mNodeCount) {
		mClock = new int[mNodeCount];
		this.mNodeCount = mNodeCount;  
		this.mSelfClockID = Integer.parseInt(mSelfNodeID);
		// System.out.println("VectorClock "+mSelfClockID+" : Created for "+mNodeCount+" nodes");
	}

	public synchronized int[] getCurrentClock() {
		return mClock;
	}

	public synchronized int[] getClockToSend() {

		/* increment self clock before sending message */
		mClock[mSelfClockID] += 1;

		/* return the updated clocks */
		return mClock;
	}

	public synchronized int[] getClockOnReceive(int mReceivedClock[]) {

		/* increment self clock on receiving a message */
		mClock[mSelfClockID] += 1;

		for (int i = 0; i < mNodeCount; i++) {
			if (i == mSelfClockID) {
				continue;
			} else {
				/* update clocks if received clocks is greater than ours */
				if (mReceivedClock[i] > mClock[i]) {
					mClock[i] = mReceivedClock[i];
				}
			}
		}

		/* return the updated clocks */
		return mClock;
	}
}
