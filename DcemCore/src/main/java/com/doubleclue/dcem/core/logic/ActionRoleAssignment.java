package com.doubleclue.dcem.core.logic;

import java.util.HashMap;
import java.util.Map;

import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;

public class ActionRoleAssignment {
	
	DcemAction dcemAction;
	DcemRole dcemRole;
	
	Map<Integer, Boolean> roleAssigned = new HashMap<Integer, Boolean>();  // roleId and assignement
	
	

	public DcemAction getDcemAction() {
		return dcemAction;
	}

	public void setDcemAction(DcemAction dcemAction) {
		this.dcemAction = dcemAction;
	}

	public Map<Integer, Boolean> getRoleAssigned() {
//		System.out.println("ActionRoleAssignment.getRoleAssigned() " + roleAssigned);
		return roleAssigned;
	}

	public void setRoleAssigned(Map<Integer, Boolean> roleAssigned) {
		this.roleAssigned = roleAssigned;
	}

	public DcemRole getDcemRole() {
		return dcemRole;
	}

	public void setDcemRole(DcemRole dcemRole) {
		this.dcemRole = dcemRole;
	}

}
