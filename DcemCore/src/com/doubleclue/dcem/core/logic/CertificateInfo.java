package com.doubleclue.dcem.core.logic;

import java.util.Date;

public class CertificateInfo {

	String issuer;
	String subject;
	Date expires;

	public CertificateInfo(String issuer, String subject, Date expires) {
		super();
		this.issuer = issuer;
		this.subject = subject;
		this.expires = expires;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Date getExpires() {
		return expires;
	}

	public void setExpires(Date expires) {
		this.expires = expires;
	}

}
