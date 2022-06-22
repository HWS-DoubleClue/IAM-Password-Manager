package com.doubleclue.dcem.test.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.test.logic.TestModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class PreferencesSubject extends SubjectAbs {
	
	public PreferencesSubject () {
		rawActions.add (new RawAction (DcemConstants.ACTION_SAVE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
		rawActions.add (new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}));	
		
	};
	
	@Override
	public String getModuleId() {
		return TestModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 999;
	}
	
	@Override
	public String getIconName() {
		return "preferences.png";
	}

	@Override
	public String getPath() {
		return DcemConstants.PREFERENCES_VIEW_PATH;
	}
	
	
	@Override
	public Class<?> getKlass() {
		return null;
	}
	
}
