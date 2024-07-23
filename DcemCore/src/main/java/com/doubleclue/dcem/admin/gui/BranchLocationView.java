package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects. BranchLocationSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@SuppressWarnings("serial")
@Named("branchLocationView")
@SessionScoped
public class BranchLocationView extends DcemView {

	@Inject
	private BranchLocationSubject branchLocationSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private BranchLocationDialog branchLocationDialog;  // small letters


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = branchLocationSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, branchLocationDialog, "/modules/admin/BranchLocationDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, branchLocationDialog, "/modules/admin/BranchLocationDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, branchLocationDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
	}

	/*
	* This method is called when the view is displayed or reloaded
	*
	*/
	@Override
	public void reload() {
		
	}

    @Override
	public Object createActionObject() {
		return super.createActionObject();
	}
}
