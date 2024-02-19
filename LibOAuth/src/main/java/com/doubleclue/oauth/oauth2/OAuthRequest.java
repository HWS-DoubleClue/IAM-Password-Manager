package com.doubleclue.oauth.oauth2;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.utils.OAuthUtils;

@SuppressWarnings("serial")
public class OAuthRequest implements Serializable {

	protected final Map<OAuthParam, Object> paramMap = new HashMap<>();
	protected final Map<String, Object> customParamMap = new HashMap<>();

	protected OAuthRequest() {
	}

	public OAuthRequest(HttpServletRequest request) {
		Map<String, String[]> paramMap = request.getParameterMap();
		for (String key : paramMap.keySet()) {
			String value = request.getParameter(key);
			OAuthParam param = OAuthParam.fromString(key);
			if (param != null) {
				Class<?> expectedClass = param.getExpectedClass();
				if (expectedClass.equals(Integer.class)) {
					setParam(param, Integer.parseInt(value));
				} else {
					setParam(param, value);
				}
			} else {
				setCustomParam(key, value);
			}
		}
	}

	public OAuthRequest(Map<OAuthParam, Object> paramMap) {
		this.paramMap.putAll(paramMap);
	}

	public OAuthRequest(String json) {
		JSONObject obj = new JSONObject(json);
		for (String key : obj.keySet()) {
			Object value = obj.get(key);
			OAuthParam param = OAuthParam.fromString(key);
			if (param != null) {
				setParam(param, value);
			} else {
				setCustomParam(key, value);
			}
		}
	}

	public Map<OAuthParam, Object> getParamMap() {
		return paramMap;
	}

	public Map<String, Object> getCustomParamMap() {
		return customParamMap;
	}

	protected static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public void setParam(OAuthParam key, Object value) {
		if (value != null) {
			Class<?> expectedClass = key.getExpectedClass();
			Class<?> valueClass = value.getClass();
			if (expectedClass.equals(valueClass)) {
				paramMap.put(key, value);
			} else if (expectedClass.equals(Integer.class) && valueClass.equals(String.class)) {
				paramMap.put(key, Integer.parseInt((String) value));
			} else {
				paramMap.put(key, key.getExpectedClass().cast(value));
			}
		}
	}

	public void removeParam(OAuthParam key) {
		paramMap.remove(key);
	}

	public boolean hasParam(OAuthParam param) {
		return paramMap.containsKey(param);
	}

	public boolean hasParam(String param) {
		OAuthParam oauthParam = OAuthParam.fromString(param);
		if (param != null) {
			return paramMap.containsKey(oauthParam);
		} else {
			return customParamMap.containsKey(param);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getParam(OAuthParam key) {
		return (T) paramMap.get(key);
	}

	public void setCustomParam(String key, Object value) {
		if (!isNullOrEmpty(key) && value != null) {
			customParamMap.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getCustomParam(String key) {
		return (T) customParamMap.get(key);
	}

	public String getQueryString() {
		return OAuthUtils.getQueryStringFromMaps(new Map[] { paramMap, customParamMap });
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { paramMap, customParamMap });
	}

	protected String getSsvFromArray(String[] strings) {
		if (strings != null) {
			return String.join(" ", strings);
		}
		return null;
	}

	protected String[] getArrayFromSsv(String s) {
		return isNullOrEmpty(s) ? new String[0] : s.split(" ");
	}
}
