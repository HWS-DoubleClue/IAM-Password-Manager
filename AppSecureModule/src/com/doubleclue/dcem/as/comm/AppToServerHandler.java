package com.doubleclue.dcem.as.comm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor licence agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;

import com.doubleclue.comm.thrift.ActivationParam;
import com.doubleclue.comm.thrift.ActivationResponse;
import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.AppMessage;
import com.doubleclue.comm.thrift.AppMessageResponse;
import com.doubleclue.comm.thrift.AppToServer;
import com.doubleclue.comm.thrift.AuthConnectParam;
import com.doubleclue.comm.thrift.AuthSelectParam;
import com.doubleclue.comm.thrift.AuthSelectResponse;
import com.doubleclue.comm.thrift.AuthUserParam;
import com.doubleclue.comm.thrift.AuthUserResponse;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.comm.thrift.DeviceOfflineKey;
import com.doubleclue.comm.thrift.DomainSdkConfigParam;
import com.doubleclue.comm.thrift.DomainSdkConfigResponse;
import com.doubleclue.comm.thrift.LoginParam;
import com.doubleclue.comm.thrift.LoginResponse;
import com.doubleclue.comm.thrift.QrCodeResponse;
import com.doubleclue.comm.thrift.RegisterDispatcherParam;
import com.doubleclue.comm.thrift.RegisterDispatcherResponse;
import com.doubleclue.comm.thrift.RequestActivationCodeResponse;
import com.doubleclue.comm.thrift.SdkCloudSafe;
import com.doubleclue.comm.thrift.SdkCloudSafeKey;
import com.doubleclue.comm.thrift.ServerSignatureParam;
import com.doubleclue.comm.thrift.ServerSignatureResponse;
import com.doubleclue.comm.thrift.SignatureParam;
import com.doubleclue.comm.thrift.SignatureResponse;
import com.doubleclue.comm.thrift.Template;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsUtils;
import com.doubleclue.dcem.as.logic.ExceptionReporting;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.as.restapi.model.RequestLoginQrCodeResponse;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.SecureUtils;

/**
 * Class is created on every Web-Socket connection
 * 
 * @author Emanuel
 *
 */

public class AppToServerHandler implements AppToServer.Iface {

	private static final Logger logger = LogManager.getLogger(AppToServerHandler.class);

	AppServices appServices;
	AsMessageHandler asMessageHandler;
	DcemReportingLogic reportingLogic;
	DcemApplicationBean applicationBean;
	UserLogic userLogic;

	public AppToServerHandler() {
		appServices = CdiUtils.getReference(AppServices.class);
		asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
		reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
		applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		userLogic = CdiUtils.getReference(UserLogic.class);
	}

	// @Override
	// public void suspend() throws AppException, TException {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public ByteBuffer reconnect(ReconnectParam reconnectParam) throws
	// AppException, TException {
	//
	// return null;
	// }

	@Override
	public ServerSignatureResponse serverSignature(ServerSignatureParam serverSignatureParam) throws AppException, TException {
		ServerSignatureResponse serverSignature;
		try {
			serverSignature = appServices.serverSignature(serverSignatureParam);
		} catch (ExceptionReporting exp) {
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (TException exp) {
			logger.info("serverSignature Error: " + exp.getMessage());
			throw exp;
		} catch (Exception exp) {
			logger.warn("serverSignature went wrong: ", exp);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		return serverSignature;
	}

	@Override
	public ActivationResponse activation(ActivationParam activationParam) throws AppException, TException {
		ActivationResponse activationResponse;
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		String location = appSession.wsSession.getRemoteAddress();
		try {
			activationResponse = appServices.activation(activationParam, appSession, location);
		} catch (ExceptionReporting exp) {
			logger.info("Activation ERROR: " + exp.toString());
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (TException exp) {
			logger.info("Activation Error: " + exp.getMessage());
			DcemReporting reporting = new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.TRANSPORT_ERROR, location, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw exp;
		} catch (Exception exp) {
			logger.warn("Activation went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, location, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		return activationResponse;
	}

	@Override
	public LoginResponse login(LoginParam loginParam) throws AppException, TException {
		LoginResponse loginResponse;
		try {
			loginResponse = appServices.login(loginParam);
		} catch (ExceptionReporting exp) {
			logger.info(ReportAction.Login.name() + " ERROR: " + exp.toString());
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw new AppException(reporting.getErrorCode());
		} catch (TException exp) {
			logger.info("Login Error: " + exp.getMessage());
			DcemReporting reporting = new DcemReporting(ReportAction.Login, (DcemUser) null, AppErrorCodes.TRANSPORT_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw exp;
		} catch (Exception exp) {
			logger.warn("Login went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.Login, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			if (exp.getCause().getClass() == null || ExceptionReporting.class == null) {
				throw exp;
			}
			if (exp.getCause().getClass() == ExceptionReporting.class) {
				ExceptionReporting cause = (ExceptionReporting) exp.getCause();
				throw new AppException(cause.getErrorCause());
			}
			String errorMessage = exp.getMessage();
			if (errorMessage == null && exp.getCause() != null) {
				errorMessage = exp.getLocalizedMessage();
			}
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), errorMessage);
		}
		return loginResponse;
	}

	@Override
	public SignatureResponse clientSignature(SignatureParam signaturenParam) throws AppException, TException {
		SignatureResponse signatureResponse;
		try {
			signatureResponse = appServices.clientSignature(signaturenParam);
		} catch (ExceptionReporting exp) {
			logger.info(ReportAction.Login.name() + " ERROR: " + exp.toString());
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (TException exp) {
			logger.info("Activation Error: " + exp.getMessage());
			DcemReporting reporting = new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.TRANSPORT_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw exp;
		} catch (Exception exp) {
			logger.warn("Activation went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.Login_Signature, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		return signatureResponse;
	}

	@Override
	public boolean sendMessage(AppMessage appMessage) throws AppException, TException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void sendMessageResponse(AppMessageResponse appMessageResponse) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		// TenantIdResolver.setCurrentTenant(appSession.getTenantEntity());
		if (appSession.state != ConnectionState.messagePending) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		try {
			asMessageHandler.receivedMsgResponse(appMessageResponse, appSession);
			appSession.setState(ConnectionState.loggedIn);

		} catch (ExceptionReporting exp) {
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			if (exp.getException() != null) {

			}
			logger.warn("receivedMsgResponse: ", exp);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (AppException exp) {
			logger.warn("receivedMsgResponse went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.MessageResponse, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			appSession.wsSession.setMaxIdleTimeout(AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT);
			throw exp;

		} catch (Exception exp) {
			logger.warn("Message Response went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.MessageResponse, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			appSession.wsSession.setMaxIdleTimeout(AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
		return;
	}

	@Override
	public Template getTemplateFromId(int id) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state != ConnectionState.loggedIn && appSession.state != ConnectionState.messagePending) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		Template template = null;
		;
		try {
			template = appServices.getTemplateFromId(id);
		} catch (Exception exp) {
			logger.warn("getTemplateFromId went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.GetTemplate, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null,
					"TempalteId=" + id + ", " + exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		if (template == null) {
			throw new AppException(AppErrorCodes.NO_TEMPLATE_FOUND.name());
		}
		return template;
	}

	@Override
	public void disconnect(AppErrorCodes appErrorCodes, String message) throws TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state == ConnectionState.loggedIn || appSession.state == ConnectionState.messagePending
				|| appSession.state == ConnectionState.authenticated) {
			if (appErrorCodes != AppErrorCodes.OK) {
				DcemUser user = appSession.getDevice().getUser();
				DcemReporting reporting = new DcemReporting(ReportAction.Disconnected, user, appErrorCodes, null,
						"device: " + appSession.getDevice().getName() + " - " + message);
				reportingLogic.addReporting(reporting);
			}
		}
		if (appSession.state == ConnectionState.messagePending) {
			try {
				asMessageHandler.disconnectedPendingMsg(appSession, AsApiMsgStatus.DISCONNECTED);
			} catch (DcemException e) {
				logger.warn("Couldn't handle disconnect Pending Msg", e);
			}
		}
		appServices.setDeviceDisconnect(appSession.getDevice());
		appSession.setState(ConnectionState.disconnected);
		appSession.wsSession.setMaxIdleTimeout(AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT);
	}

	@Override
	public void sendLoginQrCode(String data) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state != ConnectionState.loggedIn) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		try {
			appServices.sendLogonQrCode(data, appSession);
		} catch (Exception exp) {
			logger.warn("getTemplateFromId went wrong: ", exp);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}

	}

	public void changePassword(ByteBuffer passwordHash, ByteBuffer newPasswordHash) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state != ConnectionState.loggedIn && appSession.state != ConnectionState.authenticated) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		try {
			appServices.changePassword(passwordHash.array(), newPasswordHash.array(), appSession);
		} catch (DcemException exp) {
			logger.info("Change Password failed. ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.ChangePassword, appSession.getDevice().getUser(),
					AsUtils.convertToAppErrorCodes(exp.getErrorCode()), null, "device: " + appSession.getDevice().getName() + ", message: " + exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (Exception exp) {
			logger.warn("Change Password failed. ", exp);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}

	}

	@Override
	public SdkCloudSafe getCloudSafe(SdkCloudSafeKey uniqueKey, String userLoginId) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state != ConnectionState.loggedIn && appSession.state != ConnectionState.authenticated) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		try {
			return appServices.getCloudSafe(uniqueKey, appSession.device, appSession.getUserId(), appSession.getLibVersion().version);
		} catch (DcemException exp) {
			logger.info("Get CloudSafe failed.", exp);
			AppErrorCodes appErrorCodes = AsUtils.convertToAppErrorCodes(exp.getErrorCode());
			DcemUser dcemUser = userLogic.getUser(appSession.getUserId());
			reportingLogic.addReporting(new DcemReporting(ReportAction.GetCloudSafe, dcemUser, appErrorCodes, null,
					"device: " + appSession.getDomainName() + ", exception: " + exp.toString()));
			throw createAppException(appErrorCodes.name(), uniqueKey.toString());
		} catch (Exception e) {
			logger.info("Get CloudData failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
	}

	@Override
	public long setCloudSafe(SdkCloudSafe cloudData) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		switch (appSession.state) {
		case loggedIn:
		case authenticated:
		case messagePending:
			break;
		case loggedInPasswordLess:
			if (cloudData.getUniqueKey().getOwner() == CloudSafeOwner.DEVICE) {
				break;
			}
		default:
			logger.warn("! Incorrect State: DeviceId=" + appSession.getDeviceId() + " State " + appSession.state);
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}

		try {
			LocalDateTime lastModified = appServices.setCloudSafe(cloudData, appSession.device, appSession.getUserId());
			return (lastModified.toEpochSecond(ZoneOffset.UTC) * 1000);
		} catch (ExceptionReporting exp) {
			logger.info(" setData Error: " + exp.getReporting().toString());
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (Exception e) {
			logger.warn("Set CloudData failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
	}

	@Override
	public void deactivate() throws TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		try {
			appServices.deactivate(appSession);
			appSession.getWsSession().setMaxIdleTimeout(AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT);
		} catch (ExceptionReporting exp) {
			logger.warn("Device Deactivate failed for " + appSession.getUserId(), exp);
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		}

	}

	@Override
	public void keepAlive() throws TException {
		if (logger.isDebugEnabled()) {
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			logger.debug("keepAlive from UserID: " + appSession.getUserId());
		}
		return;
	}

	@Override
	public RequestActivationCodeResponse requestActivationCode() throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();

		try {
			return appServices.requestActivationCode(appSession.device.getUser());
		} catch (ExceptionReporting exp) {
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		}
	}

	@Override
	public void verifyPassword(ByteBuffer encPassword) throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		if (appSession.state != ConnectionState.loggedIn) {
			logger.info("Incorrect State: DeviceId=" + appSession.getDeviceId());
			throw new AppException(AppErrorCodes.INCORRECT_STATE.name());
		}
		try {
			appServices.verifyPassword(encPassword.array(), appSession);
		} catch (ExceptionReporting exp) {
			logger.info("verifyPassword failed. ", exp);
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		}

	}

	@Override
	public DomainSdkConfigResponse getDomainSdkConfig(DomainSdkConfigParam domainSdkConfigParam) throws AppException, TException {
		DomainSdkConfigResponse domainSdkConfigResponse;
		try {
			domainSdkConfigResponse = appServices.getDomainSdkConfig(domainSdkConfigParam);
		} catch (DcemException exp) {
			logger.info("getDomainSdkConfig", exp);
			AppErrorCodes appErrorCodes = AsUtils.convertToAppErrorCodes(exp.getErrorCode());
			reportingLogic.addReporting(new DcemReporting(ReportAction.GetDomainSdkConfig, (DcemUser) null, appErrorCodes, (String) null, exp.toString()));
			throw new AppException(appErrorCodes.name());
		} catch (ExceptionReporting exp) {
			logger.info(" getDomainSdkConfig Error: " + exp.getReporting().toString(), exp);
			DcemReporting reporting = exp.getReporting();
			reportingLogic.addReporting(reporting);
			throw createAppException(reporting.getErrorCode(), reporting.getInfo());
		} catch (Exception exp) {
			logger.warn("Activation went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		logger.info("Dispatcher for " + domainSdkConfigParam.getUserId());
		return domainSdkConfigResponse;
	}

	@Override
	public RegisterDispatcherResponse registerDispatcher(RegisterDispatcherParam registerDispatcherParam) throws AppException {

		try {
			// Verify signature
			PublicKey publicKey = appServices.getDispatcherPublicKey();
			String clusterId = appServices.getClusterId();
			boolean verified = SecureUtils.isVerifySignature(publicKey, clusterId.getBytes(DcemConstants.CHARSET_UTF8),
					registerDispatcherParam.getDispatcherSignature());
			if (verified) {
				appServices.setDispatcherPreferences(registerDispatcherParam.getDomainName(), registerDispatcherParam.getPnKey());
				return new RegisterDispatcherResponse(clusterId);
			} else {
				String errorMessage = "Dcem Registration failed - invalid signature: " + registerDispatcherParam;
				logger.warn(errorMessage);
				throw createAppException(AppErrorCodes.INVALID_CLIENT_SIGNATURE.name(), errorMessage);
			}
		} catch (AppException e) {
			throw e;
		} catch (DcemException e) {
			logger.error("Could not set configuration: " + e.getMessage());
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), e.getMessage());
		} catch (Exception e) {
			logger.error("Corrupted dcem registration signature: " + e.getMessage());
			throw createAppException(AppErrorCodes.INVALID_CLIENT_SIGNATURE.name(), e.getMessage());
		}
	}

	@Override
	public AuthUserResponse authenticateUser(AuthUserParam authUserParam) throws AppException, TException {
		AuthUserResponse authUserResponse = null;
		try {
			AuthGatewayServices authGatewayServices = CdiUtils.getReference(AuthGatewayServices.class);
			authUserResponse = authGatewayServices.authenticateUser(authUserParam);
		} catch (ExceptionReporting exp) {
			if (exp.getReporting() != null) {
				logger.info(" authenticateUser Error: " + exp.getReporting().toString());
				DcemReporting reporting = exp.getReporting();
				reportingLogic.addReporting(reporting);
			}
			throw new AppException(exp.getErrorCause());
		} catch (Exception exp) {
			logger.warn("authenticateUser went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.Activation, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());

			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		logger.info("authenticateUser for " + authUserParam.getLoginId());
		return authUserResponse;
	}

	@Override
	public AuthSelectResponse getAuthenticationMethods(AuthSelectParam authSelectParam) throws AppException, TException {
		AuthSelectResponse authSelectResponse = null;
		try {
			AuthGatewayServices authGatewayServices = CdiUtils.getReference(AuthGatewayServices.class);
			authSelectResponse = authGatewayServices.getAuthMethods(authSelectParam);
		} catch (ExceptionReporting exp) {
			if (exp.getReporting() != null) {
				logger.info(" getAuthenticationMethods Error: " + exp.getReporting().toString());
				DcemReporting reporting = exp.getReporting();
				reportingLogic.addReporting(reporting);
			}
			throw new AppException(exp.getErrorCause());
		} catch (Exception exp) {
			logger.warn("getAuthenticationMethods went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.GetAuthMethods, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		return authSelectResponse;
	}

	@Override
	public List<SdkCloudSafe> getCloudSafeList(String nameFilter, boolean includeShare, long modifiedFromEpoch, CloudSafeOwner owner)
			throws AppException, TException {
		AppSession appSession = AppWsConnection.getInstance().getAppSession();
		try {
			return appServices.getCloudSafeList(appSession.getUserId(), nameFilter, modifiedFromEpoch, owner, appSession.getLibVersion().version);
		} catch (DcemException exp) {
			logger.info("getCloudSafeList failed.", exp);
			reportingLogic.addReporting(new DcemReporting(ReportAction.GetAccessibleCloudDataFilenames, appSession.getDevice().getUser(),
					AsUtils.convertToAppErrorCodes(exp.getErrorCode()), null,
					"device: " + appSession.getDevice().getName() + ", exception: " + exp.toString()));
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		} catch (Exception e) {
			logger.info("getCloudSafeList failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
	}

	@Override
	public void renameCloudSafe(SdkCloudSafeKey uniqueKey, String userLoginId, String newName) throws AppException, TException {
		try {
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			appServices.renameCloudSafe(uniqueKey, newName, appSession.getUserId());
		} catch (DcemException exp) {
			logger.debug("renameCloudSafe failed.", exp);
			throw createAppException(exp.getErrorCode().name(), exp.getMessage());
		} catch (Exception e) {
			logger.info("renameCloudSafe failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
	}

	@Override
	public void deleteCloudSafe(SdkCloudSafeKey uniqueKey, String userLoginId) throws AppException, TException {
		try {
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			appServices.deleteCloudSafe(uniqueKey, appSession.getUserId());
		} catch (DcemException exp) {
			logger.info("deleteCloudSafe failed.", exp);
			throw createAppException(exp.getErrorCode().name(), exp.getMessage());
		} catch (Exception e) {
			logger.info("deleteCloudSafe failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}
	}

	@Override
	public QrCodeResponse getLoginQrCode(String operatorId, String sessionId) throws AppException, TException {
		try {
			RequestLoginQrCodeResponse response = appServices.generateLoginQrCode(operatorId, sessionId);
			return new QrCodeResponse(response.getTimeToLive(), response.getData());
		} catch (DcemException e) {
			logger.error("Could not request QR Code: " + e.getMessage());
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), e.getMessage());
		}
	}

	@Override
	public List<DeviceOfflineKey> getDeviceOfflineKeys() throws AppException, TException {
		try {
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			return appServices.getDeviceOfflineKeys(appSession.getUserId());
		} catch (DcemException e) {
			logger.error("DcemException while getting Device Offline Keys: " + e.getMessage());
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), e.getMessage());
		} catch (Exception e) {
			logger.error("Unexpected error while getting Device Offline Keys: " + e.getMessage());
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), e.getMessage());
		}
	}

	@Override
	public ByteBuffer proxyData(long handle, ByteBuffer byteBuffer) throws AppException, TException {
		try {
			AuthGatewayServices authGatewayServices = CdiUtils.getReference(AuthGatewayServices.class);
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			authGatewayServices.receiveDataAuthProxy(appSession, handle, byteBuffer);
			return ByteBuffer.allocate(1);
		} catch (DcemException exp) {
			logger.info("proxyData failed.", exp);
			reportingLogic.addReporting(
					new DcemReporting(ReportAction.ProxyData, (DcemUser) null, AsUtils.convertToAppErrorCodes(exp.getErrorCode()), null, exp.toString()));
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		} catch (Exception e) {
			logger.info("proxyData failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}

	}

	@Override
	public void proxyClose(long handle) throws TException {
		try {
			AuthGatewayServices authGatewayServices = CdiUtils.getReference(AuthGatewayServices.class);
			AppSession appSession = AppWsConnection.getInstance().getAppSession();
			authGatewayServices.receiveCloseAuthProxy(appSession, handle);
		} catch (Exception e) {
			logger.info("proxyData failed.", e);
			throw new AppException(AppErrorCodes.UNEXPECTED_ERROR.name());
		}

	}

	@Override
	public int authConnect(AuthConnectParam authConnectParam) throws AppException, TException {
		int keepAliveSeconds;
		try {
			AuthGatewayServices authGatewayServices = CdiUtils.getReference(AuthGatewayServices.class);
			keepAliveSeconds = authGatewayServices.authConnect(authConnectParam);
		} catch (ExceptionReporting exp) {
			if (exp.getReporting() != null) {
				logger.info(" authConnect Error: " + exp.getReporting().toString());
				DcemReporting reporting = exp.getReporting();
				reportingLogic.addReporting(reporting);
			}
			throw new AppException(exp.getErrorCause());
		} catch (Exception exp) {
			logger.warn("authConnect went wrong: ", exp);
			DcemReporting reporting = new DcemReporting(ReportAction.AuthConnect, (DcemUser) null, AppErrorCodes.UNEXPECTED_ERROR, null, exp.getMessage());
			reportingLogic.addReporting(reporting);
			throw createAppException(AppErrorCodes.UNEXPECTED_ERROR.name(), exp.getMessage());
		}
		logger.info("authConnectParam for " + authConnectParam.toString());
		return keepAliveSeconds;
	}

	private AppException createAppException(String error, String info) {
		AppException appException = new AppException(error);
		appException.setInfo(info);
		return appException;
	}

}
