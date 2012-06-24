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
		final CustomRtmService rtmService = new CustomRtmService(RTM_API_KEY, RTM_SHARED_SECRET, settings);
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
			final String listId = rtmService.getIdForListName(settings.getRtmListName());
			
			// Initialize exchange source
			ExchangeTaskSource exchangeSource = new ExchangeTaskSource(settings.getExchangeHost(), settings.getExchangeUsername(), settings.getExchangePassword());
			
			final SyncTasks syncTasks = new SyncTasks() {
				@Override
				public void rtmAddTask(ExchangeTaskDto task) {
					String timelineId;
					try {
						timelineId = rtmService.createTimeline();
						rtmService.addTask(timelineId, listId, task);
						System.out.println("Added " + task.getName() + " to RTM.");
					} catch (RtmServerException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void rtmCompleteTask(ExchangeTaskDto task) {
					try {
						String timelineId = rtmService.createTimeline();
						rtmService.completeTask(timelineId, listId, task);
						System.out.println("Completed " + task.getName() + " in RTM.");
					} catch (RtmServerException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void exchangeCompleteTask(ExchangeTaskDto task) {
					// TODO: Complete exchange task
					System.out.println("Completed " + task.getName() + " in Exchange (not yet implemented).");
				}
			};
			
			final TaskObserver taskObserver = new TaskObserver() {
				@Override
				public void taskChanged(ExchangeTaskDto task) {
					List<ExchangeTaskDto> rtmTasks;
					try {
						rtmTasks = rtmService.getTasksWithExchangeId(listId);
						Map<String, ExchangeTaskDto> rtmTaskIdMap = generateExchangeIdMap(rtmTasks);
						Pair<ExchangeTaskDto, ExchangeTaskDto> pair = generatePairForExchangeTask(rtmTaskIdMap, task);
						syncTasks.sync(pair.getLeft(), pair.getRight());
					} catch (RtmServerException e) {
						e.printStackTrace();
					}
				}
			};
			exchangeSource.addTaskEventListener(taskObserver);

			// Generate matching pairs of tasks 
			List<Pair<ExchangeTaskDto, ExchangeTaskDto>> pairs = generatePairs(
					rtmService.getTasksWithExchangeId(listId),
					exchangeSource.getAllTasks());
			
			// Create/complete/delete as required
			for (Pair<ExchangeTaskDto, ExchangeTaskDto> pair : pairs) {
				syncTasks.sync(pair.getLeft(), pair.getRight());
			}
			
			System.out.println("Waiting for events...");
			while(true) {
				Thread.sleep(1000);
			}
		} catch (RtmServerException e) {
			throw new RuntimeException("An unexpected exception was thrown by Remember The Milk", e);
		} catch (Exception e) {
			throw new RuntimeException("An unexpected exception occurred", e);
		} finally {
			settings.save();
		}
	}

	private static List<Pair<ExchangeTaskDto, ExchangeTaskDto>> generatePairs(Collection<ExchangeTaskDto> rtmTasks, Collection<ExchangeTaskDto> exchangeTasks) {
		List<Pair<ExchangeTaskDto, ExchangeTaskDto>> results = new ArrayList<Pair<ExchangeTaskDto, ExchangeTaskDto>>();
		Map<String, ExchangeTaskDto> rtmTaskIdMap = generateExchangeIdMap(rtmTasks);
		for (ExchangeTaskDto exchangeTask : exchangeTasks) {
			results.add(generatePairForExchangeTask(rtmTaskIdMap, exchangeTask));
		}
		return results;
	}

	private static Map<String, ExchangeTaskDto> generateExchangeIdMap(Collection<ExchangeTaskDto> rtmTasks) {
		Map<String, ExchangeTaskDto> results = new HashMap<String, ExchangeTaskDto>();
		for (ExchangeTaskDto task : rtmTasks) {
			results.put(task.getExchangeId(), task);
		}
		return results;
	}

	private static Pair<ExchangeTaskDto, ExchangeTaskDto> generatePairForExchangeTask(Map<String, ExchangeTaskDto> rtmTaskIdMap, ExchangeTaskDto exchangeTask) {
		ExchangeTaskDto rtmTask = rtmTaskIdMap.get(exchangeTask.getExchangeId());
		return new Pair<ExchangeTaskDto, ExchangeTaskDto>(rtmTask, exchangeTask);
	}
}
