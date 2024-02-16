package com.doubleclue.dcem.as.comm;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.AppMessage;
import com.doubleclue.comm.thrift.AppMessageResponse;
import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.cluster.AsCluster;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsMessageLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsPreferences;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DeviceStatus;
import com.doubleclue.dcem.as.logic.DevicesUserDto;
import com.doubleclue.dcem.as.logic.ExceptionReporting;
import com.doubleclue.dcem.as.logic.FcmLogic;
import com.doubleclue.dcem.as.logic.MsgGui;
import com.doubleclue.dcem.as.logic.MsgStoragePolicy;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.as.logic.PushNotificationConfig;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.policy.PolicyTransaction;
import com.doubleclue.dcem.as.restapi.model.AddMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMessage;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.as.restapi.model.AsMapEntry;
import com.doubleclue.dcem.as.tasks.CheckMessageTask;
import com.doubleclue.dcem.as.tasks.SendMessageTask;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.SecureUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.query.Predicate;
import com.hazelcast.query.Predicates;

@ApplicationScoped
public class AsMessageHandler {

	private static Logger logger = LogManager.getLogger(TemplateLogic.class);

	@Inject
	EntityManager em;

	@Inject
	UserLogic userLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	AsModule asModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	AppServices appServices;

	@Inject
	AsMessageLogic messageLogic;

	@Inject
	OperatorSessionBean operatorSession;

	@Inject
	AsCluster asCluster;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	Event<PendingMsg> fireMsg;

	@Inject
	FcmLogic fcmLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@PostConstruct
	public void init() {
	}

	/**
	 * @param message
	 * @throws DcemException
	 */
	public AddMessageResponse sendMessage(AsApiMessage apiMessage, DcemUser toUser, DcemUser fromUser, AuthApplication authApplication, int subId, PolicyEntity policyEntity,
			String info) throws DcemException {
		if (toUser == null) {
			toUser = userLogic.getUser(apiMessage.getUserLoginId());
		}
		if (toUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, apiMessage.getUserLoginId());
		}
		if (toUser.isDisabled()) {
			throw new DcemException(DcemErrorCodes.USER_DISABLED, apiMessage.getUserLoginId());
		}
		PolicyAppEntity policyAppEntity = policyLogic.getDetachedPolicyApp(authApplication, subId);
		if (policyAppEntity == null) {
			policyAppEntity = new PolicyAppEntity(authApplication, subId, null);
//			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't find PolicyApp: " + authApplication + "/" + subId);
		}
		// TODO check the other input fields
		AppSession appSession = null;
		AsTenantData tenantData = asModule.getTenantData();

		Collection<PendingMsg> userPendingMsgs = getUserPendingMsgs(toUser.getId(), tenantData);
		int queueLength = 0;
		if (asModule.getPreferences().isClearQueuedMsgsOnNewMsg()) {
			for (PendingMsg msg : userPendingMsgs) {
				if (msg.getMsgStatus() == AsApiMsgStatus.QUEUED) {
					tenantData.getPendingMsgs().remove(msg.getId());
					continue;
				}
				queueLength++;
			}
		} else {
			queueLength = userPendingMsgs.size();
		}
		if (queueLength >= asModule.getPreferences().getMaxMessageQueueLength()) {
			throw new DcemException(DcemErrorCodes.EXCEED_USER_MESSAGE_QUEUE, toUser.getLoginId());
		}

		try {
			if (apiMessage.getTemplateName() == null || apiMessage.getTemplateName().isEmpty()) {
				apiMessage.setTemplateName(asModule.getPreferences().getDefaultTemplate());
				if (apiMessage.getTemplateName() == null || apiMessage.getTemplateName().isEmpty()) {
					throw new DcemException(DcemErrorCodes.NO_DEFAULT_TEMPLATE_FOUND, "Template name is null or empty");
				}
			}
			DcemTemplate template = templateLogic.getDefaultTemplate(apiMessage.getTemplateName());
			if (template == null) {
				throw new DcemException(DcemErrorCodes.NO_DEFAULT_TEMPLATE_FOUND, "For: " + apiMessage.getTemplateName());
			}
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.NO_DEFAULT_TEMPLATE_FOUND, e.toString(), e);
		}

		int responseTimeout = apiMessage.getResponseTimeout();
		if (responseTimeout == 0) {
			responseTimeout = asModule.getPreferences().getMessageResponseTimeout();
		}

		long msgId = tenantData.getMsgIdGenerator().newId();
		PendingMsg pendingMsg = null;

		pendingMsg = new PendingMsg(msgId, toUser, responseTimeout, apiMessage.getTemplateName(), convertToMap(apiMessage.getDataMap()),
				apiMessage.isAllowPasswordLess());
		if (policyEntity == null) {
			pendingMsg.setPolicyTransaction(new PolicyTransaction(null, null, policyAppEntity, toUser.getId()));
		} else {
			pendingMsg.setPolicyTransaction(new PolicyTransaction(policyEntity.getDcemPolicy(), policyEntity.getName(), policyAppEntity, toUser.getId()));
		}
		pendingMsg.setSessionId(apiMessage.getSessionId());
		pendingMsg.setResponseRequired(apiMessage.getResponseRequired());
		pendingMsg.setInfo(info);
		if (policyAppEntity.getAuthApplication() != AuthApplication.AuthGateway && fromUser != null) {
			pendingMsg.setOperatorId(fromUser.getId());
		}
		pendingMsg.setSignitureRequired(apiMessage.getSignatureRequired());
		pendingMsg.setNotifyNodeOnResponse(apiMessage.getNotifyNodeOnResponse());

		List<DevicesUserDto> devicesUserDtos = deviceLogic.getDevicesByUser(toUser);
		pendingMsg.setTtl(responseTimeout + asModule.getPreferences().getMessageRetrieveTimeoutSec());
		if (devicesUserDtos.isEmpty()) {
			throw new DcemException(DcemErrorCodes.USER_HAS_NO_DEVICES, toUser.getLoginId());
		}

		int sentDeviceId = 0;
		for (DevicesUserDto devicesUserDto : devicesUserDtos) {
			if (devicesUserDto.getStatus() == DeviceStatus.Online
					|| (apiMessage.isAllowPasswordLess() == true && devicesUserDto.getStatus() == DeviceStatus.OnlinePasswordLess)) {
				if (devicesUserDto.getNodeId() == (DcemCluster.getInstance().getDcemNode().getId())) {
					// Oh device is connected to this node.
					appSession = tenantData.getDeviceSessions().get(devicesUserDto.getId());
					if (appSession != null) {
						if (appSession.state == ConnectionState.loggedIn
								|| (apiMessage.isAllowPasswordLess() == true && appSession.state == ConnectionState.loggedInPasswordLess)) {
							pendingMsg.setDeviceId(devicesUserDto.getId());
							sendMessageToClient(appSession, pendingMsg, tenantData);
							sentDeviceId = devicesUserDto.getId();
							// System.out.println("AsMessageHandler.sendMessage()2"
							// + appSession.state);
						} else {
							sentDeviceId = -1; // device is pending, do not send PN
						}
						break;
					}
				} else {
					// online on other node
					pendingMsg.setDeviceId(devicesUserDto.getId());
					setMessage(pendingMsg, tenantData);
					try {
						asCluster.msgToDevice(applicationBean.getDcemNodeById(devicesUserDto.getNodeId()), msgId);
						sentDeviceId = devicesUserDto.getId();
						break;
					} catch (Exception e) {
						logger.info(e);
					}
				}
			}
		}
		boolean withPushNotification = false;
		if (sentDeviceId == 0) {
			pendingMsg.setMsgStatus(AsApiMsgStatus.QUEUED);
			setMessage(pendingMsg, tenantData);
			PushNotificationConfig pushNotificationConfig = asModule.getTenantData().getPushNotificationConfig();
			if (pushNotificationConfig != null && pushNotificationConfig.isEnable()) {
				try {
					List<Integer> cloudSafes = cloudSafeLogic.getCloudSafeFromIds(devicesUserDtos, AppSystemConstants.PUSH_NOTIFICATION_TOKEN);
					if (cloudSafes.isEmpty() == false) {
						Set<String> pnIds = new HashSet<String>(cloudSafes.size());
						for (Integer id : cloudSafes) {
							pnIds.add(cloudSafeLogic.getContentAsStringWoChiper(id));
						}
						try {
							Collection<PendingMsg> userMsgs = getUserPendingMsgs(toUser.getId(), tenantData);
							boolean withPasswordLess = apiMessage.isAllowPasswordLess();
							if (userMsgs.size() > 1) {
								withPasswordLess = false; // send passwordLess when this message is first in the queue.
							}
							fcmLogic.pushNotification(pnIds, toUser, withPasswordLess);
							withPushNotification = true;
						} catch (DcemException exp) {
							logger.error("Could't send Push Notification To Google FireBase: ", exp);
						}
					}

				} catch (Exception exp) {
					logger.error("Could't send Push Notification for " + toUser.getLoginId(), exp);
				}
			}
		}
		if (sentDeviceId == -1) {
			pendingMsg.setMsgStatus(AsApiMsgStatus.QUEUED);
			setMessage(pendingMsg, tenantData);
		}

		return new AddMessageResponse(msgId, responseTimeout, withPushNotification);
	}

	/**
	 * This method is called from CLUSTER
	 * 
	 * @param msgId
	 * @throws DcemException
	 */
	public void sendMsgToClientFromCluster(long msgId) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = tenantData.getPendingMsgs().get(msgId);
		if (pendingMsg == null) {
			throw new DcemException(DcemErrorCodes.MESSAGE_NOT_FOUND, Long.toString(msgId));
		}
		AppSession appSession = tenantData.getDeviceSessions().get(pendingMsg.getDeviceId());
		if (appSession == null || appSession.state != ConnectionState.loggedIn) {
			throw new DcemException(DcemErrorCodes.DEVICE_NOT_FOUND, Integer.toString(pendingMsg.getDeviceId()));
		}
		sendMessageToClient(appSession, pendingMsg, tenantData);
	}

	/**
	 * @param appSession
	 * @param pendingMsg
	 * @throws DcemException
	 */
	private void sendMessageToClient(AppSession appSession, PendingMsg pendingMsg, AsTenantData tenantData) throws DcemException {
		AppMessage appMessage = new AppMessage();
		appMessage.setData(pendingMsg.getOutputData());
		DcemTemplate template = null;
		if (pendingMsg.getTemplateId() == 0) {
			try {
				SupportedLanguage supportedLanguage = DcemUtils.getSuppotedLanguage(appSession.getDevice().getLocale());
				template = templateLogic.getTemplateByNameLanguage(pendingMsg.getTemplateName(), supportedLanguage);
				if (template == null) {
					pendingMsg.setMsgStatus(AsApiMsgStatus.SEND_ERROR);
					pendingMsg.setInfo(DcemErrorCodes.NO_TEMPLATE_FOUND.name() + " - name=" + pendingMsg.getTemplateName() + ", Locale="
							+ appSession.getDevice().getLocale());
					pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
					pendingMsg.setResponseTime(0);
					setMessage(pendingMsg, tenantData);
					return;
				}
				if (template.isInUse() == false) {
					templateLogic.setTemplateInUse(template);
				}
				pendingMsg.setTemplateId(template.getId());
				// set user language according to device language.
				if (appSession.getDevice().getUser().getLanguage() == null) {
					userLogic.setUserLanguage(appSession.getDevice().getUser(), template.getLanguage());
				}

			} catch (Exception exp) {
				logger.info(exp);
				pendingMsg.setMsgStatus(AsApiMsgStatus.SEND_ERROR);
				pendingMsg.setInfo(exp.getMessage());
				pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
				pendingMsg.setResponseTime(0);
				setMessage(pendingMsg, tenantData);
				return;
			}
		}
		pendingMsg.setDeviceId(appSession.deviceId);
		pendingMsg.setDeviceName(appSession.device.getName());
		appMessage.setTemplateId(pendingMsg.getTemplateId());
		appMessage.setResponseRequired(pendingMsg.isResponseRequired());
		appMessage.setResponseTime(pendingMsg.getResponseTime());
		appMessage.setId(pendingMsg.getId());
		appMessage.setSignitureRequired(pendingMsg.isSignitureRequired());
		pendingMsg.setMsgStatus(AsApiMsgStatus.SENDING);
		appSession.setPendingMsgId(pendingMsg.getId());
		appSession.setState(ConnectionState.messagePending);
		pendingMsg.setTtl(pendingMsg.getResponseTime() + asModule.getPreferences().getMessageRetrieveTimeoutSec());

		setMessage(pendingMsg, tenantData);
		appSession.setState(ConnectionState.messagePending);
		taskExecutor.schedule(new SendMessageTask(appSession, appMessage, pendingMsg, TenantIdResolver.getCurrentTenant()), 20, TimeUnit.MILLISECONDS);
	}
	
	private void storeMsgDb (PendingMsg pendingMsg, boolean retrieved) {
		AsPreferences preferences = asModule.getPreferences();
		if (preferences.getMessageStorePolicy() == null || 	preferences.getMessageStorePolicy() == MsgStoragePolicy.Dont_Store) {
			return;
		}
		messageLogic.addUserMsg(pendingMsg, retrieved);
	}

	public void messageSent(AppSession appSession, PendingMsg pendingMsg) {
		AsTenantData tenantData = asModule.getTenantData();
		if (pendingMsg.isResponseRequired() == false) {
			if (pendingMsg.getMsgStatus() != AsApiMsgStatus.SEND_ERROR) {
				pendingMsg.setMsgStatus(AsApiMsgStatus.OK);
			}
			storeMsgDb(pendingMsg, false);
			appSession.setState(ConnectionState.loggedIn);
			tenantData.getPendingMsgs().remove(pendingMsg.getId());
			try {
				checkPendingMessages(appSession, tenantData);
			} catch (DcemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}

		if (pendingMsg.getMsgStatus() == AsApiMsgStatus.SEND_ERROR) {
			// messageLogic.add(pendingMsg, appSession.getDevice());
			pendingMsg.setResponseTime(0);
			pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
			setMessage(pendingMsg, tenantData);
			AppWsConnection.getInstance().closeSession(appSession, null);
			return;
		}

		appSession.setState(ConnectionState.messagePending);
		appSession.setPendingMsgId(pendingMsg.getId());
		pendingMsg.setMsgStatus(AsApiMsgStatus.WAITING);
		pendingMsg.setTtl(pendingMsg.getResponseTime() + asModule.getPreferences().getMessageRetrieveTimeoutSec());
		setMessage(pendingMsg, tenantData);
	}

	/**
	 * @param appMessageResponse
	 * @param appSession
	 * @throws AppException
	 * @throws ExceptionReporting
	 */
	public void receivedMsgResponse(AppMessageResponse appMsgResponse, AppSession appSession) throws AppException, ExceptionReporting {
		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = tenantData.getPendingMsgs().get(appMsgResponse.id);
		// look for next message in 2 seconds
		taskExecutor.schedule(new CheckMessageTask(appSession, TenantIdResolver.getCurrentTenant(), tenantData), 2000, TimeUnit.MILLISECONDS);

		if (appSession.state == ConnectionState.messagePending) {
			appSession.state = ConnectionState.loggedIn;
		}
		if (pendingMsg == null) {
			throw new ExceptionReporting(new DcemReporting(ReportAction.MessageResponse, appSession.getDevice().getUser(),
					AppErrorCodes.RESPONSE_MESSAGE_INVALID_ID, null, "device: " + appSession.getDevice().getName() + ", message id: " + appMsgResponse.getId()),
					null);
		}

		if (appMsgResponse.errorCode != AppErrorCodes.OK) {
			pendingMsg.setMsgStatus(AsApiMsgStatus.REC_ERROR);
			pendingMsg.setInfo(appMsgResponse.getErrorMessage());
		} else {
			if (pendingMsg.isSignitureRequired()) {
				PublicKey publicKey;
				try {
					publicKey = SecureUtils.loadPublicKey(appSession.device.getPublicKey());
					if (SecureUtils.isVerifySignature(publicKey, appMsgResponse.getSignature(), pendingMsg.getTemplateId(),
							KaraUtils.mapToString(pendingMsg.getOutputData()), appMsgResponse.getActionId(),
							KaraUtils.mapToString(appMsgResponse.getResponseData())) == false) {
						throw new Exception(AppErrorCodes.INVALID_MESSAGE_SIGNATURE.name());
					}
				} catch (Exception e) {
					logger.warn("receivedMsgResponse SignVerification Exception", e);
					pendingMsg.setMsgStatus(AsApiMsgStatus.SIGNATURE_ERROR);
					pendingMsg.setResponseTime(0);
					pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
					setMessage(pendingMsg, tenantData);
					throw new ExceptionReporting(new DcemReporting(ReportAction.MessageResponse, appSession.getDevice().getUser(),
							AppErrorCodes.INVALID_MESSAGE_SIGNATURE, null, Long.toString(appMsgResponse.getId())), null);
				}
			}
			pendingMsg.setResponseData((HashMap<String, String>) appMsgResponse.getResponseData());
			pendingMsg.setActionId(appMsgResponse.getActionId());
			pendingMsg.setMsgStatus(AsApiMsgStatus.OK);
		}
		pendingMsg.setResponseTime(0);
		pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
		setMessage(pendingMsg, tenantData);
		if (pendingMsg.getNotifyNodeOnResponse() != 0) {
			try {
				asCluster.msgResponseReceived(pendingMsg, TenantIdResolver.getCurrentTenant());
			} catch (DcemException e) {
				throw new ExceptionReporting(new DcemReporting(ReportAction.MessageResponse, appSession.getDevice().getUser(), AppErrorCodes.UNEXPECTED_ERROR,
						null, "device: " + appSession.getDevice().getName() + ", message id: " + appMsgResponse.getId()), e.toString(), e);
			}
		} else {
			if (pendingMsg.getPolicyTransaction().getPolicyAppEntity().getAuthApplication() == AuthApplication.DCEM) {
				storeMsgDb(pendingMsg, false);
				tenantData.getPendingMsgs().remove(pendingMsg.getId());
			}
		}
	}

	private Collection<PendingMsg> getUserPendingMsgs(int userId, AsTenantData tenantData) {
		// EntryObject eb = new PredicateBuilder().getEntryObject();
		Predicate<?, ?> userPredicate = Predicates.equal("userId", userId);
		return tenantData.getPendingMsgs().values(userPredicate);
	}

	/**
	 * @param appSession
	 * @throws DcemException
	 */
	public void checkPendingMessages(AppSession appSession, AsTenantData tenantData) throws DcemException {
		if (appSession.getState() == ConnectionState.loggedIn || appSession.getState() == ConnectionState.loggedInPasswordLess) {
			Collection<PendingMsg> userMsgs = getUserPendingMsgs(appSession.getDevice().getUser().getId(), tenantData);
			List<PendingMsg> list = new ArrayList<PendingMsg>(userMsgs);
			Collections.sort(list, (p1, p2) -> (int) (p1.getTimeStamp() - p2.getTimeStamp()));
			if (userMsgs.isEmpty() == false) {
				PendingMsg pendingMsg = null;
				for (PendingMsg pendingMsg2 : list) {
					if (pendingMsg2.getDeviceId() < 1 || (pendingMsg2.getMsgStatus() == AsApiMsgStatus.QUEUED)) {
						pendingMsg = pendingMsg2;
						break;
					}
				}
				if (pendingMsg != null) {
					logger.debug("send pending message");
					if (pendingMsg.isAllowPasswordLess() == false && appSession.getState() == ConnectionState.loggedInPasswordLess) {
						return;
					}
					sendMessageToClient(appSession, pendingMsg, tenantData);
				}
			}
		}

	}

	public void disconnectedPendingMsg(AppSession appSession, AsApiMsgStatus apiMsgStatus) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = null;
		if (appSession.getPendingMsgId() > 0) {
			pendingMsg = tenantData.getPendingMsgs().get(appSession.getPendingMsgId());
		}
		if (pendingMsg != null) {
			pendingMsg.setMsgStatus(apiMsgStatus);
			pendingMsg.setResponseTime(0);
			pendingMsg.setTtl(asModule.getPreferences().getMessageRetrieveTimeoutSec());
			setMessage(pendingMsg, tenantData);
			if (pendingMsg.getNotifyNodeOnResponse() != 0) {
				asCluster.msgResponseReceived(pendingMsg, TenantIdResolver.getCurrentTenant());
			}
		}
	}

	/**
	 * @param user
	 * @return
	 */
	public List<MsgGui> getMsgValues(String user) {
		AsTenantData tenantData = asModule.getTenantData();
		Collection<PendingMsg> pendingMsgValues = tenantData.getPendingMsgs().values();
		List<MsgGui> msgs = new ArrayList<>(pendingMsgValues.size());
		MsgGui msgGui;
		for (PendingMsg pendingMsg : pendingMsgValues) {
			msgGui = new MsgGui(pendingMsg);
			msgGui.setUser(userLogic.getUser(pendingMsg.getUserId()).getLoginId());
			if (pendingMsg.getDeviceId() > 0) {
				msgGui.setDeviceName(deviceLogic.findDevice(pendingMsg.getDeviceId()).getName());
			}
			msgs.add(msgGui);
		}
		return msgs;
	}

	public void evictedMessage(PendingMsg pendingMsg) {
		logger.debug("Message Evicted: " + pendingMsg.toString());
		storeMsgDb(pendingMsg, false);
		// fireMsg.fire(pendingMsg);
	}

	public PendingMsg retrievePendingMsg(long id, int waitTimeMilliSeconds) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();

		PendingMsg pendingMsg = tenantData.getPendingMsgs().get(id);
		if (pendingMsg == null) {
			throw new DcemException(DcemErrorCodes.MESSAGE_NOT_FOUND, null);
		}
		boolean finalStatus = pendingMsg.isFinal();
		if (waitTimeMilliSeconds > 0 && finalStatus == false) {
			if (waitTimeMilliSeconds > AsConstants.MAX_WAIT_RETRIEVE_PENDING_MSG) {
				throw new DcemException(DcemErrorCodes.MAX_WAIT_RETRIEVE_PENDING_MSG, null);
			}
			while (waitTimeMilliSeconds > 0) {
				try {
					Thread.sleep(250);
					waitTimeMilliSeconds = waitTimeMilliSeconds - 250;
				} catch (InterruptedException e) {
					break;
				}
				pendingMsg = tenantData.getPendingMsgs().get(id);
				if (pendingMsg == null) {
					throw new DcemException(DcemErrorCodes.MESSAGE_NOT_FOUND, null);
				}
				if (pendingMsg == null || pendingMsg.isFinal()) {
					break;
				}
			}
		}

		return pendingMsg;
	}

	/**
	 * @param id
	 * @return
	 * @throws DcemException
	 */
	public AsApiMessageResponse retrieveMessageResponse(long id, int waitTimeMilleSeconds) throws DcemException {

		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = retrievePendingMsg(id, waitTimeMilleSeconds);
		boolean finalStatus = pendingMsg.isFinal();

		AsApiMessageResponse apiMessageResponse = new AsApiMessageResponse();
		apiMessageResponse.setFinal(finalStatus);
		apiMessageResponse.setId(pendingMsg.getId());
		apiMessageResponse.setMsgStatus(pendingMsg.getMsgStatus());
		apiMessageResponse.setInfo(pendingMsg.getInfo());
		apiMessageResponse.setActionId(pendingMsg.getActionId());
		apiMessageResponse.setUserLoginId(pendingMsg.getUserLoginId());
		if (finalStatus && (pendingMsg.getDeviceId() > 0)) {
			apiMessageResponse.setDeviceName(pendingMsg.getDeviceName());
		}
		if (finalStatus) {
			if (pendingMsg.getResponseData() != null) {
				apiMessageResponse.setInputMap(convertToList(pendingMsg.getResponseData()));
			}
			tenantData.getPendingMsgs().remove(pendingMsg.getId());
			storeMsgDb(pendingMsg, true);
			authenticationLogic.onSecureMessageResponseReceived(pendingMsg, apiMessageResponse);
		}
		return apiMessageResponse;
	}

	public void onFinalMessage(PendingMsg pendingMsg, boolean retrived) {
		AsTenantData tenantData = asModule.getTenantData();
		storeMsgDb(pendingMsg, retrived);
		tenantData.getPendingMsgs().remove(pendingMsg.getId());
	}

	/**
	 * @param msgId
	 * @throws DcemException
	 */
	public void cancelPendingMsg(Long msgId) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = tenantData.getPendingMsgs().get(msgId);
		if (pendingMsg == null) {
			throw new DcemException(DcemErrorCodes.MESSAGE_NOT_FOUND, null);
		}
		pendingMsg.setMsgStatus(AsApiMsgStatus.CANCELLED);
		storeMsgDb(pendingMsg, false);
		tenantData.getPendingMsgs().remove(pendingMsg.getId());
		return;
	}

	/**
	 * @param userId
	 */
	public void cancelUserPendingMsgs(int userId) {
		AsTenantData tenantData = asModule.getTenantData();
		Collection<PendingMsg> msgs = getUserPendingMsgs(userId, tenantData);
		for (PendingMsg pendingMsg : msgs) {
			pendingMsg.setMsgStatus(AsApiMsgStatus.CANCELLED);
			storeMsgDb(pendingMsg, false);
			tenantData.getPendingMsgs().remove(pendingMsg.getId());
		}

	}

	public void cancelDevicePendingMsgs(DeviceEntity deviceEntity) {
		AsTenantData tenantData = asModule.getTenantData();
		Collection<PendingMsg> msgs = getUserPendingMsgs(deviceEntity.getUser().getId(), tenantData);
		for (PendingMsg pendingMsg : msgs) {
			if (pendingMsg.getDeviceId() == deviceEntity.getId()) {
				pendingMsg.setMsgStatus(AsApiMsgStatus.CANCELLED);
			}
		}
	}

	HashMap<String, String> convertToMap(List<AsMapEntry> list) {
		HashMap<String, String> map = new HashMap<String, String>(list.size());
		for (AsMapEntry entry : list) {
			map.put(entry.getKey(), entry.getValue());
		}
		return map;
	}

	List<AsMapEntry> convertToList(Map<String, String> map) {
		ArrayList<AsMapEntry> list = new ArrayList<>(map.size());
		for (String key : map.keySet()) {
			list.add(new AsMapEntry(key, map.get(key)));
		}
		return list;
	}

	String convertToOutputData(List<AsMapEntry> list) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		if (list == null) {
			return null;
		}
		Map<String, String> map = new HashMap<String, String>(list.size());
		for (AsMapEntry entry : list) {
			map.put(entry.getKey(), entry.getValue());
		}
		return objectMapper.writeValueAsString(map);
	}

	List<AsMapEntry> convertToResponseMap(String responseData) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		AsMapEntry[] entries = objectMapper.readValue(responseData, AsMapEntry[].class);
		return Arrays.asList(entries);
	}

	private void setMessage(PendingMsg pendingMsg, AsTenantData tenantData) {
		pendingMsg.setTimeStamp(System.currentTimeMillis());
		tenantData.getPendingMsgs().set(pendingMsg.getId(), pendingMsg, pendingMsg.getTtl(), TimeUnit.SECONDS);
	}

	public void fireMsgResponseReceived(long msgId) {
		AsTenantData tenantData = asModule.getTenantData();
		PendingMsg pendingMsg = tenantData.getPendingMsgs().get(msgId);
		if (pendingMsg != null) {
			fireMsg.fire(pendingMsg);
		} else {
			logger.debug("message not found. " + msgId);
		}
	}

	// public void onFinalMessage(Long msgId, boolean retrived) {
	// PendingMsg pendingMsg = pendingMsgs.get(msgId);
	// onFinalMessage(pendingMsg, retrived);
	// }
}
