package com.zerodes.exchangesync.dto;

public class PersonDto {
	private String name;
	private String email;
	private boolean optional;
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	public boolean isOptional() {
		return optional;
	}
}
