package com.doubleclue.dcem.core.logic;

import java.time.LocalDateTime;

public class CertificateInfo {

	String issuer;
	String subject;
	LocalDateTime expires;

	public CertificateInfo(String issuer, String subject, LocalDateTime expires) {
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

	public LocalDateTime getExpires() {
		return expires;
	}

	public void setExpires(LocalDateTime expires) {
		this.expires = expires;
	}

}
