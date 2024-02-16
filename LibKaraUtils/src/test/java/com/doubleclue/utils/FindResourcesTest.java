package com.doubleclue.utils;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FindResourcesTest {
	
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
	public void findResourcesTest() throws Exception {
	
	List<FileContent> files =  ResourceFinder.find(FindResourcesTest.class, "templates", ".html");
	for (FileContent fc : files) {
		List<FileContent> templates = ResourceFinder.find(FindResourcesTest.class, "templates", ".html");
		for (FileContent template  : templates) {
			String fileName = template.getName().substring(0, template.getName().length() - ".html".length());
			String locale = fileName.substring(fileName.length() - 2);
			System.out.println("Locale: " + locale);
			fileName = fileName.substring(0, fileName.length()-3);
			System.out.println("name: " + fileName);

		}
	}
	
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
