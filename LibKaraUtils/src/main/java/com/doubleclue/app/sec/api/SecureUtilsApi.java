package com.doubleclue.app.sec.api;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


public interface SecureUtilsApi {
	
	
	public byte[] verifyMacDigest(byte[] key, byte[] data, byte[] digest)
			throws NoSuchAlgorithmException, InvalidKeyException;

	public byte[] createMacDigest(byte[] key, byte[] data, int offset, int length, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeyException;
		
	public byte[] encryptData(byte[] key, byte[] iV, byte[] encoded) throws Exception;
	

	public byte[] decryptData(byte[] key, byte [] iV, byte[] encryptedData) throws Exception;

	byte[] createMacDigestSha2(byte[] key, byte[] data, int offset, int length) throws Exception;
	
}
