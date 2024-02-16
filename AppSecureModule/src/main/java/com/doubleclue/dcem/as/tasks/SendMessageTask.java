package com.doubleclue.dcem.as.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.AppMessage;
import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;


public class SendMessageTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(SendMessageTask.class);

	AppSession appSession;
	PendingMsg pendingMsg;
	AppMessage appMessage;
	
	public SendMessageTask(AppSession appSession, AppMessage appMessage, PendingMsg pendingMsg, TenantEntity tenantEntity ) {
		super (SendMessageTask.class.getSimpleName(), tenantEntity);
		this.appSession = appSession;
		this.appMessage = appMessage;
		this.pendingMsg = pendingMsg;
	}


	@Override
	public void runTask() {
		try {
			appSession.getServerToApp().sendMessage(appMessage);
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			asMessageHandler.messageSent(appSession, pendingMsg);
		} catch (AppException e) {
			logger.info("Coundn't Send message", e);
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			pendingMsg.setMsgStatus(AsApiMsgStatus.SEND_ERROR);
			pendingMsg.setInfo(e.toString());
			asMessageHandler.messageSent(appSession, pendingMsg);
		
		} catch (Exception e) {
			logger.info("Coundn't Send message", e);
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			pendingMsg.setMsgStatus(AsApiMsgStatus.SEND_ERROR);
			pendingMsg.setInfo(e.toString());
			asMessageHandler.messageSent(appSession, pendingMsg);
		}
	}

}
