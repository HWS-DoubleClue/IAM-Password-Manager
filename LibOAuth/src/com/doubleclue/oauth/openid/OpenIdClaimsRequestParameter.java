package com.doubleclue.oauth.openid;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.openid.enums.OpenIdClaimEndpoint;
import com.doubleclue.oauth.utils.OAuthUtils;

public class OpenIdClaimsRequestParameter {

	protected final Map<OpenIdClaimEndpoint, OpenIdClaimsRequest> paramMap = new HashMap<>();

	public OpenIdClaimsRequestParameter(OpenIdClaimsRequest userInfo, OpenIdClaimsRequest idToken) {
		setParam(OpenIdClaimEndpoint.USER_INFO, userInfo);
		setParam(OpenIdClaimEndpoint.ID_TOKEN, idToken);
	}

	public OpenIdClaimsRequestParameter(String json) {
		this(new JSONObject(json));
	}

	public OpenIdClaimsRequestParameter(JSONObject obj) {
		setParamsFromJsonObject(obj);
	}

	private void setParamsFromJsonObject(JSONObject obj) {
		for (OpenIdClaimEndpoint param : OpenIdClaimEndpoint.values()) {
			if (obj.has(param.toString())) {
				setParam(param, new OpenIdClaimsRequest(obj.getJSONObject(param.toString())));
			}
		}
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { paramMap });
	}

	@Override
	public String toString() {
		return getJson();
	}

	private void setParam(OpenIdClaimEndpoint key, OpenIdClaimsRequest value) {
		if (value != null) {
			paramMap.put(key, value);
		}
	}

	private OpenIdClaimsRequest getParam(OpenIdClaimEndpoint key) {
		return paramMap.get(key);
	}

	public OpenIdClaimsRequest getUserInfo() {
		return getParam(OpenIdClaimEndpoint.USER_INFO);
	}

	public OpenIdClaimsRequest getIdToken() {
		return getParam(OpenIdClaimEndpoint.ID_TOKEN);
	}
}
