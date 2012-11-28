package com.zerodes.exchangesync.tasksource.test;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zerodes.exchangesync.dto.TaskDto;
import com.zerodes.exchangesync.tasksource.TaskSource;

public class NullTaskSourceImpl implements TaskSource {
	private static final Logger LOG = LoggerFactory.getLogger(NullTaskSourceImpl.class);

	@Override
	public Collection<TaskDto> getAllTasks() {
		return new ArrayList<TaskDto>();
	}

	@Override
	public void addTask(TaskDto task) {
		LOG.debug("Added task " + task.getName());
	}

	@Override
	public void updateDueDate(TaskDto task) {
		LOG.debug("Updated RTM task due date for " + task.getName());
	}

	@Override
	public void updateCompletedFlag(TaskDto task) {
		if (task.isCompleted()) {
			LOG.debug("Marked task as completed for " + task.getName());
		} else {
			LOG.debug("Marked task as incomplete for " + task.getName());
		}
	}
}
