package com.zerodes.exchangesync.dto;

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.joda.time.DateTime;

public class AppointmentDto {
	private String exchangeId;
	private DateTime lastModified;
	private String summary;
	private String description;
	private DateTime start;
	private DateTime end;
	private String location;
	private PersonDto organizer;
	private Set<PersonDto> attendees;
	private Integer reminderMinutesBeforeStart;
	private RecurrenceType recurrenceType;
	private Integer recurrenceCount;
	
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
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String name) {
		this.description = name;
	}
	
	public void setStart(DateTime start) {
		this.start = start;
	}
	
	public DateTime getStart() {
		return start;
	}
	
	public void setEnd(DateTime end) {
		this.end = end;
	}
	
	public DateTime getEnd() {
		return end;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	public String getLocation() {
		return location;
	}
	
	public PersonDto getOrganizer() {
		return organizer;
	}
	
	public void setOrganizer(PersonDto organizer) {
		this.organizer = organizer;
	}
	
	public Set<PersonDto> getAttendees() {
		return attendees;
	}
	
	public void setAttendees(Set<PersonDto> attendees) {
		this.attendees = attendees;
	}
	
	public Integer getReminderMinutesBeforeStart() {
		return reminderMinutesBeforeStart;
	}
	
	public void setReminderMinutesBeforeStart(Integer reminderMinutesBeforeStart) {
		this.reminderMinutesBeforeStart = reminderMinutesBeforeStart;
	}
	
	public RecurrenceType getRecurrenceType() {
		return recurrenceType;
	}
	
	public void setRecurrenceType(RecurrenceType recurrenceType) {
		this.recurrenceType = recurrenceType;
	}
	
	public Integer getRecurrenceCount() {
		return recurrenceCount;
	}
	
	public void setRecurrenceCount(Integer recurrenceCount) {
		this.recurrenceCount = recurrenceCount;
	}
	
	public void copyTo(AppointmentDto dest) {
		dest.exchangeId = exchangeId;
		dest.lastModified = lastModified;
		dest.summary = summary;
		dest.description = description;
		dest.start = start;
		dest.end = end;
		dest.location = location;
		dest.organizer = organizer;
		dest.attendees = attendees;
	}

	public enum RecurrenceType {
		DAILY,
		WEEKLY,
		MONTHLY,
		YEARLY
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(exchangeId)
			.append(lastModified)
			.append(summary)
			.append(description)
			.append(start)
			.append(end)
			.append(location)
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
		AppointmentDto other = (AppointmentDto) obj;
		return new EqualsBuilder()
			.append(exchangeId, other.exchangeId)
			.append(lastModified, other.lastModified)
			.append(summary, other.summary)
			.append(start, other.start)
			.append(end, other.end)
			.append(location, other.location)
			.isEquals();
	}
}
