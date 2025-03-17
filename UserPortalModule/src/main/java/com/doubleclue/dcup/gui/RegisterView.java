package com.doubleclue.dcup.gui;

import java.io.Serializable;
import java.net.ConnectException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcem.userportal.preferences.UserPortalPreferences;
import com.doubleclue.dcup.logic.DcupConstants;
import com.doubleclue.dcup.logic.NotificationType;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.StringUtils;

@Named("registerView")
@SessionScoped
@SuppressWarnings("serial")
public class RegisterView implements Serializable {

	@Inject
	private Conversation conversation;

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	private UserPortalModule userPortalModule;

	@Inject
	DomainLogic domainLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AsModule asModule;

	@Inject
	AdminModule adminModule;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AsActivationLogic asActivationLogic;
	
	String startedFrom;

	private static Logger logger = LogManager.getLogger(RegisterView.class);

	private String userId;
	private String displayname;
	private String password;
	private String mail;
	private String mobile = "";
	private String phone = "";
	private String privateMobileNumber;
	private AsModuleApi asModuleApi;
	private LocalDateTime activationValidTill;
	// private String sendBy = "email";
	private NotificationType notificationType;
	private String domainName;
	private String recoveryKey;
	private String userLoginName;
	private String activationCode = "";
	UrlTokenEntity urlTokenEntity;
	boolean managementLogin;

	@PostConstruct
	public void init() {
		try {
			notificationType = userPortalModule.getModulePreferences().getNotificationType();
			asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
			if (asModuleApi != null) {
				activationValidTill = asModuleApi.getActivationCodeDefaultValidTill();
			}
		} catch (Exception exp) {
			logger.warn("RegisterView.init()", exp);
		}
	}
	
	public void actionStartedFrom () {
		System.out.println("RegisterView.actionStartedFrom() " + startedFrom);
	}

	public String actionGotoLogin() {
		if (urlTokenEntity != null) {
			urlTokenLogic.deleteUrlToken(urlTokenEntity);
		}
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		session.invalidate();
		if (startedFrom != null) {
			return "/mgt/login.xhtml" + DcemConstants.FACES_REDIRECT;
		}
		return DcupConstants.LOGIN_PAGE + DcemConstants.FACES_REDIRECT;
	}

	@PreDestroy
	public void destroy() {
		// System.out.println("RegisterView.destroy()");
	}

	public String actionRegisterLocalUser() {
		UserPortalPreferences preferences = userPortalModule.getModulePreferences();
		try {
			if (!preferences.isEnableLocalUserRegistration()) {
				throw new DcemException(DcemErrorCodes.UNALLOWED_ACTION, " registration as a local user");
			}
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
				dcemUser.setLanguage(getSupportedLanguage());
				recoveryKey = userLogic.registerUser(dcemUser, userPortalModule.getServletUrl(), preferences.getUrlTokenTimeout());
				return DcupConstants.SUCCESS_REGISTRATION_PAGE;
			}
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
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
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_USERNAME"));
			return false;
		}
		userId = userId.trim();
		password = password.trim();
		mail = mail.trim();
		if (!KaraUtils.isEmailValid(mail)) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_EMAIL"));
			return false;
		}
		if (mobile != null && mobile.isEmpty() == false) {
			if (!matcher.matches()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_MOBILE_NUMBER"));
				return false;
			}
		}
		if (phone != null && phone.isEmpty() == false) {
			if (!matcherPhone.matches()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_PHONE_NUMBER"));
				return false;
			}
		}
		if (privateMobileNumber != null && privateMobileNumber.isEmpty() == false) {
			Matcher matcherPrv = pattern.matcher(privateMobileNumber);
			if (!matcherPrv.matches()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_PRIVATE_MOBILE_NUMBER"));
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

	private SupportedLanguage getSupportedLanguage() {
		Locale locale = portalSessionBean.getLocale();
		return SupportedLanguage.fromLocale(locale);
	}

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

	public PortalSessionBean getPortalSessionBean() {
		return portalSessionBean;
	}

	public UserPortalModule getUserPortalModule() {
		return userPortalModule;
	}

	public AsModuleApi getAsModuleApi() {
		return asModuleApi;
	}

	public void setAsModuleApi(AsModuleApi asModuleApi) {
		this.asModuleApi = asModuleApi;
	}

	public UserLogic getUserLogic() {
		return userLogic;
	}

	public void setUserLogic(UserLogic userLogic) {
		this.userLogic = userLogic;
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
			domainName = verifyRegistrationUser(userId);
			if (domainName == null) {
				return DcupConstants.REGISTER_LOCAL_USER;
			} else {
				return DcupConstants.REGISTER_DOM_USER;
			}
		} catch (DcemException ex) {
			logger.warn(ex);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(ex));
		} catch (Exception ex) {
			logger.warn(ex);
			if (ex.getCause() != null && ex.getCause() instanceof ConnectException) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.noconnection"));
				return null;
			}
			JsfUtils.addErrorMessage(ex.toString());
		}
		return null;
	}

	public String actionBackToRegistration() {
		return DcupConstants.REGISTER_PAGE;
	}
	


	/**
	 * @param userLoginId
	 * @return
	 * @throws DcemException
	 */
	private String verifyRegistrationUser(String userLoginId) throws DcemException {
		UserPortalPreferences preferences = userPortalModule.getModulePreferences();
		if ((StringUtils.isValidNameId(userLoginId) == false) || userLoginId.contains(DcemConstants.DOMAIN_SEPERATOR)) {
			throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, userLoginId);
		}
		userLoginName = userId;

		// Local User Registration
		if (preferences.isEnableLocalUserRegistration() == false) {
				throw new DcemException(DcemErrorCodes.LOCAL_USER_REGISTRATION_NOT_ALLOWED, userLoginId);
		}
		DcemUser dcemUser = userLogic.getUser(userLoginId);
		if (dcemUser != null) {
			throw new DcemException(DcemErrorCodes.REGISTRATION_USER_ALREADY_EXISTS, userLoginId);
		}
		return domainName;
	}

	public boolean isNotificationType() {
		return userPortalModule.getModulePreferences().getNotificationType() != NotificationType.NONE;
	}

	public void actionSendEmailWithActivationCode() {
		DcemUser dcemUser = portalSessionBean.getDcemUser();
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("registration.noUserRegsitered"));
			return;
		} else {
			try {

				asModuleApi.createActivationCode(dcemUser, activationValidTill, SendByEnum.EMAIL, "");
				JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("registration.info.email"));
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
		DcemUser dcemUser = portalSessionBean.getDcemUser();
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("registration.noUserRegsitered"));
			return;
		} else {
			try {
				asModuleApi.createActivationCode(dcemUser, activationValidTill, SendByEnum.SMS, "");
				JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("registration.info.sms"));
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
		if (notificationType == NotificationType.EMAIL_ONLY || notificationType == NotificationType.BOTH)
			return true;
		else
			return false;
	}

	public boolean isSmsNotificationTypeSelected() {
		if (notificationType == NotificationType.SMS_ONLY || notificationType == NotificationType.BOTH)
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

	public String getRecoveryKey() {
		if (isLocalUser() == false) {
			return null;
		}
		if (recoveryKey != null) {
			return recoveryKey;
		}
		if (portalSessionBean.getDcemUser() != null) {
			// creating the recovery key
			CloudSafeEntity cloudSafeEntity = new CloudSafeEntity(CloudSafeOwner.USER, portalSessionBean.getDcemUser(), null, DcemConstants.RECOVERY_KEY, null,
					CloudSafeOptions.ENC.name(), false, null);
			cloudSafeEntity.setGcm(true);
			byte[] recoverKey;
			try {
				recoverKey = cloudSafeLogic.getContentAsBytes(cloudSafeEntity, null, null);
				recoveryKey = new String(recoverKey, DcemConstants.UTF_8);
			} catch (DcemException e) {
				e.printStackTrace();
				recoveryKey = "Not Available";
			}
		}
		return recoveryKey;
	}

	public void setRecoveryKey(String recoveryKey) {
		this.recoveryKey = recoveryKey;
	}

	public boolean isLocalUser() {
		return domainName == null;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getUserLoginName() {
		return userLoginName;
	}

	public boolean isRegistrationLocalUserViewVisible() {
		if (!userPortalModule.getModulePreferences().isEnableLocalUserRegistration()) {
			return false;
		} else
			return true;
	}

	public boolean isRegisterViewVisible() {
		return userPortalModule.getModulePreferences().isCreateAccountEnabled();
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void displayActivationCode() {
		DcemUser dcemUser = portalSessionBean.getDcemUser();
		if (dcemUser == null) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("registration.noUserRegsitered"));
			return;
		} else {
			this.activationCode = asActivationLogic.requestActivationCode(dcemUser);
			PrimeFaces.current().ajax().update("notificationForm:activationCodeLabel");
		}
	}

	public UrlTokenEntity getUrlTokenEntity() {
		return urlTokenEntity;
	}

	public void setUrlTokenEntity(UrlTokenEntity urlTokenEntity) {
		this.urlTokenEntity = urlTokenEntity;
	}

	public boolean isManagementLogin() {
		return managementLogin;
	}

	public void setManagementLogin(boolean managementLogin) {
		this.managementLogin = managementLogin;
	}

	public String getStartedFrom() {
		return startedFrom;
	}

	public void setStartedFrom(String startedFrom) {
		this.startedFrom = startedFrom;
	}

}
