package com.elasticpath.exchangertmsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
			String listId = rtmService.getIdForListName(settings.getRtmListName());
			String timelineId = rtmService.createTimeline();
			TaskSource exchangeSource = new ExchangeTaskSourceImpl(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
			List<Pair<TaskDto, TaskDto>> pairs = generatePairs(settings, rtmService.getTasks(listId), exchangeSource.getAllTasks());
			for (Pair<TaskDto, TaskDto> pair : pairs) {
				if (pair.getLeft() != null && pair.getRight() == null) {
					rtmService.addTask(timelineId, listId, pair.getLeft());
					settings.addRtmExchangeLink(pair.getLeft().getRtmTaskId(), pair.getLeft().getExchangeId());
					System.out.println("Added " + pair.getLeft().getName() + " to RTM.");
				} else if (pair.getLeft() != null && pair.getRight() != null) {
					if (pair.getLeft().isCompleted() && !pair.getRight().isCompleted()) {
						// Mark RTM task as completed
						rtmService.completeTask(timelineId, listId, pair.getRight());
					} else if (!pair.getLeft().isCompleted() && pair.getRight().isCompleted()) {
						// Mark exchange task as completed
					}
				}
			}
		} catch (RtmServerException e) {
			throw new RuntimeException("Unable to authenticate with Remember The Milk", e);
		} finally {
			settings.save();
		}
	}
	
	private static Map<String, TaskDto> generateRtmTaskIdMap(Collection<TaskDto> rtmTasks) {
		Map<String, TaskDto> results = new HashMap<String, TaskDto>();
		for (TaskDto task : rtmTasks) {
			results.put(task.getRtmTaskId(), task);
		}
		return results;
	}

	private static List<Pair<TaskDto, TaskDto>> generatePairs(SettingsImpl settings, Collection<TaskDto> rtmTasks, Collection<TaskDto> exchangeTasks) {
		List<Pair<TaskDto, TaskDto>> results = new ArrayList<Pair<TaskDto, TaskDto>>();
		Map<String, TaskDto> rtmTaskIdMap = generateRtmTaskIdMap(rtmTasks);
		for (TaskDto exchangeTask : exchangeTasks) {
			String rtmTaskId = settings.getTaskId(exchangeTask.getExchangeId());
			TaskDto rtmTask = rtmTaskIdMap.get(rtmTaskId);
			Pair<TaskDto, TaskDto> pair = new Pair<TaskDto, TaskDto>(exchangeTask, rtmTask);
			results.add(pair);
		}
		return results;
	}
}
