package com.ogp.gpstoggler;

import java.util.List;

import com.ogp.gpstoggler.log.ALog;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;


public class SelectActivity extends Activity 
{
	private static final String 		TAG 					= "SelectActivity";
    private static final int 			MIN_TIMEOUT 			= 2000;
	
	private List<AppItem> 				applications;
	private ListView 					listOfApps;
	private ViewGroup 					viewGroup;
	private Handler						handler = new Handler();
	private Button						ok;
	private ProgressDialog 				progress = null;
	
	
	private class FillList extends Thread 
	{
		@SuppressLint("InflateParams")
		@Override
		public void run() 
        {
    		ALog.v(TAG, "FillList::run. Entry...");

    		long time = System.currentTimeMillis();
    		
    		StateMachine.loadGPSAwareApplications (true);
    		applications = StateMachine.getGPSAwareApplicationsList();
    		
    		long time2 = System.currentTimeMillis();
    		if (time2 - time < MIN_TIMEOUT)
    		{
    			try 
    			{
					Thread.sleep(MIN_TIMEOUT + time - time2);
				} 
    			catch (InterruptedException e) 
    			{
				}
    		}
    		

    		handler.post(new Runnable()
    		{
    			@Override
    			public void run() 
    			{
    	    		ALog.v(TAG, "FillList::post::run. Finishing.");
    				finishLoad();
    			}
    		});

    		ALog.v(TAG, "FillList::run. Exit.");
        }
    }
	
	
	private class StableArrayAdapter extends ArrayAdapter<AppItem> 
	{
	    public StableArrayAdapter(Context 		context) 
	    {
	    	super(context, 
	    		  R.layout.select_item,	
	    		  applications);
	    }

	    
	    @SuppressLint("ViewHolder")
		@Override
	    public View getView (int 		position, 
	    					 View 		convertView, 
	    					 ViewGroup	parent) 
	    {
	    	LayoutInflater 	inflater 	= (LayoutInflater)getSystemService (Context.LAYOUT_INFLATER_SERVICE);
	    	View 			rowView		= inflater.inflate (R.layout.select_item, 
	    													parent, 
	    													false);
	    	
	    	TextView appName  = (TextView)rowView.findViewById (R.id.firstLine);
	    	TextView packName = (TextView)rowView.findViewById (R.id.secondLine);
	        CheckBox checkBox = (CheckBox)rowView.findViewById (R.id.checkBox);
	        checkBox.setTag (R.string.itemKey, 
	        				 (Integer)position);
	        
	        checkBox.setOnCheckedChangeListener (new OnCheckedChangeListener()
	        									 {
														@Override
														public void onCheckedChanged (CompoundButton button,
																					  boolean 		 checked) 
														{
															checkedChanged ((Integer)button.getTag (R.string.itemKey), 
																			checked);
														}
	        									 });

	        AppItem item = applications.get (position);
	        
	        appName .setText    (item.getApplicationName());
	        packName.setText    (item.getPackageName());
	        checkBox.setChecked (item.getExpectsGPS());
	        
	        return rowView;
	    }
	}
	
	
	
	@SuppressLint("InflateParams")
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		ALog.v(TAG, "onCreate. Entry...");

		super.onCreate (savedInstanceState); 
		
		viewGroup  = (ViewGroup)getLayoutInflater().inflate (R.layout.activity_select, 
		  		   										     null);

		listOfApps = (ListView)viewGroup.findViewById (R.id.selectList);
		ok		   = (Button)viewGroup.findViewById (R.id.ok);
		ok.setEnabled(false);
		
		setContentView (viewGroup);

		progress = new ProgressDialog(this);
		progress.setTitle("");
		progress.setMessage("");
		progress.show();
		
		new FillList().start();

		ALog.v(TAG, "onCreate. Exit.");
	}


	protected void finishLoad()
	{
		try
		{
			progress.dismiss();
		}
		catch(Exception e)
		{
		}

		progress = null;
		
		
		ALog.v(TAG, "finishLoad. Entry...");

		TextView headline = (TextView)viewGroup.findViewById (R.id.selectDescription);

		if (applications.size() > 0)
		{
			listOfApps.setAdapter (new StableArrayAdapter(this));
			
			headline.setText (R.string.selectDescription);
		}
		else
		{
			headline.setText (R.string.selectDescriptionNo);
		}

		ok.setEnabled(true);

		ALog.v(TAG, "finishLoad. Exit.");
	}
	
	
	@Override
	protected void onResume() 
	{
		super.onResume();
	}
	
	
	@Override
	protected void onPause() 
	{
		StateMachine.saveGPSAwareApplicationsList (applications);
		
		super.onPause();
	}

	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
	
	
	public void clickOK (View view)
	{
		ALog.v(TAG, "clickOK. Entry...");

		finish();
		
		ALog.v(TAG, "clickOK. Exit.");
	}

	
	protected void checkedChanged (int 		id, 	
								   boolean 	checked) 
	{
		try
		{
			applications.get (id).setExpectsGPS (checked);
		}
		catch(Exception e)
		{
		}
	}	    
}
