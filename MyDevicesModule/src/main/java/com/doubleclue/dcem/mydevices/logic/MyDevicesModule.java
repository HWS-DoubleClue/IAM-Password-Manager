package com.doubleclue.dcem.mydevices.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.mydevices.preferences.MydevicesPreferences;

@ApplicationScoped
public class MyDevicesModule extends DcemModule {

	public final static String MODULE_ID = "mydevices";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.mydevices.resources.Messages";
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
		return "MyDevices";
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
		return new MydevicesPreferences();
	}
	
	public MydevicesPreferences getPreferences() {
		return (MydevicesPreferences) super.getModulePreferences();
	}
}
