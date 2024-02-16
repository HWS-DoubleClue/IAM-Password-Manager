package com.doubleclue.dcem.radius.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.radius.entities.RadiusReportEntity;
import com.doubleclue.dcem.radius.logic.RadiusModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class RadiusReportSubject extends SubjectAbs {

	public RadiusReportSubject() {

		
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
		for (RawAction action : rawActions) {
			action.setMasterOnly(true);
		}
		
	}	

	@Override
	public String getModuleId() {
		return RadiusModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 60;
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
		return RadiusReportEntity.class;
	}

}
