package com.zerodes.exchangesync.calendarsource;

import java.util.Collection;

import com.zerodes.exchangesync.dto.AppointmentDto;

public interface CalendarSource {
	Collection<AppointmentDto> getAllAppointments() throws Exception;
	
	void addAppointment(AppointmentDto appointment) throws Exception;
	
	void updateAppointment(AppointmentDto appointment) throws Exception;
	
	void deleteAppointment(AppointmentDto appointment) throws Exception;
}
