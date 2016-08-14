package com.jaredrummler.android.processes.models;

import java.io.IOException;


@SuppressWarnings("serial")
public final class Status extends ProcFile 
{
	public static Status get(int pid) throws IOException {
		return new Status(String.format("/proc/%d/status", pid));
	}

	  
	private Status(String path) throws IOException {
	    super(path);
	}


	public String getValue(String fieldName) {
		String[] lines = content.split("\n");
		
	    for (String line : lines) {
	    	if (line.startsWith(fieldName + ":")) {
	    		return line.split(fieldName + ":")[1].trim();
	    	}
	    }
	    
	    return null;
	}
}
