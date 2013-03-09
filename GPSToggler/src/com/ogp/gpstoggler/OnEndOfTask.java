package com.ogp.gpstoggler;


public interface OnEndOfTask 
{
	public enum TaskIndex
	{
		MOVE_MODULE,
		CLEAN_MODULE,
	}
	
	
	public void	onEndOfTask 	(TaskIndex		task, 
							 	 boolean 		result);
}
