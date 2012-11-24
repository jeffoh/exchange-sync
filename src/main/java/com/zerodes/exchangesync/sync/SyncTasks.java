package com.zerodes.exchangesync.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.zerodes.exchangesync.Pair;
import com.zerodes.exchangesync.dto.TaskDto;
import com.zerodes.exchangesync.tasksource.exchange.TaskSource;

public class SyncTasks {

	private TaskSource exchangeSource;
	private TaskSource otherSource;

	public SyncTasks(TaskSource exchangeSource, TaskSource otherSource) {
		this.exchangeSource = exchangeSource;
		this.otherSource = otherSource;
	}

	protected List<Pair<TaskDto, TaskDto>> generatePairs() {
		List<Pair<TaskDto, TaskDto>> results = new ArrayList<Pair<TaskDto, TaskDto>>();
		Collection<TaskDto> otherTasks = otherSource.getAllTasks();
		Collection<TaskDto> exchangeTasks = exchangeSource.getAllTasks();
		Map<String, TaskDto> otherTasksMap = generateExchangeIdMap(otherTasks);
		for (TaskDto exchangeTask : exchangeTasks) {
			results.add(generatePairForExchangeTask(otherTasksMap, exchangeTask));
		}
		return results;
	}

	/**
	 * Take a matching exchange task and other task and determine what needs to be done to sync them.
	 *
	 * @param exchangeTask Exchange task (or null if no matching task exists)
	 * @param otherTask Task from "other" data source (or null if no matching task exists)
	 */
	public void sync(TaskDto exchangeTask, TaskDto otherTask) {
		if (exchangeTask != null && !exchangeTask.isCompleted() && otherTask == null) {
			otherSource.addTask(exchangeTask);
		} else if (exchangeTask != null && otherTask != null) {
			if (exchangeTask.getLastModified().after(otherTask.getLastModified())) {
				// Exchange task has a more recent modified date, so modify other task
				if (exchangeTask.isCompleted() != otherTask.isCompleted()) {
					otherTask.setCompleted(exchangeTask.isCompleted());
					otherSource.updateCompletedFlag(otherTask);
				}
				if (!ObjectUtils.equals(exchangeTask.getDueDate(), otherTask.getDueDate())) {
					otherTask.setDueDate(exchangeTask.getDueDate());
					otherSource.updateDueDate(otherTask);
				}
			} else {
				// Other task has a more recent modified date, so modify Exchange
				if (exchangeTask.isCompleted() != otherTask.isCompleted()) {
					exchangeTask.setCompleted(otherTask.isCompleted());
					exchangeSource.updateCompletedFlag(exchangeTask);
				}
				if (!ObjectUtils.equals(exchangeTask.getDueDate(), otherTask.getDueDate())) {
					exchangeTask.setDueDate(otherTask.getDueDate());
					exchangeSource.updateDueDate(exchangeTask);
				}
			}
		}
	}

	public void syncAll() {
		// Generate matching pairs of tasks
		List<Pair<TaskDto, TaskDto>> pairs = generatePairs();

		// Create/complete/delete as required
		for (Pair<TaskDto, TaskDto> pair : pairs) {
			sync(pair.getLeft(), pair.getRight());
		}
	}

	public Map<String, TaskDto> generateExchangeIdMap(Collection<TaskDto> tasks) {
		Map<String, TaskDto> results = new HashMap<String, TaskDto>();
		for (TaskDto task : tasks) {
			results.put(task.getExchangeId(), task);
		}
		return results;
	}

	public Pair<TaskDto, TaskDto> generatePairForExchangeTask(Map<String, TaskDto> otherTaskIdMap, TaskDto exchangeTask) {
		TaskDto otherTask = otherTaskIdMap.get(exchangeTask.getExchangeId());
		return new Pair<TaskDto, TaskDto>(exchangeTask, otherTask);
	}
}
