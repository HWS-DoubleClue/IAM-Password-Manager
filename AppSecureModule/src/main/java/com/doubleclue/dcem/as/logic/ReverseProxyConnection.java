package com.doubleclue.dcem.as.logic;

import java.util.concurrent.ConcurrentHashMap;

import com.doubleclue.dcem.as.comm.AppSession;

public class ReverseProxyConnection {

	AppSession appSession;
	ConcurrentHashMap<String, AppSession> subSessions;

	public ReverseProxyConnection(AppSession appsession) {
		super();
		this.appSession = appsession;

		subSessions = new ConcurrentHashMap<String, AppSession>();
	}

	public AppSession getAppSession() {
		return appSession;
	}

	public AppSession getSubSession(String sessionId) {
		return subSessions.get(sessionId);
	}

	public AppSession addSubSession(AppSession appSession) {
		return subSessions.put(appSession.getWsSession().getSessionId(), appSession);
	}

	public void removeSubSession(String sessionId) {
		if (subSessions != null) {
			subSessions.remove(sessionId);
		}
	}

	public ConcurrentHashMap<String, AppSession> getSubSessions() {
		return subSessions;
	}
	
	

}
