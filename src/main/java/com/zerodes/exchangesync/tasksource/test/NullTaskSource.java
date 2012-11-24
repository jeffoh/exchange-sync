package com.zerodes.exchangesync.tasksource.test;

import java.util.ArrayList;
import java.util.Collection;

import com.zerodes.exchangesync.dto.TaskDto;
import com.zerodes.exchangesync.tasksource.exchange.TaskSource;

public class NullTaskSource implements TaskSource {

	@Override
	public Collection<TaskDto> getAllTasks() {
		return new ArrayList<TaskDto>();
	}

	@Override
	public void addTask(TaskDto task) {
		System.out.println("Added task " + task.getName());
	}

	@Override
	public void updateDueDate(TaskDto task) {
		System.out.println("Updated RTM task due date for " + task.getName());
	}

	@Override
	public void updateCompletedFlag(TaskDto task) {
		if (task.isCompleted()) {
			System.out.println("Marked task as completed for " + task.getName());
		} else {
			System.out.println("Marked task as incomplete for " + task.getName());
		}
	}
}
