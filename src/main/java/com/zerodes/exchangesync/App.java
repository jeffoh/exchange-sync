package com.zerodes.exchangesync;

import java.util.List;
import java.util.Map;

import com.zerodes.exchangesync.sync.SyncTasks;
import com.zerodes.exchangesync.tasksource.exchange.ExchangeTaskSource;
import com.zerodes.exchangesync.tasksource.exchange.dto.ExchangeTaskDto;
import com.zerodes.exchangesync.tasksource.rtm.RtmServerException;


public class App {
	private static final String RTM_API_KEY = "0bcf4c7e3182ec34f45321512e576300";
	private static final String RTM_SHARED_SECRET = "fbf7a0bdb0011532";
	private static final int DELAY_BETWEEN_POLLS = 300000; // 5 minutes in milliseconds
	
	public static void main(String[] args) {
		SettingsImpl settings = new SettingsImpl();
		
		try {
			// Initialize RTM source
			final CustomRtmTaskSource rtmSource = new CustomRtmTaskSource(RTM_API_KEY, RTM_SHARED_SECRET, settings);
			switch (rtmSource.getAuthStatus()) {
			case NEEDS_USER_APPROVAL:
				System.out.println("Please go to the following URL to authorize application to sync with Remember The Milk: "
						+ rtmSource.getAuthenticationUrl("write"));
				settings.save();
				return;
			case NEEDS_AUTH_TOKEN:
				rtmSource.completeAuthentication();
			}
			
			// Initialize exchange source
			final ExchangeTaskSource exchangeSource = new ExchangeTaskSource(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
			
			// Initialize synchronization engine
			final String defaultRtmListId = rtmSource.getIdForListName(settings.getRtmListName());
			final SyncTasks syncTasks = new CustomSyncTasks(rtmSource, exchangeSource, defaultRtmListId);
			
			final TaskObserver taskObserver = new TaskObserver() {
				@Override
				public void taskChanged(ExchangeTaskDto task) {
					List<ExchangeTaskDto> rtmTasks;
					try {
						rtmTasks = rtmSource.getTasksWithExchangeId(defaultRtmListId);
						Map<String, ExchangeTaskDto> rtmTaskIdMap = syncTasks.generateExchangeIdMap(rtmTasks);
						Pair<ExchangeTaskDto, ExchangeTaskDto> pair = syncTasks.generatePairForExchangeTask(rtmTaskIdMap, task);
						syncTasks.sync(pair.getLeft(), pair.getRight());
					} catch (RtmServerException e) {
						e.printStackTrace();
					}
				}
			};
			exchangeSource.addTaskEventListener(taskObserver);

			while(true) {
				System.out.println("Synchronizing all tasks...");
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
