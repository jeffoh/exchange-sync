package com.zerodes.exchangesync;

import java.util.Collection;
import java.util.Map;

import com.zerodes.exchangesync.calendarsource.CalendarSource;
import com.zerodes.exchangesync.calendarsource.google.GoogleCalendarSourceImpl;
import com.zerodes.exchangesync.dto.TaskDto;
import com.zerodes.exchangesync.exchange.ExchangeSourceImpl;
import com.zerodes.exchangesync.settings.SettingsImpl;
import com.zerodes.exchangesync.sync.SyncCalendarsImpl;
import com.zerodes.exchangesync.sync.SyncTasksImpl;
import com.zerodes.exchangesync.tasksource.TaskSource;
import com.zerodes.exchangesync.tasksource.rtm.RtmServerException;
import com.zerodes.exchangesync.tasksource.rtm.RtmTaskSourceImpl;


public class App {
	private static final int DELAY_BETWEEN_POLLS = 300000; // 5 minutes in milliseconds
	
	public static void main(String[] args) {
		SettingsImpl settings = new SettingsImpl();
		
		try {
			// Initialize exchange source
			final ExchangeSourceImpl exchangeSource = new ExchangeSourceImpl(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
			
			// Initialize RTM source
			final TaskSource rtmSource = new RtmTaskSourceImpl(settings);
			
			// Initialize Google source
			final CalendarSource googleSource = new GoogleCalendarSourceImpl(settings);
			
			// Initialize synchronization engines
			final SyncTasksImpl syncTasks = new SyncTasksImpl(exchangeSource, rtmSource);
			final SyncCalendarsImpl syncCalendars = new SyncCalendarsImpl(exchangeSource, googleSource);
			
			final TaskObserver taskObserver = new TaskObserver() {
				@Override
				public void taskChanged(TaskDto task) {
					Collection<TaskDto> rtmTasks = rtmSource.getAllTasks();
					Map<String, TaskDto> rtmTaskIdMap = syncTasks.generateExchangeIdMap(rtmTasks);
					Pair<TaskDto, TaskDto> pair = syncTasks.generatePairForExchangeTask(rtmTaskIdMap, task);
					syncTasks.sync(pair.getLeft(), pair.getRight());
				}
			};
			exchangeSource.addTaskEventListener(taskObserver);

			while(true) {
				System.out.println("Synchronizing calendars...");
				syncCalendars.syncAll();
				System.out.println("Synchronizing tasks...");
				syncTasks.syncAll();
				System.out.println("Waiting for events...");
				Thread.sleep(DELAY_BETWEEN_POLLS);
			}
		} catch (RtmServerException e) {
			throw new RuntimeException("An unexpected exception was thrown by Remember The Milk", e);
		} catch (Exception e) {
			throw new RuntimeException("An unexpected exception occurred", e);
		} finally {
			settings.save();
		}
	}
}
