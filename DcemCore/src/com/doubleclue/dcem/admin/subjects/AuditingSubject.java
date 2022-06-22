//#excludeif COMMUNITY_EDITION == true
package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.Auditing;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class AuditingSubject extends SubjectAbs{

	
	public  AuditingSubject () {
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
		return "fa fa-book";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return Auditing.class;
	}

	
}
