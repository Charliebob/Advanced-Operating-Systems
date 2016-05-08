package com.charliezhangsde;
public class MyApplication implements Runnable{
	RaymondAlgorithm algo;
	int no = SystemInfo.req_no;
	MyApplication(RaymondAlgorithm ra)
	{
		algo = ra;
	}
	public void run()
	{
		System.out.println("Node " + SystemInfo.hostId + ": App: App starts, number of request: " + no);
		
		int i = 0;
		for(i = 0; i< no; i ++)
		{
			algo.CS_Enter();
			
			try
			{
				double time = myExpRandom.exp(SystemInfo.cs_duration)*1000;
				//Long time = new Long(50);
				Thread.sleep((long)(time));
				//System.out.println("App: Node " + SystemInfo.hostId + ": delay: " + time);
			}catch(InterruptedException e)
			{
				
			}
			algo.CS_Exit();
			try
			{
				double time2 = myExpRandom.exp(SystemInfo.req_interval)*1000;
				Thread.sleep((long)(time2));
				//System.out.println("App: Node " + SystemInfo.hostId + ": interval: " + time2);
			}catch(InterruptedException e){
				
			}
		}
		System.out.println("Node " + SystemInfo.hostId + ": App: App ends");
	}
}
