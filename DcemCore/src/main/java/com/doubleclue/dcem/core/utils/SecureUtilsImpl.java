package com.doubleclue.dcem.core.utils;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.doubleclue.app.sec.api.SecureUtilsApi;

public class SecureUtilsImpl implements SecureUtilsApi {

	
	static final String  KEY_ALGORITHM = "AES";
	static final String  CHIPER_TYPE = "AES/CBC/PKCS5Padding";
	static final String  HMAC_SHA256 = "HmacSHA256";
	static final String  HMAC_SHA1 = "HmacSHA1";


	@Override
	public byte[] verifyMacDigest(byte[] key, byte[] data, byte[] digest)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey macKey = new SecretKeySpec(key, "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(macKey);

		return mac.doFinal(data);
	}
	
	@Override
	public byte[] createMacDigestSha2(byte[] key, byte[] data, int offset, int length) throws Exception {
		return createMacDigest(key, data, offset, length, HMAC_SHA256);
	}

	@Override
	public byte[] createMacDigest(byte[] key, byte[] data, int offset, int length, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey macKey = new SecretKeySpec(key, algorithm);
		Mac mac = Mac.getInstance(algorithm);
		mac.init(macKey);
		mac.update(data, offset, length);
		return mac.doFinal();
	}
	


	@Override
	public byte[] decryptData(byte[] key, byte[] iV,  byte[] encryptedData) throws Exception {
		AlgorithmParameterSpec spec = new IvParameterSpec(iV);
		Cipher decryptionCipher = Cipher
				.getInstance(CHIPER_TYPE);
		decryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), spec);
		return decryptionCipher.doFinal(encryptedData);
	}

	

	@Override
	public byte[] encryptData(byte[] key,  byte[] iV, byte[] data) throws Exception {
		AlgorithmParameterSpec spec = new IvParameterSpec(iV);
		Cipher cipher = Cipher.getInstance(CHIPER_TYPE);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), spec);
		return cipher.doFinal(data);
	}
	
}
