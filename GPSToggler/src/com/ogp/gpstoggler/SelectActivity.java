package com.ogp.gpstoggler;

import java.util.List;

import com.ogp.gpstoggler.log.ALog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.content.Context;


public class SelectActivity extends Activity 
{
	private static final String 		TAG 					= "SelectActivity";
	
	private ListView					listOfApps;
	private List<AppItem> 				applications;
	
	
	private class StableArrayAdapter extends ArrayAdapter<AppItem> 
	{
	    public StableArrayAdapter(Context 		context) 
	    {
	    	super(context, 
	    		  R.layout.select_item,	
	    		  applications);
	    }

	    
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
	
	
	
	@Override
	protected void onCreate (Bundle savedInstanceState) 
	{
		ALog.v(TAG, "onCreate. Entry...");

		super.onCreate (savedInstanceState); 
		
		ViewGroup viewGroup = (ViewGroup)getLayoutInflater().inflate (R.layout.activity_select, 
				  													  null);
		setContentView (viewGroup);

		listOfApps 		  = (ListView)viewGroup.findViewById (R.id.selectList);
		TextView headline = (TextView)viewGroup.findViewById (R.id.selectDescription);

		StateMachine.loadGPSAwareApplications (true);
		applications = StateMachine.getGPSAwareApplicationsList();
		
		if (applications.size() > 0)
		{
			listOfApps.setAdapter (new StableArrayAdapter(this));
			
			headline.setText (R.string.selectDescription);
		}
		else
		{
			headline.setText (R.string.selectDescriptionNo);
		}

		
		
		ALog.v(TAG, "onCreate. Exit.");
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
