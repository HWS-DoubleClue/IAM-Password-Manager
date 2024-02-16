package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class AsVersionSubject extends SubjectAbs {

	public AsVersionSubject() {
		RawAction rawAction = new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.CREATE_OBJECT);
		rawAction.setMasterOnly(true);
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.ONE_ONLY);
		rawAction.setMasterOnly(true);
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.ONE_OR_MORE);
		rawAction.setMasterOnly(true);
		rawActions.add(rawAction);

		rawAction = new RawAction(AsConstants.ACTION_GENERATE_SDK_CONFIG,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-plus-square-o");
		rawAction.setMasterOnly(true);
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.ACTION_VIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN });
		rawAction.setMasterOnly(false);
		rawActions.add(rawAction);
		rawAction = new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN });
		rawAction.setMasterOnly(false);
		rawActions.add(rawAction);


	}

	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 85;
	}

	@Override
	public String getIconName() {
		return "fa fa-list-ol";
	}

	@Override
	public String getPath() {
		// return "/modules/asm/devices.xhtml";
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return AsVersionEntity.class;
	}

}
