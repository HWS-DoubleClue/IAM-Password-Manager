package com.doubleclue.dcem.core.servlets;

import java.io.IOException;
import java.net.URL;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.logic.AzureAdConfig;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.utils.AzureUtils;
import com.doubleclue.oauth.oauth2.OAuthAuthCodeResponse;
import com.doubleclue.oauth.oauth2.OAuthErrorResponse;
import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;
import com.microsoft.aad.adal4j.AuthenticationResult;

@SuppressWarnings("serial")
@SessionScoped
public class AzureCallbackServlet extends HttpServlet {

	@Inject
	DomainLogic domainLogic;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}

	private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			OAuthRequest request = new OAuthRequest(req);
			if (request.hasParam(OAuthParam.AUTH_CODE)) {
				OAuthAuthCodeResponse acResponse = new OAuthAuthCodeResponse(request);
				String authCode = acResponse.getAuthCode();
				int domainId = Integer.parseInt(acResponse.getState());
				DomainEntity domain = domainLogic.getDomainEntityById(domainId);
				if (domain != null) {
					URL url = new URL(req.getRequestURL().toString());
					AuthenticationResult authResult = AzureUtils.getAuthResultByAuthCode(url.getHost(), authCode, domain);
					//domain.setAzureAdConfig(new AzureAdConfig(authResult));
					domainLogic.addOrUpdateDcemLdap(domain, AzureUtils.getEditAction());
					resp.sendRedirect("mgt/index.xhtml");
				} else {
					throw new ServletException("Domain with id " + domainId + " not found.");
				}
			} else if (request.hasParam(OAuthParam.ERROR)) {
				OAuthErrorResponse errorResponse = new OAuthErrorResponse(request);
				throw new ServletException(errorResponse.getErrorDescription());
			}
		} catch (IOException e) {
			throw e;
		} catch (ServletException e) {
			throw e;
		} catch (Exception e) {
			throw new ServletException(e.getMessage());
		}
	}
}
