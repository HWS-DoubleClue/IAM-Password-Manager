//#excludeif COMMUNITY_EDITION
package com.doubleclue.dcem.system.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.system.logic.SystemModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class TenantSubject extends SubjectAbs {

	public TenantSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.IGNORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		RawAction rawAction = new RawAction(DcemConstants.ACTION_RECOVER_SUPERADMIN_ACCESS, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-rotate-left");
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_SWITCH_TO_TENANT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN },
				ActionSelection.ONE_ONLY );
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{tenantDialog.actionSwitchToTenant()}");
		rawAction.setIcon("fa fa-shuffle");
		rawActions.add(rawAction);
		for (RawAction action : rawActions) {
			action.setMasterOnly(true);
		}
	}

	public String getModuleId() {
		return SystemModule.MODULE_ID;
	}

	public int getRank() {
		return 20;
	}

	@Override
	public String getIconName() {
		return "fa fa-building";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return TenantEntity.class;
	}
}
