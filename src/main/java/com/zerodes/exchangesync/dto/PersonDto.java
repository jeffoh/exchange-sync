package com.zerodes.exchangesync.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

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
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(name)
			.append(email)
			.append(optional)
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
		PersonDto other = (PersonDto) obj;
		return new EqualsBuilder()
			.append(name, other.name)
			.append(email, other.email)
			.append(optional, other.optional)
			.isEquals();
	}
}
