package com.elasticpath.exchangertmsync;

import java.net.URI;

import com.elasticpath.exchangertmsync.tasksource.TaskDto;
import com.elasticpath.exchangertmsync.tasksource.TaskSource;
import com.elasticpath.exchangertmsync.tasksource.exchange.ExchangeTaskSourceImpl;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmServerException;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmService;


public class App {
	private static final String RTM_API_KEY = "0bcf4c7e3182ec34f45321512e576300";
	private static final String RTM_SHARED_SECRET = "fbf7a0bdb0011532";

	public static void main(String[] args) {
		SettingsImpl settings = new SettingsImpl();
		RtmService rtmService = new RtmService(RTM_API_KEY, RTM_SHARED_SECRET, settings);
		try {
			switch (rtmService.getAuthStatus()) {
			case NEEDS_USER_APPROVAL:
				System.out.println("Please go to the following URL to authorize application to sync with Remember The Milk: "
						+ rtmService.getAuthenticationUrl("write"));
				settings.save();
				return;
			case NEEDS_AUTH_TOKEN:
				rtmService.completeAuthentication();
			}
			settings.save();
			String listId = rtmService.getIdForListName(settings.getRtmListName());
			String timelineId = rtmService.createTimeline();
			TaskDto task = new TaskDto();
			task.setName("Me & I am a fish");
			rtmService.addTask(timelineId, listId, task);
//			TaskSource exchangeSource = new ExchangeTaskSourceImpl(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
//			for (TaskDto task : exchangeSource.getAllTasks()) {
//				rtmService.addTask(timelineId, listId, task);
//			}
		} catch (RtmServerException e) {
			throw new RuntimeException("Unable to authenticate with Remember The Milk", e);
		}
	}
}
