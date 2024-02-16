package com.doubleclue.dcem.boot;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class TrustManagerTest {

	public static void main(String[] args) {

		TrustManagerFactory trustManagerFactory;
		try {
			trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			X509Certificate[] certChain = new X509Certificate[1];
			
			
			
			// certChain[0] = YourCert; // ADD YOUR CERTIFICATE HERE
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
				System.out.println(trustManager);

				if (trustManager instanceof X509TrustManager) {
					X509TrustManager x509TrustManager = (X509TrustManager) trustManager;

					try {
						x509TrustManager.checkServerTrusted(certChain, "RSA");
					} catch (CertificateException e) {
						// THROWS EXCEPTION IF NOT TRUSTED
						e.printStackTrace();
					}

				}
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
