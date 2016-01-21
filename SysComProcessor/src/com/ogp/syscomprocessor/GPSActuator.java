package com.ogp.syscomprocessor;

import com.ogp.syscomprocessor.log.ALog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.LocationManager;
import android.provider.Settings;


public class GPSActuator 
{
	private static final String		TAG					= "GPSActuator";
	
	private static String 			beforeEnable		= null;	

	
	public static void setGpsStatus(Context context, boolean enable)
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
		{
			if (enable)
			{ 
				turnGpsOn21(context); 
			}
			else
			{
				turnGpsOff21(context);
			}
		}
		else
		{
			if (enable)
			{ 
				turnGpsOn(context); 
			}
			else
			{
				turnGpsOff(context);
			}
		}
	}
	
	
	public static boolean getGpsStatus(Context context)
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
		{
			return askGps21(context); 
		}
		else
		{
			return askGps(context); 
		}
		
	}
	
	
	@SuppressWarnings("deprecation")
	private static void turnGpsOn (Context	context) 
	{
		ALog.v(TAG, "turnGpsOn. Entry...");

		beforeEnable = Settings.Secure.getString (context.getContentResolver(),
					  							  Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		String newSet = String.format ("%s,%s",
									   beforeEnable,
									   LocationManager.GPS_PROVIDER);

		try
		{
			Settings.Secure.putString (context.getContentResolver(),
									   Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
									   newSet);	
			ALog.i(TAG, "turnGpsOn. New string: " + newSet);
		}
		catch(Exception e)
		{
			ALog.e(TAG, "turnGpsOn. !!! Exception !!!");
			e.printStackTrace();
		}

		ALog.v(TAG, "turnGpsOn. Exit.");
	}


	@SuppressWarnings("deprecation")
	private static void turnGpsOff (Context	context) 
	{
		ALog.v(TAG, "turnGpsOff. Entry...");

		if (null == beforeEnable)
		{
			String str = Settings.Secure.getString (context.getContentResolver(),
						  							Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if (null == str)
			{
				str = "";
			}
			else
			{				
				String[] list = str.split (",");

				str = "";
				
				int j = 0;
				for (int i = 0; i < list.length; i++)
				{
					if (!list[i].equals (LocationManager.GPS_PROVIDER))
					{
						if (j > 0)
						{
							str += ",";
						}
						
						str += list[i];
						j++;
					}
				}
				
				beforeEnable = str;
			}
		}
		
		try
		{
			Settings.Secure.putString (context.getContentResolver(),
									   Settings.Secure.LOCATION_PROVIDERS_ALLOWED,
									   beforeEnable);
			ALog.i(TAG, "turnGpsOff. New string: " + beforeEnable);
		}
		catch(Exception e)
		{
			ALog.e(TAG, "turnGpsOff. !!! Exception !!!");
			e.printStackTrace();
		}

		ALog.v(TAG, "turnGpsOff. Exit.");
	}

	
	private static boolean askGps(Context context) 
	{
		ALog.v(TAG, "askGps. Entry...");

		try
		{
			@SuppressWarnings("deprecation")
			String gpsNow = Settings.Secure.getString (context.getContentResolver(),
					  								   Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

			ALog.i(TAG, "turnGpsOff. Current string: " + gpsNow);
			ALog.v(TAG, "askGps. Exit.");

			return gpsNow.contains(LocationManager.GPS_PROVIDER);
		}
		catch(Exception e)
		{
			ALog.e(TAG, "askGps. !!! Exception !!!");
			e.printStackTrace();
			
			return false;
		}
	}


	@SuppressLint("InlinedApi")
	private static void turnGpsOn21 (Context	context) 
	{
		ALog.v(TAG, "turnGpsOn21. Entry...");

		try
		{
			Settings.Secure.putInt (context.getContentResolver(),
								    Settings.Secure.LOCATION_MODE,
								    Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);	
	
			ALog.i(TAG, "turnGpsOn21. New value: " + Settings.Secure.getInt(context.getContentResolver(),
				    														Settings.Secure.LOCATION_MODE));
		}
		catch(Exception e)
		{
			ALog.e(TAG, "turnGpsOn21. !!! Exception !!!");
		}

		ALog.v(TAG, "turnGpsOn21. Exit.");
	}


	@SuppressLint("InlinedApi")
	private static void turnGpsOff21 (Context	context) 
	{
		ALog.v(TAG, "turnGpsOff21. Entry...");

		try
		{
			Settings.Secure.putInt (context.getContentResolver(),
									Settings.Secure.LOCATION_MODE,
									Settings.Secure.LOCATION_MODE_BATTERY_SAVING);

			ALog.i(TAG, "turnGpsOn21. New value: " + Settings.Secure.getInt(context.getContentResolver(),
																			Settings.Secure.LOCATION_MODE));
		}
		catch(Exception e)
		{
			ALog.e(TAG, "turnGpsOff. !!! Exception !!!");
			e.printStackTrace();
		}

		ALog.v(TAG, "turnGpsOf21. Exit.");
	}

	
	@SuppressLint("InlinedApi")
	private static boolean askGps21(Context context) 
	{
		ALog.v(TAG, "askGps21. Entry...");

		try
		{
			int gpsNow = Settings.Secure.getInt (context.getContentResolver(),
											     Settings.Secure.LOCATION_MODE);

			ALog.i(TAG, "askGps21. Current value: " + gpsNow);
			ALog.v(TAG, "askGps21. Exit.");
			
			return Settings.Secure.LOCATION_MODE_HIGH_ACCURACY == gpsNow;
		}
		catch(Exception e)
		{
			ALog.e(TAG, "askGps21. !!! Exception !!!");
			e.printStackTrace();
			
			return false;
		}
	}
}
