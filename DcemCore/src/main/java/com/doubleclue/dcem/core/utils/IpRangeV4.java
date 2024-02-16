package com.doubleclue.dcem.core.utils;

import java.io.Serializable;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;

public class IpRangeV4 implements IpRange, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long min;
	private long max;

	public IpRangeV4(String range) throws DcemException {
		String[] parts = range.split("-");
		if (parts.length == 2) {
			min = parseIpString(parts[0]);
			max = parseIpString(parts[1]);
			if (min > max) {
				throw new DcemException(DcemErrorCodes.INVALID_IP_RANGE,
						"Invalid range: " + "min: " + min + ", " + "max: " + max);
			}
		} else if (parts.length == 1) {
			min = parseIpString(range);
			max = min;
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "Invalid format: " + range);
		}
	}

	public static long parseIpString(String ip) throws DcemException {

		if (ip.contains(".")) {
			String[] ipAddressInArray = ip.split("[.]");

			int[] intArray = new int[ipAddressInArray.length];

			long result = 0;

			for (int i = 0; i < ipAddressInArray.length; i++) {
				String numberAsString = ipAddressInArray[i];
				intArray[i] = Integer.parseInt(numberAsString);
				int power = (ipAddressInArray.length - 1) - i;
				result += intArray[i] * Math.pow(256, power);
			}

			return result;
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "String " + ip + " does not contain .");
		}
	}

	public boolean isInRange(String ip) throws DcemException {
		long ipAddress = parseIpString(ip);
		return ipAddress >= min && ipAddress <= max;
	}

	@Override
	public int getIpVersion() {
		return 4;
	}
}
