package com.zerodes.exchangesync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zerodes.exchangesync.calendarsource.CalendarSource;
import com.zerodes.exchangesync.calendarsource.google.GoogleCalendarSourceImpl;
import com.zerodes.exchangesync.exchange.ExchangeSourceImpl;
import com.zerodes.exchangesync.settings.SettingsImpl;
import com.zerodes.exchangesync.sync.SyncCalendarsImpl;
import com.zerodes.exchangesync.sync.SyncTasksImpl;
import com.zerodes.exchangesync.tasksource.TaskSource;
import com.zerodes.exchangesync.tasksource.rtm.RtmTaskSourceImpl;

public class App {
	private static final Logger LOG = LoggerFactory.getLogger(App.class);
	
	public static void main(String[] args) {
		SettingsImpl settings = new SettingsImpl();
		
		try {
			// Initialize exchange source
			final ExchangeSourceImpl exchangeSource = new ExchangeSourceImpl(settings);
			
			// Initialize RTM source
			final TaskSource rtmSource = new RtmTaskSourceImpl(settings);
			
			// Initialize Google source
			final CalendarSource googleSource = new GoogleCalendarSourceImpl(settings);
			
			// Initialize statistics collector
			final StatisticsCollector stats = new StatisticsCollector();
			
			// Synchronize calendars
			final SyncCalendarsImpl syncCalendars = new SyncCalendarsImpl(exchangeSource, googleSource);
			syncCalendars.syncAll(stats);
			
			// Synchronize tasks
			final SyncTasksImpl syncTasks = new SyncTasksImpl(exchangeSource, rtmSource);
			syncTasks.syncAll(stats);
			
			// Show stats
			stats.display();
		} catch (Exception e) {
			LOG.error("An unexpected exception occurred", e);
		} finally {
			settings.save();
		}
	}
}
