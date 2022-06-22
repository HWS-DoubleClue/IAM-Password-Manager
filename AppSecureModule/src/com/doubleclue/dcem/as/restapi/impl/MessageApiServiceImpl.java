package com.doubleclue.dcem.as.restapi.impl;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.restapi.ApiResponseMessage;
import com.doubleclue.dcem.as.restapi.model.AddMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMessage;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;


public class MessageApiServiceImpl  {
	
	private static Logger logger = LogManager.getLogger(MessageApiServiceImpl.class);

	
	@Inject
	AsMessageHandler messageHandler;
	
	@Inject
	DcemReportingLogic reportingLogic;
	
	@Inject
	UserLogic userLogic;
	
	@Inject
	PolicyLogic policyLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;

	
    public Response addMessage(AsApiMessage apiMessage) throws DcemApiException {
        
    	AddMessageResponse  addMessageResponse;
		try {
			addMessageResponse = messageHandler.sendMessage(apiMessage, null, null, AuthApplication.WebServices, operatorSessionBean.getDcemUser().getId(), null, null);
		} catch (DcemException exp) {
			reportingLogic.addReporting(new DcemReporting(ReportAction.RestAddMessage, (DcemUser) null, AppErrorCodes.REST_ADD_MESSAGE_FAILURE, null, exp.toString()));
			if (logger.isDebugEnabled()) {
				logger.info("REST- addMessage: " + exp.toString(), exp);
			} else {
				logger.info("REST- addMessage: " + exp.toString());
			}
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
        return Response.ok().entity(addMessageResponse).build();
    }

    
    public Response getMessageResponse(Long msgId, int waitTimeSeconds, SecurityContext securityContext) throws DcemApiException {
    	AsApiMessageResponse responseMessage;
		try {
			responseMessage = messageHandler.retrieveMessageResponse(msgId, waitTimeSeconds);
			return Response.ok().entity(responseMessage).build();
		} catch (DcemException exp) {
			logger.info("REST- messageResponse failed: " +  exp.toString());
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
    	
    	
    }
    

    public Response cancelMessage(Long msgId, SecurityContext securityContext) throws DcemApiException {
 
    	try {
			messageHandler.cancelPendingMsg(msgId);
		} catch (DcemException exp) {
			logger.warn("REST- cancelMessage failed: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
 
        return Response.ok().build();
    }
 
    public Response cancelUserMessages(String loginId, SecurityContext securityContext) throws DcemApiException {
    	DcemUser dcemUser = null;
		try {
			dcemUser = userLogic.getUser(loginId);
		} catch (DcemException exp) {
			logger.warn("REST- cancelUserMessages failed: " + exp.toString(), exp);
		}

		if (dcemUser == null) {
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION)
					.entity(new DcemApiException(DcemErrorCodes.INVALID_USERID.getErrorCode(),
							DcemErrorCodes.INVALID_USERID.name(), null))
					.build();
		}
    	messageHandler.cancelUserPendingMsgs(dcemUser.getId());
    	
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    
}
