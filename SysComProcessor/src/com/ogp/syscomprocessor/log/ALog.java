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
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		if (callStack.length < 4)
		{
			Log.e(tag, message);
		}
		else
		{
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.e(mainTag, text);
		}
	}
	
	
	public static void w (String 	tag, 
				   		  String 	message) 
	{
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		if (callStack.length < 4)
		{
			Log.w(tag, message);
		}
		else
		{
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.w(mainTag, text);
		}
	}
	
	
	public static void i (String 	tag, 
				   		  String 	message) 
	{
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		if (callStack.length < 4)
		{
			Log.i(tag, message);
		}
		else
		{
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.i(mainTag, text);
		}
	}
	
	
	public static void d (String 	tag, 
				   		  String 	message) 
	{
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		if (callStack.length < 4)
		{
			Log.d(tag, message);
		}
		else
		{
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.d(mainTag, text);
		}
	}
	
	
	public static void v (String 	tag, 
				   		  String 	message) 
	{
		StackTraceElement[] callStack = Thread.currentThread().getStackTrace();
		if (callStack.length < 4)
		{
			Log.v(tag, message);
		}
		else
		{
			String file = getFileName (callStack[3].getFileName());
			String text = String.format ("%s::%d (%s)  %s", 
										 file, 
										 callStack[3].getLineNumber(),
										 callStack[3].getMethodName(),
										 message);  
			
			Log.v(mainTag, text);
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
