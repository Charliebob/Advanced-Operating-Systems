package com.charliezhangsde;
import java.io.*;
import java.net.*;
import java.util.concurrent.Semaphore;
import java.util.Queue;
import java.util.LinkedList;

import com.sun.nio.sctp.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
public class RaymondAlgorithm {
	public static final int MESSAGE_SIZE = 100;
	
	public static final int MESSAGE_TYPE_BUILD_TREE = 0;
	public static final int MESSAGE_TYPE_ACK = 1;
	public static final int MESSAGE_TYPE_NACK = 2;
	public static final int MESSAGE_TYPE_BUILD_TREE_COMPLETE = 3;
	public static final int MESSAGE_TYPE_BROADCAST_RESPONSE = 4;
	public static final int MESSAGE_TYPE_REQUEST_TOKEN = 5;
	public static final int MESSAGE_TYPE_TRANSFER_TOKEN = 6;
	
	public static final String HOST_SUFFIX = ".utdallas.edu";
	
	int hostId = 0;
	static int[] TN = new int[SystemInfo.MAX_NUMBER_OF_NODES];;
	static int TNNo = 0;
	static int req_for_token_id = -1;
	static boolean self_req = false;  //There's a request from local machine
	
	static Semaphore sem_got_token = new Semaphore(0); //for cs_enter()
	static Semaphore tree_complete_sem = new Semaphore(0);
	static Semaphore sem_req_token = new Semaphore(0); //for token service after transfered here, or released by itself
	static Semaphore sem_wait_req = new Semaphore(0);// waiting for token request
	static SystemInfo sysInfo = new SystemInfo();
	static Raymond_Clock clock;
	Thread Trd_Server = null;
	Token_Service tokenService = new Token_Service();
	FileLog fl;
	
	static boolean first_build_mes = true;
	//static final int def_sctp_port = 6001;
	static boolean tree_complete = false;
	class node{
		String hostName;
		int port;
	};

	RaymondAlgorithm(int id)
	{
		hostId = id;
	}
	void runServer()
	{
		SctpServer ss = new SctpServer();
		Trd_Server = new Thread(ss);
		Trd_Server.start();
	}
	
	class Token_Service implements Runnable{
		
		class ConnInfo{
			SctpChannel sc;
			int hostFrom;
		}
		Queue<ConnInfo> req_queue = new LinkedList<ConnInfo>();
	
		public void run()
		{
			System.out.println("Node "+ SystemInfo.hostId + ": Token_Service starts to run");
			while(true)
			{
				try{
					sem_wait_req.acquire();//no request, do nothing
					System.out.println("Node "+ SystemInfo.hostId + ": Token_Service: Got request");
					if((req_for_token_id != SystemInfo.hostId))  //token is not here, request it.
					{
						requestToken();	
					}
					sem_req_token.acquire(); //waiting for token available
					System.out.println("Node "+ SystemInfo.hostId + ": Token_Service: Token acquired");
					req_for_token_id = SystemInfo.hostId;
				}catch(InterruptedException e){
					
				}
				if(self_req == true)
				{
					sem_got_token.release();
					System.out.println("Node "+ SystemInfo.hostId + ": Token_Service sem_got_token.release");
				}
				else
				{
					transferToken();
				}
				/*if(req_queue.isEmpty() != true)
				{
					
					sem_wait_req.release();
					System.out.println("Node "+ SystemInfo.hostId + ": Token_Service QUEUE NOT EMPTY" + req_queue.size());
				}*/
			}
		}
		public void addTokenRequest(int id, SctpChannel sc)
		{
			
			ConnInfo e = new ConnInfo();
			e.sc = sc;
			e.hostFrom = id;
			
			synchronized(req_queue)
			{
				req_queue.add(e);
				System.out.println("Node "+ SystemInfo.hostId + ": Token_Service addTokenRequest() from "+ id + " queue size: " + req_queue.size() );
			}
			
		}
		void requestToken()
		{
			System.out.println("Node "+ SystemInfo.hostId + ": Token_Service REQUEST TOKEN from " + req_for_token_id);
			SctpClient sc = new SctpClient(req_for_token_id, MESSAGE_TYPE_REQUEST_TOKEN);
			Thread t = new Thread(sc);
			t.start();
			try
			{
				t.join();
			}catch(InterruptedException e){
				
			}
		}
		void transferToken()
		{
			if(req_queue.isEmpty() != true)
			{
				ConnInfo info;
				synchronized(req_queue)
				{
					info = req_queue.remove();
				}
				try{
					
					SctpClient sclient = new SctpClient(info.hostFrom, MESSAGE_TYPE_TRANSFER_TOKEN, info.sc);
					System.out.println("Node "+ SystemInfo.hostId + ": Token_Service TRANSFER TOKEN TO " + info.hostFrom + " size " + req_queue.size());
					Thread t = new Thread(sclient);
					t.start();
					req_for_token_id = info.hostFrom;
					t.join();
				}catch(InterruptedException e)
				{
					
				}
			}
		}
		
	}
	class Communication_Service implements Runnable{
		int messageType;
		SctpChannel sc;
		int hostFrom;
		Communication_Service(int mesType, SctpChannel schannel, int host_from)
		{
			messageType = mesType;
			sc = schannel;
			hostFrom = host_from;
		}
		
		public void run()
		{
			switch(messageType)
			{
			case MESSAGE_TYPE_BUILD_TREE:   //build tree
				buildSpanningTree(sc);
				break;
			case MESSAGE_TYPE_BUILD_TREE_COMPLETE:   //build tree
				broadCast(sc, messageType);
				break; 
			default:
				break;
			}
		}
		void buildSpanningTree(SctpChannel schannel)
		{
			ArrayList<Thread> t_tree = new ArrayList<Thread>();
			int j = 0;
			
			if(SystemInfo.FNNo == 1 && schannel != null)
			{
				sendResponse(MESSAGE_TYPE_ACK, schannel);
				printMessage(MESSAGE_TYPE_ACK, SystemInfo.hostId, hostFrom, true);
				
				System.out.print("Node " + SystemInfo.hostId + " has " + TNNo + " tree Neighbors. They are/It is: ");
				for(int i = 0; i< TNNo; i++)
				{
					System.out.print(" " + TN[i]);
				}
				
				System.out.println("");
				try
				{
					schannel.close();
				}catch(IOException e)
				{
					
				}
				return;
			}
			for(int i = 0; i<SystemInfo.FNNo; i++)
			{
				if(hostFrom != SystemInfo.FN[i])
				{
					SctpClient sc = new SctpClient(SystemInfo.FN[i], MESSAGE_TYPE_BUILD_TREE);
					Thread t = new Thread(sc);
					t_tree.add(j++, t);
					t_tree.get(j-1).start();
				}
			}
			try{
				for(int i = 0; i<j; i++)
				{
					t_tree.get(i).join();
				}
				}catch(InterruptedException e){
				
			}
			sendResponse(MESSAGE_TYPE_ACK, schannel);
			printMessage(MESSAGE_TYPE_ACK, SystemInfo.hostId, hostFrom, true);
			tree_complete = true;
			System.out.println("Node "+ SystemInfo.hostId + ": Building tree: responded by all its neighbors");
			System.out.print("Node " + SystemInfo.hostId + " has " + TNNo + " tree Neighbors. They are/It is: ");
			for(int i = 0; i< TNNo; i++)
			{
				System.out.print(" " + TN[i]);
			}
			System.out.println("");
			
			try
			{
				schannel.close();
			}catch(IOException e)
			{
				
			}
		}
		void broadCast(SctpChannel schannel, int MesType)
		{
		
			Thread[] t_bc = new Thread[10];
			printMessage(MesType, hostFrom, SystemInfo.hostId, false);
			if(TNNo == 1 && schannel != null)
			{
				sendResponse(MESSAGE_TYPE_BROADCAST_RESPONSE, schannel);
				printMessage(MESSAGE_TYPE_BROADCAST_RESPONSE, SystemInfo.hostId, hostFrom, true);
				//System.out.println("");
				try
				{
					schannel.close();
					tree_complete_sem.release();
				}catch(IOException e)
				{
					System.out.println("Node "+ SystemInfo.hostId + ": BroadCast: channel close error");
				}
				return;
			}
			for(int i = 0; i<TNNo; i++)
			{
				int id = TN[i];
				if(id != hostFrom)
				{
					SctpClient sc = new SctpClient(id, MesType);
					t_bc[i] = new Thread(sc);
					t_bc[i].start();
				}
			}
			try{
					for(int i = 0; i<TNNo; i++)
					{
						int id = TN[i];
						if(id != hostFrom)
							t_bc[i].join();
					}
					System.out.println("Node " + SystemInfo.hostId +" BroadCast: responded by all its neighbors");
				}catch(InterruptedException e){
				System.out.println("Boadcast, join, InterruptedException" + e.getMessage());
				
			}
			sendResponse(MESSAGE_TYPE_BROADCAST_RESPONSE, schannel);
			printMessage(MESSAGE_TYPE_BROADCAST_RESPONSE, SystemInfo.hostId, hostFrom, true);

			try
			{
				schannel.close();
				tree_complete_sem.release();
			}catch(IOException e)
			{
				System.out.println("BroadCast: channel close error");
			}
		}
		
	}
	static void printMessage(int type, int hostfrom, int hostto, boolean bl_send)
	{
		String str = bl_send?"is sent":"is received";
		String strto = new String(" to " + hostto);
		String strfrom = new String(" from " + hostfrom);
		String message = null;
		if(bl_send)
		{
			message = strto;
		}
		else
		{
			message = strfrom;
		}
		switch(type)
		{
		case MESSAGE_TYPE_BUILD_TREE:
			System.out.println("Node "+ SystemInfo.hostId + ": Building Tree: Message " + str + message);
			break;
		case MESSAGE_TYPE_BUILD_TREE_COMPLETE:
			System.out.println("Node "+ SystemInfo.hostId + ": That tree has been built!" + str + message);
			break;
		/*case MESSAGE_TYPE_BROADCAST:
			System.out.println("Node "+ SystemInfo.hostId + ": BroadCast: Message " + str + message);
			break;*/
		case MESSAGE_TYPE_ACK:
			System.out.println("Node "+ SystemInfo.hostId + ": Building Tree: ACK " + str + message);
			break;
		case MESSAGE_TYPE_NACK:
			System.out.println("Node "+ SystemInfo.hostId + ": Building Tree: NACK " + str + message);
			break;
		case MESSAGE_TYPE_BROADCAST_RESPONSE:
			System.out.println("Node "+ SystemInfo.hostId + ": BroadCast: Response " + str + message);
			break;
		default:
			break;
		}
	}
	static void sendResponse(int type, SctpChannel schannel)
	{
		ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
		String message = new String(type+"|"+SystemInfo.hostId);
		MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
		
		byteBuffer.put(message.getBytes());
		
		byteBuffer.flip();
		try{
		schannel.send(byteBuffer,messageInfo);
		}catch(IOException e){
			
		}
		return;
	}
	
	public static int getNumber(String s)
	{
		int n = 0;
		
		String[] str = s.split("[^0-9]");
		n = Integer.valueOf(str[0]);
		return n;
	}
	public static String byteToString(ByteBuffer byteBuffer)
	{
		byteBuffer.position(0);
		byteBuffer.limit(MESSAGE_SIZE);
		byte[] bufArr = new byte[byteBuffer.remaining()];
		byteBuffer.get(bufArr);
		return new String(bufArr);
	}
	class SctpClient implements Runnable{
		int conn_Id = 0;
		int mesType = 0;
		SctpChannel in_sc = null;
		SctpClient(int connectId, int messageType)
		{
			conn_Id = connectId;
			mesType = messageType;
		}
		SctpClient(int connectId, int messageType, SctpChannel schannel)
		{
			conn_Id = connectId;
			mesType = messageType;
			in_sc = schannel;
		}
		public void run()
		{
			ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			String message = null;
			
			SocketAddress remoteAddress = new InetSocketAddress(SystemInfo.allNodes.get(conn_Id -1).hostName + HOST_SUFFIX
																	,SystemInfo.allNodes.get(conn_Id-1).port);
			try{
				SctpChannel sc;
				if(in_sc != null)
				{
					sc = in_sc;
				}
				else
				{
					sc = SctpChannel.open();
					//sc.bind(new InetSocketAddress(in_port));
					sc.connect(remoteAddress);
				}
				MessageInfo messageInfo = MessageInfo.createOutgoing(null,0);
				if(mesType == MESSAGE_TYPE_TRANSFER_TOKEN)
				{
					String m = new String(mesType + "|" + SystemInfo.hostId + "|" + clock.toStringClock() + "|");
					System.out.println("SctpClient: transfer token: " + m);
					byteBuffer.put(m.getBytes());
				}
				else
					byteBuffer.put(new String(mesType + "|" + SystemInfo.hostId + "|").getBytes());
				byteBuffer.flip();
				sc.send(byteBuffer,messageInfo);
				
				printMessage(mesType, SystemInfo.hostId, conn_Id, true);
				if(mesType != MESSAGE_TYPE_TRANSFER_TOKEN)
				{
					ByteBuffer bBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
					sc.receive(bBuffer, null, null);
					
					message = byteToString(bBuffer);
					String[] str_split = message.split("\\|");
					//System.out.println("After split " + str_split[0] + " and " + str_split[1]);
					int type = getNumber(str_split[0]);
					printMessage(type, conn_Id, SystemInfo.hostId, false);
					
					switch(type)
					{
					case MESSAGE_TYPE_ACK://ACK--|x|x|
						TN[TNNo++] = conn_Id;
						//System.out.println("Tree neighbor added " + TreeNeighbors[TreeNeighborsNo -1] + " TreeNeighborsNo " + TreeNeighborsNo);
						break;
					case MESSAGE_TYPE_NACK://NACK
						break;
					case MESSAGE_TYPE_BROADCAST_RESPONSE:
						break;
					case MESSAGE_TYPE_TRANSFER_TOKEN://x|x|x x x x x|---|mestype|fromid|clock vector|
						//parse clock
						clock.updateClock(str_split[2]);
						sem_req_token.release();
						break;
					default:
						break;
					}
				}
				sc.shutdown();
				sc.close();
				
			}catch(IOException e){
				System.out.println("Node "+ SystemInfo.hostId + ": SctpClient: IOEXCEPTION: " + e);
				
			}
		}
		
	}
	class SctpServer implements Runnable{
	
		void executeRequest(SctpChannel sc)
		{
			//System.out.println("Node "+ SystemInfo.hostId + ": Sctp Server receives message");
			ByteBuffer byteBuffer = ByteBuffer.allocate(MESSAGE_SIZE);
			String message;
			try{
			
			MessageInfo messageInfo = sc.receive(byteBuffer,null,null);
			message = byteToString(byteBuffer);
			//System.out.println(message + " " + message.length());
			
			String[] str_splits = message.split("\\|");
			//System.out.println("After split " + str_splits[0] + "and" + str_splits[1]+"test"+ str_splits[1].length());
			int type = getNumber(str_splits[0]);
			int hostFrom = getNumber(str_splits[1]);
			switch(type)
			{
			case MESSAGE_TYPE_BUILD_TREE:
				if(true == first_build_mes)
				{
					first_build_mes = false;
					TN[TNNo++] = hostFrom; //parent!
					req_for_token_id = hostFrom;
					
					printMessage(type, hostFrom, SystemInfo.hostId, false);
					Communication_Service CommSer = new Communication_Service(MESSAGE_TYPE_BUILD_TREE, sc, hostFrom);//Integer.valueOf(str_splits[1]));
					Thread t = new Thread(CommSer);
					t.start();
				}
				else
				{
					printMessage(MESSAGE_TYPE_NACK, SystemInfo.hostId, hostFrom, true);
					sendResponse(MESSAGE_TYPE_NACK, sc);
				}
				break;
			case MESSAGE_TYPE_BUILD_TREE_COMPLETE: 
				Communication_Service CommSer = new Communication_Service(MESSAGE_TYPE_BUILD_TREE_COMPLETE, sc, hostFrom);//Integer.valueOf(str_splits[1]));
				Thread t = new Thread(CommSer);
				t.start();
				break;
			case MESSAGE_TYPE_REQUEST_TOKEN:
				tokenService.addTokenRequest(hostFrom, sc);
				sem_wait_req.release();
				System.out.println("Node "+ SystemInfo.hostId + ": Sctp Server sem_wait_req.release()");
				break;
			/*case MESSAGE_TYPE_TRANSFER_TOKEN: //No such message here since SctpChannel is blocking type
				break;*/
			default:
				break;
			}
			
			}catch(IOException e){
				System.out.println("Node "+ SystemInfo.hostId + ": SctpServer: IOEXCEPTION: " + e);
				
			}
		}
		public void run()
		{
			try
			{
				SctpServerChannel ssc= SctpServerChannel.open();
				InetSocketAddress serverAddr = new InetSocketAddress(SystemInfo.allNodes.get(SystemInfo.hostId-1).port);
				ssc.bind(serverAddr);
				System.out.println("Node "+ SystemInfo.hostId + ": SctpServer is running!");
				while(true)
				{
					SctpChannel sc = ssc.accept();
					executeRequest(sc);	
				}
			}
			catch(IOException ex)
			{
				System.out.println("Node "+ SystemInfo.hostId + ": SctpServer: IOException: " + ex.getMessage());
				ex.printStackTrace();
			}
		
		}
	}
	
	
	private void buildTree()
	{
		ArrayList<Thread> t_tree = new ArrayList<Thread>();
		
		System.out.println("Node "+ SystemInfo.hostId + ": Spanning tree begins to build from source node! The number of neighbors is " + SystemInfo.FNNo);

		for(int i = 0; i<SystemInfo.FNNo; i++)
		{
			SctpClient sc = new SctpClient(SystemInfo.FN[i], MESSAGE_TYPE_BUILD_TREE);
			Thread t = new Thread(sc);
			t_tree.add(i,t);
			t_tree.get(i).start();
		}
		try{
			for(int i = 0; i<SystemInfo.FNNo; i++)
			{
				t_tree.get(i).join();
			}
			System.out.println("Node "+ SystemInfo.hostId + ": Spanning tree is built!");
			//System.out.println("");
			System.out.print("Node "+ SystemInfo.hostId + ": Node " + SystemInfo.hostId + " has " + TNNo + " tree Neighbors. They are/It is: ");
			
			for(int i = 0; i< TNNo; i++)
			{
				System.out.print(" " + TN[i]);
			}
			System.out.println("");
		}catch(InterruptedException e){
			
		}
		tree_complete = true;
	}
	private void broadCast(int MesType)//main thread
	{
		ArrayList<Thread> t_bc = new ArrayList<Thread>();
		
		System.out.println("Node "+ SystemInfo.hostId + ": As a source node, Starting to broadCast: Tree is built!");
		
		for(int i = 0; i< TNNo; i++)
		{
			SctpClient sc = new SctpClient(TN[i], MesType);
			Thread t = new Thread(sc);
			t_bc.add(i,t);
			t_bc.get(i).start();
		}
		try{
			for(int i = 0; i<TNNo; i++)
			{
				t_bc.get(i).join();
			}
		}catch(InterruptedException e){
			
		}
		tree_complete_sem.release();
	}
	void initialize() 
	{
		//System Info is read from config file
		SystemInfo sysInfo = new SystemInfo(hostId);
		if(sysInfo.readConfigFile() != 0)
			return;
		//Start Token service
		Thread t_tokenService = new Thread(tokenService);
		t_tokenService.start();
		//Start Server
		runServer();
		
		try{
		Thread.sleep(200);
		}catch(InterruptedException e)
		{
			
		}
		
		clock = new Raymond_Clock(SystemInfo.hostId, SystemInfo.hostNum);
		fl = new FileLog(SystemInfo.hostId);
		
		if(SystemInfo.orig_token_nodeId == SystemInfo.hostId)//start to build tree, because it has the token by default
		{
			req_for_token_id = SystemInfo.hostId;
			first_build_mes = false;
			buildTree();
			broadCast(MESSAGE_TYPE_BUILD_TREE_COMPLETE);
			sem_req_token.release();//tell Token_Service, it has the token
			System.out.println("Node " + SystemInfo.hostId + ": Raymond:sem_req_token.release()");
		}
		try
		{
			tree_complete_sem.acquire();//(tree_complete_sem);
			
		}catch(InterruptedException e){
			
		}
	}
	
	void CS_Enter()
	{
	
		System.out.println("Node " + SystemInfo.hostId + ": Raymond:CS_Enter");
		self_req = true;
		sem_wait_req.release();
		
		try
		{
		sem_got_token.acquire();
		System.out.println("Node "+ SystemInfo.hostId + ": cs_enter acquire token");
		//self_req = false;
		
		clock.IncrementClock();
		fl.write("Node " + SystemInfo.hostId + ":"+clock.Tologic()+"\r\n");
		}catch(InterruptedException e)
		{
			
		}
		
	}
	void CS_Exit()
	{
		System.out.println("Node " + SystemInfo.hostId + ": Raymond CS_Exit");
		self_req = false;
		sem_req_token.release();// It can be transferred to other nodes.
	}

}
