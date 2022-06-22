package com.doubleclue.utils;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.doubleclue.app.sec.api.SecureUtilsApi;

public class SecureUtils {

	// static { Application should add Bouncy Castle once
	// Security.addProvider(new BouncyCastleProvider());
	// }
	static final String  HMAC_SHA256 = "HmacSHA256";
	static final String  HMAC_SHA1 = "HmacSHA1";
	

	
	public static SecureUtilsApi secureUtilsApi;

	public static void setSecureUtilsImpl(SecureUtilsApi secureUtilsApi_) {
		secureUtilsApi = secureUtilsApi_;
	}
	
//	public static SecureUtilsApi getSecureUtilsImpl () {
//		return  secureUtilsApi;
//	}

	public static final String DEFAULT_SUPPORTED_CIPHERS = "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, "
			+ "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA";

	
	public static final String  KEY_ALGORITHM = "AES/CBC/PKCS5Padding";

	
	public static final byte[] ENCRYPTION_ALGORITHM_IV = { (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00,
			(byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00,
			(byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00 };

	static byte[] commonKey = { (byte) 0xA7, (byte) 0xF3, (byte) 0x46, (byte) 0xCF, (byte) 0xF3, (byte) 0x17,
			(byte) 0x54, (byte) 0x3c, (byte) 0x63, (byte) 0xC3, (byte) 0x32, (byte) 0x4E, (byte) 0x01, (byte) 0xF0,
			(byte) 0x55, (byte) 0x67 };

	// private static final String SIGNATURE_ALGORITHM_SPONGY =
	// "SHA256WithRSAEncryption";

	private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

	// private static final int ROOT_KEYSIZE = 2048;

	private static final String KEY_GENERATION_ALGORITHM = "RSA";

	// private static final boolean REGENERATE_FRESH_CA_CERTIFICATE = false;

	public static PrivateKey loadPrivateKey(byte[] value)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(new PKCS8EncodedKeySpec(value));
	}

	public static PublicKey loadPublicKey(byte[] value)
			throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(new X509EncodedKeySpec(value));
	}

	/**
	 * Create a random 2048 bit RSA key pair with the given length
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM);
		generator.initialize(keySize, new SecureRandom());
		return generator.generateKeyPair();
	}


//	public static byte[] getSha1(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
//		MessageDigest md = MessageDigest.getInstance("SHA-1");
//		md.update(value.getBytes("UTF-8"));
//		return md.digest();
//	}


	public static byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	public static byte[] sign(PrivateKey privateKey, int tempalteId, String data, String actionId, String data2)
			throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(KaraUtils.intToByteArray(tempalteId));
		if (data != null) {
			signature.update(data.getBytes(StandardCharsets.UTF_8));
		}
		if (actionId != null) {
			signature.update(actionId.getBytes(StandardCharsets.UTF_8));
		}
		if (data2 != null) {
			signature.update(data2.getBytes(StandardCharsets.UTF_8));
		}
		return signature.sign();
	}


//	public static byte[] verifyMacDigest(byte[] key, byte[] data, byte[] digest)
//			throws NoSuchAlgorithmException, InvalidKeyException {
//		SecretKey macKey = new SecretKeySpec(key, "HmacSHA256");
//		Mac mac = Mac.getInstance("HmacSHA256");
//		mac.init(macKey);
//
//		return mac.doFinal(data);
//	}
//
//	public static byte[] createMacDigest(byte[] key, byte[] data, int offset, int length)
//			throws NoSuchAlgorithmException, InvalidKeyException {
//		SecretKey macKey = new SecretKeySpec(key, "HmacSHA256");
//		Mac mac = Mac.getInstance("HmacSHA256");
//		mac.init(macKey);
//		mac.update(data, offset, length);
//		return mac.doFinal();
//	}

	public static byte[] createMacDigestCommonSha2(byte[] data, int offset, int length)
			throws InvalidKeyException, NoSuchAlgorithmException {
		return secureUtilsApi.createMacDigest(commonKey, data, offset, length, HMAC_SHA256);
	}
	

	public static byte[] encryptDataCommon(byte[] data) throws Exception {
		return secureUtilsApi.encryptData(commonKey, ENCRYPTION_ALGORITHM_IV, data);
	}


	public static byte[] decryptDataCommon(byte[] data) throws Exception {
		return secureUtilsApi.decryptData(commonKey, ENCRYPTION_ALGORITHM_IV, data);
	}
	
	public static byte[] verifyMacDigest(byte[] key, byte[] data, byte[] digest)
			throws NoSuchAlgorithmException, InvalidKeyException {
		return secureUtilsApi.verifyMacDigest(key, data, digest);
	}

	public static byte[] createMacDigest(byte[] key, byte[] data, int offset, int length, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeyException {
		return secureUtilsApi.createMacDigest(key, data, offset, length, algorithm);
	}
		
	public static byte[] encryptData(byte[] key, byte[] encoded) throws Exception {
		return secureUtilsApi.encryptData(key, ENCRYPTION_ALGORITHM_IV, encoded);
	}
	

	public static byte[] decryptData(byte[] key, byte[] encryptedData) throws Exception {
		return secureUtilsApi.decryptData(key, ENCRYPTION_ALGORITHM_IV, encryptedData);
	}
	
		

//	public static byte[] encryptData(byte[] key, byte[] data, int offset, int length) throws Exception {
//		Cipher cipher = Cipher.getInstance(DB_KEY_ALGORITHM + "/" + DB_KEY_CRYPTO_MODE + "/" + DB_KEY_PADDING);
//		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, DB_KEY_ALGORITHM), spec);
//		return cipher.doFinal(data, offset, length);
//	}
	
	
	public static boolean isVerifySignature(PublicKey publicKey, byte[] data, byte[] digitalSignature)
			throws Exception {
		// Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		Signature signature = Signature.getInstance("SHA256WithRSA");

		signature.initVerify(publicKey);
		signature.update(data);
		return signature.verify(digitalSignature);
	}

	

	public static boolean isVerifySignature(PublicKey publicKey, byte[] digitalSignature, int templateId, String data,
			String actionId, String data2) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicKey);
		signature.update(KaraUtils.intToByteArray(templateId));
		if (data != null) {
			signature.update(data.getBytes(StandardCharsets.UTF_8));
		}
		if (actionId != null) {
			signature.update(actionId.getBytes(StandardCharsets.UTF_8));
		}
		if (data2 != null) {
			signature.update(data2.getBytes(StandardCharsets.UTF_8));
		}
		return signature.verify(digitalSignature);
	}

	//@J2ObjCIncompatible
	public static boolean removeCryptographyRestrictions() throws Exception {
		if (isRestrictedCryptography() == false) {
			// logger.fine("Cryptography restrictions removal not needed");
			return false;
		}

		/*
		 * Do the following, but with reflection to bypass access checks:
		 *
		 * JceSecurity.isRestricted = false;
		 * JceSecurity.defaultPolicy.perms.clear();
		 * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
		 */
		final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
		final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
		final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

		final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
		isRestrictedField.setAccessible(true);
		final Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
		isRestrictedField.set(null, false);

		final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
		defaultPolicyField.setAccessible(true);
		final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

		final Field perms = cryptoPermissions.getDeclaredField("perms");
		perms.setAccessible(true);
		((Map<?, ?>) perms.get(defaultPolicy)).clear();

		final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
		instance.setAccessible(true);
		defaultPolicy.add((Permission) instance.get(null));

		// logger.fine("Successfully removed cryptography restrictions");
		return true;

	}

	//@J2ObjCIncompatible
	private static boolean isRestrictedCryptography() {
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	public static List<byte []> convertPemToCerList (byte[] content) throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		boolean parse = false;
		String line;
		byte[] dest = new byte[8096];
		int destOffset = 0;
		List<byte []> list = new LinkedList<>();
		while ((line = StringUtils.getNextLine(bis)) != null) {
			line = line.trim();
			if (line.startsWith("#")) {
				continue;
			}
			if (line.startsWith("-----BEGIN CERTIFICATE-----")) {
				parse = true;
				destOffset = 0;
				continue;
			}
			if (line.startsWith("-----END CERTIFICATE-----")) {
				parse = false;
				byte [] cert = new byte [destOffset];
				System.arraycopy(dest, 0, cert, 0, destOffset);
				list.add(cert);
			}
			if (parse) {
				byte [] base64Data = it.sauronsoftware.base64.Base64.decode(line.substring(0, line.length()).getBytes("ASCII"));
				System.arraycopy(base64Data, 0, dest, destOffset, base64Data.length);   
				destOffset += base64Data.length;
			}
		}
  		return list;
	}


	public static void setCommonKey(byte[] commonKey) {
		SecureUtils.commonKey = commonKey;
	}

	public static String[] getSupportedCiphers() {
		return DEFAULT_SUPPORTED_CIPHERS.split(", ");
	}

//	public static int decodeBase64(byte[] src, int srcOff, int len, byte[] dst, int dstOff) {
//
//		while (len >= 4) {
//			// Decode 4 bytes at a time
//			try {
//				TBase64Utils.decode(src, srcOff, 4, dst, dstOff); // NB: decoded in
//
//				// place
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			srcOff += 4;
//			len -= 4;
//			dstOff += 3;
//		}
//		// Don't decode if we hit the end or got a single leftover byte (invalid
//		// base64 but legal for skip of regular string type)
//		if (len > 1) {
//			// Decode remainder
//			TBase64Utils.decode(src, srcOff, len, dst, dstOff); // NB: decoded
//																// in place
//			dstOff += len - 1;
//		}
//		return dstOff;
//	}

}
