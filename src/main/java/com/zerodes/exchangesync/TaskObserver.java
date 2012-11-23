package com.zerodes.exchangesync;

import com.zerodes.exchangesync.tasksource.exchange.dto.ExchangeTaskDto;

public interface TaskObserver {
	void taskChanged(ExchangeTaskDto task);
}
