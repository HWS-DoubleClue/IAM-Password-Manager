package com.doubleclue.dcem.core.utils;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;

public class IpUtils {

	private static Pattern VALID_IPV4_PATTERN = null;
	private static Pattern VALID_IPV6_PATTERN = null;
	private static final String ipv4Pattern = "(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])";
	private static final String ipv6Pattern = "^(((?=(?>.*?::)(?!.*::)))(::)?([0-9a-fA-F]{1,4}::?){0,5}|([0-9a-fA-F]{1,4}:){6})(\\2([0-9a-fA-F]{1,4}(::?|$)){0,2}|((25[0-5]|(2[0-4]|1\\d|[1-9])?\\d)(\\.|$)){4}|[0-9a-fA-F]{1,4}:[0-9a-fA-F]{1,4})(?<![^:]:|\\.)\\z";

	static {
		try {
			VALID_IPV4_PATTERN = Pattern.compile(ipv4Pattern, Pattern.CASE_INSENSITIVE);
			VALID_IPV6_PATTERN = Pattern.compile(ipv6Pattern, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			System.out.println("Unable to compile pattern");
		}
	}

	public static int countChar(String str, char reg) {
		char[] ch = str.toCharArray();
		int count = 0;
		for (int i = 0; i < ch.length; ++i) {
			if (ch[i] == reg) {
				if (ch[i + 1] == reg) {
					++i;
					continue;
				}
				++count;
			}
		}
		return count;
	}

	public static int getIpVersion(String ipAddress) throws DcemException {
		
		ipAddress = trimIpString(ipAddress);
		if (IpUtils.VALID_IPV4_PATTERN.matcher(ipAddress).matches()) {
			return 4;
		} else if (IpUtils.VALID_IPV6_PATTERN.matcher(ipAddress).matches()) {
			return 6;
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "Invalid format: " + ipAddress);
		}
	}
	
	public static String trimIpString(String ipAddress) {
		if (ipAddress.contains("%")) {
			ipAddress.replace("%", "");
			int index = ipAddress.indexOf("%");
			ipAddress = ipAddress.substring(0, index);
		}
		return ipAddress;
	}
}
