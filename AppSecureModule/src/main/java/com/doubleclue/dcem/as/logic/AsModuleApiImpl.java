package com.doubleclue.dcem.as.logic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.comm.AuthGatewayServices;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.FingerprintLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.as.restapi.model.RequestLoginQrCodeResponse;
import com.doubleclue.dcem.as.tasks.AsCreateChangeTenantCall;
import com.doubleclue.dcem.as.tasks.AsCreateChangeTenantCallResult;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsMessageResponse;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.as.AsMsgStatus;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.as.QrCodeResponse;
import com.doubleclue.dcem.core.as.QueryLoginResponse;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.JpaEntityCacheLogic;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.JndiProxyParam;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;

@ApplicationScoped
@Named("asModuleApiImpl")
public class AsModuleApiImpl implements AsModuleApi {

	// private static final Logger logger = LogManager.getLogger(AsModuleApiImpl.class);

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	AsModule asModule;

	@Inject
	AsVersionLogic versionLogic;

	@Inject
	AsMessageLogic messageLogic;

	@Inject
	AsAuthGatewayLogic authGatewayLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	AsMessageHandler messageHandler;

	@Inject
	AppServices appServices;

	@Inject
	FingerprintLogic fingerprintLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	AuthGatewayServices authGatewayServices;

	@Inject
	AsAuthGatewayLogic asAuthGatewayLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	JpaEntityCacheLogic jpaEntityCacheLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	EntityManager em;

	public AsModuleApiImpl() {
		super();
	}

	@Override
	public String createActivationCode(DcemUser dcemUser, LocalDateTime validTill, SendByEnum sendBy, String info) throws DcemException {
		ActivationCodeEntity ActivationCodeEntity = activationLogic.createActivationCode(dcemUser, validTill, sendBy, info);
		return ActivationCodeEntity.getActivationCode();
	}

	@Override
	public LocalDateTime getActivationCodeDefaultValidTill() {
		return LocalDateTime.now().plusHours(asModule.getPreferences().getActivationCodeDefaultValidTill());
	}

	@Override
	public AsMessageResponse getMessageResponse(Long msgId, int waitTimeMilliSeconds) throws DcemException {
		AsApiMessageResponse apiMessageResponse = messageHandler.retrieveMessageResponse(msgId, waitTimeMilliSeconds);
		AsMsgStatus asMsgStatus = AsMsgStatus.valueOf(apiMessageResponse.getMsgStatus().name());
		AsMessageResponse asMessageResponse = new AsMessageResponse(apiMessageResponse.getId(), asMsgStatus, apiMessageResponse.getActionId(),
				apiMessageResponse.getSessionId(), apiMessageResponse.getUserLoginId());
		asMessageResponse.setSessionCookie(apiMessageResponse.getSessionCookie());
		asMessageResponse.setSessionCookieExpiresOn(apiMessageResponse.getSessionCookieExpiresOn());
		return asMessageResponse;
	}

	@Override
	public AuthenticateResponse authenticate(AuthApplication authApplication, int subId, String userLoginId, AuthMethod authMethod, String password,
			String passcode, AuthRequestParam requestParam) throws DcemException {
		return authenticationLogic.authenticate(authApplication, subId, userLoginId, authMethod, password, passcode, requestParam);
	}

	@Override
	public void cancelMessage(long msgId) throws DcemException {
		messageHandler.cancelPendingMsg(msgId);
	}

	@Override
	public QrCodeResponse requestQrCode(String source, String sessionId) throws DcemException {
		RequestLoginQrCodeResponse qrCodeResponse = appServices.generateLoginQrCode(source, sessionId);
		return new QrCodeResponse(qrCodeResponse.getData(), qrCodeResponse.getTimeToLive());
	}

	@Override
	public void resetStayLogin(DcemUser user) throws DcemException {
		fingerprintLogic.deleteUserFingerprints(user.getId());
	}

	@Override
	public void modifiedUser(DcemUser preUser, DcemUser newUser) {
		if (newUser.isDisabled() && preUser.isDisabled() == false) {
			deviceLogic.killUserDevices(newUser);
		}
	}

	@Override
	public void onCreateTenant(TenantEntity tenantEntity) throws Exception {
		PolicyEntity globalPolicy = policyLogic.createOrGetGlobalPolicy();
		PolicyEntity mgtPolicy = policyLogic.createOrGetManagementPolicy();
		List<AsVersionEntity> versions = versionLogic.getDetachedVersions();
		Future<?> future = taskExecutor.submit(new AsCreateChangeTenantCall(tenantEntity, globalPolicy, mgtPolicy, versions));
		AsCreateChangeTenantCallResult result = (AsCreateChangeTenantCallResult) future.get();
		if (result.getException() != null) {
			throw result.getException();
		}
	}

	@Override
	public String onCreateActivationCodeTenant(TenantEntity tenantEntity, String email, String mobileNumber, SendByEnum activationCodeSendBy,
			SupportedLanguage supportedLanguage, boolean sendPasswordBySms, String superAdminPassword, String loginId, boolean selfCreateTenant)
			throws Exception {
		DcemTemplate dcemTemplate;
		String templateName;
		if (selfCreateTenant == false) {
			templateName = DcemConstants.EMAIL_ACTIVATION_NEW_TENANT_TEMPLATE;
		} else {
			templateName = DcemConstants.EMAIL_ACTIVATION_SELF_CREATE_TENANT_TEMPLATE;
		}
		dcemTemplate = templateLogic.getTemplateByNameLanguage(templateName, supportedLanguage);
		if (dcemTemplate == null) {
			throw new DcemException(DcemErrorCodes.NO_TEMPLATE_FOUND, templateName);
		}
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(supportedLanguage.getLocale());
		String smsTextResource = dbResourceBundle.getString(AsConstants.PASSWORD_BY_SMS_BUNDLE_KEY);
		Future<?> future = taskExecutor.submit(new AsCreateChangeTenantCall(tenantEntity, email, mobileNumber, activationCodeSendBy, dcemTemplate,
				sendPasswordBySms, superAdminPassword, smsTextResource, loginId, selfCreateTenant));
		AsCreateChangeTenantCallResult result = (AsCreateChangeTenantCallResult) future.get();
		if (result.getException() != null) {
			throw result.getException();
		} else {
			return result.getActivationCode().getActivationCode();
		}
	}

	@Override
	public void onRecoverSuperAdminAccess(TenantEntity tenantEntity) throws Exception {
		Future<?> future = taskExecutor.submit(new AsCreateChangeTenantCall(tenantEntity));
		AsCreateChangeTenantCallResult result = (AsCreateChangeTenantCallResult) future.get();
		if (result.getException() != null) {
			throw result.getException();
		}
	}

	@Override
	public QueryLoginResponse queryLoginQrCode(String source, String sessionId, boolean pollOnly, int waitMaxTimeMilliSeconds) throws DcemException {
		return appServices.queryLoginQrCode(source, sessionId, pollOnly, waitMaxTimeMilliSeconds);
	}

	@Override
	public void setUserCloudSafe(String name, String options, LocalDateTime discardAfter, DcemUser dcemUser, boolean withAuditing, char[] password, byte[] content)
			throws DcemException {
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.USER, dcemUser, null, name, discardAfter, options, false, null);
		cloudSafeLogic.setCloudSafeByteArray(cloudSafeEntity, password, content, dcemUser, null);
	}

	@Override
	public List<AuthMethod> getAllowedAuthMethods(AuthApplication authApplication, int subId, DcemUser dcemUser) {
		try {
			return policyLogic.getPolicy(authApplication, subId, dcemUser).getDcemPolicy().getAllowedMethods();
		} catch (Exception e) {
			return new ArrayList<AuthMethod>();
		}
	}

	@Override
	public JndiProxyParam openAuthProxyConnection(String authConnectorName, String host, int port, boolean secure, boolean verifyCertificate,
			AuthProxyListener authProxyListener) throws DcemException {
		return authGatewayServices.openAuthProxyConnection(authConnectorName, host, port, secure, verifyCertificate, authProxyListener);
	}

	@Override
	public void closeAuthProxyConnection(JndiProxyParam proxyParam) {
		authGatewayServices.closeAuthProxyConnection(proxyParam);
	}

	@Override
	public void sendDataAuthProxy(JndiProxyParam proxyParam, byte[] data, int offset, int length) throws Exception {
		authGatewayServices.sendDataAuthProxy(proxyParam, data, offset, length);

	}

	@Override
	public List<String> getAuthConnectorNames() {
		List<AuthGatewayEntity> entiries = asAuthGatewayLogic.getAllAuthGateway();
		List<String> list = new ArrayList<String>(entiries.size());
		for (AuthGatewayEntity authGatewayEntity : entiries) {
			list.add(authGatewayEntity.getName());
		}
		return list;
	}


	@Override
	public void killUserDevices(DcemUser dcemUser) {
		deviceLogic.killUserDevices(dcemUser);
	}

	@Override
	public long getCloudSafeUsageMb() {
		return asModule.getTenantData().getGlobalCloudSafeUsageTotal().get() / (1024 * 1024);
	}

	@Override
	public String requestActivationCode(DcemUser user) {
		return  activationLogic.requestActivationCode(user);
	}

	@Override
	public boolean verifyFingerprint(Integer userId, Integer id, String fingerprint) {
		return fingerprintLogic.verifyFingerprint(userId, id, fingerprint);
	}
}
