package com.doubleclue.dcem.core.logic;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class AzureAdConfig implements Serializable {

	public String accessToken;
	public String refreshToken;
	public Date accessTokenExpiresOn;

	public AzureAdConfig() {
	}

	public AzureAdConfig(String accessToken, String refreshToken, Date accessTokenExpiresOn) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.accessTokenExpiresOn = accessTokenExpiresOn;
	}



	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Date getAccessTokenExpiresOn() {
		return accessTokenExpiresOn;
	}

	public void setAccessTokenExpiresOn(Date accessTokenExpiresOn) {
		this.accessTokenExpiresOn = accessTokenExpiresOn;
	}
}
