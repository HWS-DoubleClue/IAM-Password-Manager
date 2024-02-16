package com.doubleclue.dcem.userportal.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.userportal.logic.UserPortalConstants;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class UserPortalConfigSubject extends SubjectAbs {

	public UserPortalConfigSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_SAVE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
	};

	@Override
	public String getModuleId() {
		return UserPortalModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 999;
	}

	@Override
	public String getIconName() {
		return "fa fa-cog";
	}

	@Override
	public String getPath() {
		return UserPortalConstants.CONFIG_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}
}
