package com.doubleclue.dcem.radius.logic;

import java.util.LinkedList;
import java.util.List;

import com.doubleclue.dcem.core.SupportedCharsets;
import com.doubleclue.dcem.core.logic.ClaimAttribute;

public class RadiusClientSettings {

	SupportedCharsets supportedCharset = SupportedCharsets.ISO_8859_1;
	private List<ClaimAttribute> claimAttributes = new LinkedList<ClaimAttribute>();
	

	public List<ClaimAttribute> getClaimAttributes() {
		return claimAttributes;
	}

	public void setClaimAttributes(List<ClaimAttribute> claimAttributes) {
		this.claimAttributes = claimAttributes;
	}

	public SupportedCharsets getSupportedCharset() {
		return supportedCharset;
	}

	public void setSupportedCharset(SupportedCharsets supportedCharset) {
		this.supportedCharset = supportedCharset;
	}

}
