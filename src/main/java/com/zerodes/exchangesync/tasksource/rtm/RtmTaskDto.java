package com.zerodes.exchangesync.tasksource.rtm;

import com.zerodes.exchangesync.dto.TaskDto;

public class RtmTaskDto extends TaskDto {
	private String rtmTaskId;
	private String rtmTimeSeriesId;

	public String getRtmTaskId() {
		return rtmTaskId;
	}

	public void setRtmTaskId(String rtmTaskId) {
		this.rtmTaskId = rtmTaskId;
	}

	public String getRtmTimeSeriesId() {
		return rtmTimeSeriesId;
	}

	public void setRtmTimeSeriesId(String rtmTimeSeriesId) {
		this.rtmTimeSeriesId = rtmTimeSeriesId;
	}
}
