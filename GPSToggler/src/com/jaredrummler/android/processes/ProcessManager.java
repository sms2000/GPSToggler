package com.jaredrummler.android.processes;

import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ProcessManager 
{
	public static List<AndroidAppProcess> getRunningAppProcesses() 
	{
		List<AndroidAppProcess> processes 	= new ArrayList<AndroidAppProcess>();
		File[] 					files 		= new File("/proc").listFiles();
    
		for (File file : files) 
		{
			if (file.isDirectory()) 
			{
				int pid;
				
				try 
				{
					pid = Integer.parseInt(file.getName());
				} 
				catch (NumberFormatException e) 
				{
					continue;
				}
				
				try 
				{
					processes.add (new AndroidAppProcess(pid));
				} 
				catch (Exception e) 
				{
				}
			}
		}
		
		return processes;
	}
}
