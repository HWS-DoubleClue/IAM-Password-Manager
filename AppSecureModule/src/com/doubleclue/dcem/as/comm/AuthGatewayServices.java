package com.doubleclue.dcem.as.comm;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.AuthConnectParam;
import com.doubleclue.comm.thrift.AuthSelectParam;
import com.doubleclue.comm.thrift.AuthSelectResponse;
import com.doubleclue.comm.thrift.AuthUserParam;
import com.doubleclue.comm.thrift.AuthUserResponse;
import com.doubleclue.comm.thrift.ProxyOpenParam;
import com.doubleclue.comm.thrift.ThriftAuthMethod;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.logic.AsAuthGatewayLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.as.logic.AsUtils;
import com.doubleclue.dcem.as.logic.AuthAppSession;
import com.doubleclue.dcem.as.logic.AuthAppSession.AuthAppState;
import com.doubleclue.dcem.as.logic.ExceptionReporting;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.tasks.ProcessProxyReceivedData;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.JndiProxyParam;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.SecureUtils;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("authGatewayServices")
public class AuthGatewayServices {

	private static final int MAX_WAIT_RECEIVE = 2000; // 2 seconds

	private static Logger logger = LogManager.getLogger(AuthGatewayServices.class);

	@Inject
	AsAuthGatewayLogic authGatewayLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AsModule asModule;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	TaskExecutor taskExecutor;
	
	

	/**
	 * @param authUserParam
	 * @return
	 * @throws ExceptionReporting
	 */
	public AuthUserResponse authenticateUser(AuthUserParam authUserParam) throws ExceptionReporting {

		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		AsTenantData tenantData = asModule.getTenantData();
		switch (appSession.getState()) {
		case serverSignature:
		case authenticated:
		case authenticatedInProgress:
		case loggedIn:	
			break;
		default:
			throw new ExceptionReporting(new DcemReporting(ReportAction.Authenticate, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, null, authUserParam.getLoginId()), null);
		}
		VerifyAuthGatewayResponse verifyGatewayResponse = verifyGatewayCredentials(appSession, authUserParam.authGatewayId, authUserParam.getSharedSecret(), ReportAction.Authenticate);
		PolicyAppEntity appEntity = verifyGatewayResponse.appEntity;
		AuthMethod authMethod = null;
		if (authUserParam.getAuthMethod() != ThriftAuthMethod.AUTO) {
			authMethod = getAuthMethod(authUserParam.getAuthMethod());
			if (authMethod == null) {
				throw new ExceptionReporting(new DcemReporting(getAppName(appEntity), ReportAction.Authenticate, (DcemUser) null, AppErrorCodes.WRONG_CREDENTIALS.name(), null,
						"No AuthMethod found: " + authUserParam.getAuthGatewayId(),AlertSeverity.FAILURE), null);
			}
		}

		AuthRequestParam requestParam = new AuthRequestParam();
		String passcode = null;
		String password = null;
		byte[] binPassword = null;
		byte[] binPasscode = null;
		appSession.getWsSession().setMaxIdleTimeout(asModule.getPreferences().getKeepAliveConnection() * 1000);
		try {

			if (authUserParam.encPassword != null) {
				binPassword = SecureServerUtils.decryptData(appSession.passwordEncryptionKey, authUserParam.encPassword.array());
				password = new String(binPassword, DcemConstants.CHARSET_UTF8);
			}

			if (authUserParam.encPasscode != null) {
				binPasscode = SecureServerUtils.decryptData(appSession.passwordEncryptionKey, authUserParam.encPasscode.array());
				passcode = new String(binPasscode, DcemConstants.CHARSET_UTF8);
			}
			requestParam.setSessionId(appSession.getWsSession().getSessionId());
			requestParam.setReportInfo("From: " + authUserParam.getWorkstationName());

			if ((authMethod == AuthMethod.SESSION_RECONNECT || authMethod == null) && passcode != null) {
				requestParam.setSessionCookie(passcode);
			}

			Map<String, String> paramMap = authUserParam.getPropertyMap();
			requestParam.setParamMap(paramMap);
			if (paramMap != null) {
				for (Map.Entry<String, String> entry : paramMap.entrySet()) {
					switch (entry.getKey()) {
					case AppSystemConstants.AUTH_PARAM_UNLOCK:
						requestParam.setUnlockUserAuth(entry.getValue().equals(AppSystemConstants.TRUE_VALUE));
						break;
					case AppSystemConstants.AUTH_PARAM_USE_ALTERNATIVES:
						requestParam.setUseAlternativeAuthMethods(entry.getValue().equals(AppSystemConstants.TRUE_VALUE));
						break;
					default:
						break;
					}
				}
			}

			// now do authentication
			AuthenticateResponse authenticateResponse = authenticationLogic.authenticate(AuthApplication.AuthGateway, verifyGatewayResponse.authGatewayEntity.getId(), authUserParam.getLoginId(),
					authMethod, password, passcode, requestParam);
			AuthUserResponse authUserResponse = new AuthUserResponse();
			authUserResponse.setSuccess(authenticateResponse.isSuccessful());
			List<ThriftAuthMethod> listThriftMethods = new ArrayList<>();
			if (authenticateResponse.getAuthMethods() != null) {
				for (AuthMethod authMethod2 : authenticateResponse.getAuthMethods()) {
					listThriftMethods.add(getThriftAuthMethod(authMethod2));
				}
			}
			authUserResponse.setAuthMethods(listThriftMethods);
			authUserResponse.setMsgId(authenticateResponse.getSecureMsgId());
			DcemUser dcemUser = authenticateResponse.getDcemUser();
			String udid = java.util.Base64.getEncoder().encodeToString(authUserParam.getUdid());
			if (authenticateResponse.getDcemException() != null) {
				throw authenticateResponse.getDcemException();
			}

			if (dcemUser != null && dcemUser.getDomainEntity() != null) {
				authUserResponse.setLdapDomain(dcemUser.getDomainEntity().getName());
			}

			if (authUserResponse.isSuccess() == false) {
				if (authenticateResponse.getAuthMethods() == null) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "getAuthMethods() = null");
				}
				if (authenticateResponse.getAuthMethods().size() == 1) {
					authMethod = authenticateResponse.getAuthMethods().get(0);
					if (authMethod == AuthMethod.PUSH_APPROVAL) {
						authUserResponse.setResponseTime(authenticateResponse.getSecureMsgTimeToLive());
						authUserResponse.setSessionCookie(authenticateResponse.getSecureMsgRandomCode());
					}
				}
				appSession.setState(ConnectionState.authenticatedInProgress);
			} else {
				appSession.setState(ConnectionState.authenticated);
				appSession.setUserId(dcemUser.getId());
				authUserResponse.setSessionCookie(authenticateResponse.getSessionCookie());
				authUserResponse.setSessionCookieExpiresOn(authenticateResponse.getSessionCookieExpiresOn());
				authUserResponse.setSecureMsgRandomCode(authenticateResponse.getSecureMsgRandomCode());
				authUserResponse.setPhoneNumber(authenticateResponse.getPhoneNumber());
				authUserResponse.setFqUserLoginId(authenticateResponse.getFqUserLoginId());
				authUserResponse.setFidoResponse(authenticateResponse.getFidoResponse());
				byte[] key;
				key = dcemUser.getSalt();
				if (key == null) {
					key = RandomUtils.getRandom(32);
					dcemUser.setSalt(key);
					userLogic.setUserSalt(dcemUser);
				} 
				authUserResponse.setUserKey(key);
			}

			AuthAppSession authAppSession = new AuthAppSession(appSession, appEntity, dcemUser, authUserParam.getWorkstationName(), udid, verifyGatewayResponse.authGatewayEntity.getName(),
					authUserResponse);
			tenantData.getAuthAppSessions().put(appSession.getWsSession().getSessionId(), authAppSession);
			return authUserResponse;

		} catch (DcemException exp) {
			AppErrorCodes appErrorCode = AsUtils.convertToAppErrorCodes(exp.getErrorCode());
			throw new ExceptionReporting(appErrorCode.name(), appErrorCode.name()); // will not write any report
		} catch (Exception e) {
			logger.info("AuthUser, Tenant " + TenantIdResolver.getCurrentTenantName() + " user: " + authUserParam.getLoginId(), e);
			throw new ExceptionReporting(new DcemReporting(getAppName(appEntity), ReportAction.Authenticate, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR.name(), null, e.toString(),AlertSeverity.FAILURE), null, null);
		} finally {
			if (binPassword != null) {
				Arrays.fill(binPassword, (byte) 0x0);
			}
			if (password != null) {
				StringUtils.wipeString(password);
			}
		}
	}

	public AuthSelectResponse getAuthMethods(AuthSelectParam authSelectParam) throws ExceptionReporting {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		VerifyAuthGatewayResponse verifyGatewayResponse = verifyGatewayCredentials(appSession, authSelectParam.authGatewayId, authSelectParam.getSharedSecret(), ReportAction.GetAuthMethods);
		try {
			List<AuthMethod> authMethods = policyLogic.getAuthMethods(AuthApplication.AuthGateway, verifyGatewayResponse.authGatewayEntity.getId(), null);
			List<ThriftAuthMethod> listThriftMethods = new ArrayList<>(authMethods.size());
			for (AuthMethod authMethod2 : authMethods) {
				listThriftMethods.add(getThriftAuthMethod(authMethod2));
			}
			AuthSelectResponse authSelectResponse = new AuthSelectResponse(listThriftMethods);
			return authSelectResponse;
		} catch (DcemException exp) {
			throw new ExceptionReporting(
					new DcemReporting(getAppName(verifyGatewayResponse.appEntity), ReportAction.GetAuthMethods, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR.name(), null, exp.toString(),AlertSeverity.FAILURE), null,
					null);
		}
	}

	private VerifyAuthGatewayResponse verifyGatewayCredentials(AppSession appSession, String gatewayName, byte[] gatewaySharedSecret, ReportAction reportAction) throws ExceptionReporting {
		PolicyAppEntity appEntity;
		AuthGatewayEntity authGatewayEntity = authGatewayLogic.getAuthAppEntitiy(gatewayName);
		if (authGatewayEntity == null) {
			appEntity = asModule.getMainPolicyAppEntity(AuthApplication.AuthGateway);
			throw new ExceptionReporting(
					new DcemReporting(getAppName(appEntity), reportAction, (DcemUser) null, AppErrorCodes.WRONG_CREDENTIALS.name(), null, "AuthApp doesn't exist or disbaled. ID=" + gatewayName,AlertSeverity.FAILURE), null);
		}
		appEntity = policyLogic.getDetachedPolicyApp(AuthApplication.AuthGateway, authGatewayEntity.getId());
		byte[] sharedSecret = null;
		if (authGatewayEntity.getRetryCounter() > adminModule.getPreferences().getPasswordMaxRetryCounter()) {
			throw new ExceptionReporting(new DcemReporting(getAppName(appEntity), reportAction, (DcemUser) null, AppErrorCodes.WRONG_CREDENTIALS.name(), null, "Max retries for: " + gatewayName,AlertSeverity.FAILURE), null);
		}
		try {
			sharedSecret = SecureUtils.decryptData(appSession.passwordEncryptionKey, gatewaySharedSecret);
		} catch (Exception e1) {
			throw new ExceptionReporting(
					new DcemReporting(getAppName(appEntity), reportAction, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR.name(), null, e1.toString() + " ID=" + authGatewayEntity.getName(),AlertSeverity.FAILURE), null);
		}
		if (Arrays.equals(authGatewayEntity.getSharedKey(), sharedSecret) == false) {
			authGatewayEntity.addRetryCounter();
			throw new ExceptionReporting(new DcemReporting(getAppName(appEntity), reportAction, (DcemUser) null, AppErrorCodes.WRONG_CREDENTIALS.name(), null, "Wrong Shared-Secret for: " + gatewayName,AlertSeverity.FAILURE),
					null);
		}
		authGatewayEntity.setRetryCounter(0);
		return new VerifyAuthGatewayResponse(appEntity, authGatewayEntity);
	}

	private ThriftAuthMethod getThriftAuthMethod(AuthMethod authMethod) {
		return ThriftAuthMethod.valueOf(authMethod.name());
	}

	private AuthMethod getAuthMethod(ThriftAuthMethod thriftAuthMethod) {
		return AuthMethod.valueOf(thriftAuthMethod.name());
	}

	public JndiProxyParam openAuthProxyConnection(String authConnectorName, String host, int port, boolean secure, boolean verifyCertificate, AuthProxyListener authProxyListener)
			throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		AuthAppSession authAppSession = null;
		if (tenantData.getAuthAppSessions() == null) {
			throw new DcemException(DcemErrorCodes.AUTH_PROXY_OPEN, "Auth Connector is not active: " + authConnectorName);
		}
		for (AuthAppSession authAppSession2 : tenantData.getAuthAppSessions().values()) {
			if (authAppSession2.getEntityName().equals(authConnectorName)) {
				authAppSession = authAppSession2;
				break;
			}
		}
		if (authAppSession == null) {
			throw new DcemException(DcemErrorCodes.AUTH_PROXY_OPEN, "Auth Connector is not active: " + authConnectorName);
		}
		Long handle = authAppSession.getNextIndex();
		try {
			ProxyOpenParam proxyOpenParam = new ProxyOpenParam(handle, host, port, secure, verifyCertificate, MAX_WAIT_RECEIVE);
			authAppSession.getAppSession().getServerToApp().proxyOpen(proxyOpenParam);
			authAppSession.addAuthProxyListener(handle, authProxyListener);
			authAppSession.setAuthAppState(AuthAppState.connected);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.AUTH_PROXY_OPEN, e.getMessage(), e);
		}
		JndiProxyParam proxyParam = new JndiProxyParam(authAppSession.getAppSession().getWsSession().getSessionId(), handle);
		if (logger.isDebugEnabled()) {
			logger.debug("AuthProxy Open " + proxyParam.toString());
		}
		return proxyParam;
	}

	public void closeAuthProxyConnection(JndiProxyParam proxyParam) {
		AsTenantData tenantData = asModule.getTenantData();
		AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(proxyParam.getSessionId());
		if (authAppSession == null) {
			logger.info("CloseAuthProxy for an AuthConnection which doesn't exists any more");
			return;
		}
		try {
			if (authAppSession.getAuthAppState() == AuthAppState.connected) {
				authAppSession.getAppSession().getServerToApp().proxyClose(proxyParam.getHandle());
			}
		} catch (Exception e) {
			logger.info("CloseAuthProxy", e);
		}
		authAppSession.removeAuthProxyListener(proxyParam.getHandle());
		if (logger.isDebugEnabled()) {
			logger.debug("AuthProxy Close " + proxyParam.toString());
		}
		return;
	}

	public synchronized void sendDataAuthProxy(JndiProxyParam proxyParam, byte[] data, int offset, int length) throws Exception {
		// logger.debug("AuthGatewayServices.dataAuthProxyConnection() IN " + length);
		AsTenantData tenantData = asModule.getTenantData();
		if (proxyParam == null) {
			throw new Exception("authConnectorSessionId is null");
		}
		AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(proxyParam.getSessionId());
		if (authAppSession == null) {
			logger.info("CloseAuthProxy for an AuthConnection whcih doesn't exists any more. " + proxyParam.toString());
			throw new DcemException(DcemErrorCodes.AUTH_PROXY_DATA, "No AuthConnectorSession found." + proxyParam.toString());
		}
		try {
			int recLength = 0;
			ByteBuffer recByteBuffer = authAppSession.getAppSession().getServerToApp().proxyData(proxyParam.getHandle(), ByteBuffer.wrap(data, offset, length));
			if (recByteBuffer != null) {
				recLength = recByteBuffer.limit();
				if (recLength > 0) {
					taskExecutor.submit(new ProcessProxyReceivedData(authAppSession.getAuthProxyListener(proxyParam.getHandle()), recByteBuffer));
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("AuthProxy Data to Client" + proxyParam.toString() + " , Send-Length: " + length + " Received: " + recLength);
			}
		} catch (Exception e) {
			logger.warn("Couldn't send Data to proxy." + proxyParam.toString(), e);
			closeAuthProxyConnection(proxyParam);
			throw e;
		}
	}

	/*
	 * Receiving Data from clinet proxy
	 */
	public synchronized void receiveDataAuthProxy(AppSession appSession, long handle, ByteBuffer byteBuffer) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(appSession.wsSession.getSessionId());
		taskExecutor.execute(new ProcessProxyReceivedData(authAppSession.getAuthProxyListener(handle), byteBuffer));
		if (logger.isDebugEnabled()) {
			logger.debug("AuthProxy Data from Client handle: " + handle + " , length: " + byteBuffer.remaining());
		}
	}

	public void receiveCloseAuthProxy(AppSession appSession, long handle) {
		AsTenantData tenantData = asModule.getTenantData();
		AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(appSession.wsSession.getSessionId());
		authAppSession.getAuthProxyListener(handle).onClose();
		authAppSession.removeAuthProxyListener(handle);
		authAppSession.setAuthAppState(AuthAppState.disconnected);
		if (logger.isDebugEnabled()) {
			logger.debug("AuthProxy Close from Client handle: " + handle);
		}

	}

	/**
	 * @param authConnectParam
	 * @return
	 * @throws ExceptionReporting
	 */
	public int authConnect(AuthConnectParam authConnectParam) throws ExceptionReporting {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		AsTenantData tenantData = asModule.getTenantData();
		if (appSession.getState() != ConnectionState.serverSignature) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.AuthConnect, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, null, null), (String) null);
		}
		VerifyAuthGatewayResponse verifyGatewayResponse = verifyGatewayCredentials(appSession, authConnectParam.authGatewayId, authConnectParam.getSharedSecret(), ReportAction.AuthConnect);
		appSession.setState(ConnectionState.AuthConnect);
		int keepAlive = asModule.getPreferences().getKeepAliveConnection();
		appSession.getWsSession().getSession().setMaxIdleTimeout(keepAlive * 1000);
		String udid = java.util.Base64.getEncoder().encodeToString(authConnectParam.getUdid());
		AuthAppSession authAppSession = new AuthAppSession(appSession, (PolicyAppEntity) null, (DcemUser) null, authConnectParam.getWorkstationName(), udid,
				verifyGatewayResponse.authGatewayEntity.getName(), (AuthUserResponse) null);
		tenantData.getAuthAppSessions().put(appSession.getWsSession().getSessionId(), authAppSession);
		return keepAlive;
	}

	private String getAppName(PolicyAppEntity appEntity) {
		return appEntity.getSubName() != null ? appEntity.getSubName() : appEntity.getAuthApplication().name();
	}
}

class VerifyAuthGatewayResponse {
	PolicyAppEntity appEntity;
	AuthGatewayEntity authGatewayEntity;

	public VerifyAuthGatewayResponse(PolicyAppEntity appEntity, AuthGatewayEntity authGatewayEntity) {
		super();
		this.appEntity = appEntity;
		this.authGatewayEntity = authGatewayEntity;
	}
}
