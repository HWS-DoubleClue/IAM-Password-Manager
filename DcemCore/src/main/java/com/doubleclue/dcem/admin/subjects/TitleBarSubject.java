package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@ApplicationScoped
public class TitleBarSubject extends SubjectAbs {

	private static final long serialVersionUID = 1L;

	public TitleBarSubject() {
		
		rawActions.add(new RawAction(DcemConstants.ACTION_USER_PROFILE, new String[] { DcemConstants.SYSTEM_ROLE_USER },  ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_CHANGE_PASSWORD, new String[] { DcemConstants.SYSTEM_ROLE_USER },  ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_SWITCH_USER_PORTAL, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK, DcemConstants.SYSTEM_ROLE_VIEWER, DcemConstants.SYSTEM_ROLE_USER },  ActionSelection.ONE_ONLY));
//		
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		setHiddenMenu(true);
	}

	@Override
	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 10;
	}

	@Override
	public String getIconName() {
		return "fa fa-dashboard";
	}

	@Override
	public String getPath() {
		return DcemConstants.WELCOME_VIEW_PATH;
	}
}
