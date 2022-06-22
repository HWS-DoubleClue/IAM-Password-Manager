package com.doubleclue.utils;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

public class SecureUtilsTest {
	
	File outputDir;
	
	@Before
	public void setUp() throws Exception {
//		Security.addProvider(new BouncyCastleProvider());
		outputDir = new File (System.getProperty("user.home") + "\\Tests");
		if (outputDir.exists() == false) {
			outputDir.mkdirs();
		}

	}

	@Test
	public void createKeyStoreTest() throws Exception {
		
//		File rootKeyStoreFile = new File (outputDir, "rootKeystore.p12");
//		File mgtKeyStoreFile = new File (outputDir, "mgtKeystore.p12");
//		char [] password = new char [] {'1','2','3','4'};
//
//			KeyStore rootKeyStore = SecureUtils.createKeyStore(1024, "cn=www.hws-gruppe.de",
//					null, null, password, null, "root", null);
//			
//			rootKeyStore.store(new FileOutputStream(rootKeyStoreFile), password);
//			/*
//			 *  Create keystore signedn from root
//			 * 
//			 */
//			PrivateKey pvKey = (PrivateKey) rootKeyStore.getKey("root", password);
//			KeyStore mgtKeyStore = SecureUtils.createKeyStore(1024, "cn=hwsMgt",
//					rootKeyStore.getCertificateChain("root"), null , password, pvKey,
//					"mgt", null);
//			mgtKeyStore.store(new FileOutputStream(mgtKeyStoreFile), password);
		
	}
	
	@Test
	public void convertPk12ToPemTest() throws Exception {
//		createKeyStoreTest();
//		
//		File rootKeyStoreFile = new File (outputDir, "rootKeystore.jks");
//		File inputStoreFile = new File (outputDir, "mgtKeystore.p12");
//		char [] password = new char [] {'1','2','3','4'};
//
//		KeyStore keyStore = KeyStore.getInstance("PKCS12");
//		keyStore.load(new FileInputStream(inputStoreFile), password);
//		
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		keyStore.store(bos, password);
//		byte [] pemContent = SecureUtils.convertPk12ToPem (bos.toByteArray(), new String (password), "mgt");
//		
//		
//		KeyStore keyStoreResult = SecureUtils.convertPemToTrustStore(pemContent);
//		keyStoreResult.store(new FileOutputStream(rootKeyStoreFile), password);
		
	}


}
