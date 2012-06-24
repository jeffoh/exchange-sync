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
			rtmAddTask(exchangeTask);
		} else if (exchangeTask != null && rtmTask != null) {
			if (exchangeTask.isCompleted() && !rtmTask.isCompleted()) {
				rtmCompleteTask(rtmTask);
			} else if (!exchangeTask.isCompleted() && rtmTask.isCompleted()) {
				exchangeCompleteTask(exchangeTask);
			}
		}
	}
	
	protected abstract void rtmAddTask(ExchangeTaskDto task);
	
	protected abstract void rtmCompleteTask(ExchangeTaskDto task);

	protected abstract void exchangeCompleteTask(ExchangeTaskDto task);
}
