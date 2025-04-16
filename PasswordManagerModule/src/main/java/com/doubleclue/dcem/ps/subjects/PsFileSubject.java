package com.doubleclue.dcem.ps.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.ps.logic.PasswordSafeModule;
import com.doubleclue.dcem.ps.logic.PmConstants;


@SuppressWarnings("serial")
@ApplicationScoped
public class PsFileSubject extends SubjectAbs {

	public PsFileSubject() {
		setHiddenMenu(true);
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
	};

	@Override
	public String getModuleId() {
		return PasswordSafeModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 20;
	}

	@Override
	public String getIconName() {
		return "fa fa-square";
	}

	@Override
	public String getPath() {
		return PmConstants.PS_FILE_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}
}
