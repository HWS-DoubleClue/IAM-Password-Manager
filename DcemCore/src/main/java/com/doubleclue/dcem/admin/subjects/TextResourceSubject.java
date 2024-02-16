package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.TextMessage;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class TextResourceSubject extends SubjectAbs {

	public TextResourceSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_COPY,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY));

		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_OR_MORE));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		RawAction rawAction = new RawAction(DcemConstants.ACTION_UPLOAD,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.IGNORE);
		rawAction.setIcon(DcemConstants.ACTION_UPLOAD_ICON);
		rawActions.add(rawAction);
		rawAction = new RawAction(DcemConstants.ACTION_DOWNLOAD,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.IGNORE);
		rawAction.setIcon(DcemConstants.ACTION_DOWNLOAD_ICON);
		rawActions.add(rawAction);

	}

	public int getRank() {
		return 110;
	}

	@Override
	public String getIconName() {
		return "fa fa-file-text";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
		// return DcemConstants.TEXT_RESOURCE_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return TextMessage.class;
	}

	@Override
	public String getModuleId() {
		// TODO Auto-generated method stub
		return AdminModule.MODULE_ID;
	}

}
