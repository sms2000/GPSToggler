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
	
	private static boolean					initiated 				= false;
	
	private static Context					appContext;
	private static String 					version					= "?";
	private static boolean 					rebootRequired;
	
	private static boolean 					watchForGPSSoftware;
	private static boolean 					turnBT;
		
	
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
		
		readFromPersistantStorage();
	}
	
	
	public static void readFromPersistantStorage() 
	{
		SharedPreferences pref = appContext.getSharedPreferences (PERSISTANT_STORAGE, 
				  												  Context.MODE_PRIVATE);
		
		rebootRequired			= pref.getBoolean	(REBOOT_REQUIRED, 		rebootRequired);
		
		watchForGPSSoftware 	= pref.getBoolean 	(WATCH_GPS_SOFTWARE, 	watchForGPSSoftware);
		turnBT					= pref.getBoolean 	(TURN_BT, 				turnBT);
	}

	
	public static void writeToPersistantStorage() 
	{
		SharedPreferences pref = appContext.getSharedPreferences (PERSISTANT_STORAGE, 
																  Context.MODE_PRIVATE);
		
		Editor editor = pref.edit();
		
		editor.putBoolean	(REBOOT_REQUIRED, 		rebootRequired);

		editor.putBoolean	(WATCH_GPS_SOFTWARE, 	watchForGPSSoftware);
		editor.putBoolean	(TURN_BT, 				turnBT);
		
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
}
