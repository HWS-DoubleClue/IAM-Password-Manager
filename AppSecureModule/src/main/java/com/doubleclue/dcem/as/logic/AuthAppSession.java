package com.doubleclue.dcem.as.logic;

import java.util.Hashtable;

import com.doubleclue.comm.thrift.AuthUserResponse;
import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.entities.DcemUser;

public class AuthAppSession {

	AppSession appSession;
	long proxyIndex = 0;
	PolicyAppEntity policyAppEntity;
	String userLoginId;
	String workStation;
	String udid;
	String entityName;
	AuthUserResponse authUserResponse;
	Hashtable<Long, AuthProxyListener> authProxyListeners;
	AuthAppState authAppState = AuthAppState.initialized;
	

	public AuthAppState getAuthAppState() {
		return authAppState;
	}

	public void setAuthAppState(AuthAppState authAppState) {
		this.authAppState = authAppState;
	}

	public AuthAppSession(AppSession appSession, PolicyAppEntity policyAppEntity, DcemUser dcemUser, String workStation,
			String udid, String entityName, AuthUserResponse authUserResponse) {
		super();
		this.appSession = appSession;
//		this.messageId = messageId;
		this.policyAppEntity = policyAppEntity;
		if (dcemUser != null) {
			this.userLoginId = dcemUser.getLoginId();
		}
		this.workStation = workStation;
		this.udid = udid;
		this.entityName = entityName;
		this.authUserResponse = authUserResponse;
		
	}

	public AuthAppSession(AppSession appSession) {
		super();
		this.appSession = appSession;
	}

	public AppSession getAppSession() {
		return appSession;
	}

	public void setAppSession(AppSession appSession) {
		this.appSession = appSession;
	}

	public long getMessageId() {
		return appSession.getPendingMsgId();
	}

	public PolicyAppEntity getPolicyAppEntity() {
		return policyAppEntity;
	}

	public void setPolicyAppEntity(PolicyAppEntity policyAppEntity) {
		this.policyAppEntity = policyAppEntity;
	}

	

	public String getWorkStation() {
		return workStation;
	}

	public void setWorkStation(String workStation) {
		this.workStation = workStation;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String uidi) {
		this.udid = uidi;
	}

	public DcemUser getDcemUserDummy() {
		return new DcemUser(appSession.getUserId());
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public void setUserLoginId(String userLoginId) {
		this.userLoginId = userLoginId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public AuthUserResponse getAuthUserResponse() {
		return authUserResponse;
	}

	public void setAuthUserResponse(AuthUserResponse authUserResponse) {
		this.authUserResponse = authUserResponse;
	}
	
	public String getStatus() {
		return appSession.getState().name();
	}

	public long getProxyIndex() {
		return proxyIndex;
	}

	public void setProxyIndex(long proxyIndex) {
		this.proxyIndex = proxyIndex;
	}
	
	public long getNextIndex() {
		proxyIndex++;
		return proxyIndex;
	}
	
	public AuthProxyListener getAuthProxyListener(Long handle) {
		if (authProxyListeners == null) {
			authProxyListeners = new Hashtable<Long, AuthProxyListener>(2);
		}
		return authProxyListeners.get(handle);
	}

	public Hashtable<Long, AuthProxyListener> getAuthProxyListeners() {
		return authProxyListeners;
	}

	public void addAuthProxyListener(Long handle, AuthProxyListener authProxyListener) {
		if (authProxyListeners == null) {
			authProxyListeners = new Hashtable<Long, AuthProxyListener>(2);
		}
		authProxyListeners.put(handle, authProxyListener);
	}

	public void removeAuthProxyListener(long handle) {
		authProxyListeners.remove(handle);
	}
	
	public enum AuthAppState {
		initialized,
		connected,
		inDisconnection,
		disconnected;
	}
	
	public String getActiveProxySessions() {
		StringBuilder builder = new StringBuilder(); 
		if (authProxyListeners == null || authProxyListeners.isEmpty()) {
			return null;
		}
		for (Long handle : authProxyListeners.keySet()) {
			builder.append(handle);
			builder.append(", ");
		}
		return builder.toString();
	}


}
