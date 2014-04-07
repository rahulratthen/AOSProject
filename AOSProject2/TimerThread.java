import java.util.Random;


public class TimerThread implements Runnable {
	
	Application parentThread;
	
	/**
	 * 
	 * @param p
	 */
	public TimerThread(Application p)
	{
		parentThread = p;
	}
	@Override
	public void run() {
		Random rand = new Random(10);
		//Continually initiate CS entry requests at certain time intervals
		while(true)
		{
			//int ms = rand.nextInt()*100;
			try {
				
				Thread.sleep(1000);
				parentThread.updateTimer();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	

}
