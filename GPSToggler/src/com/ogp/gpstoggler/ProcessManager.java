package com.ogp.gpstoggler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;


public class ProcessManager 
{
	static private final String	APP_INDEX		   = "App%03d";
	static private final String	APP_NUMBER		   = "AppNumber";
	
	static private List<AppItem> installedPackages = null; 
	
	
//	
// All installed packages with FINE_LOCATION permission
//
	static public List<AppItem> listGPSAwarePackages (Context 	context, 
													  boolean 	forceReload)
	{
		if (null == installedPackages
			||
			forceReload)
		{
			if (null == installedPackages)
			{
				installedPackages = new ArrayList<AppItem>();
			}
			else
			{
				installedPackages.clear();
			}
			
			
			reloadAllMarkedPackages (context, 	
									   installedPackages);
		}
		
		return installedPackages;
	}
	

//
// Those packages with FINE_LOCATION permission marked by user
//
	static public void loadMarkedPackages (SharedPreferences 	preferences, 
										   List<AppItem> 		listOfGPSAwareApplications)
	{
		for (AppItem item : listOfGPSAwareApplications)
		{
			item.setExpectsGPS (false);
		}

		
		int number = preferences.getInt (APP_NUMBER, 
										 0);
		
		for (int i = 0; i < number; i++)
		{
			String strIndex = String.format (APP_INDEX, 
											 i);
			
			String packageName = preferences.getString (strIndex,
														"");
			
			if (packageName.isEmpty())	
			{
				continue;
			}
			
			boolean found = false;
			
			for (int j = 0; j < installedPackages.size(); j++)
			{
				if (installedPackages.get (j).getPackageName().equals (packageName))
				{
					found = true;
					break;
				}
			}
			
			
			if (found)
			{
				for (AppItem item : listOfGPSAwareApplications)
				{
					if (item.getPackageName().equals (packageName))
					{
						item.setExpectsGPS (true);
						break;
					}
				}
			}
		}
	}
	

//
// Preserve the list of marked applications
//
	static public void preserveMarkedApplications (List<AppItem> 		applications, 
												   SharedPreferences 	preferences)
	{
		int oldNumber = preferences.getInt (APP_NUMBER, 
											0);
		
		int newNumber = (null == applications) ? 0 : applications.size();
		int savedNumber = 0;
		
		Editor editor = preferences.edit();
		
		for (int i = 0; i < newNumber; i++)
		{ 
			if (applications.get (i).getExpectsGPS())
			{
				String strIndex = String.format (APP_INDEX, 
												 savedNumber++);
				editor.putString (strIndex, 
								  applications.get (i).getPackageName());
			}
		}
		
		editor.putInt (APP_NUMBER, savedNumber);
		
		for (int i = savedNumber; i < oldNumber; i++)
		{
			String strIndex = String.format (APP_INDEX, 
											 i);
			editor.remove (strIndex);
			
		}
		
		editor.commit();
	}

	
//
// Privates
//
	private static void reloadAllMarkedPackages (Context 		 context, 
												 List<AppItem> installedPackages) 
	{
		final PackageManager 	pm 		 = context.getPackageManager();
		List<ApplicationInfo> 	packages = pm.getInstalledApplications (PackageManager.GET_META_DATA);
		boolean 				empty    = true;
		
		for (ApplicationInfo applicationInfo : packages) 
		{
			try 
			{
				PackageInfo packageInfo = pm.getPackageInfo (applicationInfo.packageName, 
															 PackageManager.GET_PERMISSIONS);

				if (null != packageInfo.requestedPermissions
					&&
					!applicationInfo.packageName.contains ("gpstoggler"))		// Self not included! 
				{
					for (int i = 0; i < packageInfo.requestedPermissions.length; i++) 
					{
						if (packageInfo.requestedPermissions[i].equals (Manifest.permission.ACCESS_FINE_LOCATION))
						{
							String label = (String)applicationInfo.loadLabel (pm);
							
							installedPackages.add (new AppItem (applicationInfo.packageName, 
																label, 
																false));
							empty = false;
							break;
						}
						
					}
				}
			} 
			catch (NameNotFoundException e) 
			{
			} 
		}	
		
		
		if (!empty)
		{
			Collections.sort (installedPackages, new Comparator<Object>()
			{
		        public int compare (Object o1, 
		        					Object o2) 
		        {
		        	AppItem p1 = (AppItem)o1;
		        	AppItem p2 = (AppItem)o2;

		        	return p1.getApplicationName().compareToIgnoreCase (p2.getApplicationName());
		        }
		    });
		}
	}
}
