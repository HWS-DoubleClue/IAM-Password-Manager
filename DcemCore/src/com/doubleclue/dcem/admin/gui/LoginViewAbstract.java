package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.expression.ComponentNotFoundException;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.shaded.json.JSONObject;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.windowssso.WindowsSso;
import com.doubleclue.dcem.admin.windowssso.WindowsSsoResult;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsMessageResponse;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.as.AsMsgStatus;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.as.QrCodeResponse;
import com.doubleclue.dcem.core.as.QueryLoginResponse;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ListUserAccounts;
import com.doubleclue.dcem.core.gui.UserAccount;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.DomainAzure;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.QrCodeUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
public abstract class LoginViewAbstract implements Serializable {

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	ClusterConfig clusterConfig;

	@Inject
	AdminModule adminModule;

	@Inject
	WindowsSso windowsSso;

	ConnectionServicesType connectionServicesType;

	public ConnectionServicesType getConnectionServicesType() {
		return connectionServicesType;
	}

	public void setConnectionServicesType(ConnectionServicesType connectionServicesType) {
		this.connectionServicesType = connectionServicesType;
	}

	public static Logger logger = LogManager.getLogger(LoginViewAbstract.class);

	private String username;
	protected String userLoginId;
	private String password;
	private String browserFingerprint;
	private String passcode;
	private String fidoError;
	private String fidoResponse;
	private String rpId;

	private boolean inProgress;

	String preLoginMessage;

	protected DcemUser dcemUser;
	private boolean useAlternativeAuthMethods;
	private boolean stayLoggedIn = false;

	protected Integer progress;
	protected AsMsgStatus msgStatus;
	private long startTime;
	private String loginOtp;
	private String phoneNumber;

	private int msgTimeToLive;
	private long msgId;
	private String randomCode;
	protected boolean stopQrCode = true;
	protected boolean loggedIn = false;

	protected List<AuthMethod> availableAuthMethods;
	private AuthMethod chosenAuthMethod;
	private QrCodeResponse qrCodeResponse;
	protected AuthenticateResponse authResponse;
	private String serializedAccounts;

	private static final String WIDGET_VAR_AUTH_CHOICE = "authMethodDialog";
	private static final String WIDGET_VAR_PASSCODE = "pcDlg";
	private static final String WIDGET_VAR_PROGRESS = "progressDlg";

	private AsModuleApi asModuleApi;
	protected ResourceBundle adminResourceBundle;
	private AtomicBoolean loginReEnter = new AtomicBoolean(false);
	private AuthApplication authApplication = AuthApplication.DCEM;
	private int applicationSubId = 0;
	protected DcemException lastException;

	private String passwordOld = "";
	private String passwordNew;
	private String passwordRepeat;

	protected boolean loginPanelRendered = true;
	protected boolean passwordPanelRendered = false;
	String mgtActiveView;
	String mgtUrlView;

	String location;
	ListUserAccounts listUserAccounts;
	UserAccount selectedAccount;

	private String sessionCookie;

	protected void setAuthApplication(AuthApplication authApplication) {
		this.authApplication = authApplication;
	}

	protected void setApplicationSubId(int applicationSubId) {
		this.applicationSubId = applicationSubId;
	}

	protected void init() {
		Locale locale = Locale.ENGLISH;
		try {
			locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
		} catch (Exception e) {
			// TODO: handle exception
		}
		adminResourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, locale);
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
	}

	public void onPreRenderView() {
	}

	protected void setNewLocale(Locale locale) {
		adminResourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, locale);
	}

	protected void openProgressDialog() {
		if (msgId > 0 && msgTimeToLive > 0) {
			progress = 0;
			startTime = System.currentTimeMillis();
			msgStatus = null;
			PrimeFaces.current().ajax().update("loginForm:timeLeft");
			PrimeFaces.current().ajax().update("loginForm:status");
			PrimeFaces.current().ajax().update("loginForm:randomCode");
			PrimeFaces.current().executeScript("PF('" + WIDGET_VAR_PROGRESS + "').show();");
			PrimeFaces.current().executeScript("PF('pbAjax').start();");
		}
	}

	/**
	 * @return
	 */
	public Integer getProgress() {
		// logger.debug("MfaLoginView.getProgress() " + inProgress + " " + progress);
		long elapseTime;
		if (inProgress == false || progress == 100) {
			return 0;
		}
		if (loginReEnter.getAndSet(true) == true) {
			logger.debug("MfaLoginView.getProgress() ReENTER");
			return progress;
		}
		try {
			// long start = System.currentTimeMillis();
			// logger.debug("MfaLoginView.getProgress() calling getMessage");
			int waitResponse = DcemConstants.LOGIN_WAIT_INTERVAL_MILLI_SECONDS;
			if (progress < 1) {
				waitResponse = 200; // first time don't wait sp long.
			}
			AsMessageResponse asMessageResponse = asModuleApi.getMessageResponse(msgId, waitResponse);
			// logger.debug("MfaLoginView.getProgress() response");
			msgStatus = asMessageResponse.getMsgStatus();
			if (asMessageResponse.isFinal()) {
				inProgress = false;
				if (msgStatus.equals(AsMsgStatus.OK)) {
					if ((asMessageResponse.getActionId() != null) && (asMessageResponse.getActionId().equals("ok"))) {
						authResponse.setSessionCookie(asMessageResponse.getSessionCookie());
						authResponse.setSessionCookieExpiresOn(asMessageResponse.getSessionCookieExpiresOn());
						finishLogin();
					} else {
						JsfUtils.addErrorMessage(JsfUtils.getMessageFromBundle(adminResourceBundle, "mfalogin.rejected"));
						closeProgressDialog();
						loginReEnter.set(false);
						return 0;
					}
				} else {
					String msg = JsfUtils.getMessageFromBundle(adminResourceBundle, "mfalogin.failed");
					JsfUtils.addErrorMessage(msg + " " + JsfUtils.getMessageFromBundle(adminResourceBundle, "mfalogin." + msgStatus.name()));
					closeProgressDialog();
					loginReEnter.set(false);
					return 0;
				}
				PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
				loginReEnter.set(false);
				return 100;
			} else {
				elapseTime = (System.currentTimeMillis() - startTime) / 1000;
				progress = (((int) elapseTime) * 100) / msgTimeToLive;
				if (progress > 100) {
					progress = 100;
				}
			}
		} catch (DcemException exp) {
			if ((exp.getErrorCode() != DcemErrorCodes.MESSAGE_NOT_FOUND) && progress != 0) {
				logger.info(exp);
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				closeProgressDialog();
			}
			return 0;
		} catch (Exception exp) {
			logger.info("LoginMfa", exp);
			JsfUtils.addErrorMessage(exp.toString());
			closeProgressDialog();
			return 0;
		} finally {
			loginReEnter.set(false);
		}
		PrimeFaces.current().ajax().update("loginForm:randomCode");
		PrimeFaces.current().ajax().update("loginForm:status");
		PrimeFaces.current().ajax().update("loginForm:timeLeft");
		loginReEnter.set(false);
		return progress;
	}

	protected void closeProgressDialog() {
		progress = null;
		inProgress = false;
		try {
			PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
			PrimeFaces.current().executeScript("PF('progressDlg').hide();");
		} catch (Exception e) {
			logger.trace("Could not close progress dialog.", e);
		}
	}

	public void setUserName(String name) {
		this.username = name;
	}

	public String getUserName() {
		if (preLoginMessage != null) {
			JsfUtils.addErrorMessage(preLoginMessage);
			preLoginMessage = null;
		}
		return this.username;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public void onComplete() {
		closeProgressDialog();
	}

	public void cancel() {
		closeProgressDialog();
		try {
			asModuleApi.cancelMessage(msgId);
		} catch (DcemException exp) {
			if (exp.getErrorCode() != DcemErrorCodes.MESSAGE_NOT_FOUND) {
				JsfUtils.addErrorMessage(exp.toString());
			}
		}
	}

	public AsMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getTimeLeft() {
		if (msgTimeToLive == 0) {
			return null;
		}
		long elapseTime = (System.currentTimeMillis() - startTime) / 1000;
		return Integer.toString((msgTimeToLive - (int) elapseTime));
	}

	public String getLoginOtp() {
		return loginOtp;
	}

	public void setLoginOtp(String loginOtp) {
		this.loginOtp = loginOtp;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBrowserFingerprint() {
		return browserFingerprint;
	}

	public void setBrowserFingerprint(String browserFingerprint) {
		this.browserFingerprint = browserFingerprint;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

	public String getRandomCode() {
		return randomCode;
	}

	public void setRandomCode(String randomCode) {
		this.randomCode = randomCode;
	}

	public String getAuthInstruction() {
		if (chosenAuthMethod != null) {
			switch (chosenAuthMethod) {
			case HARDWARE_TOKEN:
			case DOUBLECLUE_PASSCODE:
				return JsfUtils.getMessageFromBundle(adminResourceBundle, "mfalogin.passcodeGenerate." + chosenAuthMethod.getAbbreviation());
			case SMS:
			case VOICE_MESSAGE:
				return JsfUtils.getMessageFromBundle(adminResourceBundle, "mfalogin.passcodeGenerate." + chosenAuthMethod.getAbbreviation(), phoneNumber);
			default:
				break;
			}
		}
		return "";
	}

	public String actionAuthMethodChosen(AuthMethod chosenAuthMethod) {
		this.chosenAuthMethod = chosenAuthMethod;
		closeDialog(WIDGET_VAR_AUTH_CHOICE);
		try {
			authenticateUser(true, true);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
		return null;
	}

	public void actionPasscodeEntered() {
		try {
			authenticateUser(true, false);
			closeDialog(WIDGET_VAR_PASSCODE);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public void actionAzureLogin() {
		DomainAzure domainAzure = domainLogic.getDomainAzure();
		try {
			domainAzure.sendAuthRedirect(connectionServicesType);
			JsfUtils.getFacesContext().responseComplete();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public boolean isAzureLogin() {
		return adminModule.getPreferences().isEnableAzureDirectLogin() && domainLogic.getDomainAzure() != null;
	}

	public void actionLogin() {
		loggedIn = false;
		dcemUser = null;
		chosenAuthMethod = null;
		stopQrCode();
		if (validateInput() == false) {
			return;
		}
		if (location == null || location.isEmpty()) {
			location = JsfUtils.getRemoteIpAddress();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("MGT-Login action for " + username + " from " + location);
		}
		int ind = username.indexOf(AppSystemConstants.TENANT_SEPERATOR);
		if (ind != -1) {
			userLoginId = username.substring(0, ind);
			try {
				switchTenant();
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				logger.info(e.getLocalizedMessage());
				return;
			}
		} else {
			userLoginId = username;
		}
		passcode = null;
		try {
			authenticateUser(false, true);
			lastException = null;
		} catch (DcemException exp) {
			lastException = exp;
			switch (exp.getErrorCode()) {
			case INVALID_USERID:
			case INVALID_PASSWORD:
			case CREATE_ACCOUNT_INVALID_CREDENTIALS:
			case INVALID_DOMAIN_NAME:
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, DcemConstants.MESSAGE_WRONG_CREDENTIALS);
				break;
			case UNEXPECTED_ERROR:
				JsfUtils.addErrorMessage(exp.toString());
				break;
			case USER_MUST_RESET_PASSWORD:
			case USER_PASSWORD_EXPIRED:
				JsfUtils.addWarningMessage(AdminModule.RESOURCE_NAME, DcemConstants.MESSAGE_RESET_PASSWORD_BEFORE_LOGIN);
				showChangePassword();
				break;
			case LDAP_CONNECTION_FAILED:
				reportingLogic.addWelcomeViewAlert(AdminModule.MODULE_ID, exp.getErrorCode(), DcemConstants.DOUBLECLUE, AlertSeverity.ERROR, true);
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				logger.warn("actionLogin error", exp);
				break;
			case AZURE_NEEDS_MFA:
				logger.debug ("AZURE_NEEDS_MFA for: " +  userLoginId );
				// We need to redirect
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				break;
			default:
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "DcemErrorCodes." + exp.getErrorCode());
				break;
			}
			logger.info("login failed for " + userLoginId + ", Cause: " +  exp.getLocalizedMessage());
			return;
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(exp.toString());
			logger.warn("actionLogin error", exp);
			return;
		}
		try {
			PrimeFaces.current().ajax().update("progressForm:progressDlg");
		} catch (ComponentNotFoundException exp) {
			logger.warn("ComponentNotFoundException: progressForm:progressDlg");
		}
	}

	public abstract void showChangePassword();

	private void authenticateUser(boolean ignorePassword, boolean ignorePasscode) throws Exception {
		msgId = 0;
		msgTimeToLive = 0;
		randomCode = null;

		AuthRequestParam requestParam = new AuthRequestParam();

		requestParam.setNetworkAddress(JsfUtils.getRemoteIpAddress());
		// requestParam.setBrowserFingerPrint(browserFingerprint);
		requestParam.setIgnorePassword(ignorePassword);
		requestParam.setFidoResponse(fidoResponse);
		requestParam.setFidoRpId(rpId);
		requestParam.setUseAlternativeAuthMethods(useAlternativeAuthMethods);
		requestParam.setLocation(location);
		if (chosenAuthMethod == AuthMethod.SESSION_RECONNECT) {
			requestParam.setSessionCookie(sessionCookie);
		}
		if (ignorePasscode == false && passcode.isEmpty()) {
			throw new DcemException(DcemErrorCodes.PASSCODE_EMPTY, null);
		}
		authResponse = asModuleApi.authenticate(authApplication, applicationSubId, userLoginId, chosenAuthMethod, password, passcode, requestParam);
		availableAuthMethods = authResponse.getAuthMethods();
		dcemUser = authResponse.getDcemUser();
		if (password != null) {
			StringUtils.wipeString(password);
		}
		password = null;
		availableAuthMethods = authResponse.getAuthMethods();
		if (authResponse.isSuccessful()) {
			finishLogin();
		} else {
			if (authResponse.getDcemException() != null) {
				throw authResponse.getDcemException();
			}
			if (availableAuthMethods.size() > 1) {
				availableAuthMethods.remove(AuthMethod.QRCODE_APPROVAL);
				PrimeFaces.current().ajax().update("authMethodForm:authMethodDialog");
				openDialog(WIDGET_VAR_AUTH_CHOICE);
			} else if (availableAuthMethods.size() == 1) {
				chosenAuthMethod = availableAuthMethods.get(0);
				msgId = authResponse.getSecureMsgId();
				msgTimeToLive = authResponse.getSecureMsgTimeToLive();
				randomCode = authResponse.getSecureMsgRandomCode();
				phoneNumber = authResponse.getPhoneNumber();
				openDialogForChosenAuthMethod();
			} else {
				JsfUtils.addErrorMessage("Authentication failed.");
			}
		}
	}

	private void openDialogForChosenAuthMethod() throws DcemException {
		passcode = null;
		switch (chosenAuthMethod) {
		case PUSH_APPROVAL:
			inProgress = true;
			openProgressDialog();
			break;
		case HARDWARE_TOKEN:
		case DOUBLECLUE_PASSCODE:
		case SMS:
		case VOICE_MESSAGE:
			PrimeFaces.current().ajax().update("passcodeForm:passcodeDialog");
			openDialog(WIDGET_VAR_PASSCODE);
			break;
		case PASSWORD:
			finishLogin();
			break;
		case FIDO_U2F:
			processFidoAuthentication();
			break;
		default:
			break;
		}
	}

	private void openDialog(String widgetVar) {
		PrimeFaces.current().executeScript("PF('" + widgetVar + "').show();");
	}

	private void closeDialog(String widgetVar) {
		PrimeFaces.current().executeScript("PF('" + widgetVar + "').hide();");
	}

	public String actionRemoveUserAccount() {
		if (selectedAccount == null || listUserAccounts == null) {
			return null;
		}
		listUserAccounts.removeAccount(selectedAccount);
		PrimeFaces.current().executeScript("localStorage.setItem('accounts', '" + serializeUserAccounts(listUserAccounts) + "')");
		return DcemConstants.PRE_LOGIN_PAGE;
	}

	protected void finishLogin() throws DcemException {
		TimeZone timeZone = adminModule.getTimezone();
		ExternalContext ec = JsfUtils.getExternalContext();
		HttpServletRequest httpServletRequest = (HttpServletRequest) ec.getRequest();
		httpServletRequest.getSession().setAttribute((String) DcemConstants.SESSION_TIMEZONE, timeZone);
		if (listUserAccounts == null) {
			listUserAccounts = new ListUserAccounts();
		}
		if (authResponse != null) {
			if (authResponse.isStayLoggedInAllowed() == false) {
				stayLoggedIn = false;
			} else {
				removeUserAccount(dcemUser.getId());
				if (stayLoggedIn == true) {
					UserAccount userAccount = new UserAccount(username, dcemUser.getId(), authResponse.getSessionCookie(),
							authResponse.getSessionCookieExpiresOn());
					listUserAccounts.addAccount(userAccount);
				}
				PrimeFaces.current().executeScript("localStorage.setItem('accounts', '" + serializeUserAccounts(listUserAccounts) + "')");
			}
		}
	}

	public List<AuthMethodGui> getAuthMethodGuis() {
		List<AuthMethodGui> authMethodGuis = new ArrayList<>();
		if (availableAuthMethods != null) {
			for (AuthMethod method : availableAuthMethods) {
				if (method == AuthMethod.QRCODE_APPROVAL || method.getValue() == null) {
					System.out.println("LoginViewAbstract.getAuthMethodGuis()  " + method.getValue());
					continue;
				}
				authMethodGuis.add(new AuthMethodGui(method, getResounceBundleModule()));
			}
		}
		return authMethodGuis;
	}

	public void cancelPassCode() {
		closeDialog(WIDGET_VAR_PASSCODE);
	}

	private void processFidoAuthentication() throws DcemException {
		if (authResponse != null && authResponse.getFidoResponse() != null && !authResponse.getFidoResponse().isEmpty()) {
			if (!authResponse.isSuccessful()) {
				PrimeFaces.current().executeScript("startFidoAuthentication('" + authResponse.getFidoResponse() + "');");
			} else {
				String regResultJson = authResponse.getFidoResponse();
				JSONObject obj = new JSONObject(regResultJson);
				boolean successful = obj.getBoolean("success");
				if (successful) {
					finishLogin();
				} else {
					JsfUtils.addWarnMessage(adminResourceBundle.getString("mfalogin.error.local.FIDO_AUTH_FAILED"));
				}
			}
		}
	}

	public void actionFinishFidoAuthentication() {
		try {
			if (fidoResponse == null || fidoResponse.isEmpty()) {
				JsfUtils.addErrorMessage(adminResourceBundle.getString("mfalogin.error.local.FIDO_NO_RESPONSE"));
			} else {
				authenticateUser(true, true);
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} finally {
			fidoResponse = null;
		}
	}

	public void actionShowFidoError() {
		if (fidoError != null && !fidoError.isEmpty()) {
			String localisedError;
			switch (fidoError) {
			case DcemConstants.FIDO_ERROR_ABORTED_CHROME:
			case DcemConstants.FIDO_ERROR_ABORTED_FIREFOX:
				localisedError = adminResourceBundle.getString("mfalogin.error.local.FIDO_AUTH_ABORTED");
				break;
			case DcemConstants.FIDO_ERROR_NO_AUTHENTICATORS_CHROME:
				localisedError = adminResourceBundle.getString("mfalogin.error.local.FIDO_NO_AUTHENTICATORS");
				break;
			case DcemConstants.FIDO_ERROR_NOT_REGISTERED_CHROME:
			case DcemConstants.FIDO_ERROR_NOT_REGISTERED_FIREFOX:
				localisedError = adminResourceBundle.getString("mfalogin.error.local.FIDO_AUTHENTICATOR_NOT_REGISTERED");
				break;
			default:
				localisedError = fidoError;
				break;
			}
			fidoError = null;
			JsfUtils.addErrorMessage(localisedError);
		}
	}

	public void stopQrCode() {
		stopQrCode = true;
		qrCodeResponse = null;
		PrimeFaces.current().executeScript("PF('poller').stop()");
		PrimeFaces.current().ajax().update("loginForm:qrcodePanel");
		PrimeFaces.current().ajax().update("loginForm:basic");

	}

	public void requestNewQrCode() throws Exception {
		qrCodeResponse = asModuleApi.requestQrCode(AuthApplication.DCEM.name(), JsfUtils.getSessionId());
		stopQrCode = false;
		dcemUser = null;
		PrimeFaces.current().executeScript("PF('poller').start()");
	}

	public StreamedContent getQrCodeImage() {
		if (stopQrCode == false) {
			try {
				requestNewQrCode();
				byte[] image = null;
				image = QrCodeUtils.createQRCode(qrCodeResponse.getData(), 200, 200);
				InputStream in = new ByteArrayInputStream(image);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			} catch (Exception exp) {
				if (exp.getCause() instanceof ConnectException) {
					JsfUtils.addErrorMessage("Connection to Server failed, please check connection parameters. Go to portalConfig.xhtml.");
					return JsfUtils.getEmptyImage();
				}
				JsfUtils.addErrorMessage(exp.toString());
			}
		}
		return JsfUtils.getEmptyImage();

	}

	// private String getIpAddress() {
	// HttpServletRequest request = (HttpServletRequest)
	// FacesContext.getCurrentInstance().getExternalContext().getRequest();
	// String ipAddress = request.getHeader("X-FORWARDED-FOR");
	// return ipAddress == null ? request.getRemoteAddr() : ipAddress;
	// }

	public boolean isStopQrCode() {
		if (stopQrCode == true) {
			PrimeFaces.current().ajax().update("loginForm:qrcodePanel");
		}
		return stopQrCode;
	}

	public boolean isQrCodeEnabled() {
		// return asModuleApi.getAllowedAuthMethods(authApplication, applicationSubId,
		// null).contains(AuthMethod.QRCODE_APPROVAL);
		return true;
	}

	public int getQrCodeTimeToLive() {
		if (qrCodeResponse != null) {
			return (qrCodeResponse.getTimeToLive());
		}
		return 0;
	}

	public void actionQrExpired() {
		stopQrCode = true;
		qrCodeResponse = null;
	}

	public void checkQrCode() {
		if (stopQrCode == false) {
			try {
				if (qrCodeResponse == null) {
					requestNewQrCode();
				}
				QueryLoginResponse queryLoginResponse = asModuleApi.queryLoginQrCode(AuthApplication.DCEM.name(), JsfUtils.getSessionId(), false,
						DcemConstants.LOGIN_WAIT_INTERVAL_MILLI_SECONDS);
				if (queryLoginResponse.getUserLoginId() != null) {
					stopQrCode = true;
					userLoginId = queryLoginResponse.getUserLoginId();
					username = userLoginId;
					AuthRequestParam requestParam = new AuthRequestParam();
					requestParam.setNetworkAddress(JsfUtils.getRemoteIpAddress());
					// requestParam.setBrowserFingerPrint(browserFingerprint);
					requestParam.setIgnorePassword(true);
					requestParam.setUseAlternativeAuthMethods(false);
					if (location == null) {
						location = requestParam.getNetworkAddress();
					}
					requestParam.setLocation(location);
					authResponse = asModuleApi.authenticate(authApplication, applicationSubId, userLoginId, AuthMethod.QRCODE_APPROVAL, null, null,
							requestParam);
					if (authResponse.getDcemException() != null) {
						throw authResponse.getDcemException();
					}
					if (authResponse.isSuccessful() == false) {
						throw new DcemException(DcemErrorCodes.INVALID_AUTH_METHOD, userLoginId);
					}
					dcemUser = authResponse.getDcemUser();

					availableAuthMethods = new ArrayList<>(1);
					availableAuthMethods.add(AuthMethod.QRCODE_APPROVAL);
					// dcemUser = userLogic.getDistinctUser(userLoginId);
					qrCodeResponse = null;
					finishLogin();
				}
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				PrimeFaces.current().ajax().update("loginForm");
			} catch (Exception exp) {
				logger.warn("checkQRCode()", exp);
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			}
		}
	}

	public boolean isUserNameDisable() {
		return selectedAccount != null;
	}

	public boolean isBackLink() {
		return listUserAccounts != null && listUserAccounts.getAccounts().isEmpty() == false;
	}

	public String actionBackLink() {
		return DcemConstants.HTML_PAGE_PRE_LOGIN;
	}

	private void switchTenant() throws DcemException {
		TenantEntity tenantEntity = null;
		String separator = AppSystemConstants.TENANT_SEPERATOR;
		if (username != null && username.contains(separator)) {
			String tenantName = username.substring(username.indexOf(separator) + separator.length());
			if (tenantName != null) {
				tenantEntity = applicationBean.getTenant(tenantName);
				if (tenantEntity == null) {
					throw new DcemException(DcemErrorCodes.INVALID_TENANT, tenantName);
				}
			}
		} else {
			try {
				URL url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
				tenantEntity = applicationBean.getTenantFromUrlHostName(url.getHost());
			} catch (MalformedURLException e) {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.getMessage(), e);
			}
		}
		TenantIdResolver.setCurrentTenant(tenantEntity);
		JsfUtils.getHttpServletRequest().getSession().setAttribute(DcemConstants.URL_TENANT_PARAMETER, tenantEntity);
	}

	/**
	 * @return
	 */
	public String actionPreLoginOk() {
		System.out.println("LoginViewAbstract.actionPreLoginOk()");
		if (serializedAccounts != null && serializedAccounts.length() > 8) {
			listUserAccounts = deserializeAccounts(serializedAccounts);
			int myTime = (int) ((System.currentTimeMillis() / 1000));
			UserAccount foundAccount = null;
			for (UserAccount userAccount : listUserAccounts.getAccounts()) {
				if (userAccount.getSessionExpiresOn() > myTime) {
					foundAccount = userAccount;
					break;
				}
			}
			if (foundAccount != null) {
				chosenAuthMethod = AuthMethod.SESSION_RECONNECT;
				sessionCookie = foundAccount.getSessionCookie();
				username = foundAccount.getUserLoginId();
				userLoginId = username;
				stayLoggedIn = true;
				try {
					authenticateUser(true, true);
					return null;
				} catch (DcemException exp) {
					chosenAuthMethod = null;
					if (exp.getErrorCode() != DcemErrorCodes.AUTH_SESSION_COOKIE_NOT_ALLOWED) {
						logger.info("Session Reconnect failed for " + username);
						foundAccount.setSessionExpiresOn(0);
						foundAccount.setSessionCookie(null);
						PrimeFaces.current().executeScript("localStorage.setItem('accounts', '" + serializeUserAccounts(listUserAccounts) + "')");

					} else {
						JsfUtils.addWarnMessage(adminResourceBundle.getString("mfalogin.noStayLoggedIn"));
					}
					if (exp.getErrorCode() == DcemErrorCodes.INVALID_AUTH_SESSION_COOKIE) {
						JsfUtils.addWarnMessage(adminResourceBundle.getString("mfalogin.loggedInSomewhereElse"));
					}
				} catch (Exception exp) {
					logger.info("Session Reconnect failed for " + username, exp);
					foundAccount.setSessionExpiresOn(0);
					foundAccount.setSessionCookie(null);
					chosenAuthMethod = null;
					PrimeFaces.current().executeScript("localStorage.setItem('accounts', '" + serializeUserAccounts(listUserAccounts) + "')");
				}
				return DcemConstants.HTML_PAGE_SELECT_LOGIN;
			}
		}

		if (adminModule.getPreferences().isUseWindowsSSO() == true) {
			HttpServletRequest request = (HttpServletRequest) JsfUtils.getExternalContext().getRequest();
			HttpServletResponse response = (HttpServletResponse) JsfUtils.getExternalContext().getResponse();
			String page = null;
			try {
				WindowsSsoResult ssoResult = windowsSso.singleSignOn(request, response);
				if (logger.isDebugEnabled()) {
					logger.debug("Windows SSO Result: " + ssoResult);
				}
				switch (ssoResult.getResultType()) {
				case NO_AUTHORIZATION_HEADER:
					windowsSso.sendUnauthorized(response, true);
					JsfUtils.getFacesContext().responseComplete();
					break;
				case OK:
					String result = windowsSsologin(ssoResult);
					if (result != null) {
						page = result;
					}
					break;
				case NO_WINDOWS_PROVIDER:
					page = DcemConstants.HTML_PAGE_SELECT_LOGIN;
					break;
				case NON_WINDOWS:
					page = DcemConstants.HTML_PAGE_SELECT_LOGIN;
					break;
				case EXCEPTION:
					page = DcemConstants.HTML_PAGE_SELECT_LOGIN;
					break;
				default:
					page = DcemConstants.HTML_PAGE_SELECT_LOGIN;
					break;
				}
			} catch (Exception e) {
				logger.info("WindowsSSO", e);
			}
			System.out.println("LoginViewAbstract.actionPreLoginOk() PAGE " + page);
			return page;
		}

		// return DcemConstants.HTML_PAGE_LOGIN + DcemConstants.FACES_REDIRECT;
		// }

		if (listUserAccounts != null && listUserAccounts.getAccounts().isEmpty()) {
			selectedAccount = null;
			return DcemConstants.HTML_PAGE_LOGIN + DcemConstants.FACES_REDIRECT;
		}
		return DcemConstants.HTML_PAGE_SELECT_LOGIN;
	}

	private String windowsSsologin(WindowsSsoResult windowsSsoResult) {
		username = windowsSsoResult.getFqn();
		userLoginId = username;
		try {
			chosenAuthMethod = null;
			authenticateUser(true, true);
			return DcemConstants.HTML_PAGE_LOGIN;
		} catch (DcemException exp) {
			System.out.println("LoginViewAbstract.windowsSsologin() " + exp);
			switch (exp.getErrorCode()) {
			case WINDOWS_SSO_NOT_IN_DOMAIN:
			case INVALID_DOMAIN_NAME:
				preLoginMessage = null;
				break;
			default:
				preLoginMessage = exp.getLocalizedMessage();
			}
			chosenAuthMethod = null;
			return DcemConstants.HTML_PAGE_LOGIN;

		} catch (Exception exp) {
			logger.info("windowsSsologin failed ", exp);
			preLoginMessage = exp.toString();
			return DcemConstants.HTML_PAGE_LOGIN;
		} finally {
			chosenAuthMethod = null;
		}
	}

	public String actionGoToLogin() {
		selectedAccount = null;
		username = null;
		return DcemConstants.HTML_PAGE_LOGIN + DcemConstants.FACES_REDIRECT;
	}

	public String actionSelectUserAccount() {
		stayLoggedIn = true;
		// if (adminModule.getPreferences().isEnableBrowserFingerPrint() == true &&
		// browserFingerprint.equals(selectedAccount.getFingerPrint()) == false) {
		// logger.info("User tries to login with wrong Browser-Fingerprint. " +
		// selectedAccount.getUserLoginId());
		// username = selectedAccount.getUserLoginId();
		// userLoginId = username;
		// selectedAccount = null;
		// return DcemConstants.HTML_PAGE_LOGIN;
		// }
		username = selectedAccount.getUserLoginId();
		userLoginId = username;
		return DcemConstants.HTML_PAGE_LOGIN;
	}

	private ListUserAccounts deserializeAccounts(String serAccounts) {
		if (serAccounts == null || serAccounts.length() == 0) {
			return new ListUserAccounts();
		}
		try {
			byte[] data = Base64.getDecoder().decode(serAccounts);
			data = SecureServerUtils.decryptDataSalt(DcemCluster.getInstance().getClusterKey(), data);
			TypeReference<ListUserAccounts> typeRef = new TypeReference<ListUserAccounts>() {
			};
			ObjectMapper objectMapper = new ObjectMapper();
			// System.out.println("LoginViewAbstract.deserializeAccounts() " + new
			// String(data));
			ListUserAccounts listUserAccounts = objectMapper.readValue(data, typeRef);
			return listUserAccounts;
		} catch (Exception e) {
			logger.info("Deserialize Browser Accounts error: " + e.toString());
			return new ListUserAccounts();
		}
	}

	private String serializeUserAccounts(ListUserAccounts listUserAccounts) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			String ser = objectMapper.writeValueAsString(listUserAccounts);
			// System.out.println("LoginViewAbstract.serializeUserAccounts() " + ser);
			byte[] data = SecureServerUtils.encryptDataSalt(DcemCluster.getInstance().getClusterKey(), ser.getBytes(DcemConstants.UTF_8));
			return Base64.getEncoder().encodeToString(data);
		} catch (Exception e) {
			logger.warn("serializeUserAccounts", e);
			return "";
		}
	}

	/**
	 * @return
	 */
	public String logoff() {
		UserAccount userAccountFound = null;
		if (listUserAccounts != null) {
			for (UserAccount account : listUserAccounts.getAccounts()) {
				if (account.getId() == dcemUser.getId()) {
					userAccountFound = account;
					break;
				}
			}
			if (userAccountFound != null && stayLoggedIn == true) {
				userAccountFound.setSessionCookie(null);
				userAccountFound.setSessionExpiresOn(0);
				PrimeFaces.current().executeScript("localStorage.setItem('accounts', '" + serializeUserAccounts(listUserAccounts) + "')");
			}
		}
		loggedIn = false;
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		session.setMaxInactiveInterval(1);
		return "logoff_.xhtml"  + DcemConstants.FACES_REDIRECT;
	}

	private void removeUserAccount(int userId) {
		int ind;
		UserAccount account = null;
		for (ind = 0; ind < listUserAccounts.getAccounts().size(); ind++) {
			account = listUserAccounts.getAccounts().get(ind);
			if (account.getId() == userId) {
				break;
			}
		}
		if (ind < listUserAccounts.getAccounts().size()) {
			listUserAccounts.getAccounts().remove(ind);
		}
	}

	public boolean isStayLoggedIn() {
		return stayLoggedIn;
	}

	public void setStayLoggedIn(boolean stayLoggedIn) {
		this.stayLoggedIn = stayLoggedIn;
	}

	public boolean isUseAlternativeAuthMethods() {
		return useAlternativeAuthMethods;
	}

	public void setUseAlternativeAuthMethods(boolean useAlternativeAuthMethods) {
		this.useAlternativeAuthMethods = useAlternativeAuthMethods;
	}

	public List<UserAccount> getUserAccounts() {
		if (listUserAccounts == null) {
			return new ArrayList<>(0);
		}
		return listUserAccounts.getAccounts();
	}

	// public void setUserAccounts(List<UserAccount> userAccounts) {
	// this.userAccounts = userAccounts;
	// }

	public String getFidoError() {
		return fidoError;
	}

	public void setFidoError(String fidoError) {
		this.fidoError = fidoError;
	}

	public String getFidoResponse() {
		return fidoResponse;
	}

	public void setFidoResponse(String fidoResponse) {
		this.fidoResponse = fidoResponse;
	}

	public String getRpId() {
		return rpId;
	}

	public void setRpId(String rpId) {
		this.rpId = rpId;
	}

	private boolean validateInput() {
		boolean valid = true;
		if (username == null) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "mfalogin.userNameMissing");
			return false;
		}
		username = username.trim();
		if (username.isEmpty()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "mfalogin.userNameMissing");
			valid = false;
		} else if (password == null || password.isEmpty()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "mfalogin.passwordMissing");
			valid = false;
		}
		return valid;
	}

	public String getSerializedAccounts() {
		return serializedAccounts;
	}

	public void setSerializedAccounts(String serializedAccounts) {
		this.serializedAccounts = serializedAccounts;
	}

	public UserAccount getSelectedAccount() {
		return selectedAccount;
	}

	public void setSelectedAccount(UserAccount selectedAccount) {
		this.selectedAccount = selectedAccount;
	}

	public String getUserLoginId() {
		return userLoginId;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void actionChangePassword() {
		try {
			if (validatePasswordInput()) {
				try {
					userLogic.changePassword(username, passwordOld, passwordNew);
					JsfUtils.addInfoMessage(adminResourceBundle.getString("userDialog.info.passwordChangedSuccessfully"));
					StringUtils.wipeString(passwordOld);
					StringUtils.wipeString(passwordNew);
					actionLogin();
					username = null;
				} catch (DcemException e) {
					JsfUtils.addErrorMessage(e.getLocalizedMessage());
				} catch (Exception exp) {
					JsfUtils.addErrorMessage(adminResourceBundle.getString("mfalogin.error.failed"));
				}
				passwordOld = null;
				passwordNew = null;
				passwordRepeat = null;
				loginPanelRendered = true;
				passwordPanelRendered = false;
				PrimeFaces.current().ajax().update("loginForm");
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	private boolean validatePasswordInput() {
		boolean valid = true;
		if (passwordOld.isEmpty() || passwordNew.isEmpty() || passwordRepeat.isEmpty()) {
			JsfUtils.addErrorMessage(adminResourceBundle.getString("userDialog.error.emptyFields"));
			valid = false;
		} else if (passwordNew.equals(passwordOld)) {
			JsfUtils.addErrorMessage(adminResourceBundle.getString("userDialog.error.newPasswordIdentToOld"));
			valid = false;
		} else if (!Objects.equals(passwordNew, passwordRepeat)) {
			JsfUtils.addErrorMessage(adminResourceBundle.getString("userDialog.error.passwordsNotIdent"));
			valid = false;
		}
		return valid;
	}

	public String getMgtActiveView() {
		return mgtActiveView;
	}

	public void setMgtActiveView(String mgtActiveView) {
		this.mgtActiveView = mgtActiveView;
	}

	public String getMgtUrlView() {
		return mgtUrlView;
	}

	public void setMgtUrlView(String mgtUrlView) {
		this.mgtUrlView = mgtUrlView;
	}

	protected ResourceBundle getResounceBundleModule() {
		return adminResourceBundle;
	}

	public boolean isPasswordPanelRendered() {
		return passwordPanelRendered;
	}

	public boolean isLoginPanelRendered() {
		return loginPanelRendered;
	}

	public String getPasswordOld() {
		return passwordOld;
	}

	public void setPasswordOld(String passwordOld) {
		this.passwordOld = passwordOld;
	}

	public String getPasswordNew() {
		return passwordNew;
	}

	public void setPasswordNew(String passwordNew) {
		this.passwordNew = passwordNew;
	}

	public String getPasswordRepeat() {
		return passwordRepeat;
	}

	public void setPasswordRepeat(String passwordRepeat) {
		this.passwordRepeat = passwordRepeat;
	}

	public String getPreLoginMessage() {
		return preLoginMessage;
	}

	public void setPreLoginMessage(String preLoginMessage) {
		this.preLoginMessage = preLoginMessage;
	}

}
