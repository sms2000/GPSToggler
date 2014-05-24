package com.ogp.gpstoggler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;

import com.ogp.gpstoggler.KernelServices.Mount;
import com.ogp.gpstoggler.log.ALog;
import com.ogp.gpstoggler.xml.VersionXMLParser;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnEndOfTask 
{
	private static final String 		TAG 					= "MainActivity";

	private static final String 		SYSTEM_FS		 		= "/system";
	private static final String 		SYSTEM_DIRECTORY_LEGACY	= "/system/app/";
	private static final String 		SYSTEM_DIRECTORY_44 	= "/system/priv-app/";
	private static final String 		XBIN_DIRECTORY			= "/system/xbin/";
	private static final String 		MODULE_STUB				= "libscp.so";
	private static final String 		MODULE_NAME 			= "SysComProcessor.apk";
	private static final String 		NATIVE_RUNNER			= "liboperator.so";

	private static final long 			REBOOT_WAIT 			= 3000;		// 3 seconds
	
	private static boolean				legacyAndroid			= true;
	private Button						button;
	private CheckBox					watchWaze;
	private CheckBox					turnBT;
	private CheckBox					useNotification;
	private CheckBox					splitAware;
	private ServiceConnection 			serviceConnection;
	private Handler						handler					= new Handler();
	private BroadcastReceiver 			broadcastReceiver;
	private boolean						currentGPSStatus		= false;

	
	private class Reboot implements Runnable
	{
		@Override
		public void run() 
		{
			rebootAndroid();
		}
	}
	
	private class MoverThread extends Thread
	{
		@Override
		public void run()
		{
			moveToSystem();
		}
	};

	
	private class UninstallThread extends Thread
	{
		@Override
		public void run()
		{
			cleanSystem();
		}
	};

	
	private class FatalError implements Runnable
	{
		@Override
		public void run() 
		{
			fatalError();
		}
	}
	
	
	private class EndOfTask implements Runnable
	{
		private TaskIndex 	index;
		private boolean 	result;


		private EndOfTask(TaskIndex		index,
						  boolean		result)
		{
			this.index	= index;
			this.result	= result;		
		}
		
		
		@Override
		public void run() 
		{
			endOfTask (index, 
					   result);
		}
	}
	
	
	private class RefreshGRPStatus implements Runnable
	{
		private	boolean	status;
		
		private RefreshGRPStatus(boolean	status)
		{
			this.status = status;
		}
		
		
		@Override
		public void run() 
		{
			requestCurrentGPSState (status);
		}
	}
	
	
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		ALog.v(TAG, "onCreate. Entry...");

		super.onCreate (savedInstanceState);
		
		legacyAndroid = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT;
		
		ViewGroup viewGroup = (ViewGroup)getLayoutInflater().inflate (R.layout.activity_main, 
																	  null);
		setContentView (viewGroup);

		button 			= (Button)viewGroup.findViewById   (R.id.button);
		watchWaze		= (CheckBox)viewGroup.findViewById (R.id.waze);
		turnBT			= (CheckBox)viewGroup.findViewById (R.id.bt);
		useNotification	= (CheckBox)viewGroup.findViewById (R.id.notification);
		splitAware		= (CheckBox)viewGroup.findViewById (R.id.splitAware);
				
		StateMachine.init (this);
		VersionXMLParser.printVersion (this);

		TextView verText = (TextView)viewGroup.findViewById (R.id.version);
		verText.setText (String.format (getResources().getString (R.string.verison_format), 
										StateMachine.getVersion()));

		watchWaze.		setChecked (StateMachine.getWatchGPSSoftware());
		turnBT.	  		setChecked (StateMachine.getTurnBT());
		useNotification.setChecked (StateMachine.getUseNotification());
		splitAware.		setChecked (StateMachine.getSplitAware());
		
		ALog.v(TAG, "onCreate. Exit.");
	}


	@Override
	protected void onResume() 
	{
		super.onResume();
		
		inviteSystemize();
		initAferSystem();

		IntentFilter intentFilter = new IntentFilter (MainService.GPS_MAIN);
		
        broadcastReceiver = new BroadcastReceiver() 
        {
            @Override
            public void onReceive (Context 	context, 
            					   Intent 	intent) 
            {
            	ALog.v(TAG, "onResume. Entry...");
            	
                String 		payload = intent.getStringExtra (MainService.GPS_PAYLOAD);
                if (payload.equals (MainService.GPS_REFRESH))
                {
                	boolean status  = intent.getIntExtra (MainService.GPS_STATUS, 
                										  0) == 1;

                	ALog.w(TAG, "onResume. Called 'RefreshGRPStatus'.");
                	handler.post (new RefreshGRPStatus(status));
                }

                
                ALog.v(TAG, "onResume. Exit.");
            }
        };

        registerReceiver (broadcastReceiver, 
        				  intentFilter);
        
        if (StateMachine.getRebootRequired())
        {
// Maybe this time user wants to reboot?
			showRebootDialog();
        }
	}
	
	
	@Override
	protected void onPause() 
	{
		super.onPause();
	
		unregisterReceiver (broadcastReceiver);
	}

	
	@Override
	protected void onDestroy() 
	{
		unbindFromService();
		
		super.onDestroy();
	}

	
	private void initAferSystem()
	{
		requestCurrentGPSState (false);
		
		MainService.startServiceManually (this);
		
		bindToService();
	}


	private void bindToService() 
	{
		if (null == serviceConnection)
		{
			Intent serviceIntent = new Intent(this, 
					  						  MainService.class);
	
			serviceConnection = new SystemWideConnection();
	
			if (bindService (serviceIntent,
		    				 serviceConnection, 
							 Context.BIND_AUTO_CREATE))
			{
				ALog.d(TAG, "bindToService. bindService succeeded.");
			}
			else
			{
				ALog.e(TAG, "bindToService. bindService failed.");
			}	
		}
		else
		{
			ALog.e(TAG, "bindToService. Repeated call.");
		}
	}


	private void unbindFromService() 
	{
		if (null != serviceConnection)
		{
			unbindService (serviceConnection);
			serviceConnection = null;

			ALog.d(TAG, "unbindFromService. unbindService finished.");
		}
		else
		{
			ALog.e(TAG, "unbindFromService. unbindService not called.");
		}
	}

	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		getMenuInflater().inflate (R.menu.activity_main, menu);
		return true;
	}
	
	
	public void requestCurrentGPSState (boolean	status)
	{
		currentGPSStatus = status;
	
		ALog.d(TAG, "requestCurrentGPSState. GPS now " + (currentGPSStatus ? "on" : "off"));

			
		String strStatus = getResources().getString (R.string.gps_status);
		strStatus += " " + getResources().getString (currentGPSStatus ? R.string.gps_on : R.string.gps_off);
		
		button.setText (strStatus);
	}
	
	
	public void clickButton (View view)
	{
		ALog.v(TAG, "clickButton. Entry...");
		
		if (currentGPSStatus)
		{
			ALog.w(TAG, "clickButton. Pressed when ON");
		}
		else
		{
			ALog.w(TAG, "clickButton. Pressed when OFF");
		}

		MainService.swapGPSStatus (getApplicationContext());

		ALog.v(TAG, "clickButton. Exit.");
	}
	
	
	public void clickSelectPackets (View view)
	{
		ALog.v(TAG, "clickSelectPackets. Entry...");
		
		Intent intent = new Intent(this, SelectActivity.class);
		startActivity (intent);

		ALog.v(TAG, "clickSelectPackets. Exit.");
	}
	
	
	public void clickCopyBinaries (View view)
	{
		ALog.v(TAG, "clickCopyBinaries. Entry...");
		
	    new MoverThread().start();
	    
		ALog.v(TAG, "clickCopyBinaries. Exit.");
	}

	
	public void clickNotification (View view)
	{
		ALog.v(TAG, "clickNotification. Entry...");

		StateMachine.setUseNotification (((CheckBox)view).isChecked());
		StateMachine.writeToPersistantStorage();

		MainService.setServiceForeground();
		
		ALog.v(TAG, "clickNotification. Exit.");
	}
	
	
	public void clickSplitAware (View view)
	{
		ALog.v(TAG, "clickSplitAware. Entry...");

		StateMachine.setSplitAware (((CheckBox)view).isChecked());
		StateMachine.writeToPersistantStorage();

		ALog.v(TAG, "clickSplitAware. Exit.");
	}

	
	public void clickWatchWaze (View view)
	{
		ALog.v(TAG, "clickWatchWaze. Entry...");

		StateMachine.setWatchGPSSoftware (((CheckBox)view).isChecked());
		StateMachine.writeToPersistantStorage();
		
		MainService.updateWidgets (getApplicationContext());
		
		ALog.v(TAG, "clickWatchWaze. Exit.");
	}

	
	public void clickTurnBT (View view)
	{
		ALog.v(TAG, "clickTurnBT. Entry...");

		StateMachine.setTurnBT (((CheckBox)view).isChecked());
		StateMachine.writeToPersistantStorage();

		MainService.updateBTAsGPS();
		
		ALog.v(TAG, "clickTurnBT. Exit.");
	}
	
		
	public void clickUninstall (View view)
	{
		ALog.v(TAG, "clickUninstall. Entry...");

		new UninstallThread().start();

		ALog.v(TAG, "clickUninstall. Exit.");
	}


	public void clickDebugging (View view)
	{
		ALog.v(TAG, "clickDebugging. Entry...");

		StateMachine.setUseDebugging (((CheckBox)view).isChecked());
		StateMachine.writeToPersistantStorage();

		ALog.v(TAG, "clickDebugging. Exit.");
	}
	
	
	private void inviteSystemize() 
	{
		if (!verifyCompliance())
		{
			showModuleInstallDialog();
		}
	}
	
	
	private static String getSystemAppDirectory()
	{
		return legacyAndroid ? SYSTEM_DIRECTORY_LEGACY : SYSTEM_DIRECTORY_44;
	}
	

	private boolean verifyCompliance() 
	{
		InputStream targetStream;
		byte[] 		targetDigest    = null;
		
		try 
		{
			targetStream = new FileInputStream(getSystemAppDirectory() + MODULE_NAME);
		} 
		catch (Exception e) 
		{
			ALog.w(TAG, "FALSE (1)");
			return false;
		} 

		targetDigest = computeDigest (targetStream); 
		if (null == targetDigest)
		{
			ALog.w(TAG, "FALSE (2)");
			return false;
		}  

		
		InputStream sourceInput;
		byte[] 		sourceDigest = null; 
		try 
		{
			String sourceString = getApplicationInfo().nativeLibraryDir + "/" + MODULE_STUB;
			
			sourceInput = new FileInputStream(sourceString);
			
			sourceDigest = computeDigest (sourceInput); 
		} 
		catch (Exception e) 
		{
			ALog.e(TAG, "EXC(1)");
		}
		
		if (null == sourceDigest)
		{
			ALog.w(TAG, "FATAL (1)");
			handler.post (new FatalError());
			return true;
		}


		ALog.v(TAG, String.format ("Original hash: %s", byteArray2String (sourceDigest)));
		ALog.v(TAG, String.format ("Copied   hash: %s", byteArray2String (targetDigest)));
		
		boolean comparable = targetDigest.length == sourceDigest.length;
		
		if (comparable) 
		{
			for (int i = 0; i < targetDigest.length; i++)
			{
				try
				{
					if (targetDigest[i] != sourceDigest[i])
					{
						comparable = false;
						break;
					}
				}
				catch(Exception e)
				{
					ALog.e(TAG, "EXC(2)");
					comparable = false;
					break;
				}
			}
		}
		
		if (!comparable)
		{
			ALog.w(TAG, "FALSE (3)");
			return false;
		}
		
		ALog.w(TAG, "TRUE");
		return true;
	}


	private byte[] computeDigest (InputStream stream)
	{
		try 
		{
			byte[] 			buffer 	 = new byte[1024];
			MessageDigest 	complete = MessageDigest.getInstance ("MD5");
			int 			numRead;

			do 
			{
				numRead = stream.read (buffer);
				if (numRead > 0) 
				{
					complete.update (buffer, 
									 0, 
									 numRead);
				}
			} while (numRead != -1);

			stream.close();
			return complete.digest();
		} 
		catch (Exception e) 
		{
			return null;
		}	
	}
	
	
	private void moveToSystem() 
	{
		ALog.v(TAG, "Entry...");
		
		boolean success = false;
		Mount 	sysFS 	= new KernelServices().findFS (SYSTEM_FS);
		
		if (null != sysFS)
		{
			try
			{
		    	String 				libDir  = getApplicationInfo().nativeLibraryDir + "/";
		    	
		    	String 				command;
		    			
		    	command  = libDir + NATIVE_RUNNER;
 		    	command += " " + "copy2system";
		    	command += " " + libDir + NATIVE_RUNNER;
		    	command += " " + XBIN_DIRECTORY + NATIVE_RUNNER;
		    	command += " " + SYSTEM_FS; 
		    	command += " " + sysFS.mDevice; 
		    	command += " " + sysFS.mType; 
		    	command += " " + "gid-uid";
		    	command += "\n"; 

			    ALog.w(TAG, "Executing command [1]:\n" + command);
			    ALog.w(TAG, "\n");

			    flushCommand (command);			    
			    
			    command  = XBIN_DIRECTORY + NATIVE_RUNNER;
 		    	command += " " + "copy2system";
		    	command += " " + libDir + MODULE_STUB;
		    	command += " " + getSystemAppDirectory() + MODULE_NAME;
		    	command += " " + SYSTEM_FS; 
		    	command += " " + sysFS.mDevice; 
		    	command += " " + sysFS.mType; 
		    	command += "\n"; 

	
		    	ALog.w(TAG, "Executing command [2]:\n" + command);
			    ALog.w(TAG, "\n");

			    flushCommand (command);
			    
			    ALog.w(TAG, "Calling native code succeeded.");
			    
			    success = true;
			}
			catch(Exception e)
			{
				ALog.e(TAG, "EXC(1)");
			}
		}
		
		if (!success)
		{
			ALog.e(TAG, "Native code portion failed.");
		}

		onEndOfTask (TaskIndex.MOVE_MODULE, 
					 success);
		
		ALog.v(TAG, "Exit.");
	}

	
	private void cleanSystem()
	{
		ALog.v(TAG, "Entry...");
		
		boolean success = false;
		Mount 	sysFS 	= new KernelServices().findFS (SYSTEM_FS);
		
		if (null != sysFS)
		{
			try
			{
				String 				command;
				
			    command  = XBIN_DIRECTORY + NATIVE_RUNNER;
 		    	command += " " + "remove4system";
		    	command += " " + getSystemAppDirectory() + MODULE_NAME;
		    	command += " " + SYSTEM_FS; 
		    	command += " " + sysFS.mDevice; 
		    	command += " " + sysFS.mType; 
		    	command += "\n"; 

		    	ALog.w(TAG, "Executing command [1]:\n" + command);
			    ALog.w(TAG, "\n");

			    flushCommand (command);			    
				
			    ALog.w(TAG, "Calling native code succeeded.");
			    
			    success = true;
			}
			catch(Exception e)
			{
			}
		}
		
		if (!success
			||
			isSystemModuleExists())
		{
			ALog.e(TAG, "Native code portion failed.");
			success = false;
		}

		
		onEndOfTask (TaskIndex.CLEAN_MODULE, 
				 	 success);

		ALog.v(TAG, "Exit.");
	}

	
	private void rebootAndroidWait()
	{
		Toast.makeText(this, 
					   R.string.wait_reboot, 
					   Toast.LENGTH_SHORT).show();
		
		handler.postDelayed (new Reboot(), 
							 REBOOT_WAIT);
	}
	
	private void rebootAndroid()
	{
		ALog.v(TAG, "Entry...");
		
		try 
		{
			String 				command;
			
		    command  = XBIN_DIRECTORY + NATIVE_RUNNER;
	    	command += " " + "reboot";
	    	command += "\n"; 

	    	ALog.w(TAG, "Executing command [1]:\n" + command);
		    ALog.w(TAG, "\n");

		    flushCommand (command);			    
			
		    ALog.w(TAG, "Calling native code succeeded.");
			ALog.w(TAG, "Rebooting...");
		}
		catch(Exception e)
		{
			ALog.e(TAG, "EXC(1).");
			e.printStackTrace();
		}

		finish();
		
		ALog.v(TAG, "Exit.");
	}
	
	
	@Override
	public void onEndOfTask (TaskIndex 	task, 
							 boolean 	result) 
	{
		handler.post (new EndOfTask(task, 
									result));
	}
	

	private void endOfTask (TaskIndex 	index, 
							boolean 	result) 
	{
		switch (index)
		{
		case MOVE_MODULE:
			if (result 
				&& 
				verifyCompliance())
			{
				StateMachine.setRebootRequired (true);
				StateMachine.writeToPersistantStorage();
 				showRebootDialog();
			}
			else
			{
				showErrorDialog();
			}
			
			break;
			
			
		case CLEAN_MODULE:
			showUninstallDialog (result);
				
			break;
		}
	}

	
	private void showUninstallDialog (boolean result)
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle (result ? R.string.warning : R.string.error);
		dialog.setMessage (result ? R.string.uninstall_module : R.string.uninstall_failed);
		dialog.setPositiveButton (R.string.ok, 
								  new DialogInterface.OnClickListener() 
		{
			 public void onClick (DialogInterface 	dialog, 
					 			  int 				id) 
			 {
				 dialog.cancel();

				 finish();
			 }
		});
		

		dialog.show();
	}

	
	private void showModuleInstallDialog() 
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle (R.string.warning);
		dialog.setMessage (R.string.install_module);
		dialog.setPositiveButton (R.string.ok, 
								  new DialogInterface.OnClickListener() 
		{
			 public void onClick (DialogInterface 	dialog, 
					 			  int 				id) 
			 {
				 dialog.cancel();
				 
				 new MoverThread().start();
			 }
		});
		
		dialog.setNegativeButton (R.string.cancel, 
								  new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface 	dialog, 
								 int 				id) 
			{
				dialog.cancel();
				//finish();
			}
		});

		dialog.show();
	}

	
	private void showErrorDialog() 
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle (R.string.error);
		dialog.setMessage (R.string.move_error);
		dialog.setPositiveButton (R.string.ok, 
								  new DialogInterface.OnClickListener() 
		{
			 public void onClick (DialogInterface 	dialog, 
					 			  int 				id) 
			 {
				 dialog.cancel();

				 finish();
			 }
		});

		dialog.show();
	}


	private void showRebootDialog() 
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle (R.string.warning);
		dialog.setMessage (R.string.move_success);
		dialog.setPositiveButton (R.string.yes, 
								  new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface 	dialog, 
								 int 				id) 
			{
				dialog.cancel();
				
				rebootAndroidWait();
			}
		});

		dialog.setNegativeButton (R.string.no, 
								  new DialogInterface.OnClickListener() 
		{
			public void onClick (DialogInterface 	dialog, 
								 int 				id) 
			{
				dialog.cancel();
				//finish();
			}
		});

		dialog.show();
	}

	
	private void fatalError() 
	{
		ALog.e(TAG, "---------- FATAL ERROR ----------");
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setTitle (R.string.error);
		dialog.setMessage (R.string.fatal_error);
		dialog.setPositiveButton (R.string.ok, 
								  new DialogInterface.OnClickListener() 
		{
			 public void onClick (DialogInterface 	dialog, 
					 			  int 				id) 
			 {
				 dialog.cancel();

				 finish();
			 }
		});

		dialog.show();
	}


	public static void verifySystemModuleExists (Context context) 
	{
		ALog.v(TAG, "Entry...");
		
		
		String 	modulePath 	= getSystemAppDirectory() + MODULE_NAME;
		File 	file 		= new File(modulePath);

		if (file.exists())
		{
			ALog.d(TAG, "System module exists.");
		}
		else
		{
			ALog.w(TAG, "System module doesn't exist. Starting activity.");

			startMainActivity (context);
		}
		
		ALog.v(TAG, "Exit.");
	}
	
	
	public static void startMainActivity (Context context) 
	{
		ALog.v(TAG, "Entry...");

		Intent intent = new Intent(context.getApplicationContext(), 
				   				   MainActivity.class);

		intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);

		context.getApplicationContext().startActivity (intent);

		ALog.v(TAG, "Exit.");
	}

	
	private boolean isSystemModuleExists()
	{
		try 
		{
			new FileInputStream(new File(getSystemAppDirectory() + MODULE_NAME));
			return true;
		} 
		catch (FileNotFoundException e) 
		{
		} 
		
		return false;
	}
	
	
	public void clickTest (View view)
	{
		ALog.v(TAG, "Entry...");
		

		ALog.v(TAG, "Exit.");
	}


	private Object byteArray2String (byte[] array) 
	{
		String str = new String();
		
		for (int i = 0; i < array.length; i++)
		{
			int bt = array[i] & 0x0F;
			
			if (bt > 9)
			{
				str += 'A' + bt - 10;
			}
			else
			{
				str += '0' + bt;
			}
			
			bt = array[i] >> 4;

			if (bt > 9)
			{
				str += 'A' + bt - 10;
			}
			else
			{
				str += '0' + bt;
			}
			
		}
		
		return str;
	}
	

	private void flushCommand (String command)
	{
		ALog.v(TAG,  "flushCommand. Entry...");

		try
		{
	    	Process chperm = Runtime.getRuntime().exec ("su");	// The only one place we actually need SU
			DataOutputStream os = new DataOutputStream(chperm.getOutputStream());
			
			os.writeBytes (command);
		    os.flush();
		    
		    os.writeBytes ("exit\n");
		    os.flush();

		    chperm.waitFor();  				

		    ALog.v(TAG,  "flushCommand. Exit.");
		}
		catch(Exception e)
		{
			ALog.e(TAG,  "flushCommand. EXC(1)");
		}
	}

}
