package com.charliezhangsde;
public class MuExProject2 {

	public static void main(String[] args) { //java MuExProject2 hostId
		// TODO Auto-generated method stub
		int hostId = -1;
		if(args.length == 0)
		{
			System.out.println("Please input the host ID");
			return;
		}
		hostId = Integer.valueOf(args[0]);
		 //hostId = 4;//for test
		RaymondAlgorithm CSAlgo = new RaymondAlgorithm(hostId);
		CSAlgo.initialize();  // start server
		
		MyApplication app = new MyApplication(CSAlgo);
		
		Thread t = new Thread(app);
		t.start();
		try
		{
			t.join();
		}catch(InterruptedException e)
		{
			
		}
	}

}
