package com.doubleclue.dcup.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.TreeNode;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.ActionItem;

@Named("cloudSafeShareDialog")
@SessionScoped
public class CloudSafeShareDialog implements Serializable {

	private static final long serialVersionUID = 1L;
	static private Logger logger = LogManager.getLogger(CloudSafeShareDialog.class);

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	UserPortalModule userPortalModule;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	CloudSafeView cloudSafeView;

	@Inject
	GroupLogic groupLogic;

	List<CloudSafeShareEntity> cloudSafeShareEntities = null;
	List<CloudSafeShareEntity> cloudSafeShareFolderEntities = null;

	private TreeNode selectedFolder;
	private CloudSafeShareEntity selectedShareCloudSafe;
	private CloudSafeShareEntity selectedShareCloudSafeFolder;

	private String shareCloudSafeName = null;
	private boolean editingShareCloudSafe = false;

	private boolean shareCloudSafeWriteAccess = false;
	private boolean shareCloudSafeRestrictDownload = false;
	private String shareCloudSafeType = "u";
	boolean openedDialog = false;

	@PostConstruct
	public void init() {

	}

	public void actionOpenAddShareCloudSafe() {
		shareCloudSafeType = "u";
		shareCloudSafeName = null;
		shareCloudSafeWriteAccess = false;
		editingShareCloudSafe = false;
		cloudSafeView.showDialog("shareEditDlg");
	}

	public void actionOpenAddShareCloudSafeFolder() {
		shareCloudSafeType = "u";
		shareCloudSafeName = null;
		shareCloudSafeWriteAccess = false;
		editingShareCloudSafe = false;
		cloudSafeView.showDialog("shareFolderEditDlg");
	}

	public void actionOpenEditShareCloudSafe() {
		if (selectedShareCloudSafe != null) {
			if (selectedShareCloudSafe.getUser() != null) {
				shareCloudSafeType = "u";
				shareCloudSafeName = selectedShareCloudSafe.getUser().getLoginId();
			} else {
				shareCloudSafeType = "g";
				shareCloudSafeName = selectedShareCloudSafe.getGroup().getName();
			}
			shareCloudSafeWriteAccess = selectedShareCloudSafe.isWriteAccess();
			editingShareCloudSafe = true;
			cloudSafeView.showDialog("shareEditDlg");
		} else {
			JsfUtils.addWarnMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.selectUser"), "errorMessageShareDialog");
			PrimeFaces.current().ajax().update("shareForm:errorMessageShareDialog");
		}
	}

	public void actionOpenEditShareCloudSafeFolder() {
		if (selectedShareCloudSafeFolder != null) {
			if (selectedShareCloudSafeFolder.getUser() != null) {
				shareCloudSafeType = "u";
				shareCloudSafeName = selectedShareCloudSafeFolder.getUser().getLoginId();
			} else {
				shareCloudSafeType = "g";
				shareCloudSafeName = selectedShareCloudSafeFolder.getGroup().getName();
			}
			shareCloudSafeWriteAccess = selectedShareCloudSafeFolder.isWriteAccess();
			editingShareCloudSafe = true;
			cloudSafeView.showDialog("shareFolderEditDlg");
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectUser"));
		}
	}

	public void actionDeleteShareCloudSafe() {
		if (selectedShareCloudSafe != null) {
			try {
				cloudSafeLogic.removeShareCloudSafeFile(selectedShareCloudSafe);
				selectedShareCloudSafe = null;
				cloudSafeShareEntities = null;
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectUser"));
		}
	}

	public void actionDeleteShareCloudSafeFolder() {
		if (selectedShareCloudSafeFolder != null) {
			try {
				cloudSafeLogic.removeShareCloudSafeFile(selectedShareCloudSafeFolder);
				selectedShareCloudSafeFolder = null;
				cloudSafeShareEntities = null;
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectUser"));
		}
	}

	public List<CloudSafeShareEntity> getShareCloudSafeUsersAccess() {

		if (cloudSafeView.getSelectedCloudSafeFiles() != null && cloudSafeView.getSelectedCloudSafeFiles().size() == 1) {
			try {
				if (cloudSafeShareEntities == null) {
					cloudSafeShareEntities = cloudSafeLogic.getSharedCloudSafeUsersAccess(cloudSafeView.getSelectedCloudSafeFiles().get(0));
				}
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}

		} else {
			cloudSafeShareEntities = null;
		}
		return cloudSafeShareEntities;
	}

	public List<CloudSafeShareEntity> getShareFolderCloudSafeUsersAccess() {
		CloudSafeEntity selectedFolder = (CloudSafeEntity) cloudSafeView.getSelectedSharedNode().getData();
		cloudSafeShareFolderEntities = null;
		if (selectedFolder != null) {
			try {
				if (cloudSafeShareFolderEntities == null) {
					cloudSafeShareFolderEntities = cloudSafeLogic.getSharedCloudSafeUsersAccess(selectedFolder);
				}
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}

		} else {
			cloudSafeShareFolderEntities = null;
		}
		PrimeFaces.current().ajax().update("shareFolderForm:sharedFolderUsersTable");

		return cloudSafeShareFolderEntities;
	}

	public String getSelectedCloudSafeName() {
		if (cloudSafeView.getSelectedCloudSafeFiles() != null && cloudSafeView.getSelectedCloudSafeFiles().size() == 1) {
			return cloudSafeView.getSelectedCloudSafeFiles().get(0).getName();
		}
		return null;
	}

	public void actionCloseShareCloudSafeDialog() {
		selectedShareCloudSafe = null;
		cloudSafeView.hideDialog("shareDlg");
		openedDialog = false;
	}

	public void actionAddEditShareCloudSafe(boolean isFolder) throws DcemException {
		if (shareCloudSafeName.equals(portalSessionBean.getUserName())) {
			JsfUtils.addWarnMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.notAllowedToShareToSelf"), "shareEditDlgMessages");
			PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
			return;
		}
		if (shareCloudSafeName.trim().isEmpty()) {
			JsfUtils.addErrorMessageToComponentId(portalSessionBean.getResourceBundle().getString("error.missingName"), "shareEditDlgMessages");
			PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
			return;
		}
		portalSessionBean.isActionEnable(ActionItem.CLOUD_SAFE_SHARE_FILE_ACTION);
		CloudSafeShareEntity cloudSafeShareEntity;
		if (isFolder) {
			cloudSafeShareEntity = selectedShareCloudSafeFolder;
		} else {
			cloudSafeShareEntity = selectedShareCloudSafe;
		}
		if (shareCloudSafeWriteAccess == true && cloudSafeView.getSelectedCloudSafeFiles().get(0).isOption(CloudSafeOptions.ENC) == false) {
			JsfUtils.addErrorMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.notAllowedToShareProtectedFile"),
					"shareEditDlgMessages");
			PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
			return;
		}
		if (editingShareCloudSafe == false) {
			cloudSafeShareEntity = new CloudSafeShareEntity();
		}
		DcemGroup dcemGroup = null;
		String userLoginId = null;
		switch (shareCloudSafeType) {
		case "u":
			userLoginId = shareCloudSafeName;
			break;
		case "g":
			dcemGroup = findGroup(shareCloudSafeName);
			if (dcemGroup == null) {
				JsfUtils.addWarnMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.wrongGroup"), "shareEditDlgMessages");
				PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
				return;
			}
			break;
		default:
			JsfUtils.addWarnMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.chooseCloudSafeShareType"), "shareEditDlgMessages");
			PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
			return;
		}
		if (isFolder == false) {
			cloudSafeShareEntity.setCloudSafe(cloudSafeView.getSelectedCloudSafeFiles().get(0));
		} else {
			CloudSafeEntity selectedFolder = (CloudSafeEntity) cloudSafeView.getSelectedSharedNode().getData();
			cloudSafeShareEntity.setCloudSafe(selectedFolder);
		}
		cloudSafeShareEntity.setWriteAccess(shareCloudSafeWriteAccess);
		cloudSafeShareEntity.setRestrictDownload(shareCloudSafeRestrictDownload);
		cloudSafeShareEntities = null;

		try {
			cloudSafeLogic.addOrEditShareCloudSafeFile(cloudSafeShareEntity, userLoginId, dcemGroup);
			if (isFolder == false) {
				cloudSafeView.hideDialog("shareEditDlg");
			} else {
				cloudSafeView.hideDialog("shareFolderEditDlg");
			}
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessageToComponentId(portalSessionBean.getErrorMessage(e), "shareEditDlgMessages");
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessageToComponentId(e.toString(), "shareEditDlgMessages");
		}
		PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
		PrimeFaces.current().executeScript("PF('shareFolderEditDlg').hide();");
	}

	public List<String> completeUser(String name) {
		try {
			if (shareCloudSafeType.equals("u")) {
				return userLogic.getCompleteUserList(name, 50);
			}
			if (shareCloudSafeType.equals("g")) {
				return completeGroup(name);
			}
			return null;
		} catch (Exception e) {
			logger.warn("couldn't retrive user list ", e);
			return null;
		}
	}

	public List<String> completeGroup(String name) {
		List<String> list = new ArrayList<String>();
		name = name.toLowerCase();
		for (DcemGroup dcemGroup : cloudSafeView.getAllUsersGroups()) {
			if (dcemGroup.getRawName().toLowerCase().startsWith(name)) {
				list.add(dcemGroup.getName());
			}
		}
		return list;
	}

	public DcemGroup findGroup(String name) {
		for (DcemGroup dcemGroup : cloudSafeView.getAllUsersGroups()) {
			if (dcemGroup.getName().equalsIgnoreCase(name)) {
				return dcemGroup;
			}
		}
		return null;
	}

	public void onChangeShareCloudSafeType(String cloudSafeType) {
		shareCloudSafeType = cloudSafeType;
		return;
	}

	public void actionCloseShareCloudSafeFolderDialog() {
		selectedShareCloudSafe = null;
		cloudSafeView.hideDialog("shareFolderDlg");
	}

	public CloudSafeShareEntity getSelectedShareCloudSafe() {
		return selectedShareCloudSafe;
	}

	public void setSelectedShareCloudSafe(CloudSafeShareEntity selectedShareCloudSafe) {
		this.selectedShareCloudSafe = selectedShareCloudSafe;
	}

	public String getShareCloudSafeName() {
		return shareCloudSafeName;
	}

	public void setShareCloudSafeName(String shareCloudSafeName) {
		this.shareCloudSafeName = shareCloudSafeName;
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
		return shareCloudSafeType;
	}

	public void setShareCloudSafeType(String shareCloudSafeType) {
		this.shareCloudSafeType = shareCloudSafeType;
	}

	public void onOpenDialog() {
		openedDialog = true;
		cloudSafeShareEntities = null;
	}

	public TreeNode getSelectedFolder() {
		return selectedFolder;
	}

	public void setSelectedFolder(TreeNode selectedFolder) {
		this.selectedFolder = selectedFolder;
	}

	public CloudSafeShareEntity getSelectedShareCloudSafeFolder() {
		return selectedShareCloudSafeFolder;
	}

	public void setSelectedShareCloudSafeFolder(CloudSafeShareEntity selectedShareCloudSafeFolder) {
		this.selectedShareCloudSafeFolder = selectedShareCloudSafeFolder;
	}

	public boolean isEnableAutoComplete() {
		if (userPortalModule.getModulePreferences() != null) {
			return userPortalModule.getModulePreferences().isEnableAutoComplete();
		}
		return false;
	}

	public boolean isOpenedDialog() {
		return openedDialog;
	}

	public void setOpenedDialog(boolean openedDialog) {
		this.openedDialog = openedDialog;
	}

}
