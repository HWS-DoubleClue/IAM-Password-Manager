
package com.doubleclue.dcem.dm.subjects;


import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class DmRecycleBinSubject extends SubjectAbs {

	public DmRecycleBinSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }));
	}

	@Override
	public String getModuleId() {
		return DocumentManagementModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 3;
	}

	@Override
	public String getIconName() {
		return "fa fa-trash";
	}

	@Override
	public String getPath() {
		return DmConstants.DM_RECYCLE_BIN_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}

}
