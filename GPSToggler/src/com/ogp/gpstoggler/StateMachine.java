package com.ogp.gpstoggler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class StateMachine 
{
	private static final String 			PERSISTANT_STORAGE 		= "GPS_TOGGLER";
	private static final String 			WATCH_GPS_SOFTWARE	 	= "WatchForWaze";
	private static final String 			TURN_BT				 	= "TurnBluetooth";
	private static final String 			REBOOT_REQUIRED		 	= "RebootRequired";
	private static final String 			USE_NOTIFICATION	 	= "UseNotification";
	private static final String 			USE_DEBUGGING		 	= "UseDebugging";
	private static final String 			SPLIT_AWARE		 		= "SplitAware";
	
	private static boolean					initiated 				= false;
	
	private static Context					appContext;
	private static String 					version					= "?";
	private static boolean 					rebootRequired;
	
	private static boolean 					watchForGPSSoftware;
	private static boolean 					turnBT;
	private static boolean 					useNotification;
	private static boolean 					useDebugging;
	private static boolean 					splitAware;
		
	
	
	private StateMachine()
	{
	}
	

	public static void init (Context		context)
	{
		if (initiated)
		{
			return;
		}
		
		initiated = true;
		
		appContext = context.getApplicationContext();
		
		
// Defaults
		watchForGPSSoftware	= false;
		turnBT				= false;
		useNotification		= false;
		useDebugging		= false;
		splitAware			= false;
		
		readFromPersistantStorage();
	}
	
	
	public static void readFromPersistantStorage() 
	{
		SharedPreferences pref = appContext.getSharedPreferences (PERSISTANT_STORAGE, 
				  												  Context.MODE_PRIVATE);
		
		rebootRequired			= pref.getBoolean	(REBOOT_REQUIRED, 		rebootRequired);
		
		watchForGPSSoftware 	= pref.getBoolean 	(WATCH_GPS_SOFTWARE, 	watchForGPSSoftware);
		turnBT					= pref.getBoolean 	(TURN_BT, 				turnBT);
		useNotification			= pref.getBoolean 	(USE_NOTIFICATION, 		useNotification);
		useDebugging			= pref.getBoolean 	(USE_DEBUGGING, 		useDebugging);
		splitAware				= pref.getBoolean 	(SPLIT_AWARE, 			splitAware);
	}

	
	public static void writeToPersistantStorage() 
	{
		SharedPreferences pref = appContext.getSharedPreferences (PERSISTANT_STORAGE, 
																  Context.MODE_PRIVATE);
		
		Editor editor = pref.edit();
		
		editor.putBoolean	(REBOOT_REQUIRED, 		rebootRequired);

		editor.putBoolean	(WATCH_GPS_SOFTWARE, 	watchForGPSSoftware);
		editor.putBoolean	(TURN_BT, 				turnBT);
		editor.putBoolean	(USE_NOTIFICATION, 		useNotification);
		editor.putBoolean	(USE_DEBUGGING, 		useDebugging);
		editor.putBoolean	(SPLIT_AWARE,			splitAware);

		editor.commit();
	}

	
	public static boolean 	getWatchGPSSoftware			()				{return watchForGPSSoftware;}
	public static void 		setWatchGPSSoftware			(boolean value)	{watchForGPSSoftware = value;}

	public static boolean 	getTurnBT					()				{return turnBT;}
	public static void 		setTurnBT					(boolean value)	{turnBT = value;}


	public static void 		setVersion					(String value) 	{version = value;}
	public static String	getVersion					() 				{return version;}


	public static boolean 	getRebootRequired			() 				{return rebootRequired;}
	public static void		setRebootRequired			(boolean value)	{rebootRequired = value;}


	public static boolean 	getUseNotification			() 				{return useNotification;}
	public static void		setUseNotification			(boolean value)	{useNotification = value;}

	public static boolean 	getUseDebugging 			() 				{return useDebugging;}
	public static void		setUseDebugging				(boolean value)	{useDebugging = value;}

	public static boolean 	getSplitAware 				() 				{return splitAware;}
	public static void		setSplitAware				(boolean value)	{splitAware = value;}
}
