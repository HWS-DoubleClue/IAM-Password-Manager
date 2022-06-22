package com.doubleclue.dcem.as.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.shaded.json.JSONObject;

import com.doubleclue.dcem.as.logic.AsFidoLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("asFidoAuthenticatorDialog")
@SessionScoped
public class AsFidoAuthenticatorDialog extends DcemDialog {

	@Inject
	AsFidoLogic fidoLogic;

	@Inject
	UserLogic userLogic;
	
	private static final Logger logger = LogManager.getLogger(AsFidoAuthenticatorDialog.class);

	private String username = null;
	private String displayName = null;
	private String regResponse = null;
	private String regError = null;
	private String rpId = null;
	private String domainName = null;

	public List<String> completeUser(String name) {
		if (domainName == null || domainName.isEmpty()) {
			return userLogic.getCompleteUserList(name, 50);
		} else {
			return userLogic.getCompleteUserList(domainName + DcemConstants.DOMAIN_SEPERATOR + name, 50);
		}
	}

	public void actionStartRegistration() {
		if (displayName != null && !displayName.isEmpty()) {
			try {
				String regRequestJson = fidoLogic.startRegistration(username, rpId);
				PrimeFaces.current().ajax().addCallbackParam(DcemConstants.FIDO_PARAM_JSON, regRequestJson);
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error." + e.getErrorCode()));
			} catch (Exception e) {
				logger.warn("actionFinishRegistration", e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} else {
			JsfUtils.addErrorMessage(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error.local.FIDO_NO_DISPLAY_NAME"));
		}
	}

	public void actionFinishRegistration() {
		try {
			if (regResponse == null || regResponse.isEmpty()) {
				JsfUtils.addErrorMessage(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error.local.FIDO_NO_RESPONSE"));
			} else {
				String regResultJson = fidoLogic.finishRegistration(regResponse, displayName);
				JSONObject obj = new JSONObject(regResultJson);
				boolean successful = obj.getBoolean("success");

				regResponse = null;
				displayName = null;
				username = null;
				domainName = null;

				if (successful) {
					dialogReturn(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.message.registerSuccessful"));
				} else {
					dialogReturn(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.message.registerFailed"));
				}
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error." + e.getErrorCode()));
		} catch (Exception e) {
			logger.warn("actionFinishRegistration", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void actionShowError() {
		if (regError != null && !regError.isEmpty()) {
			String localisedMessage;
			switch (regError) {
			case DcemConstants.FIDO_ERROR_ABORTED_CHROME:
			case DcemConstants.FIDO_ERROR_ABORTED_FIREFOX:
				localisedMessage = JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error.local.FIDO_REGISTRATION_ABORTED");
				break;
			case DcemConstants.FIDO_ERROR_ALREADY_REGISTERED_CHROME:
			case DcemConstants.FIDO_ERROR_ALREADY_REGISTERED_FIREFOX:
				localisedMessage = JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error.local.FIDO_ALREADY_REGISTERED");
				break;
			case DcemConstants.FIDO_ERROR_WRONG_RP_ID_FIREFOX:
				localisedMessage = JsfUtils.getMessageFromBundle(AsModule.RESOURCE_NAME, "fidoView.error.local.FIDO_WRONG_RP_ID");
				break;
			default:
				localisedMessage = regError;
				break;
			}
			JsfUtils.addErrorMessage(localisedMessage);
		}
	}

	public void changeDomain() {
		username = null;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRegResponse() {
		return regResponse;
	}

	public void setRegResponse(String regResponse) {
		this.regResponse = regResponse;
	}

	public String getRegError() {
		return regError;
	}

	public void setRegError(String regError) {
		this.regError = regError;
	}

	public String getRpId() {
		return rpId;
	}

	public void setRpId(String rpId) {
		this.rpId = rpId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	@Override
	public String getHeight() {
		return "350px";
	}
	
}
