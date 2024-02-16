package com.doubleclue.dcem.as.logic;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
public class LoginQrCodeContent implements Serializable {
	
	String operatorId;
	String sessionId;
	String nonce;
	
	public LoginQrCodeContent() {
	}
	
	@JsonIgnore
	public String getKey () {
		return operatorId + "\t" + sessionId;
	}
	
	public LoginQrCodeContent(String operatorId, String sessionId, String nonce) {
		super();
		this.operatorId = operatorId;
		this.sessionId = sessionId;
		this.nonce = nonce;
	}
	
	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getNonce() {
		return nonce;
	}

	public void setNonce(String nonce) {
		this.nonce = nonce;
	}
	
}
