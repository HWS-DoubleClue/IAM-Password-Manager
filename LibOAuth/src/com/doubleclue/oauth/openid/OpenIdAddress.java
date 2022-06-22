package com.doubleclue.oauth.openid;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.openid.enums.OpenIdAddressClaim;
import com.doubleclue.oauth.utils.OAuthUtils;

public class OpenIdAddress {

	protected final Map<OpenIdAddressClaim, String> claimMap = new HashMap<>();

	public OpenIdAddress(String formattedAddress, String streetAddress, String locality, String region, String postcode, String country) {
		setClaim(OpenIdAddressClaim.FORMATTED, formattedAddress);
		setClaim(OpenIdAddressClaim.STREET_ADDRESS, streetAddress);
		setClaim(OpenIdAddressClaim.LOCALITY, locality);
		setClaim(OpenIdAddressClaim.REGION, region);
		setClaim(OpenIdAddressClaim.POSTCODE, postcode);
		setClaim(OpenIdAddressClaim.COUNTRY, country);
	}

	public OpenIdAddress(String json) {
		this(new JSONObject(json));
	}
	
	public OpenIdAddress(JSONObject obj) {
		setClaimsFromJsonObject(obj);
	}
	
	private void setClaimsFromJsonObject(JSONObject obj) {
		for (OpenIdAddressClaim claim : OpenIdAddressClaim.values()) {
			if (obj.has(claim.toString())) {
				setClaim(claim, obj.getString(claim.toString()));
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

	private void setClaim(OpenIdAddressClaim key, String value) {
		if (value != null && !value.isEmpty()) {
			claimMap.put(key, value);
		}
	}

	private String getClaim(OpenIdAddressClaim key) {
		return claimMap.get(key);
	}

	public String getFormattedAddress() {
		return getClaim(OpenIdAddressClaim.FORMATTED);
	}

	public String getStreetAddress() {
		return getClaim(OpenIdAddressClaim.STREET_ADDRESS);
	}

	public String getLocality() {
		return getClaim(OpenIdAddressClaim.LOCALITY);
	}

	public String getRegion() {
		return getClaim(OpenIdAddressClaim.REGION);
	}

	public String getPostcode() {
		return getClaim(OpenIdAddressClaim.POSTCODE);
	}

	public String getCountry() {
		return getClaim(OpenIdAddressClaim.COUNTRY);
	}
}
