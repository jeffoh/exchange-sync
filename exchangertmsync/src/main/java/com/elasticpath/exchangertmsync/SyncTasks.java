package com.elasticpath.exchangertmsync;

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
				if (!exchangeTask.getDueDate().equals(rtmTask.getDueDate())) {
					rtmTask.setDueDate(exchangeTask.getDueDate());
					rtmTaskUpdateDueDate(rtmTask);
				}
			} else {
				// RTM task has a more recent modified date, so modify Exchange
				if (exchangeTask.isCompleted() != rtmTask.isCompleted()) {
					exchangeTask.setCompleted(rtmTask.isCompleted());
					exchangeTaskUpdateCompletedFlag(exchangeTask);
				}
				if (!exchangeTask.getDueDate().equals(rtmTask.getDueDate())) {
					exchangeTask.setDueDate(rtmTask.getDueDate());
					exchangeTaskUpdateDueDate(exchangeTask);
				}
			}
		}
	}
	
	protected abstract void rtmTaskAdd(ExchangeTaskDto task);
	
	protected abstract void rtmTaskUpdateCompletedFlag(ExchangeTaskDto task);

	protected abstract void rtmTaskUpdateDueDate(ExchangeTaskDto task);

	protected abstract void exchangeTaskUpdateCompletedFlag(ExchangeTaskDto task);

	protected abstract void exchangeTaskUpdateDueDate(ExchangeTaskDto task);
}
