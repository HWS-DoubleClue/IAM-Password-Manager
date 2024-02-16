package com.doubleclue.dcem.oauth.logic;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenId;
import com.doubleclue.dcem.oauth.preferences.OAuthPreferences;
import com.doubleclue.dcem.oauth.tasks.UpdateClientMetadataCacheTask;
import com.doubleclue.oauth.oauth2.OAuthAuthorisationRequest;
import com.doubleclue.oauth.oauth2.OAuthErrorResponse;
import com.doubleclue.oauth.oauth2.OAuthGrantAuthCodeRequest;
import com.doubleclue.oauth.oauth2.OAuthGrantRequest;
import com.doubleclue.oauth.oauth2.OAuthServerMetadata;
import com.doubleclue.oauth.oauth2.enums.OAuthCodeChallengeMethod;
import com.doubleclue.oauth.oauth2.enums.OAuthError;
import com.doubleclue.oauth.oauth2.enums.OAuthGrantType;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;
import com.doubleclue.oauth.oauth2.enums.OAuthTokenAuthMethod;
import com.doubleclue.oauth.openid.OpenIdAuthenticationRequest;
import com.doubleclue.oauth.openid.OpenIdClaimsRequest;
import com.doubleclue.oauth.openid.OpenIdClaimsRequestInfo;
import com.doubleclue.oauth.openid.OpenIdConfiguration;
import com.doubleclue.oauth.openid.OpenIdUser;
import com.doubleclue.oauth.openid.enums.OpenIdClaim;
import com.doubleclue.oauth.openid.enums.OpenIdPrompt;
import com.doubleclue.oauth.openid.enums.OpenIdScope;
import com.google.common.hash.Hashing;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;

import io.jsonwebtoken.SignatureAlgorithm;

@ApplicationScoped
@Named("oauthLogic")
public class OAuthLogic {

	@Inject
	OAuthModule oauthModule;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	private static final Logger logger = LogManager.getLogger(OAuthLogic.class);
	private static final SecureRandom random = new SecureRandom();
	private static HashMap<String, ClaimAttribute> defaultClaimProperties = null;

	// private DcemUser getUser(String username) {
	// if (!isNullOrEmpty(username)) {
	// try {
	// return userLogic.getDistinctUser(username);
	// } catch (DcemException e) {
	// logger.info("OAuth - could not find user " + username);
	// }
	// }
	// return null;
	// }

	private DcemUser getUser(int id) {
		try {
			return userLogic.getUser(id);
		} catch (Exception e) {
			logger.info("OAuth - could not find user " + id);
			return null;
		}
	}

	public OAuthClientEntity getClientMetadata(int id) {
		OAuthClientEntity entity = null;
		for (OAuthClientEntity e : getMetadataMap().values()) {
			if (e.getId() == id) {
				entity = e;
				break;
			}
		}
		if (entity == null) {
			try {
				TypedQuery<OAuthClientEntity> query = em.createNamedQuery(OAuthClientEntity.GET_CLIENT_BY_ID, OAuthClientEntity.class);
				query.setParameter(1, id);
				entity = query.getSingleResult();
				getMetadataMap().put(entity.getClientId(), entity);
			} catch (Exception e) {
				logger.debug("OAuth - could not find Client Metadata with id: " + id);
				entity = null;
			}
		}
		return entity;
	}

	public OAuthClientEntity getClientMetadata(String clientId) {
		OAuthClientEntity entity = getMetadataMap().get(clientId);
		if (entity == null) {
			try {
				TypedQuery<OAuthClientEntity> query = em.createNamedQuery(OAuthClientEntity.GET_CLIENT_BY_CLIENT_ID, OAuthClientEntity.class);
				query.setParameter(1, clientId);
				entity = query.getSingleResult();
				getMetadataMap().put(clientId, entity);
			} catch (Exception e) {
				logger.debug("OAuth - could not find Client Metadata with EntityID: " + clientId);
				entity = null;
			}
		}
		return entity;
	}

	public List<OAuthClientEntity> getAllClientMetadataEntities() {
		OAuthTenantData oauthTenantData = (OAuthTenantData) oauthModule.getModuleTenantData();
		try {
			TypedQuery<OAuthClientEntity> query = em.createNamedQuery(OAuthClientEntity.GET_ALL_CLIENTS, OAuthClientEntity.class);
			List<OAuthClientEntity> entities = query.getResultList();
			for (OAuthClientEntity entity : entities) {
				String clientId = entity.getClientId();
				if (!oauthTenantData.getMetadataMap().containsKey(clientId)) {
					oauthTenantData.getMetadataMap().put(clientId, entity);
				}
			}
			return entities;
		} catch (Exception e) {
			logger.error("OAuth - could not find SP Metadata Entities", e);
			return null;
		}
	}

	@DcemTransactional
	public void addUpdateClientMetadata(DcemAction dcemAction, OAuthClientEntity entity, boolean withAuditing) throws DcemException {
		try {
			String error = null;
			if (isNullOrEmpty(entity.getDisplayName())) {
				error = "error.emptyClientName";
			} else if (isNullOrEmpty(entity.getClientId())) {
				error = "error.emptyClientId";
			} else if (isNullOrEmpty(entity.getClientSecret())) {
				error = "error.emptyClientSecret";
			}
			if (error != null) {
				throw new DcemException(DcemErrorCodes.INVALID_OAUTH_CLIENT_METADATA, JsfUtils.getStringSafely(OAuthModule.RESOURCE_NAME, error));
			}
			if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
				em.persist(entity);
			} else {
				em.merge(entity);
			}
			if (withAuditing) {
				auditingLogic.addAudit(dcemAction, entity.toString());
			}
			IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
			executorService.executeOnAllMembers(new UpdateClientMetadataCacheTask(entity.getClientId(), TenantIdResolver.getCurrentTenantName()));
		} catch (DcemException e) {
			logger.debug("OAuth - validation error while adding new Client Metadata: " + e.toString() + "\nEntity: " + entity.toString());
			throw e;
		} catch (Exception e) {
			logger.debug("OAuth - unknown error while adding new Client Metadata: " + e.toString() + "\nEntity: " + entity.toString());
			throw new DcemException(DcemErrorCodes.INVALID_OAUTH_CLIENT_METADATA, e.toString(), e);
		}
	}

	public OAuthTokenEntity getTokenEntity(OAuthClientEntity client, DcemUser user) {
		return getTokenEntity(new OAuthTokenId(client.getId(), user.getId()));
	}

	public OAuthTokenEntity getTokenEntity(OAuthTokenId authTokenId) {
		return em.find(OAuthTokenEntity.class, authTokenId);
	}

	public OAuthTokenEntity getTokenEntityFromAccessToken(String token) {
		TypedQuery<OAuthTokenEntity> query = em.createNamedQuery(OAuthTokenEntity.GET_TOKEN_BY_ACCESS_TOKEN, OAuthTokenEntity.class);
		query.setParameter(1, token);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public OAuthTokenEntity getTokenEntityFromRefreshToken(String token) {
		TypedQuery<OAuthTokenEntity> query = em.createNamedQuery(OAuthTokenEntity.GET_TOKEN_BY_REFRESH_TOKEN, OAuthTokenEntity.class);
		query.setParameter(1, token);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@DcemTransactional
	public OAuthTokenEntity addUpdateTokenEntity(OAuthTokenId id, LocalDateTime authTime, boolean updateAccessToken, boolean updateRefreshToken, OpenIdScope[] scopes,
			OpenIdClaimsRequest userInfoCRP) {
		OAuthPreferences preferences = oauthModule.getModulePreferences();
		OAuthTokenEntity entity = getTokenEntity(id);
		boolean persist = entity == null;
		if (persist) {
			entity = new OAuthTokenEntity(id);
		}
		if (authTime != null) {
			entity.setLastAuthenticated(authTime);
		}
		if (updateAccessToken) {
			entity.setAccessToken(createAccessToken(), preferences.getAccessTokenLifetime());
		}
		if (updateRefreshToken) {
			entity.setRefreshToken(createRefreshToken(), preferences.getRefreshTokenLifetime());
		}
		if (scopes != null && scopes.length > 0) {
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (OpenIdScope scope : scopes) {
				if (!first)
					sb.append(" ");
				sb.append(scope.toString());
				first = false;
			}
			entity.setScope(sb.toString());
		}
		entity.setClaimsRequest(userInfoCRP != null ? userInfoCRP.getJson() : null);
		if (persist) {
			em.persist(entity);
		} else {
			em.merge(entity);
		}
		return entity;
	}

	public OpenIdScope[] getScopes(OAuthTokenEntity entity) {
		if (entity != null && !isNullOrEmpty(entity.getScope())) {
			String[] scopeStrings = entity.getScope().split(" ");
			OpenIdScope[] scopes = new OpenIdScope[scopeStrings.length];
			for (int i = 0; i < scopeStrings.length; i++) {
				scopes[i] = OpenIdScope.fromString(scopeStrings[i]);
			}
			return scopes;
		}
		return new OpenIdScope[0];
	}

	public OpenIdClaimsRequest getUserInfoClaimsRequest(OAuthTokenEntity entity) {
		if (entity != null && !isNullOrEmpty(entity.getClaimsRequest())) {
			return new OpenIdClaimsRequest(entity.getClaimsRequest());
		}
		return null;
	}

	public void invalidateMetadata(String clientId) {
		getMetadataMap().remove(clientId);
	}

	public OAuthClientEntity validateClient(String clientId, String clientSecret) {
		OAuthClientEntity client = getClientMetadata(clientId);
		if (client != null && client.getClientSecret().equals(clientSecret)) {
			return client;
		}
		return null;
	}

	public DcemUser validateUser(String loginId, String password) {
		try {
			DcemUser dcemUser = userLogic.getUser(loginId);
			userLogic.verifyUserPassword(dcemUser, password.getBytes(StandardCharsets.UTF_8));
			return dcemUser;
		} catch (DcemException e) {
			return null;
		}
	}

	public String createAuthorisationCode(DcemUser user, String clientId) {
		OAuthClientEntity client = getClientMetadata(clientId);
		if (user != null && client != null) {
			String code = createAuthorizationCode();
			OAuthAuthCodeInfo info = new OAuthAuthCodeInfo(new OAuthTokenId(client.getId(), user.getId()));
			getAuthorisationCodes().put(code, info, oauthModule.getModulePreferences().getAuthCodeLifetime(), TimeUnit.SECONDS);
			return code;
		}
		return null;
	}

	public String createIdToken(OpenIdAuthenticationRequest authnRequest, OAuthTokenEntity tokenEntity, OAuthTokenId oauthTokenId, String authCode,
			String userLoginId) {
		OpenIdClaimsRequest idTokenClaimsRequest = authnRequest.getClaimRequestParameter() != null ? authnRequest.getClaimRequestParameter().getIdToken()
				: new OpenIdClaimsRequest();
		String[] acrValues = authnRequest.getAcrValues();
		if (acrValues != null && acrValues.length > 0 && idTokenClaimsRequest.getClaimsRequestInfo(OpenIdClaim.ACR) == null) {
			idTokenClaimsRequest.setClaim(OpenIdClaim.ACR, new OpenIdClaimsRequestInfo(true, acrValues[0]));
		}
		DcemUser dcemUser = getUser(oauthTokenId.getUserId());
		OAuthClientEntity client = getClientMetadata(oauthTokenId.getClientId());
		if (dcemUser != null && client != null) {
			OAuthPreferences preferences = oauthModule.getModulePreferences();
			OpenIdUser oidUser = getOpenIdUser(dcemUser, client.getIdpSettings());
			return oidUser.getJwtString(authnRequest.getOpenIdScopes(), idTokenClaimsRequest, SignatureAlgorithm.HS256, client.getClientSecret(),
					preferences.getIssuer() + OAuthModuleConstants.URI_ENDPOINT, getRandomBase64String(10), preferences.getIdTokenLifetime() * 1000,
					client.getClientId(), authnRequest.getNonce(), tokenEntity.getAccessToken(), authCode, tokenEntity.getLastAuthenticated());
		}
		return null;
	}

	// private String createIdToken(DcemUser user, OAuthClientEntity client, OpenIdScope[] scopes, OpenIdClaimsRequest
	// claimsRequest, String nonce,
	// String accessToken, String authCode, Date authTime) {
	// OAuthPreferences preferences = oauthModule.getModulePreferences();
	// OpenIdUser oidUser = getOpenIdUser(user);
	// return oidUser.getJwtString(scopes, claimsRequest, null, SignatureAlgorithm.HS256, client.getClientSecret(),
	// preferences.getIssuer() + OAuthModuleConstants.URI_ENDPOINT, getRandomBase64String(10),
	// preferences.getIdTokenLifetime() * 1000,
	// client.getClientId(), nonce, accessToken, authCode, authTime);
	// }

	public OAuthAuthCodeInfo validateAuthCode(String code) {
		if (!isNullOrEmpty(code)) {
			return getAuthorisationCodes().get(code);
		}
		return null;
	}

	public void invalidateAuthCode(String code) {
		IMap<String, OAuthAuthCodeInfo> authCodes = getAuthorisationCodes();
		if (!isNullOrEmpty(code) && authCodes.containsKey(code)) {
			OAuthAuthCodeInfo info = authCodes.get(code);
			info.setUsed(true);
			authCodes.put(code, info);
		}
	}

	public OAuthTokenEntity validateAccessToken(String token) {
		if (!isNullOrEmpty(token)) {
			OAuthTokenEntity entity = getTokenEntityFromAccessToken(token);
			if (entity != null && entity.getAccessToken() != null && entity.getAccessTokenExpiresOn() != null
					&& entity.getAccessTokenExpiresOn().isAfter(LocalDateTime.now())) {
				return entity;
			}
		}
		return null;
	}

	public OAuthTokenId getTokenId(String clientId, DcemUser user) {
		OAuthClientEntity client = getClientMetadata(clientId);
		if (client != null) {
			int userId = user != null ? user.getId() : OAuthTokenId.EMPTY_USER;
			return new OAuthTokenId(client.getId(), userId);
		}
		return null;
	}

	@DcemTransactional
	public void invalidateAccessTokenByAuthCode(String authCode) {
		OAuthAuthCodeInfo info = getAuthorisationCodes().get(authCode);
		if (info != null) {
			OAuthTokenEntity entity = getTokenEntity(info.getTokenId());
			if (entity != null) {
				entity.setAccessToken(null);
				entity.setAccessTokenExpiresOn(null);
				em.merge(entity);
			}
		}
	}

	public OAuthTokenId validateRefreshToken(String token) {
		if (!isNullOrEmpty(token)) {
			OAuthTokenEntity entity = getTokenEntityFromRefreshToken(token);
			if (entity != null && entity.getRefreshToken() != null && entity.getRefreshTokenExpiresOn() != null
					&& entity.getRefreshTokenExpiresOn().isAfter(LocalDateTime.now())) {
				return entity.getId();
			}
		}
		return null;
	}

	public OAuthServerMetadata getServerMetadata() {
		String issuer = oauthModule.getModulePreferences().getIssuer();
		if (!isNullOrEmpty(issuer)) {
			String endpoint = issuer + OAuthModuleConstants.URI_ENDPOINT;
			OAuthServerMetadata metadata = new OAuthServerMetadata();
			metadata.setIssuer(endpoint);
			metadata.setTokenEndpoint(endpoint, new OAuthTokenAuthMethod[] { OAuthTokenAuthMethod.CLIENT_SECRET_BASIC, OAuthTokenAuthMethod.CLIENT_SECRET_POST,
					OAuthTokenAuthMethod.CLIENT_SECRET_JWT }, SignatureAlgorithm.values());
			metadata.setAuthEndpoint(endpoint);
			metadata.setSupportedCodeChallengeMethods(OAuthCodeChallengeMethod.values());
			metadata.setSupportedGrantTypes(OAuthGrantType.values());
			metadata.setSupportedScopes(new String[] { OpenIdScope.OPENID.toString(), OpenIdScope.PROFILE.toString(), OpenIdScope.ADDRESS.toString(),
					OpenIdScope.EMAIL.toString(), OpenIdScope.PHONE.toString() });
			metadata.setSupportedResponseModes(OAuthResponseMode.values());
			metadata.setSupportedUiLocales(new Locale[] { Locale.ENGLISH, Locale.GERMAN });
			metadata.setJwksUri(endpoint + OAuthModuleConstants.URI_JWKS);
			return metadata;
		} else {
			return null;
		}
	}

	public OpenIdConfiguration getOpenIdConfiguration() {
		OpenIdConfiguration config = new OpenIdConfiguration(getServerMetadata());
		config.setUserInfoEndpoint(config.getIssuer() + OAuthModuleConstants.URI_USER_INFO, SignatureAlgorithm.values(), null, null);
		config.setClaimConfig(null, new OpenIdClaim[] { OpenIdClaim.SUBJECT, OpenIdClaim.FULL_NAME, OpenIdClaim.EMAIL, OpenIdClaim.EMAIL_VERIFIED,
				OpenIdClaim.PHONE_NUMBER, OpenIdClaim.PHONE_NUMBER_VERIFIED, OpenIdClaim.LOCALE }, new Locale[] { Locale.ENGLISH }, true);
		config.setRequestConfig(true, false, false);
		return config;
	}

	public OpenIdUser getOpenIdUser(int userId, OAuthIdpSettings idpSettings) {
		return getOpenIdUser(getUser(userId), idpSettings);
	}

	public OpenIdUser getOpenIdUser(DcemUser user, OAuthIdpSettings idpSettings) {
		if (user != null) {
			OpenIdUser oidUser = new OpenIdUser();
			HashMap<String, ClaimAttribute> claimMap = getDefaultClaimProperties();
			for (ClaimAttribute claimAttribute : idpSettings.getClaims()) {
				claimMap.put(claimAttribute.getName(), claimAttribute);
			}
			List<ClaimAttribute> claimValues = authenticationLogic.getClaimAttributeValues(new ArrayList<>(claimMap.values()), user, null, null);
			for (ClaimAttribute claimAttribute : claimValues) {
				String value = claimAttribute.getValue();
				if (value != null) {
					OpenIdClaim oidClaim = OpenIdClaim.fromString(claimAttribute.getName());
					if (oidClaim != null) {
						oidUser.setClaim(oidClaim, value);
						switch (oidClaim) {
						case EMAIL:
							oidUser.setClaim(OpenIdClaim.EMAIL_VERIFIED, true);
							break;
						case PHONE_NUMBER:
							oidUser.setClaim(OpenIdClaim.PHONE_NUMBER_VERIFIED, true);
							break;
						default:
							break;
						}
					} else {
						oidUser.setCustomClaim(claimAttribute.getName(), value);
					}
				}
			}
			return oidUser;
		}
		return null;
	}

	private static HashMap<String, ClaimAttribute> getDefaultClaimProperties() {
		if (defaultClaimProperties == null) {
			defaultClaimProperties = new HashMap<>();
			defaultClaimProperties.put(OpenIdClaim.SUBJECT.getValue(), new ClaimAttribute(OpenIdClaim.SUBJECT.getValue(), AttributeTypeEnum.LOGIN_ID, null));
			defaultClaimProperties.put(OpenIdClaim.FULL_NAME.getValue(),
					new ClaimAttribute(OpenIdClaim.FULL_NAME.getValue(), AttributeTypeEnum.DISPLAY_NAME, null));
			defaultClaimProperties.put(OpenIdClaim.PREFERRED_USERNAME.getValue(),
					new ClaimAttribute(OpenIdClaim.PREFERRED_USERNAME.getValue(), AttributeTypeEnum.ACCOUNT_NAME, null));
			defaultClaimProperties.put(OpenIdClaim.PHONE_NUMBER.getValue(),
					new ClaimAttribute(OpenIdClaim.PHONE_NUMBER.getValue(), AttributeTypeEnum.TELEPHONE, null));
			defaultClaimProperties.put(OpenIdClaim.EMAIL.getValue(), new ClaimAttribute(OpenIdClaim.EMAIL.getValue(), AttributeTypeEnum.EMAIL, null));
			defaultClaimProperties.put(OpenIdClaim.LOCALE.getValue(), new ClaimAttribute(OpenIdClaim.LOCALE.getValue(), AttributeTypeEnum.LOCALE, null));
		}
		return defaultClaimProperties;
	}

	public String createClientId() {
		return java.util.UUID.randomUUID().toString();
	}

	public String createClientSecret() {
		return getRandomBase64String(32);
	}

	private String createAccessToken() {
		return getRandomBase64String(30);
	}

	private String createAuthorizationCode() {
		return getRandomBase64String(35);
	}

	private String createRefreshToken() {
		return getRandomBase64String(40);
	}

	private byte[] getRandomBytes(int n) {
		byte bytes[] = new byte[n];
		random.nextBytes(bytes);
		return bytes;
	}

	private String getRandomBase64String(int n) {
		return Base64.getEncoder().encodeToString(getRandomBytes(n));
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	private OAuthTenantData getTenantData() {
		return (OAuthTenantData) oauthModule.getModuleTenantData();
	}

	private Map<String, OAuthClientEntity> getMetadataMap() {
		return getTenantData().getMetadataMap();
	}

	private IMap<String, OAuthAuthCodeInfo> getAuthorisationCodes() {
		return getTenantData().getAuthCodes();
	}

	private IMap<OAuthTokenId, OpenIdAuthenticationRequest> getAuthnRequests() {
		return getTenantData().getAuthnRequests();
	}

	public OpenIdAuthenticationRequest getAuthnRequest(OAuthTokenId id) {
		return getAuthnRequests().get(id);
	}

	public void setAuthnRequest(DcemUser user, String clientId, OpenIdAuthenticationRequest request) {
		OAuthClientEntity client = getClientMetadata(clientId);
		OAuthTokenId id = new OAuthTokenId(client.getId(), user.getId());
		setAuthnRequest(id, request);
	}

	public void setAuthnRequest(OAuthTokenId id, OpenIdAuthenticationRequest request) {
		getAuthnRequests().put(id, request, oauthModule.getModulePreferences().getAuthCodeLifetime(), TimeUnit.SECONDS);
	}

	public OAuthResponseMode getResponseMode(OAuthAuthorisationRequest authnRequest) {
		if (authnRequest != null) {
			OAuthResponseMode responseMode = authnRequest.getResponseMode();
			if (responseMode == null) {
				boolean isHybridOrImplicit = false;
				for (OAuthResponseType responseType : authnRequest.getResponseTypes()) {
					if (responseType != OAuthResponseType.AUTH_CODE) {
						isHybridOrImplicit = true;
						break;
					}
				}
				responseMode = isHybridOrImplicit ? OAuthResponseMode.FRAGMENT : OAuthResponseMode.QUERY;
			}
			return responseMode;
		}
		return null;
	}

	public <T> boolean arrayContainsValue(final T[] array, final T value) {
		if (value == null) {
			for (final T e : array)
				if (e == null)
					return true;
		} else {
			for (final T e : array)
				if (e == value || value.equals(e))
					return true;
		}
		return false;
	}

	public OAuthErrorResponse validateAuthnRequest(OpenIdAuthenticationRequest request, boolean isLoggedIn) {

		OAuthError error = OAuthError.INVALID_REQUEST;
		String errorMessage = null;

		// check for a response type
		OAuthResponseType[] responseTypes = request.getResponseTypes();
		if (responseTypes == null || responseTypes.length == 0) {
			errorMessage = "Response Type(s) missing";
		}

		if (errorMessage == null) { // check prompts
			OpenIdPrompt[] prompts = request.getPrompts();
			if (prompts != null) {
				if (prompts.length == 1 && prompts[0] == OpenIdPrompt.NONE && !isLoggedIn) {
					error = OAuthError.LOGIN_REQUIRED;
					errorMessage = "Requested NONE prompt when a login is required";
				} else if (prompts.length > 1 && arrayContainsValue(prompts, OpenIdPrompt.NONE)) {
					error = OAuthError.INVALID_REQUEST;
					errorMessage = "Requested NONE prompt along with other prompts";
				}
			}
		}

		if (errorMessage == null) { // check nonce
			OpenIdScope[] scopes = request.getOpenIdScopes();
			boolean isOpenId = arrayContainsValue(scopes, OpenIdScope.OPENID);
			boolean isIdToken = arrayContainsValue(responseTypes, OAuthResponseType.ID_TOKEN);
			boolean isAuthCode = arrayContainsValue(responseTypes, OAuthResponseType.AUTH_CODE);
			if (isOpenId && (isIdToken || !isAuthCode) && isNullOrEmpty(request.getNonce())) {
				errorMessage = "Expected Nonce value";
			}
		}

		if (errorMessage == null) { // check Redirect URI (also fills in one if found)
			OAuthClientEntity client = getClientMetadata(request.getClientId());
			if (client == null) {
				errorMessage = "Unknown Client";
			} else {
				String redirectUri = request.getRedirectUri();
				String[] registeredRedirectUris = client.getRedirectUriArray();
				if (isNullOrEmpty(redirectUri)) {
					if (registeredRedirectUris.length == 0) {
						errorMessage = ("No Redirect URI found in the request, and none are registered");
					} else {
						request.setParam(OAuthParam.REDIRECT_URI, registeredRedirectUris[0]);
					}
				} else {
					if (registeredRedirectUris.length > 0 && !arrayContainsValue(registeredRedirectUris, redirectUri)) {
						request.removeParam(OAuthParam.REDIRECT_URI);
						errorMessage = ("The supplied Redirect URI does not match any that are registered");
					}
				}
			}
		}

		return errorMessage == null ? null : new OAuthErrorResponse(error, errorMessage, null, request.getState());
	}

	public OAuthErrorResponse validateGrantRequest(OAuthGrantRequest request) {
		OAuthError error = OAuthError.INVALID_REQUEST;
		String message = null;
		if (request.getGrantType() == null) {
			message = "Grant Type is missing";
		}
		return message == null ? null : new OAuthErrorResponse(error, message, null, null);
	}

	public boolean validateCodeChallenge(OAuthAuthorisationRequest authnRequest, OAuthGrantAuthCodeRequest acRequest) {
		String lastCodeChallenge = authnRequest.getCodeChallenge();
		if (lastCodeChallenge != null) {
			String codeVerifier = acRequest.getCodeVerifier();
			if (codeVerifier != null) {
				switch (authnRequest.getCodeChallengeMethod()) {
				case PLAIN:
					return lastCodeChallenge.equals(codeVerifier);
				case SHA_256:
					String sha256hex = Hashing.sha256().hashString(codeVerifier, StandardCharsets.US_ASCII).toString();
					String codeChallenge = Base64.getUrlEncoder().encodeToString(sha256hex.getBytes(StandardCharsets.US_ASCII));
					return lastCodeChallenge.equals(codeChallenge);
				default:
					return false;
				}
			} else {
				return false;
			}
		} else {
			return true;
		}
	}

	public boolean shouldAuthenticate(OpenIdAuthenticationRequest authnRequest, String username, DcemUser dcemUser) {
		boolean shouldAuthenticate = true;
		if (username != null) { // is logged in
			Integer maxAge = authnRequest.getMaxAge();
			if (maxAge != null) {
				OAuthTokenId tokenId = getTokenId(authnRequest.getClientId(), dcemUser);
				OAuthTokenEntity tokenEntity = getTokenEntity(tokenId);
				if (tokenEntity != null) {
					LocalDateTime lastAuth = tokenEntity.getLastAuthenticated();
					shouldAuthenticate = (((new Date().getTime() / 1000) - lastAuth.toEpochSecond(ZoneOffset.UTC))) > maxAge;
				}
			}

			OpenIdPrompt[] prompts = authnRequest.getPrompts();
			if (prompts != null && prompts.length > 0) {
				if (shouldAuthenticate) {
					shouldAuthenticate = prompts[0] != OpenIdPrompt.NONE;
				} else {
					shouldAuthenticate = arrayContainsValue(prompts, OpenIdPrompt.LOGIN);
				}
			}
		}
		return shouldAuthenticate;
	}
}
