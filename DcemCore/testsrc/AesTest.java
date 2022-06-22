import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.doubleclue.dcem.core.exceptions.DcemException;

public class AesTest {

	public static final String ENCRYPTION_ALGORITHM = "AES";
	public static final String ENCRYPTION_CRYPTOMODE = "EBC";
	public static final String ENCRYPTION_PADDING = "PKCS5PADDING";

	public static final byte[] ENCRYPTION_ALGORITHM_IV = { (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00,
			(byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55,
			(byte) 0x00 };
	static final byte[] ENCRYPTION_KEY = { (byte) 0xd4, (byte) 0x01, (byte) 0x02, (byte) 0x01, (byte) 0x50, (byte) 0x11, (byte) 0x03,
			(byte) 0x99, (byte) 0x76, (byte) 0x01, (byte) 0x80, (byte) 0x55, (byte) 0xA3, (byte) 0x01, (byte) 0x10, (byte) 0xFE };

	public static final byte[] data = { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x07, (byte) 0xf8, (byte) 0x09, (byte) 0xfa, (byte) 0x0b, 1, 2, 0};

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {

		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance("AES/CTR/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, generateKeyFromByteArray(ENCRYPTION_KEY, ENCRYPTION_ALGORITHM));
			
			byte [] encrypted =  cipher.doFinal(data);			
		
			String key =   Base64.getEncoder().encodeToString(encrypted);
			System.out.println("AesTest.main() data length=" + data.length);
			System.out.println("AesTest.main() " + key + " Length=" + key.length() + " Encrypted length=" + encrypted.length);
			

		} catch (NoSuchPaddingException | InvalidKeyException | DcemException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);

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

}
