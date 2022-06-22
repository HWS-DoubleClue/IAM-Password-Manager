package com.doubleclue.dcem.as.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.utils.IpRanges;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DcemPolicy implements Serializable {

	private static final long serialVersionUID = 3417765109907814139L;
	
	private boolean denyAccess = false;
//	private boolean browserFingerPrint;
	private boolean refrain2FaWithInTime;
	private boolean enableSessionAuthentication = true;
	private int rememberBrowserFingerPrint = 24;
	private String networksBypass2FA;
	private boolean allowAll2FAMethods = true;
	private List<AuthMethod> allowedMethods; // Allow ALL if empty
	private AuthMethod defaultPolicy;
	private boolean mfaOnUnlock = false;

	public DcemPolicy() {
		super();
		allowedMethods = new ArrayList<>();
		allowedMethods.add(AuthMethod.PASSWORD);
		allowedMethods.add(AuthMethod.PUSH_APPROVAL);
		allowedMethods.add(AuthMethod.QRCODE_APPROVAL);
		allowedMethods.add(AuthMethod.DOUBLECLUE_PASSCODE);
	}

	@Transient
	@JsonIgnore
	private IpRanges ipRanges;

	public boolean isAllowAll2FAMethods() {
		return allowAll2FAMethods;
	}

	public void setAllowAll2FAMethods(boolean allowAll2FAMethods) {
		this.allowAll2FAMethods = allowAll2FAMethods;
	}

	@Override
	public String toString() {
		return "denyAccess=" + denyAccess + ", rememberBrowserFingerPrint=" + rememberBrowserFingerPrint
				+ ", networksWithout2FA=" + networksBypass2FA + ", allowedMethods=" + allowedMethods;
	}

	public boolean isDenyAccess() {
		return denyAccess;
	}

	public void setDenyAccess(boolean denyAccess) {
		this.denyAccess = denyAccess;
	}

	public int getRememberBrowserFingerPrint() {
		return rememberBrowserFingerPrint;
	}

	public void setRememberBrowserFingerPrint(int rememberBrowserFingerPrint) {
		this.rememberBrowserFingerPrint = rememberBrowserFingerPrint;
	}

	public String getNetworksBypass2FA() {
		return networksBypass2FA;
	}

	public void setNetworksBypass2FA(String networksBypass2FA) {
		this.networksBypass2FA = networksBypass2FA;
	}

	public List<AuthMethod> getAllowedMethods() {
		return allowedMethods;
	}

	public void setAllowedMethods(List<AuthMethod> allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public void allowMethod(AuthMethod method, boolean allowed) {
		boolean methodIsAllowed = allowedMethods.contains(method);
		if (allowed && !methodIsAllowed) {
			allowedMethods.add(method);
		} else if (!allowed && methodIsAllowed) {
			allowedMethods.remove(method);
		}
	}

	public IpRanges getIpRanges() {
		return ipRanges;
	}

	public void setIpRanges(IpRanges ipRanges) {
		this.ipRanges = ipRanges;
	}

	public void updateIpranges() throws Exception {
		if (networksBypass2FA != null && networksBypass2FA.isEmpty() == false) {
			ipRanges = new IpRanges(networksBypass2FA);
		} else {
			ipRanges = null;
		}
	}

	public AuthMethod getDefaultPolicy() {
		return defaultPolicy;
	}

	public void setDefaultPolicy(AuthMethod defaultPolicy) {
		this.defaultPolicy = defaultPolicy;
	}

	public boolean isEnableSessionAuthentication() {
		return enableSessionAuthentication;
	}

	public void setEnableSessionAuthentication(boolean enableSessionAuthentication) {
		this.enableSessionAuthentication = enableSessionAuthentication;
	}

	public boolean isRefrain2FaWithInTime() {
		return refrain2FaWithInTime;
	}

	public void setRefrain2FaWithInTime(boolean refrain2FaWithInTime) {
		this.refrain2FaWithInTime = refrain2FaWithInTime;
	}

	public boolean isMfaOnUnlock() {
		return mfaOnUnlock;
	}

	public void setMfaOnUnlock(boolean mfaOnUnlock) {
		this.mfaOnUnlock = mfaOnUnlock;
	}

}
