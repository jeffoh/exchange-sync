package com.elasticpath.exchangertmsync;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.elasticpath.exchangertmsync.sync.SyncTasks;
import com.elasticpath.exchangertmsync.tasksource.exchange.ExchangeTaskSource;
import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmServerException;

public class CustomSyncTasks extends SyncTasks {
	
	private CustomRtmTaskSource rtmSource;
	private ExchangeTaskSource exchangeSource;
	private String defaultRtmListId;
	
	public CustomSyncTasks(CustomRtmTaskSource rtmSource, ExchangeTaskSource exchangeSource, String defaultRtmListId) {
		this.rtmSource = rtmSource;
		this.exchangeSource = exchangeSource;
		this.defaultRtmListId = defaultRtmListId;
	}

	@Override
	protected List<Pair<ExchangeTaskDto, ExchangeTaskDto>> generatePairs() {
		List<Pair<ExchangeTaskDto, ExchangeTaskDto>> results = new ArrayList<Pair<ExchangeTaskDto, ExchangeTaskDto>>();
		try {
			Collection<ExchangeTaskDto> rtmTasks = rtmSource.getTasksWithExchangeId(defaultRtmListId);
			Collection<ExchangeTaskDto> exchangeTasks = exchangeSource.getAllTasks();
			Map<String, ExchangeTaskDto> rtmTaskIdMap = generateExchangeIdMap(rtmTasks);
			for (ExchangeTaskDto exchangeTask : exchangeTasks) {
				results.add(generatePairForExchangeTask(rtmTaskIdMap, exchangeTask));
			}
		} catch (RtmServerException e) {
			e.printStackTrace();
		}
		return results;
	}

	@Override
	public void rtmTaskAdd(ExchangeTaskDto task) {
		String timelineId;
		try {
			timelineId = rtmSource.createTimeline();
			rtmSource.addTask(timelineId, defaultRtmListId, task);
			System.out.println("Added RTM task " + task.getName());
		} catch (RtmServerException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void rtmTaskUpdateCompletedFlag(ExchangeTaskDto task) {
		try {
			String timelineId = rtmSource.createTimeline();
			rtmSource.updateCompleteFlag(timelineId, defaultRtmListId, task);
			if (task.isCompleted()) {
				System.out.println("Marked RTM task as completed for " + task.getName());
			} else {
				System.out.println("Marked RTM task as incomplete for " + task.getName());
			}
		} catch (RtmServerException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void rtmTaskUpdateDueDate(ExchangeTaskDto task) {
		try {
			String timelineId = rtmSource.createTimeline();
			rtmSource.updateDueDate(timelineId, defaultRtmListId, task);
			System.out.println("Updated RTM task due date for " + task.getName());
		} catch (RtmServerException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exchangeTaskUpdateCompletedFlag(ExchangeTaskDto task) {
		try {
			exchangeSource.updateCompletedFlag(task);
			if (task.isCompleted()) {
				System.out.println("Marked Exchange task as completed for " + task.getName());
			} else {
				System.out.println("Marked Exchange task as incomplete for " + task.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void exchangeTaskUpdateDueDate(ExchangeTaskDto task) {
		try {
			exchangeSource.updateDueDate(task);
			System.out.println("Updated Exchange task due date for " + task.getName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
