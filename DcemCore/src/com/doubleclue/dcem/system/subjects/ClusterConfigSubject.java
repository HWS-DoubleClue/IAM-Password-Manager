package com.doubleclue.dcem.system.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.system.logic.SystemModule;

@ApplicationScoped
public class ClusterConfigSubject extends SubjectAbs {

	public ClusterConfigSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_SAVE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.IGNORE));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW,	new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
		for (RawAction action: rawActions) {
			action.setMasterOnly(true);
		}
	}

	public String getModuleId() {
		return SystemModule.MODULE_ID;
	}

	public int getRank() {
		return 10;
	}

	@Override
	public String getIconName() {
		return "fa fa-sitemap";
	}

	@Override
	public String getPath() {
		return DcemConstants.CLUSTER_CONFIG_VIEW_PATH;
	}

	
	@Override
	public Class<?> getKlass() {
		return null;
	}
		
}
