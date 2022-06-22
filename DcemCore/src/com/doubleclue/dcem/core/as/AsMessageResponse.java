package com.doubleclue.dcem.core.as;

import java.io.Serializable;

/**
 * 
 * 
 * @author Emanuel Galea
 */

@SuppressWarnings("serial")
public class AsMessageResponse implements Serializable {
	// private static Logger logger = LogManager.getLogger(AppDevice.class);

	private long id;

	AsMsgStatus msgStatus;

	String actionId;

	String sessionId;

	String userLoginId;

	private int sessionCookieExpiresOn;

	private String sessionCookie = null;

	public AsMessageResponse(long id, AsMsgStatus msgStatus, String actionId, String sessionId, String userLoginId) {
		super();
		this.id = id;
		this.msgStatus = msgStatus;
		this.actionId = actionId;
		this.sessionId = sessionId;
		this.userLoginId = userLoginId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return new Long(id).hashCode();
	}

	public AsMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
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

}