package com.zerodes.exchangesync;

import com.zerodes.exchangesync.dto.TaskDto;

public interface TaskObserver {
	void taskChanged(TaskDto task);
}
