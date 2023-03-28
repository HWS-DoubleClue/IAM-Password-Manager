package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.Auditing;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class DepartmentSubject extends SubjectAbs{

	
	public  DepartmentSubject () {
		rawActions.add(new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_COPY, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_EDIT, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_DELETE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));
		rawActions.add(new RawAction (DcemConstants.ACTION_ORGANIGRAM, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));
		rawActions.add (new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));		
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));		
					
	}

	
	@Override
	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 160;
	}


	@Override
	public String getIconName() {
		return "fa fa-building-user";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return DepartmentEntity.class;
	}

	
}
