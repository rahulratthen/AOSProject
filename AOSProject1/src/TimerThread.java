
public class TimerThread implements Runnable {
	
	Application parentThread;
	
	public TimerThread(Application p)
	{
		parentThread = p;
	}
	@Override
	public void run() {
		while(true)
		{
			try {
				Thread.sleep(3000);
				parentThread.updateTimer();
				//System.out.println("Thread executing");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	

}
