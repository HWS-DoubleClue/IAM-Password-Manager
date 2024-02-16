package com.doubleclue.dcem.core.logic;

public class JndiProxyParam {
	
	String sessionId;
	long handle;
	
	
	
	public JndiProxyParam(String sessionId, long handle) {
		super();
		this.sessionId = sessionId;
		this.handle = handle;
	}
	
	public String getSessionId() {
		return sessionId;
	}
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
	public long getHandle() {
		return handle;
	}
	public void setHandle(long handle) {
		this.handle = handle;
	}

	@Override
	public String toString() {
		return "JndiProxyParam [sessionId=" + sessionId + ", handle=" + handle + "]";
	}
	

}
