package com.doubleclue.portaldemo.gui;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
import org.primefaces.shaded.json.JSONObject;

import com.doubleclue.as.restapi.ApiException;
import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AsApiAuthMethod;
import com.doubleclue.as.restapi.model.AsApiAuthenticateResponse;
import com.doubleclue.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.portaldemo.AbstractPortalView;
import com.doubleclue.portaldemo.PortalDemoConstants;
import com.doubleclue.portaldemo.PortalSessionBean;
import com.doubleclue.portaldemo.utils.JsfUtils;

@SuppressWarnings("serial")
@Named("loginView")
@SessionScoped
public class LoginView extends AbstractPortalView {

	private static Logger logger = LogManager.getLogger(LoginView.class);

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	LoginQrCodeView loginQrCodeView;

	private String username;
	private String password;
	private String browserFingerprint;
	private String passcode;
	private String fidoError;
	private String fidoResponse;
	private String rpId;

	boolean loggedIn = false;
	boolean inProgress;

	private Integer progress;
	private AsApiMsgStatus msgStatus;
	// AsClientRestApi clientRestApi = AsClientRestApi.getInstance();
	private long startTime;
	private String loginOtp;
	private List<AsApiAuthMethod> authMethods;

	private int msgTimeToLive;
	private long msgId;
	private String randomCode;

	private List<AsApiAuthMethod> availableAuthMethods;
	private AsApiAuthMethod chosenAuthMethod;
	private AsApiAuthenticateResponse authResponse;

	private static final String WIDGET_VAR_AUTH_CHOICE = "authDlg";
	private static final String WIDGET_VAR_PASSCODE = "pcDlg";
	private static final String WIDGET_VAR_PROGRESS = "progressDlg";
	private String fqUserName;

	private ResourceBundle resourceBundle;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(PortalDemoConstants.PD_RESOURCE);
		progress = 0;

	}

	public String actionRequestSecureMessage() {
		return "portalLogin.xhtml";
	}

	public String actionRequestRadiusLogin() {
		return "radiusLogin.xhtml";
	}

	private void openProgressDialog() {
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
		long elapseTime;
		if (inProgress == false || progress == 100) {
			return 0;
		}
		try {
			int waitResponse = PortalDemoConstants.WAIT_INTERVAL_MILLI_SECONDS;
			if (progress < 1) {
				waitResponse = 200; // first time don't wait sp long.
			}
			AsApiMessageResponse response = AsClientRestApi.getInstance().getMessageResponse(msgId, waitResponse);
			msgStatus = response.getMsgStatus();
			if (response.getFinal()) {
				inProgress = false;
				if (msgStatus.equals(AsApiMsgStatus.OK)) {
					response.getInputMap();

					if ((response.getActionId() != null) && (response.getActionId().equals("ok"))) {
						portalSessionBean.setLoggedIn(true);
						portalSessionBean.setUserName(response.getUserLoginId());
						portalSessionBean.setDeviceName(response.getDeviceName());
						loggedIn = true;
					} else {
						loggedIn = false;
						JsfUtils.addInfoMessage("Login was rejected");
						PrimeFaces.current().executeScript("PF('progressDlg').hide();");
						PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
						return 100;
					}
				} else {
					JsfUtils.addErrorMessage("Error: Login Failed due to: " + msgStatus.toString() + " " + response.getInfo());
					PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
					PrimeFaces.current().executeScript("PF('progressDlg').hide();");
					return 0;
				}
				// context.execute("PF('progressDlg').hide();");
				PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
				return 100;
			} else {
				elapseTime = (System.currentTimeMillis() - startTime) / 1000;
				progress = (((int) elapseTime) * 100) / msgTimeToLive;
				if (progress > 100) {
					progress = 100;
				}
			}
		} catch (ApiException | DcemApiException exp) {
			logger.info(exp);
			if (progress != 0) { // message was canceled
				JsfUtils.addErrorMessage(exp.toString());
			}
			PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
			PrimeFaces.current().executeScript("PF('progressDlg').hide();");
			inProgress = false;
			progress = 0;
			return 0;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
			PrimeFaces.current().executeScript("PF('progressDlg').hide();");
		}
		PrimeFaces.current().ajax().update("loginForm:randomCode");
		PrimeFaces.current().ajax().update("loginForm:status");
		PrimeFaces.current().ajax().update("loginForm:timeLeft");

		return progress;
	}

	@Override
	public String getName() {
		return username;
	}

	@Override
	public String getPath() {
		return "login.xhtml";
	}

	public void setName(String name) {
		this.username = name;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public void cancel() {
		progress = 0;
		PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
		PrimeFaces.current().executeScript("PF('progressDlg').hide();");

		try {
			AsClientRestApi.getInstance().cancelMessage(msgId);
		} catch (DcemApiException exp) {
			if (exp.getCode() != 34) {
				JsfUtils.addErrorMessage(exp.toString());
			}
		} catch (ApiException exp) {

			JsfUtils.addErrorMessage(exp.toString());
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(exp.toString());
		}
	}

	public AsApiMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsApiMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getTimeLeft() {
		if (msgTimeToLive == 0) {
			return null;
		}
		long elapseTime = (System.currentTimeMillis() - startTime) / 1000;
		return (msgTimeToLive - (int) elapseTime) + " seconds";
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

	public boolean isAnyMethods() {
		return authMethods == null ? false : true;
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
			String key = "message.authInstruction." + chosenAuthMethod.getValue();
			switch (chosenAuthMethod) {
			case VOICE_MESSAGE:
			case SMS:
				return JsfUtils.getMessageFromBundle(resourceBundle, key, authResponse.getPhoneNumber());
			default:
				return JsfUtils.getStringSafely(resourceBundle, key);
			}
		}
		return "";
	}

	public void actionAuthMethodChosen(AsApiAuthMethod chosenAuthMethod) {
		this.chosenAuthMethod = chosenAuthMethod;
		closeDialog(WIDGET_VAR_AUTH_CHOICE);
		try {
			authenticateUser(true);
		} catch (DcemApiException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void actionPasscodeEntered() {

		try {
			authenticateUser(true);
			closeDialog(WIDGET_VAR_PASSCODE);
		} catch (DcemApiException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public void actionLogin() {
		loginQrCodeView.stopQrCode();
		loggedIn = false;
		if (username == null || username.isEmpty()) {
			JsfUtils.addErrorMessage("Missing username.");
		} else if (password == null || password.isEmpty()) {
			JsfUtils.addErrorMessage("Missing password.");
		} else {
			chosenAuthMethod = null;
			passcode = null;
			try {
				authenticateUser(false);
			} catch (DcemApiException e) {
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.getMessage());
			}
			PrimeFaces.current().ajax().update("loginForm:progressDlg");
		}
	}

	private void authenticateUser(boolean ignorePassword) throws Exception {

		availableAuthMethods = null;
		msgId = 0;
		msgTimeToLive = 0;
		randomCode = null;

		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}

		String authMethodString = chosenAuthMethod != null ? chosenAuthMethod.name() : null;
		String passcodeParam = chosenAuthMethod == AsApiAuthMethod.FIDO_U2F ? URLEncoder.encode(fidoResponse, "UTF-8") : passcode;
		authResponse = AsClientRestApi.getInstance().authenticate(username, authMethodString, password, passcodeParam, ipAddress, browserFingerprint,
				ignorePassword, fidoResponse, rpId);

		password = null;

		if (authResponse.isSuccessful()) {
			fqUserName = authResponse.getFqUserLoginId();
			finishLogin();
		} else {
			availableAuthMethods = authResponse.getAuthMethods();
			if (availableAuthMethods.size() > 1) {
				availableAuthMethods.remove(AsApiAuthMethod.QRCODE_APPROVAL);
				PrimeFaces.current().ajax().update("loginForm:authMethodDialog");
				openDialog(WIDGET_VAR_AUTH_CHOICE);
			} else if (availableAuthMethods.size() == 1) {
				chosenAuthMethod = availableAuthMethods.get(0);
				msgId = authResponse.getSecureMsgId();
				msgTimeToLive = authResponse.getSecureMsgTimeToLive();
				randomCode = authResponse.getSecureMsgRandomCode();
				openDialogForChosenAuthMethod();
			} else {
				JsfUtils.addErrorMessage("Authentication failed.");
			}
		}

	}

	private void openDialogForChosenAuthMethod() {
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

	private void finishLogin() {
		try {
			loggedIn = true;
			portalSessionBean.setLoggedIn(true);
			if (fqUserName != null) {
				portalSessionBean.setUserName(fqUserName);
			} else {
				portalSessionBean.setUserName(username);
			}
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			ec.redirect("welcome.xhtml");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public List<AuthMethodGui> getAuthMethodGuis() {
		List<AuthMethodGui> authMethodGuis = new ArrayList<>();
		if (availableAuthMethods != null) {
			for (AsApiAuthMethod method : availableAuthMethods) {
				authMethodGuis.add(new AuthMethodGui(method, resourceBundle));
			}
		}
		return authMethodGuis;
	}

	public int getPollIntervalMilli() {
		return PortalDemoConstants.POLL_INTERVAL_MILLI_SECONDS;
	}

	public int getPollIntervalSeconds() {
		return PortalDemoConstants.POLL_INTERVAL_MILLI_SECONDS / 1000;
	}

	public void cancelPassCode() {
		closeDialog(WIDGET_VAR_PASSCODE);
	}

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

	private void processFidoAuthentication() {
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
					JsfUtils.addWarnMessage(resourceBundle.getString("error.local.FIDO_AUTH_FAILED"));
				}
			}
		}
	}

	public void actionFinishFidoAuthentication() {
		try {
			if (fidoResponse == null || fidoResponse.isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.local.FIDO_NO_RESPONSE"));
			} else {
				authenticateUser(true);
			}
		} catch (DcemApiException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
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
			case PortalDemoConstants.FIDO_ERROR_ABORTED_CHROME:
			case PortalDemoConstants.FIDO_ERROR_ABORTED_FIREFOX:
				localisedError = resourceBundle.getString("error.local.FIDO_AUTH_ABORTED");
				break;
			case PortalDemoConstants.FIDO_ERROR_NO_AUTHENTICATORS_CHROME:
				localisedError = resourceBundle.getString("error.local.FIDO_NO_AUTHENTICATORS");
				break;
			case PortalDemoConstants.FIDO_ERROR_NOT_REGISTERED_CHROME:
			case PortalDemoConstants.FIDO_ERROR_NOT_REGISTERED_FIREFOX:
				localisedError = resourceBundle.getString("error.local.FIDO_AUTHENTICATOR_NOT_REGISTERED");
				break;
			default:
				localisedError = fidoError;
				break;
			}
			fidoError = null;
			JsfUtils.addErrorMessage(localisedError);
		}
	}
}
