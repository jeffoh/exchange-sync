package com.elasticpath.exchangertmsync.sync;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;

import com.elasticpath.exchangertmsync.Pair;
import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;

public abstract class SyncTasks {
	
	/**
	 * Take a matching RTM task and exchange task and determine what needs to be done to sync them.
	 * 
	 * @param rtmTask Remember the Milk task (or null if no matching task exists)
	 * @param exchangeTask Exchange task (or null if no matching task exists)
	 */
	public void sync(ExchangeTaskDto rtmTask, ExchangeTaskDto exchangeTask) {
		if (exchangeTask != null && rtmTask == null) {
			rtmTaskAdd(exchangeTask);
		} else if (exchangeTask != null && rtmTask != null) {
			if (exchangeTask.getLastModified().after(rtmTask.getLastModified())) {
				// Exchange task has a more recent modified date, so modify RTM
				if (exchangeTask.isCompleted() != rtmTask.isCompleted()) {
					rtmTask.setCompleted(exchangeTask.isCompleted());
					rtmTaskUpdateCompletedFlag(rtmTask);
				}
				if (!ObjectUtils.equals(exchangeTask.getDueDate(), rtmTask.getDueDate())) {
					rtmTask.setDueDate(exchangeTask.getDueDate());
					rtmTaskUpdateDueDate(rtmTask);
				}
			} else {
				// RTM task has a more recent modified date, so modify Exchange
				if (exchangeTask.isCompleted() != rtmTask.isCompleted()) {
					exchangeTask.setCompleted(rtmTask.isCompleted());
					exchangeTaskUpdateCompletedFlag(exchangeTask);
				}
				if (!ObjectUtils.equals(exchangeTask.getDueDate(), rtmTask.getDueDate())) {
					exchangeTask.setDueDate(rtmTask.getDueDate());
					exchangeTaskUpdateDueDate(exchangeTask);
				}
			}
		}
	}
	
	public void syncAll() {
		// Generate matching pairs of tasks 
		List<Pair<ExchangeTaskDto, ExchangeTaskDto>> pairs = generatePairs();
		
		// Create/complete/delete as required
		for (Pair<ExchangeTaskDto, ExchangeTaskDto> pair : pairs) {
			sync(pair.getLeft(), pair.getRight());
		}
	}

	protected abstract List<Pair<ExchangeTaskDto, ExchangeTaskDto>> generatePairs();

	public Map<String, ExchangeTaskDto> generateExchangeIdMap(Collection<ExchangeTaskDto> rtmTasks) {
		Map<String, ExchangeTaskDto> results = new HashMap<String, ExchangeTaskDto>();
		for (ExchangeTaskDto task : rtmTasks) {
			results.put(task.getExchangeId(), task);
		}
		return results;
	}

	public Pair<ExchangeTaskDto, ExchangeTaskDto> generatePairForExchangeTask(Map<String, ExchangeTaskDto> rtmTaskIdMap, ExchangeTaskDto exchangeTask) {
		ExchangeTaskDto rtmTask = rtmTaskIdMap.get(exchangeTask.getExchangeId());
		return new Pair<ExchangeTaskDto, ExchangeTaskDto>(rtmTask, exchangeTask);
	}
	
	protected abstract void rtmTaskAdd(ExchangeTaskDto task);
	
	protected abstract void rtmTaskUpdateCompletedFlag(ExchangeTaskDto task);

	protected abstract void rtmTaskUpdateDueDate(ExchangeTaskDto task);

	protected abstract void exchangeTaskUpdateCompletedFlag(ExchangeTaskDto task);

	protected abstract void exchangeTaskUpdateDueDate(ExchangeTaskDto task);
}
