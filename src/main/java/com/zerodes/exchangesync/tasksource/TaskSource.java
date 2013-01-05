package com.zerodes.exchangesync.tasksource;

import java.util.Collection;

import com.zerodes.exchangesync.dto.TaskDto;

public interface TaskSource {
	Collection<TaskDto> getAllTasks() throws Exception;
	
	void addTask(TaskDto task) throws Exception;
	
	void updateDueDate(TaskDto task) throws Exception;
	
	void updateCompletedFlag(TaskDto task) throws Exception;
}
