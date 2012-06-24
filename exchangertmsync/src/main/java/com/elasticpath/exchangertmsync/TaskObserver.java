package com.elasticpath.exchangertmsync;

import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;

public interface TaskObserver {
	void taskChanged(ExchangeTaskDto task);
}
