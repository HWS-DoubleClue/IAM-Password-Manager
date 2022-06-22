package com.doubleclue.dcem.as.logic;

import java.io.Serializable;

@SuppressWarnings("serial")
public class LoginQrCode implements Serializable {
	
	String nonce;
	int timeout;
	String userName;
	String deviceName;
	
	public LoginQrCode() {
	}
	
	public LoginQrCode(String nonce, int timeout) {
		super();
		this.nonce = nonce;
		this.timeout = timeout;
	}

	
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public String getNonce() {
		return nonce;
	}
	public void setNonce(String nonce) {
		this.nonce = nonce;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Override
	public String toString() {
		return "LoginQrCode [nonce=" + nonce + ", timeout=" + timeout + ", userName=" + userName + ", deviceName=" + deviceName + "]";
	}
	

}
