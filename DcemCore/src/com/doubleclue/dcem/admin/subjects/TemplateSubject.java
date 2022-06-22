package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class TemplateSubject extends SubjectAbs {

	public TemplateSubject () {
		rawActions.add(new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_COPY, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_EDIT, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_DELETE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));

		RawAction rawAction = new RawAction (DcemConstants.ACTION_SHOW, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-eye");
		rawActions.add(rawAction);
		
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
	}

	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	public int getRank() {
		return 100;
	}

	public String getIconName() {
		return "fa fa-file-contract";
	}

	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	public Class<?> getKlass() {
		return DcemTemplate.class;
	}

}
