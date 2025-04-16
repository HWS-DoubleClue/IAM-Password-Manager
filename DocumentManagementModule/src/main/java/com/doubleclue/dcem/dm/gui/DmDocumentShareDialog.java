package com.doubleclue.dcem.dm.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;

@Named("dmDocumentShareDialog")
@SessionScoped
public class DmDocumentShareDialog implements Serializable {

	private static final long serialVersionUID = 1L;
	static private Logger logger = LogManager.getLogger(DmDocumentShareDialog.class);

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	DmDocumentView dmDocumentView;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DmNewDocumentView dmNewDocumentView;

	List<CloudSafeShareEntity> cloudSafeShareEntities = null;
	private CloudSafeShareEntity selectedShareCloudSafe;
	private CloudSafeEntity cloudSafeEntity;
	private String shareGroupName = null;
	private boolean editingShareCloudSafe = false;
	private boolean shareCloudSafeWriteAccess = false;
	private boolean shareCloudSafeRestrictDownload = false;
	private String shareType = "u";
	boolean openedDialog = false;
	private ResourceBundle resourceBundle;
	private DcemUser dcemUser;

	@PostConstruct
	public void init() {
		resourceBundle = JsfUtils.getBundle(DocumentManagementModule.RESOURCE_NAME, operatorSessionBean.getLocale());
	}

	public void actionOpenAddShareCloudSafe() {
		shareType = "u";
		shareGroupName = null;
		shareCloudSafeWriteAccess = false;
		editingShareCloudSafe = false;
		dmDocumentView.showDialog("shareEditDlg");
	}

	public void actionOpenEditShareCloudSafe() {
		if (selectedShareCloudSafe != null) {
			if (selectedShareCloudSafe.getUser() != null) {
				shareType = "u";
				dcemUser = selectedShareCloudSafe.getUser();
			} else {
				shareType = "g";
				shareGroupName = selectedShareCloudSafe.getGroup().getName();
			}
			shareCloudSafeWriteAccess = selectedShareCloudSafe.isWriteAccess();
			shareCloudSafeRestrictDownload = selectedShareCloudSafe.isRestrictDownload();
			editingShareCloudSafe = true;
			dmDocumentView.showDialog("shareEditDlg");
		} else {
			JsfUtils.addWarnMessageToComponentId(resourceBundle.getString("message.selectUser"), "errorMessageShareDialog"); 
			PrimeFaces.current().ajax().update("shareForm:errorMessageShareDialog");
		}
	}

	public void actionDeleteShareCloudSafe() {
		if (selectedShareCloudSafe != null) {
			try {
				cloudSafeLogic.removeShareCloudSafeFile(selectedShareCloudSafe);
				selectedShareCloudSafe = null;
				cloudSafeShareEntities = null;
				JsfUtils.addInfoMessageToComponentId(resourceBundle.getString("editDocument.message.shareDeleted"), "shareTabMsg");
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} else {
			JsfUtils.addWarnMessage(resourceBundle.getString("message.selectUser")); 
		}
	}

	public List<CloudSafeShareEntity> getShareCloudSafeUsersAccess() {
		cloudSafeEntity = dmNewDocumentView.getCloudSafeEntity();
		if (cloudSafeEntity != null && cloudSafeEntity.getId() != null) {
			try {
				if (cloudSafeShareEntities == null) {
					cloudSafeShareEntities = cloudSafeLogic.getSharedCloudSafeUsersAccess(cloudSafeEntity);
				}
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}

		} else {
			cloudSafeShareEntities = null;
		}
		return cloudSafeShareEntities;
	}

	public void actionAddEditShareCloudSafe() throws DcemException {
		DcemGroup dcemGroup = null;
		switch (shareType) {
		case "u":
			if (dcemUser.equals(operatorSessionBean.getDcemUser())) {
				JsfUtils.addWarnMessageToComponentId(resourceBundle.getString("message.notAllowedToShareToSelf"), "shareEditDlgMessages"); 
				PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
				return;
			}
			dcemGroup = null;
			break;
		case "g":
			dcemUser = null;
			if (shareGroupName  == null || shareGroupName.trim().isEmpty()) {
				JsfUtils.addErrorMessageToComponentId(resourceBundle.getString("error.missingName"), "shareEditDlgMessages"); 
				PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
				return;
			}
			dcemGroup = findGroup(shareGroupName);
			if (dcemGroup == null) {
				JsfUtils.addWarnMessageToComponentId(resourceBundle.getString("message.wrongGroup"), "shareEditDlgMessages");
				PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
				return;
			}
			break;
		default:
			JsfUtils.addWarnMessageToComponentId(resourceBundle.getString("message.chooseCloudSafeShareType"), "shareEditDlgMessages");
			PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
			return;
		}
		cloudSafeEntity = dmNewDocumentView.getCloudSafeEntity();
		CloudSafeShareEntity cloudSafeShareEntity;
		if (editingShareCloudSafe == false) {
			cloudSafeShareEntity = new CloudSafeShareEntity();
		} else {
			cloudSafeShareEntity = selectedShareCloudSafe;
		}
		cloudSafeShareEntity.setCloudSafe(cloudSafeEntity);
		cloudSafeShareEntity.setWriteAccess(shareCloudSafeWriteAccess);
		cloudSafeShareEntity.setRestrictDownload(shareCloudSafeRestrictDownload);
		cloudSafeShareEntities = null;

		try {
			cloudSafeLogic.addOrEditShareCloudSafeFile(cloudSafeShareEntity, dcemUser, dcemGroup);
			dmDocumentView.hideDialog("shareEditDlg");
			JsfUtils.addInfoMessageToComponentId(resourceBundle.getString("editDocument.message.fileShared"), "shareTabMsg");
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessageToComponentId(e.getLocalizedMessage(), "shareEditDlgMessages");
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessageToComponentId(e.toString(), "shareEditDlgMessages");
		}
		PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
		PrimeFaces.current().executeScript("PF('shareFolderEditDlg').hide();");
	}

	public List<String> completeGroup(String name) {
		List<String> list = new ArrayList<String>();
		name = name.toLowerCase();
		for (DcemGroup dcemGroup : operatorSessionBean.getUserGroups()) {
			if (dcemGroup.getRawName().toLowerCase().startsWith(name)) {
				list.add(dcemGroup.getName());
			}
		}
		return list;
	}

	public DcemGroup findGroup(String name) {
		for (DcemGroup dcemGroup : operatorSessionBean.getUserGroups()) {
			if (dcemGroup.getName().equalsIgnoreCase(name)) {
				return dcemGroup;
			}
		}
		return null;
	}

	public void onChangeShareCloudSafeType(String cloudSafeType) {
		shareType = cloudSafeType;
		return;
	}

	public CloudSafeShareEntity getSelectedShareCloudSafe() {
		return selectedShareCloudSafe;
	}

	public void setSelectedShareCloudSafe(CloudSafeShareEntity selectedShareCloudSafe) {
		this.selectedShareCloudSafe = selectedShareCloudSafe;
	}

	public boolean isEditingShareCloudSafe() {
		return editingShareCloudSafe;
	}

	public void setEditingShareCloudSafe(boolean editingShareCloudSafe) {
		this.editingShareCloudSafe = editingShareCloudSafe;
	}

	public boolean isShareCloudSafeWriteAccess() {
		return shareCloudSafeWriteAccess;
	}

	public void setShareCloudSafeWriteAccess(boolean shareCloudSafeWriteAccess) {
		this.shareCloudSafeWriteAccess = shareCloudSafeWriteAccess;
	}

	public boolean isShareCloudSafeRestrictDownload() {
		return shareCloudSafeRestrictDownload;
	}

	public void setShareCloudSafeRestrictDownload(boolean shareCloudSafeRestrictDownload) {
		this.shareCloudSafeRestrictDownload = shareCloudSafeRestrictDownload;
	}

	public String getShareCloudSafeType() {
		return shareType;
	}

	public void setShareCloudSafeType(String shareCloudSafeType) {
		this.shareType = shareCloudSafeType;
	}

	public boolean isEnableAutoComplete() {
		return true; // TODO Autocomplete in Preferences
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	public String getShareGroupName() {
		return shareGroupName;
	}

	public void setShareGroupName(String shareGroupName) {
		this.shareGroupName = shareGroupName;
	}
}
