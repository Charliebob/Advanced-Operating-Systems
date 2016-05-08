package com.charliezhangsde;
public class Raymond_Clock
{
	int[] clock;
	int hostId;
	
	Raymond_Clock(int id, int size)
	{
		hostId = id;
		
		clock = new int[size];
		for(int i = 0; i<size; i++)
		{
			clock[i] = 0;
		}
	}
	void IncrementClock()
	{
		
		clock[hostId-1] ++;
	}
	void updateClock(int[] c)
	{
		for(int i = 0; i<clock.length; i++)
		{
			if(clock[i]<c[i])
			{
				clock[i] = c[i];
			}
		}
	}
	void updateClock(String c)
	{
		String[] arr = c.split("\\s+");
		System.out.println("clock: update"+ c + " " + c.length());
		for(int i = 0; i<clock.length; i++)
		{
			clock[i] = Integer.valueOf(arr[i]);
		}
	}
	String toStringClock()
	{
		String c = new String();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<clock.length; i++)
		{
			sb.append(Integer.toString(clock[i]));
			if(i != clock.length-1)
				sb.append(" ");
		}
		c = sb.toString();
		System.out.println("clock: to string: " + c);
		return c;
	}
	Long Tologic()
	{
		Long result = new Long(0);
		for(int i = 0; i < clock.length; i++)
		{
			result += new Long(clock[i]);
		}
		System.out.println("Raymond_Clock: addition: " + result);
		return result;
	}
}
