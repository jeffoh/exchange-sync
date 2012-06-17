package com.elasticpath.exchangertmsync.tasksource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

public class TaskDto {
	private String rtmId;
	private String otherId;
	private String name;
	private Date dueDate;
	private Byte priority;
	
	public String getRtmId() {
		return rtmId;
	}

	public void setRtmId(String rtmId) {
		this.rtmId = rtmId;
	}

	public String getOtherId() {
		return otherId;
	}

	public void setOtherId(String otherId) {
		this.otherId = otherId;
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
