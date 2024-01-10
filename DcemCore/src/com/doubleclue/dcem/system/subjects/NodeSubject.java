package com.doubleclue.dcem.system.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.system.logic.SystemModule;

@ApplicationScoped
public class NodeSubject extends SubjectAbs {

	public NodeSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.IGNORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY));
		
//		RawAction rawAction = new RawAction(DcemConstants.ACTION_STOP_HEALTHCHECK, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
//		rawAction.setActionType(ActionType.EL_METHOD);
//		rawAction.setElMethodExpression("#{nodeDialog.actionStopHealthCheck()}");
//		rawActions.add(rawAction);
//		
//		rawAction = new RawAction(DcemConstants.ACTION_START_HEALTHCHECK, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }, ActionSelection.ONE_ONLY);
//		rawAction.setActionType(ActionType.EL_METHOD);
//		rawAction.setElMethodExpression("#{nodeDialog.actionStartHealthCheck()}");		
//		rawActions.add(rawAction);
	
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		for (RawAction action: rawActions) {
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
		return "fa fa-sitemap";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	
	@Override
	public Class<?> getKlass() {
		return DcemNode.class;
	}
		
}
