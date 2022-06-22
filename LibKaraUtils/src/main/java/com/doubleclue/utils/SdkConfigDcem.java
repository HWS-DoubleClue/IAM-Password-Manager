package com.doubleclue.utils;

import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.List;

import com.doubleclue.comm.thrift.SdkConfig;

public class SdkConfigDcem {

	X509Certificate[] trustCertsX509;
	byte[] connectionKey;
	SdkConfig sdkConfig;
	List<byte[]> trustCertsBytes;
	String redirectionUri;

	public String getUri() {
		if (redirectionUri == null) {
			return sdkConfig.getServerUrl() + "?key="
				+ new String(connectionKey, Charset.forName("UTF-8"));
		} else {
			return redirectionUri + "?key="
					+ new String(connectionKey, Charset.forName("UTF-8"));
		}
	}

	public String getRedirectionUri() {
		return redirectionUri;
	}

	public void setRedirectionUri(String redirectionUri) {
		this.redirectionUri = redirectionUri;
	}

	public byte[] getConnectionKey() {
		return connectionKey;
	}

	public void setConnectionKey(byte[] connectionKey) {
		this.connectionKey = connectionKey;
	}

	public SdkConfig getSdkConfig() {
		return sdkConfig;
	}

	public void setSdkConfig(SdkConfig sdkConfig) {
		this.sdkConfig = sdkConfig;
	}

	public X509Certificate[] getTrustCertsX509() {
		return trustCertsX509;
	}

	public void setTrustCertsX509(X509Certificate[] trustCertsX509) {
		this.trustCertsX509 = trustCertsX509;
	}

	public List<byte[]> getTrustCertsBytes() {
		return trustCertsBytes;
	}

	public void setTrustCertsBytes(List<byte[]> trustCertsBytes) {
		this.trustCertsBytes = trustCertsBytes;
	}

}
