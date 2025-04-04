package com.doubleclue.dcem.core.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.gui.EndMessageView;
import com.doubleclue.dcem.admin.gui.ForgotPasswordView;
import com.doubleclue.dcem.admin.gui.RegisterView;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;


@WebServlet(name = "verificationServlet")
@RequestScoped
public class VerificationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Inject
	private DcemApplicationBean applicationBean;

	@Inject
	private UserLogic userLogic;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	ForgotPasswordView forgotPasswordView;
	
	@Inject
	RegisterView registerView;

	@Inject
	private EndMessageView forgotPasswordErrorView;


	private static final Logger logger = LogManager.getLogger(VerificationServlet.class);

	// private AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	@Override
	public void init() throws ServletException {

	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("DCUP - Received Request via GET: " + request.getQueryString());
		}
		UrlTokenType tokenType = null;
		ResourceBundle resourceBundle = ResourceBundle.getBundle(AdminModule.RESOURCE_NAME);
		try {
			tokenType = UrlTokenType.valueOf(request.getParameter("type"));
			if (tokenType == null) {
				logger.error("TokenType is not valid");
				redirectToErrorPage(request, response, "Token Type", "TokenType is not valid");
				return;
			}
			TenantEntity tenantEntity = applicationBean.getTenantFromRequest(request);
			TenantIdResolver.setCurrentTenant(tenantEntity);
			if (tokenType.equals(UrlTokenType.ShowFile)) {
				
			} else {
				String token = request.getParameter("token");
				if (token == null) {
					throw new Exception("no token received");
				}
				UrlTokenEntity urlTokenEntity = urlTokenLogic.verifyUrlToken(token, tokenType.name());
				DcemUser dcemUser;
				if (tokenType != null) {
					switch (tokenType) {
					case ResetPassword:
						dcemUser = userLogic.getUser(Integer.valueOf(urlTokenEntity.getObjectIdentifier()));
						forgotPasswordView.setDcemUser(dcemUser);
						respondHttpRequest(true, request, response);
						break;
					case VerifyEmail:
						dcemUser = userLogic.getUser(Integer.valueOf(urlTokenEntity.getObjectIdentifier()));
						userLogic.enableUserWoAuditing(dcemUser);
						registerView.setDcemUser(dcemUser);
						response.sendRedirect(request.getContextPath() + DcemConstants.WEB_MGT_CONTEXT + "/" + DcemConstants.JSF_NOTIFICATION_PAGE);
						break;
					default:
						break;
					}
				}
			}
		} catch (DcemException e) {
			String errorMessage = e.getLocalizedMessage();
			switch (tokenType) {
			case ResetPassword:
				redirectToErrorPage(request, response, resourceBundle.getString("title.forgotPassword"), errorMessage);
				break;
			case VerifyEmail:
				redirectToErrorPage(request, response, resourceBundle.getString("title.verifyEmail"), errorMessage);
				break;
//			case ShowFile:
//				redirectToErrorPage(request, response, resourceBundle.getString("title.showFileInBroswer"), errorMessage);
//				break;
			default:
				break;
			}
		} catch (Exception exp) {
			logger.warn("Something went wrong URL-Token ", exp);
			String error = resourceBundle.getString("error.SOMETHING_WENT_WRONG");
			if (tokenType == null) {
				redirectToErrorPage(request, response, "", error);
			} else {
				switch (tokenType) {
				case ResetPassword:
					redirectToErrorPage(request, response, resourceBundle.getString("title.forgotPassword"), error);
					break;
				case VerifyEmail:
					redirectToErrorPage(request, response, resourceBundle.getString("title.verifyEmail"), error);
					break;
//				case ShowFile:
//					redirectToErrorPage(request, response, resourceBundle.getString("title.showFileInBroswer"), error);
//					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("DCUP - Received Request via POST: " + request.getParameterMap().toString());
		}
		respondHttpRequest(false, request, response);
	}

	private void respondHttpRequest(boolean isGet, HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {

			// request.getSession().setMaxInactiveInterval(samlModule.getModulePreferences().getSessionIdleTimeout()
			// * 60);
			/*
			 * String tenantName = request.getParameter(DcemConstants.URL_TENANT_PARAMETER);
			 * if (tenantName != null) { TenantEntity tenantEntity =
			 * applicationBean.getTenant(tenantName); if (tenantEntity == null) {
			 * redirectToErrorPage(response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
			 * "Invalid Tenant Name", null, SamlErrorCodes.UNSUPPORTED_ENCODING, null); }
			 * request.getSession().setAttribute(DcemConstants.URL_TENANT_PARAMETER,
			 * tenantEntity); TenantIdResolver.setCurrentTenant(tenantEntity); }
			 */
			request.setCharacterEncoding("UTF-8");
			redirectToPasswordResetPage(request, response);
		} catch (UnsupportedEncodingException e) {
			logger.error("CharacterEncoding is not valied", e);
			ResourceBundle resourceBundle = ResourceBundle.getBundle(AdminModule.RESOURCE_NAME);
			String error = resourceBundle.getString("error.SOMETHING_WENT_WRONG");
			redirectToErrorPage(request, response, resourceBundle.getString("title.forgotPassword"), error);
		}
	}

	private void redirectToPasswordResetPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		logger.trace("DCUP - Proceeding to reset password screen.");
		response.sendRedirect(request.getContextPath() + DcemConstants.WEB_MGT_CONTEXT + "/" + DcemConstants.JSF_PAGE_FORGOT_PASSWORD);
	}

	private void redirectToErrorPage(HttpServletRequest request, HttpServletResponse response, String title, String errorMessage) throws IOException {
		forgotPasswordErrorView.setTitle(title);
		forgotPasswordErrorView.setMessage(errorMessage);
		forgotPasswordErrorView.setError(true);
		response.sendRedirect(request.getContextPath() + DcemConstants.WEB_MGT_CONTEXT + "/" + DcemConstants.JSF_PAGE_END_MESSAGE);
	}
}
