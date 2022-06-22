package com.doubleclue.utils;

import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

public class WildCardHostnameVerifier implements HostnameVerifier {

	private static final String COMMON_NAME_RDN_PREFIX = "CN=";
	private static final String WILD_CARD = "*.";

	@Override
	public boolean verify(String hostname, SSLSession session) {

		X509Certificate peerCertificate;
		try {
			peerCertificate = (X509Certificate) session.getPeerCertificates()[0];
		} catch (SSLPeerUnverifiedException e) {
			throw new IllegalStateException("The session does not contain a peer X.509 certificate.");
		}
		String commonName = getCommonName(peerCertificate);

		if (commonName.startsWith(WILD_CARD)) {
			if (hostname.equalsIgnoreCase(commonName.substring(WILD_CARD.length()))) {
				return true;
			}
			int index = hostname.indexOf(".");
			if (index > 0) {
				return hostname.substring(index + 1).equalsIgnoreCase(commonName.substring(WILD_CARD.length()));
			}
		}
		return hostname.equalsIgnoreCase(commonName);
	}

	private String getCommonName(X509Certificate peerCertificate) {
		String subjectDN = peerCertificate.getSubjectDN().getName();
		String[] dnComponents = subjectDN.split(",");
		for (String dnComponent : dnComponents) {
			if (dnComponent.startsWith(COMMON_NAME_RDN_PREFIX)) {
				return dnComponent.substring(COMMON_NAME_RDN_PREFIX.length());
			}
		}
		throw new IllegalArgumentException("The certificate has no common name.");
	}
}
