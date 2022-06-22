package com.doubleclue.dcem.as.logic;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AuthAppMessageResponse;
import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.comm.ConnectionState;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.as.tasks.SendAuthMessageResponseTask;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.utils.RandomUtils;

@ApplicationScoped
public class AsAuthGatewayLogic {

	private static Logger logger = LogManager.getLogger(AsAuthGatewayLogic.class);

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	EntityManager em;

	@Inject
	AsModule asModule;

	@Inject
	JpaLogic jpaLogic;
	
	@Inject
	TaskExecutor taskExecutor;
	
	@Inject
	AsMessageHandler messageHandler; 
	
	@Inject
	UserLogic userLogic;
	
	@Inject
	AsMessageHandler asMessageHandler;
	
	
	public void onMsgResponseReceived(@Observes PendingMsg pendingMsg) {
		if (pendingMsg.getPolicyTransaction().getPolicyAppEntity().getAuthApplication() != AuthApplication.AuthGateway) {
			return;
		}
		try {
			AsApiMessageResponse apiMessageResponse = asMessageHandler.retrieveMessageResponse(pendingMsg.getId(), 0);
			onAuthGatewayMsgResponseReceived(pendingMsg, apiMessageResponse.getSessionCookieExpiresOn(), apiMessageResponse.getSessionCookie());
		} catch (DcemException e1) {
			logger.error("AuthGatewayServices-onMsgResponseReceived", e1);
		}
	}
	
	

	@DcemTransactional
	public void addOrUpdateAuthApp (AuthGatewayEntity authAppEntity, DcemAction dcemAction)
			throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			authAppEntity.setId(null);
			authAppEntity.setSharedKey(RandomUtils.getRandom(32));
			em.persist(authAppEntity);
		} else {			
			em.merge(authAppEntity);
		}
		auditingLogic.addAudit(dcemAction, authAppEntity.toString());
	}
	
	/**
	 * 
	 * @param pendingMsg
	 */
	public void onAuthGatewayMsgResponseReceived(PendingMsg pendingMsg, int sessionCookieExpiresOn, String sessionCookie) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		
		if (pendingMsg.getPolicyTransaction().getPolicyAppEntity().getAuthApplication() != AuthApplication.AuthGateway) {
			return;
		}
		AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(pendingMsg.getSessionId());
		AppSession appSession = authAppSession.getAppSession();
		if (authAppSession == null || appSession == null) {
			logger.info("MsgResponse received but AuthAppSession is not there");
			messageHandler.onFinalMessage(pendingMsg, false);
			return;
		}
		AuthAppMessageResponse appMessageResponse = new AuthAppMessageResponse();
		appMessageResponse.setMsgId(pendingMsg.getId());
		if (pendingMsg.getMsgStatus() == AsApiMsgStatus.OK) {
			if (pendingMsg.getActionId().equals("ok")) {
				appMessageResponse.setSuccesful(true);
				appMessageResponse.setSessionCookie(sessionCookie);
				appMessageResponse.setSessionCookieExpiresOn(sessionCookieExpiresOn);
				
				appSession.setState(ConnectionState.authenticated);
				appSession.setUserId(pendingMsg.getUserId());
				DcemUser dcemUser = userLogic.getUser(pendingMsg.getUserId());
				byte[] key;
				key = dcemUser.getSalt();
				if (key == null) {
					key = RandomUtils.getRandom(32);
					dcemUser.setSalt(key);
					userLogic.setUserSalt(dcemUser);
				} 
				appMessageResponse.setUserKey(key);
			} else {
				appMessageResponse.setSuccesful(false);
				appMessageResponse.setErrorCode(DcemConstants.REPORT_ERROR_CODE_DENY);
			}
		} else {
			appMessageResponse.setSuccesful(false);
			appMessageResponse.setErrorCode(pendingMsg.getMsgStatus().name());
			appMessageResponse.setErrorMessage(pendingMsg.getInfo());
		}
		taskExecutor.schedule(new SendAuthMessageResponseTask(authAppSession, appMessageResponse, pendingMsg), 50, TimeUnit.MICROSECONDS);
	}
	
//	@DcemTransactional
//	public void addAuthGatewayReport (AuthAppReportingEntity appReportingEntity, PendingMsg pendingMsg) {
//		appReportingEntity.setTimestamp(new Date (pendingMsg.getTimeStamp()));
//		if (pendingMsg.getMsgStatus() == AsApiMsgStatus.OK && pendingMsg.getActionId().equals("ok")) {
//			appReportingEntity.setResult(true);
//		}
//		em.persist(appReportingEntity);
//	}

	@DcemTransactional
//	public void addAuthGatewayReport(AuthAppReportingEntity appReportingEntity) {
//		appReportingEntity.setTimestamp(new Date());
//		em.persist(appReportingEntity);
//	}

	public AuthGatewayEntity getAuthAppEntitiy(String authAppId) {
		TypedQuery<AuthGatewayEntity> query = em.createNamedQuery(AuthGatewayEntity.GET_GATEWAY, AuthGatewayEntity.class);
		query.setParameter(1, authAppId);
		query.setParameter(2, false);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}
	
	public List<AuthGatewayEntity> getAllAuthGateway () {
		TypedQuery<AuthGatewayEntity> query = em.createNamedQuery(AuthGatewayEntity.GET_ALL_AUTHGATEWAYS, AuthGatewayEntity.class);
		return query.getResultList();
	}

}
