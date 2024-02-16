package com.doubleclue.portaldemo.gui;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.as.restapi.ApiException;
import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.as.restapi.model.AddMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMessage;
import com.doubleclue.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.as.restapi.model.AsMapEntry;
import com.doubleclue.as.restapi.model.DcemApiException;
import com.doubleclue.portaldemo.AbstractPortalView;
import com.doubleclue.portaldemo.PortalSessionBean;
import com.doubleclue.portaldemo.utils.JsfUtils;

@SuppressWarnings("serial")
@Named("messageTabView")
@SessionScoped
public class MessageTabView extends AbstractPortalView {

	private static final String WIDGET_VAR_PROGRESS = "progressDlg";
	
	@Inject
	private PortalSessionBean portalSessionBean;

	AsClientRestApi clientRestApi = AsClientRestApi.getInstance();

	Integer progress;
	AddMessageResponse addMessageResponse = new AddMessageResponse();
	boolean inProgress;
	long startTime;
	AsApiMsgStatus msgStatus;

	String recipient;
	String iban;
	String amount;
	String purpose;
	
	private long msgId;
	private int msgTimeToLive;
	

	public MessageTabView() {
		System.out.println("MessageTabView.MessageTabView()");
	}

	public static boolean isNumeric(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void actionSend() {

		List<AsMapEntry> dataMap = new ArrayList<>();
		dataMap.add(new AsMapEntry("recipient", recipient));
		dataMap.add(new AsMapEntry("iban", iban));
		dataMap.add(new AsMapEntry("purpose", purpose));
		if (amount == null) {
			amount = new String("0.01");
		}
		if (isNumeric(amount) == false) {
			JsfUtils.addErrorMessage("Amount valie is not numeric");
			return;
		}
		dataMap.add(new AsMapEntry("amount", amount));

		portalSessionBean.getUserName();
		AsApiMessage asApiMessage = new AsApiMessage(portalSessionBean.getUserName(), "as.MoneyTransfer", dataMap,
				true);
		
		try {
			addMessageResponse = clientRestApi.addMessage(asApiMessage);
			msgId = addMessageResponse.getMsgId();
			msgTimeToLive = addMessageResponse.getTimeToLive();
			inProgress = true;
			progress = 0;
			startTime = System.currentTimeMillis();
			msgStatus = null;
//			PrimeFaces.current().ajax().update("msgForm:msgs");
//			PrimeFaces.current().ajax().update("msgForm:timeLeft");
//			PrimeFaces.current().ajax().update("msgForm:status");
//			PrimeFaces.current().executeScript("PF('progressDlgMsg').show();");
//			PrimeFaces.current().executeScript("PF('pbAjax').start();");
			openProgressDialog();
		} catch (DcemApiException | ApiException e) {
			JsfUtils.addErrorMessage(e.getMessage());
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
		if (progress < 2) {
			progress++;
			PrimeFaces.current().ajax().update("mainMessages");
			PrimeFaces.current().ajax().update("msgForm:timeLeft");
			PrimeFaces.current().ajax().update("msgForm:status");
			return progress;
		}
		try {
			AsApiMessageResponse response = clientRestApi.getMessageResponse(addMessageResponse.getMsgId(), 2);
			msgStatus = response.getMsgStatus();

			if (response.getFinal()) {
				// context.execute("PF('progressDlgMsg').hide();");
				PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
				inProgress = false;
				if (msgStatus.equals(AsApiMsgStatus.OK)) {
					response.getInputMap();
					if ((response.getActionId() != null) && (response.getActionId().equals("ok"))) {
						JsfUtils.addInfoMessage("Money transfer successful.");
					} else {
						throw new ApiException("User rejected the message");
					}
				} else {
					JsfUtils.addErrorMessage("Error: Login Failed due to: " + msgStatus.toString() + " " + response.getInfo());
				}
				PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
				PrimeFaces.current().executeScript("PF('progressDlg').hide();");
				progress = 0;
				recipient = null;
				iban = null;
				amount = null;
				purpose = null;
				PrimeFaces.current().ajax().update("msgForm");
				PrimeFaces.current().ajax().update("mainMessages");
				return 0;
			} else {
				elapseTime = (System.currentTimeMillis() - startTime) / 1000;
				progress = (((int) elapseTime) * 100) / addMessageResponse.getTimeToLive();
				if (progress > 100) {
					progress = 100;
				}
			}
		} catch (ApiException | DcemApiException exp) {

			JsfUtils.addErrorMessage(exp.toString());
			PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
			PrimeFaces.current().executeScript("PF('progressDlg').hide();");
			return 0;
		}
		PrimeFaces.current().ajax().update("mainMessages");
		PrimeFaces.current().ajax().update("msgForm:timeLeft");
		PrimeFaces.current().ajax().update("msgForm:status");
		return progress;
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	public String getIban() {
		return iban;
	}

	public void setIban(String iban) {
		this.iban = iban;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public String getName() {
		return "MessageTabView";
	}

	@Override
	public String getPath() {
		return "messageTabView.xhtml";
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void onComplete() {

	}

	public AsApiMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsApiMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}

	public String getTimeLeft() {
		if (addMessageResponse == null || addMessageResponse.getTimeToLive() == 0) {
			return null;
		}
		long elapseTime = (System.currentTimeMillis() - startTime) / 1000;
		return (addMessageResponse.getTimeToLive() - (int) elapseTime) + " seconds";
	}

	public void cancel() {
		progress = 0;
		PrimeFaces.current().executeScript("PF('pbAjax').cancel();");
		PrimeFaces.current().executeScript("PF('progressDlg').hide();");

		try {
			clientRestApi.cancelMessage(addMessageResponse.getMsgId());
		} catch (ApiException | DcemApiException exp) {
			JsfUtils.addErrorMessage(exp.toString());
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(exp.toString());
		}
	}

	// @SuppressWarnings("unchecked")
	// public PortalSessionBean findBean(String beanName) {
	// FacesContext context = FacesContext.getCurrentInstance();
	// return (PortalSessionBean)
	// context.getApplication().evaluateExpressionGet(context, "#{" + beanName +
	// "}", Object.class);
	// }

	// public PortalSessionBean findBean(final String beanName, final Class<T>
	// clazz) {
	// ELContext elContext = FacesContext.getCurrentInstance().getELContext();
	// return (T)
	// FacesContext.getCurrentInstance().getApplication().getELResolver().getValue(elContext,
	// null, beanName);
	// }
	
	private void openProgressDialog() {
		if (msgId > 0 && msgTimeToLive > 0) {
			progress = 0;
			startTime = System.currentTimeMillis();
			msgStatus = null;
//			PrimeFaces.current().ajax().update("msgForm:msgs");
			PrimeFaces.current().ajax().update("msgForm:timeLeft");
			PrimeFaces.current().ajax().update("msgForm:status");
			//PrimeFaces.current().ajax().update("msgForm:randomCode");
			PrimeFaces.current().executeScript("PF('pbAjax').start();");
			PrimeFaces.current().executeScript("PF('" + WIDGET_VAR_PROGRESS + "').show();");
			
		}
	}

}
