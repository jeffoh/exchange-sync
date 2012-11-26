package com.zerodes.exchangesync.calendarsource;

import java.util.Collection;

import com.zerodes.exchangesync.dto.AppointmentDto;

public interface CalendarSource {
	Collection<AppointmentDto> getAllAppointments();
	
	void addAppointment(AppointmentDto appointment);
	
	void updateAppointment(AppointmentDto appointment);
	
	void deleteAppointment(AppointmentDto appointment);
}
