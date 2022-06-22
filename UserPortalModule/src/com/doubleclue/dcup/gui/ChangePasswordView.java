package com.doubleclue.dcup.gui;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.utils.StringUtils;

@SuppressWarnings("serial")
@Named("changePasswordView")
@SessionScoped
public class ChangePasswordView extends AbstractPortalView {

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	private UserLogic userLogic;

	@PostConstruct
	public void init() {
	}

	private String passwordOld = "";
	private String passwordNew;
	private String passwordRepeat;

	public void actionChangePassword() {

		try {
			if (validateInput()) {
				userLogic.changePassword(portalSessionBean.getUserName(), passwordOld, passwordNew);
				JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("info.passwordChange"));
				StringUtils.wipeString(passwordOld);
				StringUtils.wipeString(passwordNew);
				passwordOld = null;
				passwordNew = null;
				passwordRepeat = null;
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "DcemErrorCodes." + e.getErrorCode());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public String getPasswordOld() {
		return passwordOld;
	}

	public void setPasswordOld(String passwordOld) {
		this.passwordOld = passwordOld;
	}

	public String getPasswordNew() {
		return passwordNew;
	}

	public void setPasswordNew(String passwordNew) {
		this.passwordNew = passwordNew;
	}

	public String getPasswordRepeat() {
		return passwordRepeat;
	}

	public void setPasswordRepeat(String passwordRepeat) {
		this.passwordRepeat = passwordRepeat;
	}

	@Override
	public String getName() {
		return "changePasswordView";
	}

	@Override
	public String getPath() {
		return "changePasswordView.xhtml";
	}

	private boolean validateInput() {
		boolean valid = true;
		if (passwordOld.isEmpty() || passwordNew.isEmpty() || passwordRepeat.isEmpty()) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.EMPTY_FIELDS"));
			valid = false;
		} else if (passwordNew.equals(passwordOld)) {
			JsfUtils.addErrorMessage(
					portalSessionBean.getResourceBundle().getString("error.OLD_PASSWORD_IDENTIC_TO_NEW"));
			valid = false;
		} else if (!Objects.equals(passwordNew, passwordRepeat)) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.PASSWORDS_NOT_IDENTICAL"));
			valid = false;
		}
		return valid;
	}
}