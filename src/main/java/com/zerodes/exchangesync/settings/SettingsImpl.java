package com.zerodes.exchangesync.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.zerodes.exchangesync.ExchangeSettings;

public class SettingsImpl implements Settings, ExchangeSettings {
	private Properties userSettings;
	private Properties internalSettings;
	
	public SettingsImpl() {
		userSettings = new Properties();
		internalSettings = new Properties();
		try {
			InputStream userSettingsStream = new FileInputStream(System.getProperty("exchangesync.properties", "exchangesync.properties"));
			if (userSettingsStream != null) {
				userSettings.load(userSettingsStream);
				userSettingsStream.close();
			}
		} catch (IOException e) {
			// Do nothing, just use defaults
		}
		try {
			InputStream internalSettingsStream = new FileInputStream("internal.properties");
			if (internalSettingsStream != null) {
				internalSettings.load(internalSettingsStream);
				internalSettingsStream.close();
			}
		} catch (IOException e) {
			// Do nothing, just use defaults
		}
	}
	
	public void save() {
		try {
			OutputStream internalSettingsStream = new FileOutputStream("internal.properties");
			internalSettings.store(internalSettingsStream, null);
			internalSettingsStream.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to save settings.", e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save settings.", e);
		}
	}
	

	@Override
	public boolean syncTasks() {
		if ("true".equalsIgnoreCase(userSettings.getProperty("syncTasks"))) {
			return true;
		}
		return false;
	}

	@Override
	public boolean syncAppointments() {
		if ("true".equalsIgnoreCase(userSettings.getProperty("syncAppointments"))) {
			return true;
		}
		return false;
	}

	@Override
	public String getExchangeHost() {
		return userSettings.getProperty("exchangeHost");
	}

	@Override
	public String getExchangeUsername() {
		return userSettings.getProperty("exchangeUsername");
	}

	@Override
	public String getExchangePassword() {
		return userSettings.getProperty("exchangePassword");
	}

	@Override
	public String getExchangeDomain() {
		return userSettings.getProperty("exchangeDomain");
	}

	@Override
	public String getExchangeVersion() {
	    return userSettings.getProperty("exchangeVersion");
	}

	@Override
	public String getUserSetting(String key) {
		return userSettings.getProperty(key);
	}

	@Override
	public String getInternalSetting(String key) {
		return internalSettings.getProperty(key);
	}

	@Override
	public void setInternalSetting(String key, String value) {
		if (value == null) {
			internalSettings.remove(key);
		} else {
			internalSettings.setProperty(key, value);
		}
		save();
	}
}
