package com.doubleclue.dcem.as.logic;

import com.doubleclue.dcem.as.comm.client.ReverseProxyProperties;

public class RpConfig {
	
	boolean enableRp;
	
	String domainName;
	
	String password;
	
	int reconnect = 2;
	
	ReverseProxyProperties reverseProxyProperties;
	
	byte [] sdkConfigContent;

	public boolean isEnableRp() {
		return enableRp;
	}

	public void setEnableRp(boolean enableRp) {
		this.enableRp = enableRp;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public ReverseProxyProperties getReverseProxyProperties() {
		return reverseProxyProperties;
	}

	public void setReverseProxyProperties(ReverseProxyProperties reverseProxyProperties) {
		this.reverseProxyProperties = reverseProxyProperties;
	}

	public byte[] getSdkConfigContent() {
		return sdkConfigContent;
	}

	public void setSdkConfigContent(byte[] sdkConfigContent) {
		this.sdkConfigContent = sdkConfigContent;
	}

	public int getReconnect() {
		return reconnect;
	}

	public void setReconnect(int reconnect) {
		this.reconnect = reconnect;
	}	
	

}
