package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.subjects.TitleBarSubject;
import com.doubleclue.dcem.admin.subjects.WelcomeSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("titleBarView")
@SessionScoped
public class TitleBarView extends DcemView {

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	WelcomeSubject welcomeSubject;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	TitleBarSubject titleBarSubject;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	ViewNavigator viewNavigator;


	@Inject
	UserDialogBean userDialog;
	
	@Inject
	UserPasswordDialog userPasswordDialog;
	
	@Inject
	UserLogic userLogic;

	private ResourceBundle resourceBundle;
	boolean changePasswordPermission;
	boolean editProfilePermission;
		
	@PostConstruct
	private void init() {
		subject = titleBarSubject;
						
		resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		editProfilePermission = addAutoViewAction(DcemConstants.ACTION_USER_PROFILE, resourceBundle, userDialog, DcemConstants.USER_PROFILE_DIALOG);
		changePasswordPermission = addAutoViewAction(DcemConstants.ACTION_CHANGE_PASSWORD, resourceBundle, userPasswordDialog, DcemConstants.CHANGE_PASSWORD_DIALOG);		
	}
	
	@Override
	public void triggerAction(AutoViewAction autoViewAction) {
		super.triggerAction(autoViewAction);
	}

	public boolean isPrivilegedForDeletingAlerts() {
		return operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_DELETE));
	}
	
	public void leavingView() {
	}

	@Override
	public void reload() {
		super.reload();
	}
	
	public void editProfile () {
		userDialog.setParentView(viewNavigator.getActiveView());
		List<Object> selectedList = new ArrayList<Object>();
		DcemUser dcemUser = userLogic.getUser(operatorSessionBean.getDcemUser().getId());
		selectedList.add(dcemUser);
		autoViewBean.setSelectedItems(selectedList);
		viewNavigator.setActiveDialog(this.getAutoViewAction(DcemConstants.ACTION_USER_PROFILE));
	}
	
	public void updatePassword () {
		userDialog.setParentView(viewNavigator.getActiveView());
		List<Object> selectedList = new ArrayList<Object>();
		DcemUser dcemUser = userLogic.getUser(operatorSessionBean.getDcemUser().getId());
		selectedList.add(dcemUser);
		autoViewBean.setSelectedItems(selectedList);
		viewNavigator.setActiveDialog(this.getAutoViewAction(DcemConstants.ACTION_CHANGE_PASSWORD));
	}
	
	public String getWelcomeText() {
		return JsfUtils.getMessageFromBundle(AdminModule.RESOURCE_NAME, "view.Welcome.Text", operatorSessionBean.getDcemUser().getDisplayNameOrLoginId(),
				operatorSessionBean.getRolesText(), DcemCluster.getInstance().getDcemNode().getName());
	}
	
	public boolean isChangePasswordPermission() {
		return changePasswordPermission;
	}
	public void setChangePasswordPermission(boolean changePasswordPermission) {
		this.changePasswordPermission = changePasswordPermission;
	}
	public boolean isEditProfilePermission() {
		return editProfilePermission;
	}
	public void setEditProfilePermission(boolean editProfilePermission) {
		this.editProfilePermission = editProfilePermission;
	}
}
