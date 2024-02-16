package com.doubleclue.dcem.as.logic;

import java.io.Serializable;
import java.util.Date;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;

@SuppressWarnings("serial")
public class MsgGui implements Serializable {

	long id;
	String user;
	String deviceName;
	String template;
	boolean responseRequired;
	String actionId;
	Date createOn;
	int responseIn;
	int expiresIn;
	PolicyAppEntity policyAppEntity;

	AsApiMsgStatus status;
	String info;

	public MsgGui(PendingMsg pendingMsg) {
		super();
		this.id = pendingMsg.getId();
		this.template = pendingMsg.getTemplateName();
		this.responseRequired = pendingMsg.isResponseRequired();
		this.createOn = new Date(pendingMsg.getTimeStamp());
		if (pendingMsg.getResponseTime() > 0) {
			this.responseIn = pendingMsg.getResponseTime() - (int) ((System.currentTimeMillis() - pendingMsg.getTimeStamp()) / 1000);
		}
		this.expiresIn = pendingMsg.getTtl() - (int) ((System.currentTimeMillis() - pendingMsg.getTimeStamp()) / 1000);

		status = pendingMsg.getMsgStatus();
		this.info = pendingMsg.getInfo();
		this.actionId = pendingMsg.getActionId();
		this.policyAppEntity = pendingMsg.getPolicyTransaction().getPolicyAppEntity();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public boolean isResponseRequired() {
		return responseRequired;
	}

	public void setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}

	public Date getCreateOn() {
		return createOn;
	}

	public void setCreateOn(Date createOn) {
		this.createOn = createOn;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public AsApiMsgStatus getStatus() {
		return status;
	}

	public void setStatus(AsApiMsgStatus status) {
		this.status = status;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public int getResponseIn() {
		return responseIn;
	}

	public void setResponseIn(int responseIn) {
		this.responseIn = responseIn;
	}

}
