
package com.doubleclue.utils;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * <p>Generates time-based one-time passwords (TOTP) as specified in
 * <a href="https://tools.ietf.org/html/rfc6238">RFC&nbsp;6238</a>.</p>
 *
 * <p>{@code TimeBasedPasscodeGenerator} instances are thread-safe and may be shared and re-used across multiple
 * threads.</p>
 *
 */
public class TimeBasedPasscodeGeneratorTest {

	static private final int modDivisor = 1_000_000;

	// private final long timeStepMillis;

	/**
	 * A string identifier for the HMAC-SHA1 algorithm (required by HOTP and allowed by TOTP). HMAC-SHA1 is the default
	 * algorithm for TOTP.
	 */
	public static final String TOTP_ALGORITHM_HMAC_SHA1 = "HmacSHA1";

	/**
	 * A string identifier for the HMAC-SHA256 algorithm (allowed by TOTP).
	 */
	public static final String TOTP_ALGORITHM_HMAC_SHA256 = "HmacSHA256";

	/**
	 * A string identifier for the HMAC-SHA512 algorithm (allowed by TOTP).
	 */
	public static final String TOTP_ALGORITHM_HMAC_SHA512 = "HmacSHA512";

	static byte[] key = { 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x0f };

	static public  void main(String [] args) {
		int passcode;
		try {
			passcode = createPasscode(key, key, 1);
			System.out.println("TimeBasedPasscodeGeneratorTest.main() Psasscode=" + passcode);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	static public boolean verifyPasscode(final byte[] key, byte[] deviceUdid, int passcodeValidFor, int inPasscode) throws Exception {
		return verifyPasscode(key, deviceUdid, passcodeValidFor, inPasscode, 1);
	}

	static public int createPasscode(final byte[] key, byte[] deviceUdid, int passcodeValidFor) throws Exception {
		// System.out.println();
		System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Key=" + StringUtils.binaryToHexString(key, 0, key.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Udid=" + StringUtils.binaryToHexString(deviceUdid, 0, deviceUdid.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() inPassCode=" + inPasscode);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Counter=" + counter);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Date=" + new Date());
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() passcodeValidFor=" + passcodeValidFor);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() deviceUdid.hashCode()=" + Arrays.hashCode(deviceUdid));
		// System.out.println("");
		passcodeValidFor = passcodeValidFor * 1000 * 60;
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		long counter = calendar.getTimeInMillis() / passcodeValidFor;
		counter += Arrays.hashCode(deviceUdid);
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(0, counter);
		int candidate = generate(key, buffer, modDivisor);
		return candidate;
	}

	static public boolean verifyPasscode(final byte[] key, byte[] deviceUdid, int passcodeValidFor, int inPasscode, int windows) throws Exception {
		// System.out.println();
		System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Key=" + StringUtils.binaryToHexString(key, 0, key.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Udid=" + StringUtils.binaryToHexString(deviceUdid, 0, deviceUdid.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() inPassCode=" + inPasscode);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Counter=" + counter);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Date=" + new Date());
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() passcodeValidFor=" + passcodeValidFor);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() deviceUdid.hashCode()=" + Arrays.hashCode(deviceUdid));
		// System.out.println("");
		passcodeValidFor = passcodeValidFor * 1000 * 60;
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		long counter = calendar.getTimeInMillis() / passcodeValidFor;

		counter += Arrays.hashCode(deviceUdid);
		for (int ind = windows; ind >= 0; --ind) {
			final ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.putLong(0, counter - ind);
			int candidate = generate(key, buffer, modDivisor);
			if (candidate == inPasscode) {
				return true;
			}
		}
		counter++;
		for (int ind = 0; ind < windows; ind++) {
			final ByteBuffer buffer = ByteBuffer.allocate(8);
			buffer.putLong(0, counter + ind);
			int candidate = generate(key, buffer, modDivisor);
			if (candidate == inPasscode) {
				return true;
			}
		}
		return false;
	}

	static int generate(byte[] key, ByteBuffer buffer, int modDivisor) throws Exception {
		byte[] hmac;
		hmac = createMacDigest(key, buffer.array(), 0, buffer.array().length);
		final int offset = hmac[hmac.length - 1] & 0x07;
		for (int i = 0; i < 4; i++) {
			buffer.put(i, hmac[i + offset]);
		}
		int hotp = buffer.getInt(0) & 0x7fffffff;
		return hotp % modDivisor;
	}

	/**
	 * @param key
	 * @param deviceUdid
	 * @param passcodeValidFor
	 * @return
	 * @throws Exception
	 */
	static public int generatePasscode(final byte[] key, byte[] deviceUdid, int passcodeValidFor) throws Exception {
		passcodeValidFor = passcodeValidFor * 1000 * 60;
		long counter = new Date().getTime() / (long) passcodeValidFor;
		counter += Arrays.hashCode(deviceUdid);
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		buffer.putLong(0, counter);
		byte[] hmac = SecureUtils.createMacDigest(key, buffer.array(), 0, buffer.array().length, "HmacSHA256");
		final int offset = hmac[hmac.length - 1] & 0x07;
		for (int i = 0; i < 4; i++) {
			buffer.put(i, hmac[i + offset]);
		}
		final int hotp = buffer.getInt(0) & 0x7fffffff;
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Key=" + StringUtils.binaryToHexString(key, 0, key.length));
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Udid=" + StringUtils.binaryToHexString(deviceUdid, 0, deviceUdid.length));
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() PassCode=" + hotp % modDivisor);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Counter=" + counter);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Date=" + new Date());
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() passcodeValidFor=" + passcodeValidFor);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() deviceUdid.hashCode()=" + Arrays.hashCode(deviceUdid));
		// System.out.println("");
		return hotp % modDivisor;
	}

	static public byte[] createMacDigest(byte[] key, byte[] data, int offset, int length)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey macKey = new SecretKeySpec(key, "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(macKey);
		mac.update(data, offset, length);
		return mac.doFinal();
	}
}
