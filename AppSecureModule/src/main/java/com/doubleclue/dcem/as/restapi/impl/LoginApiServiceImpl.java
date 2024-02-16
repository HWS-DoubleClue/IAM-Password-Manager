package com.doubleclue.dcem.as.restapi.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.restapi.model.AsApiAuthMethod;
import com.doubleclue.dcem.as.restapi.model.AsApiAuthenticateResponse;
import com.doubleclue.dcem.as.restapi.model.RequestLoginQrCodeResponse;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.as.QueryLoginResponse;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.utils.StringUtils;

public class LoginApiServiceImpl {

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	AppServices appServices;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	PolicyLogic policyLogic;

	private static Logger logger = LogManager.getLogger(LoginApiServiceImpl.class);

	public Response queryLoginQrCode(String sessionId, boolean pollOnly, int waitTimeSeconds, SecurityContext securityContext) throws DcemApiException {
		QueryLoginResponse queryLoginResponse;
		try {
			queryLoginResponse = appServices.queryLoginQrCode(Integer.toString(operatorSessionBean.getDcemUser().getId()), sessionId, pollOnly,
					waitTimeSeconds);
		} catch (DcemException exp) {
			logger.debug("REST- addMessage: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().entity(queryLoginResponse).build();
	}

	public Response requestLoginQrCode(String sessionId, SecurityContext securityContext) throws DcemApiException {
		RequestLoginQrCodeResponse qrCodeResponse;
		try {
			qrCodeResponse = appServices.generateLoginQrCode(Integer.toString(operatorSessionBean.getDcemUser().getId()), sessionId);
		} catch (DcemException exp) {
			logger.warn("REST- requestLoginQrCode: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().entity(qrCodeResponse).build();
	}

	public Response queryLoginOTP(String otp, SecurityContext securityContext) throws DcemApiException {

		try {
			QueryLoginResponse queryLoginResponse = appServices.queryLoginOtp(otp);
			return Response.ok().entity(queryLoginResponse).build();

		} catch (DcemException exp) {
			logger.warn("REST- addMessage: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response authenticate(String userLoginId, String authMethodName, String password, String passcode, String networkAddress, String fingerPrint,
			boolean ignorePassword, String fidoResponse, String rpId, SecurityContext securityContext) throws DcemApiException {

		try {
			AuthMethod authMethod = null;
			if (authMethodName != null && authMethodName.isEmpty() == false) {
				authMethod = AuthMethod.valueOf(authMethodName);
				if (authMethod == null) {
					throw new DcemException(DcemErrorCodes.INVALID_AUTH_METHOD, authMethodName);
				}
			}
			AuthRequestParam requestParam = new AuthRequestParam();
			requestParam.setNetworkAddress(networkAddress);
			requestParam.setIgnorePassword(ignorePassword);
			requestParam.setFidoResponse(fidoResponse);
			requestParam.setFidoRpId(rpId);
			AuthenticateResponse authenticateResponse = authenticationLogic.authenticate(AuthApplication.WebServices,
					operatorSessionBean.getDcemUser().getId(), userLoginId, authMethod, password, passcode, requestParam);
			AsApiAuthenticateResponse apiAuthenticateResponse = new AsApiAuthenticateResponse(authenticateResponse);
			return Response.ok().entity(apiAuthenticateResponse).build();

		} catch (DcemException exp) {
			logger.debug("REST- authenticate: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		} 
	}

	public Response getAuthenticateMethods(SecurityContext securityContext) {
		try {
			List<AuthMethod> methods = policyLogic.getAuthMethods(AuthApplication.WebServices, operatorSessionBean.getDcemUser().getId(), null);
			List<AsApiAuthMethod> apiAuthMethods = new ArrayList<>(methods.size());
			for (AuthMethod authMethod : methods) {
				apiAuthMethods.add(AsApiAuthMethod.fromValue(authMethod.name()));
			}
			return Response.ok().entity(apiAuthMethods).build();
		} catch (DcemException exp) {
			logger.debug("REST- addMessage: " + exp.toString(), exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

}
