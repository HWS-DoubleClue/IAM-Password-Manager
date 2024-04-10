package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.utils.RandomUtils;

@SuppressWarnings("serial")
@Named("userView")
@SessionScoped
public class UserViewBean extends DcemView {

	@Inject
	private AdminModule adminModule;

	@Inject
	private UserSubject appUserSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private UserDialogBean userDialog;

	@Inject
	private AdminActivationDialog adminActivationDialog;

	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		userDialog.setParentView(this);
		adminActivationDialog.setParentView(this);
		subject = appUserSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		ResourceBundle adminResourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, userDialog, DcemConstants.USER_VIEW_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, userDialog, DcemConstants.USER_VIEW_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, userDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_ENABLE, resourceBundle, userDialog, null);
		addAutoViewAction(DcemConstants.ACTION_DISABLE, resourceBundle, userDialog, null);
		addAutoViewAction(DcemConstants.ACTION_RESET_PASSWORD, adminResourceBundle, userDialog, DcemConstants.RESET_PASSWORD_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_MEMBER_OF, adminResourceBundle, userDialog, DcemConstants.SHOW_MEMBEROF_DIALOG);
		if (applicationBean.getModule(DcemConstants.AS_MODULE_ID) != null) {
			addAutoViewAction(DcemConstants.ACTION_RESET_STAY_LOGIN, adminResourceBundle, userDialog, null);
			addAutoViewAction(DcemConstants.CREATE_ACTIVATION_CODE, adminResourceBundle, adminActivationDialog, DcemConstants.SHOW_ACTIVATION_CODE_DIALOG);
		}
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

	public Object createActionObject() {
		if (this.subject.getKlass() == null) {
			return null;
		}
		String initialPassword;
		if (adminModule.getPreferences().isNumericPassword()) {
			initialPassword = RandomUtils.generateRandomNumberString(adminModule.getPreferences().getUserPasswordLength());
		} else {
			initialPassword = RandomUtils.generateRandomAlphaLowercaseNumericString(adminModule.getPreferences().getUserPasswordLength());
		}
		DcemUser user = new DcemUser();
		user.setInitialPassword(initialPassword);
		user.setLanguage(adminModule.getPreferences().getUserDefaultLanguage());
		actionObject = user;
		return user;
	}
}
