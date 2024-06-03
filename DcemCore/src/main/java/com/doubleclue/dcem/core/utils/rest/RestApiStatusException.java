package com.doubleclue.dcem.core.utils.rest;

@SuppressWarnings("serial")
public class RestApiStatusException extends Exception {
	
	int responseCode;
	
	
	public RestApiStatusException(int responseCode, String message) {
		super(message);
		this.responseCode = responseCode;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	
}
