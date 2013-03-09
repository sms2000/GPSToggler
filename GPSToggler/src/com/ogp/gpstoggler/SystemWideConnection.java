package com.ogp.gpstoggler;

import com.ogp.gpstoggler.log.ALog;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


public class SystemWideConnection implements ServiceConnection 
{
	private static final String 		TAG 				= "SystemWideConnection";
	
	private MainServiceInterface		mainServiceInterface;

	@Override
	public void onServiceConnected (ComponentName 	name, 
									IBinder 		service) 
	{
		ALog.v(TAG, "Entry...");

		mainServiceInterface = MainServiceInterface.Stub.asInterface (service);
		
		refreshGPSStatus();
		
		ALog.v(TAG, "Exit.");
	}


	@Override
	public void onServiceDisconnected (ComponentName name) 
	{
		ALog.v(TAG, "Entry...");

		mainServiceInterface = null;
		
		ALog.v(TAG, "Exit.");
	}
	
	
	public void refreshGPSStatus()
	{
		try 
		{
			mainServiceInterface.refreshGPSStatus();
			ALog.w(TAG, "Executed refreshGPSStatus.");
		} 
		catch (Exception e) 
		{
			ALog.e(TAG, "EXC(1).");
		}
	}
}
