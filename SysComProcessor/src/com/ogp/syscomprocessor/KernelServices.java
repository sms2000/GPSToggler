package com.ogp.syscomprocessor;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import com.ogp.syscomprocessor.log.ALog;


public class KernelServices 
{
	private static final String		TAG = "KernelServices";

	private static List<Mount>		mountPoints	= new ArrayList<Mount>(); 
	private static boolean 			rootGranted	= false;
	private static boolean 			rootDenied	= false;
	
	
	public class Mount 
	{
	    final String		mDevice;
	    final String		mMountPoint;
	    final String		mType;
	    
	    public Mount(String		device, 
	    			 String		path,
	    			 String 	type) 
	    {
	        mDevice 	= device;
	        mMountPoint = path;
	        mType		= type;
	    }
	}


	public KernelServices()
	{
		ALog.v(TAG, "Entry...");

		if (mountPoints.isEmpty())
		{
			try
			{
				LineNumberReader 	lnr = new LineNumberReader(new FileReader("/proc/mounts"));
				String 				line;
				
				while ((line = lnr.readLine()) != null)
				{
					String[] fields = line.split (" ");
					mountPoints.add (new Mount(fields[0], 		// device
										  	   fields[1],		// mounting point
										  	   fields[2])); 	// type
					
					if (fields[1].equals ("/system"))
					{
						ALog.v(TAG, "/system mounting point found.");
					}
				}
			}
			catch(IOException e)
			{
				ALog.e(TAG, "EXC(1)");
				e.printStackTrace();
			}
		}

		ALog.v(TAG, "Exit.");
	}
	
	
	public boolean obtainRoot() 
	{
		if (rootGranted)
		{
			return true;
		}
		else if (rootDenied)
		{
			return false;
		}
		
	    try 
	    {
	    	Runtime.getRuntime().exec ("su");
	    	rootGranted = true;

	    	ALog.w(TAG, "Root granted.");
	    	return true;
	    }
	    catch(Exception e)
	    {
	    	
	    }
	    	
	    rootDenied = true;
	    
	    ALog.w(TAG, "Root denied.");
		return false;
	}


	public void rebootAndroid()
	{
		ALog.v(TAG, "Entry...");

		try 
		{
			Process process = Runtime.getRuntime().exec ("su");
			DataOutputStream out = new DataOutputStream (process.getOutputStream());
			out.writeBytes ("reboot\n");
			out.flush();

			out.writeBytes ("exit\n");
			out.flush();

			ALog.w(TAG, "Rebooting...");
		}
		catch(Exception e)
		{
			ALog.e(TAG, "EXC(1).");
			e.printStackTrace();
		}

		ALog.v(TAG, "Exit.");
	}

	
	public Mount findFS (String mountPoint) 
	{
		for (Mount iterator : mountPoints)
		{
			String path = iterator.mMountPoint;  
			
			if (path.equals (mountPoint))
			{
				return iterator;
			}
		}
		
		return null;
	}
}
