package com.jaredrummler.android.processes.models;

import java.io.IOException;


@SuppressWarnings("serial")
public final class Stat extends ProcFile 
{
	private final String[] fields;

	
	public static Stat get(int pid) throws IOException 
	{
		return new Stat(String.format("/proc/%d/stat", pid));
	}


	private Stat(String path) throws IOException 
	{
		super(path);
		fields = content.split("\\s+");
	}


	public String getComm() 
	{
		return fields[1].replace("(", "").replace(")", "");
	}
}
