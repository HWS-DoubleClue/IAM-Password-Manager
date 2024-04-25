package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.RoleSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@Named("roleView")
@SessionScoped
public class RoleViewBean extends DcemView {

	@Inject
	private RoleDialogBean roleDialogBean;

	@Inject 
	RoleSubject roleSubject;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	protected void init() {
		roleDialogBean.setParentView(this);
		subject = roleSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		addAutoViewAction( DcemConstants.ACTION_ADD,  resourceBundle, roleDialogBean, null);
		addAutoViewAction( DcemConstants.ACTION_EDIT,  resourceBundle, roleDialogBean, null);
		addAutoViewAction( DcemConstants.ACTION_DELETE,  resourceBundle, roleDialogBean, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}
}
