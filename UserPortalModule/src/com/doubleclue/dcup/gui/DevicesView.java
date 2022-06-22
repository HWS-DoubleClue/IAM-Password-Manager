package com.doubleclue.dcup.gui;

import java.text.DateFormat;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.shaded.json.JSONObject;

import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.FidoAuthenticatorEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsFidoLogic;
import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.restapi.model.AsApiDevice;
import com.doubleclue.dcem.as.restapi.model.AsApiFidoAuthenticator;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.logic.module.AsApiOtpToken;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.subjects.AsDeviceSubject;
import com.doubleclue.dcem.userportal.gui.UserPortalConfigView;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.ActionItem;
import com.doubleclue.dcup.logic.DcupConstants;

@Named("devicesView")
@SessionScoped
public class DevicesView extends AbstractPortalView {

	private static final long serialVersionUID = 1L;

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	UserPortalModule userPortalModule;

	@Inject
	UserPortalConfigView userPortalConfigView;

	@Inject
	AsFidoLogic asFidoLogic;

	@Inject
	AsDeviceLogic asDeviceLogic;

	@Inject
	AsDeviceSubject deviceSubject;

	@Inject
	AsFidoLogic fidoLogic;

	private Logger logger = LogManager.getLogger(DevicesView.class);

	private List<DeviceEntity> devices;
	private List<AsApiOtpToken> otpTokens;
	private List<FidoAuthenticatorEntity> fidoAuthenticators = null;

	private String regResponse = null;
	private String regError = null;
	private String fidoDisplayName = null;
	private String rpId = null;

	private List<DeviceEntity> selectedDevices;
	private List<AsApiOtpToken> selectedOtpTokens;
	private List<FidoAuthenticatorEntity> selectedFidoAuthenticators = null;

	@PostConstruct
	public void init() {
	}

	@Override
	public String getName() {
		return DcupViewEnum.devicesView.name();
	}

	@Override
	public String getPath() {
		return "devicesView.xhtml";
	}

	public List<FidoAuthenticatorEntity> getFidoAuthenticators() {
		try {
			List<ApiFilterItem> filters = new LinkedList<>();
			filters.add(new ApiFilterItem("user.loginId", portalSessionBean.getUserName(), ApiFilterItem.SortOrderEnum.ASCENDING,
					ApiFilterItem.OperatorEnum.EQUALS));
			fidoAuthenticators = asFidoLogic.queryFidoAuthenticators(filters, 0, 100);
		} catch (DcemException e) {
			logger.error("Error while querying FIDO Authenticators: " + e.getLocalizedMessage());
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}
		return fidoAuthenticators;
	}

	public void setFidoAuthenticators(List<FidoAuthenticatorEntity> fidoAuthenticators) {
		this.fidoAuthenticators = fidoAuthenticators;
	}

	public List<DeviceEntity> getDevices() {
		try {
			List<ApiFilterItem> filters = new LinkedList<>();
			filters.add(new ApiFilterItem("user.loginId", portalSessionBean.getUserName(), ApiFilterItem.SortOrderEnum.ASCENDING,
					ApiFilterItem.OperatorEnum.EQUALS));
			filters.add(new ApiFilterItem("name", DcemConstants.DEVICE_ROOT, ApiFilterItem.SortOrderEnum.ASCENDING,
					ApiFilterItem.OperatorEnum.NOT_EQUALS));
			devices = asDeviceLogic.queryDevices(filters, 0, 100);
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}
		return devices;
	}

	public void setDevices(List<DeviceEntity> devices) {
		this.devices = devices;
	}

	public List<AsApiOtpToken> getOtpTokens() {
		try {
			List<ApiFilterItem> filters = new LinkedList<>();
			filters.add(new ApiFilterItem("user.loginId", portalSessionBean.getUserName(), ApiFilterItem.SortOrderEnum.ASCENDING,
					ApiFilterItem.OperatorEnum.EQUALS));
			OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
			if (apiServiceImpl == null) {
				return null;
			}
			otpTokens = apiServiceImpl.queryOtpTokenEntities(filters, 0, 100, false);
		} catch (DcemException e) {
			logger.error("Error while querying OTP tokens: " + e.getLocalizedMessage());
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}
		return otpTokens;
	}

	public void setOtpTokens(List<AsApiOtpToken> otpTokens) {
		this.otpTokens = otpTokens;
	}

	public String getFidoDisplayName() {
		return fidoDisplayName;
	}

	public void setFidoDisplayName(String fidoDisplayName) {
		this.fidoDisplayName = fidoDisplayName;
	}

	public String getRpId() {
		return rpId;
	}

	public void setRpId(String rpId) {
		this.rpId = rpId;
	}

	public void actionFidoStartRegistration() throws DcemException {
		portalSessionBean.isActionEnable(ActionItem.FIDO_ADD_ACTION);
		if (fidoDisplayName != null && !fidoDisplayName.isEmpty()) {
			String username = portalSessionBean.getUserName();
			try {
				String regRequestJson = fidoLogic.startRegistration(username, rpId);
				PrimeFaces.current().ajax().addCallbackParam(DcupConstants.FIDO_PARAM_JSON, regRequestJson);
			} catch (DcemException exp) {
				logger.warn(exp);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exp));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getMessage());
			}
		} else {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.local.FIDO_NO_DISPLAY_NAME"));
		}
	}

	public void actionFidoFinishRegistration() {
		try {
			if (regResponse == null || regResponse.isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.local.FIDO_NO_RESPONSE"));
			} else {
				String regResultJson = fidoLogic.finishRegistration(regResponse, fidoDisplayName);
				JSONObject obj = new JSONObject(regResultJson);
				boolean successful = obj.getBoolean("success");
				if (successful) {
					JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.fidoRegisterSuccessful"));
				} else {
					JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.fidoRegisterFailed"));
				}
				regResponse = null;
				fidoDisplayName = null;
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error." + e.getErrorCode().name()));
		} catch (Exception e) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error." + e.getMessage()));
		}
	}

	public void actionShowError() {
		if (regError != null && !regError.isEmpty()) {
			String localisedMessage;
			switch (regError) {
			case DcupConstants.FIDO_ERROR_ABORTED_CHROME:
			case DcupConstants.FIDO_ERROR_ABORTED_FIREFOX:
				localisedMessage = portalSessionBean.getResourceBundle().getString("error.local.FIDO_REGISTRATION_ABORTED");
				break;
			case DcupConstants.FIDO_ERROR_ALREADY_REGISTERED_CHROME:
			case DcupConstants.FIDO_ERROR_ALREADY_REGISTERED_FIREFOX:
				localisedMessage = portalSessionBean.getResourceBundle().getString("error.local.FIDO_ALREADY_REGISTERED");
				break;
			case DcupConstants.FIDO_ERROR_WRONG_RP_ID_FIREFOX:
				localisedMessage = portalSessionBean.getResourceBundle().getString("error.local.FIDO_WRONG_RP_ID");
				break;
			default:
				localisedMessage = regError;
				break;
			}
			JsfUtils.addErrorMessage(localisedMessage);
		}
	}

	public void actionDeleteSelectedFidoAuthenticator() throws DcemException {
		portalSessionBean.isActionEnable(ActionItem.FIDO_DELETE_ACTION);
		if (selectedFidoAuthenticators.isEmpty()) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectDeviceManager"));
			return;
		} else {
			for (FidoAuthenticatorEntity fidoAuthenticator : selectedFidoAuthenticators) {
				try {
					fidoLogic.deleteFidoAuthenticator((int) fidoAuthenticator.getId());
				} catch (DcemException e) {
					JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error." + e.getMessage()));
				} catch (Exception e) {
					logger.warn(e);
					JsfUtils.addErrorMessage(e.toString());
				}
			}
		}
	}

	public void validateDeleteSelectedFidoAuthenticator() throws DcemException {
		if (selectedFidoAuthenticators != null && selectedFidoAuthenticators.isEmpty() == false) {
			showDialog("confirmDlgSelectedFidoAuthenticator");
		} else {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
		}
	}

	public String getDialogMessage(AsApiFidoAuthenticator fidoAuthenticator) {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, portalSessionBean.getLocale());
		return JsfUtils.getMessageFromBundle(portalSessionBean.getResourceBundle(), "dialog.message.deleteFidoAuthenticator",
				df.format(fidoAuthenticator.getRegisteredOn()));
	}

	public void actionSetDeviceState(boolean enableState) throws DcemException {
		if (selectedDevices.isEmpty()) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectDeviceManager"));
			return;
		}
		if (enableState) {
			portalSessionBean.isActionEnable(ActionItem.NETWORK_DEVICE_ENABLE_ACTION);
		} else {
			portalSessionBean.isActionEnable(ActionItem.NETWORK_DEVICE_DISABLE_ACTION);
		}

		for (DeviceEntity device : selectedDevices) {
			try {
				DeviceState deviceState;
				if (enableState == true) {
					deviceState = DeviceState.Enabled;
				} else {
					deviceState = DeviceState.Disabled;
				}
				asDeviceLogic.setDeviceState(device.getId(), deviceState);

			} catch (DcemException e) {
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	public void deleteSelectedDevices() throws DcemException {
		if (selectedDevices.isEmpty()) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectDeviceManager"));
			return;
		}
		portalSessionBean.isActionEnable(ActionItem.NETWORK_DEVICE_DELETE_ACTION);

		try {
			asDeviceLogic.deleteDevices(selectedDevices, new DcemAction(deviceSubject, DcemConstants.ACTION_DELETE));
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}

	}

	public void validateSelectedDevices() throws DcemException {
		if (selectedDevices != null && selectedDevices.isEmpty() == false) {
			showDialog("confirmDlgSelectedDevices");
		} else {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
		}
	}

	public String getTranslatedPlatform(AsApiDevice.ClientTypeEnum clientType) {
		switch (clientType) {
		case ANDROID:
			return "Android";
		case MAC:
			return "Mac";
		case I_OS:
			return "iOS";
		case LINUX:
			return "Linux";
		case WINDOWS:
			return "Windows";
		default:
			return "";
		}
	}

	public String getTranslatedState(AsApiDevice.StateEnum state) {
		if (state != null) {
			switch (state) {
			case Enabled:
				return portalSessionBean.getResourceBundle().getString("label.enabled");
			case Disabled:
				return portalSessionBean.getResourceBundle().getString("label.disabled");
			case TempLocked:
				return portalSessionBean.getResourceBundle().getString("label.temp_locked");
			}
		}
		return "";
	}

	public void actionSetTokenState(boolean enableState) throws DcemException {
		if (selectedOtpTokens.isEmpty()) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectDeviceManager"));
			return;
		}
		if (enableState) {
			portalSessionBean.isActionEnable(ActionItem.OTP_ENABLE_ACTION);
		} else {
			portalSessionBean.isActionEnable(ActionItem.OTP_DISABLE_ACTION);
		}

		if (selectedOtpTokens != null) {
			for (AsApiOtpToken asApiOtpToken : selectedOtpTokens) {
				asApiOtpToken.setDisabled(enableState);
				modifyToken(asApiOtpToken);
			}
		}
	}

	public void actionUnassignSelectedTokens() throws DcemException {
		if (selectedOtpTokens.isEmpty()) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectDeviceManager"));
			return;
		}
		else {
			portalSessionBean.isActionEnable(ActionItem.OTP_DELETE_ACTION);
			for (AsApiOtpToken otpToken : selectedOtpTokens) {
				otpToken.setAssignedTo(null);
				otpToken.setInfo("");
				modifyToken(otpToken);
			}
		}
	}

	public void validateUnassignSelectedTokens() throws DcemException {
		if (selectedOtpTokens != null && selectedOtpTokens.isEmpty() == false) {
			showDialog("confirmDlgSelectedTokens");
		} else {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
		}
	}

	public String getDialogMessage(AsApiOtpToken token) {
		return portalSessionBean.getResourceBundle().getString("dialog.message.removeToken") + " " + token.getSerialNumber();
	}

	private void modifyToken(AsApiOtpToken otpToken) {
		try {
			OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
			if (apiServiceImpl == null) {
				JsfUtils.addErrorMessage("Couldn't find the HArdware Token Module");
				return;
			}
			apiServiceImpl.modifyOtpToken(otpToken, null);

		} catch (DcemException e) {
			logger.error("AsApiException while modifying the token: " + e.toString());
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public String getDialogMessage(AsApiDevice device) {
		return portalSessionBean.getResourceBundle().getString("dialog.message.deleteDevice") + " " + device.getName();
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

	public List<AsApiOtpToken> getSelectedOtpTokens() {
		return selectedOtpTokens;
	}

	public void setSelectedOtpTokens(List<AsApiOtpToken> selectedOtpTokens) {
		this.selectedOtpTokens = selectedOtpTokens;
	}

	public List<FidoAuthenticatorEntity> getSelectedFidoAuthenticators() {
		return selectedFidoAuthenticators;
	}

	public void setSelectedFidoAuthenticators(List<FidoAuthenticatorEntity> selectedFidoAuthenticators) {
		this.selectedFidoAuthenticators = selectedFidoAuthenticators;
	}

	public void addAuthMethodDialog() {
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('assignNewAuthMethod').hide();");
	}

	public List<DeviceEntity> getSelectedDevices() {
		return selectedDevices;
	}

	public void setSelectedDevices(List<DeviceEntity> selectedDevices) {
		this.selectedDevices = selectedDevices;
	}

	void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}
}
