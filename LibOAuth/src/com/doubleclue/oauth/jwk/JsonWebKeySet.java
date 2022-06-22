package com.doubleclue.oauth.jwk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.utils.OAuthUtils;

public class JsonWebKeySet {

	private static final String KEYS = "keys";
	private List<JsonWebKey> jwks = new ArrayList<>();

	public JsonWebKeySet() {
	}

	public JsonWebKeySet(List<JsonWebKey> jwks) {
		this.jwks.addAll(jwks);
	}

	public JsonWebKeySet(String json) {
		JSONObject obj = new JSONObject(json);
		JSONObject[] keys = (JSONObject[]) obj.get(KEYS);
		for (JSONObject key : keys) {
			jwks.add(new JsonWebKey(key));
		}
	}

	public void addJwk(JsonWebKey jwk) {
		jwks.add(jwk);
	}

	public String getJson() {
		Map<String, JsonWebKey[]> map = new HashMap<>();
		map.put(KEYS, jwks.toArray(new JsonWebKey[jwks.size()]));
		return OAuthUtils.getJsonFromMaps(new Map[] { map });
	}
}
