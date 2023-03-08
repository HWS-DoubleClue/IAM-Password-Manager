package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.DepartmentSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@Named("departmentView")
@SessionScoped
public class DepartmentView extends DcemView {


	@Inject
	private DepartmentSubject departmentSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private DepartmentDialog departmentDialog;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	ResourceBundle resourceBundle;

	@PostConstruct
	private void init() {
		departmentDialog.setParentView(this);
		subject = departmentSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, departmentDialog, DcemConstants.DEPARTMENT_DIALOG);
	//	addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, departmentDialog, DcemConstants.TEMPLATE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, departmentDialog, DcemConstants.DEPARTMENT_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_ORGANIGRAM, resourceBundle, departmentDialog, DcemConstants.DEPARTMENT_ORGAMIGRAM_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, departmentDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
		
}
