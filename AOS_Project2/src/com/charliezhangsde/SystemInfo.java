package com.charliezhangsde;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SystemInfo {
	final static int MAX_NUMBER_OF_NODES = 45;
	static final String CONFIG_FILE = "config.txt";
	static int orig_token_nodeId = -1;
	static int hostNum = 0;
	static int hostId = 0;
	static ArrayList<node> allNodes = new ArrayList<node>();
	
	static int FN[] = new int[MAX_NUMBER_OF_NODES];
	static int FNNo = 0;
	
	static int[] portsforApp = new int[MAX_NUMBER_OF_NODES];
	static int req_no = 0;
	static int req_interval = 0;
	static int cs_duration = 0;
	class node{
		String hostName;
		int port;
	};
	SystemInfo()
	{
		
	}
	SystemInfo(int id)
	{
		hostId = id;
	}
	int readConfigFile()
	{
		int i;
		int count_nodes = 0;
		hostNum = 0;
		boolean gettingNo = false;
		boolean gettingtoken = false;
		boolean gettingAppPorts = false;
		int appPortCounter = 0;
		System.out.println("Node "+ hostId + ": Starting to read config file!");
		try{
			FileInputStream Fin = new FileInputStream(CONFIG_FILE);
			BufferedReader Reader = new BufferedReader(new InputStreamReader(Fin));
			String content = Reader.readLine();
			while(content != null)
			{
				if(gettingNo == true)
				{
					String[] line = content.split("\\s+");
					hostNum = Integer.valueOf(line[0]);
					gettingNo = false;
					System.out.println("Node "+ hostId + ": There are " + hostNum + " hosts");
					content = Reader.readLine();
					continue;
				}
				else if(gettingtoken == true)
				{
					String[] line = content.split("\\s+");
					orig_token_nodeId = Integer.valueOf(line[0]);
					gettingtoken = false;
					if(orig_token_nodeId == hostId)
					{
						System.out.println("Node "+ hostId + ": Token is here by default");
					}
					
					content = Reader.readLine();
					continue;
				}
				else if(gettingAppPorts == true)
				{
					String[] line = content.split("\\s+");
					int id = Integer.valueOf(line[0]);
					if(id <= hostNum)
					{
						portsforApp[appPortCounter ++ ] = Integer.valueOf(line[1]);
					}
					
				}
				if(content.startsWith("dc"))
				{
					gettingAppPorts = false;
					count_nodes ++;
					int end = content.indexOf('#');
					String subContent;
					if(end != -1)
					{
						subContent = content.substring(0, end-1);
					}
					else
					{
						subContent = content;
					}
					String[] line = subContent.split("\\s+");
					
					i = 0;
					while(i < line.length)
					{
						if(i == 0)
						{
							node n = new node();
							n.hostName = line[0];
							n.port = Integer.valueOf(line[1]);
							allNodes.add(n);
							
							System.out.println("Node " + hostId+ ": node " + count_nodes + " is " + allNodes.get(count_nodes -1).hostName + " port is " + allNodes.get(count_nodes -1).port);
							if(hostId == count_nodes)//??
							{
								i = i+2;
								continue;
							}
							else
								break;
						}
						else
						{
							FN[FNNo] = Integer.valueOf(line[i]);
							FNNo++;
						}
						i++;
					}
					
						
				}
				else if(content.startsWith("# Number of nodes"))
				{
					gettingNo = true;
				}
				else if(content.startsWith("# Node Id holding token first"))
				{
					gettingtoken = true;
				}
				else if(content.startsWith("# Port numbers for application"))
				{
					gettingAppPorts = true;
				}
				else if(content.startsWith("# number of critical"))
				{
					content = Reader.readLine();
					String line[] = content.split("\\s+");
					req_no = Integer.valueOf(line[0]);
					System.out.println("Node " + hostId+ ": The number of critical section requests is: " + req_no);
				}
				else if(content.startsWith("# Mean delay"))
				{
					content = Reader.readLine();
					String line[] = content.split("\\s+");
					req_interval = Integer.valueOf(line[0]);
					System.out.println("Node " + hostId+ ": Mean C.S. request delay is: " + req_interval);
				}
				else if(content.startsWith("# Mean duration"))
				{
					content = Reader.readLine();
					String line[] = content.split("\\s+");
					cs_duration = Integer.valueOf(line[0]);
					System.out.println("Node " + hostId+ ": Mean C.S. execution duration is: " + cs_duration);
				}
				content = Reader.readLine();
						
			}
			
			System.out.print("Node "+ hostId+ ": The neighbors read from file are/is: ");
			for(int j = 0; j<FNNo; j++)
			{
				System.out.print(" " + FN[j]);
			}
			System.out.println("");
			
			Reader.close();
		}catch(FileNotFoundException e)
		{
			return 1;// fail
		}catch(IOException e)
		{
			return 1;
		}
		if(gettingNo == true)//port has not been not got
		{
			return 1;
		}
		else
			return 0;
	}
	

}
