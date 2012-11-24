package com.zerodes.exchangesync.settings;

public interface Settings {
	String getUserSetting(String key);
	String getInternalSetting(String key);
	void setInternalSetting(String key, String value);
}
