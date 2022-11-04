package com.doubleclue.dcem.system.gui;

import java.io.IOException;
import java.util.TimeZone;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.RandomUtils;

@SuppressWarnings("serial")
@Named("tenantDialog")
@SessionScoped
public class TenantDialog extends DcemDialog {

	@Inject
	TenantLogic tenantLogic;

	@Inject
	SystemModule adminModule;

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	DcemApplicationBean applicationBean;
	
	@Inject
	AuditingLogic auditingLogic;

	private static final Logger logger = LogManager.getLogger(TenantDialog.class);

	// private String dbAdmin;
	// private String dbPassword;
	private String superAdminPassword;
	private String superAdminPhone;
	private String superAdminEmail;
	private SendByEnum superAdminSendBy = SendByEnum.EMAIL;
	private boolean embeddedDb = LocalConfigProvider.getLocalConfig().getDatabase().getDatabaseType().equals(DatabaseTypes.DERBY.name());
	private boolean actionSuccessful = false;
	private boolean createActivationCode = false;
	private boolean sendPasswordBySms = false;
	private String tenantCreatedMessage;
	SupportedLanguage language;

	public boolean actionOk() throws Exception {
		if (!actionSuccessful) {
			if (createActivationCode == true) {
				if (superAdminPhone != null) {
					superAdminPhone = superAdminPhone.trim();
				}

				superAdminEmail = superAdminEmail.trim();
				superAdminPassword = superAdminPassword.trim();

				if (superAdminSendBy != null) {
					switch (superAdminSendBy) {
					case EMAIL:
						if (superAdminEmail == null || superAdminEmail.isEmpty()) {
							JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "tenant.error.emailMissing");
							return false;
						}
						if (adminModule.getPreferences().geteMailHostPort() == 0 || adminModule.getPreferences().geteMailHostAddress().length() < 4) {
							JsfUtils.addErrorMessage("Email is not configured yet. Please configure email in System-Preferences.");
							return false;
						}
						break;
					case SMS:
						if (superAdminPhone == null || superAdminPhone.isEmpty()) {
							JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "tenant.error.phoneMissing");
							return false;
						}
						if (adminModule.getPreferences().getSmsProviderAccesKey().isEmpty() == false) {
							if (adminModule.getPreferences().getSmsOriginatorName().isEmpty()) {
								throw new DcemException(DcemErrorCodes.SMS_INVALID_CONFIGURATION,
										"SMS is not configured yet. Please configure sms in System-Preferences.");
							}
						}
						break;
					default:
						break;
					}
				}
				if (sendPasswordBySms == true && (superAdminPhone == null || superAdminPhone.isEmpty())) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "tenant.error.phoneMissing");
					return false;
				}
			}
			TenantEntity tenantEntity = (TenantEntity) this.getActionObject();
			tenantLogic.addOrUpdateTenant(tenantEntity, this.getAutoViewAction().getDcemAction(), superAdminPassword, superAdminPhone, superAdminEmail,
					language, null, null, TimeZone.getTimeZone("Europe/Berlin"), true);
			AsModuleApi asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
			if (this.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
				asModuleApi.onCreateTenant(tenantEntity);
			}
			Exception exception = DcemUtils.reloadTaskNodes(TenantLogic.class);
			if (exception != null) {
				logger.warn("Couldn't reloadRaskNodes", exception);
				JsfUtils.addErrorMessage(exception.toString());
				return false;
			}
			if (createActivationCode == true) {
				try {
					String superAdminActivationCode = asModuleApi.onCreateActivationCodeTenant(tenantEntity, superAdminEmail, superAdminPhone, superAdminSendBy,
							language, sendPasswordBySms, superAdminPassword, null, false);
					actionSuccessful = true;
					if (superAdminActivationCode != null) {
						if (this.getAutoViewAction().getDcemAction().equals(DcemConstants.ACTION_ADD)) {
							tenantCreatedMessage = JsfUtils.getMessageFromBundle(DcemConstants.CORE_RESOURCE, "tenant.tenantCreated", tenantEntity.getName(),
									superAdminActivationCode);
						} else {
							tenantCreatedMessage = JsfUtils.getMessageFromBundle(DcemConstants.CORE_RESOURCE, "tenant.tenantEdited", tenantEntity.getName(),
									superAdminActivationCode);
						}
						return false;
					} else {
						return true;
					}
				} catch (Exception exp) {
					logger.warn(exp);
					throw new DcemException(DcemErrorCodes.EXCEPTION, "Failed to create Tenant and Activation Code", exp);
				}
			}
			if (sendPasswordBySms == true) {

			}
		}
		return true;
	}

	@Override
	public void actionConfirm() throws Exception {
		super.actionConfirm();
		if (JsfUtils.getMaximumSeverity() <= 0) {
			try {
				DcemUtils.reloadTaskNodes(TenantLogic.class);
			} catch (Exception e) {
				logger.warn("Couldn't reloadRaskNodes", e);
				JsfUtils.addErrorMessage(e.toString());
				return;
			}
		}
	}

	@Override
	public void leavingDialog() {
		superAdminPassword = null;
		superAdminPhone = null;
		superAdminEmail = null;
		superAdminSendBy = null;
		actionSuccessful = false;
		createActivationCode = false;
	}

	public void actionRecoverSuperAdminAccess() {
		if (actionSuccessful) {
			viewNavigator.getActiveView().closeDialog();
		} else {
			TenantEntity tenantEntity = (TenantEntity) this.getActionObject();
			try {
				tenantLogic.recoverSuperAdminAccess(tenantEntity, this.getAutoViewAction().getDcemAction(), superAdminPassword.trim());
				actionSuccessful = true;
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	public String actionSwitchToTenant() {
		TenantEntity tenantEntity = (TenantEntity) this.getActionObject();
		try {
			HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
			request.getSession().invalidate();
			Thread.sleep(400);  // give time to tomcat to deactivate the session
			request.getSession(true);  // create new Session
			request.getSession().setAttribute(DcemConstants.URL_TENANT_PARAMETER, tenantEntity);
			request.getSession().setAttribute(DcemConstants.URL_TENANT_SWITCH, tenantEntity);
			HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
			response.sendRedirect("/dcem/mgt/index.xhtml" );
			auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), tenantEntity.getName());
		//	JsfUtils.getFacesContext().responseComplete();
			return null;
		} catch (Exception e) {
			logger.warn("Coundn't swithc to tenant ", e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		superAdminPassword = RandomUtils.generateRandomAlphaNumericString(10);
		superAdminSendBy = SendByEnum.EMAIL;
		createActivationCode = true;
		sendPasswordBySms = false;
	}

	public String getSuperAdminPassword() {
		return superAdminPassword;
	}

	public void setSuperAdminPassword(String superAdminPassword) {
		this.superAdminPassword = superAdminPassword;
	}

	public String getSuperAdminPhone() {
		return superAdminPhone;
	}

	public void setSuperAdminPhone(String superAdminPhone) {
		this.superAdminPhone = superAdminPhone;
	}

	public String getSuperAdminEmail() {
		return superAdminEmail;
	}

	public void setSuperAdminEmail(String superAdminEmail) {
		this.superAdminEmail = superAdminEmail;
	}

	public SendByEnum getSuperAdminSendBy() {
		return superAdminSendBy;
	}

	public void setSuperAdminSendBy(SendByEnum superAdminSendBy) {
		this.superAdminSendBy = superAdminSendBy;
	}

	public boolean isEmbeddedDb() {
		return embeddedDb;
	}

	public void setEmbeddedDb(boolean embeddedDb) {
		this.embeddedDb = embeddedDb;
	}

	public boolean isActionSuccessful() {
		return actionSuccessful;
	}

	public String getTenantCreatedMessage() {
		return tenantCreatedMessage;
	}

	public SendByEnum[] getSendByValues() {
		return SendByEnum.values();
	}

	public SupportedLanguage getLanguage() {
		return language;
	}

	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

	public boolean isCreateActivationCode() {
		return createActivationCode;
	}

	public void setCreateActivationCode(boolean createActivationCode) {
		this.createActivationCode = createActivationCode;
	}

	public boolean isSendPasswordBySms() {
		return sendPasswordBySms;
	}

	public void setSendPasswordBySms(boolean sendPasswordBySms) {
		this.sendPasswordBySms = sendPasswordBySms;
	}

}
