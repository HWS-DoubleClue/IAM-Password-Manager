package com.doubleclue.dcem.oauth.logic;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.oauth.openid.enums.OpenIdClaim;

@SuppressWarnings("serial")
public class OAuthIdpSettings implements Serializable {

	private boolean traceRequests = false;
	private List<ClaimAttribute> claims = new LinkedList<ClaimAttribute>(
			Arrays.asList(new ClaimAttribute(OpenIdClaim.SUBJECT.getValue(), AttributeTypeEnum.LOGIN_ID, null),
					new ClaimAttribute(OpenIdClaim.FULL_NAME.getValue(), AttributeTypeEnum.DISPLAY_NAME, null),
					new ClaimAttribute(OpenIdClaim.PREFERRED_USERNAME.getValue(), AttributeTypeEnum.ACCOUNT_NAME, null),
					new ClaimAttribute(OpenIdClaim.PHONE_NUMBER.getValue(), AttributeTypeEnum.TELEPHONE, null),
					new ClaimAttribute(OpenIdClaim.EMAIL.getValue(), AttributeTypeEnum.EMAIL, null),
					new ClaimAttribute(OpenIdClaim.LOCALE.getValue(), AttributeTypeEnum.LOCALE, null)));

	public OAuthIdpSettings() {
	}

	public boolean isTraceRequests() {
		return traceRequests;
	}

	public void setTraceRequests(boolean traceRequests) {
		this.traceRequests = traceRequests;
	}

	public List<ClaimAttribute> getClaims() {
		return claims;
	}

	public void setClaims(List<ClaimAttribute> claims) {
		this.claims = claims;
	}
}
