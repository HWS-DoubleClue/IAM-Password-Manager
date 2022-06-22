package com.doubleclue.dcem.as.logic;

import java.io.Serializable;
import java.util.HashMap;

import com.doubleclue.dcem.as.policy.PolicyTransaction;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * 
 * 
 * @author Emanuel Galea
 */

public class PendingMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7457582824172304120L;
	// private static Logger logger = LogManager.getLogger(AppDevice.class);

	private long id;
	private long timeStamp;
	private int deviceId;
	private int userId;
	private int templateId;
	private String templateName;
	private boolean responseRequired;
	private int responseTime = 300;
	private AsApiMsgStatus msgStatus;
	private String info;
	private String actionId;
	private HashMap<String, String> outputData;
	private HashMap<String, String> responseData;
	private int operatorId;
	private String sessionId;
	private int ttl;
	private boolean signitureRequired;
	private int notifyNodeOnResponse;
	private String deviceName;
	private String userLoginId;
	private PolicyTransaction policyTransaction;
	private boolean allowPasswordLess;

	public PendingMsg(long id, DcemUser user, int responseTime, String templteName, HashMap<String, String> data, boolean allowPasswordLess) {
		super();
		this.id = id;
		this.userId = user.getId();
		userLoginId = user.getLoginId();
		this.responseTime = responseTime;
		this.templateName = templteName;
		this.outputData = data;
		timeStamp = System.currentTimeMillis();
		this.allowPasswordLess = allowPasswordLess;
	}

	@DcemGui
	boolean messageRead;

	byte[] signature;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getTemplateId() {
		return templateId;
	}

	public void setTemplateId(int templateId) {
		this.templateId = templateId;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public boolean isMessageRead() {
		return messageRead;
	}

	public void setMessageRead(boolean messageRead) {
		this.messageRead = messageRead;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public boolean isResponseRequired() {
		return responseRequired;
	}

	public void setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}

	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (id != ((PendingMsg) obj).id) {
			return false;
		}
		return true;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public AsApiMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsApiMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public int getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(int operatorId) {
		this.operatorId = operatorId;
	}

	public int getTtl() {
		return ttl;
	}

	public void setTtl(int ttl) {
		this.ttl = ttl;
	}

	public boolean isSignitureRequired() {
		return signitureRequired;
	}

	public void setSignitureRequired(boolean signitureRequired) {
		this.signitureRequired = signitureRequired;
	}

	public HashMap<String, String> getOutputData() {
		return outputData;
	}

	public void setOutputData(HashMap<String, String> outputData) {
		this.outputData = outputData;
	}

	public HashMap<String, String> getResponseData() {
		return responseData;
	}

	public void setResponseData(HashMap<String, String> responseData) {
		this.responseData = responseData;
	}

	public boolean isFinal() {
		switch (msgStatus) {
		case DISCONNECTED:
		case OK:
		case SEND_ERROR:
		case REC_ERROR:
		case CANCELLED:
		case SIGNATURE_ERROR:
			return true;
		default:
			return false;
		}
	}

	public int getNotifyNodeOnResponse() {
		return notifyNodeOnResponse;
	}

	public void setNotifyNodeOnResponse(int notifyNodeOnResponse) {
		this.notifyNodeOnResponse = notifyNodeOnResponse;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public String toString() {
		return "PendingMsg [id=" + id + ", templateName=" + templateName + ", msgStatus=" + msgStatus + ", userLoginId=" + userLoginId;
	}
	
	public boolean isAllowPasswordLess() {
		return allowPasswordLess;
	}

	public void setAllowPasswordLess(boolean allowPasswordLess) {
		this.allowPasswordLess = allowPasswordLess;
	}

	public PolicyTransaction getPolicyTransaction() {
		return policyTransaction;
	}

	public void setPolicyTransaction(PolicyTransaction policyTransaction) {
		this.policyTransaction = policyTransaction;
	}

	

}