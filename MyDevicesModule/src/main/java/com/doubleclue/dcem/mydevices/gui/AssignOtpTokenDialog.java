package com.doubleclue.dcem.mydevices.gui;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.AsApiOtpToken;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.mydevices.logic.MyDevicesModule;

@RequestScoped
@Named("assignOtpTokenDialog")
public class AssignOtpTokenDialog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private OperatorSessionBean operatorSessionBean;
	
	@Inject
	MyDevicesModule myDevicesModule;
	
	@Inject
	MyDevicesView myDevicesView;
	
	

	private String serialNumber;
	private String info;
	private String passcode;


	public void assignOtpToken() throws DcemException {
//		portalSessionBean.isActionEnable(ActionItem.OTP_ADD_ACTION);
		try {
			if (validateInput()) {
				AsApiOtpToken asApiOtpToken = new AsApiOtpToken();
				asApiOtpToken.setSerialNumber(serialNumber);
				asApiOtpToken.setInfo(info);
				asApiOtpToken.setAssignedTo(operatorSessionBean.getDcemUser().getLoginId());
				OtpModuleApi apiServiceImpl = CdiUtils.getReference(OtpModuleApi.OTP_SERVICE_IMPL);
				if (apiServiceImpl == null) {
					JsfUtils.addErrorMessageToComponentId("Couldn't find the Hardware Token Module", "hardwareOtpMsg");
					return;
				}
				apiServiceImpl.modifyOtpToken(asApiOtpToken, passcode);
				JsfUtils.addInfoMessageToComponent(myDevicesView.getResourceBundle().getString("message.tokenAssignSuccess"), "hardwareOtpMsg");
//				PrimeFaces current = PrimeFaces.current();
//				current.executeScript("PF('assignOTP').hide();");
				PrimeFaces.current().ajax().update("deviceForm:hardwareTokenTable");
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessageToComponentId(e.getLocalizedMessage(), "deviceForm:hardwareOtpMsg");
			PrimeFaces.current().ajax().update("deviceForm:hardwareOtpMsg");
		} catch (Exception e) {
			JsfUtils.addErrorMessageToComponentId(e.toString(), "hardwareOtpMsg");
			PrimeFaces.current().ajax().update("deviceForm:hardwareOtpMsg");
		}
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

	private boolean validateInput() {
		if (serialNumber.isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(myDevicesView.getResourceBundle().getString("error.MISSING_SERIAL_NUMBER"), "hardwareOtpMsg");
			PrimeFaces.current().ajax().update("deviceForm:hardwareOtpMsg");
			return false;
		}
		if (passcode.isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(myDevicesView.getResourceBundle().getString("error.MISSING_PASSCODE"), "hardwareOtpMsg");
			PrimeFaces.current().ajax().update("deviceForm:hardwareOtpMsg");
			return false;
		}
		return true;
	}
}