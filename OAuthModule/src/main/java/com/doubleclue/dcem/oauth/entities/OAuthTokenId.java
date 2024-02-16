package com.doubleclue.dcem.oauth.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
@SuppressWarnings("serial")
public class OAuthTokenId implements Serializable {

	@Transient
	public static final Integer EMPTY_USER = -1;

	@Column(name = "client_id")
	private Integer clientId;

	@Column(name = "user_id")
	private Integer userId;

	public OAuthTokenId() {
	}

	public OAuthTokenId(Integer clientId, Integer userId) {
		this.clientId = clientId;
		this.userId = userId;
	}

	public Integer getClientId() {
		return clientId;
	}

	public void setClientId(Integer clientId) {
		this.clientId = clientId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OAuthTokenId other = (OAuthTokenId) obj;
		if (clientId == null) {
			if (other.getClientId() != null)
				return false;
		} else if (!clientId.equals(other.clientId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
