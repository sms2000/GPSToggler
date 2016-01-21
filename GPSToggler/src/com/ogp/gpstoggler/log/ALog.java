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
		Log.e(mainTag, prepareLog (mainTag, message));
	}
	
	
	public static void w (String 	tag, 
				   		  String 	message) 
	{
		Log.w(mainTag, prepareLog (mainTag, message));
	}
	
	
	public static void i (String 	tag, 
				   		  String 	message) 
	{
		Log.i(mainTag, prepareLog (mainTag, message));
	}
	
	
	public static void d (String 	tag, 
				   		  String 	message) 
	{
		Log.d(mainTag, prepareLog (mainTag, message));
	}
	
	
	public static void v (String 	tag, 
				   		  String 	message) 
	{
		Log.v(mainTag, prepareLog (mainTag, message));
	}


	private static String prepareLog (String 	tag,
									  String 	subtug)
	{
		return tag + "::" + subtug;
	}
}
