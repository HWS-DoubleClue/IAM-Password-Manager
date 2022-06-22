package com.doubleclue.dcem.as.tasks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AuthAppMessageResponse;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.logic.AuthAppSession;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class SendAuthMessageResponseTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(SendAuthMessageResponseTask.class);

	AuthAppSession authAppSession;
	AuthAppMessageResponse appMessageResponse;
	PendingMsg pendingMsg;

	public SendAuthMessageResponseTask(AuthAppSession authAppSession, AuthAppMessageResponse appMessageResponse, PendingMsg pendingMsg) {
		super (SendAuthMessageResponseTask.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
		this.authAppSession = authAppSession;
		this.appMessageResponse = appMessageResponse;
		this.pendingMsg = pendingMsg;
	}

	@Override
	public void runTask() {
		try {
			authAppSession.getAppSession().getServerToApp().authAppMessageResponse(appMessageResponse);
		} catch (Exception e) {
			logger.error("Couldn't Send AuthAppMessageResponse", e);
			pendingMsg.setInfo("Couldn't Send AuthAppMessageResponse. " + e.toString());
			DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			DcemReporting asReporting = new DcemReporting(getAppName(authAppSession.getPolicyAppEntity()), ReportAction.Authenticate_push, authAppSession.getDcemUserDummy(), e.getMessage(), null,
					"From: " + authAppSession.getWorkStation(),AlertSeverity.FAILURE);
			reportingLogic.addReporting(asReporting);
		}

		authAppSession.getAppSession().setPendingMsgId(-1);
	}

	private String getAppName(PolicyAppEntity appEntity) {
		return appEntity.getSubName() != null ? appEntity.getSubName() : appEntity.getAuthApplication().name();
	}
}
