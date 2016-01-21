package com.jaredrummler.android.processes.models;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


@SuppressWarnings("serial")
public class ProcFile extends File
{
	public final String content;

	  
	protected static String readFile(String path) throws IOException 
	{
		BufferedReader reader = null;
    
		try 
		{
			StringBuilder output = new StringBuilder();
			reader = new BufferedReader(new FileReader(path));
      
			for (String line = reader.readLine(), newLine = ""; line != null; line = reader.readLine()) 
			{
				output.append(newLine).append(line);
				newLine = "\n";
			}
			
			return output.toString();
		} 
		finally 
		{
			if (reader != null) 
			{
				reader.close();
			}
		}
	}


	protected ProcFile(String path) throws IOException 
	{
		super(path);
    
		content = readFile(path);
	}
}
