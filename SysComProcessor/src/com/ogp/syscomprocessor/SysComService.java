package com.ogp.syscomprocessor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.ogp.syscomprocessor.KernelServices.Mount;
import com.ogp.syscomprocessor.log.ALog;


public class SysComService extends Service 
{
	private static final String 		TAG 				= "SysComService";
	
	private static final String 		PACKAGE_SCHEME 		= "package";
	private static final String 		SYSTEM_FS	 		= "/system";
	private static final String 		SYSTEM_DIRECTORY	= "/system/app/";
	private static final String 		XBIN_DIRECTORY		= "/system/xbin/";
	private static final String 		MODULE_NAME 		= "SysComProcessor.apk";
	private static final String 		NATIVE_RUNNER		= "liboperator.so";
	
	private static final int 			PRIORITY 			= 1000;
	
	
	static private SysComService		thisService;
	
	private boolean 					receiverInit		= false;
	private SysComBroadcastReceiver		broadcastReceiver	= new SysComBroadcastReceiver(); 
	
	
	public final SysComServiceInterface.Stub mainServiceBinder = new SysComServiceInterface.Stub() 
	{
		@Override
		public void setGpsStatus(boolean gpsStatus) throws RemoteException 
		{
			GPSActuator.setGpsStatus(thisService, gpsStatus);

			ALog.i(TAG, String.format ("SysComServiceInterface.Stub::setGpsStatus. Setting new GPS status: [%s]", gpsStatus ? "ON" : "OFF"));
		}


		@Override
		public boolean getGpsStatus() throws RemoteException 
		{
			boolean gpsStatus = GPSActuator.getGpsStatus(thisService); 

			ALog.i(TAG, String.format ("SysComServiceInterface.Stub::getGpsStatus. [%s]", gpsStatus ? "ON" : "OFF"));

			return gpsStatus;
		}
	};

	
	public SysComService()
	{
		super();

		ALog.v(TAG, "SysComService. Constructor called.");
	}
	
	
	
	@Override
	public IBinder onBind (Intent arg) 
	{
		return mainServiceBinder;
	}

	
	@Override
	public void onCreate()
	{
		ALog.v(TAG, "onCreate. Entry...");

		thisService = this;
		
		super.onCreate();

		ALog.v(TAG, "onCreate. Exit.");
	}

	
	@Override
	public int onStartCommand (Intent 	intent, 
							   int 		flags, 
							   int 		startId)
	{
		ALog.v(TAG, "onStartCommand. Entry...");

		startReceivers (thisService);
		
		ALog.v(TAG, "onStartCommand. Exit.");
		
		return START_STICKY;
	}
	
	
	@Override
	public void onDestroy()
	{
		ALog.v(TAG, "onDestroy. Entry...");

		super.onDestroy();

		stopReceivers (thisService);

		ALog.v(TAG, "onDestroy. Exit.");
	}
	
	
	public static void bindToService (Context context) 
	{
		ALog.v(TAG, "bindToService. Entry...");
		
		
		Intent serviceIntent = new Intent(context.getApplicationContext(), 
				  						  SysComService.class);

		context.startService (serviceIntent);
		
		ALog.v(TAG, "bindToService. Exit.");
	}

	
	public static void unbindFromService (Context context) 
	{
		ALog.v(TAG, "unbindFromService. Entry...");

		Intent serviceIntent = new Intent(context.getApplicationContext(), 
				  						  SysComService.class);

		context.startService (serviceIntent);
			
		ALog.v(TAG, "unbindFromService. Exit.");
	}

	
	private void startReceivers (Context context) 
	{
		ALog.v(TAG, "startReceivers. Entry...");

		if (!receiverInit)
		{
			IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
/*			filter.addAction (Intent.ACTION_PACKAGE_FULLY_REMOVED); */
			filter.addDataScheme (PACKAGE_SCHEME);
			filter.setPriority (PRIORITY);
			
			context.registerReceiver (broadcastReceiver, 
									  filter);
			
			receiverInit = true;
		}
		
		ALog.v(TAG, "startReceivers. Exit.");
	}


	private void stopReceivers (Context context) 
	{
		ALog.v(TAG, "stopReceivers. Entry...");

		try
		{
			if (receiverInit)
			{
				context.unregisterReceiver (broadcastReceiver);
				ALog.w(TAG, "Success.");
				
				receiverInit = false;
			}
		}
		catch(Exception e)
		{
			ALog.e(TAG, "stopReceivers. EXC(1)");
		}

		ALog.v(TAG, "stopReceivers. Exit.");
	}

	
	public static void removeSelf (Context context)
	{
		ALog.v(TAG, "removeSelf. Entry...");
		
		int	pid = android.os.Process.myPid();
		context = context.getApplicationContext();
		
		Mount sysFS = new KernelServices().findFS (SYSTEM_FS);
		if (null != sysFS)
		{
			try
			{
				String 				command;
				
			    command  = XBIN_DIRECTORY + NATIVE_RUNNER;
 		    	command += " " + "remove4system";
		    	command += " " + SYSTEM_DIRECTORY + MODULE_NAME;
		    	command += " " + SYSTEM_FS; 
		    	command += " " + sysFS.mDevice; 
		    	command += " " + sysFS.mType; 
		    	command += " " + "remove-self";
		    	command += "\n"; 

			    ALog.w(TAG, "removeSelf. This must be the last print. Any diagnostics printed after this line is errorneous!");

			    Runtime.getRuntime().exec (command);
			}
			catch(Exception e)
			{
			}
		}
		

		android.os.Process.killProcess (pid);
		
		
		ALog.e(TAG, "removeSelf. Native code portion failed.");
		ALog.v(TAG, "removeSelf. Exit.");
	}
}
