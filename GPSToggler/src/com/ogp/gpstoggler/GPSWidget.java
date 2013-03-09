package com.ogp.gpstoggler;


import com.ogp.gpstoggler.log.ALog;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.widget.RemoteViews;


public class GPSWidget extends AppWidgetProvider 
{
    private static final String TAG 			= "GPSWidget";

    private boolean			receiverInstalled	= false;
    
    
    @Override
    public void onReceive (Context 		context, 
    					   Intent 		intent) 
    {
    	String action = intent.getAction();
    	if (null == action)
    	{
    		action = "<unknown>";
    	}
    	
    	
    	ALog.v(TAG, "Entry for action: " + action);
    	
    	if (action.equals (MainService.GPS_MAIN))
    	{
    		String payload = intent.getStringExtra (MainService.GPS_PAYLOAD);
    		if (null != payload
    			&&
    			payload.equals(MainService.GPS_SWAPSTATUS))
    		{
    			MainActivity.verifySystemModuleExists (context);
    		}
    	}
    	else if (action.equals (AppWidgetManager.ACTION_APPWIDGET_ENABLED)
    			 ||
    			 action.equals (AppWidgetManager.ACTION_APPWIDGET_OPTIONS_CHANGED)
    			 ||
    			 action.equals (AppWidgetManager.ACTION_APPWIDGET_UPDATE))
    	{
    		MainService.startServiceManually (context);

    		installReceiver 	   (context);
    		askServiceForBroadcast (context);
        	ALog.i(TAG, "Installed receiver. Initiating broadcast ordered.");
    	}
    	else if (action.equals (AppWidgetManager.ACTION_APPWIDGET_DISABLED))
    	{
    		removeReceiver (context);
        	ALog.i(TAG, "Removed receiver.");
    	}
    	else if (action.equals (MainService.GPS_MAIN))
    	{
        	ALog.i(TAG, "Action: GPS_MAIN"); 
        	
        	String payload = intent.getStringExtra (MainService.GPS_PAYLOAD);
	        if (null != payload
	        	&&
	        	payload.equals (MainService.GPS_REFRESH))
	        {
	        	ALog.w(TAG, "Called 'RefreshGRPStatus'.");
	        	
	        	createWidgetView (context);
	        }
    	}
        
        ALog.v(TAG, "Exit.");
    }
    
    
	@Override
    public void onUpdate (Context 			context, 
    					  AppWidgetManager 	appWidgetManager,
    					  int[] 			appWidgetIds) 
    {
    	ALog.v(TAG, "Entry...");
    	
		MainService.startServiceManually (context);
    	
		GPSWidget.createWidgetView (context);
        
        ALog.v(TAG, "Exit.");
    }


	public static void createWidgetView (Context context) 
	{
    	ALog.v(TAG, "Entry...");

    	RemoteViews updateViews = new RemoteViews(context.getPackageName(), 
				  								  R.layout.widget_layout);

    	
    	Drawable drawable = context.getResources().getDrawable (getResIdByStatus());
        Bitmap 	 bitmap	  = ((BitmapDrawable)drawable).getBitmap();

    	updateViews.setImageViewBitmap (R.id.bitmap, 
    								    bitmap);

        Intent intent = new Intent(MainService.GPS_MAIN);
        intent.putExtra (MainService.GPS_PAYLOAD, 
        				 MainService.GPS_SWAPSTATUS);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast (context, 
        														  0,
        														  intent,
        														  0);

        updateViews.setOnClickPendingIntent (R.id.widget,
        									 pendingIntent);
        
        ALog.w(TAG, "setOnClickPendingIntent invoked.");
    	
    	
        ComponentName thisWidget = new ComponentName(context, 
				 									 GPSWidget.class);

        AppWidgetManager manager = AppWidgetManager.getInstance (context);
        manager.updateAppWidget (thisWidget, 
        						 updateViews);

        ALog.w(TAG, "updateAppWidget invoked.");

        
        ALog.v(TAG, "Exit.");
	}


	private static int getResIdByStatus() 
	{
		if (!MainService.isGPSDecided())
		{
			return R.drawable.gps_unknown;
		}
		else
		{
			return MainService.getGPSStatus() ? R.drawable.gps_on : R.drawable.gps_off;
		}
	}
    

    private void installReceiver (Context context) 
    {
    	ALog.v(TAG, "Entry...");

    	if (!receiverInstalled)
    	{
			IntentFilter intentFilter = new IntentFilter(MainService.GPS_MAIN);
	    	context.getApplicationContext().registerReceiver (this, 
	    							  						  intentFilter);
	    	
	    	receiverInstalled = true;
    	}
    	
    	ALog.v(TAG, "Exit.");
    }

    
    private void removeReceiver (Context context)
    {
    	ALog.v(TAG, "Entry...");

    	if (receiverInstalled)
    	{
    		receiverInstalled = false;
    		context.getApplicationContext().unregisterReceiver (this);
    	}
    	
    	ALog.v(TAG, "Exit.");
    }

    
    private void askServiceForBroadcast (Context  context) 
    {
    	ALog.v(TAG, "Entry...");

    	GPSBroadcastReceiver broadcastReceiver = new GPSBroadcastReceiver();
    	
    	Intent intent = new Intent();
    	intent.setAction (LocationManager.PROVIDERS_CHANGED_ACTION);
    	broadcastReceiver.onReceive (context,  
    								 intent);

    	ALog.v(TAG, "Exit.");
    }
}
