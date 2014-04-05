
public class TimerThread implements Runnable {
	
	Application parentThread;
	
	public TimerThread(Application p)
	{
		parentThread = p;
	}
	@Override
	public void run() {
	
		//Continually initiate CS entry requests at certain time intervals
		while(true)
		{
			try {
				Thread.sleep(3000);
				parentThread.updateTimer();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	

}
