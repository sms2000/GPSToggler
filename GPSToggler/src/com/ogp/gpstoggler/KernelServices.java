package com.ogp.gpstoggler;

import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import com.ogp.gpstoggler.log.ALog;


public class KernelServices 
{
	private static final String		TAG 					= "KernelServices";
	private static final String 	MOUNTING_POINTS 		= "/proc/mounts";
	
	private static List<Mount>		mountPoints				= new ArrayList<Mount>(); 
	
	
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
			LineNumberReader 	lnr = null;
			try
			{
				lnr = new LineNumberReader(new FileReader(MOUNTING_POINTS));
				String 	line;
				
				while ((line = lnr.readLine()) != null)
				{
					String[] fields = line.split (" ");
					mountPoints.add (new Mount(fields[0], 		// device
										  	   fields[1],		// mounting point
										  	   fields[2])); 	// type
				}
			}
			catch(IOException e)
			{
				ALog.e(TAG, "EXC(1)");
				e.printStackTrace();
			}
			finally
			{
				if (null != lnr)
				{
					try 
					{
						lnr.close();
					} 
					catch (IOException e) 
					{
					}
				}
			}
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
