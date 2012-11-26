package com.zerodes.exchangesync.dto;

import java.util.Date;
import java.util.Set;

public class AppointmentDto {
	private String exchangeId;
	private Date lastModified;
	private String summary;
	private String description;
	private Date start;
	private Date end;
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
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
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
	public void setStart(Date start) {
		this.start = start;
	}
	public Date getStart() {
		return start;
	}
	public void setEnd(Date end) {
		this.end = end;
	}
	public Date getEnd() {
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
}
