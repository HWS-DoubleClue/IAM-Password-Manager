package com.doubleclue.oauth.openid;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.doubleclue.oauth.oauth2.OAuthAuthorisationRequest;
import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;
import com.doubleclue.oauth.openid.enums.OpenIdClaim;
import com.doubleclue.oauth.openid.enums.OpenIdClaimEndpoint;
import com.doubleclue.oauth.openid.enums.OpenIdDisplay;
import com.doubleclue.oauth.openid.enums.OpenIdPrompt;
import com.doubleclue.oauth.openid.enums.OpenIdScope;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;

@SuppressWarnings("serial")
public class OpenIdAuthenticationRequest extends OAuthAuthorisationRequest {

	public OpenIdAuthenticationRequest() {
	}

	public OpenIdAuthenticationRequest(HttpServletRequest request) {
		super(request);
	}

	public OpenIdAuthenticationRequest(OAuthRequest request) {
		super(request);
	}

	public OpenIdAuthenticationRequest(String json) {
		super(json);
	}

	@SuppressWarnings("rawtypes")
	public OpenIdAuthenticationRequest(Jwt jwt) {
		if (jwt != null) {
			Claims claims = (Claims) jwt.getBody();
			for (String key : claims.keySet()) {
				Object value = claims.get(key);
				OAuthParam param = OAuthParam.fromString(key);
				if (param != null) {
					setParam(param, value);
				} else {
					setCustomParam(key, value);
				}
			}
		}
	}

	public OpenIdAuthenticationRequest(OAuthResponseType[] responseTypes, String clientId, String redirectUri, OpenIdScope[] scopes, String state,
			OAuthResponseMode responseMode, String nonce, OpenIdDisplay display, OpenIdPrompt[] prompts, Integer maxAge, Locale[] uiLocales,
			String idTokenHint, String loginHint, String[] acrValues, OpenIdClaimsRequestParameter claims) {

		super(responseTypes, clientId, redirectUri, state, responseMode);

		String[] scopeStrings = new String[scopes.length];
		for (int i = 0; i < scopes.length; i++) {
			scopeStrings[i] = scopes[i].toString();
		}
		setParam(OAuthParam.SCOPE, getSsvFromArray(scopeStrings));

		String promptsString = "";
		for (OpenIdPrompt prompt : prompts) {
			promptsString += (promptsString.isEmpty() ? "" : " ") + prompt.toString();
		}

		String[] localeStrings = new String[uiLocales.length];
		for (int i = 0; i < uiLocales.length; i++) {
			localeStrings[i] = uiLocales[i].getLanguage();
		}

		setParam(OAuthParam.PROMPT, promptsString);
		setParam(OAuthParam.NONCE, nonce);
		setParam(OAuthParam.DISPLAY, display.toString());
		setParam(OAuthParam.UI_LOCALES, getSsvFromArray(localeStrings));
		setParam(OAuthParam.MAX_AGE, maxAge);
		setParam(OAuthParam.ID_TOKEN_HINT, idTokenHint);
		setParam(OAuthParam.LOGIN_HINT, loginHint);
		setParam(OAuthParam.ACR_VALUES, getSsvFromArray(acrValues));
		setParam(OAuthParam.CLAIMS, claims.getJson());
	}

	public OpenIdScope[] getOpenIdScopes() {
		String[] scopes = getScopes();
		List<OpenIdScope> oidScopes = new ArrayList<>();
		for (String scope : scopes) {
			OpenIdScope oidScope = OpenIdScope.fromString(scope);
			if (oidScope != null) {
				oidScopes.add(oidScope);
			}
		}
		return oidScopes.toArray(new OpenIdScope[oidScopes.size()]);
	}

	public String getNonce() {
		return getParam(OAuthParam.NONCE);
	}

	public OpenIdDisplay getDisplay() {
		return OpenIdDisplay.fromString(getParam(OAuthParam.DISPLAY));
	}

	public OpenIdPrompt[] getPrompts() {
		String[] promptStrings = getArrayFromSsv(getParam(OAuthParam.PROMPT));
		OpenIdPrompt[] prompts = new OpenIdPrompt[promptStrings.length];
		for (int i = 0; i < prompts.length; i++) {
			prompts[i] = OpenIdPrompt.fromString(promptStrings[i]);
		}
		return prompts;
	}

	public Integer getMaxAge() {
		return getParam(OAuthParam.MAX_AGE);
	}

	public String getIdTokenHint() {
		return getParam(OAuthParam.ID_TOKEN_HINT);
	}

	public String getLoginHint() {
		return getParam(OAuthParam.LOGIN_HINT);
	}

	public String[] getAcrValues() {
		return getArrayFromSsv(getParam(OAuthParam.ACR_VALUES));
	}

	public String getRequestParameter() {
		return getParam(OAuthParam.REQUEST);
	}

	public String getRequestUri() {
		return getParam(OAuthParam.REQUEST_URI);
	}

	public Locale[] getUiLocales() {
		String[] localeStrings = getArrayFromSsv(getParam(OAuthParam.UI_LOCALES));
		Locale[] locales = new Locale[localeStrings.length];
		for (int i = 0; i < localeStrings.length; i++) {
			locales[i] = new Locale(localeStrings[i]);
		}
		return locales;
	}

	public OpenIdClaimsRequestParameter getClaimRequestParameter() {
		String json = getParam(OAuthParam.CLAIMS);
		return json != null ? new OpenIdClaimsRequestParameter(json) : null;
	}

	public OpenIdClaim[] getEssentialClaims(OpenIdClaimEndpoint endpoint) {
		List<OpenIdClaim> essentialClaims = new ArrayList<>();
		OpenIdClaimsRequestParameter crp = getClaimRequestParameter();
		if (crp != null) {
			OpenIdClaimsRequest endpointInfo = null;
			switch (endpoint) {
			case ID_TOKEN:
				endpointInfo = crp.getIdToken();
				break;
			case USER_INFO:
				endpointInfo = crp.getUserInfo();
				break;
			}
			if (endpointInfo != null) {
				Map<OpenIdClaim, OpenIdClaimsRequestInfo> claimMap = endpointInfo.getClaimMap();
				if (claimMap != null) {
					for (OpenIdClaim claim : claimMap.keySet()) {
						OpenIdClaimsRequestInfo info = claimMap.get(claim);
						if (info != null && info.isEssential()) {
							essentialClaims.add(claim);
						}
					}
				}
			}
		}
		return essentialClaims.toArray(new OpenIdClaim[essentialClaims.size()]);
	}
}
