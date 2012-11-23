package com.zerodes.exchangesync;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import com.zerodes.exchangesync.tasksource.exchange.ExchangeSettings;
import com.zerodes.exchangesync.tasksource.rtm.RtmSettings;

public class SettingsImpl implements RtmSettings, ExchangeSettings {
	private Properties userSettings;
	private Properties internalSettings;
	
	public SettingsImpl() {
		userSettings = new Properties();
		internalSettings = new Properties();
		try {
			InputStream userSettingsStream = new FileInputStream("rtmsync.properties");
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
	public String getFrob() {
		return internalSettings.getProperty("frob");
	}

	@Override
	public void setFrob(String frob) {
		if (frob == null) {
			internalSettings.remove("frob");
		} else {
			internalSettings.setProperty("frob", frob);
		}
	}

	@Override
	public String getAuthToken() {
		return internalSettings.getProperty("authToken");
	}

	@Override
	public void setAuthToken(String authToken) {
		internalSettings.setProperty("authToken", authToken);
	}

	@Override
	public String getRtmListName() {
		return userSettings.getProperty("rtmListName");
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
}
