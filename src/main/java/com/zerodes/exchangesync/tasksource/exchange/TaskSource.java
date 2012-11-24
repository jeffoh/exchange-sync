package com.zerodes.exchangesync.tasksource.exchange;

import java.util.Collection;

import com.zerodes.exchangesync.dto.TaskDto;

public interface TaskSource {
	Collection<TaskDto> getAllTasks();
	
	void addTask(TaskDto task);
	
	void updateDueDate(TaskDto task);
	
	void updateCompletedFlag(TaskDto task);
}
