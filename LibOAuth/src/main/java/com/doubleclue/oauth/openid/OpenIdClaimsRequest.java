package com.doubleclue.oauth.openid;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.openid.enums.OpenIdClaim;
import com.doubleclue.oauth.utils.OAuthUtils;

public class OpenIdClaimsRequest {

	protected final Map<OpenIdClaim, OpenIdClaimsRequestInfo> claimMap = new HashMap<>();
	protected final Map<String, OpenIdClaimsRequestInfo> customClaimMap = new HashMap<>();

	public OpenIdClaimsRequest() {
	}

	public OpenIdClaimsRequest(String json) {
		this(new JSONObject(json));
	}

	public OpenIdClaimsRequest(JSONObject obj) {
		setClaimsFromJsonObject(obj);
	}

	private void setClaimsFromJsonObject(JSONObject obj) {
		for (String key : obj.keySet()) {
			JSONObject infoObj;
			try {
				infoObj = obj.getJSONObject(key);
			} catch (Exception e) {
				infoObj = null;
			}
			OpenIdClaimsRequestInfo value = infoObj == null ? null : new OpenIdClaimsRequestInfo(infoObj);
			OpenIdClaim claim = OpenIdClaim.fromString(key);
			if (claim == null) {
				customClaimMap.put(key, value);
			} else {
				claimMap.put(claim, value);
			}
		}
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { claimMap });
	}

	@Override
	public String toString() {
		return getJson();
	}

	public void setClaim(OpenIdClaim key, OpenIdClaimsRequestInfo value) {
		if (value != null) {
			claimMap.put(key, value);
		}
	}

	public OpenIdClaimsRequestInfo getClaimsRequestInfo(OpenIdClaim key) {
		return claimMap.get(key);
	}

	public void setCustomClaim(String key, OpenIdClaimsRequestInfo value) {
		if (key != null && key.isEmpty() == false && value != null) {
			customClaimMap.put(key, value);
		}
	}

	public OpenIdClaimsRequestInfo getCustomClaimsRequestInfo(String key) {
		return customClaimMap.get(key);
	}

	public Map<OpenIdClaim, OpenIdClaimsRequestInfo> getClaimMap() {
		return claimMap;
	}

	public Map<String, OpenIdClaimsRequestInfo> getCustomClaimMap() {
		return customClaimMap;
	}
}
