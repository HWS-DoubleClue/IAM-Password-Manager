package com.doubleclue.dcem.saml.logic;

import java.io.Serializable;

import com.doubleclue.dcem.saml.logic.enums.CanonicalizationAlgorithmEnum;
import com.doubleclue.dcem.saml.logic.enums.DigestAlgorithmEnum;
import com.doubleclue.dcem.saml.logic.enums.SignatureAlgorithmEnum;

@SuppressWarnings("serial")
public class SamlSignatureSettings implements Serializable {

	// variable names shortened to reduce JSON size
	private SignatureAlgorithmEnum signAlg = SignatureAlgorithmEnum.RSA_SHA256;
	private DigestAlgorithmEnum digestAlg = DigestAlgorithmEnum.SHA256;
	private CanonicalizationAlgorithmEnum c14nAlg = CanonicalizationAlgorithmEnum.EXCL_OMIT_COMMENTS;

	public SamlSignatureSettings() {
	}

	public SamlSignatureSettings(SignatureAlgorithmEnum signAlg, DigestAlgorithmEnum digestAlg, CanonicalizationAlgorithmEnum c14nAlg) {
		this.signAlg = signAlg;
		this.digestAlg = digestAlg;
		this.c14nAlg = c14nAlg;
	}

	public SignatureAlgorithmEnum getSignAlg() {
		return signAlg;
	}

	public void setSignAlg(SignatureAlgorithmEnum signAlg) {
		this.signAlg = signAlg;
	}

	public DigestAlgorithmEnum getDigestAlg() {
		return digestAlg;
	}

	public void setDigestAlg(DigestAlgorithmEnum digestAlg) {
		this.digestAlg = digestAlg;
	}

	public CanonicalizationAlgorithmEnum getC14nAlg() {
		return c14nAlg;
	}

	public void setC14nAlg(CanonicalizationAlgorithmEnum c14nAlg) {
		this.c14nAlg = c14nAlg;
	}
}
