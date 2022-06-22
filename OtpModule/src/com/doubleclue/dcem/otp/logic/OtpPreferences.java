package com.doubleclue.dcem.otp.logic;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "otpPreferences")
public class OtpPreferences extends ModulePreferences {

	@DcemGui (help="This is the amount of time slots the OTP verification will go back. '1' means default time. ")
	@Min(1)
	@Max (10)
	int delayWindow = 2;
		

	public int getDelayWindow() {
		return delayWindow;
	}

	public void setDelayWindow(int delayWindow) {
		this.delayWindow = delayWindow;
	}
	
}
