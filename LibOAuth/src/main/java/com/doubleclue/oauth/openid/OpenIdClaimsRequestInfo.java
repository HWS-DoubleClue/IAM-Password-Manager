package com.doubleclue.oauth.openid;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.openid.enums.OpenIdClaimRequestInfoEnum;
import com.doubleclue.oauth.utils.OAuthUtils;

public class OpenIdClaimsRequestInfo {

	protected final Map<OpenIdClaimRequestInfoEnum, Object> paramMap = new HashMap<>();

	public OpenIdClaimsRequestInfo(boolean essential) {
		setParam(OpenIdClaimRequestInfoEnum.ESSENTIAL, essential);
	}

	public OpenIdClaimsRequestInfo(boolean essential, String value) {
		this(essential);
		setParam(OpenIdClaimRequestInfoEnum.VALUE, value);
	}

	public OpenIdClaimsRequestInfo(boolean essential, String[] values) {
		this(essential);
		setParam(OpenIdClaimRequestInfoEnum.VALUES, values);
	}

	public OpenIdClaimsRequestInfo(String json) {
		this(new JSONObject(json));
	}

	public OpenIdClaimsRequestInfo(JSONObject obj) {
		setParamsFromJsonObject(obj);
	}

	private void setParamsFromJsonObject(JSONObject obj) {
		for (OpenIdClaimRequestInfoEnum param : OpenIdClaimRequestInfoEnum.values()) {
			if (obj.has(param.toString())) {
				setParam(param, obj.get(param.toString()));
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

	private void setParam(OpenIdClaimRequestInfoEnum key, Object value) {
		if (value != null) {
			paramMap.put(key, key.getExpectedClass().cast(value));
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getParam(OpenIdClaimRequestInfoEnum key) {
		return (T) paramMap.get(key);
	}

	public boolean isEssential() {
		Boolean essential = getParam(OpenIdClaimRequestInfoEnum.ESSENTIAL);
		return essential != null ? essential : false;
	}

	public String getValue() {
		return getParam(OpenIdClaimRequestInfoEnum.VALUE);
	}

	public String[] getValues() {
		return getParam(OpenIdClaimRequestInfoEnum.VALUES);
	}
}
