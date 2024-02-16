package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.util.ResourceBundle;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.core.utils.DcemUtils;

public class AutoViewAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	
	DcemAction dcemAction;
	String xhtmlPage;
	DcemDialog dcemDialog;
	ResourceBundle resourceBundle;
	RawAction rawAction;
	ViewLink viewLink;
	
	
	public AutoViewAction (DcemAction dcemAction, DcemDialog sasDialog, ResourceBundle resourceBundle, RawAction rawAction, String xhtmlPage, ViewLink viewLink) {
		this.dcemAction = dcemAction;
		this.xhtmlPage = xhtmlPage;
		this.dcemDialog = sasDialog;
		this.resourceBundle = resourceBundle;
		this.rawAction = rawAction;
		this.viewLink = viewLink;
	}

	public DcemDialog getDcemDialog() {
		return dcemDialog;
	}

	public void setDcemDialog(DcemDialog dcemDialog) {
		this.dcemDialog = dcemDialog;
	}

	
	public DcemAction getDcemAction() {
		return dcemAction;
	}

	public void setDcemAction(DcemAction dcemAction) {
		this.dcemAction = dcemAction;
	}

	public String getActionText() {
		String name;
		if (resourceBundle == null) {
			name = DcemUtils.resourceKeyToName(dcemAction.getAction());
		} else {
			name = JsfUtils.getStringSafely(resourceBundle, "AUTO_ACTION." + dcemAction.getAction());
		}
		return name;
	}	

	public RawAction getRawAction() {
		return rawAction;
	}

	public void setRawAction(RawAction rawAction) {
		this.rawAction = rawAction;
	}

	public String getXhtmlPage() {
		if (xhtmlPage == null) {
			return DcemConstants.AUTO_DIALOG_PATH;
		}
		return xhtmlPage;
	}

	public void setXhtmlPage(String xhtmlPage) {
		this.xhtmlPage = xhtmlPage;
	}

	public ViewLink getViewLink() {
		return viewLink;
	}

	public void setViewLink(ViewLink viewLink) {
		this.viewLink = viewLink;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
	
	public boolean isLink () {
		return (rawAction != null && rawAction.getActionType() == ActionType.VIEW_LINK);
	}


}
