package com.charliezhangsde;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Test {

	static boolean compareArrayValue(int[] t1, int[] t2)//true: some elements are the same
	{
		boolean result = false;
		for(int i = 0; i < t1.length; i++)
		{
			for(int j = 0; j <t2.length; j ++)
			{
				if(t1[i] == t2[j])
				{
					System.out.println((i+1) + "th element and " + (j+1) + "th element");
					return true;
				}
			}
		}
		return result;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SystemInfo si = new SystemInfo(0);
		si.readConfigFile();
		System.out.println("Test: There are "+ SystemInfo.hostNum + "nodes!");
		ArrayList<int[]> logValue = new ArrayList<int[]>();
		BufferedReader br = null;
		boolean result = true;
		try
		{
			String Line;
			String[] Line_split;
			boolean more_en = false;
			for(int i = 1; i<= SystemInfo.hostNum; i++)
			{
				int j = 0;
				int[] time = new int[SystemInfo.req_no];
				String filename = new String("log" + i +".txt");
				 
				br = new BufferedReader(new FileReader(filename));
	 
				while (((Line = br.readLine()) != null)) {
					if( j>=SystemInfo.req_no)
					{
						more_en = true;
						break;
					}
					//System.out.println(Line);
					Line_split = Line.split(":");
					//System.out.println(Line_split[1] + " size: " + Line_split[1].length());
					time[j++] = Integer.valueOf(Line_split[1]);
				}
				logValue.add(time);
				if(j == SystemInfo.req_no)
				{
					if(more_en == false)
						System.out.println(filename + " is read! And it has entered into the critical section for " + j + " times.");
					else
					{
						System.out.println(filename + " is read! And it has entered into the critical section for more than" + j + " times.");
						result = false;
					}
				}
				else
				{
					System.out.println(filename + " is read! But it hasn't entered into the critical section for " + j + " times.");
					
					result = false;
				}
			}
			if(result == false)
			{
				System.out.println("The algorithm can't work properly for letting applications enter into critical section.");
				br.close();
				return;
			}
		}catch(IOException e){
			
		}
		int[] tmp1;
		int[] tmp2;
		boolean res = false;
		for(int i = 0; i<logValue.size(); i ++)
		{	
			tmp1 = logValue.get(i);
			for(int j = i+1; j<logValue.size(); j++)
			{
				tmp2 = logValue.get(j);
				if(i != j)
				{
					//compare the two array
					res = compareArrayValue(tmp1,tmp2);
					if(res == true)
					{
						System.out.println("log"+(i+1)+".txt and log" + (j+1)+".txt have some same clocks");
						break;
					}
					else
						System.out.println("log"+(i+1)+".txt and log" + (j+1)+".txt are totally different");
				}
			}
			if(res == true)
				break;
		}
		if(res == true)
		{
			System.out.println("There are some nodes which have entered into the critical section concurrently");
		}
		else
		{
			System.out.println("No any two nodes have entered into the critical section concurrently");
		}
	}
}
