package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class ReportingSubject extends SubjectAbs {

	public ReportingSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		RawAction exportAll = new RawAction(DcemConstants.ACTION_EXCEL_EXPORT_ALL,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.IGNORE);
		exportAll.setActionType(ActionType.EXCEL_EXPORT_ALL);
		exportAll.setIcon("fa fa-file-excel-o");
		rawActions.add(exportAll);
	}

	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 35;
	}

	@Override
	public String getIconName() {
		return "fa fa-clipboard";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return DcemReporting.class;
	}
}
