package com.ogp.gpstoggler;

import java.util.List;

import com.ogp.gpstoggler.log.ALog;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.content.BroadcastReceiver;


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
		
	
	private static MainService			thisService				= null;
	private static boolean 				currentGPSDecided		= false;
	private static boolean 				currentGPSStatus		= false;
	private Handler						messageHandler			= new Handler();
	private ActivityManagement 			activityManagement;
	private WatchdogThread 				watchdogThread;
	private boolean						gpsSoftwareStatus		= false;
	private long 						firstClickTime			= 0;

	
	
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

		activityManagement = new ActivityManagement();
		
		IntentFilter intentFilter2 = new IntentFilter(Intent.ACTION_SCREEN_ON);
		intentFilter2.addAction (Intent.ACTION_SCREEN_OFF);

		registerReceiver (activityManagement, 
						  intentFilter2);
		
		ALog.w(TAG, "Registered screen receivers.");
		
		initWatchdogThread (true);
		
		
		ALog.v(TAG, "Exit.");
	}


	@Override
	public void onDestroy()
	{
    	ALog.v(TAG, "Entry...");

    	initWatchdogThread (false);
    	gpsSoftwareStatus = false;
    	
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
	
	
	public static void startServiceManually (Context 	context) 
	{
		if (null == thisService)
		{
			Intent serviceIntent = new Intent(context.getApplicationContext(), 
					  						  MainService.class);
	
			context.startService (serviceIntent);
		}
	}

	
	public static void swapGPSStatus (Context context) 
	{
		ALog.v(TAG, "Entry...");
		
		Intent intent = new Intent(GPS_ACTION);
		
		intent.putExtra (GPS_COMMAND, 
						 getGPSStatus() ? GPS_OFF : GPS_ON);
		
		context.sendBroadcast (intent);

		setGPSStatus (!getGPSStatus());
		
	
		ALog.v(TAG, "Exit.");
	}

	
	public static void setGPSStatus (Context 	context, 
			 						 boolean 	enable) 
	{
		ALog.v(TAG, "Entry...");
		
		Intent intent = new Intent(GPS_ACTION);
		
		intent.putExtra (GPS_COMMAND, 
						 enable ? GPS_ON : GPS_OFF);
		
		context.sendBroadcast (intent);

		setGPSStatus (enable);
		
		ALog.v(TAG, "Exit.");
	}
	

	public static boolean getGPSStatus() 
	{
		return currentGPSStatus;
	}

	
	public static void setGPSStatus (boolean status) 
	{
		currentGPSStatus = status;
	}
	

	public static boolean isGPSDecided() 
	{
		return currentGPSDecided;
	}

	
	public static void reportScreenStatus (boolean 	status) 
	{
		ALog.v(TAG, "Entry...");

		try
		{
			thisService.messageHandler.post (thisService.new ScreenStatusChanged (status));

			ALog.d(TAG, "Post message succeeded.");
		}
		catch(Exception e)
		{
			ALog.e(TAG, "EXCE(1)");
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
				swapGPSStatus (applicationContext);
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
		
// 0. Ask for the status		
		askForGPSStatus();
		
		
// 1. Refresh widget based on this server.
    	updateWidget();		
    	
    	
// 2. Inform the MainActivity and Widgets if any about the same event. 
//    Use broadcast.     	
    	Intent intent = new Intent(MainService.GPS_MAIN);
    	
    	intent.putExtra (GPS_PAYLOAD, 
    				     GPS_REFRESH);
    	
    	intent.putExtra (GPS_STATUS, 
    					 getGPSStatus() ? 1 : 0);
    	
    	sendBroadcast (intent);
    	
// 4. Update BT status if required
    	updateBTAsGPSInternal();
    	
    	ALog.v(TAG, "Exit.");
	}

	
	private void askForGPSStatus() 
	{
		boolean newStatus = false;
			
		LocationManager lm = (LocationManager)getSystemService (Context.LOCATION_SERVICE);
		if (null != lm)
		{
			List<String> list = lm.getProviders (true);
				
			for (int i = 0; i < list.size(); i++)
			{
				ALog.v(TAG, "Provider: " + list.get (i));
			
				if (list.get (i).contains (GPS))
				{
					newStatus = true;
				}
			}

			ALog.v(TAG, " ");
		}	
			
			
		currentGPSDecided = true;

		ALog.d(TAG, "Real GPS status now " + (newStatus ? "on" : "off"));
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
			setGPSStatus (this, 
						  gpsSoftwareStatus);
				
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

			if (getGPSStatus())
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
