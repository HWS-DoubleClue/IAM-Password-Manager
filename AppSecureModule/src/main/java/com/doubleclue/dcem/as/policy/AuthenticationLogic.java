package com.doubleclue.dcem.as.policy;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.FingerprintId;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.entities.UserFingerprintEntity;
import com.doubleclue.dcem.as.logic.AsAuthGatewayLogic;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsFidoLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.as.restapi.model.AddMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMessage;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.as.restapi.model.AsMapEntry;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.DomainApi;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.DomainType;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.send.MessageBird;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.StringUtils;
import com.microsoft.graph.http.GraphServiceException;

@ApplicationScoped
@Named("authenticationLogic")
public class AuthenticationLogic {

	@Inject
	EntityManager em;

	@Inject
	DcemApplicationBean dcemApplication;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	AsMessageHandler messageHandler;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	AsModule asModule;

	@Inject
	MessageBird messageBird;

	@Inject
	FingerprintLogic fingerprintLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	AsAuthGatewayLogic authGatewayLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	AsFidoLogic fidoLogic;

	@Inject
	LicenceLogic licenceLogic;

	private static final Logger logger = LogManager.getLogger(AuthenticationLogic.class);

	public static final int OTP_LENGTH = 6;
	public static final char OTP_PASSCODE_SEPARATOR = '/';
	public static final String PASSWORD_AUTH_METHOD_DETECTION = "##";

	private static final String ACTION_OK = "ok";

	@DcemTransactional
	public AuthenticateResponse authenticate(AuthApplication authApplication, int subId, String userLoginId, AuthMethod authMethod, String password,
			String passcode, AuthRequestParam requestParam) throws DcemException {

		AuthenticateResponse authenticateResponse = new AuthenticateResponse();
		DcemUser dcemUser = null;
		byte[] binPassword = null;
		PolicyAppEntity appEntity = null;
		boolean networkBypass = false;
		try {
			appEntity = policyLogic.getDetachedPolicyApp(authApplication, subId);
			if (appEntity == null) {
				throw new DcemException(DcemErrorCodes.UNKNOWN_POLICY_APPLICATION, authApplication.name() + "/" + subId);
			} else if (userLoginId == null) {
				throw new DcemException(DcemErrorCodes.USER_IS_NULL, appEntity.toString());
			}
			int ind = userLoginId.indexOf('#');
			if (ind != -1) {
				authMethod = AuthMethod.fromAbbr(userLoginId.substring(0, ind));
				if (authMethod == null) {
					throw new DcemException(DcemErrorCodes.INVALID_AUTH_METHOD, userLoginId.substring(0, ind));
				}
				userLoginId = userLoginId.substring(ind + 1);
			}
			boolean ignorePassword = requestParam.isIgnorePassword();
			if (authMethod == AuthMethod.SESSION_RECONNECT) {
				ignorePassword = true;
				passcode = null;
			}
			if (ignorePassword == false) {
				if (password == null) {
					throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, userLoginId);
				}
				if (password.startsWith(PASSWORD_AUTH_METHOD_DETECTION)) {
					ind = password.indexOf(PASSWORD_AUTH_METHOD_DETECTION, PASSWORD_AUTH_METHOD_DETECTION.length());
					if (ind != -1) {
						authMethod = AuthMethod.fromAbbr(password.substring(PASSWORD_AUTH_METHOD_DETECTION.length(), ind));
						if (authMethod == null) {
							throw new DcemException(DcemErrorCodes.INVALID_AUTH_METHOD, "From Password. Password is starts with ## ? " + userLoginId);
						}
						password = password.substring(ind + PASSWORD_AUTH_METHOD_DETECTION.length());
					}
				}
			}

			dcemUser = userLogic.getUser(userLoginId);
			if (dcemUser == null) {
				dcemUser = createDomainAccount(userLoginId, password, ignorePassword);
				if (dcemUser == null) {
					throw new DcemException(DcemErrorCodes.CREATE_ACCOUNT_INVALID_CREDENTIALS, "loginId: " + userLoginId);
				}
			}
			authenticateResponse.setFqUserLoginId(dcemUser.getLoginId());
			authenticateResponse.setDcemUser(dcemUser);
			if (dcemUser.getDomainEntity() != null && dcemUser.getDomainEntity().getDomainType() == DomainType.Azure_AD) {
				if (adminModule.getPreferences().isUseOnlyAzureDirectLogin()) {
					throw new DcemException(DcemErrorCodes.MUST_USE_AZURE_DIRECT_LOGIN, "loginId: " + userLoginId);
				}
			}

			String networkAddress = requestParam.getNetworkAddress();

			PolicyEntity policyEntity = policyLogic.getPolicy(authApplication, subId, dcemUser);
			// System.out.println("USER " + dcemUser + ", networkAddress: " + networkAddress + ", Policy " + policyEntity);
			List<AuthMethod> methods = policyLogic.getAuthMethods(policyEntity, authApplication, subId, dcemUser, requestParam.getSessionCookie());
			if (methods.isEmpty()) {
				throw new DcemException(DcemErrorCodes.NO_AUTH_METHOD_FOUND, null);
			}
			if (authMethod != null) {
				if (authMethod.getValue() != null && methods.contains(authMethod) == false
						&& !(appEntity.getAuthApplication() == AuthApplication.WebServices && appEntity.getSubId() == 0)) {
					throw new DcemException(DcemErrorCodes.AUTH_METHOD_NOT_ALLOWED, null);
				}
				List<AuthMethod> retunredMethods = new ArrayList<>(1);
				retunredMethods.add(authMethod);
				authenticateResponse.setAuthMethods(retunredMethods);
			}
			authenticateResponse.setStayLoggedInAllowed(policyEntity.getDcemPolicy().isEnableSessionAuthentication());
			authenticateResponse.setPolicyName(policyEntity.getName());

			// Check if there is a passcode the password
			if (passcode == null && password != null && password.length() > AuthenticationLogic.OTP_LENGTH
					&& password.charAt(AuthenticationLogic.OTP_LENGTH) == OTP_PASSCODE_SEPARATOR) {
				int i;
				for (i = 0; i < AuthenticationLogic.OTP_LENGTH; i++) {
					if (Character.isDigit(password.charAt(i)) == false) {
						break;
					}
				}
				if (i == AuthenticationLogic.OTP_LENGTH) { // they are all digits
					passcode = password.substring(0, AuthenticationLogic.OTP_LENGTH);
					if (password.length() > AuthenticationLogic.OTP_LENGTH) {
						password = password.substring(AuthenticationLogic.OTP_LENGTH + 1, password.length());
					} else {
						password = "";
					}
				}
			}
			if (ignorePassword == false) {
				binPassword = password.getBytes(DcemConstants.CHARSET_UTF8);
				userLogic.verifyUserPassword(dcemUser, binPassword);
				authenticateResponse.setBinPassword(binPassword);
			}

			if (authMethod == null) {
				if (requestParam.isUnlockUserAuth() && policyEntity.getDcemPolicy().isMfaOnUnlock() == false) {
					authMethod = AuthMethod.PASSWORD;
					List<AuthMethod> retunredMethods = new ArrayList<>(1);
					retunredMethods.add(authMethod);
					authenticateResponse.setAuthMethods(retunredMethods);
				} else {
					if (policyLogic.isNetworkPassThrough(policyEntity.getDcemPolicy(), networkAddress)) {
						networkBypass = true;
						authMethod = AuthMethod.PASSWORD;
						authenticateResponse.setAuthMethods(methods);
					} else if (methods.size() == 1 && methods.get(0) == AuthMethod.PASSWORD) {
						authMethod = methods.get(0);
					} else if (policyEntity.getDcemPolicy().getDefaultPolicy() != null && (requestParam.isUseAlternativeAuthMethods() == false)) {
						authMethod = policyEntity.getDcemPolicy().getDefaultPolicy();
						List<AuthMethod> retunredMethods = new ArrayList<>(1);
						retunredMethods.add(authMethod);
						authenticateResponse.setAuthMethods(retunredMethods);
					} else {
						if (methods.size() > 1) {
							authenticateResponse.setAuthMethods(methods);
							return authenticateResponse; // Return with a selection
						}
						if (methods.size() == 1) {
							authMethod = methods.get(0);
						}
						authenticateResponse.setAuthMethods(methods);
					}
				}
			}

			licenceLogic.checkForLicence(authApplication, true);
			AsTenantData tenantData = asModule.getTenantData();

			switch (authMethod) {
			case HARDWARE_TOKEN:
				authenticateResponse = respondHardwareToken(authenticateResponse, dcemUser, passcode);
				break;
			case DOUBLECLUE_PASSCODE:
				authenticateResponse = respondPasscode(authenticateResponse, dcemUser, passcode);
				break;
			case PASSWORD:
				authenticateResponse.setSuccessful(true);
				break;
			case PUSH_APPROVAL:
				authenticateResponse = respondSecureMessage(authenticateResponse, appEntity, dcemUser, tenantData, requestParam, policyEntity, authApplication,
						subId, requestParam.getLocation());
				break;
			case SESSION_RECONNECT:
				if (policyEntity.getDcemPolicy().isEnableSessionAuthentication() == false) {
					throw new DcemException(DcemErrorCodes.AUTH_SESSION_COOKIE_NOT_ALLOWED, dcemUser.getLoginId());
				}
				authenticateResponse = respondSessionReconnect(authenticateResponse, appEntity, dcemUser, requestParam);
				break;
			case SMS:
			case VOICE_MESSAGE:
				authenticateResponse = respondSmsOrVoiceMessage(authenticateResponse, appEntity, dcemUser, tenantData, passcode, authMethod);
				break;
			case FIDO_U2F:
				authenticateResponse = respondFido(authenticateResponse, requestParam, userLoginId);
				break;
			case QRCODE_APPROVAL:
				authenticateResponse.setSuccessful(true);
				break;
			default:
				break;
			}

			if (authenticateResponse.isSuccessful()) {
				FingerprintId fpId = new FingerprintId(dcemUser.getId(), appEntity);
				UserFingerprintEntity userFingerprintEntity = createFingerPrint(fpId, policyEntity.getDcemPolicy());
				if (userFingerprintEntity != null && authenticateResponse.isStayLoggedInAllowed() == true) {
					fingerprintLogic.updateFingerprint(userFingerprintEntity);
					authenticateResponse.setSessionCookie(userFingerprintEntity.getFingerprint());
					authenticateResponse.setSessionCookieExpiresOn((int) (userFingerprintEntity.getTimestamp().toEpochSecond(ZoneOffset.UTC)));
				}
				if (dcemUser.getPassCounter() > 0) {
					userLogic.resetPasswordCounter(dcemUser);
				}
				userLogic.setUserLogin(dcemUser);
				DcemReporting report = new DcemReporting(getAppName(appEntity), getReportAction(authMethod, networkBypass), dcemUser, null,
						requestParam.getLocation(), requestParam.getReportInfo(), AlertSeverity.OK);
				reportingLogic.addReporting(report);
			}
			return authenticateResponse;

		} catch (DcemException exp) {
			authenticateResponse.setDcemException(exp);
			authenticateResponse.setSuccessful(false);
			PolicyAppEntity appEntity_ = appEntity;
			AuthMethod authMethod_ = authMethod;
			DcemUser dcemUser_ = dcemUser;
			String userLoginId_ = userLoginId;
			boolean networkBypass_ = networkBypass;

			taskExecutor.execute(new com.doubleclue.dcem.core.tasks.CoreTask(this.getClass().getSimpleName(), TenantIdResolver.getCurrentTenant()) {
				@Override
				public void runTask() {
					String info = requestParam.getReportInfo();
					if (info == null) {
						info = exp.getMessage();
					} else {
						info = info + " " + exp.getMessage();
					}
					if (exp.getErrorCode() != DcemErrorCodes.DATABASE_CONNECTION_ERROR) {
						DcemReporting report = new DcemReporting(getAppName(appEntity_), getReportAction(authMethod_, networkBypass_), dcemUser_,
								exp.getErrorCode().name(), requestParam.getLocation(), info, AlertSeverity.FAILURE);
						reportingLogic.addReporting(report);
					}
					logger.info("Authentication Failed, Cause: " + exp.toString() + " from: " + authApplication.name() + "/" + subId + ", " + userLoginId_);
					if (exp.getErrorCode() == DcemErrorCodes.INVALID_PASSWORD || exp.getErrorCode() == DcemErrorCodes.INVALID_OTP) {
						userLogic.setPasswordCounter(dcemUser_.getId(), (dcemUser_.getPassCounter() + 1));
						fingerprintLogic.deleteFingerPrint(dcemUser_.getId(), appEntity_);
					}
				}
			});
			return authenticateResponse;
		} catch (Exception exp) {
			logger.info("Authentication Failed. " + authApplication.name() + "/" + subId + ", " + userLoginId, exp);
			PolicyAppEntity appEntity_ = appEntity;
			AuthMethod authMethod_ = authMethod;
			DcemUser dcemUser_ = dcemUser;
			boolean networkBypass_ = networkBypass;
			taskExecutor.execute(new com.doubleclue.dcem.core.tasks.CoreTask("test", TenantIdResolver.getCurrentTenant()) {
				@Override
				public void runTask() {
					DcemReporting report = new DcemReporting(getAppName(appEntity_), getReportAction(authMethod_, networkBypass_), dcemUser_,
							DcemErrorCodes.UNEXPECTED_ERROR.name(), requestParam.getLocation(), exp.toString(), AlertSeverity.FAILURE);
					reportingLogic.addReporting(report);
				}
			});
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, exp.toString());
		}
	}

	private String getAppName(PolicyAppEntity appEntity) {
		return appEntity.getSubName() != null ? appEntity.getSubName() : appEntity.getAuthApplication().name();
	}

	private AuthenticateResponse respondHardwareToken(AuthenticateResponse authenticateResponse, DcemUser dcemUser, String passcode) throws DcemException {
		OtpModuleApi otpModuleApi = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
		if (passcode != null && !passcode.isEmpty()) {
			otpModuleApi.verifyOtpPasscode(dcemUser, passcode);
			authenticateResponse.setSuccessful(true);
		}
		return authenticateResponse;
	}

	private AuthenticateResponse respondPasscode(AuthenticateResponse authenticateResponse, DcemUser dcemUser, String passcode) throws DcemException {
		if (passcode != null && !passcode.isEmpty()) {
			deviceLogic.verifyUserPasscode(dcemUser, passcode);
			authenticateResponse.setSuccessful(true);
		}
		return authenticateResponse;
	}

	/**
	 * @param authenticateResponse
	 * @param appEntity
	 * @param dcemUser
	 * @param tenantData
	 * @param requestParam
	 * @param policyEntity
	 * @param authApplication
	 * @param subId
	 * @return
	 * @throws DcemException
	 */
	private AuthenticateResponse respondSecureMessage(AuthenticateResponse authenticateResponse, PolicyAppEntity appEntity, DcemUser dcemUser,
			AsTenantData tenantData, AuthRequestParam requestParam, PolicyEntity policyEntity, AuthApplication authApplication, int subId, String location)
			throws DcemException {

		AsApiMessage apiMessage = new AsApiMessage();
		apiMessage.setUserLoginId(dcemUser.getLoginId());
		apiMessage.setTemplateName(requestParam.getTemplateName());
		apiMessage.setResponseRequired(true);
		apiMessage.setSessionId(requestParam.getSessionId());
		apiMessage.setNotifyNodeOnResponse(DcemCluster.getDcemCluster().getDcemNode().getId());
		apiMessage.setAllowPasswordLess(true);
		List<AsMapEntry> dataMap = new ArrayList<>(1);
		String code = RandomUtils.generateRandomNumberString(4);
		dataMap.add(new AsMapEntry(DcemConstants.AUTH_MAP_CODE, code));
		if (appEntity.getSubName() != null) {
			dataMap.add(new AsMapEntry(DcemConstants.AUTH_MAP_SOURCE, appEntity.getSubName()));
		} else {
			dataMap.add(new AsMapEntry(DcemConstants.AUTH_MAP_SOURCE, appEntity.getAuthApplication().name()));
		}
		apiMessage.setDataMap(dataMap);
		AddMessageResponse addMessageResponse = messageHandler.sendMessage(apiMessage, dcemUser, operatorSessionBean.getDcemUser(), authApplication, subId,
				policyEntity, location);
		long msgId = addMessageResponse.getMsgId();
		authenticateResponse.setSecureMsgId(msgId);
		authenticateResponse.setSecureMsgTimeToLive(addMessageResponse.getTimeToLive());
		authenticateResponse.setSecureMsgRandomCode(code);
		return authenticateResponse;
	}

	private AuthenticateResponse respondSessionReconnect(AuthenticateResponse authenticateResponse, PolicyAppEntity appEntity, DcemUser dcemUser,
			AuthRequestParam requestParam) throws DcemException {
		int appId = 0;
		if (appEntity.getAuthApplication().isShareSession() == false) {
			appId = appEntity.getId();
		}
		if (fingerprintLogic.verifyFingerprint(dcemUser.getId(), appId, requestParam.getSessionCookie()) == false) {
			throw new DcemException(DcemErrorCodes.INVALID_AUTH_SESSION_COOKIE, dcemUser.getLoginId());
		} else {
			authenticateResponse.setSuccessful(true);
		}
		return authenticateResponse;
	}

	private AuthenticateResponse respondSmsOrVoiceMessage(AuthenticateResponse authenticateResponse, PolicyAppEntity appEntity, DcemUser dcemUser,
			AsTenantData tenantData, String passcode, AuthMethod authMethod) throws DcemException {

		FingerprintId fingerprintId = new FingerprintId(dcemUser.getId(), appEntity);
		if (passcode == null || passcode.isEmpty()) {
			String otp = RandomUtils.generateRandomNumberString(OTP_LENGTH);
			tenantData.getSmsPasscodesMap().put(fingerprintId, otp, 5, TimeUnit.MINUTES);
			List<String> telephoneNumbers = new LinkedList<>();
			String mobile = dcemUser.getMobile();
			DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
			if (authMethod == AuthMethod.SMS) {
				if (mobile == null) {
					throw new DcemException(DcemErrorCodes.SMS_USER_HAS_NO_MOBILE, dcemUser.getLoginId());
				}
				Map<String, String> map = new HashMap<>();
				map.put(AsConstants.SMS_CODE, otp);
				map.put(AsConstants.SMS_USER_NAME, dcemUser.getDisplayName());
				String body = StringUtils.substituteTemplate(dbResourceBundle.getString(AsConstants.SMS_PASSCODE_BUNDLE_KEY), map);
				telephoneNumbers.add(mobile);
				messageBird.sendSmsMessage(telephoneNumbers, body);
				authenticateResponse.setPhoneNumber(telephoneNumbers.get(0));
			} else {
				// Voice Message
				if (dcemUser.getPrivateMobileNumber() != null && dcemUser.getPrivateMobileNumber().isEmpty() == false) {
					telephoneNumbers.add(dcemUser.getPrivateMobileNumber());
				} else {
					if (dcemUser.getTelephoneNumber() == null || dcemUser.getTelephoneNumber().isEmpty()) {
						if (mobile != null) {
							telephoneNumbers.add(mobile);
						}
					} else {
						telephoneNumbers.add(dcemUser.getTelephoneNumber());
					}
				}
				if (telephoneNumbers.isEmpty()) {
					throw new DcemException(DcemErrorCodes.USER_HAS_NO_TELEPHONE_OR_MOBILE, dcemUser.getLoginId());
				}
				StringBuffer buffer = new StringBuffer();
				buffer.append(dbResourceBundle.getString(AsConstants.VOICE_MESSAGE_BUNDLE_KEY));
				for (int i = 0; i < otp.length(); i++) {
					buffer.append("<break time=\"600ms\"/>");
					buffer.append(otp.charAt(i));
				}
				messageBird.sendVoiceMessage(telephoneNumbers, buffer.toString(), dcemUser.getLanguage());
				authenticateResponse.setPhoneNumber(telephoneNumbers.get(0));
			}
		} else {
			String otp = tenantData.getSmsPasscodesMap().get(fingerprintId);
			if (otp != null && otp.equals(passcode)) {
				tenantData.getSmsPasscodesMap().remove(fingerprintId);
				authenticateResponse.setSuccessful(true);
			} else {
				throw new DcemException(DcemErrorCodes.INVALID_OTP, null);
			}
		}
		return authenticateResponse;
	}

	private AuthenticateResponse respondFido(AuthenticateResponse authenticateResponse, AuthRequestParam requestParam, String userLoginId)
			throws DcemException {
		String response = requestParam.getFidoResponse();
		if (response == null || response.isEmpty()) {
			authenticateResponse.setFidoResponse(fidoLogic.startAuthentication(userLoginId, requestParam.getFidoRpId()));
			licenceLogic.resetExpiredLicenceUserShouldAuthenticate();
		} else {
			authenticateResponse.setFidoResponse(fidoLogic.finishAuthentication(response));
			authenticateResponse.setSuccessful(true);
		}
		return authenticateResponse;
	}

	private ReportAction getReportAction(AuthMethod authMethod, boolean netWorkBypass) {
		if (authMethod == null) {
			return ReportAction.Authenticate;
		}
		switch (authMethod) {
		case PASSWORD:
			if (netWorkBypass == true) {
				return ReportAction.Authenticate_NetworkBypass;
			}
			return ReportAction.Authenticate_pwd;
		case SMS:
			return ReportAction.Authenticate_sms;
		case VOICE_MESSAGE:
			return ReportAction.Authenticate_voice;
		case HARDWARE_TOKEN:
			return ReportAction.Authenticate_otp;
		case DOUBLECLUE_PASSCODE:
			return ReportAction.Authenticate_motp;
		case PUSH_APPROVAL:
			return ReportAction.Authenticate_push;
		case QRCODE_APPROVAL:
			return ReportAction.Authenticate_qrcode;
		case FIDO_U2F:
			return ReportAction.Authenticate_fido;
		case SESSION_RECONNECT:
			return ReportAction.Authenticate_reconnect;
		default:
			break;
		}
		return null;
	}

	public void onSecureMessageResponseReceived(PendingMsg pendingMsg, AsApiMessageResponse apiMessageResponse) {
		DcemPolicy dcemPolicy = pendingMsg.getPolicyTransaction().getDcemPolicy();
		DcemUser dcemUser = userLogic.getUser(pendingMsg.getUserId());
		FingerprintId fpId = new FingerprintId(dcemUser.getId(), pendingMsg.getPolicyTransaction().getPolicyAppEntity());
		String errorCode = null;
		if (pendingMsg.getMsgStatus() == AsApiMsgStatus.OK) {
			if (pendingMsg.getActionId().equals(ACTION_OK)) {
				if (dcemPolicy != null) {
					UserFingerprintEntity userFingerprintEntity = createFingerPrint(fpId, dcemPolicy);
					if (userFingerprintEntity != null && dcemPolicy.isEnableSessionAuthentication() == true) {
						fingerprintLogic.updateFingerprint(userFingerprintEntity);
						apiMessageResponse.setSessionCookie(userFingerprintEntity.getFingerprint());
						apiMessageResponse.setSessionCookieExpiresOn((int) (userFingerprintEntity.getTimestamp().toEpochSecond(ZoneOffset.UTC)));
						apiMessageResponse.setStayLoggedInAllowed(dcemPolicy.isEnableSessionAuthentication());
					}
				}
				if (dcemUser.getPassCounter() > 0) {
					userLogic.resetPasswordCounter(dcemUser);
				}
			} else {
				errorCode = DcemConstants.REPORT_ERROR_CODE_DENY;
			}
		} else {
			errorCode = pendingMsg.getMsgStatus().name();
		}
		userLogic.setUserLogin(dcemUser);
		DcemReporting report = new DcemReporting(getAppName(pendingMsg.getPolicyTransaction().getPolicyAppEntity()),
				getReportAction(AuthMethod.PUSH_APPROVAL, false), dcemUser, errorCode, pendingMsg.getInfo(), pendingMsg.getDeviceName(), AlertSeverity.OK);
		reportingLogic.addReporting(report);
		return;
	}

	private UserFingerprintEntity createFingerPrint(FingerprintId fpId, DcemPolicy dcemPolicy) {
		if ((dcemPolicy.getRememberBrowserFingerPrint() > 0) && (dcemPolicy.isEnableSessionAuthentication() == true)) {
			String sessionCookie = RandomUtils.generateRandomAlphaNumericString(48);
			return new UserFingerprintEntity(fpId, sessionCookie, dcemPolicy.getRememberBrowserFingerPrint() * 60);
		}
		return null;
	}

	private DcemUser createDomainAccount(String userLoginId, String password, boolean ignorePassword) throws Exception {
		/*
		 * create user automatically if it is a domain user with correct password
		 */
		DcemUser dcemUser = null;
		DomainApi domainApi = domainLogic.getDomainFromEmail(userLoginId, null);
		if (domainApi == null) {
			String[] domainUser = userLoginId.split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
			if (domainUser.length > 1) {
				domainUser[0] = domainUser[0].toUpperCase();
				dcemUser = new DcemUser(domainUser[0], domainUser[1]);
				domainApi = domainLogic.getDomainApi(domainUser[0]);
				dcemUser.setDomainEntity(domainApi.getDomainEntity());
			} else {
				return null;
			}
		} else {
			dcemUser = new DcemUser(domainApi.getDomainEntity(), null, userLoginId);
			dcemUser.setUserPrincipalName(userLoginId);
			dcemUser.setDisplayName(userLoginId);
		}
		if (ignorePassword == false) {
			try {
				domainLogic.verifyDomainLogin(dcemUser, password.getBytes(DcemConstants.CHARSET_UTF8));
			} catch (DcemException exp) {
				if ((exp.getErrorCode() == DcemErrorCodes.DB_TRANSACTION_ERROR) && (exp.getCause() instanceof GraphServiceException)) {
					throw new DcemException(DcemErrorCodes.CREATE_ACCOUNT_INVALID_CREDENTIALS, null, exp.getCause());
				}
				throw exp;
			}
		} else {
			dcemUser = domainLogic.getUser(dcemUser.getDomainEntity().getName(), dcemUser.getAccountName());
		}
		dcemUser.setLanguage(adminModule.getPreferences().getUserDefaultLanguage());
		userLogic.addOrUpdateUserWoAuditing(dcemUser);
		return dcemUser;
	}

	public List<ClaimAttribute> getClaimAttributeValues(List<ClaimAttribute> claimAttributes, DcemUser user, String policyName, String password) {
		List<ClaimAttribute> result = new ArrayList<ClaimAttribute>();
		Map<String, String> attrMap = null;
		if (user.isDomainUser()) {
			List<String> domainAttributes = new ArrayList<String>();
			for (ClaimAttribute claimAttribute : claimAttributes) {
				if (claimAttribute.getAttributeTypeEnum() == AttributeTypeEnum.DOMAIN_ATTRIBUTE) {
					domainAttributes.add(claimAttribute.getValue());
				}
			}
			if (domainAttributes.isEmpty() == false) {
				try {
					attrMap = domainLogic.getUserAttributes(user, domainAttributes);
				} catch (Exception e) {
					logger.warn("SAML - Couldn't retreive the Domain Attributes for " + user.getLoginId(), e);
				}
			}
		}

		for (ClaimAttribute ca : claimAttributes) {
			ClaimAttribute claimAttribute = new ClaimAttribute(ca);

			switch (claimAttribute.getAttributeTypeEnum()) {
			case DISPLAY_NAME:
				claimAttribute.setValue(user.getDisplayName());
				break;
			case LOGIN_ID:
				claimAttribute.setValue(user.getLoginId());
				break;
			case EMAIL:
				claimAttribute.setValue(user.getEmail());
				break;
			case MOBILE:
				claimAttribute.setValue(user.getMobileNumber());
				break;
			case TELEPHONE:
				claimAttribute.setValue(user.getTelephoneNumber());
				break;
			case CLOUD_SAFE_USER:
				try {
					CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(CloudSafeOwner.USER, claimAttribute.getValue(), user, null, 0, null);
					if (cloudSafeEntity != null) {
						claimAttribute.setValue(cloudSafeLogic.getContentAsString(cloudSafeEntity, null, null, false));
					} else {
						logger.info("SAML - could not find CloudSafe '" + claimAttribute.getName() + "' for user '" + user.getDisplayNameOrLoginId() + "'.");
					}
				} catch (DcemException e) {
					logger.warn("SAML - error while getting CloudSafe '" + claimAttribute.getName() + "' for user '" + user.getDisplayNameOrLoginId() + "': ",
							e);
				}
				break;
			case AD_OBJECT_GUID:
				claimAttribute.setValue(user.getObjectGuidString());
				break;
			case GROUPS:
				try {
					String groups = userLogic.getUserGroupNamesAsString(user, claimAttribute.getValue());
					groups = groups.replace("\\", "\\\\"); // escape backslashes
					claimAttribute.setValue(groups);
				} catch (DcemException e) {
					logger.error("Couldn't retrieve user groups", e);
				}
				break;
			case DOMAIN_ATTRIBUTE:
				if (attrMap != null) {
					claimAttribute.setValue(attrMap.get(claimAttribute.getValue().toLowerCase()));
				}
				break;
			case POLICY:
				claimAttribute.setValue(policyName);
				break;
			case ACCOUNT_NAME:
				claimAttribute.setValue(user.getAccountName());
				break;
			case UPN:
				claimAttribute.setValue(user.getUserPrincipalName());
				break;
			case LOCALE:
				claimAttribute.setValue(user.getLanguage().getLocale().toString());
				break;
			case PASSWORD:
				claimAttribute.setValue(password);
				break;
			default:
				break;
			}
			result.add(claimAttribute);
		}
		return result;
	}

}
