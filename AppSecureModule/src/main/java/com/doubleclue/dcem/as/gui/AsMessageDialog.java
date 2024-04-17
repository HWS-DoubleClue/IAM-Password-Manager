package com.doubleclue.dcem.as.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.restapi.model.AddMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMessage;
import com.doubleclue.dcem.as.restapi.model.AsMapEntry;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.utils.StringUtils;

@SuppressWarnings("serial")
@Named("asMessageDialog")
@SessionScoped
public class AsMessageDialog extends DcemDialog {

	@Inject
	TemplateLogic templateLogic;
	
	@Inject
	UserLogic userLogic;
	
	@Inject
	AsMessageHandler messageHandler;
	
	@Inject
	PolicyLogic policyLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;

	AsApiMessage apiMessage;
	
	DcemUser dcemUser;
	
	// AppMessage appMessage;

	List<AsMapEntry> dataTable;



	public boolean actionOk() throws Exception {
				
		apiMessage.setUserLoginId(dcemUser.getLoginId());
		apiMessage.setDataMap(dataTable);
		if (apiMessage.getTemplateName() == null || apiMessage.getTemplateName().isEmpty()) {
			JsfUtils.addErrorMessage("Please select a Template");
			return false;
		}
		AddMessageResponse addMessageResponse =  messageHandler.sendMessage(apiMessage, dcemUser, operatorSessionBean.getDcemUser(), AuthApplication.DCEM, 0, null, null);
		if (addMessageResponse.isWithPushNotification() == false) {
			JsfUtils.addWarningMessage(AsModule.RESOURCE_NAME, "pushNotificationNotSent");
			return false;
		}
		return true;
	}

	/**
	 * @return
//	 */
	private Map<String, String> convertToMap() {
		Map<String, String> map = new HashMap<>();
		for (AsMapEntry pair : dataTable) {
			map.put(pair.getKey(), pair.getValue());
		}
		return map;
	}

	@Override
	public void actionConfirm() {
		// TODO Auto-generated method stub

	}
	
	public boolean isResponseRequired () {
		return apiMessage.getResponseRequired();
	}

	public List<AsMapEntry> getDataTable() {
		if (dataTable == null) {
			try {
				DcemTemplate template = templateLogic.getDefaultTemplate(apiMessage.getTemplateName());
				if (template == null) {
					JsfUtils.addErrorMessage(
							"No Default-Template found for tempalte-name: " + apiMessage.getTemplateName());
					return null;
				}
				dataTable = new LinkedList<>();
				List<String> tokens = template.getTokens();
				if (tokens != null && tokens.isEmpty() == false) {
					dataTable = new ArrayList<>(tokens.size());
					for (String token : tokens) {
						dataTable.add(new AsMapEntry(token, null));
					}
				}

			} catch (Exception e) {
				JsfUtils.addErrorMessage(
						"Couldn't get Default-Template for tempalte-name: " + apiMessage.getTemplateName());
				return null;
			}

		}
//		System.out.println("AsMessageDialog.getDataTable()" + dataTable);
		return dataTable;
	}

	public void loadTable() {
//		System.out.println("AsMessageDialog.loadTable() " + message.getTemplateName());
		dataTable = null;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		dataTable = null;
		apiMessage = new AsApiMessage();
		apiMessage.setResponseRequired(true);
		apiMessage.setTemplateName("Default");
	}
	
	@Override
	public String getWidth() {
		return "840";		
	}
	
	@Override
	public String getHeight() {
		return "800";		
	}

	
	
	public String getHtmlData() {
		if (apiMessage.getTemplateName() != null && (apiMessage.getTemplateName().isEmpty() == false)) {
			getDataTable();
			Map<String, String> map = convertToMap();
			
			try {
				DcemTemplate template =templateLogic.getDefaultTemplate(apiMessage.getTemplateName());
				String content = StringUtils.substituteTemplate(template.getContent(), map);
				return content;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "";
	}

	public AsApiMessage getApiMessage() {
		return apiMessage;
	}

	public void setApiMessage(AsApiMessage apiMessage) {
		this.apiMessage = apiMessage;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	

}
