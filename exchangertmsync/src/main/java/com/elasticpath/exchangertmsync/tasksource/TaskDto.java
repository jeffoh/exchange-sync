package com.elasticpath.exchangertmsync.tasksource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

public class TaskDto {
	private String rtmTaskId;
	private String rtmTimeSeriesId;
	private String exchangeId;
	private String name;
	private Date dueDate;
	private Byte priority;
	private boolean completed;
	
	public String getRtmTaskId() {
		return rtmTaskId;
	}

	public void setRtmTaskId(String rtmTaslId) {
		this.rtmTaskId = rtmTaslId;
	}

	public String getRtmTimeSeriesId() {
		return rtmTimeSeriesId;
	}

	public void setRtmTimeSeriesId(String rtmTimeSeriesId) {
		this.rtmTimeSeriesId = rtmTimeSeriesId;
	}

	public String getExchangeId() {
		return exchangeId;
	}

	public void setExchangeId(String exchangeId) {
		this.exchangeId = exchangeId;
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
	
	/**
	 * @return the completed
	 */
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * @param completed the completed to set
	 */
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

	public String getSmartAdd() {
		List<String> components = new ArrayList<String>();
		components.add(name);
		if (dueDate != null) {
			components.add("^" + DateFormatUtils.format(dueDate, "yyyy-MM-dd"));
		}
		if (priority != null) {
			components.add("!" + String.valueOf(priority));
		}
		return StringUtils.join(components, " ");
	}
}
