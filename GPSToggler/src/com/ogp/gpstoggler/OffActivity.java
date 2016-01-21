package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;

import android.app.Activity;


public class OffActivity extends Activity 
{
	private static final String TAG 			= "OffActivity";
	

	@Override
	protected void onResume()
	{
		ALog.v(TAG, "Entry...");

		StateMachine.init (this);

		super.onResume();

		MainService.setGpsStatus (false);

		finish();

		ALog.v(TAG, "Exit.");
	}
}
