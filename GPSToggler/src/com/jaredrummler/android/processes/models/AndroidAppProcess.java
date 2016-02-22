package com.jaredrummler.android.processes.models;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.text.TextUtils;


public class AndroidAppProcess 
{
	public boolean 		foreground;
	public final String name;
	public final int 	pid;
	
	
	public AndroidAppProcess(int pid) throws IOException 
	{
		this.pid  		= pid;
		this.name 		= getProcessName(pid);
		this.foreground = CPUSet.get(pid).isForeground();
	}


	public String getPackageName() 
	{
		return name.split(":")[0];
	}

	
	@SuppressLint("DefaultLocale")
	static public String getProcessName(int pid) throws IOException 
	{
		String cmdline = null;
  
		try 
		{
			cmdline = ProcFile.readFile(String.format("/proc/%d/cmdline", pid)).trim();
		} 
		catch (IOException ignored) 
		{
		}
		
		if (TextUtils.isEmpty(cmdline)) 
		{
			return Stat.get(pid).getComm();
		}
		
		return cmdline;
	}
}
