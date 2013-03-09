package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;


public class GPSBroadcastReceiver extends BroadcastReceiver 
{
	private static final String 			TAG 				= "GPSBroadcastReceiver";

	
	private static SystemWideConnection		serviceConnection 	= null;

	

	@Override
	public void onReceive (Context 	context, 
						   Intent 	intent) 
	{
		ALog.v(TAG, "Entry...");

		String action = intent.getAction(); 

		if (action.equals (LocationManager.PROVIDERS_CHANGED_ACTION))
		{
			if (null == serviceConnection)
			{
				serviceConnection = new SystemWideConnection();
				
				Intent serviceIntent = new Intent(context.getApplicationContext(), 
												  MainService.class);
				
		    	if (context.getApplicationContext().bindService (serviceIntent,
									    						 serviceConnection, 
		    													 Context.BIND_AUTO_CREATE))
		    	{
		    		ALog.d(TAG, "bindService succeeded.");
		    	}
		    	else
		    	{
		    		ALog.e(TAG, "bindService failed.");
		    	}
			}
			else
			{
				serviceConnection.refreshGPSStatus();
				
	    		ALog.d(TAG, "bindService not called.");
			}
		}

		
		ALog.v(TAG, "Exit.");
	}
};
