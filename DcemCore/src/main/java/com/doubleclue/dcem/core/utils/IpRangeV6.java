package com.doubleclue.dcem.core.utils;
import java.io.Serializable;
import java.math.BigInteger;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;

public class IpRangeV6 implements IpRange, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private BigInteger min;
	private BigInteger max;
	
	public IpRangeV6(String range) throws DcemException {
		String[] parts = range.split("-");
		if (parts.length == 2) {
			min = parseIpString(parts[0]);
			max = parseIpString(parts[1]);
			if (min.compareTo(max) > 0) {
				throw new DcemException(DcemErrorCodes.INVALID_IP_RANGE, "Invalid range: " + "min: " + min + ", " + "max: " + max);
			}
		} else if (parts.length == 1) {
			min = parseIpString(range);
			max = min;
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "Invalid format: " + range);
		}
	}
	
	public static BigInteger parseIpString(String addr) throws DcemException {

		addr = IpUtils.trimIpString(addr);
		if (addr.contains("::")) {
			int startIndex = addr.indexOf("::");

			if (startIndex != -1) {
				String firstStr = addr.substring(0, startIndex);
				String secondStr = addr.substring(startIndex + 2, addr.length());
				BigInteger first = firstStr.isEmpty() ? new BigInteger("0") :  parseIpString(firstStr);
				
				int x = IpUtils.countChar(addr, ':');
				first = first.shiftLeft(16 * (7 - x)).add(parseIpString(secondStr));
				return first;
			}
		} else {
			String[] strArr = addr.split(":");

			BigInteger retValue = BigInteger.valueOf(0);
			for (int i = 0; i < strArr.length; i++) {
				BigInteger bi = new BigInteger(strArr[i], 16);
				retValue = retValue.shiftLeft(16).add(bi);
			}
			return retValue;
		}
		throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "String " + addr + " does not contain .");
	}
	
	public boolean isInRange(String ip) throws DcemException {
		BigInteger ipAddress = parseIpString(ip);
		int compWithMin = ipAddress.compareTo(min);
		int compWithMax = ipAddress.compareTo(max);
		return compWithMin > -1 && compWithMax < 1;
	}

	@Override
	public int getIpVersion() {
		return 6;
	}
}
