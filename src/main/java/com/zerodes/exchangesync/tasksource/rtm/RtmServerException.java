package com.zerodes.exchangesync.tasksource.rtm;

public class RtmServerException extends Exception {
	private int rtmErrorCode;
	private String rtmMessage;
	
	public RtmServerException(final int rtmErrorCode, final String rtmMessage) {
		super("Error " + String.valueOf(rtmErrorCode) + ": " + rtmMessage);
		this.rtmErrorCode = rtmErrorCode;
		this.rtmMessage = rtmMessage;
	}

	public String getRtmMessage() {
		return rtmMessage;
	}

	public int getRtmErrorCode() {
		return rtmErrorCode;
	}
}
