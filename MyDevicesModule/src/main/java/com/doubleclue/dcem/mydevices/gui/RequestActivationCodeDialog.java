package com.doubleclue.dcem.mydevices.gui;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.mydevices.logic.MyDevicesModule;
import com.doubleclue.dcem.subjects.AsActivationSubject;
import com.doubleclue.utils.ActivationParameters;
import com.fasterxml.jackson.databind.ObjectMapper;

@RequestScoped
@Named("requestActivationCodeDialog")
public class RequestActivationCodeDialog implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	AsActivationSubject activationSubject;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AsModule asModule;


	private SendByEnum sendBy;
	ResourceBundle resourceBundle;
	
	byte[] pngImage = null;
	String dateTxt;
	ActivationParameters activationParameters;
	
	@PostConstruct
	public void init() {
		resourceBundle = ResourceBundle.getBundle(MyDevicesModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}
	
		
	public void onOpen() {
		pngImage = null;
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('addNewDeviceDialog').hide();");
		current.executeScript("PF('addSmartDevice').show();");	
	}

	public String actionRequestActivationCode() throws DcemException {
		try {
			if (sendBy == null) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.NO_SEND_METHOD_SELECTED"));
				return null;
			}
			ActivationCodeEntity asActivationCode = new ActivationCodeEntity();
			asActivationCode.setUser(operatorSessionBean.getDcemUser());
			if (asActivationCode.getValidTill() == null) {
				int hours = asModule.getPreferences().getActivationCodeDefaultValidTill();
				asActivationCode.setValidTill(LocalDateTime.now().plusHours(hours));
			}
			activationParameters = activationLogic.addUpdateActivationCode(asActivationCode, new DcemAction(activationSubject, DcemConstants.ACTION_ADD), sendBy, true);
			LocalDateTime nowDate = asActivationCode.getValidTill();
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(operatorSessionBean.getLocale());
			dateTxt = nowDate.format(formatter);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				pngImage = DcemUtils.createQRCode(objectMapper.writeValueAsString(activationParameters), 200, 200);
			} catch (Throwable e) {
				new DcemException(DcemErrorCodes.CANNOT_CREATE_QRCODE, "Couldn't Create QrCode", e);
			}			
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
		return null;
	}

	public SendByEnum getSendBy() {
		return sendBy;
	}

	public void setSendBy(SendByEnum sendBy) {
		this.sendBy = sendBy;
	}
	
	public String getUserFullQualifiedId() {
		return asModule.getUserFullQualifiedId(operatorSessionBean.getDcemUser());
	}

	public byte[] getPngImage() {
		return pngImage;
	}
	
	public StreamedContent getActivationImage() {
		if (pngImage == null) {
			return JsfUtils.getEmptyImage();
		}
		return DefaultStreamedContent.builder().contentType("image/png")
				.stream(() -> new ByteArrayInputStream(pngImage)).build();
	}
	
	public boolean isActivationAvialble() {
		if(sendBy != null && pngImage != null && sendBy.equals(SendByEnum.NONE)) {
			return true;
		}
		return false;
	}

	public void setPngImage(byte[] pngImage) {
		this.pngImage = pngImage;
	}

	public ActivationParameters getActivationParameters() {
		return activationParameters;
	}

	public void setActivationParameters(ActivationParameters activationParameters) {
		this.activationParameters = activationParameters;
	}

	public String getDateTxt() {
		return dateTxt;
	}
	
}