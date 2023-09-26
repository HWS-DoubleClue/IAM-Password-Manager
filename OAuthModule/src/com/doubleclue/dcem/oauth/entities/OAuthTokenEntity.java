package com.doubleclue.dcem.oauth.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

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
	private LocalDateTime lastAuthenticated;

	@Column(name = "access_token")
	private String accessToken;

	@Column(name = "at_expires_on")
	private LocalDateTime accessTokenExpiresOn;

	@Column(name = "refresh_token")
	private String refreshToken;

	@Column(name = "rt_expires_on")
	private LocalDateTime refreshTokenExpiresOn;

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

	public LocalDateTime getLastAuthenticated() {
		return lastAuthenticated;
	}

	public void setLastAuthenticated(LocalDateTime lastAuthenticated) {
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
		this.accessTokenExpiresOn = LocalDateTime.now().plusSeconds(accessTokenLifetime);
	}

	public LocalDateTime getAccessTokenExpiresOn() {
		return accessTokenExpiresOn;
	}

	public void setAccessTokenExpiresOn(LocalDateTime accessTokenExpiresOn) {
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
		this.refreshTokenExpiresOn = LocalDateTime.now().plusSeconds(refreshTokenLifetime);
	}

	public LocalDateTime getRefreshTokenExpiresOn() {
		return refreshTokenExpiresOn;
	}

	public void setRefreshTokenExpiresOn(LocalDateTime refreshTokenExpiresOn) {
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
