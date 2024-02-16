package com.doubleclue.dcem.as.tasks;

import java.nio.ByteBuffer;

import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;

public class ProcessProxyReceivedData extends CoreTask {

	AuthProxyListener authProxyListener;
	ByteBuffer byteBuffer;

	public ProcessProxyReceivedData(AuthProxyListener authProxyListener, ByteBuffer byteBuffer) {
		super (ProcessProxyReceivedData.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
		this.authProxyListener = authProxyListener;
		this.byteBuffer = byteBuffer;

	}

	@Override
	public void runTask() {
		authProxyListener.onReceive(byteBuffer.array());
	}

}
