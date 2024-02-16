package com.doubleclue.dcem.as.logic;

import java.io.Serializable;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;

@SuppressWarnings("serial")
public class FidoRegRequestInfo implements Serializable {

	private final RelyingParty relyingParty;
	private final PublicKeyCredentialCreationOptions request;

	public FidoRegRequestInfo(RelyingParty relyingParty, PublicKeyCredentialCreationOptions request) {
		this.relyingParty = relyingParty;
		this.request = request;
	}

	public RelyingParty getRelyingParty() {
		return relyingParty;
	}

	public PublicKeyCredentialCreationOptions getRequest() {
		return request;
	}
}