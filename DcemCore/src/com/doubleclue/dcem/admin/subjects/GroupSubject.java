package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class GroupSubject extends SubjectAbs {

	public GroupSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY));
		
		RawAction rawAction = new RawAction(DcemConstants.ACTION_MEMBERS, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-user");
		rawActions.add(rawAction);
		
		rawAction = new RawAction (DcemConstants.CREATE_ACTIVATION_CODE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE);
		rawAction.setIcon("fa fa-qrcode");
		rawAction.setDependsOnModule(DcemConstants.AS_MODULE_ID);
		rawActions.add(rawAction);
		
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }, ActionSelection.ONE_OR_MORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
	}

	@Override
	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 30;
	}

	@Override
	public String getIconName() {
		return "fa fa-users";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return DcemGroup.class;
	}
}
