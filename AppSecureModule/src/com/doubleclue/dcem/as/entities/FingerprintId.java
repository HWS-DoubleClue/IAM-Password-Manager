package com.doubleclue.dcem.as.entities;

import java.io.Serializable;

import javax.persistence.Embeddable;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;

@Embeddable
public class FingerprintId implements Serializable {

	private static final long serialVersionUID = 1L;

	Integer userId;
	Integer policyAppId;

	public FingerprintId() {
		super();
	}

	public FingerprintId(Integer userId, PolicyAppEntity appEntity) {
		super();
		this.userId = userId;
		this.policyAppId = 0;
		if (appEntity.getAuthApplication().isShareSession() == false) {
			policyAppId = appEntity.getId();
		}
	}

	public FingerprintId(Integer userId, Integer policyAppId) {
		super();
		this.userId = userId;
		this.policyAppId = policyAppId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getPolicyAppId() {
		return policyAppId;
	}

	public void setPolicyAppId(Integer policyAppId) {
		this.policyAppId = policyAppId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((policyAppId == null) ? 0 : policyAppId.hashCode());
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
		FingerprintId other = (FingerprintId) obj;
		if (policyAppId == null) {
			if (other.policyAppId != null)
				return false;
		} else if (!policyAppId.equals(other.policyAppId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
}
