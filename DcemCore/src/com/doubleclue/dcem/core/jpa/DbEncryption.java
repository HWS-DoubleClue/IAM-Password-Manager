package com.doubleclue.dcem.core.jpa;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.BlockCipherPadding;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.utils.RandomUtils;

/**
 * 
 * @author Emanuel Galea
 * 
 */
public class DbEncryption {

	public static final String ENCRYPTION_CRYPTOMODE = "CBC";
	public static final String ENCRYPTION_PADDING = "PKCS5PADDING";
	public static final byte[] ENCRYPTION_ALGORITHM_IV = { (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55,
			(byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00 };

	static final byte[] ENCRYPTION_KEY = { (byte) 0xd4, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x50, (byte) 0x11, (byte) 0x03, (byte) 0x99, (byte) 0x76,
			(byte) 0x01, (byte) 0x80, (byte) 0x55, (byte) 0xA3, (byte) 0x01, (byte) 0x10, (byte) 0xFE, (byte) 0xd4, (byte) 0x01, (byte) 0x02, (byte) 0x01,
			(byte) 0x50, (byte) 0x11, (byte) 0x03, (byte) 0x99, (byte) 0x76, (byte) 0x01, (byte) 0x80, (byte) 0x55, (byte) 0xA3, (byte) 0x01, (byte) 0x10,
			(byte) 0xFF };

	private static final String DB_KEY_ALGORITHM = "AES";

	private static final AlgorithmParameterSpec algorithmParameterSpec = new IvParameterSpec(ENCRYPTION_ALGORITHM_IV);

	private static Cipher encryptionCipher = null;
	private static Cipher decryptionCipher = null;

	static byte[] localDbKey;

	/**
	 * ATTENTION: Do not use this at runtime. i.e. during encryption and
	 * decryption
	 * 
	 * @param localConfig
	 * @throws DcemException
	 */
	public static void createDbCiphers(DatabaseConfig databaseConfig) throws DcemException {

		String databaseEncryptionKey = databaseConfig.getDatabaseEncryptionKey();
		if (databaseEncryptionKey == null || databaseEncryptionKey.trim().isEmpty()) {
			throw new DcemException(DcemErrorCodes.NO_DB_ENCRYPTION_KEY, "No Database Encrption key found in DCEM_HOME/configuration.xml", null);
		}

		byte[] encryptedDbKey;
		try {
			encryptedDbKey = Base64.getDecoder().decode(databaseEncryptionKey);
			DbEncryption.createDbCiphers(encryptedDbKey);

		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_DB_ENCRYPTION_KEY, "Couldn't create DB decryption key", e);
		}
	}

	/**
	 * ATTENTION: Do not use this at runtime. i.e. during encryption and
	 * decryption
	 * 
	 * @param encryptedDbKey
	 * @throws DatabaseDecryptionException
	 */
	public static void createDbCiphers(byte[] encryptedDbKey) throws Exception {

		// if (encryptionCipher != null) {
		// return;
		// }
		// decrypting dbKey
		// byte[] localDbKey;
		Cipher cipher = null;
		cipher = Cipher.getInstance(DcemConstants.DB_KEY_ALG_MODE);
		cipher.init(Cipher.DECRYPT_MODE, generateKeyFromByteArray(ENCRYPTION_KEY, DB_KEY_ALGORITHM), algorithmParameterSpec);
		localDbKey = cipher.doFinal(encryptedDbKey);
		encryptionCipher = Cipher.getInstance(DcemConstants.DB_KEY_ALG_MODE);
		encryptionCipher.init(Cipher.ENCRYPT_MODE, generateKeyFromByteArray(localDbKey, DB_KEY_ALGORITHM), algorithmParameterSpec);
		decryptionCipher = createDecyptCipher();
	}

	public static Cipher createDecyptCipher() throws Exception {
		Cipher cipher = Cipher.getInstance(DcemConstants.DB_KEY_ALG_MODE);
		cipher.init(Cipher.DECRYPT_MODE, generateKeyFromByteArray(localDbKey, DB_KEY_ALGORITHM), algorithmParameterSpec);
		return cipher;
	}


	public static InputStream getBlockCipherInputStream (boolean forEncryption, byte [] salt, InputStream inputStream, boolean gcm) throws Exception {
		if (gcm) {
			GCMBlockCipher gcmBlockCipher = new GCMBlockCipher(new AESEngine());
			CipherParameters parameters = new AEADParameters(new KeyParameter(localDbKey), 128, salt); 
			gcmBlockCipher.init(forEncryption, parameters);
			return new CipherInputStream(inputStream, gcmBlockCipher, SecureServerUtils.MAX_CIPHER_BUFFER);
		} else {
			if (forEncryption) {
				inputStream = new InputStreamWithSeed(inputStream, 4);
			}
			BlockCipherPadding padding = new PKCS7Padding();
			BufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()), padding);
			CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(localDbKey), ENCRYPTION_ALGORITHM_IV);
			bufferedBlockCipher.reset();
			bufferedBlockCipher.init(forEncryption, ivAndKey);
			return new CipherInputStream(inputStream, bufferedBlockCipher, SecureServerUtils.MAX_CIPHER_BUFFER);
		}
	}

	public static byte[] encryptSeed(String data) throws DcemException {
		try {
			return encryptSeed(data.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new DcemException(DcemErrorCodes.DB_ENCRYPTION, "Unsupported Encoding", e);
		}
	}

	/**
	 * Encrypt the data with a 4 byte Seed with the decrypted dbKey
	 * 
	 * @param data
	 * @return Encrypted data
	 * @throws DatabaseDecryptionException
	 */
	public static byte[] encryptSeed(byte[] data) throws DcemException {

		synchronized (encryptionCipher) {
			try {
				encryptionCipher.update(RandomUtils.getRandom(4)); // SEED
				return encryptionCipher.doFinal(data);
			} catch (IllegalBlockSizeException e) {
				throw new DcemException(DcemErrorCodes.DB_ENCRYPTION, "Illegal block size for data.", e);
			} catch (BadPaddingException e) {
				throw new DcemException(DcemErrorCodes.DB_ENCRYPTION, "Bad padding for data.", e);
			}
		}
	}

	/**
	 * Decrypt the data and remove the four byte seed with the decrypted dbKey
	 * 
	 * @param data
	 * @return Decrypted data
	 * @throws DatabaseDecryptionException
	 */
	public static byte[] decryptSeed(byte[] data) throws DcemException {

		synchronized (decryptionCipher) {
			try {
				byte[] decrpted = decryptionCipher.doFinal(data);
				byte[] result = new byte[decrpted.length - 4];
				System.arraycopy(decrpted, 4, result, 0, result.length);
				return result;
			} catch (IllegalBlockSizeException e) {
				try {
					decryptionCipher = createDecyptCipher();
				} catch (Exception e1) {
					throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "\"Illegal block size for data.\" AND couldn't create decyptCiphers", e);
				}
				throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Illegal block size for data.", e);
			} catch (BadPaddingException e) {
				try {
					decryptionCipher = createDecyptCipher();
				} catch (Exception e1) {
					throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for data AND couldn't create decyptCiphers", e);
				}
				throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for data.", e);
			}
		}
	}

	/**
	 * Decrypt the data and remove the four byte seed with the decrypted dbKey
	 * 
	 * @param data
	 * @return Decrypted data
	 * @throws DatabaseDecryptionException
	 */
	public static String decryptSeedToString(byte[] data) throws DcemException {

		synchronized (decryptionCipher) {
			try {
				byte[] decrpted = decryptionCipher.doFinal(data);
				return new String(decrpted, 4, decrpted.length - 4, "UTF-8");
			} catch (IllegalBlockSizeException e) {
				try {
					decryptionCipher = createDecyptCipher();
				} catch (Exception e1) {
					throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for data AND couldn't create decyptCiphers", e);
				}
				throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Illegal block size for data.", e);
			} catch (BadPaddingException e) {
				try {
					decryptionCipher = createDecyptCipher();
				} catch (Exception e1) {
					throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for data AND couldn't create decyptCiphers", e);
				}
				throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for data.", e);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Couldn't convert to String.", e);
			}
		}
	}

	/**
	 * Generate a key from a given byte array and algorithm
	 * 
	 * @param keyAsByteArray
	 * @param keyAlgorithm
	 * @return A key
	 * @throws DatabaseDecryptionException
	 */
	private static Key generateKeyFromByteArray(byte[] keyAsByteArray, String keyAlgorithm) throws DcemException {
		SecretKeySpec keySpec = new SecretKeySpec(keyAsByteArray, keyAlgorithm);
		return keySpec;
	}

	public static Key generateKeyFromByteArray(String keyAlgorithm) throws DcemException {
		SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY, keyAlgorithm);
		return keySpec;
	}

	public static AlgorithmParameterSpec getAlgorithmParameterSpec() {
		return algorithmParameterSpec;
	}
}
