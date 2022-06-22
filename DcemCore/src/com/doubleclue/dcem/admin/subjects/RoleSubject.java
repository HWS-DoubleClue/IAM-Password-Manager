package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class RoleSubject extends SubjectAbs {

	public RoleSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.IGNORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_OR_MORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));

	}

	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	public int getRank() {
		return 60;
	}

	@Override
	public String getIconName() {
		return "fa fa-id-card";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	
	@Override
	public Class<?> getKlass() {
		return DcemRole.class;
	}
	
	public String getTableStyle() {
		return "roleViewTableStyle";
	}

}
