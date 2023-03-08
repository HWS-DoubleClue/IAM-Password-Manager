package com.doubleclue.dcem.admin.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;

@Named("departmentDialog")
@SessionScoped
public class DepartmentDialog extends DcemDialog {

	@Inject
	UserLogic userLogic;

	@Inject
	DepartmentLogic departmentLogic;

	@Inject
	DepartmentView departmentView;

	@Inject
	JpaLogic jpaLogic;

	String loginId;
	String deputyLoginId;
	String parentDepartment;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean actionOk() throws Exception {
		// AsActivationCode activationCode = (AsActivationCode) this.getActionObject();
		DepartmentEntity departmentEntity = (DepartmentEntity) this.getActionObject();

		DcemUser headOf = userLogic.getDistinctUser(loginId);
		if (headOf == null) {
				JsfUtils.addErrorMessage(departmentView.getResourceBundle(), "departmentDialog.invalidHeadOf");
				return false;
		}
		DcemUser deputyUser = null;
		if (deputyLoginId != null && deputyLoginId.isEmpty() == false) {
			deputyUser = userLogic.getDistinctUser(deputyLoginId);
			if (deputyUser == null) {
				JsfUtils.addErrorMessage(departmentView.getResourceBundle(), "departmentDialog.invalidDeputy");
				return false;
			}
		}
		
		DepartmentEntity parentDepartmentEntity = null;
		if (parentDepartment != null && parentDepartment.isEmpty() == false) {
			parentDepartmentEntity = departmentLogic.getDepartmentByName (parentDepartment);
			if (parentDepartmentEntity == null) {
				JsfUtils.addErrorMessage(departmentView.getResourceBundle(), "departmentDialog.invalidDepartment");
				return false;
			}
		}
		departmentEntity.setDeputy(deputyUser);
		departmentEntity.setHeadOf(headOf);
		departmentEntity.setParentDepartment(parentDepartmentEntity);
		jpaLogic.addOrUpdateEntity(departmentEntity, this.getAutoViewAction().getDcemAction());

		// JsfUtils.addInformationMessage(AsModule.RESOUCE_NAME, "activationDialog.success", activationCode.getActivationCode());
		return true;
	}

	public List<String> completeUser(String name) {
		return userLogic.getCompleteUserList(name, 50);
	}

	public List<String> completeDepartment(String name) {
		return departmentLogic.getCompleteDepartmentList(name, 50);
	}

	public String getHeight() {
		return "650";
	}

	public String getWidth() {
		return "800";
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemDialog#show(com.doubleclue.dcem.core.gui.DcemView, com.doubleclue.dcem.core.gui.AutoViewAction)
	 */
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_EDIT)) {

		}
		if (action.equals(DcemConstants.ACTION_COPY)) {

		}
		parentView = dcemView;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getParentDepartment() {
		return parentDepartment;
	}

	public void setParentDepartment(String parentDepartment) {
		this.parentDepartment = parentDepartment;
	}

	public String getDeputyLoginId() {
		return deputyLoginId;
	}

	public void setDeputyLoginId(String deputyLoginId) {
		this.deputyLoginId = deputyLoginId;
	}
}
