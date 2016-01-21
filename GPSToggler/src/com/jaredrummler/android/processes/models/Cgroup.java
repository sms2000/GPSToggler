package com.jaredrummler.android.processes.models;

import java.io.IOException;
import java.util.ArrayList;


@SuppressWarnings("serial")
public final class Cgroup extends ProcFile 
{
	public final ArrayList<ControlGroup> groups;

	
	public static Cgroup get(int pid) throws IOException 
	{
		return new Cgroup(String.format("/proc/%d/cgroup", pid));
	}

	
	private Cgroup(String path) throws IOException 
	{
		super(path);
		
		String[] lines = content.split("\n");
		groups = new ArrayList<ControlGroup>();
		for (String line : lines) 
		{
			try 
			{
				groups.add(new ControlGroup(line));
			} 
			catch (Exception ignored) 
			{
			}
		}
	}

	
	public ControlGroup getGroup(String subsystem) 
	{
		for (ControlGroup group : groups) 
		{
			String[] systems = group.subsystems.split(",");
			
			for (String name : systems) 
			{
				if (name.equals(subsystem)) 
				{
					return group;
				}
			}
		}
		
		return null;
	}
}
