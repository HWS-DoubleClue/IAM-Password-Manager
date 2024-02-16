package com.doubleclue.dcem.oauth.servlets;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenId;
import com.doubleclue.dcem.oauth.logic.OAuthAuthCodeInfo;
import com.doubleclue.dcem.oauth.logic.OAuthLogic;
import com.doubleclue.dcem.oauth.logic.OAuthModule;
import com.doubleclue.dcem.oauth.logic.OAuthModuleConstants;
import com.doubleclue.dcem.oauth.preferences.OAuthPreferences;
import com.doubleclue.dcem.oauth.sso.gui.OAuthReturnView;
import com.doubleclue.dcem.oauth.sso.logic.OAuthSsoLogic;
import com.doubleclue.oauth.jwk.JsonWebKeySet;
import com.doubleclue.oauth.oauth2.OAuthAccessTokenResponse;
import com.doubleclue.oauth.oauth2.OAuthErrorResponse;
import com.doubleclue.oauth.oauth2.OAuthGrantAuthCodeRequest;
import com.doubleclue.oauth.oauth2.OAuthGrantRefreshTokenRequest;
import com.doubleclue.oauth.oauth2.OAuthGrantRequest;
import com.doubleclue.oauth.oauth2.OAuthGrantRopcRequest;
import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.OAuthServerMetadata;
import com.doubleclue.oauth.oauth2.enums.OAuthError;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenType;
import com.doubleclue.oauth.openid.OpenIdAccessTokenResponse;
import com.doubleclue.oauth.openid.OpenIdAuthenticationRequest;
import com.doubleclue.oauth.openid.OpenIdClaimsRequest;
import com.doubleclue.oauth.openid.OpenIdConfiguration;
import com.doubleclue.oauth.openid.OpenIdUser;
import com.doubleclue.oauth.openid.enums.OpenIdDisplay;
import com.doubleclue.oauth.openid.enums.OpenIdScope;
import com.doubleclue.oauth.utils.JwtUtils;
import com.doubleclue.oauth.utils.OAuthConstants;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;

@SuppressWarnings("serial")
@SessionScoped
public class OAuthServlet extends HttpServlet {

	@Inject
	OAuthLogic oauthLogic;

	@Inject
	OAuthSsoLogic ssoLogic;

	// @Inject
	// OAuthLoginView authLoginView;

	@Inject
	OAuthModule oauthModule;

	@Inject
	OAuthReturnView returnView;

	@Inject
	DcemApplicationBean applicationBean;

	private static final Logger logger = LogManager.getLogger(OAuthServlet.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		TenantEntity tenantEntity = applicationBean.getTenantFromRequest(request);
		TenantIdResolver.setCurrentTenant(tenantEntity);
		try {
			String path = request.getRequestURI().substring(request.getContextPath().length());
			if (path.endsWith(OAuthConstants.URI_AUTH_SERVER_METADATA)) {
				sendAuthServerMetadata(response);
			} else if (path.endsWith(OAuthConstants.URI_OPENID_CONFIGURATION)) {
				sendOpenIdConfiguration(response);
			} else if (path.endsWith(OAuthModuleConstants.URI_USER_INFO)) {
				sendUserInfo(true, request, response);
			} else if (path.endsWith(OAuthModuleConstants.URI_JWKS)) {
				sendJwks(request, response);
			} else {
				OpenIdAuthenticationRequest authnRequest = new OpenIdAuthenticationRequest(request);
				String requestParameter = authnRequest.getRequestParameter();
				if (!isNullOrEmpty(requestParameter)) {
					OAuthClientEntity client = oauthLogic.getClientMetadata(authnRequest.getClientId());
					if (client != null) {
						authnRequest = new OpenIdAuthenticationRequest(JwtUtils.getJwtFromString(requestParameter, client.getClientSecret()));
					}
				}
				OAuthErrorResponse error = oauthLogic.validateAuthnRequest(authnRequest, !isNullOrEmpty(ssoLogic.getUserLoginId()));
				if (error == null) {
					respondAuthRequest(request.getSession(), authnRequest, response);
				} else {
					sendError(response, error, authnRequest);
				}
			}
		} catch (Exception e) {
			sendError(response, OAuthError.SERVER_ERROR, e.getMessage(), request.getParameter(OAuthParam.STATE.toString()));
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		TenantEntity tenantEntity = applicationBean.getTenantFromRequest(request);
		TenantIdResolver.setCurrentTenant(tenantEntity);
		try {
			String path = request.getRequestURI().substring(request.getContextPath().length());
			if (path.endsWith(OAuthModuleConstants.URI_USER_INFO)) {
				sendUserInfo(false, request, response);
			} else {
				OAuthGrantRequest grantRequest = new OAuthGrantRequest(request);
				OAuthErrorResponse error = oauthLogic.validateGrantRequest(grantRequest);
				if (error == null) {
					respondGrant(grantRequest, response);
				} else {
					sendError(response, error);
				}
			}
		} catch (Exception e) {
			sendError(response, OAuthError.SERVER_ERROR, e.getMessage(), request.getParameter(OAuthParam.STATE.toString()));
		}
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private void sendAuthServerMetadata(HttpServletResponse resp) throws IOException {
		OAuthServerMetadata config = oauthLogic.getServerMetadata();
		respondWithJson(config.getJson(), resp);
	}

	private void sendOpenIdConfiguration(HttpServletResponse resp) throws IOException {
		OpenIdConfiguration metadata = oauthLogic.getOpenIdConfiguration();
		respondWithJson(metadata.getJson(), resp);
	}

	private void sendUserInfo(boolean isGet, HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String accessToken = null;
		String bearerToken = req.getHeader("Authorization");
		if (!isNullOrEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
			accessToken = bearerToken.substring(7);
		} else {
			if (!isGet) {
				accessToken = req.getParameter(OAuthParam.ACCESS_TOKEN.toString());
			} else {
				sendError(resp, OAuthError.INVALID_TOKEN, "Bearer Token not found", null);
				return;
			}
		}
		if (!isNullOrEmpty(accessToken)) {
			OAuthTokenEntity entity = oauthLogic.validateAccessToken(accessToken);
			if (entity != null) {
				OAuthClientEntity client = oauthLogic.getClientMetadata(entity.getId().getClientId());
				OpenIdUser user = oauthLogic.getOpenIdUser(entity.getId().getUserId(), client.getIdpSettings());
				OpenIdScope[] scopes = oauthLogic.getScopes(entity);
				OpenIdClaimsRequest claimsRequest = oauthLogic.getUserInfoClaimsRequest(entity);
				String json;
				if (scopes == null && claimsRequest == null) {
					json = user.getJson();
				} else {
					json = user.getJson();
//					json = user.getJson(scopes, claimsRequest);
				}
				if (client.getIdpSettings().isTraceRequests()) {
					logger.info("OpenID - Sending UserInfo response:\n" + json);
				}
				respondWithJson(json, resp);
			} else {
				sendError(resp, OAuthError.INVALID_TOKEN, "Access Token expired or invalid", null);
			}
		} else {
			sendError(resp, OAuthError.INVALID_TOKEN, "Access Token not found", null);
		}
	}

	private void sendJwks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		respondWithJson(new JsonWebKeySet().getJson(), resp);
	}

	private void respondWithJson(String json, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/json;charset=UTF-8");
		resp.getWriter().write(json);
	}

	private void respondAuthRequest(HttpSession session, OpenIdAuthenticationRequest authnRequest, HttpServletResponse resp) throws IOException {
		String userLoginId = ssoLogic.getUserLoginId(); // check if session is still going
		if (oauthLogic.shouldAuthenticate(authnRequest, userLoginId, ssoLogic.getDcemUser())) {
			redirectToLoginPage(authnRequest, resp);
		} else {
			redirectToClient(session, authnRequest, resp, userLoginId);
		}
	}

	private void redirectToClient(HttpSession session, OpenIdAuthenticationRequest authnRequest, HttpServletResponse resp, String username)
			throws IOException {
		try {
			ssoLogic.setAuthnRequest(authnRequest);
			ssoLogic.setupResponse(true);
			OAuthRequest authnResponse = ssoLogic.getAuthnResponse();
			session.setMaxInactiveInterval(oauthModule.getModulePreferences().getAuthCodeLifetime());
			resp.sendRedirect(ssoLogic.getRedirectUrl(authnRequest, authnResponse));
		} catch (DcemException e) {
			sendError(resp, OAuthError.SERVER_ERROR, e.getMessage(), authnRequest.getState(), authnRequest);
		}
	}

	private void redirectToLoginPage(OpenIdAuthenticationRequest authnRequest, HttpServletResponse resp) throws IOException {
		logger.trace("SAML - Proceeding to login screen.");
		ssoLogic.setAuthnRequest(authnRequest);
		String path = OAuthModuleConstants.PATH_JSF_PAGES + "/" + DcemConstants.HTML_PAGE_PRE_LOGIN;
		OpenIdDisplay display = authnRequest.getDisplay();
		if (display != null && display == OpenIdDisplay.POPUP) {
			String script = "<script type=\"text/javascript\">window.onload = function() { window.open(\"" + path
					+ "\", \"\", \"width=800,height=600\"); }</script>";
			resp.getWriter().write(script);
		} else {
			resp.sendRedirect(path);
		}
	}

	private void respondGrant(OAuthGrantRequest request, HttpServletResponse resp) throws IOException {
		switch (request.getGrantType()) {
		case AUTH_CODE:
			respondAuthCodeGrant(request, resp);
			break;
		case PASSWORD:
			respondRopcGrant(request, resp);
			break;
		case CLIENT_CREDENTIALS:
			respondClientCredentialsGrant(request, resp);
			break;
		case REFRESH_TOKEN:
			respondRefreshTokenGrant(request, resp);
			break;
		default:
			sendError(resp, OAuthError.UNSUPPORTED_GRANT_TYPE, "Unsupported Grant type", request.getState());
			break;
		}
	}

	@SuppressWarnings("rawtypes")
	private void respondAuthCodeGrant(OAuthGrantRequest request, HttpServletResponse resp) throws IOException {

		OAuthGrantAuthCodeRequest grantRequest = new OAuthGrantAuthCodeRequest(request);
		String authCode = grantRequest.getAuthCode();

		OAuthError error = null;
		String errorMessage = null;

		OpenIdAuthenticationRequest authnRequest = null;
		OAuthTokenId tokenId = null;

		OAuthAuthCodeInfo authCodeInfo = oauthLogic.validateAuthCode(authCode);
		if (authCodeInfo == null) { // search for Auth Code in memory
			error = OAuthError.INVALID_GRANT;
			errorMessage = "Invalid Authorisation Code";
		} else {
			tokenId = authCodeInfo.getTokenId();
			authnRequest = oauthLogic.getAuthnRequest(tokenId); // search for original auth request
			if (authnRequest == null) {
				error = OAuthError.UNAUTHORISED_CLIENT;
				errorMessage = "Initial Authorisation Code request not found";
			} else if (authCodeInfo.isUsed()) {
				oauthLogic.invalidateAccessTokenByAuthCode(authCode); // cater for repeat attacks
				error = OAuthError.INVALID_GRANT;
				errorMessage = "Authorisation Code already used";
			}
		}

		String redirectUri = null;
		if (error == null) { // check Redirect URI
			redirectUri = grantRequest.getRedirectUri();
			if (isNullOrEmpty(redirectUri) || !authnRequest.getRedirectUri().equals(redirectUri)) {
				error = OAuthError.INVALID_GRANT;
				errorMessage = "Redirect URI is empty or does not match initial request";
			}
		}

		OAuthClientEntity client = null;
		if (error == null) { // check client ID from auth request
			client = oauthLogic.getClientMetadata(tokenId.getClientId());
			if (client == null) {
				error = OAuthError.INVALID_CLIENT;
				errorMessage = "Invalid Client ID in Authentication Request";
			} else {
				if (client.getIdpSettings().isTraceRequests()) {
					logger.info("OAuth - Received Auth Code request:\n" + request.getJson());
				}
			}
		}

		if (error == null) { // check client ID from grant request
			String clientId = grantRequest.getClientId();

			if (isNullOrEmpty(clientId)) {
				String assertionType = grantRequest.getClientAssertionType();
				if (isNullOrEmpty(assertionType) || !assertionType.equals(JwtUtils.JWT_ASSERTION_TYPE)) {
					error = OAuthError.INVALID_REQUEST;
					errorMessage = "Client Assertion Type is missing or invalid";
				}

				String jwtString = null;
				if (error == null) {
					jwtString = grantRequest.getClientAssertion();
					if (isNullOrEmpty(jwtString)) {
						error = OAuthError.INVALID_REQUEST;
						errorMessage = "Client Assertion is missing";
					}
				}

				if (error == null) {
					try {
						Jwt jwt = JwtUtils.getJwtFromString(jwtString, client.getClientSecret());
						Claims claims = ((Claims) jwt.getBody());
						clientId = claims.getSubject();
						if (claims.getExpiration().before(new Date())) {
							error = OAuthError.INVALID_REQUEST;
							errorMessage = "JWT is expired";
						} else if (!isNullOrEmpty(authCodeInfo.getJti()) && authCodeInfo.getJti().equals(claims.getId())) {
							error = OAuthError.INVALID_REQUEST;
							errorMessage = "JWT has the same JTI as a previously sent one";
						} else if (!claims.getAudience().contains(oauthModule.getModulePreferences().getIssuer() + OAuthModuleConstants.URI_ENDPOINT)) {
							error = OAuthError.INVALID_REQUEST;
							errorMessage = "JWT audience does not match this OP";
						}
					} catch (Exception e) {
						error = OAuthError.INVALID_REQUEST;
						errorMessage = e.getMessage();
					}
				}
			}

			if (error == null) {
				if (isNullOrEmpty(clientId)) {
					error = OAuthError.INVALID_CLIENT;
					errorMessage = "Client ID is missing";
				} else if (!client.getClientId().equals(clientId)) {
					error = OAuthError.INVALID_CLIENT;
					errorMessage = "Client ID does not match owner of the Authorisation Code";
				}
			}
		}

		if (error == null) { // check Code Challenge
			if (!oauthLogic.validateCodeChallenge(authnRequest, grantRequest)) {
				error = OAuthError.INVALID_GRANT;
				errorMessage = "Invalid code verifier or the original request had a bad code challenge";
			}
		}

		if (error == null) {
			OAuthTokenEntity tokenEntity = oauthLogic.getTokenEntity(tokenId);
			String accessToken = tokenEntity.getAccessToken();
			String refreshToken = tokenEntity.getRefreshToken();
			boolean isOpenId = oauthLogic.arrayContainsValue(authnRequest.getOpenIdScopes(), OpenIdScope.OPENID);
			OAuthPreferences preferences = oauthModule.getModulePreferences();
			OAuthRequest response;
			if (isOpenId) {
				String idToken = oauthLogic.createIdToken(authnRequest, tokenEntity, tokenId, authCode, ssoLogic.getUserLoginId());
				response = new OpenIdAccessTokenResponse(accessToken, preferences.getAccessTokenLifetime(), refreshToken, idToken);
			} else {
				response = new OAuthAccessTokenResponse(accessToken, OAuthTokenType.BEARER, preferences.getAccessTokenLifetime(), refreshToken);
			}
			respondWithJson(response.getJson(), resp);
			oauthLogic.invalidateAuthCode(authCode);

		} else {
			sendError(resp, error, errorMessage, request.getState());
		}
	}

	private void respondRopcGrant(OAuthGrantRequest request, HttpServletResponse resp) throws IOException {
		OAuthGrantRopcRequest ropcRequest = new OAuthGrantRopcRequest(request);
		String username = ropcRequest.getUsername();
		DcemUser user = oauthLogic.validateUser(username, ropcRequest.getPassword());
		if (user != null) {
			String clientId = ropcRequest.getClientId();
			OAuthClientEntity client = oauthLogic.validateClient(clientId, ropcRequest.getClientSecret());
			if (client != null) {
				if (client.getIdpSettings().isTraceRequests()) {
					logger.info("OAuth - Received ROPC request:\n" + request.getJson());
				}
				OAuthTokenEntity entity = oauthLogic.addUpdateTokenEntity(new OAuthTokenId(client.getId(), user.getId()), LocalDateTime.now(), true, true, null,
						null);
				OAuthRequest response = new OAuthAccessTokenResponse(entity.getAccessToken(), OAuthTokenType.BEARER,
						oauthModule.getModulePreferences().getAccessTokenLifetime(), entity.getRefreshToken());
				respondWithJson(response.getJson(), resp);
			} else {
				sendError(resp, OAuthError.INVALID_CLIENT, "Client ID not found", request.getState());
			}
		} else {
			sendError(resp, OAuthError.INVALID_GRANT, "User not found", request.getState());
		}
	}

	private void respondClientCredentialsGrant(OAuthGrantRequest request, HttpServletResponse resp) throws IOException {
		String clientId = request.getClientId();
		OAuthClientEntity client = oauthLogic.validateClient(clientId, request.getClientSecret());
		if (client != null) {
			if (client.getIdpSettings().isTraceRequests()) {
				logger.info("OAuth - Received Client Credentials request:\n" + request.getJson());
			}
			OAuthTokenEntity entity = oauthLogic.addUpdateTokenEntity(new OAuthTokenId(client.getId(), OAuthTokenId.EMPTY_USER), LocalDateTime.now(), true, false,
					null, null);
			OAuthRequest response = new OAuthAccessTokenResponse(entity.getAccessToken(), OAuthTokenType.BEARER,
					oauthModule.getModulePreferences().getAccessTokenLifetime());
			respondWithJson(response.getJson(), resp);
		} else {
			sendError(resp, OAuthError.INVALID_CLIENT, "Client ID not found", request.getState());
		}
	}

	private void respondRefreshTokenGrant(OAuthGrantRequest request, HttpServletResponse resp) throws IOException {
		OAuthGrantRefreshTokenRequest rtRequest = new OAuthGrantRefreshTokenRequest(request);
		OAuthTokenId id = oauthLogic.validateRefreshToken(rtRequest.getRefreshToken());
		if (id != null) {
			OAuthTokenEntity entity = oauthLogic.addUpdateTokenEntity(id, null, true, false, null, null);
			OAuthRequest response = new OAuthAccessTokenResponse(entity.getAccessToken(), OAuthTokenType.BEARER,
					oauthModule.getModulePreferences().getAccessTokenLifetime(), null);
			respondWithJson(response.getJson(), resp);
		} else {
			sendError(resp, OAuthError.INVALID_GRANT, "Invalid Refresh token", request.getState());
		}
	}

	private void sendError(HttpServletResponse resp, OAuthError error, String description, String state) throws IOException {
		sendError(resp, new OAuthErrorResponse(error, description, null, state));
	}

	private void sendError(HttpServletResponse resp, OAuthErrorResponse error) throws IOException {
		resp.setStatus(error.getError().getHttpStatusCode());
		respondWithJson(error.getJson(), resp);
	}

	private void sendError(HttpServletResponse resp, OAuthError error, String description, String state, OpenIdAuthenticationRequest authnRequest)
			throws IOException {
		sendError(resp, new OAuthErrorResponse(error, description, null, state), authnRequest);
	}

	private void sendError(HttpServletResponse resp, OAuthErrorResponse error, OpenIdAuthenticationRequest authnRequest) throws IOException {
		if (!isNullOrEmpty(authnRequest.getRedirectUri())) {
			resp.sendRedirect(ssoLogic.getRedirectUrl(authnRequest, error));
		} else {
			sendError(resp, error); // we should actually redirect to an error page
		}
	}
}