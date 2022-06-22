package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.data.FilterEvent;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.PrivilegeSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.ActionRoleAssignment;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;

@SuppressWarnings("serial")
@Named("privilegeView")
@SessionScoped
public class PrivilegeViewBean extends DcemView {

	@Inject
	private DcemApplicationBean dcemApplicationBean;

	@Inject
	ActionLogic actionLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	PrivilegeSubject privilegeSubject;

	List<String> dialogs = new ArrayList<String>();
	List<DcemAction> dcemActions;
	List<DcemRole> dcemRoles;
	Map<Integer, ActionRoleAssignment> assignmentMap; // key is the ActionId
	String moduleFilter;

	@PostConstruct
	private void init() {
		subject = privilegeSubject;
	}

	@Override
	public void reload() {
		dcemActions = actionLogic.getAllDcemActions();
		dcemRoles = roleLogic.getDcemRolesBelowRank(operatorSessionBean.getHighestRole().getRank());
		assignmentMap = new HashMap<Integer, ActionRoleAssignment>(dcemActions.size());
		for (DcemAction dcemAction : dcemActions) {
			ActionRoleAssignment ara = new ActionRoleAssignment();
			ara.setDcemAction(dcemAction);
			for (DcemRole dcemRole : dcemRoles) {
				ara.getRoleAssigned().put(dcemRole.getid(), dcemRole.getActions().contains(dcemAction));
			}
			assignmentMap.put(dcemAction.getId(), ara);
		}
		autoViewBean.reload();
	}

	public List<SelectItem> getModuleListFilter() {
		List<SelectItem> listConfirmFilter;
		listConfirmFilter = new ArrayList<SelectItem>();
		for (DcemModule module : dcemApplicationBean.getSortedModules()) {
			if (TenantIdResolver.isCurrentTenantMaster() == false && module.isMasterOnly()) {
				continue;
			}
			listConfirmFilter.add(new SelectItem(module.getId(), module.getName()));
		}
		return listConfirmFilter;
	}

	public String getModuleFilter() {
		return moduleFilter;
	}

	public void setModuleFilter(String moduleFilter) {
		this.moduleFilter = moduleFilter;
	}

	public Map<Integer, ActionRoleAssignment> getAssignment() {
		return assignmentMap;
	}

	public void setAssignment(Map<Integer, ActionRoleAssignment> assignement) {
		this.assignmentMap = assignement;
	}

	public List<DcemAction> getDcemActions() {
		if (moduleFilter == null || moduleFilter.isEmpty()) {
			return dcemActions;
		}
		List<DcemAction> retrunList = new LinkedList<DcemAction>();
		for (DcemAction dcemAction : dcemActions) {
			if (dcemAction.getModuleId().equals(moduleFilter)) {
				retrunList.add(dcemAction);
			}
		}
		return retrunList;
	}

	public void setDcemActions(List<DcemAction> dcemActions) {
		this.dcemActions = dcemActions;
	}

	public List<DcemRole> getDcemRoles() {
		return dcemRoles;
	}

	public void setDcemRoles(List<DcemRole> dcemRoles) {
		this.dcemRoles = dcemRoles;
	}

	public int getRoleCount() {
		return this.dcemRoles.size();
	}

	public String actionSave() {
		try {
			roleLogic.saveAssignments(assignmentMap);
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "privilege.view.save.error", exp.getMessage());
			return null;
		}
		JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "privilege.view.save.ok");
		return null;
	}

	public boolean isPermissionSave() {
		DcemAction dcemAction = new DcemAction(subject, DcemConstants.ACTION_SAVE);
		DcemAction dcemActionManage = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
		return operatorSessionBean.isPermission(dcemActionManage, dcemAction);
	}

	public void leavingView() {
		dcemActions = null;
		dcemRoles = null;
	}

	public void filterListener(FilterEvent filterEvent) {
	}

	public void setFilteredResults(List<?> filteredResults) {
	}

	public List<?> getFilteredResults() {
		return null;
	}
}
