package com.doubleclue.dcem.as.comm.client;

import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class PingDcemProxyTask extends CoreTask {

	TyrusClientWebsocket tyrusClientWebsocket;

	public PingDcemProxyTask() {
		super (PingDcemProxyTask.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
	}

	public PingDcemProxyTask(TyrusClientWebsocket tyrusClientWebsocket) {
		super (PingDcemProxyTask.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
		this.tyrusClientWebsocket = tyrusClientWebsocket;
	}

	@Override
	public void runTask() {
		Thread.currentThread().setName(this.getClass().getSimpleName());
		try {
			tyrusClientWebsocket.sendPing();
			
		} catch (Exception e) {
			ProxyCommClient proxyCommClient = CdiUtils.getReference(ProxyCommClient.class);
			proxyCommClient.addReport(new RpReport(RpClientAction.SendPing, false, e.toString()));
			proxyCommClient.close("Server Ping Failed");
		}

	}

}
