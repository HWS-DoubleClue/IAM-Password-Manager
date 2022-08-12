package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@Named("adminLicenceSubject")
@ApplicationScoped
public class AdminLicenceSubject extends SubjectAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AdminLicenceSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		RawAction rawAction = new RawAction(DcemConstants.ACTION_IMPORT_LICENCE_KEY, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.CREATE_OBJECT);
		rawAction.setIcon("fa fa-plus");
		rawActions.add(rawAction);
	}

	@Override
	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 170;
	}

	@Override
	public String getIconName() {
		return "fa fa-id-badge";
	}

	@Override
	public String getPath() {
		return DcemConstants.LICENCE_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}
}
