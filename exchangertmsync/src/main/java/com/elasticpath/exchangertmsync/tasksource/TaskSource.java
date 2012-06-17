package com.elasticpath.exchangertmsync.tasksource;

import java.util.Observer;
import java.util.Set;

public interface TaskSource {
	public Set<TaskDto> getAllTasks();
	public void addTaskAddedListener(Observer observer);
	public void addTaskCompletedListener(Observer observer);
	public void addTaskDeletedListener(Observer observer);
}
