package com.zerodes.exchangesync;

import java.util.ArrayList;
import java.util.List;

import com.zerodes.exchangesync.tasksource.exchange.dto.ExchangeTaskDto;
import com.zerodes.exchangesync.tasksource.rtm.RtmServerException;
import com.zerodes.exchangesync.tasksource.rtm.RtmSettings;
import com.zerodes.exchangesync.tasksource.rtm.RtmTaskSource;
import com.zerodes.exchangesync.tasksource.rtm.dto.NoteDto;
import com.zerodes.exchangesync.tasksource.rtm.dto.TaskDto;

public class CustomRtmTaskSource extends RtmTaskSource {
	private static final String EXCHANGE_ID_NOTE_TITLE = "ExchangeID";
	private static final String ORIGINAL_SUBJECT_NOTE_TITLE = "Original Email Subject";

	public CustomRtmTaskSource(final String apiKey, final String sharedSecret, final RtmSettings settings) {
		super(apiKey, sharedSecret, settings);
	}

	@Override
	public void addTask(final String timelineId, final String listId, final TaskDto task) throws RtmServerException {
		throw new UnsupportedOperationException("Make sure to call addTask with an ExchangeTaskDto object.");
	}

	public void addTask(String timelineId, String listId, ExchangeTaskDto task) throws RtmServerException {
		// Add email tag
		task.addTag("email");

		// Add ExchangeID note
		NoteDto exchangeIdNote = new NoteDto();
		exchangeIdNote.setTitle(EXCHANGE_ID_NOTE_TITLE);
		exchangeIdNote.setBody(task.getExchangeId());
		task.addNote(exchangeIdNote);

		// Add Original Subject note
		NoteDto originalSubjectNote = new NoteDto();
		originalSubjectNote.setTitle(ORIGINAL_SUBJECT_NOTE_TITLE);
		originalSubjectNote.setBody(task.getName());
		task.addNote(originalSubjectNote);

		// Add task
		super.addTask(timelineId, listId, task);
	}

	public List<ExchangeTaskDto> getTasksWithExchangeId(final String listId) throws RtmServerException {
		List<ExchangeTaskDto> results = new ArrayList<ExchangeTaskDto>();
		for (TaskDto task : super.getAllTasks(listId)) {
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
