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

import org.primefaces.event.ToggleEvent;
import org.primefaces.event.data.FilterEvent;
import org.primefaces.model.Visibility;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.PrivilegeSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.ActionRoleAssignment;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Named("privilegeView")
@SessionScoped
public class PrivilegeViewBean extends DcemView {

	final String COLUMN_TOGGLER = "CT-";

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
	Map<Integer, Boolean> roleFilterSettings;
	String moduleFilter;
	DcemAction save;

	@PostConstruct
	private void init() {
		subject = privilegeSubject;
		save = new DcemAction(subject, DcemConstants.ACTION_SAVE);
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
		loadRoleFilter();
		autoViewBean.reload();
	}

	private void loadRoleFilter() {
		roleFilterSettings = new HashMap<Integer, Boolean>();
		try {
			String value = operatorSessionBean.getLocalStorageUserSetting(COLUMN_TOGGLER + viewNavigator.getActiveView().getClassName());
			if (value != null) {
				String[] columnVisibilties = value.split(", ");
				for (String columnVisibilty : columnVisibilties) {
					String[] keyValuePair = columnVisibilty.split("=");
					roleFilterSettings.put(Integer.valueOf(keyValuePair[0]), Boolean.valueOf(keyValuePair[1]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				operatorSessionBean.removeLocalStorageUserSetting(COLUMN_TOGGLER + viewNavigator.getActiveView().getClassName());
			} catch (Exception exp) {
			}
		}
	}

	private void updateRoleFilter() {
		try {
			if (roleFilterSettings == null || roleFilterSettings.isEmpty()) {
				operatorSessionBean.removeLocalStorageUserSetting(COLUMN_TOGGLER + viewNavigator.getActiveView().getClassName());
				return;
			}
			List<String> columnVisibilty = new ArrayList<String>();
			for (Map.Entry<Integer, Boolean> entry : roleFilterSettings.entrySet()) {
				columnVisibilty.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
			}
			operatorSessionBean.setLocalStorageUserSetting(COLUMN_TOGGLER + viewNavigator.getActiveView().getClassName(), String.join(", ", columnVisibilty));
		} catch (Exception e) {
			logger.warn("Could not save role filter to local storage for user: " + operatorSessionBean.getDcemUser().getDisplayName(), e);
		}
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

	public void onToggle(ToggleEvent event) {
		try {
			int columPosition = (int) event.getData();
			if (columPosition < 3) {
				return;
			}
			DcemRole columnRole = dcemRoles.get(columPosition - 3);
			roleFilterSettings.put(columnRole.getId(), event.getVisibility() == Visibility.VISIBLE);
			updateRoleFilter();
		} catch (Exception e) {
			logger.warn("Could not toggle column filter", e);
		}
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
			roleLogic.saveAssignments(assignmentMap, save);
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "privilege.view.save.error", exp.getMessage());
			return null;
		}
		JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "privilege.view.save.ok");
		return null;
	}

	public boolean isPermissionSave() {
		return operatorSessionBean.isPermission(save);
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

	public Map<Integer, Boolean> getRoleFilterSettings() {
		return roleFilterSettings;
	}

	public void setRoleFilterSettings(Map<Integer, Boolean> roleFilterSettings) {
		this.roleFilterSettings = roleFilterSettings;
	}
}
