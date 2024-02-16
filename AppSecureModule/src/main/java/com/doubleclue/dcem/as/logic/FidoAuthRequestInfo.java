package com.doubleclue.dcem.as.logic;

import java.io.Serializable;

import com.yubico.webauthn.AssertionRequest;
import com.yubico.webauthn.RelyingParty;

@SuppressWarnings("serial")
public class FidoAuthRequestInfo implements Serializable {

	private final RelyingParty relyingParty;
	private final AssertionRequest request;

	public FidoAuthRequestInfo(RelyingParty relyingParty, AssertionRequest request) {
		this.relyingParty = relyingParty;
		this.request = request;
	}

	public RelyingParty getRelyingParty() {
		return relyingParty;
	}

	public AssertionRequest getRequest() {
		return request;
	}
}
