package com.zerodes.exchangesync.calendarsource.google;

import com.zerodes.exchangesync.dto.AppointmentDto;

public class GoogleAppointmentDto extends AppointmentDto {
	private String googleId;
	
	public String getGoogleId() {
		return googleId;
	}
	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}
}
