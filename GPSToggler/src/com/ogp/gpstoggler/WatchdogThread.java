package com.ogp.gpstoggler;

import android.os.Handler;
import android.widget.Toast;
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
		listOfApps.add ("com.google.android.apps.maps");
		
		listOfApps.add ("com.navngo.");
		listOfApps.add ("com.nng.");
		
	}
	
	
	private class StatusChange implements Runnable
	{
		private boolean enableGPS;
		private String  application;
		
		
		private StatusChange(boolean enableGPS, String application)
		{
			this.enableGPS   = enableGPS;
			this.application = application;
		}

		
		@Override
		public void run() 
		{
			try
			{
				mainService.reportGPSSoftwareStatus (enableGPS);
				ALog.v(TAG, "StatusChange::run. reportGPSSoftwareStatus succeeded for " + enableGPS);
				
				
				String text = mainService.getResources().getString (enableGPS ? R.string.gps_app_on : R.string.gps_app_off);

				if (enableGPS)
				{
					text = String.format(text, 
							             application);
				}
				
				Toast.makeText (mainService.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
			}
			catch(Exception e)
			{
				ALog.e(TAG, "StatusChange::run. EXC(1)");
			}
		}
	}
	
	
	public WatchdogThread(MainService		mainService)
	{
		ALog.v(TAG, "WatchdogThread. Entry...");
		
		this.mainService		= mainService;
		this.runThread			= true;
		this.activityManager	= (ActivityManager)mainService.getSystemService (Activity.ACTIVITY_SERVICE);
		this.startTime			= System.currentTimeMillis();
		
		
		start();

		ALog.v(TAG, "WatchdogThread. Exit.");
	}
	
	
	public void finish()
	{
		ALog.v(TAG, "finish. Entry...");

		try 
		{
			runThread = false;
			
			interrupt();
			join();
		} 
		catch (InterruptedException e) 
		{
			ALog.e(TAG, "finish. EXC(1)");
		}

		ALog.v(TAG, "finish. Exit.");
	}
	
	@Override
	public void run()
	{
		ALog.v(TAG, "run. Entry...");

		while (runThread)
		{
			if (StateMachine.getWatchGPSSoftware() 
				&& 
				System.currentTimeMillis() - startTime > LAZY_START)
			{
				if (StateMachine.getUseDebugging())
				{
					ALog.v(TAG,  "run. Checking: ... ?");
				}
				
				verifyGPSSoftwareRunning();
			}
			else
			{
				if (StateMachine.getUseDebugging())
				{
					ALog.v(TAG,  "run. Skipping check...");
				}
			}
			
			try 
			{
				Thread.sleep (statusSaved ? WATCHDOG_ON : WATCHDOG_OFF);
			} 
			catch (InterruptedException e) 
			{
			}
		}

		ALog.v(TAG, "run. Exit.");
	}


	private void verifyGPSSoftwareRunning() 
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
							ALog.w(TAG, "verifyGPSSoftwareRunning. GPS status active due to foreground process: " + found);
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
			ALog.w(TAG, "verifyGPSSoftwareRunning. GPS software status changed. Now it's " + (statusSaved ? "running." : "stopped."));

			handler.post(new StatusChange (statusSaved, 
										   found));
		}
	}
}


