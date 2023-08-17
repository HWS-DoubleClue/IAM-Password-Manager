package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
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
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.subjects.AsActivationSubject;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcem.userportal.preferences.UserPortalPreferences;
import com.doubleclue.dcup.logic.ActionItem;
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
	private PortalSessionBean portalSessionBean;

	@Inject
	AsActivationSubject activationSubject;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AsModule asModule;

	@Inject
	UserPortalModule userPortalModule;

	@Inject
	UserPortalPreferences userPortalPreferences;

	private SendByEnum sendBy;
	
	byte[] pngImage = null;
	String dateTxt;
	ActivationParameters activationParameters;
	
	
	
		
	public void onOpen() {
		pngImage = null;
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('addNewDeviceDialog').hide();");
		current.executeScript("PF('addSmartDevice').show();");	
	}

	public String actionRequestActivationCode() throws DcemException {
		portalSessionBean.isActionEnable(ActionItem.NETWORK_DEVICE_ADD_ACTION);
		try {
			if (sendBy == null) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.NO_SEND_METHOD_SELECTED"));
				return null;
			}
			ActivationCodeEntity asActivationCode = new ActivationCodeEntity();
			asActivationCode.setUser(portalSessionBean.getDcemUser());
			if (asActivationCode.getValidTill() == null) {
				int hours = asModule.getPreferences().getActivationCodeDefaultValidTill();
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.HOUR_OF_DAY, hours);
				asActivationCode.setValidTill(calendar.getTime());
			}
			activationParameters = activationLogic.addUpdateActivationCode(asActivationCode, new DcemAction(activationSubject, DcemConstants.ACTION_ADD), sendBy, true);
			LocalDateTime nowDate = Instant.ofEpochMilli(asActivationCode.getValidTill().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
			DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(portalSessionBean.getLocale());
			dateTxt = nowDate.format(formatter);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				pngImage = DcemUtils.createQRCode(objectMapper.writeValueAsString(activationParameters), 200, 200);
			} catch (Throwable e) {
				new DcemException(DcemErrorCodes.CANNOT_CREATE_QRCODE, "Couldn't Create QrCode", e);
			}			
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
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
		DcemUser dcemUser;
		try {
			dcemUser = userLogic.getUser(portalSessionBean.getUserName());
		} catch (DcemException e) {
			return null;
		}
		return asModule.getUserFullQualifiedId(dcemUser);
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