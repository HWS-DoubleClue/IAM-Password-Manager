package com.doubleclue.dcem.dev.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.dev.logic.DevModule;
import com.doubleclue.dcem.dev.logic.DevelopmentConstants;

@SuppressWarnings("serial")
@ApplicationScoped
public class CreateModuleSubject extends SubjectAbs {
	

	public CreateModuleSubject() {

		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE));	
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		
	}	

	@Override
	public String getModuleId() {
		return DevModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 30;
	}

	
	@Override
	public String getIconName() {
		return "user_headset.png";
	}

	@Override
	public String getPath() {
		return DevelopmentConstants.TEST_CREATE_MODULE_VIEW_PATH;
	}


	@Override
	public Class<?> getKlass() {
		return null;
	}

}
