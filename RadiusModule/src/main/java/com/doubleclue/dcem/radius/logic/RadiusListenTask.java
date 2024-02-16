package com.doubleclue.dcem.radius.logic;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldSessionContext;

public class RadiusListenTask implements Runnable {

	private static final Logger logger = LogManager.getLogger(RadiusListenTask.class);

	int listenPort;

	private InetAddress listenAddress = null;
	DatagramSocket authSocket;
	DcemUser operator;

	TaskExecutor taskExecutor;

	public RadiusListenTask(int listenPort, InetAddress listenAddress,  DcemUser operator) {
		super();
		this.listenPort = listenPort;
		this.operator = operator;
		taskExecutor = CdiUtils.getReference(TaskExecutor.class);
	}

	@Override
	public void run() {
		DatagramPacket inPacket;
		Thread.currentThread().setName(this.getClass().getSimpleName());
		try {
			if (listenAddress == null) {
				authSocket = new DatagramSocket(listenPort);
			} else {
				authSocket = new DatagramSocket(listenPort, listenAddress);
			}

		} catch (SocketException exp) {
			logger.error(exp);
			return;
		}
		WeldSessionContext sessionContext = null;
		try {
			sessionContext = WeldContextUtils.activateSessionContext(null);
			OperatorSessionBean operatorSessionBean = CdiUtils.getReference(OperatorSessionBean.class);
			operatorSessionBean.setDcemUser(operator);
			while (true) {
				inPacket = new DatagramPacket(new byte[RadiusPacket.MAX_PACKET_LENGTH], RadiusPacket.MAX_PACKET_LENGTH);
				try {
					authSocket.receive(inPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.warn(e);
					continue;
				}
				taskExecutor.execute(new ProcessRadiusTask(inPacket, authSocket, sessionContext.getSessionDataStore()));
			}
			

		} catch (Exception e) {
			logger.debug(e);
		} finally {
			try {
				WeldContextUtils.deactivateSessionContext(sessionContext);
			} catch (Exception exp) {
				logger.debug(exp);
			}
		}

	}

}
