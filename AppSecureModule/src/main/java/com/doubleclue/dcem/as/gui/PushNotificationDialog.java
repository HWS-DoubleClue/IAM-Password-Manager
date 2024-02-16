package com.doubleclue.dcem.as.gui;

import java.io.IOException;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.FcmLogic;
import com.doubleclue.dcem.as.logic.PushNotificationConfig;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Named("pushNotificationDialog")
@SessionScoped
public class PushNotificationDialog extends DcemDialog {

	@Inject
	AsModule asModule;

	@Inject
	ConfigLogic configLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	FcmLogic fcmLogic;

	private UploadedFile uploadedFile;

	PushNotificationConfig pushNotificationConfig;

	// DcemConfiguration dcemConfiguration;

	// ResourceBundle asResourceBundle;

	boolean deleteUploaded;

	@PostConstruct
	private void init() {

	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		pushNotificationConfig = fcmLogic.loadConfiguration();
	}

	@Override
	public void leavingDialog() {
		pushNotificationConfig = null;
		uploadedFile = null;
		deleteUploaded = false;
	}

	public String getProjectId() {
		if (pushNotificationConfig != null) {
			if (pushNotificationConfig.getGoogleServiceFile() == null || pushNotificationConfig.getGoogleServiceFile().isEmpty()) {
				ResourceBundle resourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME, operatorSessionBean.getLocale());
				return JsfUtils.getStringSafely(resourceBundle, "pushNotification.noProject");
			}
			try {
				JsonNode jsonNode = new ObjectMapper().readTree(pushNotificationConfig.getGoogleServiceFile());
				return jsonNode.get("project_id").asText();
			} catch (IOException e) {
				JsfUtils.addErrorMessage(e.toString());
				return null;
			}
		}
		return null;

	}

	public boolean isRenderInherit() {
		return !TenantIdResolver.isCurrentTenantMaster();
	}
	
	public boolean actionOk() throws Exception {

		if (pushNotificationConfig.isEnable() && pushNotificationConfig.isInherit() == false) {
			if (pushNotificationConfig.getGoogleServiceFile() == null && (uploadedFile == null || uploadedFile.getSize() == 0)) {
				JsfUtils.addErrorMessage("Please uplaod a google-services.json file.");
				return false;
			} else {
				if (uploadedFile.getSize() > 0) {
					JsonNode jsonNodeRoot;
					try {
						jsonNodeRoot = new ObjectMapper().readTree(uploadedFile.getContent());
					} catch (Exception e) {
						JsfUtils.addErrorMessage("Wrong File Format");
						return false;
					}
					JsonNode jsonNode = jsonNodeRoot.get("type");
					if (jsonNode == null) {
						JsfUtils.addErrorMessage("Account Type is missing in file.");
						return false;
					}
					if (jsonNode.asText().equals("service_account") == false) {
						JsfUtils.addErrorMessage("Wrong service type. Type should be service_account.");
						return false;
					}
					jsonNode = jsonNodeRoot.get("project_id");
					if (jsonNode == null) {
						JsfUtils.addErrorMessage("No project_id foudn in file.");
						return false;
					}
					pushNotificationConfig.setGoogleServiceFile(new String(uploadedFile.getContent(), DcemConstants.CHARSET_UTF8));
				} else {
					if (deleteUploaded) {
						pushNotificationConfig.setGoogleServiceFile(null);
					}
				}
			}
		} else {
			if (deleteUploaded) {
				pushNotificationConfig.setGoogleServiceFile(null);
			}
		}
		deleteUploaded = false;
		fcmLogic.writeConfiguration(pushNotificationConfig);
		return true;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public PushNotificationConfig getPushNotificationConfig() {
		return pushNotificationConfig;
	}

	public void setPushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
		this.pushNotificationConfig = pushNotificationConfig;
	}

	public boolean isDeleteUploaded() {
		return deleteUploaded;
	}

	public void setDeleteUploaded(boolean deleteUploaded) {
		this.deleteUploaded = deleteUploaded;
	}

}
