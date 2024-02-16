package com.doubleclue.dcem.oauth.logic;

import java.io.Serializable;

import com.doubleclue.dcem.oauth.entities.OAuthTokenId;

@SuppressWarnings("serial")
public class OAuthAuthCodeInfo implements Serializable {

	private final OAuthTokenId tokenId;
	private boolean used = false;
	private String jti = null;

	public OAuthAuthCodeInfo(OAuthTokenId tokenId) {
		this.tokenId = tokenId;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}

	public OAuthTokenId getTokenId() {
		return tokenId;
	}

	public String getJti() {
		return jti;
	}

	public void setJti(String jti) {
		this.jti = jti;
	}
}
