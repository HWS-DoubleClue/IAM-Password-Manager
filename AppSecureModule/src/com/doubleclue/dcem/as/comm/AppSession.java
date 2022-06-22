package com.doubleclue.dcem.as.comm;

import java.io.Serializable;

import org.apache.thrift.protocol.TProtocol;

import com.doubleclue.comm.thrift.AppVersion;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.ServerToApp;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.core.entities.TenantEntity;

@SuppressWarnings("serial")
public class AppSession implements Serializable {

	transient WsSessionI wsSession;
	ConnectionState state = ConnectionState.init;
	int deviceId;
	DeviceEntity device;
	String oneTimePassword;
	long timeStamp;
	long pendingMsgId;
	String domainName;
	CommClientType commClientType;
	byte [] passwordEncryptionKey;
	
	transient AppSession reverseProxySession;
	private transient AppVersion appVersion;
	transient AppVersion libVersion;
	transient ServerToApp.Client serverToApp;
	transient TProtocol protocolAtoS;
	private int userId;
	TenantEntity tenantEntity;

	public AppSession(WsSessionI wsSession, ServerToApp.Client serverToApp, TProtocol protocolAtoS) {
		super();
		this.wsSession = wsSession;
		this.protocolAtoS = protocolAtoS;
		this.serverToApp = serverToApp;
	}
	
//	public AppSession(Session session, ServerToApp.Client serverToApp) {
//		super();
//		this.session = session;
//		this.serverToApp = serverToApp;
//	}

	
	public ConnectionState getState() {
		return state;
	}

	public void setState(ConnectionState state) {
		this.state = state;
	}

	public int getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}

	public String getOneTimePassword() {
		return oneTimePassword;
	}

	public void setOneTimePassword(String oneTimePassword) {
		this.oneTimePassword = oneTimePassword;
	}

	public AppVersion getAppVersion() {
		return appVersion;
	}

	public void setAppVersion(AppVersion appVersion) {
		this.appVersion = appVersion;
	}

	public AppVersion getLibVersion() {
		return libVersion;
	}

	public void setLibVersion(AppVersion libVersion) {
		this.libVersion = libVersion;
	}

//	public byte[] getReconnectTicket() {
//		return reconnectTicket;
//	}
//
//	public void setReconnectTicket(byte[] reconnectTicket) {
//		this.reconnectTicket = reconnectTicket;
//	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public ServerToApp.Client getServerToApp() {
		return serverToApp;
	}

	public void setServerToApp(ServerToApp.Client serverToApp) {
		this.serverToApp = serverToApp;
	}

	public DeviceEntity getDevice() {
		return device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public long getPendingMsgId() {
		return pendingMsgId;
	}

	public void setPendingMsgId(long pendingMsgId) {
		this.pendingMsgId = pendingMsgId;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public WsSessionI getWsSession() {
		return wsSession;
	}

	public void setWsSession(WsSessionI wsSession) {
		this.wsSession = wsSession;
	}

	public AppSession getReverseProxySession() {
		return reverseProxySession;
	}

	public void setReverseProxySession(AppSession reverseProxySession) {
		this.reverseProxySession = reverseProxySession;
	}

	public CommClientType getCommClientType() {
		return commClientType;
	}

	public void setCommClientType(CommClientType commClientType) {
		this.commClientType = commClientType;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public TenantEntity getTenantEntity() {
		return tenantEntity;
	}

	public void setTenantEntity(TenantEntity tenantEntity) {
		this.tenantEntity = tenantEntity;
	}
		
}
