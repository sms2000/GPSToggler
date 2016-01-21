package com.ogp.syscomprocessor;

import com.ogp.syscomprocessor.log.ALog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class SysComBroadcastReceiver extends BroadcastReceiver
{
	private static final String		TAG					= "SysComBroadcastReceiver";
	private static final String		GUI_PACKAGE_MASK	= "com.ogp.gpstoggler";	
	

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
		else if (action.equals (Intent.ACTION_PACKAGE_REMOVED)
			/*	||
				 action.equals (Intent.ACTION_PACKAGE_FULLY_REMOVED)*/)
		{
			ALog.d(TAG, "BroadcastReceiver::onReceive. Action: " + intent.getAction());
			
			String 	packageName = "?";
			
			try
			{
				packageName = intent.getData().getSchemeSpecificPart();
			}
			catch(Exception e)
			{
			}

			ALog.d(TAG, "BroadcastReceiver::onReceive. Package: " + packageName);
			
			if (packageName.equals (GUI_PACKAGE_MASK))
			{
				ALog.w(TAG,  "BroadcastReceiver::onReceive. Attempting to uninstall the system module.");
				
				SysComService.unbindFromService(context);
				SysComService.removeSelf (context);
			}
		}
		
		ALog.v(TAG, "onReceive. Exit.");
	}
}
