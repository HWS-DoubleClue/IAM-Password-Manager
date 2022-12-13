package com.doubleclue.dcup.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.DcupConstants;

@Named("forgotPasswordView")
@SessionScoped
public class ForgotPasswordView extends AbstractPortalView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(ForgotPasswordView.class);

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	UserPortalModule userPortalModule;

	@Inject
	UserLogic userLogic;

	@Inject
	EndMessageView endMessageView;

	private String passwordNew;

	private String username;

	private String email;

	@PostConstruct
	public void init() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String getName() {
		return "forgotPasswordView";
	}

	@Override
	public String getPath() {
		return "forgotPasswordView.xhtml";
	}

	public String actionRequestPassword() {
		try {
			if (username == null || username.isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_USERNAME"));
				return null;
			}
			if (email == null || email.isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_EMAIL"));
				return null;
			}
			DcemUser dcemUser = userLogic.getUser(username.trim());
			portalSessionBean.setDcemUser(dcemUser);
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, username);
			}
			if (dcemUser.getEmail() == null || dcemUser.getEmail().equalsIgnoreCase(email) == false) {
				if (dcemUser.getPrivateEmail() == null || dcemUser.getPrivateEmail().equalsIgnoreCase(email) == false) {
					JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_EMAIL"));
					return null;
				}
			}
			UrlTokenEntity entity = urlTokenLogic.addUrlTokenToDb(UrlTokenType.ResetPassword,
					userPortalModule.getModulePreferences().getUrlTokenTimeout(), null, dcemUser.getId().toString());
			urlTokenLogic.sendUrlTokenByEmail(dcemUser, userPortalModule.getServletUrl(), entity);
			endMessageView.setError(false);
			endMessageView.setMessage(portalSessionBean.getResourceBundle().getString("info.passwordRequestSuccess"));
			endMessageView.setTitle(portalSessionBean.getResourceBundle().getString("title.forgotPassword"));
			username = null;
			email = null;
			return DcupConstants.JSF_PAGE_END_MESSAGE + DcemConstants.FACES_REDIRECT;

		} catch (DcemException exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exp));
		} catch (Exception exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage(exp.getMessage());
		}
		return null;
	}

	public String actionResetPassword() {
		try {
			DcemUser dcemUser = portalSessionBean.getDcemUser();
			if (dcemUser == null) {
				throw new Exception("");
			}
			if (validateInput()) {
				userLogic.setPassword(dcemUser, passwordNew);
				endMessageView.setError(false);
				endMessageView.setMessage(portalSessionBean.getResourceBundle().getString("info.passwordResetSuccess"));
				endMessageView.setTitle(portalSessionBean.getResourceBundle().getString("title.forgotPassword"));
				return DcupConstants.JSF_PAGE_END_MESSAGE + DcemConstants.FACES_REDIRECT;
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
		return null;
	}

	public String getPasswordNew() {
		return passwordNew;
	}

	public void setPasswordNew(String passwordNew) {
		this.passwordNew = passwordNew;
	}

	private boolean validateInput() {
		boolean valid = true;
		if (passwordNew.isEmpty()) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.NEW_PASSWORD_MISSING"));
			valid = false;
		}
		return valid;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}