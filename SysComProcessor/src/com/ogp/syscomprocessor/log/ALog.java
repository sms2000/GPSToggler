package com.ogp.syscomprocessor.log;

import android.util.Log;


public class ALog 
{
	private static String	mainTag;
	
	
	static
	{
		mainTag = "SLog";
	}
	
	
	public static void e (String 	tag, 
				   		  String 	message) 
	{
		try
		{
			StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.e(mainTag, text);
		}
		catch(Exception e)
		{
			Log.e(tag, message);
		}
	}
	
	
	public static void w (String 	tag, 
				   		  String 	message) 
	{
		try
		{
			StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.w(mainTag, text);
		}
		catch(Exception e)
		{
			Log.w(tag, message);
		}
	}
	
	
	public static void i (String 	tag, 
				   		  String 	message) 
	{
		try
		{
			StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.i(mainTag, text);
		}
		catch(Exception e)
		{
			Log.i(tag, message);
		}
	}
	
	
	public static void d (String 	tag, 
				   		  String 	message) 
	{
		try
		{
			StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.d(mainTag, text);
		}
		catch(Exception e)
		{
			Log.d(tag, message);
		}
	}
	
	
	public static void v (String 	tag, 
				   		  String 	message) 
	{
		try
		{
			StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.v(mainTag, text);
		}
		catch(Exception e)
		{
			Log.v(tag, message);
		}
	}

	
	private static String getFileName(String fileName) 
	{
		String file = fileName;
		
		if (file.endsWith (".java"))
		{
			file = (String)file.subSequence (0,  file.length() - 5);
		}
		
		return file;
	}
}
