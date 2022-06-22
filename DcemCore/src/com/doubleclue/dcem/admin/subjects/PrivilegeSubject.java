package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.Auditing;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class PrivilegeSubject extends SubjectAbs {

	public PrivilegeSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_SAVE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.IGNORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
	}

	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	public int getRank() {
		return 70;
	}

	public String getIconName() {
		return "fa fa-star";
	}

	public String getPath() {
		return DcemConstants.PRIVILEGE_VIEW_PATH;
	}

	public Class<?> getKlass() {
		return Auditing.class;
	}
}
