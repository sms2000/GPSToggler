package com.ogp.gpstoggler;

import android.os.Handler;
import android.app.Activity;
import android.app.ActivityManager;

import java.util.ArrayList;
import java.util.List;

import com.ogp.gpstoggler.log.ALog;


public class WatchdogThread extends Thread
{
	private static final String		TAG								= "WatchdogThread";
	
	private static final long 		WATCHDOG_OFF	 				= 2000;	// Polling time (ms) when off
	private static final long 		WATCHDOG_ON	 					= 5000; // Polling time (ms) when on
	private static final long 		LAZY_START 						= 5000; // Lazy start 
	
	private static List<String>		listOfApps						= new ArrayList<String>();
	
	private MainService				mainService;
	private ActivityManager 		activityManager;
	private boolean					runThread;
	private boolean					statusSaved 					= false;
	private Handler					handler							= new Handler();
	private long 					startTime;
	
	
	static
	{
		listOfApps.add ("com.waze");
		listOfApps.add ("com.navngo.igo");
		listOfApps.add ("com.google.android.apps.maps");
	}
	
	
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
		this.startTime			= System.currentTimeMillis();
		
		
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
			if (System.currentTimeMillis() - startTime > LAZY_START)
			{
				verifyWazeRunning();
			}
			
			
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
		String										found		= null;
		
		
		for (ActivityManager.RunningAppProcessInfo iterator : list)
		{
			if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND >= iterator.importance)
			{
				synchronized(listOfApps)
				{
					for (String iterator2 : listOfApps)
					{
						if (iterator.processName.contains (iterator2)) 
						{
							found = iterator2;
							ALog.i(TAG, "GPS status active due to foreground process: " + found);
							break;
						}
					}
				}
			}
			
			
			if (null != found)
			{
				statusNow = true;
				break;
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


