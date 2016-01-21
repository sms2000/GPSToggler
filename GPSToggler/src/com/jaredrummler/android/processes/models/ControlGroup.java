package com.jaredrummler.android.processes.models;


public class ControlGroup 
{
	public final int 	id;
	public final String subsystems;
	public final String group;

	
	protected ControlGroup(String line) throws NumberFormatException, IndexOutOfBoundsException 
	{
		String[] fields = line.split(":");
		
		id 			= Integer.parseInt(fields[0]);
		subsystems 	= fields[1];
		group 		= fields[2];
	}
}
