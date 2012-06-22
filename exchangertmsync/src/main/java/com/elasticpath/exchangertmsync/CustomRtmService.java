package com.elasticpath.exchangertmsync;

import java.util.ArrayList;
import java.util.List;

import com.elasticpath.exchangertmsync.tasksource.exchange.dto.ExchangeTaskDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmServerException;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmService;
import com.elasticpath.exchangertmsync.tasksource.rtm.RtmSettings;
import com.elasticpath.exchangertmsync.tasksource.rtm.dto.NoteDto;
import com.elasticpath.exchangertmsync.tasksource.rtm.dto.TaskDto;

public class CustomRtmService extends RtmService {
	private static final String EXCHANGE_ID_NOTE_TITLE = "ExchangeID";

	public CustomRtmService(final String apiKey, final String sharedSecret, final RtmSettings settings) {
		super(apiKey, sharedSecret, settings);
	}

	public void addTask(String timelineId, String listId, ExchangeTaskDto task) throws RtmServerException {
		// Add email tag
		task.addTag("email");
		
		// Add ExchangeID note
		NoteDto note = new NoteDto();
		note.setTitle(EXCHANGE_ID_NOTE_TITLE);
		note.setBody(task.getExchangeId());
		task.addNote(note);
		
		// Add task
		super.addTask(timelineId, listId, task);
	}
	
	public List<ExchangeTaskDto> getTasksWithExchangeId(final String listId) throws RtmServerException {
		List<ExchangeTaskDto> results = new ArrayList<ExchangeTaskDto>();
		for (TaskDto task : super.getTasks(listId)) {
			ExchangeTaskDto result = ExchangeTaskDto.fromTaskDto(task);
			for (NoteDto note : task.getNotes()) {
				if (note.getTitle().equals(EXCHANGE_ID_NOTE_TITLE)) {
					result.setExchangeId(note.getBody());
				}
			}
			results.add(result);
		}
		return results;
	}
}
