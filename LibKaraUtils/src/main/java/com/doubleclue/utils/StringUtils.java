package com.doubleclue.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	private static final String HEX = "0123456789ABCDEF";
	private static String specialCharacters = "!#$%&'()*+/:;<=>?[]^`{|}~";
	private static String specialCharactersFileName = "@!#$%&'*+/:;<=>?^`|~";

	static String getNextLine(InputStream inputStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		int i;
		while (true) {
			i = inputStream.read();
			if (i == -1) {
				if (sb.length() == 0) {
					return null;
				} else {
					return sb.toString();
				}
			}
			if (i == '\r') { // ignore
				continue;
			}
			if (i == '\n') {
				break;
			}
			sb.append((char) i);
		}
		return sb.toString();
	}

	public static String substituteTemplate(String template, Map<String, String> map) {
		Pattern pattern = Pattern.compile("\\{\\{(.+?)\\}\\}");
		Matcher matcher = pattern.matcher(template);
		// StringBuilder cannot be used here because Matcher expects StringBuffer
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			if (map.containsKey(matcher.group(1))) {
				String replacement = map.get(matcher.group(1));
				// quote to work properly with $ and {,} signs
				if (replacement != null) {
					matcher.appendReplacement(buffer, replacement != null ? Matcher.quoteReplacement(replacement) : "null");
				}
			}
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * Returns the byte array as a hex string
	 *
	 * @param data
	 *            byte array
	 * @return hex string
	 */
	public static String getHexString(byte[] data) {
		StringBuilder hex = new StringBuilder("0x");
		if (data != null)
			for (byte aData : data) {
				String digit = Integer.toString(aData & 0x0ff, 16);
				if (digit.length() < 2)
					hex.append('0');
				hex.append(digit);
				hex.append(' ');
			}

		return hex.toString();
	}

	public static String getHexStringRaw(byte[] data) {
		StringBuilder sb = new StringBuilder();
		if (data != null)
			for (byte ind : data) {
				sb.append(HEX.charAt((ind & 0x00ff) >> 4));
				sb.append(HEX.charAt(ind & 0x000f));
			}
		return sb.toString();
	}

	/**
	 * Creates a string from the passed byte array containing the string in UTF-8
	 * representation.
	 *
	 * @param utf8
	 *            UTF-8 byte array
	 * @return Java string
	 */
	public static String getStringFromUtf8(byte[] utf8) {
		return new String(utf8, StandardCharsets.UTF_8);
	}

	public static byte[] getBytesFromUtf8(String value) {
		try {
			return value.getBytes("UTF-8");
		} catch (UnsupportedEncodingException uee) {
			return new byte[0];
		}
	}

	/**
	 * Creates a Byte Array from a Hex String
	 *
	 * @param hexString
	 * @return sequence of byte arrays specified by hexadecial formatted string
	 * @throws Exception
	 */
	public static byte[] hexStringToBinary(String hexString) throws Exception {
		int len = hexString.length();
		if (len % 2 != 0) {
			throw new Exception("wrong string length");
		}
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
		}
		return data;
	}

	/**
	 * @param b
	 * @param len
	 * @param offset
	 * @return
	 */
	static public String binaryToHexString(byte[] b, int offset, int len) {
		int length = b.length;

		if (offset < 0 || offset >= length) {
			throw new IndexOutOfBoundsException("Invalid offset: " + offset);
		}
		if (len < 0) {
			len = length - offset;
		}
		StringBuilder builder = new StringBuilder(len * 2);
		for (int i = offset; i < len + offset; i++) {
			if (i >= length) {
				throw new IndexOutOfBoundsException("Index " + i + " is greater than length " + length);
			}
			builder.append(HEX.charAt((b[i] >> 4) & 0x0f));
			builder.append(HEX.charAt(b[i] & 0x0f));
		}
		return builder.toString();
	}

	/**
	 * @param buffer
	 * @param len
	 * @param offset
	 * @return
	 */
	static byte[] binaryToHex(byte[] buffer, int len, int offset) {
		int length = buffer.length;
		byte[] data = new byte[len * 2];
		if (offset < 0 || offset >= length) {
			throw new IndexOutOfBoundsException("Invalid offset: " + offset);
		}
		if (len < 0) {
			len = length - offset;
		}
		int dataIndex = 0;
		for (int i = offset; i < len + offset; i++) {
			if (i >= length) {
				throw new IndexOutOfBoundsException("Index " + i + " is greater than length " + length);
			}
			data[dataIndex++] = (byte) HEX.charAt((buffer[i] >> 4) & 0x0f);
			data[dataIndex++] = (byte) (HEX.charAt(buffer[i] & 0x0f));
		}
		return data;
	}

	/**
	 * Converts an Char Array to an Byte Array
	 *
	 * @param chars
	 *            Input Char Array
	 * @return the Byte Array
	 */
	public static byte[] charsToBytes(char[] chars) {
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
		byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
		return bytes;
	}

	/**
	 * Converts an Char Array to an Byte Buffer
	 *
	 * @param chars
	 *            Input Char Array
	 * @return a Byte Buffer
	 */
	public static ByteBuffer charsToByteBuffer(char[] chars) {
		CharBuffer charBuffer = CharBuffer.wrap(chars);
		ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
		Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
		return byteBuffer;
	}

	/**
	 * @param buffer
	 * @param hexa
	 * @return
	 */
	// public static String traceBuffer(ByteBuffer buffer, boolean hexa) {
	// int length = buffer.limit() - buffer.position();
	// StringBuilder stringBuffer = new StringBuilder();
	// stringBuffer.append("Length: ");
	// stringBuffer.append(length);
	// stringBuffer.append(", Data: ");
	// if (!hexa) {
	// stringBuffer.append(new String(buffer.array(), buffer.position(), length));
	// }
	// return stringBuffer.toString();
	// }

	/**
	 * @param buffer
	 * @param offset
	 * @param hexa
	 * @return
	 */
	public static String traceBuffer(byte[] buffer, int offset, int length, boolean hexa) {
		StringBuilder stringBuffer = new StringBuilder();
		stringBuffer.append("Length: ");
		stringBuffer.append(buffer.length - offset);
		stringBuffer.append(",  Data: ");
		if (hexa == false) {
			stringBuffer.append(new String(buffer, offset, buffer.length));
		} else {
			stringBuffer.append(binaryToHexString(buffer, offset, length));
		}
		return stringBuffer.toString();
	}

	public static boolean isValidNameId(String name) {
		for (int i = 0; i < name.length(); i++) {
			if (specialCharacters.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidName(String name) {
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '\\') {
				return false;
			}
			if (specialCharacters.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidFileName(String name) {
		for (int i = 0; i < name.length(); i++) {
			if (name.charAt(i) == '\\') {
				return false;
			}
			if (specialCharactersFileName.contains(name.subSequence(i, i + 1))) {
				return false;
			}
		}
		return true;
	}

	public static void wipeString(String secret) {
		if (secret == null) {
			return;
		}
		try {
			Field valueField = String.class.getDeclaredField("value");
			valueField.setAccessible(true);
			char[] chars = (char[]) valueField.get(secret);
			Arrays.fill(chars, '-');
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}