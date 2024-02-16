package com.doubleclue.dcem.dev.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.dev.preferences.DevPreferences;

@ApplicationScoped
@Named("devModule")
public class DevModule extends DcemModule {

	public final static String MODULE_ID = "dev";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.dev.resources.Messages";
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
		return RESOURCE_NAME;
	}
	
	public String getName() {
		return "Development";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}
	
	public int getRank() {
		return 999;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	
	public ModulePreferences getDefaultPreferences() {
		return new DevPreferences();
	}
	
	public DevPreferences getPreferences() {
		return (DevPreferences) super.getModulePreferences();
	}
}
