package com.doubleclue.dcem.as.comm.client;

import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class ConnectDcemProxyTask extends CoreTask {

	public ConnectDcemProxyTask() {
		super (ConnectDcemProxyTask.class.getSimpleName());
	}

	@Override
	public void runTask() {
		Thread.currentThread().setName(this.getClass().getSimpleName());
		ProxyCommClient proxyCommClient = CdiUtils.getReference(ProxyCommClient.class);
		proxyCommClient.start();

	}

}
