package com.doubleclue.dcem.radius.logic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.radius.attribute.RadiusAttribute;
import com.doubleclue.dcem.radius.attribute.RadiusAttributeEnum;
import com.doubleclue.dcem.radius.attribute.StringAttribute;
import com.doubleclue.utils.KaraUtils;

/**
 * This class represents an Access-Request Radius packet.
 */
public class AccessRequest extends RadiusPacket {
	
	private static final Logger logger = LogManager.getLogger(AccessRequest.class);

	/*TODO: check: is password hash improvement via bcrypt needed here? */

	/**
	 * Passphrase Authentication Protocol
	 */
	public static final String AUTH_PAP = "pap";

	/**
	 * Challenged Handshake Authentication Protocol
	 */
	public static final String AUTH_CHAP = "chap";

	/**
	 * Constructs an empty Access-Request packet.
	 */
	public AccessRequest() {
		super();
	}

	/**
	 * Constructs an Access-Request packet, sets the code, identifier and adds
	 * an User-Name and an User-Password attribute (PAP).
	 * 
	 * @param userName
	 *            user name
	 * @param userPassword
	 *            user password
	 */
	public AccessRequest(String userName, byte [] userPassword) {
		super(RadiusPacketType.ACCESS_REQUEST, getNextPacketIdentifier());
		setUserName(userName);
		setUserPassword(userPassword);
	}

	/**
	 * Sets the User-Name attribute of this Access-Request.
	 * 
	 * @param userName
	 *            user name to set
	 */
	public void setUserName(String userName) {
		if (userName == null)
			throw new NullPointerException("user name not set");
		if (userName.length() == 0)
			throw new IllegalArgumentException("empty user name not allowed");

		removeAttributes(RadiusAttributeEnum.UserName.getType());
		addAttribute(new StringAttribute(RadiusAttributeEnum.UserName.getType(), userName));
	}

	/**
	 * Sets the plain-text user password.
	 * 
	 * @param userPassword
	 *            user password to set
	 */
	public void setUserPassword(byte[] userPassword) {
		if (userPassword == null || userPassword.length == 0)
			throw new IllegalArgumentException("password is empty");
		this.password = userPassword;
	}

	/**
	 * Retrieves the plain-text user password. Returns null for CHAP - use
	 * verifyPassword().
	 * 
	 * @see #verifyPassword(String)
	 * @return user password
	 */
	public byte [] getUserPassword() {
		return password;
	}

	/**
	 * Retrieves the user name from the User-Name attribute.
	 * 
	 * @return user name
	 */
	public String getUserName() {
		RadiusAttribute attr = getAttribute(RadiusAttributeEnum.UserName);
		if (attr == null) {
			return null;
		}
 		return ((StringAttribute) attr).getAttributeValue(charset);
	}

	/**
	 * Returns the protocol used for encrypting the passphrase.
	 * 
	 * @return AUTH_PAP or AUTH_CHAP
	 */
	public String getAuthProtocol() {
		return authProtocol;
	}

	/**
	 * Selects the protocol to use for encrypting the passphrase when encoding
	 * this Radius packet.
	 * 
	 * @param authProtocol
	 *            AUTH_PAP or AUTH_CHAP
	 */
	public void setAuthProtocol(String authProtocol) {
		if (authProtocol != null && (authProtocol.equals(AUTH_PAP) || authProtocol.equals(AUTH_CHAP)))
			this.authProtocol = authProtocol;
		else
			throw new IllegalArgumentException("protocol must be pap or chap");
	}

	/**
	 * Verifies that the passed plain-text password matches the password (hash)
	 * send with this Access-Request packet. Works with both PAP and CHAP.
	 * 
	 * @param plaintext
	 * @return true if the password is valid, false otherwise
	 */
	public boolean verifyPassword(String plaintext) throws RadiusException {
		if (plaintext == null || plaintext.length() == 0)
			throw new IllegalArgumentException("password is empty");
		if (getAuthProtocol().equals(AUTH_CHAP)) {
			return verifyChapPassword(plaintext);
		} else {
			return getUserPassword().equals(plaintext);
		}
	}
	
	public boolean verifyHash(byte [] password, byte [] pinHash) throws RadiusException {
		if (getAuthProtocol().equals(AUTH_CHAP)) {
			throw new RadiusException("Chap Password authentication is not supported");
		} else {
			byte[] recPinHash;
			try {
				recPinHash = KaraUtils.getSha1(password);
			} catch (Exception e) {
				throw new RadiusException("Sha1 failed");
			} 
			return Arrays.equals(pinHash, recPinHash);
		}
	}

	/**
	 * Decrypts the User-Password attribute.
	 * 
	 * @see org.tinyradius.packet.RadiusPacket#decodeRequestAttributes(java.lang.String)
	 */
	protected void decodeRequestAttributes(String sharedSecret) throws RadiusException {
		// detect auth protocol
		RadiusAttribute userPassword = getAttribute(USER_PASSWORD);
		RadiusAttribute chapPassword = getAttribute(CHAP_PASSWORD);
		RadiusAttribute chapChallenge = getAttribute(CHAP_CHALLENGE);

		if (userPassword != null) {
			setAuthProtocol(AUTH_PAP);
			this.password = decodePapPassword(userPassword.getAttributeData(), sharedSecret.getBytes(DcemConstants.UTF_8));
			// copy truncated data
			userPassword.setAttributeData(password);
		} else if (chapPassword != null && chapChallenge != null) {
			setAuthProtocol(AUTH_CHAP);
			this.chapPassword = chapPassword.getAttributeData();
			this.chapChallenge = chapChallenge.getAttributeData();
		} 
	}

	/**
	 * Sets and encrypts the User-Password attribute.
	 * 
	 */
	protected void encodeRequestAttributes(String sharedSecret) {
		if (password == null || password.length == 0)
			return;
		// ok for proxied packets whose CHAP password is already encrypted
		// throw new RuntimeException("no password set");

		if (getAuthProtocol().equals(AUTH_PAP)) {
			byte[] pass = encodePapPassword(password, sharedSecret.getBytes(StandardCharsets.UTF_8));
			removeAttributes(USER_PASSWORD);
			addAttribute(new RadiusAttribute(USER_PASSWORD, pass));
		} else if (getAuthProtocol().equals(AUTH_CHAP)) {
			byte[] challenge = createChapChallenge();
			byte[] pass = encodeChapPassword(password, challenge);
			removeAttributes(CHAP_PASSWORD);
			removeAttributes(CHAP_CHALLENGE);
			addAttribute(new RadiusAttribute(CHAP_PASSWORD, pass));
			addAttribute(new RadiusAttribute(CHAP_CHALLENGE, challenge));
		}
	}

	/**
	 * This method encodes the plaintext user password according to RFC 2865.
	 * 
	 * @param userPass
	 *            the password to encrypt
	 * @param sharedSecret
	 *            shared secret
	 * @return the byte array containing the encrypted password
	 */
	private byte[] encodePapPassword(final byte[] userPass, byte[] sharedSecret) {
		// the password must be a multiple of 16 bytes and less than or equal
		// to 128 bytes. If it isn't a multiple of 16 bytes fill it out with
		// zeroes
		// to make it a multiple of 16 bytes. If it is greater than 128 bytes
		// truncate it at 128.
		byte[] userPassBytes = null;
		if (userPass.length > 128) {
			userPassBytes = new byte[128];
			System.arraycopy(userPass, 0, userPassBytes, 0, 128);
		} else {
			userPassBytes = userPass;
		}

		// declare the byte array to hold the final product
		byte[] encryptedPass = null;
		if (userPassBytes.length < 128) {
			if (userPassBytes.length % 16 == 0) {
				// tt is already a multiple of 16 bytes
				encryptedPass = new byte[userPassBytes.length];
			} else {
				// make it a multiple of 16 bytes
				encryptedPass = new byte[((userPassBytes.length / 16) * 16) + 16];
			}
		} else {
			// the encrypted password must be between 16 and 128 bytes
			encryptedPass = new byte[128];
		}

		// copy the userPass into the encrypted pass and then fill it out with
		// zeroes
		System.arraycopy(userPassBytes, 0, encryptedPass, 0, userPassBytes.length);
		for (int i = userPassBytes.length; i < encryptedPass.length; i++) {
			encryptedPass[i] = 0;
		}

		// digest shared secret and authenticator
		MessageDigest md5 = getMd5Digest();
		byte[] lastBlock = new byte[16];

		for (int i = 0; i < encryptedPass.length; i += 16) {
			md5.reset();
			md5.update(sharedSecret);
			md5.update(i == 0 ? getAuthenticator() : lastBlock);
			byte bn[] = md5.digest();

			System.arraycopy(encryptedPass, i, lastBlock, 0, 16);

			// perform the XOR as specified by RFC 2865.
			for (int j = 0; j < 16; j++)
				encryptedPass[i + j] = (byte) (bn[j] ^ encryptedPass[i + j]);
		}

		return encryptedPass;
	}

	/**
	 * Decodes the passed encrypted password and returns the clear-text form.
	 * 
	 * @param encryptedPass
	 *            encrypted password
	 * @param sharedSecret
	 *            shared secret
	 * @return decrypted password
	 */
	private byte [] decodePapPassword(byte[] encryptedPass, byte[] sharedSecret) throws RadiusException {
		if (encryptedPass == null || encryptedPass.length < 16) {
			// PAP passwords require at least 16 bytes
			logger.warn("invalid Radius packet: User-Password attribute with malformed PAP password, length = "
					+ encryptedPass.length + ", but length must be greater than 15");
			throw new RadiusException("malformed User-Password attribute");
		}

		MessageDigest md5 = getMd5Digest();
		byte[] lastBlock = new byte[16];

		for (int i = 0; i < encryptedPass.length; i += 16) {
			md5.reset();
			md5.update(sharedSecret);
			md5.update(i == 0 ? getAuthenticator() : lastBlock);
			byte bn[] = md5.digest();

			System.arraycopy(encryptedPass, i, lastBlock, 0, 16);

			// perform the XOR as specified by RFC 2865.
			for (int j = 0; j < 16; j++)
				encryptedPass[i + j] = (byte) (bn[j] ^ encryptedPass[i + j]);
		}

		// remove trailing zeros
		int len = encryptedPass.length;
		while (len > 0 && encryptedPass[len - 1] == 0)
			len--;
		byte[] passtrunc = new byte[len];
		System.arraycopy(encryptedPass, 0, passtrunc, 0, len);
		return passtrunc;
	}

	/**
	 * Creates a random CHAP challenge using a secure random algorithm.
	 * 
	 * @return 16 byte CHAP challenge
	 */
	private byte[] createChapChallenge() {
		byte[] challenge = new byte[16];
		random.nextBytes(challenge);
		return challenge;
	}

	/**
	 * Encodes a plain-text password using the given CHAP challenge.
	 * 
	 * @param plaintext
	 *            plain-text password
	 * @param chapChallenge
	 *            CHAP challenge
	 * @return CHAP-encoded password
	 */
	private byte[] encodeChapPassword(byte[] plaintext, byte[] chapChallenge) {
		// see RFC 2865 section 2.2
		byte chapIdentifier = (byte) random.nextInt(256);
		byte[] chapPassword = new byte[17];
		chapPassword[0] = chapIdentifier;

		MessageDigest md5 = getMd5Digest();
		md5.reset();
		md5.update(chapIdentifier);
		md5.update(plaintext);
		byte[] chapHash = md5.digest(chapChallenge);

		System.arraycopy(chapHash, 0, chapPassword, 1, 16);
		return chapPassword;
	}

	/**
	 * Verifies a CHAP password against the given plaintext password.
	 * 
	 * @return plain-text password
	 */
	private boolean verifyChapPassword(String plaintext) throws RadiusException {
		if (plaintext == null || plaintext.length() == 0)
			throw new IllegalArgumentException("plaintext must not be empty");
		if (chapChallenge == null || chapChallenge.length != 16)
			throw new RadiusException("CHAP challenge must be 16 bytes");
		if (chapPassword == null || chapPassword.length != 17)
			throw new RadiusException("CHAP password must be 17 bytes");

		byte chapIdentifier = chapPassword[0];
		MessageDigest md5 = getMd5Digest();
		md5.reset();
		md5.update(chapIdentifier);
		md5.update(plaintext.getBytes(charset));
		byte[] chapHash = md5.digest(chapChallenge);

		// compare
		for (int i = 0; i < 16; i++)
			if (chapHash[i] != chapPassword[i + 1])
				return false;
		return true;
	}

	/**
	 * Temporary storage for the unencrypted User-Password attribute.
	 */
	private byte [] password;

	/**
	 * Authentication protocol for this access request.
	 */
	private String authProtocol = AUTH_PAP;

	/**
	 * CHAP password from a decoded CHAP Access-Request.
	 */
	private byte[] chapPassword;

	/**
	 * CHAP challenge from a decoded CHAP Access-Request.
	 */
	private byte[] chapChallenge;

	/**
	 * Random generator
	 */
	private static SecureRandom random = new SecureRandom();

	
	/**
	 * Radius attribute type for User-Password attribute.
	 */
	private static final int USER_PASSWORD = 2;

	/**
	 * Radius attribute type for CHAP-Password attribute.
	 */
	private static final int CHAP_PASSWORD = 3;

	/**
	 * Radius attribute type for CHAP-Challenge attribute.
	 */
	private static final int CHAP_CHALLENGE = 60;


}
