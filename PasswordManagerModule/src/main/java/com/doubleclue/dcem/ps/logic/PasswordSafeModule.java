package com.doubleclue.dcem.ps.logic;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.ps.preferences.PsPreferences;

@ApplicationScoped
public class PasswordSafeModule extends DcemModule {

	public final static String MODULE_ID = "ps";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.ps.resources.Messages";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	public void init() throws DcemException {
	}

	public String getResourceName() {
		return RESOURCE_NAME;
	}
	
	public String getName() {
		return "PasswordSafe";
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
		return new PsPreferences();
	}
	
	public PsPreferences getPreferences() {
		return (PsPreferences) super.getModulePreferences();
	}
}
