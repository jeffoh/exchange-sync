package com.zerodes.exchangesync.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class TaskDto {
	private String exchangeId;
	private Date lastModified;
	private String name;
	private Date dueDate;
	private Byte priority;
	private String url;
	private boolean completed;
	private Set<String> tags = new HashSet<String>();
	private Set<NoteDto> notes = new HashSet<NoteDto>();
	
	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
	}
	
	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Date getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	
	public Byte getPriority() {
		return priority;
	}
	
	public void setPriority(Byte priority) {
		this.priority = priority;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void addTag(final String tag) {
		tags.add(tag);
	}

	public Set<NoteDto> getNotes() {
		return notes;
	}

	public void addNote(final NoteDto note) {
		notes.add(note);
	}
	
	public void copyTo(final TaskDto dest) {
		dest.exchangeId = exchangeId;
		dest.lastModified = lastModified;
		dest.name = name;
		dest.dueDate = dueDate;
		dest.priority = priority;
		dest.url = url;
		dest.completed = completed;
		dest.tags = tags;
		dest.notes = notes;
	}
}
