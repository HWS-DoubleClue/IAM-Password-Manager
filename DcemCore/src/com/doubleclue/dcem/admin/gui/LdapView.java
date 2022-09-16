package com.doubleclue.dcem.admin.gui;

import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.LdapSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DomainLogic;

@Named("ldapView")
@SessionScoped
public class LdapView extends DcemView {

	// @Inject
	// private AdminModule adminModule;

	@Inject
	private LdapSubject domainSubject;
	
	@Inject
	private DomainLogic domainLogic;

	@Inject
	private DomainDialogBean domainDialogBean;

	ResourceBundle resourceBundle;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	protected void init() {
		domainDialogBean.setParentView(this);

		subject = domainSubject;

		autoViewActions = new ArrayList<AutoViewAction>();
		resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
			
		addAutoViewAction( DcemConstants.ACTION_ADD,  resourceBundle, domainDialogBean, DcemConstants.DOMAIN_DIALOG_PATH);
		addAutoViewAction( DcemConstants.ACTION_EDIT,  resourceBundle, domainDialogBean, DcemConstants.DOMAIN_DIALOG_PATH);
		addAutoViewAction( DcemConstants.ACTION_DELETE,  resourceBundle, domainDialogBean, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, domainDialogBean, DcemConstants.DOMAIN_DIALOG_PATH);
	}
	
	public void reload() {
		String errors = domainLogic.testDomainConnections(false);
		if (errors.isEmpty() == false) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "ldap.initialization.failed", errors);
		}
		
	}


	

}
