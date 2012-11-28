package com.zerodes.exchangesync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsCollector {
	private static final Logger LOG = LoggerFactory.getLogger(StatisticsCollector.class);
	
	private int appointmentsAdded = 0;
	private int appointmentsUpdated = 0;
	private int appointmentsDeleted = 0;
	private int tasksAdded = 0;
	private int tasksUpdated = 0;
	private int tasksDeleted = 0;
	
	public void appointmentAdded() {
		appointmentsAdded++;
	}
	
	public void appointmentUpdated() {
		appointmentsUpdated++;
	}
	
	public void appointmentDeleted() {
		appointmentsDeleted++;
	}

	public void taskAdded() {
		tasksAdded++;
	}

	public void taskUpdated() {
		tasksUpdated++;
	}
	
	public void taskDeleted() {
		tasksDeleted++;
	}

	public void display() {
		LOG.info(String.format("Appointments added/updated/deleted: %d/%d/%d",
				appointmentsAdded, appointmentsUpdated, appointmentsDeleted));
		LOG.info(String.format("Tasks added/updated/deleted: %d/%d/%d",
				tasksAdded, tasksUpdated, tasksDeleted));
	}
}
