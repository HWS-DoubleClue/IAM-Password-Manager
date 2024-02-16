package com.doubleclue.dcem.core.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;


public class IpRanges implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<IpRange> ranges = new ArrayList<>();

	public IpRanges(String rangesString) throws DcemException {

		String stringVal = rangesString.replaceAll("\\s", "");
		String[] parts = stringVal.split(";");

		for (String part : parts) {
			int index = part.indexOf("-");
			int ipVersion = index == -1 ? IpUtils.getIpVersion(part) : IpUtils.getIpVersion(part.substring(0, index));
			switch (ipVersion) {
			case 4:
				ranges.add(new IpRangeV4(part));
				break;
			case 6:
				ranges.add(new IpRangeV6(part));
				break;
			default:
				throw new DcemException(DcemErrorCodes.INVALID_IP_FORMAT, "Unrecognised IP: " + ipVersion);
			}
		}
	}

	public boolean isInRange(String ip) throws DcemException {

		if (ip == null) {
			return false;
		}

		int ipVersion = IpUtils.getIpVersion(ip);
		boolean isInRange = false;

		for (IpRange range : ranges) {
			if (ipVersion == range.getIpVersion() && range.isInRange(ip)) {
				isInRange = true;
				break;
			}
		}
		return isInRange;
	}
}
