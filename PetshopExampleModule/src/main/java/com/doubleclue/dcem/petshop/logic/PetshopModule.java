package com.doubleclue.dcem.petshop.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.petshop.preferences.PetshopPreferences;

@ApplicationScoped
@Named("petshopModule")
public class PetshopModule extends DcemModule {

	private static final long serialVersionUID = 1L;
	// private static Logger logger = LogManager.getLogger(PetshopModule.class);

	public final static String MODULE_ID = "petshop";
	public final static String MODULE_NAME = "Petshop";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.petshop.resources.Messages";
	public final static int MODULE_RANK = 90;

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public String getName() {
		return MODULE_NAME;
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return MODULE_RANK;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new PetshopPreferences();
	}

	@Override
	public PetshopPreferences getModulePreferences() {
		return ((PetshopPreferences) super.getModulePreferences());
	}

}
