package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ActivityManagement extends BroadcastReceiver
{
	private static final String TAG 			= "ActivityManagement";


	@Override
	public void onReceive (Context 		context, 
						   Intent 		intent) 
	{
		String action = intent.getAction();
		
		if (null == action)
		{
			ALog.e(TAG, "No action available.");
			return;
		}
		
		if (action.equals (Intent.ACTION_BOOT_COMPLETED) 
			||
			action.equals (Intent.ACTION_USER_PRESENT))
		{
			if (action.equals (Intent.ACTION_BOOT_COMPLETED))
			{
				StateMachine.init (context);

				if (StateMachine.getRebootRequired())
				{
					StateMachine.setRebootRequired (false);
					StateMachine.writeToPersistantStorage();
				}
			}
						
			
			MainService.startServiceManually (context);
		}
		else if (action.equals (Intent.ACTION_MY_PACKAGE_REPLACED))
		{
			StateMachine.init (context);

			if (StateMachine.getRebootRequired())
			{
				StateMachine.setRebootRequired (false);
				StateMachine.writeToPersistantStorage();
			}

			
			MainService.startServiceManually (context);
		}
		else if (action.equals (Intent.ACTION_SCREEN_OFF))
		{
			MainService.reportScreenStatus (false);
		}
		else if (action.equals (Intent.ACTION_SCREEN_ON))
		{
			MainService.reportScreenStatus (true);
		}
		else
		{
			ALog.v(TAG, "Caught something: " + action);
		}
			
		
		ALog.w(TAG, "Action: " + intent.getAction());
	}
}
