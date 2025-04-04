package com.doubleclue.dcem.as.comm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;

import com.doubleclue.comm.thrift.ActivationParam;
import com.doubleclue.comm.thrift.ActivationResponse;
import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.AppVersion;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.CpuArch;
import com.doubleclue.comm.thrift.DeviceOfflineKey;
import com.doubleclue.comm.thrift.DomainSdkConfigParam;
import com.doubleclue.comm.thrift.DomainSdkConfigResponse;
import com.doubleclue.comm.thrift.LoginParam;
import com.doubleclue.comm.thrift.LoginResponse;
import com.doubleclue.comm.thrift.RequestActivationCodeResponse;
import com.doubleclue.comm.thrift.SdkCloudSafe;
import com.doubleclue.comm.thrift.SdkCloudSafeKey;
import com.doubleclue.comm.thrift.ServerSignatureParam;
import com.doubleclue.comm.thrift.ServerSignatureResponse;
import com.doubleclue.comm.thrift.SignatureParam;
import com.doubleclue.comm.thrift.SignatureResponse;
import com.doubleclue.comm.thrift.Template;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.dm.DmModuleApi;
import com.doubleclue.dcem.as.dm.UploadDocument;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.FingerprintId;
import com.doubleclue.dcem.as.entities.UserFingerprintEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsMessageLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsPreferences;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.as.logic.AsUtils;
import com.doubleclue.dcem.as.logic.AsVersionLogic;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.logic.DeviceStatus;
import com.doubleclue.dcem.as.logic.DevicesUserDtoOffline;
import com.doubleclue.dcem.as.logic.DispatcherApi;
import com.doubleclue.dcem.as.logic.ExceptionReporting;
import com.doubleclue.dcem.as.logic.FcmLogic;
import com.doubleclue.dcem.as.logic.LoginQrCode;
import com.doubleclue.dcem.as.logic.LoginQrCodeContent;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.as.logic.RegisteredDomain;
import com.doubleclue.dcem.as.logic.ReverseProxyConnection;
import com.doubleclue.dcem.as.logic.ReverseProxyReport;
import com.doubleclue.dcem.as.logic.ReverseProxyStatus;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.FingerprintLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.as.restapi.model.RequestLoginQrCodeResponse;
import com.doubleclue.dcem.as.tasks.CallGetReversProxyStatusTask;
import com.doubleclue.dcem.as.tasks.CheckMessageTask;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.QueryLoginResponse;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.AsApiOtpToken;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.SecureUtils;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Charsets;
import com.google.common.io.Files;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

@ApplicationScoped
@Named("appServices")
public class AppServices {

	private static Logger logger = LogManager.getLogger(AppServices.class);

	@Inject
	EntityManager em;

	@Inject
	UserLogic userLogic;

	@Inject
	AsVersionLogic versionLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	AsModule asModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	AsMessageLogic messageLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	AdminModule adminModule;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	ConfigLogic configLogic;

	@Inject
	AsMessageHandler asMessageHandler;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	FcmLogic fcmLogic;

	@Inject
	FingerprintLogic fingerprintLogic;

	@Inject
	GroupLogic groupLogic;

	// ConcurrentHashMap<String, AppSession> reverseProxyDcemConnections; //

	DcemModule dispatcherModule;

	private final static ObjectMapper objectMapper = new ObjectMapper();

	private static final int MAX_REPORT = 200;

	// @PostConstruct
	public void init() {
		dispatcherModule = applicationBean.getModule(DcemConstants.DISPATCHER_MODULE_ID);
		if (dispatcherModule != null) {
			AppWsConnection.getInstance().createRpReportList(MAX_REPORT);
		}
	}

	/**
	 * @param activationParam
	 * @return
	 * @throws AppException
	 * @throws TException
	 */
	public ActivationResponse activation(ActivationParam activationParam, AppSession appSession, String location) throws ExceptionReporting, TException {
		if (appSession.getState() == ConnectionState.invalidTenant) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.INVALID_TENANT_IDENTIFIER, location, null),
					null);
		}
		if (appSession.getState() != ConnectionState.serverSignature) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, location, null), null);
		}
		DcemUser user = null;
		try {
			user = userLogic.getUser(activationParam.getUserId());
		} catch (DcemException e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.NO_DISTINCT_USER_NAME, location,
					"userid=" + activationParam.getUserId()), null);
		}
		if (user == null) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.INVALID_USERID, location,
					"userid=" + activationParam.getUserId()), null);
		}
		int userState = userLogic.isUserEnabled(user);
		if (userState != 0) {
			if (userState == 1) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.USER_DISABLED, location, null), null, null);
			} else {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.USER_TEMPORARY_DISABLED, location, null), null);
			}
		}

		verifyUserPassword(user, appSession, activationParam.getEncPassword(), ReportAction.Activation);

		ActivationCodeEntity activationCode = activationLogic.validateActivationCode(user, activationParam.getActivationCode());
		if (activationCode == null) {
			String tempDisabledInfo = null;

			int tempDisabled = userLogic.incFailActivations(user, asModule.getPreferences().getMaxFailActivations(),
					asModule.getPreferences().getRetryActivationDelayinMinutes());
			if (tempDisabled > 0) {
				tempDisabledInfo = "Temporary disabled for " + tempDisabled + " Minutes";
				throw new ExceptionReporting(
						new DcemReporting(ReportAction.Activation, user, AppErrorCodes.USER_TEMPORARY_DISABLED, location, tempDisabledInfo), null, null);
			}
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.INVALID_ACTIVATION_CODE, location, tempDisabledInfo),
					null, null);
		}

		activationParam.setDeviceName(deviceLogic.getUniqueDeviceName(user, activationParam.getDeviceName()));
		// Check Licence
		try {
			licenceLogic.checkForLicence(AuthApplication.WebServices, false);
		} catch (DcemException exp) {
			AppErrorCodes errorCode = AsUtils.convertToAppErrorCodes(exp.getErrorCode());
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, errorCode, location, exp.getMessage()), null, exp);
		}

		/*
		 * Input is OK Now verifing the Version
		 */
		AppVersion appVersion = appSession.getAppVersion();
		if (activationParam.getCpuArch() == null) {
			activationParam.setCpuArch(CpuArch.unknown);
		}

		// RegVersion regVersion = versionLogic.getRegVersion(appVersion,
		// activationParam.getClientType(),
		// activationParam.getCpuArch());
		// if (regVersion == null) {
		AsVersionEntity versionEntity = versionLogic.getVersion(appVersion, activationParam.getClientType());
		ProductVersion pv = new ProductVersion(null, appVersion.version);
		if (versionEntity == null) {
			if (asModule.isEnableAppAutoVersionRegistration()) {
				versionEntity = new AsVersionEntity();
				versionEntity.setInformationUrl("auto-generated");
				versionEntity.setName(appVersion.getName());
				versionEntity.setClientType(activationParam.getClientType());
				versionEntity.setVersion(appVersion.getVersion());
				versionEntity.setVersionStr(pv.getVersionStr());
				versionEntity.setUser(null);
				versionLogic.addVersion(versionEntity);
			} else {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.INVALID_VERSION, location,
						activationParam.getClientType().name() + ": " + appVersion.getName() + "-" + pv.getVersionStr()), null, null);
			}
		}
		/*
		 * get PublicKey and Verify the signature
		 * 
		 */
		byte[] publicKeyBin = activationParam.getPublicKey();
		boolean verified = false;
		PublicKey publicKey = null;
		try {
			publicKey = SecureUtils.loadPublicKey(publicKeyBin);
			verified = SecureUtils.isVerifySignature(publicKey, activationParam.getUserId().getBytes(DcemConstants.CHARSET_UTF8),
					activationParam.getSignature());
		} catch (Exception exp) {
			throw new ExceptionReporting(
					new DcemReporting(ReportAction.Activation, user, AppErrorCodes.CSR_SIGNATURE, location, "userid=" + activationParam.getUserId()),
					exp.toString());
		}

		if (verified == false) {
			throw new ExceptionReporting(
					new DcemReporting(ReportAction.Activation, user, AppErrorCodes.CSR_SIGNATURE, location, "userid=" + activationParam.getUserId()),
					appSession.wsSession.getRemoteAddress(), null);
		}

		/*
		 * All Inputs are OK
		 * 
		 */

		// remove initial Pin
		try {
			userLogic.activationOk(user);
		} catch (DcemException e1) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.UNEXPECTED_ERROR, location, e1.toString()),
					appSession.wsSession.getRemoteAddress(), null);
		}
		if (activationCode.getInfo() != null && activationCode.getInfo().equals("debug") == false) {
			em.remove(activationCode);
		}
		DeviceEntity device = deviceLogic.addDevice(user, versionEntity, activationParam, publicKey);
		BigInteger serialNumber = BigInteger.valueOf(device.getId());
		X509Certificate signedCertificate;
		try {
			signedCertificate = SecureServerUtils.createCertificate(publicKey, asModule.getPrivateKey(), AsConstants.DCEM_AS_CA_ISSUER,
					"cn=" + user.getLoginId(), serialNumber, null, null);
		} catch (Exception e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.UNEXPECTED_ERROR, location, e.getMessage()), null, e);

		}
		ActivationResponse activationResponse = new ActivationResponse();
		activationResponse.setDeviceId(device.getId());
		activationResponse.setDeviceKey(device.getDeviceKey());
		activationResponse.setUpdateAvailable(false);
		activationResponse.setOfflineKey(device.getOfflineKey());
		try {
			activationResponse.setSignedCertificate(signedCertificate.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, user, AppErrorCodes.UNEXPECTED_ERROR, location, e.getMessage()), null, e);
		}

		reportingLogic.addReporting(new DcemReporting(ReportAction.Activation, device.getUser(), null, location, "device: " + device.getName()));
		activationResponse.setDeviceName(activationParam.getDeviceName());
		return activationResponse;
	}

	/**
	 * @param activationParam
	 * @return
	 * @throws AppException
	 * @throws TException
	 */
	public LoginResponse login(LoginParam loginParam) throws ExceptionReporting, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		String location = appSession.wsSession.getRemoteAddress();
		if (appSession.getState() == ConnectionState.invalidTenant) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login, (DcemUser) null, AppErrorCodes.INVALID_TENANT_IDENTIFIER, location, null), null);
		}
		DeviceEntity deviceDetached = deviceLogic.getDeviceDetached(loginParam.deviceId);
		if (deviceDetached == null) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login, (DcemUser) null, AppErrorCodes.INVALID_DEVICE_ID, location,
					"DeviceId = " + Integer.toString(loginParam.deviceId)), location);
		}
		if (appSession.getState() != ConnectionState.serverSignature && appSession.getState() != ConnectionState.loggedIn) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login, deviceDetached.getUser(), AppErrorCodes.INCORRECT_STATE, location,
					"Login while in state: " + appSession.getState().name()), null);
		}

		if (deviceDetached.getDeviceKey() == null) {
			throw new ExceptionReporting(
					new DcemReporting(ReportAction.Login, deviceDetached.getUser(), AppErrorCodes.INVALID_DEVICE_KEY, location, deviceDetached.getName()),
					null);
		}
		if (deviceDetached.getState() != DeviceState.Enabled) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login, deviceDetached.getUser(), AppErrorCodes.DEVICE_DISABLED, location,
					"device name: " + deviceDetached.getName()), null);
		}
		AsVersionEntity deviceVersionEntity = deviceDetached.getAsVersion();
		if (deviceVersionEntity.isTestApp() == false) {
			if (Arrays.equals(loginParam.getUdid(), deviceDetached.getUdid()) == false) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Login, deviceDetached.getUser(), AppErrorCodes.INVALID_UDID, location,
						"device: " + deviceDetached.getName()), location);
			}
		}

		DcemUser dcemUser = deviceDetached.getUser();
		if (dcemUser.isDisabled()) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login, dcemUser, AppErrorCodes.USER_DISABLED, location, null), null, null);
		}
		appSession.setDevice(deviceDetached);
		ReportAction reportAction = ReportAction.Login;

		if (loginParam.getSessionCookie() != null) {
			if (fingerprintLogic.verifyFingerprint(dcemUser.getId(), DcemConstants.FINGERPRINT_ID_FOR_APP, loginParam.getSessionCookie()) == false) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Login_reconnect, deviceDetached.getUser(),
						AppErrorCodes.INVALID_AUTH_SESSION_COOKIE, location, "device: " + deviceDetached.getName()), location);
			}
			reportAction = ReportAction.Login_reconnect;
		} else if (loginParam.isPasswordLess()) {
			if (deviceVersionEntity.isTestApp() == false) {
				reportAction = ReportAction.LoginPasswordLess;
				try {
					deviceLogic.verifyDevicePasscode(deviceDetached, KaraUtils.byteArrayToInt(loginParam.getEncPassword()));
				} catch (DcemException exp) {
					throw new ExceptionReporting(
							new DcemReporting(ReportAction.VerifyPasscode, dcemUser, AppErrorCodes.INVALID_PASSCODE, location, exp.toString()), location, exp);
				}
			}
		} else {
			verifyUserPassword(dcemUser, appSession, loginParam.getEncPassword(), ReportAction.Login);
		}

		deviceDetached.setLastLoginTime(LocalDateTime.now());
		if (loginParam.locale != null) {
			deviceDetached.setLocale(loginParam.locale.substring(0, 2));
		}
		deviceDetached.setRetryCounter(0);

		// Check Licence
		try {
			licenceLogic.checkForLicence(AuthApplication.WebServices, true);
		} catch (DcemException e) {
			throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AsUtils.convertToAppErrorCodes(e.getErrorCode()), location,
					"device: " + deviceDetached.getName() + ", error: " + e.getMessage()), null, e);
		}

		/*
		 * Input is OK Now verifing the Version
		 */
		AppVersion appVersion = appSession.getAppVersion();
		boolean updateVersion = false;
		if (appSession.getCommClientType() == CommClientType.APP && appVersion.getVersion() != deviceVersionEntity.getVersion()) {
			// up or down grade
			// RegVersion regVersion = versionLogic.getRegVersion(appVersion,
			// versionEntity.getClientType(),
			// loginParam.getCpuArch());
			AsVersionEntity newVersionEntity = versionLogic.getVersion(appVersion, deviceVersionEntity.getClientType());
			if (newVersionEntity == null) {
				ProductVersion pv = new ProductVersion(null, appVersion.version);
				if (asModule.isEnableAppAutoVersionRegistration()) {
					newVersionEntity = new AsVersionEntity();
					newVersionEntity.setInformationUrl("Auto generated at login");
					newVersionEntity.setName(appVersion.getName());
					newVersionEntity.setClientType(deviceVersionEntity.getClientType());
					newVersionEntity.setVersion(appVersion.getVersion());
					newVersionEntity.setVersionStr(pv.getVersionStr());
					newVersionEntity.setUser(null);
					versionLogic.addVersion(newVersionEntity);
					deviceDetached.setAsVersion(newVersionEntity);
				} else {
					throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, deviceDetached.getUser(), AppErrorCodes.INVALID_VERSION, location,
							deviceVersionEntity.getClientType().name() + ": " + appVersion.getName() + "-" + pv.getVersionStr()), null, null);
				}
			}
			deviceVersionEntity = newVersionEntity;
			deviceDetached.setAsVersion(deviceVersionEntity);
			updateVersion = true;
		}

		if (deviceVersionEntity.isDisabled()) {
			throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AppErrorCodes.VERSION_DISABLED, location,
					"device: " + deviceDetached.getName() + ", " + appVersion.getName() + "-" + KaraUtils.versionToString(appVersion)), null, null);
		}
		if ((deviceVersionEntity.getExpiresOn() != null && deviceVersionEntity.getExpiresOn().isBefore(LocalDateTime.now()))) {
			throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AppErrorCodes.VERSION_UPDATED_REQUIRED, location,
					"device: " + deviceDetached.getName() + ", " + appVersion.getName() + "-" + deviceVersionEntity.getVersionStr()), null, null);
		}
		if (updateVersion) {
			deviceLogic.updateDeviceVersion(deviceDetached);
		}

		LoginResponse loginResponse = new LoginResponse();
		if (deviceVersionEntity.getExpiresOn() != null) {
			loginResponse.setUpdateAvailableTill(deviceVersionEntity.getExpiresOn().toEpochSecond(ZoneOffset.UTC) * 1000);
		}
		if (loginParam.getCommClientType() != null && loginParam.getCommClientType() == CommClientType.DCEM_AS_CLIENT) {
			if (dispatcherModule == null) {
				throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AppErrorCodes.NOT_A_DISPATCHER_PROXY, location,
						"device: " + deviceDetached.getName() + ", domain: " + appSession.getDomainName()), null);
			}

			DispatcherApi dispatcherApi = CdiUtils.getReference(AsConstants.DISPATCHER_IMPL_CLASS);
			RegisteredDomain registeredDomain = dispatcherApi.getProxyDomain(appSession.getDomainName());
			if (registeredDomain == null) {
				AppWsConnection.getInstance().addRpReport(new ReverseProxyReport(appSession.getDomainName(), "Login", AppErrorCodes.UNREGISTERED_DCEM.name(),
						appSession.getDomainName(), appSession.wsSession.getRemoteAddress()));
				throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AppErrorCodes.UNREGISTERED_DCEM, location,
						"device: " + deviceDetached.getName() + ", domain: " + appSession.getDomainName()), null);
			}
			dispatcherApi.setOnline(registeredDomain.getRegistrationEntityId());
			appSession.setState(ConnectionState.rpDcemLoginProcess);
		} else {
			appSession.state = loginParam.isPasswordLess() ? ConnectionState.loggedInPasswordLess : ConnectionState.midLogin;
		}

		if (dcemUser.isDomainUser()) {
			loginResponse.setLdapUser(true);
		}

		LicenceKeyContent licenceKeyContent;
		try {
			licenceKeyContent = licenceLogic.getLicenceKeyContent();
		} catch (DcemException e) {
			throw new ExceptionReporting(new DcemReporting(reportAction, deviceDetached.getUser(), AppErrorCodes.LICENCE_EXPIRED, location,
					"device: " + deviceDetached.getName() + ", domain: " + appSession.getDomainName()), null);
		}
		appSession.setDevice(deviceDetached);
		loginResponse.setPasscodeValidFor(asModule.getPreferences().getPasscodeValidFor());
		loginResponse.setLicenceExpiresOn(licenceKeyContent.getExpiresOn().getTime());
		loginResponse.setTestLicence(licenceKeyContent.isTrialVersion());
		appSession.setDeviceId(deviceDetached.getId());
		appSession.setUserId(dcemUser.getId());
		if (loginParam.isPasswordLess() == false) {
			String oneTimePassword = Integer.toString(deviceDetached.getId()) + "z" + RandomUtils.generateRandomAlphaNumericString(64);
			loginResponse.setOneTimePassword(oneTimePassword);
			appSession.setOneTimePassword(oneTimePassword);
			loginResponse.setDeviceKey(deviceDetached.getDeviceKey());
			loginResponse.keepAliveSeconds = asModule.getPreferences().getKeepAliveConnection();

			if (asModule.getModulePreferences().getAppReLoginWithin() > 0) {
				String sessionCookie = null;
				FingerprintId fpId = new FingerprintId(dcemUser.getId(), DcemConstants.FINGERPRINT_ID_FOR_APP);
				if (loginParam.getSessionCookie() != null) {
					sessionCookie = loginParam.getSessionCookie(); // use the same fingerprint
				} else {
					sessionCookie = RandomUtils.generateRandomAlphaNumericString(32);
				}
				UserFingerprintEntity fingerprintEntity = new UserFingerprintEntity(fpId, sessionCookie, asModule.getModulePreferences().getAppReLoginWithin());
				fingerprintLogic.updateFingerprint(fingerprintEntity);
				loginResponse.setSessionCookie(sessionCookie);
				loginResponse.setSessionCookieExpiresOn((int) (fingerprintEntity.getTimestamp().toEpochSecond(ZoneOffset.UTC)));
			} else {
				loginResponse.setSessionCookie(null);
				loginResponse.setSessionCookieExpiresOn(0);
			}

		} else {
			loginResponse.keepAliveSeconds = deviceLoggedIn(appSession, deviceDetached, true);
		}
		if (adminModule.getPreferences().isReportErrorsOnly() == false) {
			reportingLogic.addReporting(new DcemReporting(reportAction, deviceDetached.getUser(), null, location, "device: " + deviceDetached.getName()));
		}
		return loginResponse;
	}

	/**
	 * @param signaturenParam
	 * @return
	 * @throws ExceptionReporting
	 * @throws TException
	 */
	public SignatureResponse clientSignature(SignatureParam signaturenParam) throws ExceptionReporting, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		String location = appSession.wsSession.getRemoteAddress();
		if (appSession.getState() != ConnectionState.midLogin && appSession.getState() != ConnectionState.rpDcemLoginProcess) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, location,
					Integer.toString(appSession.getDeviceId())), location);
		}

		DeviceEntity deviceDetached = appSession.getDevice();
		if (deviceDetached == null) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.INVALID_DEVICE_ID, location,
					Integer.toString(appSession.getDeviceId())), location);
		}
		/*
		 * Verify the Client Signature
		 */
		try {
			if (SecureUtils.isVerifySignature(SecureUtils.loadPublicKey(deviceDetached.getPublicKey()), appSession.getOneTimePassword().getBytes("UTF-8"),
					signaturenParam.getClientSignature()) == false) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.INVALID_CLIENT_SIGNATURE, location,
						Integer.toString(appSession.getDeviceId())), location);

			}
		} catch (Exception e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, location,
					Integer.toString(appSession.getDeviceId())), null);
		}

		SignatureResponse signatureResponse = new SignatureResponse();
		signatureResponse.keepAliveSeconds = deviceLoggedIn(appSession, deviceDetached, false);
		return signatureResponse;
	}

	private int deviceLoggedIn(AppSession appSession, DeviceEntity deviceDetached, boolean passwordLess) {
		int keepAlive = asModule.getPreferences().getKeepAliveConnection();
		appSession.getWsSession().setMaxIdleTimeout(keepAlive * 1000);

		// // signatureResponse.setReconnectTicket(reconnectTicket);
		// signatureResponse.setKeepAliveSeconds(keepAlive);

		deviceDetached.setStatus(passwordLess == false ? DeviceStatus.Online : DeviceStatus.OnlinePasswordLess);
		deviceDetached.setNodeId(DcemCluster.getInstance().getDcemNode().getId());
		deviceLogic.setDeviceOnline(deviceDetached);

		AppSession appSessionPre = asModule.getTenantData().getDeviceSessions().get(deviceDetached.getId());
		if (appSessionPre != null) {
			// WOW this device has two Sessions !!!
			logger.warn("Device has a previos Session! Device=" + appSessionPre.getDevice().toString());
			try {
				appSessionPre.getWsSession().close(null);
			} catch (IOException e) {
				logger.warn("Couln't close session for previous Device - " + appSessionPre.getDevice().toString());
			}
		}
		appSession.setTimeStamp(new Date().getTime());
		AsTenantData tenantData = asModule.getTenantData();
		tenantData.getDeviceSessions().put(deviceDetached.getId(), appSession);
		if (appSession.getCommClientType() == CommClientType.DCEM_AS_CLIENT) {
			ReverseProxyConnections.add(appSession);
			AppWsConnection.getInstance().addRpReport(new ReverseProxyReport(appSession.getDomainName(), "Login", ReverseProxyReport.RESULT_OK,
					appSession.getDomainName(), appSession.wsSession.getRemoteAddress()));
			appSession.setState(ConnectionState.rpDcemLoggedIn);
		} else {
			if (logger.isDebugEnabled()) {
				String userDeviceId = "null";
				if (appSession.getDevice() != null) {
					userDeviceId = appSession.getDevice().getUser().getLoginId() + ":" + appSession.getDevice().getName();
				}
				logger.debug("Device Logged in User:Device=" + userDeviceId + ", State: " + appSession.state);
			}
			appSession.setState(appSession.getState() == ConnectionState.midLogin ? ConnectionState.loggedIn : ConnectionState.loggedInPasswordLess);
			taskExecutor.schedule(new CheckMessageTask(appSession, TenantIdResolver.getCurrentTenant(), tenantData), 500, TimeUnit.MILLISECONDS);
		}
		return keepAlive;
	}

	/**
	 * @param serverSignatureParam
	 * @return
	 * @throws ExceptionReporting
	 */
	public ServerSignatureResponse serverSignature(ServerSignatureParam serverSignatureParam) throws AppException, ExceptionReporting {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		String location = appSession.wsSession.getRemoteAddress();
		appSession.setAppVersion(serverSignatureParam.getAppVersion());
		appSession.setLibVersion(serverSignatureParam.getLibVersion());

		ServerSignatureResponse serverSignatureResponse = new ServerSignatureResponse();
		ProductVersion pv = applicationBean.getProductVersion();
		AppVersion appServerVersion = new AppVersion(pv.getVersionInt(), pv.getAppName(), pv.getState());
		serverSignatureResponse.setServerVersion(appServerVersion);
		try {
			byte[] digitalSignature = SecureUtils.sign(asModule.getPrivateKey(), serverSignatureParam.getDataForSignature());
			serverSignatureResponse.setServerSignature(digitalSignature);
		} catch (Exception e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, ((DcemUser) null), AppErrorCodes.UNEXPECTED_ERROR, location,
					"Signing is not possible: " + e.getMessage()), null);
		}
		appSession.setState(ConnectionState.serverSignature);
		appSession.setDomainName(serverSignatureParam.domainName);
		appSession.setCommClientType(serverSignatureParam.getCommClientType());
		if (appSession.getCommClientType() == null) {
			appSession.setCommClientType(CommClientType.APP);
		}
		RegisteredDomain registeredDomain = null;
		if (dispatcherModule != null) {
			registeredDomain = rpServerSignature(serverSignatureParam, appSession);
			if (registeredDomain != null) {
				if (registeredDomain.isReverseProxy()) {
					serverSignatureResponse.isReverseProxy = true;
				} else {
					serverSignatureResponse.isDispatched = true;
				}
			}
		} else {
			// Attention CommClientType() may be null on old 1.3.1 apps
			if (serverSignatureParam.getCommClientType() != null && serverSignatureParam.getCommClientType() == CommClientType.DCEM_AS_CLIENT) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, ((DcemUser) null), AppErrorCodes.NOT_A_DISPATCHER_PROXY, location,
						appSession.getWsSession().getRemoteAddress()), null);
			}
		}
		if (registeredDomain == null && applicationBean.isMultiTenant()) {
			String domainName = appSession.domainName;
			TenantEntity tenantEntity;
			if (domainName != null && domainName.isEmpty() == false) {
				String tenantIdentifier = null;
				int ind = domainName.indexOf(AppSystemConstants.TENANT_SEPERATOR);
				if (ind != -1) {
					tenantIdentifier = domainName.substring(ind + 1);
					tenantEntity = applicationBean.getTenant(tenantIdentifier);
					if (tenantEntity == null) {
						throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, ((DcemUser) null),
								AppErrorCodes.INVALID_TENANT_IDENTIFIER, location, " Domain=" + domainName), null);
						// logger.warn("Invalid Tenant Identifier for: " + domainName);
					}

				} else {
					tenantEntity = TenantIdResolver.getMasterTenant();
				}
				appSession.setTenantEntity(tenantEntity);
			}
		}
		return serverSignatureResponse;
	}

	public Template getTemplateFromId(int id) {
		DcemTemplate asTempalte = templateLogic.getTemplate(id);
		Template template = new Template(asTempalte.getId(), asTempalte.getName(), asTempalte.getLanguage().getLocale().getLanguage());
		template.setContent(asTempalte.getContent());
		return template;
	}

	public FlakeIdGenerator getMsgIdGenerator() {
		return asModule.getTenantData().getMsgIdGenerator();
	}

	public IMap<Long, PendingMsg> getPendingMsgs() {
		return asModule.getTenantData().getPendingMsgs();
	}

	/**
	 * @param operatorId
	 * @param sessionId
	 * @return
	 * @throws DcemException
	 */
	public RequestLoginQrCodeResponse generateLoginQrCode(String operatorId, String sessionId) throws DcemException {
		// System.out.println("AppServices.generateLoginQrCode() " + sessionId);
		int timeout = asModule.getPreferences().getLoginQrCodeResponseTimeout();
		String nonce = RandomUtils.generateRandomAlphaNumericString(32);
		LoginQrCode loginQrCode = new LoginQrCode(nonce, timeout);

		LoginQrCodeContent codeContent = new LoginQrCodeContent(operatorId, sessionId, nonce);
		String qrCodeData;
		try {
			String jsonEncode = objectMapper.writeValueAsString(codeContent);
			byte[] data = SecureServerUtils.encryptDataCommon(jsonEncode.getBytes(DcemConstants.CHARSET_UTF8));
			qrCodeData = java.util.Base64.getEncoder().encodeToString(data);

		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.QRCODE_GENERATION_FAILED, exp.getMessage());
		}
		asModule.getTenantData().getLoginQrCodes().set(operatorId + "\t" + sessionId, loginQrCode, timeout + 10, TimeUnit.SECONDS);
		return new RequestLoginQrCodeResponse(qrCodeData, timeout);
	}

	/**
	 * @param operatorId
	 * @param sessionId
	 * @param waitTimeSeconds
	 * @param pollOnly
	 * @return
	 * @throws DcemException
	 */
	public QueryLoginResponse queryLoginQrCode(String operatorId, String sessionId, boolean pollOnly, int waitTimeMilliSec) throws DcemException {
		String qrKey = operatorId + "\t" + sessionId;
		IMap<String, LoginQrCode> loginQrCodes = asModule.getTenantData().getLoginQrCodes();
		LoginQrCode loginQrCode = loginQrCodes.get(qrKey);
		if (loginQrCode == null) {
			throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_NOT_FOUND, "");
		}
		if (waitTimeMilliSec > 0 && loginQrCode.getUserName() == null) {
			while (waitTimeMilliSec > 0) {
				try {
					Thread.sleep(250);
					waitTimeMilliSec = waitTimeMilliSec - 250;
				} catch (InterruptedException e) {
					break;
				}
				loginQrCode = loginQrCodes.get(qrKey);
				if (loginQrCode == null) {
					throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_NOT_FOUND, "");
				}
				if (loginQrCode.getUserName() != null) {
					break;
				}
			}
		}
		if (loginQrCode.getUserName() != null && pollOnly == false) {
			DcemUser dcemUser = userLogic.getUser(loginQrCode.getUserName());
			DcemReporting asReporting = new DcemReporting(ReportAction.Authenticate_qrcode, dcemUser, null, null, null);
			reportingLogic.addReporting(asReporting);
			loginQrCodes.delete(operatorId + "\t" + sessionId);
		}
		return new QueryLoginResponse(loginQrCode.getUserName(), loginQrCode.getDeviceName());
	}

	/**
	 * @param data
	 * @param appSession
	 * @throws DcemException
	 */
	public void sendLogonQrCode(String data, AppSession appSession) throws DcemException {

		byte[] binary = java.util.Base64.getDecoder().decode(data);
		LoginQrCodeContent codeContent = null;
		try {
			binary = SecureServerUtils.decryptDataCommon(binary);
			codeContent = objectMapper.readValue(new String(binary, DcemConstants.CHARSET_UTF8), LoginQrCodeContent.class);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_FAILED, e.getMessage());
		}
		IMap<String, LoginQrCode> loginQrCodes = asModule.getTenantData().getLoginQrCodes();
		LoginQrCode loginQrCode = loginQrCodes.get(codeContent.getKey());
		if (loginQrCode == null) {
			throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_NOT_FOUND, null);
		}
		if (loginQrCode.getUserName() != null) {
			throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_CONSUMED, "QrCode already consumed by " + loginQrCode.getUserName());
		}
		if (codeContent.getNonce().equals(loginQrCode.getNonce()) == false) {
			throw new DcemException(DcemErrorCodes.LOGIN_QR_CODE_INVALID, "Invalid nonce");
		}
		loginQrCode.setUserName(appSession.getDevice().getUser().getLoginId());
		loginQrCode.setDeviceName(appSession.getDevice().getName());
		loginQrCodes.set(codeContent.getKey(), loginQrCode, loginQrCode.getTimeout(), TimeUnit.SECONDS);
	}

	public QueryLoginResponse queryLoginOtp(String otp) throws DcemException {

		if (otp == null) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP, "OTP is null");
		}
		int ind = otp.indexOf('z');
		if (ind == -1) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP, "invalid");

		}

		return null;
	}

	@DcemTransactional
	public void changePassword(byte[] encPassword, byte[] newEncPassword, AppSession appSession) throws DcemException {

		DeviceEntity device = appSession.getDevice();
		int userId = device != null ? appSession.getDevice().getUser().getId() : appSession.getUserId();
		DcemUser user = userLogic.getUser(userId);
		if (user == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, "User ID: " + userId);
		}

		try {
			byte[] passwordBytes = SecureServerUtils.decryptData(appSession.passwordEncryptionKey, encPassword);
			String password = new String(passwordBytes, Charsets.UTF_8);
			byte[] newPasswordBytes = SecureServerUtils.decryptData(appSession.passwordEncryptionKey, newEncPassword);
			String newPassword = new String(newPasswordBytes, Charsets.UTF_8);
			userLogic.changePassword(user, password, newPassword);
		} catch (DcemException e) {
			throw e;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
		}
		DcemReporting reporting;
		if (device != null) {
			device.setUser(user);
			reporting = new DcemReporting(ReportAction.ChangePassword, user, null, null, device.getName());
		} else {
			reporting = new DcemReporting(ReportAction.ChangePassword, user, null, null, null);
		}
		reportingLogic.addReporting(reporting);
	}

	public void setDeviceDisconnect(DeviceEntity device) {
		deviceLogic.setDeviceOff(device);
		fingerprintLogic.deleteFingerPrint(device.getUser().getId(), DcemConstants.FINGERPRINT_ID_FOR_APP);
	}

	/**
	 * @param uniqueKey
	 * @param detachedDevice
	 * @return
	 * @throws ExceptionReportingflogin
	 * 
	 */
	public SdkCloudSafe getCloudSafe(SdkCloudSafeKey uniqueKey, DeviceEntity detachedDevice, int userId, int libVersion)
			throws DcemException, ExceptionReporting {
		CloudSafeEntity cloudSafeEntity = getCloudSafeEntityFromKey(uniqueKey, userId);
		uniqueKey.setName(cloudSafeEntity.getName());
		uniqueKey.setDbId(cloudSafeEntity.getId());
		if (libVersion < AsConstants.LIB_VERION_2) { // For backward compatible with old appVersion 2.5
			uniqueKey.setOwner(CloudSafeOwner.USER); // For backward compatible with old appVersion 2.5
		}
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.GROUP) {
			uniqueKey.setGroupName(cloudSafeEntity.getGroup().getName());
		}
		byte[] content = cloudSafeLogic.getContentAsBytes(cloudSafeEntity, null, userLogic.getUser(userId));
		return new SdkCloudSafe(uniqueKey, content != null ? ByteBuffer.wrap(content) : null, cloudSafeEntity.getOptions(),
				cloudSafeEntity.getDiscardAfterAsLong(),
				cloudSafeEntity.getLastModified() != null ? (cloudSafeEntity.getLastModified().toEpochSecond(ZoneOffset.UTC) * 1000) : 0, null,
				cloudSafeEntity.getLength(), null, cloudSafeEntity.isWriteAccess(), cloudSafeEntity.isRestrictDownload());
	}

	/**
	 * @param sdkCloudSafe
	 * @param detachedDevice
	 * @throws DcemException
	 */
	public LocalDateTime setCloudSafe(SdkCloudSafe sdkCloudSafe, DeviceEntity detachedDevice, int userId) throws Exception {
		// Check user
		DcemUser user = (detachedDevice != null) ? detachedDevice.getUser() : userLogic.getUser(userId);
		if (user == null) {
			throw new ExceptionReporting(
					new DcemReporting(ReportAction.WriteCloudSafe, (DcemUser) null, AppErrorCodes.INVALID_USERID, null, "User ID " + userId),
					"User with ID: " + userId + "not found.");
		}
		CloudSafeOwner owner = sdkCloudSafe.getUniqueKey().getOwner();
		String fileName = sdkCloudSafe.getUniqueKey().getName();
		CloudSafeEntity parent = null;
		DcemUser fileOwner = user;
		boolean isShared = fileName.contains(AsConstants.SHARE_BY_SEPERATOR);
		CloudSafeEntity cloudSafeFromDb = null;
		try {
			cloudSafeFromDb = cloudSafeLogic.getOwnedOrSharedCloudSafeFromPath(user, sdkCloudSafe.getUniqueKey().getDbId(), fileName);
			if (isShared && cloudSafeFromDb.isWriteAccess() == false) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.WriteCloudSafe, user, AppErrorCodes.NO_WRITE_ACCESS, null, fileName),
						user.getLoginId() + " does not have access to overwrite " + fileName);
			} else {
				fileOwner = cloudSafeFromDb.getUser();
				fileName = cloudSafeFromDb.getName();
				parent = cloudSafeFromDb.getParent();
			}
		} catch (DcemException e) { // not found
			if (isShared) {
				throw new ExceptionReporting(
						new DcemReporting(ReportAction.WriteCloudSafe, user, AsUtils.convertToAppErrorCodes(e.getErrorCode()), null, fileName),
						"Exception while getting shared CloudSafe data.");
			} else if (fileName.contains(CloudSafeLogic.FOLDER_SEPERATOR)) {
				parent = cloudSafeLogic.makeDirectories(null, fileName, user, null); // create directories
				fileName = fileName.substring(fileName.lastIndexOf(CloudSafeLogic.FOLDER_SEPERATOR) + 1);
			} else {
				parent = cloudSafeLogic.getCloudSafeRoot();
			}
		}
		// Check owner
		switch (owner) {
		case GLOBAL:
			throw new ExceptionReporting(new DcemReporting(ReportAction.WriteCloudSafe, user, AppErrorCodes.INVALID_CLOUD_SAFE_OWNER, null, owner.name()),
					"Cannot set Global Cloud Data.");
		case DEVICE:
			if (detachedDevice == null) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.WriteCloudSafe, user, AppErrorCodes.INVALID_CLOUD_SAFE_OWNER, null, owner.name()),
						"Cannot set Device Cloud Data to a non-device user.");
			}
			break;
		default:
			break;
		}
		LocalDateTime discardAfter = (sdkCloudSafe.getDiscardAfter() > 0) ? DcemUtils.convertEpoch(sdkCloudSafe.getDiscardAfter()) : null;
		LocalDateTime lastModified = (sdkCloudSafe.getLastModified() > 0) ? DcemUtils.convertEpoch(sdkCloudSafe.getLastModified()) : null;

		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(owner, fileOwner, owner == CloudSafeOwner.USER ? null : detachedDevice, fileName, discardAfter,
				sdkCloudSafe.getOptions(), false, parent);
		cloudSafeEntity.setLastModified(lastModified);
		try {
			/*
			 */
			DmModuleApi dmModuleApi = null;
			try {
				dmModuleApi = (DmModuleApi) CdiUtils.getReference(AsConstants.DM_MODULE_API_IMPL_BEAN);
			} catch (Exception e) {
				// TODO: handle exception
			}
			CloudSafeEntity newEntity;
			if (dmModuleApi != null) {
				File file = File.createTempFile(AsConstants.DOUBLE_CLUE_DM, "");
				Files.write(sdkCloudSafe.getContent(), file);			
				UploadDocument uploadDocument = new UploadDocument(fileName, file, parent, FileUploadDetector.getMediaType(fileName, file));
				newEntity = dmModuleApi.saveNewDocument(uploadDocument, user, null);
				file.delete();
			} else {
				newEntity = cloudSafeLogic.setCloudSafeByteArray(cloudSafeEntity, null, sdkCloudSafe.getContent(), userLogic.getUser(userId), cloudSafeFromDb);
			}
			return newEntity.getLastModified();
		} catch (DcemException exp) {
			AppErrorCodes appErrorCode = null;
			try {
				appErrorCode = AppErrorCodes.valueOf(AppErrorCodes.class, exp.getErrorCode().name());
				throw new ExceptionReporting(new DcemReporting(ReportAction.WriteCloudSafe, user, appErrorCode, null, exp.getMessage()), null, exp);
			} catch (Exception exp2) {
				throw exp2;
			}
		} catch (Exception exp) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.WriteCloudSafe, user, AppErrorCodes.UNEXPECTED_ERROR, null, exp.toString()), null,
					null);
		}
	}

	public void deactivate(AppSession appSession) throws ExceptionReporting {
		try {
			List<DeviceEntity> list = new LinkedList<>();
			list.add(appSession.getDevice());
			deviceLogic.deleteDevices(list, null, false);
			asMessageHandler.disconnectedPendingMsg(appSession, AsApiMsgStatus.DISCONNECTED);
		} catch (Exception e) {
			logger.warn(e);
			throw new ExceptionReporting(new DcemReporting(ReportAction.Deactivation, appSession.getDevice().getUser(), AppErrorCodes.UNEXPECTED_ERROR, null,
					"device: " + appSession.getDevice().getName() + ", error: " + e), null, null);
		}
		reportingLogic
				.addReporting(new DcemReporting(ReportAction.Deactivation, appSession.getDevice().getUser(), null, null, appSession.getDevice().getName()));
	}

	/**
	 * @param dcemUser
	 * @return
	 * @throws ExceptionReporting
	 */
	public RequestActivationCodeResponse requestActivationCode(DcemUser dcemUser) throws ExceptionReporting {
		try {
			String ac = activationLogic.requestActivationCode(dcemUser);
			reportingLogic.addReporting(new DcemReporting(ReportAction.RequestActivationCode, dcemUser, null, null, null));
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, asModule.getPreferences().getRequestActivationCodeValidTill());
			return new RequestActivationCodeResponse(ac, calendar.getTimeInMillis());
		} catch (Exception e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Deactivation, dcemUser, AppErrorCodes.UNEXPECTED_ERROR, null, e.toString()), null,
					null);
		}

	}

	public void verifyPassword(byte[] encPassword, AppSession appSession) throws ExceptionReporting {
		verifyUserPassword(appSession.getDevice().getUser(), appSession, encPassword, ReportAction.VerifyPassword);
	}

	public int getPendingMsgsCount() {
		return asModule.getTenantData().getPendingMsgs().size();
	}

	public int getloginQrCodesCount() {
		return asModule.getTenantData().getLoginQrCodes().size();
	}

	/**
	 * @param domainSdkConfigParam
	 * @return
	 * @throws ExceptionReporting
	 * @throws DcemException
	 * @throws AppException
	 */
	public DomainSdkConfigResponse getDomainSdkConfig(DomainSdkConfigParam domainSdkConfigParam) throws ExceptionReporting, AppException, DcemException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		String location = appSession.wsSession.getRemoteAddress();
		if (appSession.getState() == ConnectionState.invalidTenant) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.INVALID_TENANT_IDENTIFIER, location, null),
					null);
		}
		if (appSession.getState() != ConnectionState.serverSignature) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.GetDomainSdkConfig, (DcemUser) null, AppErrorCodes.INCORRECT_STATE, location, null),
					null);
		}
		DomainSdkConfigResponse domainSdkConfigResponse = new DomainSdkConfigResponse();
		// are we the dispatcher?
		if (dispatcherModule != null) {
			DispatcherApi dispatcherApi = CdiUtils.getReference(AsConstants.DISPATCHER_IMPL_CLASS);
			TenantIdResolver.setMasterTenant(); // Dispatcher always aon Master
			domainSdkConfigResponse = dispatcherApi.getDomainSdkConfig(domainSdkConfigParam, location);
		} else {
			// Running on DCEM
			String userId = domainSdkConfigParam.getUserId();
			int ind = domainSdkConfigParam.domainName.lastIndexOf(AppSystemConstants.TENANT_SEPERATOR);
			if (ind != -1) {
				// with Tenant
				String tenantName = domainSdkConfigParam.domainName.substring(ind + 1);
				TenantEntity tenantEntity = applicationBean.getTenant(tenantName);
				if (tenantEntity == null) {
					throw new ExceptionReporting(
							new DcemReporting(ReportAction.GetDomainSdkConfig, (DcemUser) null, AppErrorCodes.INVALID_TENANT_IDENTIFIER, location, null), null);
				}
				TenantIdResolver.setCurrentTenant(tenantEntity);
			}
			DcemUser user = userLogic.getUser(userId);
			if (user == null) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.GetDomainSdkConfig, (DcemUser) null, AppErrorCodes.INVALID_USERID, location,
						"userid=" + domainSdkConfigParam.getUserId()), null);
			}
			int userState = userLogic.isUserEnabled(user);
			if (userState != 0) {
				if (userState == 1) {
					throw new ExceptionReporting(new DcemReporting(ReportAction.GetDomainSdkConfig, user, AppErrorCodes.USER_DISABLED, location, null), null,
							null);
				} else {
					throw new ExceptionReporting(
							new DcemReporting(ReportAction.GetDomainSdkConfig, user, AppErrorCodes.USER_TEMPORARY_DISABLED, location, null), null);
				}
			}

			if (domainSdkConfigParam.getActivationCode() != null) {
				ActivationCodeEntity activationCode = activationLogic.validateActivationCode(user, domainSdkConfigParam.getActivationCode());
				if (activationCode == null) {
					String tempDisabledInfo = null;
					int tempDisabled = userLogic.incFailActivations(user, asModule.getPreferences().getMaxFailActivations(),
							asModule.getPreferences().getRetryActivationDelayinMinutes());
					if (tempDisabled > 0) {
						tempDisabledInfo = "Temporary disabled for " + tempDisabled + " Minutes";
						throw new ExceptionReporting(
								new DcemReporting(ReportAction.GetDomainSdkConfig, user, AppErrorCodes.USER_TEMPORARY_DISABLED, location, tempDisabledInfo),
								null, null);
					}
					throw new ExceptionReporting(
							new DcemReporting(ReportAction.GetDomainSdkConfig, user, AppErrorCodes.INVALID_ACTIVATION_CODE, location, tempDisabledInfo), null,
							null);
				}
			}
			try {
				byte[] sdkConfig = null;
				DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(AsModule.MODULE_ID, DcemConstants.CONFIG_KEY_SDK_CONFIG);
				if (dcemConfiguration == null) {
					// Now we try to get it from file. Incase it is an old version 2.3.2
					FileInputStream fileInputStream = new FileInputStream(LocalPaths.getCacheSdkConfigFile());
					sdkConfig = KaraUtils.readInputStream(fileInputStream);
					fileInputStream.close();
				} else {
					sdkConfig = dcemConfiguration.getValue();
				}
				domainSdkConfigResponse.setSdkConfigDcem(sdkConfig);
			} catch (Exception e) {
				logger.warn("Couldn't load the cached SdkConfig.dcem. You should download an SdkConfig file from 'Versions' view.");
				return domainSdkConfigResponse;
			}
		} // Running on Domain
		return domainSdkConfigResponse;
	}

	public PublicKey getDispatcherPublicKey() {
		return asModule.getDispatcherPublicKey();
	}

	public String getClusterId() throws DcemException {
		return configLogic.getClusterConfig().getName();
	}

	public void setDispatcherPreferences(String realmName, String pnKey) throws DcemException {

		try {
			AsPreferences modulePreferencesClone = (AsPreferences) asModule.getPreferences().clone();
			AsPreferences modulePreferencesPrevious = (AsPreferences) asModule.getPreferences().clone();

			modulePreferencesClone.setRealmName(realmName);
			// modulePreferencesClone.setDispatcherFirebaseCloudMessagingKey(pnKey);
			String moduleId = asModule.getId();
			configLogic.setModulePreferencesInCluster(moduleId, modulePreferencesPrevious, modulePreferencesClone);
			// EG: Dispatche does not send PN key anymore
			// PushNotificationConfig pushNotificationConfig = new PushNotificationConfig();
			// pushNotificationConfig.setGoogleServiceFile(pnKey);
			// fcmLogic.writeConfiguration(pushNotificationConfig);

		} catch (CloneNotSupportedException e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Could not save Dispatcher preferences because object cloning is not supported.");
		} catch (DcemException e) {
			throw e;
		}
	}

	private RegisteredDomain rpServerSignature(ServerSignatureParam serverSignatureParam, AppSession appSession) throws ExceptionReporting, AppException {
		RegisteredDomain registeredDomain = null;
		String location = appSession.wsSession.getRemoteAddress();
		if (serverSignatureParam.getCommClientType() == null) {
			appSession.setCommClientType(CommClientType.APP);
		} else {
			appSession.setCommClientType(serverSignatureParam.getCommClientType());
		}
		if (appSession.getCommClientType() == CommClientType.APP) {
			if (serverSignatureParam.domainName != null) {
				DispatcherApi dispatcherApi = CdiUtils.getReference(AsConstants.DISPATCHER_IMPL_CLASS);
				String dcemTarget = serverSignatureParam.getDomainName();
				int ind = dcemTarget.lastIndexOf(AppSystemConstants.TENANT_SEPERATOR);
				if (ind != -1) {
					// with tenanat
					dcemTarget = dcemTarget.substring(0, ind);
					appSession.setTenantEntity(new TenantEntity(serverSignatureParam.getDomainName().substring(ind + 1)));
					if (dcemTarget.isEmpty() == false) {
						appSession.setDomainName(dcemTarget);
					}
				}
				if (dcemTarget.isEmpty() == false) {
					registeredDomain = dispatcherApi.getProxyDomain(dcemTarget);
					if (registeredDomain == null) {
						AppWsConnection.getInstance().addRpReport(new ReverseProxyReport(appSession.getDomainName(), "ServerSignature",
								AppErrorCodes.UNREGISTERED_DCEM.name(), appSession.getDomainName(), appSession.wsSession.getRemoteAddress()));
						throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, (DcemUser) null, AppErrorCodes.UNREGISTERED_DCEM,
								location, appSession.getDomainName()), null);
					}
					if (registeredDomain.isReverseProxy()) {
						ReverseProxyConnection rpConnection = ReverseProxyConnections.get(dcemTarget);
						if (rpConnection == null) {
							IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
							Callable<List<ReverseProxyStatus>> callable = new CallGetReversProxyStatusTask(dcemTarget);
							Map<Member, Future<List<ReverseProxyStatus>>> futures = executorService.submitToAllMembers(callable);
							String redirectNodeName = null;
							for (Member member : futures.keySet()) {
								try {
									List<ReverseProxyStatus> list = futures.get(member).get();
									if (list.size() > 0) {
										redirectNodeName = member.getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE);
										break;
									}
								} catch (Exception e) {
									JsfUtils.addErrorMessage("Something went wrong: " + e.toString());
									logger.warn(e);
								}
							}
							if (redirectNodeName == null) {
								throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, ((DcemUser) null),
										AppErrorCodes.NO_CONNECTION_TO_DESTINATION_DCEM, location, serverSignatureParam.domainName), null);
							} else {
								AppException appException = new AppException(AppErrorCodes.REVERSE_PROXY_REDIRECTION.name());
								appException.setInfo(dispatcherApi.getNodeRedirectionUrl(redirectNodeName));
								throw appException;
							}
						} else if (rpConnection.getSubSessions().size() > registeredDomain.getMaxConnections()) {
							throw new ExceptionReporting(new DcemReporting(ReportAction.Server_Signature, ((DcemUser) null),
									AppErrorCodes.REVERSE_PROXY_CONNECTION_LIMIT, location, serverSignatureParam.getDomainName()), null);
						} else {
							rpConnection.addSubSession(appSession);
							appSession.reverseProxySession = rpConnection.getAppSession();
							appSession.getWsSession().setMaxIdleTimeout(asModule.getPreferences().getKeepAliveConnection() * 1000);
							appSession.setState(ConnectionState.rpClientOpen);
						}
					}
				} // for if (dcemTarget.isEmpty() == false) {
			}
		} else {
			// a DCEM connection
			appSession.setDomainName(serverSignatureParam.domainName);
		}
		return registeredDomain;
	}

	private void verifyUserPassword(DcemUser dcemUser, AppSession appSession, byte[] encPassword, ReportAction reportAction) throws ExceptionReporting {
		String location = appSession.wsSession.getRemoteAddress();
		byte[] password;
		try {
			password = SecureServerUtils.decryptData(appSession.passwordEncryptionKey, encPassword);
		} catch (Exception e) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.VerifyPassword, dcemUser, AppErrorCodes.UNEXPECTED_ERROR, location, e.toString()), null,
					null);
		}
		if (dcemUser.isDomainUser()) {
			try {
				domainLogic.verifyDomainLogin(dcemUser, password);
				if (reportAction == ReportAction.Login) {
					dcemUser = em.merge(dcemUser);
				}
			} catch (DcemException exp) {
				switch (exp.getErrorCode()) {
				case INVALID_USERID:
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, AppErrorCodes.CONNECTION_TO_LDAP_FAILED, location, null),
							appSession.wsSession.getRemoteAddress(), exp);
				case LDAP_LOGIN_USER_FAILED:
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, AppErrorCodes.INVALID_USERID, location, null),
							appSession.wsSession.getRemoteAddress(), exp);
				case USER_DISABLED:
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, AppErrorCodes.USER_DISABLED, location, null),
							appSession.wsSession.getRemoteAddress(), exp);
				case AZURE_NEEDS_MFA:
					break;
				case DOMAIN_WRONG_AUTHENTICATION:
					if (appSession.device != null) {
						deviceLogic.incRetryCounter(appSession.device);
						deviceLogic.updateDeviceRc(appSession.device, null);
						throw new ExceptionReporting(
								new DcemReporting(reportAction, appSession.device.getUser(), AppErrorCodes.INVALID_PASSWORD, location,
										"device: " + appSession.device.getName() + ", RetryCount=" + Integer.toString(appSession.device.getRetryCounter())),
								exp.toString());
					}
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, AppErrorCodes.INVALID_PASSWORD, location, null), exp.toString());
				default:
					AppErrorCodes errorCode = AsUtils.convertToAppErrorCodes(exp.getErrorCode());
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, errorCode, location, exp.toString()),
							appSession.wsSession.getRemoteAddress(), exp);
				}
			} finally {
				Arrays.fill(password, (byte) 0);
			}

		} else {
			byte[] passwordHash;
			try {
				passwordHash = KaraUtils.getSha1WithSalt(dcemUser.getSalt(), password);
			} catch (Exception e) {
				throw new ExceptionReporting(new DcemReporting(reportAction, appSession.getDevice().getUser(), AppErrorCodes.UNEXPECTED_ERROR, location,
						"device: " + appSession.getDevice().getName() + ", error: " + e.toString()), null);
			} finally {
				Arrays.fill(password, (byte) 0);
			}
			if (appSession.device != null) {
				if (deviceLogic.verifyUserHash(appSession.device, passwordHash) == false) {
					deviceLogic.updateDeviceRc(appSession.device, null);
					throw new ExceptionReporting(new DcemReporting(reportAction, appSession.device.getUser(), AppErrorCodes.INVALID_PASSWORD, location,
							"device: " + appSession.device.getName() + ", RetryCount=" + Integer.toString(appSession.device.getRetryCounter())), null);
				}
			} else {
				if (Arrays.equals(dcemUser.getHashPassword(), passwordHash) == false) {
					throw new ExceptionReporting(new DcemReporting(reportAction, dcemUser, AppErrorCodes.INVALID_PASSWORD, location, null), null, null);
				}
			}
		}
		// if no exception was fired, the password has been validated
	}

	public List<SdkCloudSafe> getCloudSafeList(int userId, String nameFilter, long modifiedFromEpoch, CloudSafeOwner owner, int libVersion)
			throws DcemException {
		return cloudSafeLogic.getCloudSafeFileList(userId, nameFilter, modifiedFromEpoch, owner, true, libVersion);
	}

	public void renameCloudSafe(SdkCloudSafeKey uniqueKey, String newName, int userId) throws DcemException {
		CloudSafeEntity cloudSafeEntity = getCloudSafeEntityFromKey(uniqueKey, userId);
		if (StringUtils.isValidName(newName) == false) {
			throw new DcemException(DcemErrorCodes.FILE_NAME_WITH_SPECIAL_CHARACTERS, newName);
		}
		cloudSafeLogic.renameCloudSafe(cloudSafeEntity, newName, userLogic.getUser(userId), false);
	}

	public void deleteCloudSafe(SdkCloudSafeKey uniqueKey, int userId) throws DcemException {
		CloudSafeEntity cloudSafeEntity = getCloudSafeEntityFromKey(uniqueKey, userId);
		if (cloudSafeEntity.getUser().getId().equals(userId) == false) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_CANNOT_DELETE_SHARED_FILE, cloudSafeEntity.getName());
		}
		List<CloudSafeDto> list = cloudSafeLogic.deleteFile(cloudSafeEntity);
		if (list.isEmpty() == false) {
			cloudSafeLogic.deleteCloudSafeFilesContent(list);
		}
	}

	private CloudSafeEntity getCloudSafeEntityFromKey(SdkCloudSafeKey uniqueKey, int userId) throws DcemException {
		CloudSafeEntity cloudSafeEntity = null;

		if (uniqueKey == null) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUD_SAFE_UNIQUE_KEY, "Unique key is not valid.");
		}
		DcemUser dcemUser = userLogic.getUser(userId);
		if (dcemUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUD_SAFE_USER, "User is not valid. UserId: " + userId);
		}
		cloudSafeEntity = cloudSafeLogic.getOwnedOrSharedCloudSafeFromPath(dcemUser, uniqueKey.getDbId(), uniqueKey.getName());
		if (cloudSafeEntity == null) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, uniqueKey.getName());
		}
		// switch (cloudSafeEntity.getOwner()) {
		// case GROUP:
		// HashSet<String> groupSet = userLogic.getUserGroupNames(dcemUser, null);
		// if (groupSet.contains(cloudSafeEntity.getGroup().getName()) == false) {
		// throw new DcemException(DcemErrorCodes.INVALID_CLOUD_SAFE_USER, "User is not member of group." + cloudSafeEntity.getGroup().getName());
		// }
		// break;
		// case USER:
		// if (cloudSafeEntity.getUser().getId() != userId) {
		// List<CloudSafeShareEntity> sharedCloudSafeFiles = cloudSafeLogic.getCloudSafeShareEntities(dcemUser, cloudSafeEntity);
		// if (sharedCloudSafeFiles.size() == 0) {
		// throw new DcemException(DcemErrorCodes.INVALID_CLOUD_SAFE_USER, "User is not valid.");
		// }
		// cloudSafeEntity.setWriteAccess(sharedCloudSafeFiles.get(0).isWriteAccess());
		// cloudSafeEntity.setRestrictDownload(sharedCloudSafeFiles.get(0).isRestrictDownload());
		// }
		// break;
		// default:
		// throw new DcemException(DcemErrorCodes.INVALID_CLOUD_SAFE_USER, "Wrong Owner.");
		// }
		return cloudSafeEntity;
	}

	public List<DeviceOfflineKey> getDeviceOfflineKeys(int userId) throws DcemException {

		List<DeviceOfflineKey> deviceOfflineKeysThrift = new ArrayList<DeviceOfflineKey>();
		DcemUser user = userLogic.getUser(userId);

		if (user != null) {
			// Devices
			List<DevicesUserDtoOffline> devices = deviceLogic.getDevicesOffByUser(user);
			if (devices != null) {
				int window = getPasscodeWindow();
				int validFor = getPasscodeValidFor() * 60;
				for (DevicesUserDtoOffline device : devices) {
					DeviceOfflineKey dokThrift = new DeviceOfflineKey(ByteBuffer.wrap(device.getOfflineKey()), window, validFor,
							AsConstants.TOTP_ALGORITHM_HMAC_SHA256);
					dokThrift.udid = ByteBuffer.wrap(device.getUdid());
					deviceOfflineKeysThrift.add(dokThrift);
				}
			}

			// OTP Tokens
			List<ApiFilterItem> filters = new LinkedList<>();
			filters.add(new ApiFilterItem("user.loginId", user.getLoginId(), null, ApiFilterItem.SortOrderEnum.UNSORTED, ApiFilterItem.OperatorEnum.EQUALS));
			OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
			if (apiServiceImpl != null) {
				List<AsApiOtpToken> otpTokens = apiServiceImpl.queryOtpTokenEntities(filters, null, null, true);
				if (otpTokens != null) {
					int window = apiServiceImpl.getDelayWindow();
					for (AsApiOtpToken otpToken : otpTokens) {
						String algorithm = "";
						int validFor = 0;
						switch (otpToken.getOtpType()) {
						case TIME_6_SHA1_30:
						case TIME_8_SHA1_30:
							validFor = 30;
							algorithm = AsConstants.TOTP_ALGORITHM_HMAC_SHA1;
							break;
						case TIME_6_SHA1_60:
						case TIME_8_SHA1_60:
							validFor = 60;
							algorithm = AsConstants.TOTP_ALGORITHM_HMAC_SHA1;
							break;
						case TIME_6_SHA2_30:
						case TIME_8_SHA2_30:
							validFor = 30;
							algorithm = AsConstants.TOTP_ALGORITHM_HMAC_SHA256;
							break;
						case TIME_6_SHA2_60:
						case TIME_8_SHA2_60:
							validFor = 60;
							algorithm = AsConstants.TOTP_ALGORITHM_HMAC_SHA256;
							break;
						default:
							break;
						}
						DeviceOfflineKey dokThrift = new DeviceOfflineKey(ByteBuffer.wrap(otpToken.getSecretKey()), window, validFor, algorithm);
						deviceOfflineKeysThrift.add(dokThrift);
					}
				}
			}
		}
		return deviceOfflineKeysThrift;
	}

	public int getPasscodeValidFor() {
		return asModule.getPreferences().getPasscodeValidFor();
	}

	public int getPasscodeWindow() {
		return asModule.getPreferences().getPasscodeWindow();
	}
}