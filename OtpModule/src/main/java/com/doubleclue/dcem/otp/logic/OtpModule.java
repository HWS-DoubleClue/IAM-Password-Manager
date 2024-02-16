package com.doubleclue.dcem.otp.logic;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.tasks.TaskExecutor;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("otpModule")
public class OtpModule extends DcemModule {

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	OtpLogic otpLogic;


	public final static String MODULE_ID = "otp";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.otp.resources.Messages";

	public void start() throws DcemException {
		super.start();
	}

	public String getResourceName() {
		return RESOURCE_NAME;
	}
	
	@Override
	public boolean isPluginModule() {
		return false;
	}

	public String getName() {
		return "OTP Tokens";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	public int getRank() {
		return 50;
	}

	@Override
	public DcemView getDefaultView() {

		return null;
	}

	public ModulePreferences getDefaultPreferences() {
		return new OtpPreferences();
	}

	public OtpPreferences getPreferences() {
		return (OtpPreferences) getModuleTenantData().getModulePreferences();
	}

	public Map<String, String> getStatisticValues() {
		Map<String, String> map = super.getStatisticValues();
		return map;
	}

	@Override
	public void init() throws DcemException {
	}

	@Override
	public void runNightlyTask() {
		
	}
	
	@Override
	public void deleteUserFromDb(DcemUser dcemUser) throws DcemException {
		otpLogic.unassignUser(dcemUser);
		return ;
	}
}
