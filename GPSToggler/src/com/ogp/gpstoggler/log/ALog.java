package com.ogp.gpstoggler.log;

import android.util.Log;


public class ALog 
{
	private static String	mainTag;
	
	static
	{
		mainTag = "ALog";
	}
	
	
	public static void e (String 	tag, 
				   		  String 	message) 
	{
		Log.e(mainTag, prepareLog (tag, message));
	}
	
	
	public static void w (String 	tag, 
				   		  String 	message) 
	{
		Log.w(mainTag, prepareLog (tag, message));
	}
	
	
	public static void i (String 	tag, 
				   		  String 	message) 
	{
		Log.i(mainTag, prepareLog (tag, message));
	}
	
	
	public static void d (String 	tag, 
				   		  String 	message) 
	{
		Log.d(mainTag, prepareLog (tag, message));
	}
	
	
	public static void v (String 	tag, 
				   		  String 	message) 
	{
		Log.v(mainTag, prepareLog (tag, message));
	}


	private static String prepareLog (String 	tag,
									  String 	message)
	{
		return "<" + tag + "> " + message;
	}
}
