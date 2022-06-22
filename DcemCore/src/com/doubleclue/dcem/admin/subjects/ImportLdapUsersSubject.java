package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class ImportLdapUsersSubject extends SubjectAbs  {

	public ImportLdapUsersSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
	}

	
	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	public int getRank() {
		return 85;
	}


	@Override
	public String getIconName() {
		return "fa fa-user-plus";
	}


	@Override
	public String getPath() {
		return DcemConstants.IMPORT_USERS_VIEW_PATH;
	}
	
	@Override
	public Class<?> getKlass() {
		return null;
	}

}
