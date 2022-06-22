package com.doubleclue.dcem.as.restapi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * AsApiMessageResponse
 */

@SuppressWarnings("serial")
public class AsApiMessageResponse implements Serializable {

	private long id = 0l;

	private boolean _final = false;

	private String deviceName = null;

	private String userLoginId = null;

	private AsApiMsgStatus msgStatus = null;

	private List<AsMapEntry> inputMap = new ArrayList<AsMapEntry>();

	private String actionId = null;

	private String info = null;

	private String sessionId = null;

	private byte[] signature = null;
	
	private int sessionCookieExpiresOn;
	
	private String sessionCookie = null;
	
	private boolean stayLoggedInAllowed;

	/**
	 * Get id
	 * 
	 * @return id
	 **/
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Get _final
	 * 
	 * @return _final
	 **/
	public boolean getFinal() {
		return _final;
	}

	public void setFinal(boolean _final) {
		this._final = _final;
	}

	/**
	 * This is only available when 'final' is true.
	 * 
	 * @return deviceName
	 **/
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * Get msgStatus
	 * 
	 * @return msgStatus
	 **/
	public AsApiMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsApiMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	/**
	 * if template contains input fields, these are the input data entered by the
	 * user. Only set when final is true
	 * 
	 * @return inputMap
	 **/
	public List<AsMapEntry> getInputMap() {
		return inputMap;
	}

	public void setInputMap(List<AsMapEntry> inputMap) {
		this.inputMap = inputMap;
	}

	/**
	 * This is the template's Button's ID which the user activate.
	 * 
	 * @return actionId
	 **/
	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public AsApiMessageResponse info(String info) {
		this.info = info;
		return this;
	}

	/**
	 * Get info
	 * 
	 * @return info
	 **/
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * This is optional. Value returned from addMessage.
	 * 
	 * @return sessionId
	 **/
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * This is optional. Available only if \"signatureRequired\" was true and
	 * 'final' is true.
	 * 
	 * @return signature
	 **/
	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AsApiMessageResponse asApiMessageResponse = (AsApiMessageResponse) o;
		return Objects.equals(this.id, asApiMessageResponse.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AsApiMessageResponse {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    _final: ").append(toIndentedString(_final)).append("\n");
		sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
		sb.append("    userLoginId: ").append(toIndentedString(userLoginId)).append("\n");
		sb.append("    msgStatus: ").append(toIndentedString(msgStatus)).append("\n");
		sb.append("    inputMap: ").append(toIndentedString(inputMap)).append("\n");
		sb.append("    actionId: ").append(toIndentedString(actionId)).append("\n");
		sb.append("    info: ").append(toIndentedString(info)).append("\n");
		sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public int getSessionCookieExpiresOn() {
		return sessionCookieExpiresOn;
	}

	public void setSessionCookieExpiresOn(int sessionCookieExpiresOn) {
		this.sessionCookieExpiresOn = sessionCookieExpiresOn;
	}

	public String getSessionCookie() {
		return sessionCookie;
	}

	public void setSessionCookie(String sessionCookie) {
		this.sessionCookie = sessionCookie;
	}

	public boolean isStayLoggedInAllowed() {
		return stayLoggedInAllowed;
	}

	public void setStayLoggedInAllowed(boolean stayLoggedInAllowed) {
		this.stayLoggedInAllowed = stayLoggedInAllowed;
	}
}
