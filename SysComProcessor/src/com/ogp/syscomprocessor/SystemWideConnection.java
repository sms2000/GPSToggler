package com.ogp.syscomprocessor;

import com.ogp.syscomprocessor.log.ALog;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


public class SystemWideConnection implements ServiceConnection 
{
	private static final String 		TAG 				= "SystemWideConnection";
	
	private SysComServiceInterface		mainServiceInterface;

	@Override
	public void onServiceConnected (ComponentName 	name, 
									IBinder 		service) 
	{
		ALog.v(TAG, "Entry...");

		mainServiceInterface = SysComServiceInterface.Stub.asInterface (service);
		
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
			mainServiceInterface.bindCallback();
			
			ALog.w(TAG, "Executed bindCallback.");
		} 
		catch (Exception e) 
		{
			ALog.e(TAG, "EXC(1).");
		}
	}
}
