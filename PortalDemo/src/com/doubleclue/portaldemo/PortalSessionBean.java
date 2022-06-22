package com.doubleclue.portaldemo;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.PrimeFaces;

import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.portaldemo.gui.WelcomeTabView;
import com.doubleclue.portaldemo.utils.JsfUtils;
import com.doubleclue.portaldemo.utils.PortalUtils;

@Named("portalSessionBean")
@SessionScoped
public class PortalSessionBean implements Serializable {

	public PortalSessionBean() {
		super();
	}

	@PostConstruct
	public void init() {
	}

	private AbstractPortalView activeView;
	private String activeViewPath;
	private int viewIndex;
	private String userName;
	private String deviceName;
	private boolean loggedIn;
	private ResourceBundle resourceBundle;
	private static final long serialVersionUID = -1042847880204878575L;

	public boolean isActiveView(String name) {
		if (activeView != null && activeView.getName().equals(name)) {
			return true;
		} else {
			return false;
		}
	}

	public String getViewPath() {
		if (activeView != null) {
			return activeView.getPath();

		} else {
			if (activeViewPath != null) {
				return activeViewPath;
			}
			activeView = new WelcomeTabView();
			return activeView.getPath();
		}

	}

	public void gotoView(String viewName) {
		System.out.println("ViewNavigator.gotoView() " + viewName);
		if (viewName.endsWith("xhtml")) {
			activeView = null;
			activeViewPath = viewName;
			return;
		}
		try {
			activeView = PortalUtils.getReference(viewName);
		} catch (Exception e) {
			activeView = null;
			activeViewPath = viewName;
		}

		return;
	}

	public void gotoView(AbstractPortalView portalView) {
		activeView = portalView;
		PrimeFaces.current().ajax().update("viewPart");
		return;
	}

	public AbstractPortalView getActiveView() {
		return activeView;
	}

	public void setActiveView(AbstractPortalView portalView) {
		activeView = portalView;
	}

	public String logoff() {
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		session.invalidate();
		return "portalLogin.xhtml";
	}

	public boolean isMessages() {
		System.out.println("ViewNavigator.isMessages() " + JsfUtils.isMessages());
		return JsfUtils.isMessages();
	}

	public int getViewIndex() {
		return viewIndex;
	}

	public void setViewIndex(int viewIndex) {
		this.viewIndex = viewIndex;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getWelcomeText() {
		if (loggedIn) {
			String text = "Welcome!  You are logged in as '" + userName + "'";
			if (deviceName != null) {
				text = text + " from device '" + deviceName + "'";
			}
			return text;
		} else {
			return null;
		}
	}

	public String getErrorMessage(DcemApiException exception) {
		if (resourceBundle == null) {
			resourceBundle = JsfUtils.getBundle(PortalDemoConstants.PD_RESOURCE);
		}
		String message = exception.getMessage();
		try {
			message = resourceBundle.getString("error." + exception.getMessage());
		} catch (Exception e) {
			return exception.getMessage();
		}
		return message;
	}
}
