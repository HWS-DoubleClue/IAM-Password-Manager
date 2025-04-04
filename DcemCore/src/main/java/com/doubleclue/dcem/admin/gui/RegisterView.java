package com.doubleclue.dcem.admin.gui;

import java.io.IOException;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.NotificationType;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.admin.preferences.AdminPreferences;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.StringUtils;

@Named("registerView")
@SessionScoped
public class RegisterView extends DcemView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Inject
	UserLogic userLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AdminModule adminModule;

	@Inject
	UrlTokenLogic urlTokenLogic;

	
	@Inject
	DcemApplicationBean applicationBean;
	
//	String startedFrom;

	private static Logger logger = LogManager.getLogger(RegisterView.class);

	private String userId;
	private String displayname;
	private String password;
	private String mail;
	private String mobile = "";
	private String phone = "";
	private String privateMobileNumber;
	private LocalDateTime activationValidTill;
	// private String sendBy = "email";
	private NotificationType notificationType;
//	private String domainName;
//	private String recoveryKey;
//	private String userLoginName;
	private String activationCode = "";
	UrlTokenEntity urlTokenEntity;
//	boolean managementLogin;
	ResourceBundle resourceBundle;
	DcemUser dcemUser;
	private AsModuleApi asModuleApi;

	@PostConstruct
	public void init() {
		try {
			notificationType = adminModule.getPreferences().getNotificationType();
			asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
			if (asModuleApi != null) {
				activationValidTill = asModuleApi.getActivationCodeDefaultValidTill();
			}
			resourceBundle = ResourceBundle.getBundle(AdminModule.RESOURCE_NAME, JsfUtils.getLocale());
		} catch (Exception exp) {
			logger.warn("RegisterView.init()", exp);
		}
	}
	
	public String actionGotoLogin() {
		if (urlTokenEntity != null) {
			urlTokenLogic.deleteUrlToken(urlTokenEntity);
		}
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		session.invalidate();
		return "/mgt/login.xhtml" + DcemConstants.FACES_REDIRECT;
	}
	
	public void  preRenderViewInputs () {
		if (userId == null || userId.isBlank()) {
			ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
			try {
				extCon.redirect("/mgt/login.xhtml" + DcemConstants.FACES_REDIRECT);
			} catch (IOException e) {
				logger.error(e);
			}
		}		
	}

	@PreDestroy
	public void destroy() {
		// System.out.println("RegisterView.destroy()");
	}

	public String actionRegisterLocalUser() {
		AdminPreferences preferences = adminModule.getPreferences();
		try {
			if (validator()) {
				DcemUser dcemUser = new DcemUser();
				dcemUser.setLoginId(userId);
				dcemUser.setInitialPassword(password);
				dcemUser.setDisplayName(displayname);
				dcemUser.setEmail(mail);
				dcemUser.setMobileNumber(mobile);
				dcemUser.setTelephoneNumber(phone);
				dcemUser.setDisabled(true);
				dcemUser.setPrivateMobileNumber(privateMobileNumber);
				dcemUser.setLanguage(SupportedLanguage.fromLocale(JsfUtils.getLocale()));
				String url = applicationBean.getDcemManagementUrl(TenantIdResolver.getCurrentTenantName()) + "/" +  DcemConstants.VERIFICATION_SERVLET_PATH + "?token=";
				userLogic.registerUser(dcemUser, url, preferences.getUrlTokenTimeout());
				return "successRegistrationView_.xhtml" + DcemConstants.FACES_REDIRECT;
			}
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.warn("", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
		return null;

	}

	private boolean validator() {
		Pattern pattern = Pattern.compile(DcemConstants.regxTelefonNumber);
		if (mobile == null) {
			mobile = "";
		}
		if (phone == null) {
			phone = "";
		}
		Matcher matcher = pattern.matcher(mobile);
		Matcher matcherPhone = pattern.matcher(phone);
		if (userId == null || userId.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "error.MISSING_USERNAME");
			return false;
		}
		userId = userId.trim();
		password = password.trim();
		mail = mail.trim();
		if (!KaraUtils.isEmailValid(mail)) {
			JsfUtils.addErrorMessage(resourceBundle,"error.INVALID_EMAIL");
			return false;
		}
		if (mobile != null && mobile.isEmpty() == false) {
			if (!matcher.matches()) {
				JsfUtils.addErrorMessage(resourceBundle, "error.INVALID_MOBILE_NUMBER");
				return false;
			}
		}
		if (phone != null && phone.isEmpty() == false) {
			if (!matcherPhone.matches()) {
				JsfUtils.addErrorMessage(resourceBundle, "error.INVALID_PHONE_NUMBER");
				return false;
			}
		}
		if (privateMobileNumber != null && privateMobileNumber.isEmpty() == false) {
			Matcher matcherPrv = pattern.matcher(privateMobileNumber);
			if (!matcherPrv.matches()) {
				JsfUtils.addErrorMessage(resourceBundle, "error.INVALID_PRIVATE_MOBILE_NUMBER");
				return false;
			}
		}
		return true;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

//	private SupportedLanguage getSupportedLanguage() {
//		Locale locale = portalSessionBean.getLocale();
//		return SupportedLanguage.fromLocale(locale);
//	}

	public String getPrivateMobileNumber() {
		return privateMobileNumber;
	}

	public void setPrivateMobileNumber(String privateMobileNumber) {
		this.privateMobileNumber = privateMobileNumber;
	}

	public LocalDateTime getActivationValidTill() {
		return activationValidTill;
	}

	public void setActivationValidTill(LocalDateTime activationValidTill) {
		this.activationValidTill = activationValidTill;
	}

	
	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public String actionUserId() {
		try {
			userId = userId.trim();
			verifyRegistrationUser(userId);
//			if (domainName == null) {
//				return DcupConstants.REGISTER_LOCAL_USER;
//			} else {
				return "registerLocalUser_.xhtml";
//			}
		} catch (DcemException ex) {
			logger.warn(ex);
			JsfUtils.addErrorMessage(ex.getLocalizedMessage());
		} catch (Exception ex) {
			logger.warn(ex);
			if (ex.getCause() != null && ex.getCause() instanceof ConnectException) {
				JsfUtils.addErrorMessage(resourceBundle, "error.noconnection");
				return null;
			}
			JsfUtils.addErrorMessage(ex.toString());
		}
		return null;
	}

	public String actionBackToRegistration() {
		return "register_.xhtml";
	}
	


	/**
	 * @param userLoginId
	 * @return
	 * @throws DcemException
	 */
	private void verifyRegistrationUser(String userLoginId) throws DcemException {
		if ((StringUtils.isValidNameId(userLoginId) == false) || userLoginId.contains(DcemConstants.DOMAIN_SEPERATOR)) {
			throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, userLoginId);
		}
		DcemUser dcemUser = userLogic.getUser(userLoginId);
		if (dcemUser != null) {
			throw new DcemException(DcemErrorCodes.REGISTRATION_USER_ALREADY_EXISTS, userLoginId);
		}
		return;
	}

	public boolean isNotificationType() {
		return adminModule.getPreferences().getNotificationType() != NotificationType.NONE;
	}

	public void actionSendEmailWithActivationCode() {
// TODO		DcemUser dcemUser = portalSessionBean.getDcemUser();
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(resourceBundle, "registration.noUserRegsitered");
			return;
		} else {
			try {
				asModuleApi.createActivationCode(dcemUser, activationValidTill, SendByEnum.EMAIL, "");
				JsfUtils.addInfoMessage(resourceBundle, "registration.info.email");
			} catch (DcemException ex) {
				logger.info(ex);
				JsfUtils.addErrorMessage(ex.getLocalizedMessage());
			} catch (Exception ex) {
				logger.warn(ex);
				JsfUtils.addErrorMessage(ex.toString());
			}
		}
	}

	public void actionSendSmsWithActivationCode() {
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(resourceBundle, "registration.noUserRegsitered");
			return;
		} else {
			try {
				asModuleApi.createActivationCode(dcemUser, activationValidTill, SendByEnum.SMS, "");
				JsfUtils.addInfoMessage(resourceBundle, "registration.info.sms");
			} catch (DcemException ex) {
				logger.info(ex);
				JsfUtils.addErrorMessage(ex.getLocalizedMessage());
			} catch (Exception ex) {
				logger.warn(ex);
				JsfUtils.addErrorMessage(ex.toString());
			}
		}
	}

	public boolean isEmailNotificationTypeSelected() {
		if (notificationType == NotificationType.EMAIL || notificationType == NotificationType.BOTH)
			return true;
		else
			return false;
	}

	public boolean isSmsNotificationTypeSelected() {
		if (notificationType == NotificationType.SMS || notificationType == NotificationType.BOTH)
			return true;
		else
			return false;
	}

//	public String startConversation() {
//		try {
//			if (conversation.isTransient()) {
//				conversation.begin();
//				conversation.setTimeout(adminModule.getPreferences().getInactivityTimer() * (60 * 1000));
//			}
//			return conversation.getId();
//		} catch (Exception exp) {
//			logger.debug(exp);
//		}
//		return null;
//	}

//	public void endConversation() {
//		try {
//			if (conversation.isTransient() == false) {
//				conversation.end();
//			}
//		} catch (Exception exp) {
//			logger.debug(exp);
//		}
//		return;
//	}
	
	public boolean isRegisterViewVisible () {
		return adminModule.getPreferences().isEnableCreateAccount();
	}
	
	public String actionRequestRegister() {
		return "register_.xhtml" + DcemConstants.FACES_REDIRECT;
	}

//	public String getRecoveryKey() {
//		if (recoveryKey != null) {
//			return recoveryKey;
//		}
//		if (dcemUser != null) {
//			// creating the recovery key
//			CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.USER, dcemUser, null, DcemConstants.RECOVERY_KEY, null,
//					CloudSafeOptions.ENC.name(), false, null);
//			cloudSafeEntity.setGcm(true);
//			byte[] recoverKey;
//			try {
//				recoverKey = cloudSafeLogic.getContentAsBytes(cloudSafeEntity, null, null);
//				recoveryKey = new String(recoverKey, DcemConstants.UTF_8);
//			} catch (DcemException e) {
//				e.printStackTrace();
//				recoveryKey = "Not Available";
//			}
//		}
//		return recoveryKey;
//	}

	public String getActivationCode() {
		return activationCode;
	}

	public void displayActivationCode() {
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(resourceBundle, "registration.noUserRegsitered");
			return;
		} else {
			this.activationCode = asModuleApi.requestActivationCode(dcemUser);
			PrimeFaces.current().ajax().update("notificationForm:activationCodeLabel");
		}
	}

	public UrlTokenEntity getUrlTokenEntity() {
		return urlTokenEntity;
	}

	public void setUrlTokenEntity(UrlTokenEntity urlTokenEntity) {
		this.urlTokenEntity = urlTokenEntity;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

}
