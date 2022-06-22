package com.doubleclue.dcem.core.utils;

import com.doubleclue.dcem.core.exceptions.DcemException;

public interface IpRange {

	public boolean isInRange(String ip) throws DcemException;
	public int getIpVersion();
}
