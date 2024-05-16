package com.doubleclue.dcem.core.utils.rest;

@SuppressWarnings("serial")
public class RestApiStatusException extends Exception {
	
	int resposneCode;
	
	
	public RestApiStatusException(int resposneCode, String message) {
		super(message);
		this.resposneCode = resposneCode;
	}

	public int getResposneCode() {
		return resposneCode;
	}

	public void setResposneCode(int resposneCode) {
		this.resposneCode = resposneCode;
	}
	
}
