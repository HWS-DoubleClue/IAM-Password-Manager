package com.doubleclue.dcem.radius.logic;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;

public class ProcessRadiusTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(ProcessRadiusTask.class);
	DatagramPacket inPacket;
	DatagramSocket authSocket;
	Map<String, Object> sessionStorage;

	public ProcessRadiusTask(DatagramPacket inPacket, DatagramSocket authSocket, Map<String, Object> sessionStorage) {
		super (ProcessRadiusTask.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
		this.inPacket = inPacket;
		this.authSocket = authSocket;
		this.sessionStorage = sessionStorage;
	}

	@Override
	public void runTask() {

		WeldSessionContext sessionContext = null;
		WeldRequestContext requestContext = null;		
		try {
			sessionContext = WeldContextUtils.activateSessionContext(sessionStorage);
			requestContext = WeldContextUtils.activateRequestContext();
			ProcessRadiusPacket processRadiusPacket = CdiUtils.getReference(ProcessRadiusPacket.class);
			processRadiusPacket.process(inPacket, authSocket);
		} catch (Exception e) {
			logger.warn("", e);

		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
			WeldContextUtils.deactivateSessionContext(sessionContext);
		}
	}	

}
