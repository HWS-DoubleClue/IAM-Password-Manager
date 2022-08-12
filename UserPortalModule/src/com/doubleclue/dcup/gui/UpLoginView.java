package com.doubleclue.dcup.gui;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.gui.LoginViewAbstract;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.DcupConstants;

@SuppressWarnings("serial")
@Named("upLoginView")
@SessionScoped
public class UpLoginView extends LoginViewAbstract {

	private static Logger logger = LogManager.getLogger(UpLoginView.class);

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	AppServices appServices;

	@Inject
	UserLogic userLogic;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AsModule asModule;

	@Inject
	AsMessageHandler messageHandler;

	@Inject
	AdminModule adminModule;

	@Inject
	RegisterView registerView;

	@Inject
	KeePassView keePassView;

	@Inject
	PasswordSafeView passwordSafeView;

	private String latestView;
	private String flag;
	private boolean hideTutorial = false;
	private boolean qrCodeUseState = false;

	private boolean doNotRedirectToTutorial;

	@PostConstruct
	public void init() {
		super.init();
		setAuthApplication(AuthApplication.USER_PORTAL);
	}

	public String actionRequestRegister() {
		// registerView.startConversation();
		return DcupConstants.REGISTER_PAGE + DcemConstants.FACES_REDIRECT;
	}

	public String actionForgotPassword() {
		return DcupConstants.JSF_PAGE_FORGOT_PASSWORD_REQUEST + DcemConstants.FACES_REDIRECT;
	}

	public String actionGotoLogin() {
		// registerView.endConversation();
		return DcupConstants.LOGIN_PAGE + DcemConstants.FACES_REDIRECT;
	}

	public void actionTutorialtoLogin() {
		try {
			doNotRedirectToTutorial = true;
			FacesContext.getCurrentInstance().getExternalContext().redirect("preLogin_.xhtm");
		} catch (IOException e) {
			logger.error("Could not redirect to Login", e);
		}
	}

	public void actionRedirectionToDCEM() {
		try {
			FacesContext.getCurrentInstance().getExternalContext()
					.redirect(DcupConstants.DCEM_PAGE + DcemConstants.FACES_REDIRECT);
		} catch (IOException e) {
			logger.error("Could not redirect to DCEM", e);
		}
	}

	public void actionLocale() {
		super.setNewLocale(portalSessionBean.getLocale());
	}

	@Override
	protected ResourceBundle getResounceBundleModule() {
		return JsfUtils.getBundle(UserPortalModule.RESOURCE_NAME, portalSessionBean.getLocale());
	}

	@Override
	public void actionLogin() {
		super.actionLogin();
	}

	@Override
	protected void finishLogin() {
		try {
			super.finishLogin();
			if (logger.isDebugEnabled()) {
				logger.debug("PortalUser login " + userLoginId + " from " + JsfUtils.getRemoteIpAddress());
			}
			portalSessionBean.setDcemUser(dcemUser);
			portalSessionBean.setLoggedIn(true);
			portalSessionBean.setAuthMethod(super.availableAuthMethods.get(0));

			ExternalContext ec = JsfUtils.getExternalContext();
			((HttpServletRequest) ec.getRequest()).changeSessionId(); // avoid session hijacking
			if (latestView != null && latestView.isEmpty() == false) {
				portalSessionBean.setLatestView(latestView);
//					DcupViewEnum dcupViewEnum = DcupViewEnum.valueOf(latestView);
//					portalSessionBean.gotoView(latestView);
//					portalSessionBean.changeCurrentIndex(dcupViewEnum.ordinal());
			}
			ec.redirect(ec.getApplicationContextPath() + DcupConstants.WEB_USER_PORTAL_CONTEXT + "/"
					+ DcupConstants.HTML_PAGE_USERSTORAGE);
		} catch (Exception e) {
			logger.warn("finishlogin()", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public String usedLanguageFlag() {
		switch (portalSessionBean.getLocale().toString()) {
		case "de":
			flag = "icons/16x16/germany.png";
			break;
		case "it":
			flag = "icons/16x16/italy.png";
			break;
		case "fr":
			flag = "icons/16x16/france.png";
			break;
		case "en":
			flag = "icons/16x16/uk.png";
			break;
		default:
			flag = "icons/16x16/uk.png";
			break;
		}
		return flag;
	}

	public String getLatestView() {
		return latestView;
	}

	public void setLatestView(String latestView) {
		this.latestView = latestView;
	}

	public String getFlag() {
		return usedLanguageFlag();
	}

	@Override
	public void showChangePassword() {
		loginPanelRendered = false;
		passwordPanelRendered = true;
		PrimeFaces.current().ajax().update("loginForm");
	}

	public boolean isHideTutorial() {
		return hideTutorial;
	}

	public void setHideTutorial(boolean hideTutorial) {
		this.hideTutorial = hideTutorial;
	}

	@Override
	public String actionPreLoginOk() {

		String page = super.actionPreLoginOk();
		System.out.println("UpLoginView.actionPreLoginOk() " + page);
		if (page == null) {
			page = DcupConstants.HTML_PAGE_USERSTORAGE;
		}
		return page;
	}

	public boolean isQrCodeUseState() {
		return qrCodeUseState;
	}

	public void setQrCodeUseState(boolean qrCodeUseState) {
		this.qrCodeUseState = qrCodeUseState;
	}

	public Integer getUserId() {
		if (portalSessionBean.getDcemUser() == null) {
			return 0;
		}
		return portalSessionBean.getDcemUser().getId();
	}

	public void clearCache() {
		PrimeFaces.current().executeScript("localStorage.clear();");
	}

}
