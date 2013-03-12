package com.example.com.ogp.gpsactivitytest;

import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.view.Menu;
import android.view.View;


public class MainActivity extends Activity 
{
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		super.onCreate (savedInstanceState);
		
		setContentView (R.layout.activity_main);
	}

	
	@Override
	public boolean onCreateOptionsMenu (Menu menu) 
	{
		getMenuInflater().inflate (R.menu.main, 
								   menu);
		return true;
	}

	
	public void clickButtonOn (View view)
	{
		Intent intent = new Intent();
		
		intent.setComponent (new ComponentName("com.ogp.gpstoggler", "com.ogp.gpstoggler.OnActivity"));
		startActivity(intent);
	}

	
	public void clickButtonOff (View view)
	{
		Intent intent = new Intent();
		
		intent.setComponent (new ComponentName("com.ogp.gpstoggler", "com.ogp.gpstoggler.OffActivity"));
		startActivity(intent);
	}
}
