package com.doubleclue.dcem.oauth.sso.logic;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.ErrorDisplayBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenId;
import com.doubleclue.dcem.oauth.logic.OAuthLogic;
import com.doubleclue.dcem.oauth.logic.OAuthModule;
import com.doubleclue.dcem.oauth.logic.OAuthModuleConstants;
import com.doubleclue.dcem.oauth.sso.gui.OAuthReturnView;
import com.doubleclue.oauth.oauth2.OAuthAccessTokenResponse;
import com.doubleclue.oauth.oauth2.OAuthAuthCodeResponse;
import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenType;
import com.doubleclue.oauth.openid.OpenIdAuthenticationRequest;
import com.doubleclue.oauth.openid.OpenIdClaimsRequest;
import com.doubleclue.oauth.openid.OpenIdIdTokenResponse;
import com.doubleclue.oauth.openid.enums.OpenIdDisplay;

@SuppressWarnings("serial")
@SessionScoped
@Named("oauthSsoLogic")
public class OAuthSsoLogic implements Serializable {

	private static final Logger logger = LogManager.getLogger(OAuthSsoLogic.class);

	private OpenIdAuthenticationRequest authnRequest;
	private OAuthRequest authnResponse;

	private OAuthClientEntity metadata;
	private DbResourceBundle dbResourceBundle;

	@Inject
	OAuthModule oauthModule;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	OAuthLogic oauthLogic;

	@Inject
	OAuthReturnView returnView;

	@Inject
	ErrorDisplayBean errorDisplayBean;

	DcemUser dcemUser;
	String userLoginId;

	public OpenIdAuthenticationRequest getAuthnRequest() {
		return authnRequest;
	}

	public void setAuthnRequest(OpenIdAuthenticationRequest authnRequest) {
		this.authnRequest = authnRequest;
		this.metadata = oauthLogic.getClientMetadata(authnRequest.getClientId());
		if (metadata != null && metadata.getIdpSettings().isTraceRequests()) {
			logger.info("OpenID - Received request: " + authnRequest.getJson());
		}
	}

	public OAuthRequest getAuthnResponse() {
		return authnResponse;
	}

	public DbResourceBundle getDbResourceBundle() {
		if (dbResourceBundle == null) {
			try {
				dbResourceBundle = DbResourceBundle.getDbResourceBundle(JsfUtils.getLocale());
			} catch (Exception e) {
				logger.error("OAuth - could not get db resource bundle: " + e.getMessage());
			}
		}
		return dbResourceBundle;
	}

	public OAuthClientEntity getMetadata() {
		return metadata;
	}

	public void displayErrorCode(DcemException dcemException, String username) {
		DcemErrorCodes errorCode = dcemException.getErrorCode();
		if (errorCode == DcemErrorCodes.UNEXPECTED_ERROR) {
			Throwable cause = dcemException.getCause();
			if (cause != null && cause.getClass() == DcemException.class) {
				displayErrorCode((DcemException) cause, username);
			} else {
				logger.info("OAuth - unknown error for user: " + username, dcemException);
				JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, getDbResourceBundle(), "sso.error.unknown", null, null);
			}
		} else {
			logger.info("OAuth SSO Error: " + errorCode, dcemException);
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, getDbResourceBundle(), "sso.error.dcem." + errorCode, null, null);
		}
	}

	public void redirectToPage(String url, boolean killSession) {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			if (killSession) {
				HttpSession session = (HttpSession) ec.getSession(false);
				if (session != null) {
					// session.setMaxInactiveInterval(oauthModule.getModulePreferences().getAuthCodeLifetime());
				}
			}
			OpenIdDisplay display = authnRequest.getDisplay();
			if (display != null && display == OpenIdDisplay.POPUP) {
				PrimeFaces.current().executeScript("window.opener.document.location = \"" + url + "\"; self.close();");
			} else {
				System.out.println("OAuthSsoLogic.redirectToPage() " + url);
				ec.redirect(url);
			}
		} catch (IOException e) {
			logger.info("OAuth - could not redirect to " + url, e);
		}
	}

	public void setupResponse(DcemUser dcemUser, String userLoginId, boolean authSkipped) throws IOException, DcemException {
		this.dcemUser = dcemUser;
		this.userLoginId = userLoginId;
		setupResponse(authSkipped);
	}

	public void setupResponse(boolean authSkipped) throws IOException, DcemException {

		if (authnRequest != null) {
			String clientId = authnRequest.getClientId();
			String state = authnRequest.getState();

			OAuthRequest response;
			Map<OAuthParam, Object> paramMap = new HashMap<>();
			OAuthResponseType[] responseTypes = authnRequest.getResponseTypes();
			String authCode = null;

			/* OAuthTokenId id = null;
			if (!(responseTypes.length == 1 && responseTypes[0] == OAuthResponseType.AUTH_CODE)) {
				id = oauthLogic.getTokenId(clientId, username);
			}
			*/

			OAuthTokenId id = oauthLogic.getTokenId(clientId, dcemUser);
			OpenIdClaimsRequest userInfoClaimsRequest = authnRequest.getClaimRequestParameter() != null
					? authnRequest.getClaimRequestParameter().getUserInfo()
					: null;
			OAuthTokenEntity tokenEntity = oauthLogic.addUpdateTokenEntity(id, authSkipped ? null : LocalDateTime.now(), true, true, authnRequest.getOpenIdScopes(),
					userInfoClaimsRequest);

			boolean addIdToken = false;
			for (OAuthResponseType responseType : responseTypes) {
				switch (responseType) {
				case AUTH_CODE:
					authCode = oauthLogic.createAuthorisationCode(dcemUser, clientId);
					response = new OAuthAuthCodeResponse(authCode, state);
					paramMap.putAll(response.getParamMap());
					break;
				case TOKEN:
					response = new OAuthAccessTokenResponse(tokenEntity.getAccessToken(), OAuthTokenType.BEARER,
							oauthModule.getModulePreferences().getAccessTokenLifetime(), null, state);
					paramMap.putAll(response.getParamMap());
					break;
				case ID_TOKEN:
					addIdToken = true;
					break;
				}
			}

			if (addIdToken) {
				String idToken = oauthLogic.createIdToken(authnRequest, tokenEntity, id, authCode, userLoginId);
				response = new OpenIdIdTokenResponse(idToken, state);
				paramMap.putAll(response.getParamMap());
			}

			if (!paramMap.isEmpty()) {
				oauthLogic.setAuthnRequest(dcemUser, clientId, authnRequest);
				authnResponse = new OAuthRequest(paramMap);
				if (metadata.getIdpSettings().isTraceRequests()) {
					logger.info("OpenID - Sending response: " + authnResponse.getJson());
				}
			} else {
				authnResponse = null;
			}
		}
	}

	public void redirectToClient() {
		if (authnRequest != null && authnResponse != null) {
			String path = getRedirectUrl(authnRequest, authnResponse);
			if (path.startsWith(OAuthModuleConstants.PATH_JSF_PAGES)) {
				path = path.substring(OAuthModuleConstants.PATH_JSF_PAGES.length() + 1);
			}
			redirectToPage(path, true);
		}
	}

	public String getSessionError() {
		if (errorDisplayBean.isErrorOn() == true && errorDisplayBean.getMessage().equals(DcemConstants.EXPIRED_PAGE)) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_INFO, getDbResourceBundle(), "sso.error.expired", null, null);
			errorDisplayBean.setErrorOn(false);
		}
		return null;
	}

	public String getRedirectUrl(OpenIdAuthenticationRequest authnRequest, OAuthRequest authnResponse) {
		String path;
		String redirectUri = authnRequest.getRedirectUri();
		OAuthResponseMode responseMode = oauthLogic.getResponseMode(authnRequest);
		if (responseMode == OAuthResponseMode.FORM_POST) {
			returnView.setResponse(authnResponse, redirectUri);
			path = OAuthModuleConstants.PATH_JSF_PAGES + "/" + OAuthModuleConstants.JSF_PAGE_RETURN;
		} else {
			String separator = responseMode == OAuthResponseMode.QUERY ? "?" : "#";
			if (redirectUri.contains(separator)) {
				separator = "&";
			}
			path = redirectUri + separator + authnResponse.getQueryString();
		}
		return path;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}
}
