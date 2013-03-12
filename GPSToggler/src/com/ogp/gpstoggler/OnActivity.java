package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;

import android.app.Activity;


public class OnActivity extends Activity 
{
	private static final String TAG 			= "OnActivity";
	

	@Override
	protected void onResume()
	{
		ALog.v(TAG, "Entry...");

		StateMachine.init (this);

		super.onResume();

		MainService.setGPSStatus (getApplicationContext(), 
				  				  true);

		finish();
		
		ALog.v(TAG, "Exit.");
	}
}
