package com.elasticpath.exchangertmsync.tasksource.exchange.dto;

import com.elasticpath.exchangertmsync.tasksource.rtm.dto.NoteDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.dto.TaskDto;

public class ExchangeTaskDto extends TaskDto {
	private String exchangeId;

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}
	
	public static ExchangeTaskDto fromTaskDto(final TaskDto task) {
		ExchangeTaskDto result = new ExchangeTaskDto();
		result.setRtmTaskId(task.getRtmTaskId());
		result.setRtmTimeSeriesId(task.getRtmTimeSeriesId());
		result.setLastModified(task.getLastModified());
		result.setName(task.getName());
		result.setPriority(task.getPriority());
		result.setUrl(task.getUrl());
		result.setDueDate(task.getDueDate());
		result.setCompleted(task.isCompleted());
		for (String tag : task.getTags()) {
			result.addTag(tag);
		}
		for (NoteDto note : task.getNotes()) {
			result.addNote(note);
		}
		return result;
	}
}
