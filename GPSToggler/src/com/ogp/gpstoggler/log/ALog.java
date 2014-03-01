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
		Log.e(prepareLog (mainTag, tag), message);
	}
	
	
	public static void w (String 	tag, 
				   		  String 	message) 
	{
		Log.w(prepareLog (mainTag, tag), message);
	}
	
	
	public static void i (String 	tag, 
				   		  String 	message) 
	{
		Log.i(prepareLog (mainTag, tag), message);
	}
	
	
	public static void d (String 	tag, 
				   		  String 	message) 
	{
		Log.d(prepareLog (mainTag, tag), message);
	}
	
	
	public static void v (String 	tag, 
				   		  String 	message) 
	{
		Log.v(prepareLog (mainTag, tag), message);
	}


	private static String prepareLog (String 	tag,
									  String 	subtug)
	{
		return tag + "." + subtug;
	}
}
