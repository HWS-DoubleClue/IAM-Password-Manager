package com.doubleclue.dcem.as.tasks;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

import com.doubleclue.dcem.as.comm.ReverseProxyConnections;
import com.doubleclue.dcem.as.logic.ReverseProxyStatus;

@SuppressWarnings("serial")
public class CallGetReversProxyStatusTask implements Callable<List<ReverseProxyStatus>>, Serializable {

	String proxyName = null;

	public CallGetReversProxyStatusTask() {
	}

	public CallGetReversProxyStatusTask(String proxyName) {
		super();
		this.proxyName = proxyName;
	}

	@Override
	public List<ReverseProxyStatus> call() throws Exception {
		Thread.currentThread().setName(this.getClass().getSimpleName());
		if (proxyName == null) {
			return ReverseProxyConnections.getRpStatus();
		} else {
			return ReverseProxyConnections.getRpStatus(proxyName);
		}

	}

}
