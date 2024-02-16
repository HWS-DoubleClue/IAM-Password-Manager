package com.doubleclue.dcem.userportal.logic;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jersey.RestAuthFilter;
import com.doubleclue.dcem.core.logic.CreateTenant;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.userportal.entities.ApplicationHubEntity;
import com.doubleclue.dcem.userportal.gui.ApplicationHubAdminView;
import com.doubleclue.dcem.userportal.preferences.UserPortalPreferences;
import com.doubleclue.dcup.gui.PortalSessionBean;
import com.doubleclue.dcup.logic.ActionItem;
import com.doubleclue.dcup.logic.ViewItem;
import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.ResourceFinder;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("userPortalModule")
public class UserPortalModule extends DcemModule {

	// private static Logger logger = LogManager.getLogger(UserPortalModule.class);

	public final static String MODULE_ID = DcemConstants.USER_PORTAL_MODULE_ID;
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.userportal.resources.Messages";

	@Inject
	RoleLogic roleLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	PortalSessionBean portalSessionBean;

	@Inject
	UpAppHubLogic upAppHubLogic;

	private Logger logger = LogManager.getLogger(ApplicationHubAdminView.class);

	public String getServletUrl() throws DcemException {
		String url = dcemApplicationBean.getUserPortalUrl(null);
		int ind = url.lastIndexOf('/');
		url = url.substring(0, ind);
		return url + DcemConstants.USERPORTAL_SERVLET_PATH + "?token=";
	}

	public String getResourceName() {
		return RESOURCE_NAME;
	}

	public String getName() {
		return "UserPortal";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getDbVersion() {
		return 0;
	}
	
	@Override
	public boolean isPluginModule() {
		return false;
	}

	public int getRank() {
		return 80;
	}

	@Override
	public DcemView getDefaultView() {
		try {
			return CdiUtils.getReference("welcomeView");
		} catch (Exception exp) {
			exp.printStackTrace();
			return null;
		}
	}

	public ModulePreferences getDefaultPreferences() {
		return new UserPortalPreferences();
	}

	@Override
	public boolean isHasDbTables() {
		return true;
	}

	@Override
	public UserPortalPreferences getModulePreferences() {
		return ((UserPortalPreferences) super.getModulePreferences());
	}

	@Override
	public void initializeTenant(TenantEntity tenantEntity, ModuleTenantData tenantData) throws DcemException {
		super.initializeTenant(tenantEntity, tenantData);
		if (RestAuthFilter.getEmbeddedPass() == null) {
			RestAuthFilter.setEmbeddedPass(RandomUtils.generateRandomAlphaNumericString(20));
		}
		Map<ViewItem, Boolean> visibleViews = getModulePreferences().getVisibleViews();
		visibleViews.remove(null);
		for (ViewItem viewItem : ViewItem.values()) {
			if (visibleViews.get(viewItem) == null) {
				visibleViews.put(viewItem, false);
			}
		}
		Map<ActionItem, Boolean> visibleActions = getModulePreferences().getVisibleActions();
		visibleActions.remove(null);
		for (ActionItem actionItem : ActionItem.values()) {
			if (visibleActions.get(actionItem) == null) {
				visibleActions.put(actionItem, false);
			}
		}
	}

	@Override
	public void init() throws DcemException {
	}

	public boolean isEnableCaptcha() {
		return dcemApplicationBean.isCaptchaOn() && getModulePreferences().isEnableCaptcha();
	}

	public SupportedLanguage[] getSupportedLanguages() {
		return SupportedLanguage.values();
	}

	@Override
	public void initializeDb(DcemUser superAdmin) throws DcemException {
		List<FileContent> myApplicationsFiles = null;
		try {
			myApplicationsFiles = ResourceFinder.find(CreateTenant.class, DcemConstants.MYAPPLICATIONS_RESOURCES, DcemConstants.MYAPPLICATIONS_TYPE);
		} catch (Exception e) {
			logger.warn("Couldn't upload myapplication files.", e);
		}
		for (FileContent fileContent : myApplicationsFiles) {
			try {
				String fileContents = new String(fileContent.getContent(), DcemConstants.CHARSET_UTF8);
				ObjectMapper mapper = new ObjectMapper();
				MyApplication myApplication = mapper.readValue(fileContents, MyApplication.class);
				upAppHubLogic.updateApplication(new ApplicationHubEntity(myApplication));
			} catch (Exception exp) {
				String msg = "Couldn't upload myapplication file [" + fileContent.getName() + "] to DB";
				logger.warn(msg, exp);
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, msg, exp);
			}
		}
	}

}
