package com.ogp.gpstoggler;

import android.os.Handler;
import android.app.Activity;
import android.app.ActivityManager;

import java.util.List;

import com.ogp.gpstoggler.log.ALog;


public class WatchdogThread extends Thread
{
	private static final String		TAG								= "WatchdogThread";
	
	private static final long 		WATCHDOG_OFF	 				= 2000;
	private static final long 		WATCHDOG_ON	 					= 5000;
	private static final String		WAZE_PROCESS 					= "com.waze";
	private static final String		IGO_PROCESS 					= "com.navngo.igo";
	private static final String		MAPS_PROCESS 					= "com.google.android.apps.maps";
	
	private MainService				mainService;
	private ActivityManager 		activityManager;
	private boolean					runThread;
	private boolean					statusSaved 					= false;
	private Handler					handler							= new Handler();
	
	
	private class StatusChange implements Runnable
	{
		private boolean enableGPS;
		
		
		private StatusChange(boolean enableGPS)
		{
			this.enableGPS = enableGPS;
		}

		
		@Override
		public void run() 
		{
			try
			{
				mainService.reportGPSSoftwareStatus (enableGPS);
				ALog.v(TAG, "reportWazeStatus succeeded for " + enableGPS);
			}
			catch(Exception e)
			{
				ALog.e(TAG, "EXC(1)");
			}
		}
	}
	
	
	public WatchdogThread(MainService		mainService)
	{
		ALog.v(TAG, "Entry...");
		
		this.mainService		= mainService;
		this.runThread			= true;
		this.activityManager	= (ActivityManager)mainService.getSystemService (Activity.ACTIVITY_SERVICE);
		
		start();

		ALog.v(TAG, "Exit.");
	}
	
	
	public void finish()
	{
		ALog.v(TAG, "Entry...");

		try 
		{
			runThread = false;
			
			interrupt();
			join();
		} 
		catch (InterruptedException e) 
		{
			ALog.e(TAG, "EXC(1)");
		}

		ALog.v(TAG, "Exit.");
	}
	
	@Override
	public void run()
	{
		ALog.v(TAG, "Entry...");

		while (runThread)
		{
			verifyWazeRunning();
			
			try 
			{
				Thread.sleep (statusSaved ? WATCHDOG_ON : WATCHDOG_OFF);
			} 
			catch (InterruptedException e) 
			{
			}
		}

		ALog.v(TAG, "Exit.");
	}


	private void verifyWazeRunning() 
	{
		List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
		boolean										statusNow	= false;
		
		
		for (ActivityManager.RunningAppProcessInfo iterator : list)
		{
			if (iterator.processName.contains (WAZE_PROCESS)
				||
				iterator.processName.contains (IGO_PROCESS)
				||
				iterator.processName.contains (MAPS_PROCESS))
			{
				if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND >= iterator.importance)
				{
					statusNow = true;
					break;
				}
			}
		}

		if (statusNow != statusSaved)
		{
			statusSaved = statusNow;
			ALog.i(TAG, "GPS software status changed. Now it's " + (statusSaved ? "running." : "stopped."));

			handler.post(new StatusChange (statusSaved));
		}
	}
}


