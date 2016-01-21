package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;
import com.ogp.syscomprocessor.SysComServiceInterface;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;


public class MainService extends Service
{
	private static final String 		TAG 					= "MainService";
	
	public static final String			GPS 					= "gps";
	public static final String 			GPS_MAIN 				= "com.ogp.syscomprocessor.MAIN";
	public static final String 			GPS_ACTION 				= "com.ogp.syscomprocessor.ACTION";
	public static final String 			GPS_COMMAND				= "command";
	public static final String 			GPS_ON					= "gps_on";
	public static final String 			GPS_OFF					= "gps_off";

	public static final String 			GPS_STATUS				= "status";
	public static final String 			GPS_PAYLOAD				= "payload";
	public static final String 			GPS_SWAPSTATUS			= "swapPSStatus";
	public static final String 			GPS_REFRESH				= "refreshGPSStatus";

	private static final long 			DOUBLE_CLICK_DELAY 		= 300;			// 300 ms
	private static final long 			SCREEN_OFF_DELAY		= 5000;			// 5 seconds		
	
	private static MainService			thisService				= null;
	private Handler						messageHandler			= new Handler();
	private ActivityManagement 			activityManagement;
	private WatchdogThread 				watchdogThread;
	private boolean						gpsSoftwareStatus		= false;
	private long 						firstClickTime			= 0;
	private boolean 					sysComServiceBound		= false;
	private SysComServiceInterface 		sysComService			= null;

	
	
	private final MainServiceInterface.Stub mainServiceBinder = new MainServiceInterface.Stub() 
	{
		public void refreshGPSStatus() throws DeadObjectException 
		{
			internalRefreshGPSStatus();
		}
	};

	
	private final BroadcastReceiver			broadcastReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive (Context 	context,
							   Intent 	intent) 
		{
        	ALog.v(TAG, "Entry...");
        	
            String payload = intent.getStringExtra (GPS_PAYLOAD);
            
            if (null != payload 
            	&& 
            	payload.equals (GPS_SWAPSTATUS))
            {
            	processClickOverWidget (context.getApplicationContext());
				
            	ALog.w(TAG, "Called 'RefreshGRPStatus'.");
            }

            
            ALog.v(TAG, "Exit.");
		}
	};

																	
	protected final ServiceConnection sysComConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) 
		{
        	sysComService = SysComServiceInterface.Stub.asInterface(service);

        	ALog.w(TAG, "Called 'onServiceConnected'.");
		}


		@Override
		public void onServiceDisconnected(ComponentName name) 
		{
			sysComService = null;
			
        	ALog.w(TAG, "Called 'onServiceDisconnected'.");
		}	
	};

	
	private class ProcessSingleClick implements Runnable
	{
		private Context applicationContext;
		
		
		public ProcessSingleClick(Context applicationContext) 
		{
			this.applicationContext = applicationContext;
		}

		
		@Override
		public void run() 
		{
			processSingleClick (applicationContext);
		}
	}
	
	
	private class ScreenStatusChanged implements Runnable
	{
		private boolean		status;
		
		
		private ScreenStatusChanged(boolean status)
		{
			this.status = status;
		}


		@Override
		public void run() 
		{
			try
			{
				reportScreenStatusInternal (status);
				
				ALog.d(TAG, "Succeeded.");
			}
			catch(Exception e)
			{
				ALog.e(TAG, "EXC(1)");
			}
		}
	}
	
	
																	
@Override
	public IBinder onBind (Intent arg) 
	{
		return mainServiceBinder;
	}

	
	@Override
	public void onCreate()
	{
		ALog.v(TAG, "Entry...");

		super.onCreate();

		thisService = this;
		StateMachine.init (this);

		IntentFilter intentFilter1 = new IntentFilter(MainService.GPS_MAIN);
		registerReceiver (broadcastReceiver,
						  intentFilter1);

		ALog.w(TAG, "Registered widget processing receivers.");

		activityManagement = new ActivityManagement();
		
		IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter2.addAction (Intent.ACTION_SCREEN_OFF);

		registerReceiver (activityManagement, 
						  intentFilter2);
		
		ALog.w(TAG, "Registered screen receivers.");

		
		Intent intent = new Intent();
		intent.setClassName("com.ogp.syscomprocessor", "com.ogp.syscomprocessor.SysComService");
		
		try
		{
			bindService(intent, sysComConnection, Context.BIND_AUTO_CREATE);
			sysComServiceBound = true;
		}
		catch(SecurityException e)
		{
			sysComServiceBound = false;
		}
		
		ALog.w(TAG, "Registered SysComProcessor binding.");

		
		initWatchdogThread (true);
		
		setItForeground();
		
		ALog.v(TAG, "Exit.");
	}

	
	@Override
	public int onStartCommand (Intent 	intent, 
							   int 		flags, 
							   int 		startId)
	{
		int result = super.onStartCommand (intent, 
										   flags, 
										   startId);

		if (START_NOT_STICKY == result)
		{
			result = START_STICKY;
		}
		
		
		return result;
	}
	

	@Override
	public void onDestroy()
	{
    	ALog.v(TAG, "Entry...");

    	initWatchdogThread (false);
    	gpsSoftwareStatus = false;
    	
    	if (sysComServiceBound)
    	{
    		unbindService(sysComConnection);
    	}

        unregisterReceiver (activityManagement);
		unregisterReceiver (broadcastReceiver);

		ALog.w(TAG, "Unregistered screen receivers.");

    	thisService 	= null;
    	
    	super.onDestroy();

    	ALog.v(TAG, "Exit.");
	}


	@SuppressWarnings("deprecation")
	@Override
	public void onStart (Intent 	intent,
						 int 		startId)
	{
    	ALog.v(TAG, "Entry...");

    	super.onStart (intent, 
				       startId);

		thisService = this;

    	ALog.v(TAG, "Exit.");
	}
	
	
	public static void setServiceForeground()
	{
		try
		{
			thisService.setItForeground();
		}
		catch(Exception e)
		{
		}
	}
	
	
	@SuppressWarnings("deprecation")
	private void setItForeground()
	{
		if (StateMachine.getUseNotification())
		{
			Notification note = new Notification(getResIdByStatus(),
												 getResources().getString (R.string.notify),
												 System.currentTimeMillis());
	
			Intent intent = new Intent(this, 
				 					   MainActivity.class);
	
			intent.setFlags (Intent.FLAG_ACTIVITY_CLEAR_TOP 	| 
							 Intent.FLAG_ACTIVITY_SINGLE_TOP 	| 
							 Intent.FLAG_ACTIVITY_NEW_TASK);
	
			PendingIntent pi = PendingIntent.getActivity (this, 
				 									   	  0,
				 									   	  intent, 
				 									   	  0);
	
			note.setLatestEventInfo (this, 
									 getResources().getString (R.string.app_name),
									 getResources().getString (R.string.notify_active),
				 				  	 pi); 
			 
			startForeground (1, 
				  	 		 note);
				
			ALog.d(TAG, "setItForeground. Bringing the service foreground...");
		}
		else
		{
			try
			{
				stopForeground (true);
		    	ALog.d(TAG, "setItForeground. Bringing the service background...");
			}
			catch(Exception e)
			{
			}
		}
	}

	
	private int getResIdByStatus() 
	{
		if (StateMachine.getWatchGPSSoftware())
		{
			return getGpsStatus() ? R.drawable.gps_control_on : R.drawable.gps_control_off;
		}
		else
		{
			return getGpsStatus() ? R.drawable.gps_on : R.drawable.gps_off;
		}
	}
	
	
	public static void startServiceManually (Context 	context) 
	{
		ALog.v(TAG, "Entry...");

		try
		{
			thisService.toString();
		}
		catch(Exception e)
		{
			Intent serviceIntent = new Intent(context.getApplicationContext(), 
					  MainService.class);

			context.startService (serviceIntent);
		}
		
		ALog.v(TAG, "Exit.");
	}

	
	public static void swapGPSStatus()
	{
		thisService.swapGPSStatusInternal();
	}
	
	
	private void swapGPSStatusInternal() 
	{
		ALog.v(TAG, "Entry...");
		
		boolean currentGpsStatus = getGpsStatus();
		setGpsStatus(!currentGpsStatus);
	
		ALog.v(TAG, "Exit.");
	}

	
	public static boolean getGpsStatus()
	{
		try
		{
			return thisService.sysComService.getGpsStatus();
		}
		catch(Exception e)
		{
		}
		
		return false;
	}
	
	
	public static void setGpsStatus (boolean 	enable) 
	{
		ALog.v(TAG, "Entry...");
		
		try 
		{
			thisService.sysComService.setGpsStatus(enable);
		} 
		catch (Exception e) 
		{
		}
		
		ALog.v(TAG, "Exit.");
	}
	
	
	public static void reportScreenStatus (boolean status) 
	{
		ALog.v(TAG, "Entry...");

		try
		{
			thisService.messageHandler.removeCallbacksAndMessages (null);
			
			thisService.messageHandler.postDelayed (thisService.new ScreenStatusChanged (status), 
													status ? 0 : SCREEN_OFF_DELAY);

			ALog.d(TAG, "Post message succeeded.");
		}
		catch(Exception e)
		{
			ALog.e(TAG, "EXC(1)");
		}
		
		ALog.v(TAG, "Exit.");
	}

	
	public void reportGPSSoftwareStatus (boolean gpsSoftwareRunning) 
	{
		if (gpsSoftwareStatus != gpsSoftwareRunning)
		{
			gpsSoftwareStatus = gpsSoftwareRunning;
			activateGPSForSoftware();
		}
	}

	
	public static void updateBTAsGPS() 
	{
		ALog.v(TAG, "Entry...");

		if (null != thisService)
		{
			thisService.updateBTAsGPSInternal();
		}

		ALog.v(TAG, "Exit.");
	}

	
	public static void updateWidgets (Context applicationContext) 
	{
		ALog.v(TAG, "Entry...");

		if (null != thisService)
		{
			thisService.updateWidget();
		}

		ALog.v(TAG, "Exit.");
	}

	
	private void processClickOverWidget (Context applicationContext) 
	{
		ALog.v(TAG, "Entry...");

		if (0 == firstClickTime)
		{
			firstClickTime = System.currentTimeMillis();
			ALog.d(TAG, "First click registered at " + firstClickTime);
			
			messageHandler.postDelayed (new ProcessSingleClick(applicationContext),
										DOUBLE_CLICK_DELAY);
		}
		else 
		{
			firstClickTime = 0;
			ALog.d(TAG, "Second click registered at " + System.currentTimeMillis());

// Activate activity
			MainActivity.startMainActivity (applicationContext);
			
		}
		
		ALog.v(TAG, "Exit.");
	}
	
	
	private void processSingleClick (Context applicationContext)
	{
		ALog.v(TAG, "Entry...");
		
		if (0 == firstClickTime)
		{
			ALog.d(TAG, "Bypass. Do nothing.");
		}
		else
		{
			firstClickTime = 0;
			
			if (!StateMachine.getWatchGPSSoftware())
			{
				swapGPSStatusInternal();
			}

			ALog.d(TAG, "Single click activated.");
		}

		ALog.v(TAG, "Exit.");
	}

	
	private void reportScreenStatusInternal (boolean 	status) 
	{
		ALog.v(TAG, "Entry...");

		initWatchdogThread (status);
		
		ALog.v(TAG, "Exit.");
	}
	
		
	private void initWatchdogThread (boolean status) 
	{
		ALog.v(TAG, "Entry...");

		if (status)
		{
			if (null == watchdogThread)
			{
				watchdogThread = new WatchdogThread(this);

				ALog.d(TAG, "Watchdog thread started.");
			}
		}
		else 
		{
			if (null != watchdogThread)
			{
				watchdogThread.finish();
				watchdogThread = null;

				ALog.d(TAG, "Watchdog thread finished.");
			}
		}

		ALog.v(TAG, "Exit.");
	}


	private void internalRefreshGPSStatus()
	{
		ALog.v(TAG, "Entry...");
		
	
// 1. Refresh widget based on this server.
    	updateWidget();		
    	
    	
// 2. Inform the MainActivity and Widgets if any about the same event. 
    	Intent intent = new Intent(MainService.GPS_MAIN);
    	
    	intent.putExtra (GPS_PAYLOAD, 
    				     GPS_REFRESH);
    	

    	getGpsStatus();
    	
    	intent.putExtra (GPS_STATUS, getGpsStatus() ? 1 : 0);
    	
    	sendBroadcast (intent);
    	
// 4. Update BT status if required
    	updateBTAsGPSInternal();
    	
    	ALog.v(TAG, "Exit.");
	}

	
	private void updateWidget() 
	{
		ALog.v(TAG, "Entry...");

		GPSWidget.createWidgetView (this);

        ALog.v(TAG, "Exit.");
	}


	private void activateGPSForSoftware() 
	{
		ALog.v(TAG, "Entry...");
		
		if (StateMachine.getWatchGPSSoftware())
		{
			setGpsStatus (gpsSoftwareStatus);
				
			ALog.i(TAG, "Attempt to " + (gpsSoftwareStatus ? "activate GPS." : "deactivate GPS."));
		}
		
		ALog.v(TAG, "Exit.");
	}

	
	private void updateBTAsGPSInternal() 
	{
		ALog.v(TAG, "Entry...");
		
		if (StateMachine.getTurnBT())
		{
			BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

			boolean currentGpsStatus = false;
			try
			{
				currentGpsStatus = sysComService.getGpsStatus();
			}
			catch(Exception e)
			{
			}

			
			if (currentGpsStatus)
			{
				if (null != btAdapter 
					&& 
					!btAdapter.isEnabled())
				{
					btAdapter.enable();
				}

				ALog.i(TAG, "BT enabled.");
			}
			else
			{
				if (null != btAdapter 
					&& 
					btAdapter.isEnabled())
				{
					btAdapter.disable();
				}
				
				ALog.i(TAG, "BT disabled.");
			}
		}
		
		ALog.v(TAG, "Exit.");
	}
}
