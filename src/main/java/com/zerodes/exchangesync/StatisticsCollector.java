package com.zerodes.exchangesync;

public class StatisticsCollector {
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
		System.out.println(String.format("Appointments added/updated/deleted: %d/%d/%d",
				appointmentsAdded, appointmentsUpdated, appointmentsDeleted));
		System.out.println(String.format("Tasks added/updated/deleted: %d/%d/%d",
				tasksAdded, tasksUpdated, tasksDeleted));
	}
}
