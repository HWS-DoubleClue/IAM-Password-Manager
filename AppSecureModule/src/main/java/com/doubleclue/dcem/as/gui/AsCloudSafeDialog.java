package com.doubleclue.dcem.as.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("asCloudSafeDialog")
@SessionScoped
@Deprecated
public class AsCloudSafeDialog extends DcemDialog {

	@Inject
	UserLogic userLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	JpaLogic jpaLogic;

	private static final Logger logger = LogManager.getLogger(AsCloudSafeDialog.class);

	String contentString;
	boolean contentAsText = false;
	boolean privateData;

	String domainName;
	String loginId;

	private UploadedFile uploadedFile;

	CloudSafeEntity cloudDataEntity;

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		CloudSafeEntity cloudDataWoContentEntityEntity = (CloudSafeEntity) this.getActionObject();
		privateData = false;
		try {
			if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT) && cloudDataWoContentEntityEntity.getOptions() != null
					&& cloudDataWoContentEntityEntity.getOptions().contains(CloudSafeOptions.ENC.name())) {
				privateData = true;
				throw new DcemException(DcemErrorCodes.CLOUDDATA_PRIVATE, cloudDataWoContentEntityEntity.toString());
			} else {
				if (cloudDataWoContentEntityEntity.getId() == null) {
					cloudDataEntity = new CloudSafeEntity();
				} else {
					cloudDataEntity = cloudSafeLogic.getCloudSafe(cloudDataWoContentEntityEntity.getId());
				}

				if (isAsText() == false) {
					contentAsText = false;
					contentString = null;
				}
				if (contentAsText == true) {
	//				contentString = cloudDataEntity.getContentText();
				}
				
			}
			if (cloudDataWoContentEntityEntity.getUser() != null) {
				String[] domainUser = cloudDataWoContentEntityEntity.getUser().getLoginId().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
				if (domainUser.length > 1) {
					domainName = domainUser[0];
					loginId = domainUser[1];
				} else {
					loginId = cloudDataWoContentEntityEntity.getUser().getLoginId();
				}
			} else {
				loginId = null;
				domainName = null;
			}
		} catch (DcemException e) {
			logger.error("Couldn't read cload data: " + cloudDataWoContentEntityEntity.getId(), e);
			throw e;
		} catch (Exception e) {
			logger.error("Couldn't read cload data: " + cloudDataWoContentEntityEntity.getId(), e);
			JsfUtils.addErrorMessage("Couldn't read cload data: + " + e.toString());
			throw e;
		}
		super.show(dcemView, autoViewAction);
	}

	@Override
	public boolean actionOk() throws Exception {

//		if (contentAsText == true) {
//			cloudDataEntity.setContentText(contentString);
//		} else {
//			if (uploadedFile == null || uploadedFile.getContent().length == 0) {
//				JsfUtils.addErrorMessage("Please upload a file.");
//				return false;
//			}
//			cloudDataEntity.setContent(uploadedFile.getContent());
//		}
//		try {
//			cloudDataEntity.setLoginId(loginId);
//			cloudDataLogic.verifyCloudData(cloudDataEntity);
//			cloudDataLogic.setCloudData(cloudDataEntity, null, true);
//
//		} catch (DcemException e) {
//			JsfUtils.addErrorMessage(e.toString()); // TODO
//			return false;
//		}
		return true;
	}

	@Override
	public String getHeight() {
		return "750";
	}

	@Override
	public String getWidth() {
		return "600";
	}

	public CloudSafeOwner[] getOwners() {
		return CloudSafeOwner.values();
	}

	public List<String> completeUser(String name) {
		if (domainName == null || domainName.isEmpty()) {
			return userLogic.getCompleteUserList(name, 50);
		} else {
			return userLogic.getCompleteUserList(domainName + DcemConstants.DOMAIN_SEPERATOR + name, 50);
		}

	}

	public String getLoginId() {
		if (cloudDataEntity.getUser() == null) {
			return null;
		}
		return cloudDataEntity.getUser().getLoginId();
	}

	public List<String> getDevices() {
		if (cloudDataEntity.getLoginId() == null) {
			return null;
		}
		DcemUser user = null;
		try {
			user = userLogic.getUser(cloudDataEntity.getLoginId());
		} catch (DcemException e) {
			JsfUtils.addWarningMessage(AsModule.RESOURCE_NAME, "propertyDialog.invalidUser");
		}
		if (user == null) {
			JsfUtils.addWarningMessage(AsModule.RESOURCE_NAME, "propertyDialog.invalidUser");
			return null;
		}
		return deviceLogic.getDeviceNames(user);
	}

	public void uploadContent() {

	}

	/**
	 * 
	 */
	public void downloadContent() {
//		try {
//			JsfUtils.downloadFile(MediaType.APPLICATION_JSON, "DCEM_CloadData_" + cloudDataEntity.getName(), cloudDataEntity.getContent());
//		} catch (IOException e) {
//			JsfUtils.addErrorMessage(e.toString());
//		}
	}

	public boolean isAsText() {
		// CloudDataEntity cloudDataEntity = (CloudDataEntity) this.getActionObject();
//		if (cloudDataEntity.getContent() != null && cloudDataEntity.getContent().length > (1024 * 8)) {
//			JsfUtils.addFacesInformationMessage("Data is too long to display as text");
//			return false;
//		}
		return true;
	}

	public void listenContentAsText(AjaxBehaviorEvent event) {
//		if (contentAsText == true) {
//			try {
//				contentString = cloudDataEntity.getContentText();
//			} catch (Exception e) {
//				JsfUtils.addErrorMessage(e.toString());
//			}
//		}
	}

	public String getContentString() {
		return contentString;
	}

	public void setContentString(String contentString) {
		this.contentString = contentString;
	}

	public boolean isContentAsText() {
		return contentAsText;
	}

	public void setContentAsText(boolean contentAsText) {
		this.contentAsText = contentAsText;
	}

	
	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public CloudSafeEntity getCloudDataEntity() {
		return cloudDataEntity;
	}

	public void setCloudDataEntity(CloudSafeEntity cloudDataEntity) {
		this.cloudDataEntity = cloudDataEntity;
	}

	public void leavingDialog() {
//		cloudDataEntity.setContent(null);
//		uploadedFile = null;
//		contentString = null;
//		loginId = null;
//		domainName = null;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public boolean isPrivateData() {
		return privateData;
	}

	public void setPrivateData(boolean privateData) {
		this.privateData = privateData;
	}

}
