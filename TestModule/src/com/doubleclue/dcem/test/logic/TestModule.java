package com.doubleclue.dcem.test.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.test.preferences.TestPreferences;

@ApplicationScoped
@Named("testModule")
public class TestModule extends DcemModule {

	public final static String MODULE_ID = "test";
	public final static String RESOUCE_NAME = "com.doubleclue.dcem.test.resources.Messages";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

//	HashMap<String, SubjectAbs> subjects = new HashMap<String, SubjectAbs>();

	@Override
	public void init() throws DcemException {
//		ModuleDef moduleDef = new ModuleDef(MODULE_ID, "2.3", null);
//		moduleManifest = new ModuleManifest(moduleDef, "Administration", "", 20, null, 1, true);
	}

	public String getResourceName() {
		return RESOUCE_NAME;
	}
	
	public String getName() {
		return "Test-Module";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	public int getRank() {
		return 120;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	
	public ModulePreferences getDefaultPreferences() {
		return new TestPreferences();
	}
	
	public TestPreferences getPreferences() {
		return (TestPreferences) super.getModulePreferences();
	}
}
