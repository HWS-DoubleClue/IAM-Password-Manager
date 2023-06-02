package com.doubleclue.dcem.as.logic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.entities.FidoAuthenticatorEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.utils.KaraUtils;
import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.AuthenticatorAssertionResponse;
import com.yubico.webauthn.data.AuthenticatorAttestationResponse;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs;
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs;
import com.yubico.webauthn.data.PublicKeyCredential;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.UserIdentity;

@ApplicationScoped
public class AsFidoLogic {

	@Inject
	UserLogic userLogic;

	@Inject
	ConfigLogic configLogic;

	@Inject
	AsModule asModule;

	@Inject
	EntityManager em;

	@Inject
	AppServices appServices;

	private static Logger logger = LogManager.getLogger(AsFidoLogic.class);

	public String startRegistration(String username, String rpId) throws DcemException {
		try {
			RelyingParty rp = createFidoRelyingParty(rpId);
			DcemUser user = userLogic.getUser(username);
			PublicKeyCredentialCreationOptions request = FidoUtils.createRegisterRequest(rp, user.getLoginId(), user.getDisplayName(),
					getUserHandleFromUser(user), false);
			addRegRequestInfo(rp, request);
			return FidoUtils.getJson(request);
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
			throw e;
		} catch (Exception e) {
			logger.log(Level.DEBUG, e);
			throw new DcemException(DcemErrorCodes.CANNOT_CREATE_FIDO_REG_REQUEST, "Failed to create FIDO registration request", e);
		}
	}

	@DcemTransactional
	public String finishRegistration(String regResponseJson, String displayName) throws DcemException {
		PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> response;
		try {
			response = PublicKeyCredential.parseRegistrationResponseJson(regResponseJson);
		} catch (Throwable e) {
			logger.log(Level.INFO, e);
			response = null;
		}
		if (response == null) {
			throw new DcemException(DcemErrorCodes.CANNOT_PARSE_FIDO_REG_RESPONSE, "Cannot parse FIDO reg response: " + regResponseJson);
		}

		ByteArray challenge = response.getResponse().getClientData().getChallenge();

		try {
			FidoRegRequestInfo info = getRegRequestInfo(challenge);
			if (info == null) {
				throw new DcemException(DcemErrorCodes.FIDO_REG_REQUEST_NOT_FOUND, "Cannot find FIDO reg request for challenge: " + challenge);
			}
			PublicKeyCredentialCreationOptions request = info.getRequest();
			RegistrationResult result = FidoUtils.validateRegisterResponse(info.getRelyingParty(), request, response);
			UserIdentity userId = request.getUser();
			DcemUser user = userLogic.getUser(userId.getName());
			addFidoAuthenticator(user, result.getKeyId().getId(), result.getPublicKeyCose(), displayName);
			invalidateRegRequest(challenge);
			return createSuccessJson(true);
		} catch (DcemException e) {
			logger.log(Level.DEBUG, e);
			invalidateRegRequest(challenge);
			throw e;
		} catch (Throwable e) {
			logger.info("validateRegisterResponse", e);
			invalidateRegRequest(challenge);
			throw new DcemException(DcemErrorCodes.CANNOT_VALIDATE_FIDO_REG_RESPONSE, "Failed to validate FIDO registration response", e);
		}
	}

	public String startAuthentication(String username, String rpId) throws DcemException {
		try {
			RelyingParty rp = createFidoRelyingParty(rpId);
			AssertionRequest request = FidoUtils.createAssertRequest(rp, username);
			addAuthRequestInfo(rp, request);
			return FidoUtils.getJson(request);
		} catch (Exception e) {
			logger.log(Level.DEBUG, e);
			throw new DcemException(DcemErrorCodes.CANNOT_CREATE_FIDO_AUTH_REQUEST, "Failed to create FIDO authentication request", e);
		}
	}

	public String finishAuthentication(String authResponseJson) throws DcemException {
		PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> response;
		try {
			response = PublicKeyCredential.parseAssertionResponseJson(authResponseJson);
		} catch (IOException e) {
			response = null;
			logger.log(Level.INFO, "parseAssertionResponseJson failed", e);
		}
		if (response == null) {
			throw new DcemException(DcemErrorCodes.CANNOT_PARSE_FIDO_AUTH_RESPONSE, "Cannot parse FIDO auth response: " + authResponseJson);
		}

		ByteArray challenge = response.getResponse().getClientData().getChallenge();

		try {
			FidoAuthRequestInfo info = getAuthRequestInfo(challenge);
			if (info == null) {
				throw new DcemException(DcemErrorCodes.FIDO_AUTH_REQUEST_NOT_FOUND, "Cannot find FIDO auth request for challenge: " + challenge);
			}
			AssertionResult result = FidoUtils.validateAssertResponse(info.getRelyingParty(), info.getRequest(), response);
			DcemUser user = getUserFromUserHandle(result.getUserHandle());
			ByteArray credentialID = result.getCredentialId();
			updateFidoAuthenticator(user, credentialID);
			invalidateAuthRequest(challenge);
			return createSuccessJson(true);
		} catch (DcemException e) {
			logger.log(Level.INFO, "FIDO finishAuthentication failed", e);
			invalidateAuthRequest(challenge);
			throw e;
		} catch (Exception e) {
			logger.log(Level.INFO, "FIDO finishAuthentication failed", e);
			invalidateAuthRequest(challenge);
			throw new DcemException(DcemErrorCodes.CANNOT_VALIDATE_FIDO_AUTH_RESPONSE, "Failed to validate FIDO authentication response", e);
		}
	}

	@DcemTransactional
	public void addFidoAuthenticator(DcemUser user, ByteArray credentialID, ByteArray publicKey, String displayName) {
		FidoAuthenticatorEntity entity = new FidoAuthenticatorEntity();
		entity.setUser(user);
		entity.setCredentialId(credentialID.getBase64());
		entity.setPublicKey(publicKey.getBytes());
		entity.setRegisteredOn(new Date());
		entity.setDisplayName(displayName);
		em.persist(entity);
	}

	@DcemTransactional
	public void updateFidoAuthenticator(DcemUser user, ByteArray credentialID) {
		FidoAuthenticatorEntity entity = getFidoAuthenticator(user, credentialID);
		entity.setLastUsed(new Date());
		em.merge(entity);
	}

	@DcemTransactional
	public void deleteFidoAuthenticator(int id) throws DcemException {
		FidoAuthenticatorEntity entity = getFidoAuthenticator(id);
		if (entity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_FIDO_AUTHENTICATOR_ID, Integer.toString(id));
		} else {
			em.remove(entity);
		}
	}

	public List<FidoAuthenticatorEntity> getFidoAuthenticators(DcemUser user) {
		TypedQuery<FidoAuthenticatorEntity> query = em.createNamedQuery(FidoAuthenticatorEntity.GET_AUTHENTICATORS_BY_USER, FidoAuthenticatorEntity.class);
		query.setParameter(1, user);
		return query.getResultList();
	}

	public List<FidoAuthenticatorEntity> getFidoAuthenticatorsByCredentialID(ByteArray credentialID) {
		TypedQuery<FidoAuthenticatorEntity> query = em.createNamedQuery(FidoAuthenticatorEntity.GET_AUTHENTICATORS_BY_CREDENTIAL_ID,
				FidoAuthenticatorEntity.class);
		query.setParameter(1, credentialID.getBase64());
		return query.getResultList();
	}

	public FidoAuthenticatorEntity getFidoAuthenticator(DcemUser user, ByteArray credentialID) {
		TypedQuery<FidoAuthenticatorEntity> query = em.createNamedQuery(FidoAuthenticatorEntity.GET_AUTHENTICATOR_BY_USER_AND_CREDENTIAL_ID,
				FidoAuthenticatorEntity.class);
		query.setParameter(1, user);
		query.setParameter(2, credentialID.getBase64());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public FidoAuthenticatorEntity getFidoAuthenticator(int id) {
		TypedQuery<FidoAuthenticatorEntity> query = em.createNamedQuery(FidoAuthenticatorEntity.GET_AUTHENTICATOR_BY_ID, FidoAuthenticatorEntity.class);
		query.setParameter(1, id);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@DcemTransactional
	public void deleteUserTokens(DcemUser user) {
		List<FidoAuthenticatorEntity> list = getFidoAuthenticators(user);
		if (list != null) {
			for (FidoAuthenticatorEntity entity : list) {
				em.remove(entity);
			}
		}
	}

	public PublicKeyCredentialDescriptor getDescriptorFromFidoAuthenticator(FidoAuthenticatorEntity entity) {
		ByteArray credentialID = ByteArray.fromBase64(entity.getCredentialId());
		return PublicKeyCredentialDescriptor.builder().id(credentialID).build();
	}

	public RegisteredCredential getRegisteredCredentialFromFidoAuthenticator(FidoAuthenticatorEntity entity) throws DcemException {
		ByteArray publicKey = new ByteArray(entity.getPublicKey());
		ByteArray credentialID = ByteArray.fromBase64(entity.getCredentialId());
		ByteArray userHandle = getUserHandleFromUser(entity.getUser());
		return RegisteredCredential.builder().credentialId(credentialID).userHandle(userHandle).publicKeyCose(publicKey).build();
	}

	public ByteArray getUserHandleFromUser(DcemUser user) throws DcemException {
		try {
			byte[] userId = KaraUtils.intToByteArray(user.getId());
			String clusterIdString = TenantIdResolver.isCurrentTenantMaster() ? configLogic.getClusterConfig().getName()
					: TenantIdResolver.getCurrentTenantName(); // please improve
			byte[] clusterId = clusterIdString.getBytes(DcemConstants.CHARSET_UTF8);
			return new ByteArray((byte[]) ArrayUtils.addAll(userId, clusterId));
		} catch (UnsupportedEncodingException e) { // should never happen
			return null;
		}
	}

	public DcemUser getUserFromUserHandle(ByteArray userHandle) {
		byte[] idBytes = Arrays.copyOfRange(userHandle.getBytes(), 0, 4);
		int userId = ByteBuffer.wrap(idBytes).getInt();
		return userLogic.getUser(userId);
	}

	private String createSuccessJson(boolean success) {
		return "{ \"success\": " + (success ? "true" : "false") + " }";
	}

	private RelyingParty createFidoRelyingParty(String rpId) {
		if (rpId != null && !rpId.isEmpty()) {
			HashSet<String> allowedOrigins;
			String originsCsv = asModule.getPreferences().getFidoAllowedOrigins();
			if (originsCsv != null && !originsCsv.isEmpty()) {
				String[] originsArray = originsCsv.replaceAll("\\s+", "").split(",");
				allowedOrigins = new HashSet<>(Arrays.asList(originsArray));
			} else {
				allowedOrigins = new HashSet<>();
			}

			String clusterId;
			try {
				clusterId = appServices.getClusterId();
			} catch (Exception e) {
				clusterId = TenantIdResolver.getCurrentTenantName(); // please improve
			}

			try {
				return FidoUtils.createRelyingParty(rpId, clusterId, new FidoCredentialRepository(), allowedOrigins);
			} catch (Exception e) {
				logger.debug("Failed to create FIDO Relying Party.", e);
				return null;
			}
		} else {
			return null;
		}
	}

	private AsTenantData getTenantData() {
		return asModule.getTenantData();
	}

	private void addRegRequestInfo(RelyingParty rp, PublicKeyCredentialCreationOptions request) {
		getTenantData().getFidoRegRequests().put(request.getChallenge(), new FidoRegRequestInfo(rp, request));
	}

	private void addAuthRequestInfo(RelyingParty rp, AssertionRequest request) {
		getTenantData().getFidoAuthRequests().put(request.getPublicKeyCredentialRequestOptions().getChallenge(), new FidoAuthRequestInfo(rp, request));
	}

	private FidoRegRequestInfo getRegRequestInfo(ByteArray challenge) throws ExecutionException {
		return getTenantData().getFidoRegRequests().get(challenge);
	}

	private FidoAuthRequestInfo getAuthRequestInfo(ByteArray challenge) throws ExecutionException {
		return getTenantData().getFidoAuthRequests().get(challenge);
	}

	private void invalidateRegRequest(ByteArray challenge) {
		getTenantData().getFidoRegRequests().invalidate(challenge);
	}

	private void invalidateAuthRequest(ByteArray challenge) {
		getTenantData().getFidoAuthRequests().invalidate(challenge);
	}

	public List<FidoAuthenticatorEntity> queryFidoAuthenticators(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults) throws DcemException {
		JpaSelectProducer<FidoAuthenticatorEntity> jpaSelectProducer = new JpaSelectProducer<FidoAuthenticatorEntity>(em, FidoAuthenticatorEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}
		return jpaSelectProducer.selectCriteriaQueryFilters(filterItems, firstResult, page, null);
	}
}
