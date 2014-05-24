package com.ogp.syscomprocessor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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
	private static final String			GUI_PACKAGE			= "com.ogp.gpstoggler";	
	
	private static final int 			PRIORITY 			= 1000;
	
	
	static private SysComService		thisService;
	static private ServiceConnection 	serviceConnection;
	private boolean 					receiverInit		= false;

	
	
	public final SysComServiceInterface.Stub mainServiceBinder = new SysComServiceInterface.Stub() 
	{
		@Override
		public void bindCallback() throws RemoteException 
		{
			ALog.v(TAG, "SysComServiceInterface.Stub::bindCallback. Entry...");

			ALog.v(TAG, "SysComServiceInterface.Stub::bindCallback. Exit.");
		}
	};

	
	public final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive (Context 	context, 
							   Intent 	intent) 
		{
			ALog.v(TAG, "BroadcastReceiver::onReceive. Entry...");

			String action = intent.getAction();
			
			if (null == action)
			{
				ALog.w (TAG, "BroadcastReceiver::onReceive. Unknown action received."); 
				
				action = "";
			}
			
			if (action.equals (Intent.ACTION_PACKAGE_REMOVED)
/*				||
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
				
				if (packageName.equals (GUI_PACKAGE))
				{
					ALog.w(TAG,  "BroadcastReceiver::onReceive. Attempting to uninstall the system module.");
					
					unbindFromService (thisService);
					removeSelf (thisService);
				}
			}
			
			
			ALog.d(TAG, "BroadcastReceiver::onReceive. Processed action: " + action);
			ALog.v(TAG, "BroadcastReceiver::onReceive. Exit.");
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
		
		
		try
		{
			serviceConnection.toString();
			ALog.e(TAG, "Repeated call.");
		}
		catch(Exception e)
		{
			Intent serviceIntent = new Intent(context.getApplicationContext(), 
					  						  SysComService.class);

			context.startService (serviceIntent);
		}
		
		
		ALog.v(TAG, "bindToService. Exit.");
	}

	
	public static void unbindFromService (Context context) 
	{
		ALog.v(TAG, "unbindFromService. Entry...");

		if (null != serviceConnection)
		{
			Intent serviceIntent = new Intent(context.getApplicationContext(), 
					  						  SysComService.class);

			context.startService (serviceIntent);
			
			ALog.d(TAG, "unbindService finished.");
		}
		else
		{
			ALog.e(TAG, "unbindService not called.");
		}

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

	
	private void removeSelf (Context context)
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
