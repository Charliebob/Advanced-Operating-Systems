package com.charliezhangsde;
import java.io.FileWriter;
import java.io.IOException;


public class FileLog {

		FileWriter fw = null;
		int hostId = 0;
		FileLog(int id)
		{
			hostId = id;
		}
		void write(String s)
		{
			try
			{
				String name = new String("log" + hostId+".txt");
				fw = new FileWriter(name, true); 
				fw.append(s);
				fw.flush();
			}catch(IOException e){
				
			}
		}
		void finish()
		{
			try
			{
				fw.close();
			}catch(IOException e){
				
			}
		}
		
	
}
