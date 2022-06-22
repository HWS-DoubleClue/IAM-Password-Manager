package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;

@Named("groupDialog")
@SessionScoped
public class GroupDialogBean extends DcemDialog {
	
	

	@Inject
	UserLogic userLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;
	
	@Inject
	RoleLogic roleLogic;

	@Inject
	JpaLogic jpaLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	static final String NO_ROLE= "(No Role)";

	String domainName;

	String name;

	String groupType = DcemConstants.TYPE_LOCAL;
	
	private LinkedList<SelectItem> availableRoles;

	private static final long serialVersionUID = 1L;
	
	String selectedRole;


	@PostConstruct
	private void init() {
		// System.out.println("AddUserDialogBean.init()");
	}

	public void changeType() {		
	}

	public boolean actionOk() throws Exception {
		if (userOutranksOperator()) {
			throw new DcemException(DcemErrorCodes.CANNOT_EDIT_OUTRANKING_USER, null);
		}
		DcemRole dcemRole = null;
		if (selectedRole.equals(NO_ROLE) == false) {
			dcemRole = roleLogic.getDcemRole(selectedRole);
		}
		DcemGroup group = (DcemGroup) getActionObject();
		if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
			if (groupType.equals(DcemConstants.TYPE_DOMAIN)) {
				try {
					if (domainName == null) {
						JsfUtils.addErrorMessage("No LDAP configuration found");
						return false;
					} 
					List<DcemGroup> groups  = domainLogic.getGroups(domainName, name);
					if (groups.isEmpty()) {
						JsfUtils.addErrorMessage("Domain-Group doesn't exists.");
						return false;
					}
					group.setGroupDn(groups.get(0).getGroupDn());
					group.setName(groups.get(0).getName());

				} catch (DcemException e1) {
					JsfUtils.addErrorMessage("Please select an LDAP-Domain");
					return false;
				}

			}
		}
		group.setDcemRole(dcemRole);
		groupLogic.addOrUpdateGroup(group, getAutoViewAction().getDcemAction(), true);
		return true;
	}
	
	
	public boolean userOutranksOperator() {
		DcemUser user = operatorSessionBean.getDcemUser();
		DcemRole userRole = user.getDcemRole();
		return (userRole != null) ? userRole.getRank() > getOperatorRank() : false;
	}

	public List<String> completeGroup(String groupFilter) {
		
		if (domainName == null || domainName.isEmpty()) {
			return null;
		}
		try {
			List<DcemGroup> groups =  domainLogic.getGroups(domainName, groupFilter + "*", 50);
			List<String> list = new ArrayList<String>(groups.size());
			for (DcemGroup dcemGroup : groups) {
				list.add(dcemGroup.getRawName());
			}
			return list;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		groupLogic.deleteGroups(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public List<SelectItem> getDomainNames() {
		List<SelectItem> listSelection = domainLogic.getDomainNames();
		if (listSelection.size() == 0) {
			JsfUtils.addErrorMessage("No LDAP configuration found");
			return null;
		}
		if (domainName == null && listSelection.size() > 0) {
			domainName = (String) listSelection.get(0).getValue();
		}
		return listSelection;
	}
	
	public LinkedList<SelectItem> getAvailableRoles() {
		if (availableRoles == null) {
			availableRoles = new LinkedList<SelectItem>();
			List<DcemRole> roles = roleLogic.getAllDcemRoles();
			int operatorRank = getOperatorRank();
			availableRoles.add(new SelectItem(NO_ROLE, NO_ROLE));
			for (DcemRole role : roles) {
				availableRoles.add(new SelectItem(role, role.getName(), null, role.getRank() > operatorRank));
			}
		}
		return availableRoles;
	}
	
	private int getOperatorRank() {
		return operatorSessionBean.getHighestRole().getRank();
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		DcemGroup group = (DcemGroup) this.getActionObject();
		groupType = group.isDomainGroup() == true ? DcemConstants.TYPE_DOMAIN : DcemConstants.TYPE_LOCAL;

		if (group.getName() != null) {
			String[] domainUser = group.getName().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
			if (domainUser.length > 1) {
				domainName = domainUser[0];
			}
			name = group.getName();
		} else {
			name = null;
			domainName = null;
		}
		selectedRole = group.getDcemRole() == null ?  NO_ROLE : group.getDcemRole().getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	public String getSelectedRole() {
		return selectedRole;
	}

	public void setSelectedRole(String selectedRole) {
		this.selectedRole = selectedRole;
	}

}
