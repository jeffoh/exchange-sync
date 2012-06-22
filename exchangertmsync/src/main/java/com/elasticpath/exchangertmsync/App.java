package com.elasticpath.exchangertmsync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.elasticpath.exchangertmsync.tasksource.exchange.ExchangeTaskSource;
import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmServerException;


public class App {
	private static final String RTM_API_KEY = "0bcf4c7e3182ec34f45321512e576300";
	private static final String RTM_SHARED_SECRET = "fbf7a0bdb0011532";

	public static void main(String[] args) {
		SettingsImpl settings = new SettingsImpl();
		
		// Initialize RTM source
		CustomRtmService rtmService = new CustomRtmService(RTM_API_KEY, RTM_SHARED_SECRET, settings);
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
			
			// Initialize exchange source
			ExchangeTaskSource exchangeSource = new ExchangeTaskSource(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
			
			// Generate matching pairs of tasks 
			List<Pair<ExchangeTaskDto, ExchangeTaskDto>> pairs = generatePairs(settings,
					rtmService.getTasksWithExchangeId(listId),
					exchangeSource.getAllTasks());
			
			// Create/complete/delete as required
			for (Pair<ExchangeTaskDto, ExchangeTaskDto> pair : pairs) {
				if (pair.getLeft() != null && pair.getRight() == null) {
					rtmService.addTask(timelineId, listId, pair.getLeft());
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
			throw new RuntimeException("An unexpected exception was thrown by Remember The Milk", e);
		} finally {
			settings.save();
		}
	}

	private static Map<String, ExchangeTaskDto> generateExchangeIdMap(Collection<ExchangeTaskDto> rtmTasks) {
		Map<String, ExchangeTaskDto> results = new HashMap<String, ExchangeTaskDto>();
		for (ExchangeTaskDto task : rtmTasks) {
			results.put(task.getExchangeId(), task);
		}
		return results;
	}

	private static List<Pair<ExchangeTaskDto, ExchangeTaskDto>> generatePairs(SettingsImpl settings, Collection<ExchangeTaskDto> rtmTasks, Collection<ExchangeTaskDto> exchangeTasks) {
		List<Pair<ExchangeTaskDto, ExchangeTaskDto>> results = new ArrayList<Pair<ExchangeTaskDto, ExchangeTaskDto>>();
		Map<String, ExchangeTaskDto> rtmTaskIdMap = generateExchangeIdMap(rtmTasks);
		for (ExchangeTaskDto exchangeTask : exchangeTasks) {
			ExchangeTaskDto rtmTask = rtmTaskIdMap.get(exchangeTask.getExchangeId());
			Pair<ExchangeTaskDto, ExchangeTaskDto> pair = new Pair<ExchangeTaskDto, ExchangeTaskDto>(exchangeTask, rtmTask);
			results.add(pair);
		}
		return results;
	}
}
