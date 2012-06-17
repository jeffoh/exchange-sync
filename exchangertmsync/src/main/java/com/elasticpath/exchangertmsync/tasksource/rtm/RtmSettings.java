package com.elasticpath.exchangertmsync.tasksource.rtm;

public interface RtmSettings {
	
	String getFrob();
	
	void setFrob(String frob);
	
	String getAuthToken();
	
	void setAuthToken(String authToken);
	
	String getRtmListName();
}
