package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.TitleBarSubject;
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
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("titleBarView")
@SessionScoped
public class TitleBarView extends DcemView {

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	TitleBarSubject titleBarSubject;

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
		editProfilePermission = operatorSessionBean.isPermission(new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_TITLE_BAR, DcemConstants.ACTION_USER_PROFILE) );
		changePasswordPermission = operatorSessionBean.isPermission(new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_TITLE_BAR, DcemConstants.ACTION_CHANGE_PASSWORD  ) );
	}
	
//	DcemConstants.CHANGE_PASSWORD_DIALOG
	public void leavingView() {
		System.out.println("TitleBarView.leavingView()");
	}

	@Override
	public void reload() {
		super.reload();
	}
	
	public void editProfile () {
		
//		DcemUser dcemUser = userLogic.getUser(operatorSessionBean.getDcemUser().getId());
//		List<Object> selectedList = new ArrayList<Object>();
//		selectedList.add(dcemUser);
//		autoViewBean.setSelectedItems(selectedList);
//		DcemAction dcemAction = new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_TITLE_BAR, DcemConstants.ACTION_USER_PROFILE);
//		RawAction rawAction = titleBarSubject.getRawAction(DcemConstants.ACTION_USER_PROFILE);
//		userDialog.setParentView(this);
//		AutoViewAction autoViewAction = new AutoViewAction(dcemAction, userDialog, resourceBundle, rawAction, getWelcomeText(), null);
//		viewNavigator.setActiveDialog(autoViewAction);
		try {
			userDialog.showMyProfile();
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Something when wrong: Cause:  " + e.toString());
			logger.warn("userDialog.show", e);
			return;	
		}
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		options.put("position", "top");
		options.put("headerElement", "customheader");
		options.put("position", "top");
		options.put("headerElement", "customheader");
		options.put("contentWidth", "740");
		options.put("height", "540");
		PrimeFaces.current().dialog().openDynamic(DcemConstants.WEB_MGT_CONTEXT + DcemConstants.USER_VIEW_PATH, options, null);
	}
	
	public void updatePassword () {
		DcemAction dcemAction = new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_TITLE_BAR, DcemConstants.ACTION_CHANGE_PASSWORD);
		RawAction rawAction = titleBarSubject.getRawAction(DcemConstants.ACTION_USER_PROFILE);
		AutoViewAction autoViewAction = new AutoViewAction(dcemAction, userPasswordDialog, resourceBundle, rawAction, getWelcomeText(), null);
		viewNavigator.setActiveDialog(autoViewAction);
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		options.put("position", "top");
		options.put("headerElement", "customheader");
		options.put("position", "top");
		options.put("headerElement", "customheader");
		PrimeFaces.current().dialog().openDynamic(DcemConstants.WEB_MGT_CONTEXT + DcemConstants.CHANGE_PASSWORD_DIALOG, options, null);
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
