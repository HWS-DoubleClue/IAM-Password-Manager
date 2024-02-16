package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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

	@Column(nullable = false)
	private LocalDateTime expiryDate;

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

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
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
