package com.doubleclue.dcem.oauth.logic;

import java.util.HashMap;
import java.util.Map;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenId;
import com.doubleclue.oauth.openid.OpenIdAuthenticationRequest;
import com.hazelcast.core.IMap;

public class OAuthTenantData extends ModuleTenantData {

	private final Map<String, OAuthClientEntity> metadataMap = new HashMap<>();
	private IMap<String, OAuthAuthCodeInfo> authCodes;
	private IMap<OAuthTokenId, OpenIdAuthenticationRequest> authnRequests;

	public Map<String, OAuthClientEntity> getMetadataMap() {
		return metadataMap;
	}

	public IMap<String, OAuthAuthCodeInfo> getAuthCodes() {
		return authCodes;
	}

	public void setAuthCodes(IMap<String, OAuthAuthCodeInfo> authCodes) {
		this.authCodes = authCodes;
	}

	public IMap<OAuthTokenId, OpenIdAuthenticationRequest> getAuthnRequests() {
		return authnRequests;
	}

	public void setAuthnRequests(IMap<OAuthTokenId, OpenIdAuthenticationRequest> authnRequests) {
		this.authnRequests = authnRequests;
	}
}
