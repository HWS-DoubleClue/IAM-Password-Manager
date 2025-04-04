package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;

@Named("forgotPasswordView")
@SessionScoped
public class ForgotPasswordView extends DcemView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(ForgotPasswordView.class);

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	EndMessageView endMessageView;

	@Inject
	AdminModule adminModule;

	@Inject
	DcemApplicationBean applicationBean;

	private String passwordNew;

	private String username;

	private String email;

	DcemUser dcemUser;

	ResourceBundle resourceBundle;

	@PostConstruct
	public void init() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String actionRequestPassword() {
		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle(AdminModule.RESOURCE_NAME, JsfUtils.getLocale());
		}
		try {
			if (username == null || username.isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle ,"error.MISSING_USERNAME");
				return null;
			}
			if (email == null || email.isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle, "error.MISSING_EMAIL");
				return null;
			}
			dcemUser = userLogic.getUser(username.trim());
// TODO			portalSessionBean.setDcemUser(dcemUser);
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, username);
			}
			if (dcemUser.getEmail() == null || dcemUser.getEmail().equalsIgnoreCase(email) == false) {
				if (dcemUser.getPrivateEmail() == null || dcemUser.getPrivateEmail().equalsIgnoreCase(email) == false) {
					JsfUtils.addErrorMessage(resourceBundle, "error.INVALID_EMAIL");
					return null;
				}
			}
			UrlTokenEntity entity = urlTokenLogic.addUrlTokenToDb(UrlTokenType.ResetPassword,
					adminModule.getPreferences().getUrlTokenTimeout(), null, dcemUser.getId().toString());
			
			String url = applicationBean.getDcemManagementUrl(TenantIdResolver.getCurrentTenantName()) + "/" +  DcemConstants.VERIFICATION_SERVLET_PATH + "?token=";
			urlTokenLogic.sendUrlTokenByEmail(dcemUser, url, entity);
			endMessageView.setError(false);
			endMessageView.setMessage(resourceBundle.getString("info.passwordRequestSuccess"));
			endMessageView.setTitle(resourceBundle.getString("title.forgotPassword"));
			username = null;
			email = null;
			return DcemConstants.JSF_PAGE_END_MESSAGE + DcemConstants.FACES_REDIRECT;

		} catch (DcemException exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
		} catch (Exception exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage(exp.getMessage());
		}
		return null;
	}

	public String actionResetPassword() {
		if (resourceBundle == null) {
			resourceBundle = ResourceBundle.getBundle(AdminModule.RESOURCE_NAME, JsfUtils.getLocale());
		}
		try {
			if (validateInput()) {
				userLogic.setPassword(dcemUser, passwordNew);
				endMessageView.setError(false);
				endMessageView.setMessage(resourceBundle.getString("info.passwordResetSuccess"));
				endMessageView.setTitle(resourceBundle.getString("title.forgotPassword"));
				return DcemConstants.JSF_PAGE_END_MESSAGE + DcemConstants.FACES_REDIRECT;
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
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
			JsfUtils.addErrorMessage(resourceBundle, "error.NEW_PASSWORD_MISSING");
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

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}
}