package com.ogp.syscomprocessor;

import com.ogp.syscomprocessor.log.ALog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;

 
public class SysComBroadcastReceiver extends BroadcastReceiver
{
	private static final String		TAG					= "SysComBroadcastReceiver";
	
	private static final String		ACTION				= "com.ogp.syscomprocessor.ACTION";	
	private static final String		COMMAND				= "command";	
	private static final String		GPS_ON				= "gps_on";	
	private static final String		GPS_OFF				= "gps_off";

	private String 					beforeEnable;	

	
	@Override
	public void onReceive (Context 		context, 
						   Intent 		intent) 
	{
		ALog.v(TAG, "onReceive. Entry...");

		String action = intent.getAction();  
		
		if (null == action)
		{
			ALog.d(TAG, "onReceive. <null> action. Do nothing...");
			action = "";
		}
		else
		{
			ALog.d(TAG, "onReceive. Action: " + action);
		}
		
		
		if (action.equals (Intent.ACTION_BOOT_COMPLETED) 
			||
			action.equals (Intent.ACTION_USER_PRESENT))
		{
			SysComService.bindToService (context);
		}
		else if (action.equals (ACTION))
		{
			ALog.d(TAG, "onReceive. GPS related action encountered.");

			String	command = intent.getStringExtra (COMMAND);
			if (null != command)
			{
				if (command.equals (GPS_ON))
				{
					turnGpsOn (context);
				}
				else if (command.equals (GPS_OFF))
				{
					turnGpsOff (context);
				}
			}
				
		}
		
		ALog.v(TAG, "onReceive. Exit.");
	}


	private void turnGpsOn (Context	context) 
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


	private void turnGpsOff (Context	context) 
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
}
