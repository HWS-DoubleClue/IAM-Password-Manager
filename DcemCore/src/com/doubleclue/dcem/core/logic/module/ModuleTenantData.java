package com.doubleclue.dcem.core.logic.module;

import java.util.Map;

public class ModuleTenantData {

	protected Map<String, String> specialPorperties;
	protected ModulePreferences modulePreferences;

	public Map<String, String> getSpecialPorperties() {
		return specialPorperties;
	}

	public void setSpecialPorperties(Map<String, String> specialPorperties) {
		this.specialPorperties = specialPorperties;
	}

	public ModulePreferences getModulePreferences() {
		return modulePreferences;
	}

	public void setModulePreferences(ModulePreferences modulePreferences) {
		this.modulePreferences = modulePreferences;
	}
}
