package com.doubleclue.dcem.dm.logic;

import com.doubleclue.dcem.core.gui.JsfUtils;


public enum WorkflowTrigger {
	Added, Modify, MovedToTrash, OnDate, Periodically_Weekly, Periodically_Monthly, Periodically_Yearly;
	
	
	public String getLocaleText() {
		return JsfUtils.getStringSafely(DocumentManagementModule.RESOURCE_NAME, "workflowTrigger." + this.name());
	}
}
