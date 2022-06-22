package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.doubleclue.dcem.core.logic.UrlTokenType;

@Entity
@Table(name = "core_url_token")
@NamedQueries({

		@NamedQuery(name = UrlTokenEntity.DELETE_USER, query = "DELETE FROM UrlTokenEntity WHERE objectIdentifier = ?1"),

})

public class UrlTokenEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DELETE_USER = "UrlTokenEntity.deleteUser";

	@Id
	private String urlToken;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = false)
	private Date expiryDate;

	@Column(nullable = true)
	String objectIdentifier;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private UrlTokenType urlTokenType;

	public String getUrlToken() {
		return urlToken;
	}

	public void setUrlToken(String urlToken) {
		this.urlToken = urlToken;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}

	public UrlTokenType getUrlTokenType() {
		return urlTokenType;
	}

	public void setUrlTokenType(UrlTokenType urlTokenType) {
		this.urlTokenType = urlTokenType;
	}

	public String getObjectIdentifier() {
		return objectIdentifier;
	}

	public void setObjectIdentifier(String objectIdentifier) {
		this.objectIdentifier = objectIdentifier;
	}
}
