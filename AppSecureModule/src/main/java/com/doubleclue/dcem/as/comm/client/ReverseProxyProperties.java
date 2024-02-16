package com.doubleclue.dcem.as.comm.client;

public class ReverseProxyProperties {
	
	int deviceId;
	String domainName;
	byte [] udid;
	byte [] encryptedPv;
	byte [] publicKey;
	
	public ReverseProxyProperties() {
		
	}
	
	public ReverseProxyProperties(int deviceId, String domainName, byte[] udid, byte[] encryptedPv, byte[] publicKey) {
		super();
		this.deviceId = deviceId;
		this.domainName = domainName;
		this.udid = udid;
		this.encryptedPv = encryptedPv;
		this.publicKey = publicKey;
	}
	
	public int getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(int deviceId) {
		this.deviceId = deviceId;
	}
	
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public byte[] getUdid() {
		return udid;
	}
	public void setUdid(byte[] udid) {
		this.udid = udid;
	}
	
	public byte[] getEncryptedPv() {
		return encryptedPv;
	}
	public void setEncryptedPv(byte[] encryptedPv) {
		this.encryptedPv = encryptedPv;
	}
	public byte[] getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

}
