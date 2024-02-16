package com.doubleclue.dcem.core.gui;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class UserAccount {

	@JsonProperty("a")
	private String userLoginId;
	
	@JsonProperty("b")
	private int lastLogin;

	@JsonProperty("c")
	private int id;
	
	@JsonProperty("d")
	private String sessionCookie;
	
	@JsonProperty("e")
	private int sessionExpiresOn;

	public UserAccount() {

	}

	

	public UserAccount(String userLoginId, int id, String sessionCookie, int sessionExpiresOn) {
		super();
		this.userLoginId = userLoginId;
		this.lastLogin = (int) (System.currentTimeMillis() /1000);
		this.id = id;
		this.sessionCookie = sessionCookie;
		this.sessionExpiresOn = sessionExpiresOn;
	}



	public int getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(int lastLogin) {
		this.lastLogin = lastLogin;
	}

	public String getUserLoginId() {
		return userLoginId;
	}
	
	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}


	public int getSessionExpiresOn() {
		return sessionExpiresOn;
	}

	public void setSessionExpiresOn(int sessionExpiresOn) {
		this.sessionExpiresOn = sessionExpiresOn;
	}

	@Override
	@JsonIgnore
	public String toString() {
		return "UserAccount [userLoginId=" + userLoginId + ", lastLogin=" + lastLogin + ",  id=" + id + ", sessionId="
				+ sessionCookie + ", sessionExpiresOn=" + sessionExpiresOn + "]";
	}

	public String getSessionCookie() {
		return sessionCookie;
	}

	public void setSessionCookie(String sessionCookie) {
		this.sessionCookie = sessionCookie;
	}

}
