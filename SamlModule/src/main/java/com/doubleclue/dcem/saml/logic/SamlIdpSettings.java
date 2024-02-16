package com.doubleclue.dcem.saml.logic;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.doubleclue.dcem.core.logic.ClaimAttribute;

@SuppressWarnings("serial")
public class SamlIdpSettings implements Serializable {

	private boolean traceRequests = false;
	private boolean addUserDomain = false;
	private SamlSignatureSettings signatureSettings = new SamlSignatureSettings();
	private List<ClaimAttribute> attributes = new LinkedList<ClaimAttribute>();

	public SamlIdpSettings() {
	}

	public SamlIdpSettings(boolean traceRequests, SamlSignatureSettings signatureSettings, List<ClaimAttribute> attributes) {
		this.traceRequests = traceRequests;
		this.signatureSettings = signatureSettings;
		this.attributes = attributes;
	}

	public boolean isTraceRequests() {
		return traceRequests;
	}

	public void setTraceRequests(boolean traceRequests) {
		this.traceRequests = traceRequests;
	}

	public boolean isAddUserDomain() {
		return addUserDomain;
	}

	public void setAddUserDomain(boolean addUserDomain) {
		this.addUserDomain = addUserDomain;
	}

	public SamlSignatureSettings getSignatureSettings() {
		return signatureSettings;
	}

	public void setSignatureSettings(SamlSignatureSettings signatureSettings) {
		this.signatureSettings = signatureSettings;
	}

	public List<ClaimAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ClaimAttribute> attributes) {
		this.attributes = attributes;
	}
}
