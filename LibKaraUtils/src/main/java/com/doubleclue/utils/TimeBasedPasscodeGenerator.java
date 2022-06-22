
package com.doubleclue.utils;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * <p>Generates time-based one-time passwords (TOTP) as specified in
 * <a href="https://tools.ietf.org/html/rfc6238">RFC&nbsp;6238</a>.</p>
 *
 * <p>{@code TimeBasedPasscodeGenerator} instances are thread-safe and may be shared and re-used across multiple
 * threads.</p>
 *
 */
public class TimeBasedPasscodeGenerator {

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

	/**
	 * Constructs a new time=based one-time password generator with the given time-step, password length, and HMAC
	 * algorithm.
	 *
	 * @param timeStep the magnitude of the time-step for this generator
	 * @param timeStepUnit the units for the the given time step
	 * @param passwordLength the length, in decimal digits, of the one-time passwords to be generated; must be between
	 * 6 and 8, inclusive
	 * @param algorithm the name of the {@link javax.crypto.Mac} algorithm to use when generating passwords; TOTP allows
	 * for {@value com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA1},
	 * {@value com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA256}, and
	 * {@value com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA512}
	 *
	 * @throws NoSuchAlgorithmException if the underlying JRE doesn't support HMAC-SHA1, which should never happen
	 * except in cases of serious misconfiguration
	 *
	 * @see com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA1
	 * @see com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA256
	 * @see com.TimeBasedPasscodeGenerator.otp.TimeBasedOneTimePasswordGenerator#TOTP_ALGORITHM_HMAC_SHA512
	 */

	static public boolean verifyPasscode(final byte[] key, byte[] deviceUdid, int passcodeValidForMilli, int inPasscode) throws Exception {
		return verifyPasscode(key, deviceUdid, passcodeValidForMilli, inPasscode, 1);
	}

	static public boolean verifyPasscode(final byte[] key, byte[] deviceUdid, int passcodeValidForMilli, int inPasscode, int windows) throws Exception {
		// System.out.println();
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Key=" + StringUtils.binaryToHexString(key, 0, key.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() Udid=" + StringUtils.binaryToHexString(deviceUdid, 0, deviceUdid.length));
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() inPassCode=" + inPasscode);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Counter=" + counter);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Date=" + new Date());
		// System.out.println("TimeBasedPasscodeGenerator.verifyPasscode() passcodeValidFor=" + passcodeValidForMilli);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() deviceUdid.hashCode()=" + Arrays.hashCode(deviceUdid));
		// System.out.println("");
		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		long counter = calendar.getTimeInMillis() / passcodeValidForMilli;

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
		hmac = SecureUtils.createMacDigest(key, buffer.array(), 0, buffer.array().length, TOTP_ALGORITHM_HMAC_SHA256);
		final int offset = hmac[hmac.length - 1] & 0x07;
		for (int i = 0; i < 4; i++) {
			buffer.put(i, hmac[i + offset]);
		}
		int hotp = buffer.getInt(0) & 0x7fffffff;
		return hotp % modDivisor;
	}

	static public int generatePasscode(final byte[] key, byte[] deviceUdid, int passcodeValidForMilli) throws Exception {
		return generatePasscode(key, deviceUdid, passcodeValidForMilli, TOTP_ALGORITHM_HMAC_SHA1);
	}

	/**
	 * @param key
	 * @param deviceUdid
	 * @param passcodeValidFor
	 * @return
	 * @throws Exception
	 */
	static public int generatePasscode(final byte[] key, byte[] deviceUdid, int passcodeValidForMilli, String algorithm) throws Exception {
		long counter = new Date().getTime() / (long) passcodeValidForMilli;
		final int offset;
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		byte[] hmac;

		if (deviceUdid != null) {
			counter += Arrays.hashCode(deviceUdid);
			buffer.putLong(0, counter);
			hmac = SecureUtils.createMacDigest(key, buffer.array(), 0, buffer.array().length, algorithm);
			offset = hmac[hmac.length - 1] & 0x07;
		} else {
			buffer.putLong(0, counter);
			hmac = SecureUtils.createMacDigest(key, buffer.array(), 0, buffer.array().length, algorithm);
			offset = hmac[hmac.length - 1] & 0x0f;
		}

		for (int i = 0; i < 4; i++) {
			buffer.put(i, hmac[i + offset]);
		}
		final int hotp = buffer.getInt(0) & 0x7fffffff;
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Key=" + StringUtils.binaryToHexString(key, 0, key.length));
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Udid=" + StringUtils.binaryToHexString(deviceUdid, 0, deviceUdid.length));
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() PassCode=" + hotp % modDivisor);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Counter=" + counter);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() Date=" + new Date());
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() passcodeValidFor=" + passcodeValidForMilli);
		// System.out.println("TimeBasedPasscodeGenerator.generatePasscode() deviceUdid.hashCode()=" + Arrays.hashCode(deviceUdid));
		// System.out.println("");
		return hotp % modDivisor;
	}

}
