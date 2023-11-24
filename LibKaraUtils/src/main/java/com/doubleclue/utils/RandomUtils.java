package com.doubleclue.utils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;


/**
 * 
 * @author Emanuel Galea
 *
 */
public class RandomUtils {

//	private static Logger logger = LogManager.getLogger(RandomUtils.class);
	
	private static SecureRandom random = null;

	static {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Creating secure random FAILED due to:" + e.getMessage());
		}
	}
	
	/**
	 * Array that covers all alpha numeric characters allowed by IA5.
	 */
	private static final char[] ALPHA_NUMERIC_ALPHABET = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',
			'R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
			's','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};
	
	/**
	 * Array that covers all alpha numeric characters allowed by IA5.
	 */
	private static final char[] ALPHA_LOWERCASE_NUMERIC_ALPHABET = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
			's','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9'};
	
	/**
	 * Array that covers all alpha numeric characters allowed by IA5.
	 */
	private static final char[] ALPHA_UPPERCASE_NUMERIC_ALPHABET = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R',
			'S','T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};
	
	/**
	 * Array that covers all alpha numeric characters allowed by IA5 and special  chars +-=_‘“*%&/\()^~@#€$£?!,;.: and ß and space.
	 */
	private static final char[] ALPHA_NUMERIC_ALPHABET_SPECIALCHARS = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q',
			'R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r',
			's','t','u','v','w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','-','=','_','\'','"','*','%','&','/','(',')',
			'@','#','$','?','!',';','.',':'};	


	public static final String generateRandomSessionID() {
		String allowedChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		int max = allowedChars.length();
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < 32; i++) {
			int value = random.nextInt(max);
			builder.append(allowedChars.charAt(value));
		}

		return builder.toString();
	}

	
	/**
	 * Create a random byte array using the algorithm "SHA1PRNG".
	 * 
	 * @param length
	 *            Length of the random byte array.
	 * @return random challenge
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] getRandom(int length)  {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}
	
	public static long getRandomLong()  {
		return 	random.nextLong();
	}
	public static int getRandomInt()  {
		return 	random.nextInt();
	}

	
	/**
	 * creates a random number string ([0-9])+ with @param digits characters
	 * 
	 * @return String random number string
	 */
	public static String generateRandomNumberString(int digits) {
	
		StringBuilder buff = new StringBuilder();
	
		for (int i = 0; i < digits; i++) {
			buff.append(random.nextInt(10));
		}
	
		return buff.toString();
	}

	/**
	 * Creates a string containing SUBJECTDN_SERIALNUMBER_LENGTH alpha numerical characters. 
	 * @return String
	 */
	public static String generateRandomAlphaNumericString(int length){
		
		assert(length > 0);

		StringBuilder buff = new StringBuilder(length);
		for(int i = 0; i < length; i++){
			buff.append(RandomUtils.ALPHA_NUMERIC_ALPHABET[random.nextInt(RandomUtils.ALPHA_NUMERIC_ALPHABET.length)]);
		}
		return buff.toString();
	}
	
	/**
	 * Creates a string containing a char array containing a randomly created password with alpha lowercase & numerical characters. 
	 * @return String
	 */
	public static String generateRandomAlphaLowercaseNumericString(int length){
		StringBuilder buff = new StringBuilder(length);
		for(int i = 0; i < length; i++){
			buff.append(RandomUtils.ALPHA_LOWERCASE_NUMERIC_ALPHABET[random.nextInt(RandomUtils.ALPHA_LOWERCASE_NUMERIC_ALPHABET.length)]);
		}
		return buff.toString();
	}
	
	/**
	 * Creates a string containing a char array containing a randomly created password with alpha lowercase & numerical characters. 
	 * @return String
	 */
	public static String generateRandomAlphaUppercaseNumericString(int length){
		StringBuilder buff = new StringBuilder(length);
		for(int i = 0; i < length; i++){
			buff.append(RandomUtils.ALPHA_UPPERCASE_NUMERIC_ALPHABET[random.nextInt(RandomUtils.ALPHA_UPPERCASE_NUMERIC_ALPHABET.length)]);
		}
		return buff.toString();
	}
	
	public static String generatePassword(int length){
		StringBuilder buff = new StringBuilder(length);
		for(int i = 0; i < length; i++){
			buff.append(RandomUtils.ALPHA_UPPERCASE_NUMERIC_ALPHABET[random.nextInt(RandomUtils.ALPHA_UPPERCASE_NUMERIC_ALPHABET.length)]);
		}
		return buff.toString();
	}

	/**
	 * Creates a char array containing  a char array containing a randomly created password with alpha & numerical characters. 
	 * @return char[]
	 */
	public static char[] generateRandomAlphaNumericCharArray(int length){
		
		assert(length > 0);
		
		char[] buff = new char[length];
		for(int i = 0; i < length; i++){
			buff[i] = RandomUtils.ALPHA_NUMERIC_ALPHABET[random.nextInt(RandomUtils.ALPHA_NUMERIC_ALPHABET.length)];
		}
		return buff;
	}

	/**
	 * Generates a char array containing a randomly created password using th char set given by passwordGenerationPattern
	 * @param length - int - length of password char array to create
	 * @param passwordGenerationPattern - String - a set of chars used as generation pool for password chars like "abcdefgh12345$%&/()=.,", no regular expression allowed
	 * @return char [] random password
	 * @throws AssertionError when length<=0, passwordGenerationPattern is null or empty
	 */
	public static char[] generateRandomByPasswordGenerationPattern(int length, String passwordGenerationPattern){
		char[] pswdGenCharSet = passwordGenerationPattern.toCharArray();
		char[] buff = new char[length];
		for(int i = 0; i < length; i++){
			buff[i] = pswdGenCharSet[random.nextInt(pswdGenCharSet.length)];
		}
		return buff;
	}

	/**
	 * simple password generator for extended alphanumeric character set including ß and special characters 
	 * "+-=_‘“*%&/\\()^~@#€$£?!,;.: ". It is not rules based. Generates a password of random length [minLengthOfPswd, MaxLengthOfPassword]
	 * @param minlengthOfPswd - minimum length of password to be generated (> 4)
	 * @param maxlengthOfPswd - maximum length of password to be generated
	 * @return
	 */
	public static final String generateRandomPasswordExtAlphaNumeric(int lenght )  {
		return new String (generateRandomByPasswordGenerationPattern(8, new String (ALPHA_NUMERIC_ALPHABET_SPECIALCHARS)));
	}
	
		
	public static boolean generateRandomBoolean() {
		return random.nextBoolean();
	}
	
	
}
