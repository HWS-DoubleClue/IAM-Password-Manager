package com.doubleclue.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.AppVersion;
import com.doubleclue.comm.thrift.SdkConfig;

public class KaraUtils {

	private static String OS = System.getProperty("os.name").toLowerCase();

	final static public String IMPL_VERSION = "Implementation-Version";
	private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
	public static final boolean IS_PLATFORM_LINUX = isOsMatchesName("Linux") || isOsMatchesName("LINUX");
	public static final boolean IS_PLATFORM_WINDOWS = isOsMatchesName(OS_NAME_WINDOWS_PREFIX);
	public static final boolean IS_PLATFORM_ANDROID = IS_PLATFORM_LINUX && isJavaVendor("Android");
	public static final boolean IS_PLATFORM_IOS = isJavaVendor("J2ObjC");
	public static final boolean IS_PLATFORM_MAC = ((OS.contains("mac")) || (OS.contains("darwin")));
	public static final String VERSION_DELIMITER = ".";
	public static final int VERSION_ABS = 0x3FF;
	public static final int VERSION_BITS = 10;

	// public static PlatformInterface platformInterface = null;

	private static String os = null;

	static public String getNextLine(InputStream inputStream) throws IOException {
		StringBuffer sb = new StringBuffer();
		int i;
		while (true) {
			i = inputStream.read();
			if (i == -1) {
				return null;
			}
			if (i == '\n') {
				break;
			}
			sb.append((char) i);
		}
		return sb.toString();
	}
	
	public static int getNumeric(String strNum) {
		if (strNum == null) {
			return -1;
		}
		try {
			return Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	/**
	 * @param clazz
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	static public Attributes getManifestInformation(Class<?> clazz) throws IOException {
		String className = clazz.getSimpleName() + ".class";
		String classPath = clazz.getResource(className).toString();
		return getManifestInformation(classPath);
	}

	static public Attributes getManifestInformation(String classPath) throws IOException {
		if (classPath.startsWith("jar") == true) {

			String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
			Manifest manifest;
			InputStream inputStream = new URL(manifestPath).openStream();
			manifest = new Manifest(inputStream);
			Attributes attributes = manifest.getMainAttributes();
			inputStream.close();
			return attributes;
		} else {
			// ONLX in Development environment
			int ind = classPath.indexOf("/bin/");
			URL url;
			if (ind > 0) {
				String urlPath = classPath.substring(0, ind + 5) + "META-INF/MANIFEST.MF";
				url = new URL(urlPath);
			} else {
				ind = classPath.indexOf("/target/classes/");
				if (ind > 0) {
					String urlPath = classPath.substring(0, ind + "/target/classes/".length()) + "META-INF/MANIFEST.MF";
					url = new URL(urlPath);
				} else {
					return null;
				}
			}
			InputStream inputStream = url.openStream();
			Manifest manifest = new Manifest(inputStream);
			Attributes attributes = manifest.getMainAttributes();
			inputStream.close();
			return attributes;
		}

	}

	public static ProductVersion getProductVersion(Class<?> clazz) throws Exception {
		ProductVersion productVersion;
		try {
			Attributes attributes = KaraUtils.getManifestInformation(clazz);
			if (attributes == null) {
				productVersion = new ProductVersion(null, "99.99.99-Unknown");
			} else {
				productVersion = new ProductVersion(null, attributes.getValue(Name.IMPLEMENTATION_VERSION));
				String scmVersion = attributes.getValue(ProductVersion.SVN_NUMBER);
				if (scmVersion != null && !scmVersion.isEmpty()) {
					productVersion.setSvnBuildNr(scmVersion);
				}
			}
		} catch (Exception e) {
			System.err.println("getProductVersion() " + e.toString());
			productVersion = new ProductVersion(null, "1.0.0");
		}
		return productVersion;
	}

	public static byte[] getSha1(char[] chars) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(new String(chars).getBytes("UTF-8"));
		return md.digest();
	}

	public static byte[] getSha1(byte[] data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(data);
		return md.digest();
	}

	public static byte[] getSha1WithSalt(byte[] salt, byte[] data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		if (salt != null) {
			md.update(salt);
		}
		md.update(data);
		return md.digest();
	}

	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		copyStream(inputStream, outputStream);
		return outputStream.toByteArray();
	}

	public static int copyStream(InputStream inputStream, OutputStream outputStream) throws IOException {
		return copyStream(inputStream, outputStream, 1024);
	}

	public static int copyStream(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
		int len;
		int count = 0;
		byte[] buffer = new byte[bufferSize];
		while ((len = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, len);
			count += len;
		}
		return count;
	}

	public static int copyStream(InputStream inputStream, StringWriter writer) throws IOException {
		String text = readInputStreamText(inputStream);
		writer.append(text);
		return text.length();
	}

	public static String readInputStreamText(InputStream inputStream) throws IOException {
		return new String(readInputStream(inputStream), "UTF-8");
	}

	// static public PlatformInterface getPlatformInterface() throws Exception {
	// return platformInterface;
	// }
	static public PlatformInterface getPlatformInterface() throws Exception {
		return getPlatformInterface(null);
	}

	static public PlatformInterface getPlatformInterface(Object object) throws Exception {
		PlatformInterface platformInterface = null;
		if (IS_PLATFORM_WINDOWS) {
			platformInterface = (PlatformInterface) Class.forName("com.doubleclue.platform.windows.WindowsPlatform").newInstance();
		} else if (IS_PLATFORM_LINUX && !IS_PLATFORM_ANDROID) { // Android is also a Linux Platform
			platformInterface = (PlatformInterface) Class.forName("com.doubleclue.platform.linux.LinuxPlatform").newInstance();
		} else if (IS_PLATFORM_ANDROID) {
			platformInterface = (PlatformInterface) Class.forName("com.doubleclue.platform.android.AndroidPlatform").getConstructor(Object.class)
					.newInstance(object);
		} else if (IS_PLATFORM_IOS) {
			platformInterface = (PlatformInterface) Class.forName("DCIosPlatform").newInstance();
		} else if (IS_PLATFORM_MAC) {
			platformInterface = (PlatformInterface) Class.forName("com.doubleclue.platform.mac.MacPlatform").newInstance();
		}
		return platformInterface;
	}

	

	public static Throwable getRootCause(Exception exception) {
		return getRootCause(exception, (String) null);
	}

	public static Throwable getRootCause(Exception exception, String findExpStartWith) {
		Throwable cause = exception;
		while (cause.getCause() != null) {
			cause = cause.getCause();
			if (findExpStartWith != null) {
				if (cause.getClass().getSimpleName().startsWith(findExpStartWith)) {
					return cause;
				}
			}
		}
		if (findExpStartWith != null) {
			return null;
		}
		return cause;
	}

	public static Throwable getRootCause(Exception exception, Class klass) {
		Throwable cause = exception;
		while (cause.getCause() != null) {
			cause = cause.getCause();
			if (cause.getClass().equals(klass)) {
				return cause;
			}
		}
		return null;
	}

	public static String getOsName() {
		if (os == null) {
			os = System.getProperty("os.name");
		}
		return os;
	}

	private static boolean isOsMatchesName(final String osNamePrefix) {
		String osName = System.getProperty("os.name");
		return osName.startsWith(osNamePrefix);
	}

	private static boolean isJavaVendor(final String match) {
		String vendor = System.getProperty("java.vendor");
		return vendor.indexOf(match) > 0 || vendor.equals(match);
	}

	/**
	 * @param data
	 * @return
	 */
	public static byte[] intToByteArray(int data) {
		byte[] result = new byte[4];
		result[0] = (byte) ((data & 0xFF000000) >> 24);
		result[1] = (byte) ((data & 0x00FF0000) >> 16);
		result[2] = (byte) ((data & 0x0000FF00) >> 8);
		result[3] = (byte) ((data & 0x000000FF) >> 0);
		return result;
	}

	public static void intToByteArray(int data, byte[] result) {
		result[0] = (byte) ((data & 0xFF000000) >> 24);
		result[1] = (byte) ((data & 0x00FF0000) >> 16);
		result[2] = (byte) ((data & 0x0000FF00) >> 8);
		result[3] = (byte) ((data & 0x000000FF) >> 0);
		return;
	}

	public static int byteArrayToInt(byte[] bytes) {
		return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | ((bytes[3] & 0xFF) << 0);
	}

	public static byte[] longToByteArray(long value) {
		return new byte[] { (byte) (value >> 56), (byte) (value >> 48), (byte) (value >> 40), (byte) (value >> 32), (byte) (value >> 24), (byte) (value >> 16),
				(byte) (value >> 8), (byte) value };
	}

	// public static byte[] serializeObject(Object object) {
	// ByteArrayOutputStream bos = new ByteArrayOutputStream();
	// XMLEncoder xmlEncoder = new XMLEncoder(bos);
	// xmlEncoder.writeObject(object);
	// xmlEncoder.flush();
	// xmlEncoder.close();
	// return bos.toByteArray();
	// }
	//
	// public static Object deserializeObject(byte[] data) {
	// XMLDecoder xmlDecoder = new XMLDecoder(new ByteArrayInputStream(data));
	// Object object = xmlDecoder.readObject();
	// xmlDecoder.close();
	//
	// return object;
	// }

	public static String mapToString(Map<String, String> map) {
		if (map == null) {
			return null;
		}
		StringBuilder stringBuilder = new StringBuilder();

		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			String value = map.get(key);
			try {
				stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
				stringBuilder.append("=");
				stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		return stringBuilder.toString();
	}

	public static Map<String, String> stringToMap(String input) {
		Map<String, String> map = new HashMap<String, String>();

		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"), nameValue.length > 1 ? URLDecoder.decode(nameValue[1], "UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("This method requires UTF-8 encoding support", e);
			}
		}
		return map;
	}

	/**
	 * @param sdkConfigDcemContent
	 * @return
	 * @throws KaraException
	 */
	public static SdkConfigDcem parseSdkConfig(byte[] sdkConfigDcemContent) throws KaraException {
		SdkConfigDcem sdkConfigDcem = new SdkConfigDcem();
		ZipInputStream zipInputStream = null;
		ByteArrayInputStream baip;
		ZipEntry zipEntry;
		byte[] sdkConfigContent = null;
		byte[] signature = null;
		byte[] trustStorePem = null;
		baip = new ByteArrayInputStream(sdkConfigDcemContent);
		zipInputStream = new ZipInputStream(baip);
		try {
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				if (zipEntry.getName().equals(AppSystemConstants.SdkConfigFileName)) {
					sdkConfigContent = KaraUtils.readInputStream(zipInputStream);
				} else if (zipEntry.getName().equals(AppSystemConstants.TrustStoreFileName)) {
					trustStorePem = KaraUtils.readInputStream(zipInputStream);
				} else if (zipEntry.getName().equals(AppSystemConstants.SignatureFileName)) {
					signature = KaraUtils.readInputStream(zipInputStream);
				} else {
					throw new KaraException(KaraErrorCodes.UNKNOWN_FILE_IN_CONFIG, zipEntry.getName());
				}
			}
			zipInputStream.close();
		} catch (Exception e) {
			throw new KaraException(KaraErrorCodes.CORRUPTED_SDKCONFIG, e.toString());
		}
		if (sdkConfigContent == null) {
			throw new KaraException(KaraErrorCodes.MISSING_FILE_IN_CONFIG, AppSystemConstants.SdkConfigFileName);
		}
		if (signature == null) {
			throw new KaraException(KaraErrorCodes.MISSING_FILE_IN_CONFIG, AppSystemConstants.SignatureFileName);
		}
		if (trustStorePem == null) {
			throw new KaraException(KaraErrorCodes.MISSING_FILE_IN_CONFIG, AppSystemConstants.TrustStoreFileName);
		}
		// check the signature
		byte[] sigData = new byte[trustStorePem.length + sdkConfigContent.length];
		System.arraycopy(trustStorePem, 0, sigData, 0, trustStorePem.length);
		System.arraycopy(sdkConfigContent, 0, sigData, trustStorePem.length, sdkConfigContent.length);
		try {
			if (Arrays.equals(signature, SecureUtils.createMacDigestCommonSha2(sigData, 0, sigData.length)) == false) {
				throw new KaraException(KaraErrorCodes.CORRUPTED_SDKCONFIG, null);
			}
		} catch (Exception e) {
			throw new KaraException(KaraErrorCodes.CORRUPTED_SDKCONFIG, e.toString());
		}
		try {
			List<byte[]> trustCertsBytes = SecureUtils.convertPemToCerList(trustStorePem);
			X509Certificate[] trustCertsX509 = new X509Certificate[trustCertsBytes.size()];
			if (trustCertsBytes != null && trustCertsBytes.isEmpty() == false) {
				int i = 0;
				for (byte[] cert : trustCertsBytes) {
					CertificateFactory cf = CertificateFactory.getInstance("X.509");
					java.security.cert.Certificate xCert = cf.generateCertificate(new ByteArrayInputStream(cert));
					trustCertsX509[i++] = (X509Certificate) xCert;
					;
				}
			}
			sdkConfigDcem.setTrustCertsX509(trustCertsX509);
			sdkConfigDcem.setTrustCertsBytes(trustCertsBytes);
		} catch (Exception exp) {
			throw new KaraException(KaraErrorCodes.GENERIC, "parsing the trustStore", exp);
		}
		SdkConfig sdkConfig = new SdkConfig();
		try {
			ThriftUtils.deserializeObject(sdkConfigContent, sdkConfig, true);
			sdkConfigDcem.setSdkConfig(sdkConfig);
			sdkConfigDcem.setConnectionKey(SecureUtils.decryptDataCommon(sdkConfig.getConnectionKey()));
		} catch (Exception e) {
			throw new KaraException(KaraErrorCodes.SDKCONFIG_READ_ERROR, e.getMessage());
		}
		return sdkConfigDcem;
	}

	public static String versionToString(AppVersion appVersion) {
		// String appName = appVersion.name;
		int major = appVersion.version >> (ProductVersion.VERSION_BITS * 2);
		int minor = (appVersion.version >> (ProductVersion.VERSION_BITS)) & ProductVersion.VERSION_MINOR_ABS;
		int service = appVersion.version & ProductVersion.VERSION_RV_ABS;

		StringBuffer sb = new StringBuffer();
		sb.append(major);
		sb.append(ProductVersion.VERSION_DELIMITER);
		sb.append(minor);
		sb.append(ProductVersion.VERSION_DELIMITER);
		sb.append(service);
		if (appVersion.state != null && appVersion.state.isEmpty() == false) {
			sb.append('-');
			sb.append(appVersion.state);
		}
		return sb.toString();

	}

	public static Map<String, String> parseCommandLine(String[] args) {
		Map<String, String> argsMap = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("--")) {
				String arg = args[i].substring(2);
				int ind = arg.indexOf('=');
				if (ind != -1) {
					argsMap.put(arg.substring(0, ind), arg.substring(ind + 1, arg.length()));
				}
			}
		}
		return argsMap;
	}

	public static boolean isLocalPortInUse(int port) {
		try {
			// ServerSocket try to open a LOCAL port
			new ServerSocket(port).close();
			// local port can be opened, it's available
			return false;
		} catch (IOException e) {
			// local port cannot be opened, it's in use
			return true;
		}
	}

	public static int skipWhiteSpaces(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (Character.isWhitespace(string.charAt(i)) == false) {
				return i;
			}
		}
		return -1;
	}

	public static int nextWhiteSpace(String string) {
		for (int i = 0; i < string.length(); i++) {
			if (Character.isWhitespace(string.charAt(i)) == true) {
				return i;
			}
		}
		return -1;
	}

	static public String getComputerName() {
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else {
			try {
				return InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				String nodeName = RandomUtils.generateRandomAlphaNumericString(8);
				return nodeName;
			}
		}

	}

	public static boolean isEmailValid(String email) {
		String regex = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$";
		return email.matches(regex);
	}

}
