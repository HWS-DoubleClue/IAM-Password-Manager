package com.doubleclue.dcem.oauth.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("serial")
@Entity
@Table(name = "oauth_token")
@NamedQueries({ 
	//	@NamedQuery(name = OAuthTokenEntity.GET_TOKEN_BY_CLIENT_AND_USER, query = "SELECT t FROM OAuthTokenEntity t WHERE t.id.clientId = ?1 AND t.id.userId = ?2"),
		@NamedQuery(name = OAuthTokenEntity.GET_TOKEN_BY_ACCESS_TOKEN, query = "SELECT t FROM OAuthTokenEntity t WHERE t.accessToken = ?1"),
		@NamedQuery(name = OAuthTokenEntity.GET_TOKEN_BY_REFRESH_TOKEN, query = "SELECT t FROM OAuthTokenEntity t WHERE t.refreshToken = ?1") })
public class OAuthTokenEntity implements Serializable {

//	public static final String GET_TOKEN_BY_ID = "oauthToken.getById";
//	public static final String GET_TOKEN_BY_CLIENT_AND_USER = "oauthToken.getByClientUser";
	public static final String GET_TOKEN_BY_ACCESS_TOKEN = "oauthToken.getByAccessToken";
	public static final String GET_TOKEN_BY_REFRESH_TOKEN = "oauthToken.getByRefreshToken";

	@EmbeddedId
	private OAuthTokenId id;

	@Column(name = "last_authenticated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastAuthenticated;

	@Column(name = "access_token")
	private String accessToken;

	@Column(name = "at_expires_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date accessTokenExpiresOn;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Column(name = "rt_expires_on")
	@Temporal(TemporalType.TIMESTAMP)
	private Date refreshTokenExpiresOn;

	@Column(name = "scope")
	private String scope;

	@Column(name = "claims_request")
	private String claimsRequest;

	public OAuthTokenEntity() {
	}

	public OAuthTokenEntity(OAuthTokenId id) {
		this.id = id;
	}

	public OAuthTokenId getId() {
		return id;
	}

	public void setId(OAuthTokenId id) {
		this.id = id;
	}

	public Date getLastAuthenticated() {
		return lastAuthenticated;
	}

	public void setLastAuthenticated(Date lastAuthenticated) {
		this.lastAuthenticated = lastAuthenticated;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public void setAccessToken(String accessToken, int accessTokenLifetime) {
		this.accessToken = accessToken;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, accessTokenLifetime);
		this.accessTokenExpiresOn = calendar.getTime();
	}

	public Date getAccessTokenExpiresOn() {
		return accessTokenExpiresOn;
	}

	public void setAccessTokenExpiresOn(Date accessTokenExpiresOn) {
		this.accessTokenExpiresOn = accessTokenExpiresOn;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public void setRefreshToken(String refreshToken, int refreshTokenLifetime) {
		this.refreshToken = refreshToken;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND, refreshTokenLifetime);
		this.refreshTokenExpiresOn = calendar.getTime();
	}

	public Date getRefreshTokenExpiresOn() {
		return refreshTokenExpiresOn;
	}

	public void setRefreshTokenExpiresOn(Date refreshTokenExpiresOn) {
		this.refreshTokenExpiresOn = refreshTokenExpiresOn;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getClaimsRequest() {
		return claimsRequest;
	}

	public void setClaimsRequest(String claimsRequest) {
		this.claimsRequest = claimsRequest;
	}
}
