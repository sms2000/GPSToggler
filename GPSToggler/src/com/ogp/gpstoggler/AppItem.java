package com.ogp.gpstoggler;


public class AppItem
{
	private String	packageName;
	private String	applicationName;
	private Boolean	expectsGPS;

	
	public AppItem(String	packageName,
				   String	applicationName,
				   Boolean	expectsGPS)
	{
		this.packageName		= packageName;
		this.applicationName 	= applicationName;
		this.expectsGPS			= expectsGPS;
	}


	public String getApplicationName() 
	{
		return applicationName;
	}


	public String getPackageName() 
	{
		return packageName;
	}


	public boolean getExpectsGPS() 
	{
		return expectsGPS;
	}
	
	
	public void setExpectsGPS (boolean expectsGPS)
	{
		this.expectsGPS = expectsGPS;
	}
}
