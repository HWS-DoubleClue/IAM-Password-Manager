package com.doubleclue.dcem.core.utils;

import java.net.Socket;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DcemTrustManager extends X509ExtendedTrustManager {

	private static Logger logger = LogManager.getLogger(DcemTrustManager.class);

	X509Certificate[] issuers;
	X509TrustManager defaultTrustManager = null;

	boolean ignoreCertificates = false;
	private boolean saveServerChainCertificates = false;

	public DcemTrustManager() {
	}

	public DcemTrustManager(boolean ignoreCertifices) {
		this.ignoreCertificates = ignoreCertifices;
	}

	public DcemTrustManager(boolean ignoreCertifices, boolean saveServerChainCertificates) {
		this.ignoreCertificates = ignoreCertifices;
		this.saveServerChainCertificates = saveServerChainCertificates;
	}

	public DcemTrustManager(X509Certificate[] issuers) {
		this.issuers = issuers;
	}

	public void addDefaultTrustManager() {
		try {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init((KeyStore) null);
			for (TrustManager tm : tmf.getTrustManagers()) {
				if (tm instanceof X509TrustManager) {
					defaultTrustManager = (X509TrustManager) tm;
					break;
				}
			}
		} catch (Exception e) {
			logger.info("No Defaul TrustManager found. " + e.toString());
		}
	}

	X509Certificate[] serverChainCertificates;

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return issuers;
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType, Socket arg2) throws CertificateException {
		checkServerTrusted(chain, authType, (SSLEngine) null);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		checkServerTrusted(chain, authType, (SSLEngine) null);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine arg2)
			throws CertificateException {
		if (saveServerChainCertificates) {
			serverChainCertificates = chain;
			if (logger.isDebugEnabled() == true) {
				for (X509Certificate certificate : chain) {
					logger.debug("Server Certificate: " + certificate.getSubjectDN().getName().toString());
					logger.debug("-----BEGIN CERTIFICATE-----\n"
							+ java.util.Base64.getEncoder().encodeToString(certificate.getEncoded())
							+ "\n-----END CERTIFICATE-----");
				}
			}
		}
		if (ignoreCertificates == true) {
			return;
		}
		if (chain == null) {
			throw new IllegalArgumentException("checkServerTrusted: X509Certificate array is null");
		}
		if (!(chain.length > 0)) {
			throw new IllegalArgumentException("checkServerTrusted: X509Certificate is empty");
		}
//		if (issuers == null || issuers.length == 0 ) {
		if (defaultTrustManager != null) {
			try {
				defaultTrustManager.checkServerTrusted(chain, authType);
				return;
			} catch (Exception e) {
				logger.debug("Could not verify Certificate using default trustManager");
				// throw new CertificateException("Certificate not trusted von System as well: "
				// + chain[0].getSubjectDN().getName().toString());
			}
		}
		
//			} else {
//				throw new CertificateException("No trusted CA issuers found");
//			}			
		// }
		if (issuers == null || issuers.length == 0) {
			printCertificates(chain);
			throw new CertificateException("No trusted CA issuers found");
		}
		if (chain[0].equals(issuers[0]) == false) {
			int i;
			for (i = 0; i < issuers.length; i++) {
				try { // Not your CA's. Check if it has been signed by your CA
					chain[0].verify(issuers[i].getPublicKey());
					break;
				} catch (Exception e) {
					continue;
				}
			}
			if (i >= issuers.length) {
				printCertificates(chain);
				throw new CertificateException("Certificate not trusted");
			}
		}
		try {
			chain[0].checkValidity();
		} catch (Exception e) {
			printCertificates(chain);
			throw new CertificateException("Certificate not valid or trusted.");
		}
//		System.out.println("DcemTrustManager.checkServerTrusted()  OK");

	}
	
	private void  printCertificates (X509Certificate[] chain) {
		for (X509Certificate certificate : chain) {
			logger.info("Server Certificate: " + certificate.getSubjectDN().getName().toString());
			try {
				logger.info("-----BEGIN CERTIFICATE-----\n"
						+ java.util.Base64.getEncoder().encodeToString(certificate.getEncoded())
						+ "\n-----END CERTIFICATE-----");
			} catch (CertificateEncodingException e) {
				logger.warn ("Couldn't encode certificate", e);
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		System.out.println("DcemTrustManager.checkClientTrusted()");
	}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String authType, SSLEngine arg2)
			throws CertificateException {
		System.out.println("DcemTrustManager.checkClientTrusted()");
	}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String authType, Socket arg2) throws CertificateException {
		System.out.println("DcemTrustManager.checkClientTrusted()");
	}

	public X509Certificate[] getServerChainCertificates() {
		return serverChainCertificates;
	}

	public boolean isSaveServerChainCertificates() {
		return saveServerChainCertificates;
	}

	public void setSaveServerChainCertificates(boolean saveServerChainCertificates) {
		this.saveServerChainCertificates = saveServerChainCertificates;
	}

}
