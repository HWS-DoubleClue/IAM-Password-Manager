package com.doubleclue.dcem.saml.sso.gui;

import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.gui.LoginViewAbstract;
import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.sso.logic.SamlSsoLogic;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlLoginView")
public class SamlLoginView extends LoginViewAbstract {

	@Inject
	AsMessageHandler messageHandler;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	UserLogic userLogic;

	@Inject
	SamlSsoLogic ssoServiceLogic;

	@Inject
	SamlModule samlModule;

	@Inject
	AuthenticationLogic authLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	SamlSsoLogic samlSsoLogic;

	boolean deviceWizard;

	@PostConstruct
	public void init() {
		super.init();
		setAuthApplication(AuthApplication.SAML);
	}

	@Override
	public void onPreRenderView() {
		super.onPreRenderView();
		SamlSpMetadataEntity metadata = ssoServiceLogic.getMetadata();
		setApplicationSubId(metadata == null ? 0 : metadata.getId());
	}

	private String tenant;

	public boolean isPasswordRequired() {
		return samlModule.getModulePreferences().isPasswordRequired();
	}

	public void actionLogin() {
		if (isTenants()) {
			setTenantResolver();
		}
		deviceWizard = false;
		ssoServiceLogic.onSpChanged();
		if (ssoServiceLogic.getMetadata() == null) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, ssoServiceLogic.getDbResourceBundle(), "sso.error.missingSP", null, null);
			return;
		} else {
			super.actionLogin();
			if (super.lastException != null) {
				if (lastException.getErrorCode() == DcemErrorCodes.USER_HAS_NO_DEVICES && samlModule.getModulePreferences().isEnableDeviceWizard()) {
					deviceWizard = true;
				}
			}
		}
		return;
	}

	public String actionPreLoginOk() {
		if (samlSsoLogic.getAuthnRequest() == null) {
			return SamlConstants.JSF_PAGE_LOGIN;
		}
		return super.actionPreLoginOk();
	}

	@Override
	protected void finishLogin() throws DcemException {
		super.finishLogin();
		try {
			if (ssoServiceLogic.getMetadata() == null) {
				JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, ssoServiceLogic.getDbResourceBundle(), "sso.error.missingSP", null, null);
				return;
			}
			loggedIn = true;
			ExternalContext ec = JsfUtils.getExternalContext();
			((HttpServletRequest) ec.getRequest()).changeSessionId(); // avoid session hijacking
			ssoServiceLogic.setupResponse(authResponse);
			ssoServiceLogic.redirectToPage(SamlConstants.JSF_PAGE_RETURN_TO_SP);
		} catch (Exception e) {
			logger.warn("SAML finishLogin", e);
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, ssoServiceLogic.getDbResourceBundle(), "sso.error.authFailed", null, null);
		}
	}

	public String getAuthMethodName(AuthMethod method) {
		return JsfUtils.getMessageFromBundle(ssoServiceLogic.getDbResourceBundle(), "sso.login.authChoice.method." + method.getAbbreviation());
	}

	public String getTenant() {
		if (tenant == null && TenantIdResolver.getCurrentTenantName() != "master") {
			tenant = TenantIdResolver.getCurrentTenantName();
		}
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public boolean isTenants() {
		return (ssoServiceLogic.getAuthnRequest() == null && applicationBean.isMultiTenant());
	}

	public void onTenantChange() {
		if (tenant == null || tenant.isEmpty()) {
			setTenantResolver();
			return; // assume master;
		}
		if (applicationBean.getTenant(tenant) == null) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, ssoServiceLogic.getDbResourceBundle(), "sso.error.invalidTenant", null, null);
			return;
		}
		setTenantResolver();
	}

	public void onSpChange() {
		ssoServiceLogic.onSpChanged();
	}

	public Set<String> getSpList() {
		if (setTenantResolver() == false) {
			return null;
		}
		return ssoServiceLogic.getSpList();
	}

	private boolean setTenantResolver() {
		if (tenant == null || tenant.isEmpty()) {
			TenantIdResolver.setCurrentTenant(null);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(DcemConstants.URL_TENANT_PARAMETER, TenantIdResolver.getCurrentTenant());
		} else {
			TenantEntity tenantEntity = applicationBean.getTenant(tenant);
			if (tenantEntity == null) {
				return false;
			}
			TenantIdResolver.setCurrentTenant(tenantEntity);
			FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(DcemConstants.URL_TENANT_PARAMETER, tenantEntity);
		}
		return true;
	}

	public boolean isDeviceWizard() {
		return deviceWizard;
	}

	public void setDeviceWizard(boolean deviceWizard) {
		this.deviceWizard = deviceWizard;
	}

	@Override
	public void showChangePassword() {
		loginPanelRendered = false;
		passwordPanelRendered = true;
		PrimeFaces.current().ajax().update("loginForm");		
	}
}
