package com.zerodes.exchangesync.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class NoteDto {
	private String title;
	private String body;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(final String body) {
		this.body = body;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(title)
			.append(body)
			.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		NoteDto other = (NoteDto) obj;
		return new EqualsBuilder()
			.append(title, other.title)
			.append(body, other.body)
			.isEquals();
	}
}
