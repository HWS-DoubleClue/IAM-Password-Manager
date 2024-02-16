package com.doubleclue.dcem.as.logic;

import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.DomainSdkConfigParam;
import com.doubleclue.comm.thrift.DomainSdkConfigResponse;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface DispatcherApi {
	
	public DomainSdkConfigResponse getDomainSdkConfig (DomainSdkConfigParam domainSdkConfigParam, String location) throws DcemException, AppException, ExceptionReporting;

	public RegisteredDomain getProxyDomain(String domainName);
	
	public void setOnline(int id);
	
	public String getNodeRedirectionUrl(String nodeName);
}