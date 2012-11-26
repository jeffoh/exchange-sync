package com.zerodes.exchangesync.dto;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

public class TaskDto {
	private String exchangeId;
	private DateTime lastModified;
	private String name;
	private DateTime dueDate;
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
	
	public DateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(DateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public DateTime getDueDate() {
		return dueDate;
	}
	
	public void setDueDate(DateTime dueDate) {
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

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(exchangeId)
			.append(lastModified)
			.append(name)
			.append(dueDate)
			.append(priority)
			.append(url)
			.append(completed)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TaskDto other = (TaskDto) obj;
		return new EqualsBuilder()
			.append(exchangeId, other.exchangeId)
			.append(lastModified, other.lastModified)
			.append(name, other.name)
			.append(dueDate, other.dueDate)
			.append(priority, other.priority)
			.append(url, other.url)
			.append(completed, other.completed)
			.isEquals();
	}
}
