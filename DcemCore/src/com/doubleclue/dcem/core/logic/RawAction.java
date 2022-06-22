package com.doubleclue.dcem.core.logic;

import java.io.Serializable;

import com.doubleclue.dcem.core.DcemConstants;

public class RawAction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String icon;
	String [] noPermissionForRole;
	ActionSelection actionSelection;
	ActionType actionType = ActionType.DIALOG;
	String elMethodExpression;
	String dependsOnModule;
	boolean masterOnly;
	
	
	public RawAction(String name, String[] noPermissionForRole) {
		super();
		this.name = name;
		this.noPermissionForRole = noPermissionForRole;
		actionSelection = ActionSelection.IGNORE;
		setAutoActionType();
	}
	
	public RawAction(String name, String[] noPermissionForRole, String icon) {
		super();
		this.name = name;
		this.noPermissionForRole = noPermissionForRole;
		actionSelection = ActionSelection.IGNORE;
		this.icon = icon;
		setAutoActionType();
	}
	
	public RawAction(String name, String[] noPermissionForRole, ActionSelection actionSelection) {
		super();
		this.name = name;
		this.noPermissionForRole = noPermissionForRole;
		this.actionSelection = actionSelection;
		setAutoActionType();
	}
	
	public RawAction(String name, String[] noPermissionForRole, ActionSelection actionSelection, String icon) {
		super();
		this.name = name;
		this.noPermissionForRole = noPermissionForRole;
		this.actionSelection = actionSelection;
		this.icon = icon;
		setAutoActionType();
	}
	
	public RawAction(String name, String[] noPermissionForRole, ActionSelection actionSelection, String icon, ActionType actionType) {
		super();
		this.name = name;
		this.noPermissionForRole = noPermissionForRole;
		this.actionSelection = actionSelection;
		this.icon = icon;
		this.actionType = actionType;
	}
	
	private void setAutoActionType () {
		if (name.equals(DcemConstants.ACTION_ADD) || name.equals(DcemConstants.ACTION_GENERATE) || name.equals(DcemConstants.ACTION_UPLOAD)) {
			this.actionType = ActionType.CREATE_OBJECT;
			return;
		}
		if (name.equals(DcemConstants.ACTION_DELETE) || name.equals(DcemConstants.ACTION_LOCK) || name.equals(DcemConstants.ACTION_UNLOCK) ) {
			this.actionType = ActionType.CONFIRM;
		} else {
			this.actionType = ActionType.DIALOG;
		}
	}
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}


	public String[] getNoPermissionForRole() {
		return noPermissionForRole;
	}


	public void setNoPermissionForRole(String[] noPermissionForRole) {
		this.noPermissionForRole = noPermissionForRole;
	}

	public ActionSelection getActionSelection() {
		return actionSelection;
	}

	public void setActionSelection(ActionSelection actionSelection) {
		this.actionSelection = actionSelection;
	}

	public String getIcon() {
		if (icon == null) {
			if (name.equals(DcemConstants.ACTION_ADD)) {
				return DcemConstants.ACTION_ADD_ICON;
			} else if (name.equals(DcemConstants.ACTION_COPY)) {
				return DcemConstants.ACTION_COPY_ICON;
			} else if (name.equals(DcemConstants.ACTION_EDIT)) {
				return DcemConstants.ACTION_EDIT_ICON;
			} else if (name.equals(DcemConstants.ACTION_DELETE)) {
				return DcemConstants.ACTION_DELETE_ICON;
			} else if (name.equals(DcemConstants.ACTION_DISABLE)) {
				return DcemConstants.ACTION_DISABLE_ICON;
			} else if (name.equals(DcemConstants.ACTION_ENABLE)) {
				return DcemConstants.ACTION_ENABLE_ICON;
			} else if (name.equals(DcemConstants.ACTION_PROPERTIES)) {
				return DcemConstants.ACTION_PROPERTIES;
			} else if (name.equals(DcemConstants.ACTION_SAVE)) {
				return DcemConstants.ACTION_SAVE_ICON;
			} else if (name.equals(DcemConstants.ACTION_CONFIGURE)) {
				return DcemConstants.ACTION_EDIT_ICON;
			} else {
				return "";
			}
		}
		
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public void setActionType(ActionType actionType) {
		this.actionType = actionType;
	}

	public String getElMethodExpression() {
		return elMethodExpression;
	}

	public void setElMethodExpression(String elMethodExpression) {
		this.elMethodExpression = elMethodExpression;
	}

	public boolean isMasterOnly() {
		return masterOnly;
	}

	public void setMasterOnly(boolean masterOnly) {
		this.masterOnly = masterOnly;
	}

	public String getDependsOnModule() {
		return dependsOnModule;
	}

	public void setDependsOnModule(String dependsOnModule) {
		this.dependsOnModule = dependsOnModule;
	}

	
	

}
