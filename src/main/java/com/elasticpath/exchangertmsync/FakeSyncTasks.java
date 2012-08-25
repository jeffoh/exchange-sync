package com.elasticpath.exchangertmsync;

import com.elasticpath.exchangertmsync.tasksource.exchange.ExchangeTaskSource;
import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;

public class FakeSyncTasks extends CustomSyncTasks {
	public FakeSyncTasks(CustomRtmTaskSource rtmSource, ExchangeTaskSource exchangeSource, String defaultRtmListId) {
		super(rtmSource, exchangeSource, defaultRtmListId);
	}

	@Override
	public void rtmTaskAdd(ExchangeTaskDto task) {
		System.out.println("Added RTM task " + task.getName());
	}

	@Override
	protected void rtmTaskUpdateCompletedFlag(ExchangeTaskDto task) {
		if (task.isCompleted()) {
			System.out.println("Marked RTM task as completed for " + task.getName());
		} else {
			System.out.println("Marked RTM task as incomplete for " + task.getName());
		}
	}

	@Override
	protected void rtmTaskUpdateDueDate(ExchangeTaskDto task) {
		System.out.println("Updated RTM task due date for " + task.getName());
	}

	@Override
	protected void exchangeTaskUpdateCompletedFlag(ExchangeTaskDto task) {
		if (task.isCompleted()) {
			System.out.println("Marked Exchange task as completed for " + task.getName());
		} else {
			System.out.println("Marked Exchange task as incomplete for " + task.getName());
		}
	}

	@Override
	protected void exchangeTaskUpdateDueDate(ExchangeTaskDto task) {
		System.out.println("Updated Exchange task due date for " + task.getName());
	}
}
