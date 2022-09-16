package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CloseEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcem.userportal.preferences.UserPortalPreferences;
import com.doubleclue.dcup.logic.ActionItem;
import com.doubleclue.dcup.logic.DcupConstants;
import com.doubleclue.dcup.logic.ViewItem;
import com.doubleclue.utils.ResourceBundleUtf8Control;

@Named("portalSessionBean")
@SessionScoped
public class PortalSessionBean implements Serializable {

	private static Logger logger = LogManager.getLogger(PortalSessionBean.class);

	@Inject
	private PasswordSafeView passwordSafeView;

	@Inject
	private KeePassView keePassView;

	@Inject
	private UserPortalModule userPortalModule;

	@Inject
	private AdminModule adminModule;

	@Inject
	UserLogic userLogic;

	@Inject
	RoleLogic roleLogic;

	private AbstractPortalView activePortalView;
	private int viewIndex;
	private String deviceName;
	private boolean loggedIn;
	private boolean twoFactorLogin;
	private AuthMethod authMethod;
	private ResourceBundle resourceBundle;
	private String loginPageLanguage;
	private int currentPreferencesVersion;
	private String latestView = null;;
	private int currentTab;
	String passwordSafeHistory;

	private static final long serialVersionUID = -1042847880204878575L;
	private DcemUser dcemUser;

	@PostConstruct
	public void init() {
		UserPortalPreferences config = userPortalModule.getModulePreferences();
		currentPreferencesVersion = config.getVersion();
	}

	public String getViewPath() {
		if (activePortalView == null) {
			activePortalView = CdiUtils.getReference(PasswordSafeView.class);
		}
		return activePortalView.getPath();
	}

	public boolean isAddDeviceAction() {
		return (isActionEnable(ActionItem.OTP_ADD_ACTION) || isActionEnable(ActionItem.FIDO_ADD_ACTION)
				|| isActionEnable(ActionItem.NETWORK_DEVICE_ADD_ACTION));
	}

	public void actionReload() {
		activePortalView.onView();
	}

	public void gotoView(String viewName) {
		if (activePortalView != null) {
			activePortalView.onExit();
		}
		try {
			activePortalView = CdiUtils.getReference(viewName);
		} catch (Exception exp) {
			logger.error("Wrong View Name:" + viewName, exp);
			activePortalView = CdiUtils.getReference(PasswordSafeView.class);
		}
		activePortalView.onView();
		// if (viewName.equals("cloudSafeView.xhtml")) {
		// cloudSafeView.reload();
		// }
		// if (viewName.endsWith("xhtml")) {
		// activeViewPath = viewName;
		// } else if (viewName.equals("addDevice")) {
		// activeViewPath = viewName;
		// } else {
		// activeViewPath = new WelcomeTabView().getPath();
		// }
		PrimeFaces.current().executeScript("setLocalStorageValue('latestView', '" + activePortalView.getName() + "')");
	}

	public String userProfile() {
		return "userProfileView.xhtml";
	}

	public String changePassword() {
		return "changePasswordView.xhtml";
	}

	public boolean isMessages() {
		return JsfUtils.isMessages();
	}

	public int getViewIndex() {
		return viewIndex;
	}

	public void setViewIndex(int viewIndex) {
		this.viewIndex = viewIndex;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public boolean isUserLoggedInAndEnabled() {
		if (loggedIn == false || dcemUser == null) {
			return false;
		}
		try {
			// This will be cached by Hazelcast
			dcemUser = userLogic.getUser(dcemUser.getId());
			return userLogic.isUserEnabled(dcemUser) == 0;
		} catch (Exception e) {
			loggedIn = false;
			dcemUser = null;
			logger.info("Couldn't find user " + dcemUser.getLoginId());
			throw e;
		}
	}

	public DcemUser updateUser() {
		dcemUser = userLogic.getUser(dcemUser.getId());
		return dcemUser;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		FacesContext fc = FacesContext.getCurrentInstance();
		if (fc != null) {
			ExternalContext extCon = fc.getExternalContext();
			HttpSession session = (HttpSession) extCon.getSession(false);
			session.setMaxInactiveInterval(adminModule.getPreferences().getInactivityTimer() * 60);
		}
		this.loggedIn = loggedIn;
	}

	public boolean isTwoFactorLogin() {
		if (authMethod == null || authMethod == AuthMethod.PASSWORD) {
			twoFactorLogin = false;
		} else {
			twoFactorLogin = true;
		}
		return twoFactorLogin;
	}

	public void setTwoFactorLogin(boolean twoFactorLogin) {
		this.twoFactorLogin = twoFactorLogin;
	}

	public String changeLanguage(String locale) {
		FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());
		setResourceBundle(ResourceBundle.getBundle(UserPortalModule.RESOURCE_NAME, getLocale(), new ResourceBundleUtf8Control()));
		return locale;
	}

	public ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			initResources();
		}
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	public boolean isViewVisible(ViewItem menuItem) {
		boolean visible = false;
		UserPortalPreferences config = userPortalModule.getModulePreferences();
		if (config != null) {
			visible = config.isViewVisible(menuItem);
		}
		if (config.getVersion() != currentPreferencesVersion) {
			PrimeFaces.current().executeScript("$(document).ready(function() { window.location.reload(); });");
			currentPreferencesVersion = config.getVersion();
		}
		return visible;
	}

	public boolean isViewVisible(String menuItemStr) {
		try {
			ViewItem viewItems = ViewItem.valueOf(menuItemStr);
			return isViewVisible(viewItems);
		} catch (Exception e) {
			logger.warn(e);
			return false;
		}
	}

	public boolean isActionEnable(String menuItemStr) {
		try {
			ActionItem menuItem = ActionItem.valueOf(menuItemStr);
			return isActionEnable(menuItem);
		} catch (Exception e) {
			logger.warn(e);
			return false;
		}
	}

	public boolean isActionEnable(ActionItem menuItem) {
		boolean enabled = false;
		UserPortalPreferences config = userPortalModule.getModulePreferences();
		if (config != null) {
			enabled = config.isActionVisible(menuItem);
		}
		return enabled;
	}

	public boolean isDeviceViewVisible() {
		return isViewVisible(ViewItem.FIDO_VIEW.toString()) || isViewVisible(ViewItem.OTP_TOKEN_VIEW.toString())
				|| isViewVisible(ViewItem.NETWORK_DEVICE_VIEW.toString());
	}

	public String getErrorMessage(DcemException exception) {
		String message = exception.getErrorCode().name();
		try {
			message = getResourceBundle().getString("error." + exception.getErrorCode().name());
		} catch (MissingResourceException exp) {
			message = exception.getLocalizedMessage();
		} catch (Exception e) {
			logger.warn("No Resource found for: " + exception.getMessage());
			return exception.toString();
		}
		return message;
	}

	public String getBundleStringFormat(String path, Object... parameters) {

		try {
			MessageFormat msg = new MessageFormat(resourceBundle.getString(path));
			return msg.format(parameters);
		} catch (Exception e) {
			logger.warn("No Resource found for: " + e.getLocalizedMessage());
			return path;
		}
	}

	public Locale getLocale() {
		if (dcemUser != null) {
			return dcemUser.getLanguage().getLocale();
		}
		if (loginPageLanguage != null && loginPageLanguage != "") {
			return SupportedLanguage.toLocale(loginPageLanguage);
		}
		try {
			loginPageLanguage = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getDisplayLanguage();
			return SupportedLanguage.toLocale(loginPageLanguage);
		} catch (Exception exp) {
			return null;
		}
	}

	public String getDateTimePattern() {
		DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, getLocale());
		return ((SimpleDateFormat) formatter).toPattern();
	}

	public String getLocaleStr() {
		return getLocale().getLanguage();
	}

	private void initResources() {
		try {
			setResourceBundle(ResourceBundle.getBundle(UserPortalModule.RESOURCE_NAME, getLocale(), new ResourceBundleUtf8Control()));
			if (FacesContext.getCurrentInstance() != null) {
				FacesContext.getCurrentInstance().getViewRoot().setLocale(getLocale());
			}
		} catch (Exception e) {
			logger.info(e);
		}
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
		initResources();
	}

	public String actionUserStorage() {
		keePassView.setPsHistory(passwordSafeHistory);
		passwordSafeView.setPsHistory(passwordSafeHistory);
		if (latestView != null) {
			DcupViewEnum dcupViewEnum;
			try {
				dcupViewEnum = DcupViewEnum.valueOf(latestView);
			} catch (Exception e) {
				return DcupConstants.WELCOME_PAGE + DcemConstants.FACES_REDIRECT;
			}
			gotoView(latestView);
			changeCurrentIndex(dcupViewEnum.ordinal());
		}
		return DcupConstants.WELCOME_PAGE + DcemConstants.FACES_REDIRECT;
	}

	public String getUserName() {
		return dcemUser.getLoginId();
	}

	public String getUserLoginId() {
		if (dcemUser == null) {
			return null;
		}
		return dcemUser.getLoginId().replace("\\", "\\\\");
	}

	public String getLoginPageLanguage() {
		return loginPageLanguage;
	}

	public void setLoginPageLanguage(String loginPageLanguage) {
		this.loginPageLanguage = loginPageLanguage;
	}

	public AuthMethod getAuthMethod() {
		return authMethod;
	}

	public void setAuthMethod(AuthMethod authMethod) {
		this.authMethod = authMethod;
	}

	public boolean isUserRoleManagment() {
		if (dcemUser == null) {
			return false;
		}
		return (dcemUser.getDcemRole().getRank() > 0);
	}

	public void changeCurrentIndex(int currentTab) {
		this.currentTab = currentTab;
	}

	public int getCurrentTab() {
		PrimeFaces.current().ajax().update("menuForm:devicesID");
		return currentTab;
	}

	public void setCurrentTab(int currentTab) {
		this.currentTab = currentTab;
	}

	public String getPasswordSafeHistory() {
		return passwordSafeHistory;
	}

	public void setPasswordSafeHistory(String passwordSafeHistory) {
		this.passwordSafeHistory = passwordSafeHistory;
	}

	public StreamedContent getFotoProfileUser() {
		DcemUserExtension dcemUserExtension = dcemUser.getDcemUserExt();
		if (dcemUserExtension != null && dcemUserExtension.getPhoto() != null) {
			byte[] image = dcemUserExtension.getPhoto();
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public String getLatestView() {
		return latestView;
	}

	public void setLatestView(String latestView) {
		this.latestView = latestView;
	}
}