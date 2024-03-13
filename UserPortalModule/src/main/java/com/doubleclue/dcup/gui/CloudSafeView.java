package com.doubleclue.dcup.gui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.io.InvalidCipherTextIOException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.DcupConstants;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.StringUtils;

@Named("cloudSafeView")
@SessionScoped
public class CloudSafeView extends AbstractPortalView {

	private static final long serialVersionUID = 1L;
	private static final String DOWNLOAD_DLG = "downloadDlg";
	private static final String OPEN_FOLDER_DLG = "openFolderDlg";
	private static final String MY_DOUBLE_CLUE_FILE_ZIP = "MyDoubleClueFiles.zip";

	static private Logger logger = LogManager.getLogger(CloudSafeView.class);

	@Inject
	KeePassView keePassView;

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	CloudSafeShareDialog cloudSafeShareDialog;

	@Inject
	UserLogic userLogic;

	@Inject
	DcemApplicationBean dcemApplication;

	@Inject
	GroupLogic groupLogic;

	private List<CloudSafeEntity> cloudSafeEntityFiles;
	private List<CloudSafeEntity> selectedCloudSafeFiles = new ArrayList<CloudSafeEntity>();
	private List<CloudSafeEntity> selectedFilesToCut = new ArrayList<CloudSafeEntity>();
	List<CloudSafeEntity> selectedCloudSafeEntity = new ArrayList<CloudSafeEntity>();
	List<CloudSafeShareEntity> sharedCloudSafeFiles = null;
	private Integer currentFolderId;

	private List<CloudSafeEntity> currentSelectedFiles;
	private List<DcemUploadFile> uploadedFiles;
	private LocalDateTime expiryDate;

	private boolean confirmedUpload = false;
	private boolean uploadingSharedFile = false;
	private boolean passwordProtected;
	private boolean defineOwnerGroup;
	private String filePassword;
	private String passwordToEncryptContent;
	private CloudSafeEntity toDownLoadCloudSafeFile;
	private CloudSafeEntity toOpenFileorFolder;
	private String selectedFileName;

	boolean editFolderProcess;

	private boolean shouldRefreshCurrentFiles = true;
	private boolean shouldRefreshCurrentSharedFiles = true;

	private CloudSafeEntity moveToFolder;
	private CloudSafeEntity moveEntry;

	private CloudSafeEntity selectedFolder;
	private String addFolderName;
	private String selectedFolderName;
	private MenuModel breadCrumbModel;
	private boolean downloadSharedFileFPD = false;
	private MenuModel breadCrumbModelSharedFiles;
	private CloudSafeShareEntity selectedSharedCloudSafeFile;
	private List<CloudSafeShareEntity> currentSelectedSharedCloudSafeFiles = new ArrayList<CloudSafeShareEntity>();
	private CloudSafeShareEntity selectedSharedCloudSafeFolder;
	private List<DcemGroup> allUsersGroups = null;
	private String downloadFileName;
	private String folderName;
	private String searchFile;
	private boolean multipleFile = false;
	private CloudSafeEntity cloudSafeRoot;
	private boolean selectedFile;
	private boolean listView;
	private boolean folder;
	private CloudSafeEntity cloudSafeEntityForShow;
	private DcemUser loggedInUser;
	private String ownerGroup;
	private DcemGroup selectedDcemGroup;

	@PostConstruct
	public void init() {
		breadCrumbModel = new DefaultMenuModel();
		DefaultMenuItem menuItem = new DefaultMenuItem();
		menuItem.setId("0");
		menuItem.setValue(portalSessionBean.getResourceBundle().getString("breadCrumb.myFiles"));
		menuItem.setCommand("#{cloudSafeView.breadCrumbAction (0, 0)}");
		menuItem.setUpdate("cloudSafeForm:cloudSafeTable cloudSafeForm:cloudSafeKacheln");
		breadCrumbModel.getElements().add(menuItem);

		breadCrumbModelSharedFiles = new DefaultMenuModel();
		DefaultMenuItem sharedMenuItem = new DefaultMenuItem();
		sharedMenuItem.setId("0");
		sharedMenuItem.setValue(portalSessionBean.getResourceBundle().getString("breadCrumb.myFiles"));
		sharedMenuItem.setCommand("#{cloudSafeView.breadCrumbSharedFilesAction (0, 0, 0)}");
		sharedMenuItem.setUpdate("cloudSafeForm:cloudSafeTable cloudSafeForm:cloudSafeKacheln");
		breadCrumbModelSharedFiles.getElements().add(sharedMenuItem);

		cloudSafeRoot = cloudSafeLogic.getCloudSafeRoot();
		selectedFile = false;
		listView = true;
	}

	public CloudSafeEntity getMoveToFolder() {
		return moveToFolder;
	}

	public void setMoveToFolder(CloudSafeEntity moveToFolder) {
		this.moveToFolder = moveToFolder;
	}

	public CloudSafeEntity getMoveEntry() {
		return moveEntry;
	}

	public void setMoveEntry(CloudSafeEntity moveEntry) {
		this.moveEntry = moveEntry;
	}

	public boolean isMultipleUpload() {
		if (uploadingSharedFile) {
			return false;
		} else {
			return true;
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		if (uploadedFiles == null) {
			uploadedFiles = new LinkedList<>();
		}

		File tempFile = null;
		FileOutputStream os = null;
		InputStream is = null;
		try {
			tempFile = File.createTempFile("dcem-", "-cloudSafe");
			os = new FileOutputStream(tempFile);
			is = event.getFile().getInputStream();

			byte[] buffer = new byte[1024 * 4];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} catch (IOException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.uploadFile"));
			return;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
		}
		uploadedFiles.add(new DcemUploadFile(event.getFile().getFileName(), tempFile));

		if (uploadingSharedFile) {
			if (uploadedFiles.size() > 1) {
				uploadedFiles.clear();
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.uploadOnlyOneFile"));
				return;
			}
		}
	}

	public String getLimit() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(loggedInUser.getId());
		long limit = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getLimit() : cloudSafeLogic.getDefaultUserLimit();
		return DataUnit.getByteCountAsString(limit);
	}

	public LocalDateTime getLicenceExpires() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(loggedInUser.getId());
		return cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getExpiryDate() : null;

	}

	public String getUsage() {
		CloudSafeLimitEntity cloudSafeLimitEntity = cloudSafeLogic.getCloudSafeLimitEntity(loggedInUser.getId());
		long usage = cloudSafeLimitEntity != null ? cloudSafeLimitEntity.getUsed() : 0;
		return DataUnit.getByteCountAsString(usage);
	}

	public void actionUpload() {
		CloudSafeEntity parent;
		if (uploadingSharedFile) {
			selectedSharedCloudSafeFile = cloudSafeLogic.getCloudShareByShareId((int) selectedSharedCloudSafeFile.getId());
			parent = selectedSharedCloudSafeFile.getCloudSafeEntity().getParent();
		} else {
			parent = (selectedFolder == null || selectedFolder.getId() == 0) ? cloudSafeRoot : selectedFolder;
		}
		if (parent.isRecycled() || parent.getName().toString().equals(DcemConstants.CLOUD_SAFE_RECYCLE_BIN)) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.notAllowToUploadFile"));
			return;
		}
		if (uploadedFiles == null || uploadedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.uploadFile"));
			return;
		}
		if (ownerGroup == null) {
			ownerGroup = "";
		}
		ownerGroup = ownerGroup.trim();
		selectedDcemGroup = null;
		if (ownerGroup.isEmpty() == false) {
			selectedDcemGroup = cloudSafeShareDialog.findGroup(ownerGroup);
			if (selectedDcemGroup == null) {
				JsfUtils.addWarnMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.wrongGroup"), "shareEditDlgMessages");
				PrimeFaces.current().ajax().update("shareEditForm:shareEditDlgMessages");
				return;
			}
		}
		if (confirmedUpload == false) {
			if (uploadingSharedFile == false) {
				if (getExistingFile(selectedDcemGroup).length() > 0) {
					showDialog("confirmUploadDlg");
				} else {
					confirmedUpload = true;
				}
			} else {
				if (isExistingFileShare() == false) {
					uploadedFiles.clear();
					if (selectedSharedCloudSafeFile.isWriteAccess() == false) {
						JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noAccessToFile"));
					} else {
						// name does not match
						JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.fileNameDoesNotMatch"));
					}
					return;
				}
				confirmedUpload = true;
			}
		}
		if (confirmedUpload) {
			DcemUser dcemUser;
			try {
				if (uploadingSharedFile) {
					dcemUser = userLogic.getUser(selectedSharedCloudSafeFile.getCloudSafe().getUser().getLoginId());
				} else {
					dcemUser = loggedInUser;
				}
				CloudSafeOwner cloudSafeOwner = CloudSafeOwner.USER;
				if (selectedDcemGroup != null) {
					cloudSafeOwner = CloudSafeOwner.GROUP;
				}
				
				if (parent != null && (parent.isOption(CloudSafeOptions.PWD) || parent.isOption(CloudSafeOptions.FPD))) {
					cloudSafeLogic.saveMultipleFiles(uploadedFiles, dcemUser, passwordToEncryptContent, expiryDate, passwordProtected, true, parent,
							loggedInUser, selectedDcemGroup, cloudSafeOwner);
				} else {
					cloudSafeLogic.saveMultipleFiles(uploadedFiles, dcemUser, filePassword, expiryDate, passwordProtected, true, parent, loggedInUser,
							selectedDcemGroup, cloudSafeOwner);
				}

			} catch (DcemException exception) {
				logger.info("Couldn't upload File ", exception);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exception));
				return;
			} catch (Exception e) {
				logger.warn(e.getLocalizedMessage(), e);
				JsfUtils.addErrorMessage(e.toString());
				return;
			}

			JsfUtils.addInformationMessage(UserPortalModule.RESOURCE_NAME, "cloudSafe.fileUploaded", getUploadedFiles());
			clearUploadDialog();
			cloudSafeEntityFiles = null;
			shouldRefreshCurrentFiles = true;
			hideDialog("uploadDlg");
		}

	}

	public void actionUploadCopy() {
		CloudSafeEntity parent;
		if (uploadingSharedFile) {
			parent = selectedSharedCloudSafeFile.getCloudSafeEntity().getParent();
		} else {
			parent = selectedFolder == null || selectedFolder.getId() == 0 ? cloudSafeRoot : selectedFolder;
		}

		if (uploadedFiles == null || uploadedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.uploadFile"));
			return;
		}
		try {
			for (DcemUploadFile dcemUploadFile : uploadedFiles) {
				String fileName, ext;
				int num = 0;
				fileName = dcemUploadFile.fileName.substring(0, dcemUploadFile.fileName.lastIndexOf('.'));
				ext = dcemUploadFile.fileName.substring(dcemUploadFile.fileName.lastIndexOf('.'));
				while (getExistingFile(null).contains(dcemUploadFile.fileName)) { // TODO Emanuel: we have to check this later
					num++;
					dcemUploadFile.fileName = fileName + "(" + num + ")" + ext;
				}
			}
			if (parent.isOption((CloudSafeOptions.PWD)) || parent.isOption((CloudSafeOptions.FPD))) {
				filePassword = passwordToEncryptContent;
			}
			cloudSafeLogic.saveMultipleFiles(uploadedFiles, loggedInUser, filePassword, expiryDate, passwordProtected, true, parent, loggedInUser, null, CloudSafeOwner.USER);
		} catch (DcemException exception) {
			logger.info(exception);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exception));
			return;
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		JsfUtils.addInformationMessage(UserPortalModule.RESOURCE_NAME, "cloudSafe.fileUploaded", getUploadedFiles());
		clearUploadDialog();
		cloudSafeEntityFiles = null;
		shouldRefreshCurrentFiles = true;
		hideDialog("uploadDlg");
		hideDialog("confirmUploadDlg");
	}

	private void clearUploadDialog() {
		expiryDate = null;
		uploadedFiles = null;
		confirmedUpload = false;
	}

	public void closeUploadDialog() {
		if (uploadedFiles != null) {
			for (DcemUploadFile dcemUploadFile : uploadedFiles) {
				dcemUploadFile.file.delete();
			}
		}
		clearUploadDialog();
		hideDialog("uploadDlg");
	}

	public List<CloudSafeEntity> getAsApiCloudSafeFiles(Integer parentId, DcemUser user) {
		List<CloudSafeEntity> result = null;
		if (parentId != null && parentId == 0) {
			CloudSafeEntity root = cloudSafeLogic.getCloudSafeRoot();
			parentId = root.getId();
		}
		try {
			result = cloudSafeLogic.getCloudSafeByUserAndParentId(parentId, user, allUsersGroups);
		} catch (Exception e) {
			logger.warn("", e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
		return result;
	}

	public String getConfirmUploadMessage() {
		return MessageFormat.format(portalSessionBean.getResourceBundle().getString("message.confirmUploadFile"), getExistingFile(selectedDcemGroup));
	}

	public void actionConfirmUpload() {
		confirmedUpload = true;
		hideDialog("confirmUploadDlg");
		actionUpload();
	}

	@Override
	public String getName() {
		return DcupViewEnum.cloudSafeView.name();
	}

	@Override
	public String getPath() {
		return "cloudSafeView.xhtml";
	}

	public boolean isPermanentDelete() {
		if (selectedCloudSafeFiles.size() > 0) {
			return selectedCloudSafeFiles.get(0).isRecycled() || DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(selectedCloudSafeFiles.get(0).getName());
		}
		return false;
	}

	public void deleteCloudSafeFiles() {
		try {
			// delete files in one transaction
			List<CloudSafeDto> deletedDbFiles;
			if (listView == true) {
				deletedDbFiles = cloudSafeLogic.deleteCloudSafeFiles(selectedCloudSafeFiles, loggedInUser, true);
			} else {
				deletedDbFiles = cloudSafeLogic.deleteCloudSafeFiles(selectedCloudSafeEntity, loggedInUser, true);
			}
			cloudSafeLogic.deleteCloudSafeFilesContent(deletedDbFiles);
		} catch (Exception e) {
			logger.warn("Couldn't delete files", e);
			JsfUtils.addErrorMessage("Couldn't delete files. " + e.toString());
			return;
		}
		cloudSafeEntityFiles = null;
		selectedCloudSafeFiles.clear();
		shouldRefreshCurrentFiles = true;
		PrimeFaces.current().ajax().update("cloudSafeForm:storageInfo");
		PrimeFaces.current().ajax().update("cloudSafeForm:cloudSafeKacheln");
		hideDialog("confirmDlg");
	}

	public void validateCloudSafeFiles() {
		if (listView == true && selectedCloudSafeFiles == null || listView == true && selectedCloudSafeFiles.isEmpty() == true
				|| listView == false && selectedCloudSafeEntity == null || listView == false && selectedCloudSafeEntity.isEmpty() == true) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
			return;
		} else {
			for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
				CloudSafeEntity cloudSafeFile = cloudSafeLogic.getCloudSafe(cloudSafeEntity.getId());
				if (cloudSafeFile.getOwner().equals(CloudSafeOwner.GROUP) && cloudSafeFile.getDiscardAfter() != null) {
					JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.fileOwnedByGroupDeleted"));
					return;
				}
			}
		}
		if (listView == true) {
			showDialog("confirmDlg");
		} else {
			selectedCloudSafeFiles = selectedCloudSafeEntity;
			showDialog("confirmDlg");
		}
		PrimeFaces.current().ajax().update("cloudSafeForm:deleteTextMessage");
	}

	public void onDownloadFiles() {
		if (listView == true) {
			currentSelectedFiles = selectedCloudSafeFiles;
		} else {
			currentSelectedFiles = selectedCloudSafeEntity;
		}
		downloadCloudFiles();
	}

	private void downloadCloudFiles() {
		if (currentSelectedFiles == null || currentSelectedFiles.size() == 0) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
			return;
		}
		isDownloadFoldersContainsProtectedFiles();
		for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {
			if (cloudSafeEntity.isFolder()) {
				toDownLoadCloudSafeFile = currentSelectedFiles.get(0);
				setMultipleFile(true);
				setDownloadFileName(currentSelectedFiles.get(0).getName());
			}
		}
		if (currentSelectedFiles != null && currentSelectedFiles.size() == 1 && !currentSelectedFiles.get(0).isFolder()) {
			toDownLoadCloudSafeFile = currentSelectedFiles.get(0);
			setDownloadFileName(currentSelectedFiles.get(0).getName());
			setMultipleFile(false);
		} else if (currentSelectedFiles != null && currentSelectedFiles.size() > 1) {
			toDownLoadCloudSafeFile = currentSelectedFiles.get(0);
			setDownloadFileName(MY_DOUBLE_CLUE_FILE_ZIP);
			setMultipleFile(true);
		} else {
			setDownloadFileName(currentSelectedFiles.get(0).getName());
		}
		PrimeFaces.current().ajax().update("downloadForm");
		showDialog(DOWNLOAD_DLG);
		return;
	}

	public boolean isDownloadFileWithPassword() {
		if (toDownLoadCloudSafeFile == null || currentSelectedFiles == null) {
			return false;
		}
		if (toDownLoadCloudSafeFile.isOption(CloudSafeOptions.PWD)) {
			return true;
		}
		for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {
			if (cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
				return true;
			}
		}
		if (downloadSharedFileFPD) {
			return true;
		}
		return false;
	}

	public String getDownloadFileName() {
		if (currentSelectedFiles == null) {
			return null;
		}
		return downloadFileName;
	}

	public void verifyPasswordForDownloadFolder() {
		if (currentSelectedFiles == null) {
			return;
		}
		DcemUser currentUser = loggedInUser;
		char[] password = null;
		if (filePassword != null) {
			password = filePassword.toCharArray();
		}
		if (downloadSharedFileFPD == true) {
			passwordToEncryptContent = filePassword;
		}
		try {
			for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {

				if (cloudSafeEntity.isOption(CloudSafeOptions.PWD) && cloudSafeEntity.isFolder()) {
					InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, password, currentUser);
					byte[] buffer = KaraUtils.readInputStream(inputStream);
					int ind = 0;
					for (int i = 8; i < buffer.length; i++) {
						if (buffer[i] != CloudSafeLogic.FOLDER_CONTENT_TO_ENCRYPT[ind++]) {
							throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, null);
						}
					}
				} else if (cloudSafeEntity.isOption(CloudSafeOptions.FPD)) {
					password = passwordToEncryptContent.toCharArray();
				} else {
					if (cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
						InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, password, loggedInUser);
						byte[] buffer = new byte[1024 * 64];
						while (inputStream.read(buffer) != -1) {
						}
					}
				}
			}
			actionDownloadMultipleFilesOrFolders(password);

		} catch (InvalidCipherTextIOException ex) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForDownload");
			logger.info("Could not download file or folder verify your password for User : " + currentUser);
			actionCloseDownloadFile();
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.INVALID_PASSWORD) {
				logger.info("Could not download file or folder verify your password for User : " + currentUser);
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForDownload");
			} else {
				logger.info("Could not download file or folder verify your password for User : " + currentUser, e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			}
			actionCloseDownloadFile();
		} catch (Exception ex) {
			logger.info("somthing went wrong by verifying the password for download, User : " + currentUser, ex);
			JsfUtils.addErrorMessage("Something went wrong. Please contact your administrator Cause:" + ex.toString());
			actionCloseDownloadFile();
		}
	}

	public void actionDownloadMultipleFilesOrFolders(char[] password) {
		if (currentSelectedFiles == null) {
			return;
		}
		try {
			OutputStream output;
			output = JsfUtils.getDownloadFileOutputStream("application/zip", MY_DOUBLE_CLUE_FILE_ZIP);
			ZipOutputStream zipOutputStream = new ZipOutputStream(output);
			String path;
			for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {
				toDownLoadCloudSafeFile = cloudSafeEntity;
				path = cloudSafeEntity.getName();
				zipFoldersOrFiles(path, zipOutputStream, output, cloudSafeEntity, password);
			}
			zipOutputStream.close();
			output.close();
			actionCloseDownloadFile();
			FacesContext.getCurrentInstance().responseComplete();
		} catch (Exception e) {
			logger.info("Coundn't downlaod files " + MY_DOUBLE_CLUE_FILE_ZIP, e);
			FacesContext.getCurrentInstance().responseComplete();
		}
	}

	public StreamedContent actionDownloadFile() {
		if (toDownLoadCloudSafeFile == null) {
			return JsfUtils.getEmptyImage();
		}
		InputStream inputStream;
		char[] password = null;
		if (isDownloadFileWithPassword()) {
			password = filePassword.toCharArray();
		}
		if (downloadSharedFileFPD == true) {
			passwordToEncryptContent = filePassword;
		}
		DefaultStreamedContent defaultStreamedContent;
		try {
			if (toDownLoadCloudSafeFile.isOption(CloudSafeOptions.FPD)) {
				inputStream = cloudSafeLogic.getCloudSafeContentAsStream(toDownLoadCloudSafeFile, passwordToEncryptContent.toCharArray(), loggedInUser);
			} else {
				toDownLoadCloudSafeFile = cloudSafeLogic.getCloudSafe(toDownLoadCloudSafeFile.getId()); // refresh from
																										// DB
				inputStream = cloudSafeLogic.getCloudSafeContentAsStream(toDownLoadCloudSafeFile, password, loggedInUser);
			}
			defaultStreamedContent = DefaultStreamedContent.builder().contentType(MediaType.APPLICATION_FORM_URLENCODED).name(toDownLoadCloudSafeFile.getName())
					.stream(() -> inputStream).build();

			return defaultStreamedContent;
		} catch (DcemException e) {
			logger.info("Coundn't downlaod file " + toDownLoadCloudSafeFile.getName(), e);
			if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_READ_ERROR) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForDownload");
			}
			return null;
		} catch (Throwable e) {
			logger.info("Coundn't downlaod files " + toDownLoadCloudSafeFile.getName(), e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForDownload");
			return null;
		}
	}

	private void zipFoldersOrFiles(String path, ZipOutputStream zipOutputStream, OutputStream output, CloudSafeEntity cloudSafeEntity, char[] password)
			throws IOException {

		zipOutputStream.setLevel(Deflater.BEST_SPEED);
		int length = -1;
		byte[] buffer = new byte[1024 * 16];
		InputStream inputStream;
		ZipEntry zipEntry;
		BufferedInputStream bis;
		try {
			List<CloudSafeEntity> childernSubFolder = getAsApiCloudSafeFiles(cloudSafeEntity != null ? cloudSafeEntity.getId() : null,
					cloudSafeEntity.getUser());
			if (childernSubFolder.size() == 0 && cloudSafeEntity.isFolder()) {
				zipEntry = new ZipEntry(cloudSafeEntity.getName() + "/");
				zipOutputStream.putNextEntry(zipEntry);
			} else if (cloudSafeEntity.isFolder() == false) {

				inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, password, loggedInUser);
				bis = new BufferedInputStream(inputStream);
				zipEntry = new ZipEntry(cloudSafeEntity.getName());
				zipOutputStream.putNextEntry(zipEntry);
				while ((length = bis.read(buffer)) != -1) {
					zipOutputStream.write(buffer, 0, length);
				}
			}
			for (CloudSafeEntity child : childernSubFolder) {
				if (child.isFolder() == false) {
					if (child.isOption(CloudSafeOptions.PWD)) {
						continue;
					}
					inputStream = cloudSafeLogic.getCloudSafeContentAsStream(child, password, loggedInUser);
					bis = new BufferedInputStream(inputStream);
					zipEntry = new ZipEntry(path + "/" + child.getName());
					zipOutputStream.putNextEntry(zipEntry);
					while ((length = bis.read(buffer)) != -1) {
						zipOutputStream.write(buffer, 0, length);
					}
				} else {
					zipFoldersOrFiles(path + "/" + child.getName(), zipOutputStream, output, child, password);
				}
				zipOutputStream.closeEntry();
			}
		} catch (Exception e) {
			logger.info("Couldn't zip files and download them");
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForDownload");
		}
	}

	public boolean isDownloadMultipleProtectedFilesOrFolders() {
		if (currentSelectedFiles == null) {
			return false;
		}
		if (currentSelectedFiles.size() > 1) {
			int foundSize = 0;
			for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {
				if (cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
					foundSize += 1;
				}
			}
			if (foundSize >= 2) {
				return true;
			}
		}
		return false;
	}

	public boolean isDownloadFoldersContainsProtectedFiles() {
		if (currentSelectedFiles == null) {
			return false;
		}
		for (CloudSafeEntity cloudSafeEntity : currentSelectedFiles) {
			return (verifySelectedFoldersOrFiles(cloudSafeEntity) && isDownloadMultipleProtectedFilesOrFolders() == false);
		}
		return false;
	}

	private boolean verifySelectedFoldersOrFiles(CloudSafeEntity cloudSafeEntity) {
		boolean found = false;
		if (cloudSafeEntity.isFolder() == true && cloudSafeEntity.isOption(CloudSafeOptions.PWD) == false) {
			List<CloudSafeEntity> childernSubFolder = getAsApiCloudSafeFiles(cloudSafeEntity != null ? cloudSafeEntity.getId() : null,
					cloudSafeEntity.getUser());
			for (CloudSafeEntity child : childernSubFolder) {
				if (child.isFolder() == false) {
					if (child.isOption(CloudSafeOptions.PWD)) {
						found = true;
					}
				} else if (child.isFolder() == true && child.isOption(CloudSafeOptions.ENC) == true) {
					verifySelectedFoldersOrFiles(child);
					found = false;
				} else if (child.isFolder() == true && child.isOption(CloudSafeOptions.PWD) == true) {
					found = true;
				}
			}
			return found;
		} else if (cloudSafeEntity.isOption(CloudSafeOptions.PWD) == true && currentSelectedFiles.size() > 1) {
			found = true;
		} else if (cloudSafeEntity.isOption(CloudSafeOptions.ENC) == true && currentSelectedFiles.size() > 1) {
			found = false;
		}
		return found;
	}

	public void actionCloseDownloadFile() {
		toDownLoadCloudSafeFile = null;
		currentSelectedFiles = null;
		downloadSharedFileFPD = false;
		if (isError() == false) {
			hideDialog(DOWNLOAD_DLG);
		}
	}

	public void downloadShareCloudSafeFiles() {
		if (currentSelectedSharedCloudSafeFiles == null) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
			return;
		}

		currentSelectedFiles = new ArrayList<CloudSafeEntity>();
		for (CloudSafeShareEntity cloudSafeShareEntity : currentSelectedSharedCloudSafeFiles) {
			if (cloudSafeShareEntity.isRestrictDownload() == true) {
				JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.notAllowToDownload"));
				return;
			}
			if (cloudSafeShareEntity.getCloudSafe().isOption(CloudSafeOptions.FPD)) {
				downloadSharedFileFPD = true;
			} else {
				downloadSharedFileFPD = false;
			}
			currentSelectedFiles.add(cloudSafeShareEntity.getCloudSafe());
		}
		downloadCloudFiles();
	}

	public void openUploadDialog() {
		uploadingSharedFile = false;
		filePassword = null;
		passwordProtected = false;
		uploadedFiles = null;
		ownerGroup = null;
		hideDialog("confirmUploadDlg");
		showDialog("uploadDlg");
	}

	public void openUploadDialogForSharedFile() {
		if (currentSelectedSharedCloudSafeFiles == null || currentSelectedSharedCloudSafeFiles.size() != 1) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
			return;
		} else {
			selectedSharedCloudSafeFile = currentSelectedSharedCloudSafeFiles.get(0);
		}

		if (selectedSharedCloudSafeFile == null) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
		} else if (selectedSharedCloudSafeFile.isWriteAccess() == false) {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.cannotOverwrite"));
		} else {
			// filename = (selectedSharedCloudSafeFile != null) ?
			// selectedSharedCloudSafeFile.getCloudSafeFile().getName() : null;
			uploadingSharedFile = true;
			hideDialog("confirmUploadDlg");
			showDialog("uploadDlg");
		}
	}

	public void openShareDialog() {
		if (listView == false) {
			selectedCloudSafeFiles = selectedCloudSafeEntity;
		}
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.size() == 1) {
			if (selectedCloudSafeFiles.get(0).isRecycled() == true) {
				JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.recycledFileCannotBeShared"));
				return;
			}
			boolean recycleBinExists = false;
			for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
				if (cloudSafeEntity.getName().equals(DcemConstants.CLOUD_SAFE_RECYCLE_BIN)) {
					recycleBinExists = true;
					break;
				}
			}
			if (recycleBinExists == false) {
				cloudSafeShareDialog.onOpenDialog();
				showDialog("shareDlg");
				PrimeFaces.current().ajax().update("shareForm:shareDlg");

			} else {
				JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.recycleBinCannotBeShared"));
			}
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
		}
	}

	void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}

	void hideDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').hide();");
	}

	public String getUploadedFiles() {
		if (uploadedFiles == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (DcemUploadFile uploadedFile : uploadedFiles) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(uploadedFile.fileName);
		}
		return sb.toString();
	}

	public List<CloudSafeShareEntity> getCurrentSharedFiles() {
		if (shouldRefreshCurrentSharedFiles) {
			shouldRefreshCurrentSharedFiles = false;
			try {
				if (selectedSharedCloudSafeFolder == null) {
					sharedCloudSafeFiles = cloudSafeLogic.getUserCloudSafeShareEntities(portalSessionBean.getDcemUser(), null);
				} else {
					List<CloudSafeEntity> children = getAsApiCloudSafeFiles(selectedSharedCloudSafeFolder.getCloudSafe().getId(),
							selectedSharedCloudSafeFolder.getCloudSafe().getUser());
					sharedCloudSafeFiles = new ArrayList<CloudSafeShareEntity>();
					for (CloudSafeEntity child : children) {
						CloudSafeShareEntity shareEntity = new CloudSafeShareEntity(child, selectedSharedCloudSafeFolder.getUser(),
								selectedSharedCloudSafeFolder.getGroup(), selectedSharedCloudSafeFolder.isWriteAccess(),
								selectedSharedCloudSafeFolder.isRestrictDownload());
						shareEntity.setId(selectedSharedCloudSafeFolder.getId());
						sharedCloudSafeFiles.add(shareEntity);
					}
				}
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			} catch (Exception e) {
				logger.warn("getCurrentSharedFiles", e);
				JsfUtils.addErrorMessage(e.toString());
			}
		}
		PrimeFaces.current().ajax().update("cloudSafeShareForm:sharedCloudSafeTable");
		return sharedCloudSafeFiles;
	}

	public String getFolderName(Object folder) {
		return folder.getClass().equals(CloudSafeShareEntity.class) ? ((CloudSafeShareEntity) folder).getCloudSafe().getName()
				: ((CloudSafeEntity) folder).getName();
	}

	@Override
	public void onView() {
		cloudSafeEntityFiles = null;
		shouldRefreshCurrentFiles = true;
		selectedCloudSafeFiles = new ArrayList<CloudSafeEntity>();
		selectedCloudSafeEntity = new ArrayList<CloudSafeEntity>();
		shouldRefreshCurrentSharedFiles = true;
		loggedInUser = portalSessionBean.getDcemUser();
		try {
			allUsersGroups = groupLogic.getAllUserGroups(loggedInUser);
		} catch (DcemException e) {
			return;
		}
	}

	public void actionPasswordProtected() {
	}

	public void actionEncryptProtected() {
	}

	private String getExistingFile(DcemGroup dcemGroup) {
		if (uploadedFiles == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (DcemUploadFile uploadFile : uploadedFiles) {
			for (CloudSafeEntity file : cloudSafeEntityFiles) {
				if (file.getName().equals(uploadFile.fileName)) {
					if (file.getOwner() == CloudSafeOwner.GROUP) {
						if (dcemGroup == null) {
							continue;
						} else if (file.getGroup().equals(dcemGroup) == false) {
							continue;
						}
					} else { // user
						if (dcemGroup != null) {
							continue;
						}
					}
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(file.getName());
				}
			}
		}
		return sb.toString();
	}

	private CloudSafeEntity getExistingCloudSafeEntity() {
		if (uploadedFiles == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		for (DcemUploadFile uploadFile : uploadedFiles) {
			for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
				if (cloudSafeEntity.getName().equals(uploadFile.fileName)) {
					return cloudSafeEntity;
				}
			}
		}
		return null;
	}

	private boolean isExistingFileShare() {
		if (uploadedFiles == null) {
			return false;
		}
		for (DcemUploadFile uploadFile : uploadedFiles) {
			if (selectedSharedCloudSafeFile.isWriteAccess() && uploadFile.fileName.equals(selectedSharedCloudSafeFile.getCloudSafe().getName())) {
				return true;
			}
		}
		return false;
	}

	public void openRenameDialog() {
		if (listView == false) {
			selectedCloudSafeFiles = selectedCloudSafeEntity;
		}
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.size() == 1) {
			setSelectedFileName(selectedCloudSafeFiles.get(0).getName());
			showDialog("renameDlg");
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
		}
		return;
	}

	public void openChangeOwnerShip() {
		if (listView == false) {
			selectedCloudSafeFiles = selectedCloudSafeEntity;
		}
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.size() == 1) {
			CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(selectedCloudSafeFiles.get(0).getId());
			if (cloudSafeEntity.getParent().getName().equals(DcemConstants.CLOUD_SAFE_ROOT) == false || cloudSafeEntity.isFolder()) {
				JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.notAllowedToChangeOwner"));
				return;
			}
			setSelectedFileName(cloudSafeEntity.getName());
			if (cloudSafeEntity.getOwner() == CloudSafeOwner.GROUP) {
				ownerGroup = cloudSafeEntity.getGroup().getName();
			} else {
				ownerGroup = null;
			}
			showDialog("changeOwnerShipDlg");
		} else {
			JsfUtils.addWarnMessage(portalSessionBean.getResourceBundle().getString("message.selectOnlyOneFile"));
		}
		return;
	}

	public void actionChangeOwnerShip() {
		try {
			DcemGroup currentOwnerGroup = null;
			ownerGroup = ownerGroup.trim();
			if (ownerGroup.isEmpty() == false) {
				currentOwnerGroup = cloudSafeShareDialog.findGroup(ownerGroup);
				if (currentOwnerGroup == null) {
					JsfUtils.addErrorMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.wrongGroup"), "changeOwerShipMsg");
					PrimeFaces.current().ajax().update("changeOwnerShipForm:changeOwerShipMsg");
					return;
				}
			}
			CloudSafeEntity cloudSafeEntity = selectedCloudSafeFiles.get(0);
			if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(cloudSafeEntity.getName())) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableTochangeOwnerRecycleBin");
				return;
			}
			if (getExistingFileGroup(cloudSafeEntity, currentOwnerGroup)) {
				JsfUtils.addErrorMessageToComponentId(portalSessionBean.getResourceBundle().getString("message.fileGroupExist"), "changeOwerShipMsg");
				PrimeFaces.current().ajax().update("changeOwnerShipForm:changeOwerShipMsg");
				return;
			}
			cloudSafeLogic.changeOnwerShipCloudSafeEntity(cloudSafeEntity, currentOwnerGroup, loggedInUser);
			shouldRefreshCurrentFiles = true;
			hideDialog("changeOwnerShipDlg");
		} catch (DcemException e) {
			logger.warn("Couldn't change ownerShip of file to Group.", e);
			JsfUtils.addWarnMessage(e.getLocalizedMessage());
			return;
		} catch (Exception e) {
			logger.warn("something went wrong by changing the ownership.", e);
			JsfUtils.addErrorMessage("something went wrong by changing the ownership." + e.toString());
			return;
		}
	}

	private boolean getExistingFileGroup(CloudSafeEntity cloudSafeEntityToGroup, DcemGroup currentOwnerGroup) {
		if (cloudSafeEntityFiles == null || currentOwnerGroup == null) {
			return false;
		}
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
			if (cloudSafeEntity.getName().equalsIgnoreCase(cloudSafeEntityToGroup.getName())
					&& cloudSafeEntity.getGroup().getId().equals(currentOwnerGroup.getId())) {
				return true;
			}
		}
		return false;
	}

	public void setNewFileName() throws DcemException {
		try {
			if (selectedFileName == null || selectedFileName.trim().isEmpty()) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.missingName");
				return;
			}
			validateFileOrFolderName(selectedFileName);
			CloudSafeEntity cloudSafeEntity = selectedCloudSafeFiles.get(0);
			if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(cloudSafeEntity.getName())) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableToRenameRecycleBin");
				return;
			}
			CloudSafeEntity clonedCloudSafeEntity = (CloudSafeEntity) cloudSafeEntity.clone();
			clonedCloudSafeEntity.setName(selectedFileName);
			cloudSafeEntity = cloudSafeLogic.updateCloudSafeEntity(clonedCloudSafeEntity, loggedInUser, true, null);
			shouldRefreshCurrentFiles = true;
			hideDialog("renameDlg");
		} catch (DcemException exception) {
			logger.info(exception);
			JsfUtils.addErrorMessageToComponentId(portalSessionBean.getErrorMessage(exception), "renameDlgMsg");
			PrimeFaces.current().ajax().update("renameForm:renameDlgMsg");
			return;
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
	}

	public void validateFileOrFolderName(String selectedFileOrFolderName) throws DcemException {
		if (StringUtils.isValidFileName(selectedFileOrFolderName) == false) {
			throw new DcemException(DcemErrorCodes.FILE_NAME_WITH_SPECIAL_CHARACTERS, selectedFileOrFolderName);
		}
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getFilePassword() {
		return filePassword;
	}

	public void setFilePassword(String filePassword) {
		this.filePassword = filePassword;
	}

	public boolean isPasswordProtected() {
		return passwordProtected;
	}

	public void setPasswordProtected(boolean passwordProtected) {
		this.passwordProtected = passwordProtected;
	}

	public List<CloudSafeEntity> getSelectedCloudSafeFiles() {
		return selectedCloudSafeFiles;
	}

	public void setSelectedCloudSafeFiles(List<CloudSafeEntity> selectedCloudSafeFiles) {
		this.selectedCloudSafeFiles = selectedCloudSafeFiles;
	}

	public List<CloudSafeShareEntity> getSelectedSharedCloudSafeFile() {
		List<CloudSafeShareEntity> result = new ArrayList<CloudSafeShareEntity>();
		result.add(selectedSharedCloudSafeFile);
		return result;
	}

	public void setSelectedSharedCloudSafeFile(List<CloudSafeShareEntity> selectedSharedCloudSafeFile) {
		if (selectedSharedCloudSafeFile.isEmpty()) {
			this.selectedSharedCloudSafeFile = null;
		} else if (selectedSharedCloudSafeFile.size() > 1) {
			this.selectedSharedCloudSafeFile = selectedSharedCloudSafeFile.get(1);
		} else if (selectedSharedCloudSafeFile.size() == 1) {
			this.selectedSharedCloudSafeFile = selectedSharedCloudSafeFile.get(0);
		}
	}

	public String getSelectedFileName() {
		return selectedFileName;
	}

	public void setSelectedFileName(String selectedFileName) {
		this.selectedFileName = selectedFileName;
	}

	private TreeNode selectedSharedNode;

	public List<CloudSafeEntity> getCurrentFiles() {
		if (shouldRefreshCurrentFiles || cloudSafeEntityFiles == null) {
			shouldRefreshCurrentFiles = false;
			if (cloudSafeRoot == null) {
				JsfUtils.addFacesErrorMessage("CoudSafe Root Entity not Found");
				return null;
			}
			cloudSafeEntityFiles = getAsApiCloudSafeFiles(selectedFolder != null ? selectedFolder.getId() : cloudSafeRoot.getId(), loggedInUser);
			for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
				if (cloudSafeEntity.getGroup() != null) {
					cloudSafeEntity.getGroup().getName(); // get the lazy groups
				}
			}
		} else {
		}
		return cloudSafeEntityFiles;
	}

	private boolean fileOrFolderExists(String folderName, Integer parentId, boolean isFolder) throws DcemApiException, Exception {
		try {
			List<CloudSafeEntity> cse = cloudSafeLogic.getCloudSafeSingleResult(folderName, parentId, isFolder, loggedInUser.getId());
			if (cse.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return false;
		}
	}

	public void onDropFile(DragDropEvent<?> event) {
		if ((selectedCloudSafeFiles == null || selectedCloudSafeFiles.size() == 0) && event.getData() == null) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_FILE_SELECTED");
			return;
		}
		CloudSafeEntity selectedMoveEntry = (CloudSafeEntity) event.getData();
		String[] idTokens = event.getDropId().split(String.valueOf(UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance())));
		int rowIndex = Integer.parseInt(idTokens[idTokens.length - 2]);
		CloudSafeEntity moveTo = cloudSafeEntityFiles.get(rowIndex);
		if (selectedMoveEntry.getOwner().equals(CloudSafeOwner.GROUP)) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.notPossibleToMoveFileOwnedByGroup");
			return;
		}
		if (selectedMoveEntry.isOption(CloudSafeOptions.ENC) && moveTo.isOption(CloudSafeOptions.PWD)) {
			JsfUtils.addWarningMessage(UserPortalModule.RESOURCE_NAME, "error.notPossibleToMoeIntoProtectedFolder");
			return;
		}
		if (selectedMoveEntry.isOption(CloudSafeOptions.PWD) && moveTo.getName().equals(DcemConstants.CLOUD_SAFE_RECYCLE_BIN) == false
				&& moveTo.isOption(CloudSafeOptions.ENC) == false) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.notPossibleToMove");
			return;
		}
		if (moveTo.isFolder() == false) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.DROP_IN_FOLDER");
			return;
		} else {
			boolean found = false;
			for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
				if (cloudSafeEntity.getId() == selectedMoveEntry.getId()) {
					found = true;
					break;
				}
			}
			if (found == false) {
				ArrayList<CloudSafeEntity> selectedCloudSafeFiles = new ArrayList<CloudSafeEntity>(this.selectedCloudSafeFiles);
				selectedCloudSafeFiles.add(selectedMoveEntry);
				this.selectedCloudSafeFiles = selectedCloudSafeFiles;
			}
			moveToFolder = moveTo;
		}

		PrimeFaces.current().executeScript("PF('moveEntryConfirmationDialog').show();");
		PrimeFaces.current().ajax().update("moveEntryConfirmationForm:moveEntryConfirmation");
	}

	public void moveFileEntry() {
		if (selectedCloudSafeFiles == null || moveToFolder == null) {
			JsfUtils.addErrorMessage("MoveTo or Move From is not defined");
			return;
		}

		try {
			for (CloudSafeEntity selectedCloudSafeFile : selectedCloudSafeFiles) {
				if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(selectedCloudSafeFile.getName())) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableToMoveRecycleBin");
					return;
				}
				if (selectedCloudSafeFile.isOption(CloudSafeOptions.ENC) && moveToFolder.getName().equals(DcemConstants.CLOUD_SAFE_RECYCLE_BIN)) {
					deleteCloudSafeFiles();
					selectedCloudSafeFiles.clear();
					PrimeFaces.current().executeScript("PF('moveEntryConfirmationDialog').hide();");
					return;
				}
				if (!fileOrFolderExists(selectedCloudSafeFile.getName(), moveToFolder.getId(), false)) {
					cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, passwordToEncryptContent, moveToFolder.getId(), loggedInUser);
					cloudSafeEntityFiles.remove(selectedCloudSafeFile);
				} else {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.FILE_NAME_ALREADY_EXISTS", selectedCloudSafeFile.getName(),
							moveToFolder.getName());
				}
			}

		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		selectedCloudSafeFiles.clear();
		PrimeFaces.current().executeScript("PF('moveEntryConfirmationDialog').hide();");
	}

	public TreeNode getSelectedSharedNode() {
		return selectedSharedNode;
	}

	public void setSelectedSharedNode(TreeNode selectedSharedNode) {
		this.selectedSharedNode = selectedSharedNode;
	}

	public void onSharedNodeSelected(NodeSelectEvent event) {
		shouldRefreshCurrentSharedFiles = true;
	}
	// TODO do we need this method
	// @PreDestroy
	// public void destroy() {
	// if (cloudSafeEntityFiles != null)
	// cloudSafeEntityFiles.clear();
	//
	// if (selectedCloudSafeFiles != null)
	// selectedCloudSafeFiles.clear();
	//
	// if (sharedCloudSafeFiles != null)
	// sharedCloudSafeFiles.clear();
	//
	// if (currentSelectedFiles != null)
	// currentSelectedFiles.clear();
	//
	// if (sharedCloudSafeFolders != null)
	// sharedCloudSafeFolders.clear();
	//
	// selectedSharedCloudSafeFile = null;
	// uploadedFiles = null;
	// moveToFolder = null;
	// moveEntry = null;
	// }

	public boolean isOpenFolderWithPassword() {
		if (toOpenFileorFolder.isOption(CloudSafeOptions.PWD)) {
			return true;
		}
		return false;
	}

	public void openFolder() throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException {
		if (toOpenFileorFolder == null) {
			return;
		}
		char[] password = null;
		if (toOpenFileorFolder.isOption(CloudSafeOptions.PWD)) {
			password = filePassword.toCharArray();
			passwordToEncryptContent = filePassword;
		}
		processFileFolderClick(toOpenFileorFolder, password);
		PrimeFaces.current().ajax().update("viewPart");
	}

	public void actinClickFolderOrFile(CloudSafeEntity cloudSafeEntity) {
		if (selectedFilesToCut.contains(cloudSafeEntity)) {
			selectedFilesToCut.clear();
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "message.cannotPasteItemsHere");
			return;
		}
		if (cloudSafeEntity.isFolder() == true && cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
			folder = true;
		} else {
			folder = false;
		}
		toOpenFileorFolder = cloudSafeEntity;
		if (cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
			setFolderName(toOpenFileorFolder.getName());
			showDialog(OPEN_FOLDER_DLG);
			return;
		}
		try {
			if (toOpenFileorFolder.isOption(CloudSafeOptions.ENC) || DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(toOpenFileorFolder.getName())) {
				processFileFolderClick(cloudSafeEntity, null);
			} else {
				processFileFolderClick(cloudSafeEntity, passwordToEncryptContent.toCharArray());
			}
		} catch (Exception e) {
			logger.error("Something went wrong!", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	private void processFileFolderClick(CloudSafeEntity cloudSafeEntity, char[] password) {
		try {
			if (cloudSafeEntity.isFolder() == false) {
				cloudSafeEntityForShow = cloudSafeEntity;
				URL url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
				String port = "";
				if (url.getPort() != -1) {
					port = ":" + url.getPort();
				}
				String pdfFileURL = url.getProtocol() + "://" + url.getHost() + port + DcupConstants.DCEM_WEB_NAME + DcemConstants.USERPORTAL_SERVLET_PATH
						+ DcupConstants.TYPE + UrlTokenType.ShowFile;
				PrimeFaces.current().executeScript("openTab ('" + pdfFileURL + "');");
			} else { // when it is a folder.
				if (cloudSafeEntity.getOptions() != null && cloudSafeEntity.isOption(CloudSafeOptions.ENC) == false) { // check only if folder is encrypted
					InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, password, loggedInUser);
					KaraUtils.readInputStream(inputStream); // check for cipher valid
				}
				currentFolderId = cloudSafeEntity.getId();
				selectedFolder = cloudSafeEntity;
				String folderId = cloudSafeEntity.getId().toString();
				String counter = Integer.toString(breadCrumbModel.getElements().size());
				DefaultMenuItem menuItem = new DefaultMenuItem();
				menuItem.setValue(selectedFolder.getName());
				menuItem.setCommand("#{cloudSafeView.breadCrumbAction(" + folderId + ", " + counter + ")}");
				menuItem.setUpdate("cloudSafeForm:cloudSafeTable");
				breadCrumbModel.getElements().add(menuItem);
				menuItem.setId(counter);
				shouldRefreshCurrentFiles = true;
			}
		} catch (InvalidCipherTextIOException exp) {
			logger.info("could not open folder invald password for: " + cloudSafeEntity.getName());
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.verifyPasswordForOpenFolder");
		} catch (Exception ex) {
			logger.info("Could not open folder: " + toOpenFileorFolder.getName() + " User : " + loggedInUser, ex);
			JsfUtils.addErrorMessage("Something went wrong. Please contact your administrator Cause:" + ex.toString());
		}
	}

	public void actionCloseOpenFolderDlg() {
		toOpenFileorFolder = null;
		folderName = null;
		hideDialog(OPEN_FOLDER_DLG);
	}

	public MenuModel getBreadCrumbModel() {
		return breadCrumbModel;
	}

	public void breadCrumbAction(String cloudSafeId, String index) {
		try {
			if (cloudSafeId.equals("0")) {
				selectedFolder = null;
				currentFolderId = cloudSafeRoot.getId();
				passwordToEncryptContent = null;
			} else {
				selectedFolder = cloudSafeLogic.getCloudSafe(Integer.valueOf(cloudSafeId));
				currentFolderId = selectedFolder.getId();
			}
			int endOfList = breadCrumbModel.getElements().size() - 1;
			int ind = Integer.parseInt(index);
			for (; endOfList > ind; endOfList--) {
				breadCrumbModel.getElements().remove(endOfList);
			}
			shouldRefreshCurrentFiles = true;
			PrimeFaces.current().ajax().update("cloudSafeForm:nodeContextMenu");
			PrimeFaces.current().ajax().update("cloudSafeForm:cloudSafeBreadCrumbId");
		} catch (Exception e) {
			logger.warn("breadCrumbAction", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void breadCrumbSharedFilesAction(String shareId, String cloudSafeId, String index) {
		try {
			if (cloudSafeId.equals("0")) {
				selectedSharedCloudSafeFolder = null;
			} else {
				CloudSafeShareEntity sharedParent = cloudSafeLogic.getCloudShareByShareId(Integer.valueOf(shareId));
				if (sharedParent != null) {
					if (sharedParent.getCloudSafe().getId().equals(Integer.valueOf(cloudSafeId))) {
						selectedSharedCloudSafeFolder = sharedParent;
					} else {
						CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(Integer.valueOf(cloudSafeId));
						selectedSharedCloudSafeFolder = new CloudSafeShareEntity(cloudSafeEntity, sharedParent.getUser(), sharedParent.getGroup(),
								sharedParent.isWriteAccess(), sharedParent.isRestrictDownload());
						selectedSharedCloudSafeFolder.setId(sharedParent.getId());
					}
				} else {
					selectedSharedCloudSafeFolder = null;
				}
			}
			int endOfList = breadCrumbModelSharedFiles.getElements().size() - 1;
			int ind = Integer.parseInt(index);
			for (; endOfList > ind; endOfList--) {
				breadCrumbModelSharedFiles.getElements().remove(endOfList);
			}
			shouldRefreshCurrentSharedFiles = true;
			PrimeFaces.current().ajax().update("cloudSafeShareForm:sharedcloudSafeBreadCrumbId");
			PrimeFaces.current().ajax().update("cloudSafeShareForm:sharedCloudSafeTable");
		} catch (Exception e) {
			logger.warn("breadCrumbSharedFilesAction", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void onClickShareFolder(final SelectEvent<?> event) {
		CloudSafeShareEntity currentFolder = (CloudSafeShareEntity) event.getObject();
		if (currentFolder == null || currentFolder.getCloudSafe().isFolder() == false) {
			return;
		} else {
			selectedSharedCloudSafeFolder = currentFolder;
			String shareId = selectedSharedCloudSafeFolder.getId().toString();
			String folderId = selectedSharedCloudSafeFolder.getCloudSafe().getId().toString();
			String counter = Integer.toString(breadCrumbModelSharedFiles.getElements().size());
			DefaultMenuItem sharedMenuItem = new DefaultMenuItem();
			sharedMenuItem.setValue(selectedSharedCloudSafeFolder.getCloudSafe().getName());
			sharedMenuItem.setCommand("#{cloudSafeView.breadCrumbSharedFilesAction(" + shareId + ", " + folderId + ", " + counter + ")}");
			sharedMenuItem.setUpdate("cloudSafeShareForm:sharedcloudSafeBreadCrumbId cloudSafeShareForm:sharedCloudSafeTable");
			breadCrumbModelSharedFiles.getElements().add(sharedMenuItem);
			sharedMenuItem.setId(counter);
			shouldRefreshCurrentSharedFiles = true;
		}
		PrimeFaces.current().ajax().update("cloudSafeShareForm:sharedcloudSafeBreadCrumbId");
	}

	public CloudSafeEntity getSelectedFolder() {
		return selectedFolder;
	}

	public void setSelectedFolder(CloudSafeEntity selectedFolder) {
		this.selectedFolder = selectedFolder;
	}

	public void setBreadCrumbModel(MenuModel breadCrumbModel) {
		this.breadCrumbModel = breadCrumbModel;
	}

	public void addFolder() throws DcemApiException, Exception {
		if (selectedFolder == null) {
			selectedFolder = new CloudSafeEntity(null, null, null, "MyFiles", null, null, true, null, null);
			selectedFolder.setId(0);
		}
		if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(selectedFolder.getName())) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableToAddInRecycleBin");
			return;
		}
		String folderName = getAddFolderName();
		if (folderName.trim().isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.missingName");
			return;
		}
		DcemUser currentUser = loggedInUser;
		try {
			CloudSafeEntity cloudSafeEntity = null;
			validateFileOrFolderName(folderName);
			if (!fileOrFolderExists(folderName, selectedFolder.getId(), true)) {
				if (selectedFolder.getId() == 0) {
					cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(cloudSafeRoot, currentUser, folderName, passwordProtected, true, filePassword);
				} else {
					if (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD)) {
						cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(selectedFolder, currentUser, folderName, passwordProtected, true,
								passwordToEncryptContent);
					} else if (selectedFolder.isOption(CloudSafeOptions.ENC)) {
						cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(selectedFolder, currentUser, folderName, passwordProtected, true,
								filePassword);
					}
				}
				cloudSafeLogic.addCloudSafeFolder(cloudSafeEntity);
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.FOLDER_ALREADY_EXISTS");
				return;
			}
		} catch (DcemException exception) {
			logger.warn(exception);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exception));
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

		shouldRefreshCurrentFiles = true;
		PrimeFaces.current().executeScript("PF('processFolderDialog').hide();");
		PrimeFaces.current().ajax().update("cloudSafeForm:cloudSafeTable");
	}

	public void onAddFolder() {
		setEditFolderProcess(false);
		filePassword = null;
		setAddFolderName(null);
		passwordProtected = false;
		PrimeFaces.current().executeScript("PF('processFolderDialog').show();");
		PrimeFaces.current().ajax().update("processFolderForm:processFolderDialog");
	}

	public boolean isParentProtected() {
		if (selectedFolder == null) {
			return false;
		}
		return (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD));
	}

	private boolean isError() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		Boolean error = (Boolean) request.getSession().getAttribute(DcemConstants.DOWNLOAD_CIPHER_EXCEPTION);
		if (error == null) {
			return false;
		}
		request.getSession().removeAttribute(DcemConstants.DOWNLOAD_CIPHER_EXCEPTION);
		if (error == true) {
			DcemException dcemException = new DcemException(DcemErrorCodes.CLOUD_SAFE_FILE_DECRYPTION, "");
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(dcemException));
		}
		return true;
	}

	public String getAddFolderName() {
		return addFolderName;
	}

	public void setAddFolderName(String addFolderName) {
		this.addFolderName = addFolderName;
	}

	public boolean isEditFolderProcess() {
		return editFolderProcess;
	}

	public void setEditFolderProcess(boolean editFolderProcess) {
		this.editFolderProcess = editFolderProcess;
	}

	public String getSelectedFolderName() {
		return selectedFolderName;
	}

	public void setSelectedFolderName(String selectedFolderName) {
		this.selectedFolderName = selectedFolderName;
	}

	public MenuModel getBreadCrumbModelSharedFiles() {
		return breadCrumbModelSharedFiles;
	}

	public void setBreadCrumbModelSharedFiles(MenuModel breadCrumbModelSharedFiles) {
		this.breadCrumbModelSharedFiles = breadCrumbModelSharedFiles;
	}

	public CloudSafeShareEntity getSelectedSharedCloudSafeFolder() {
		return selectedSharedCloudSafeFolder;
	}

	public void setSelectedSharedCloudSafeFolder(CloudSafeShareEntity selectedSharedCloudSafeFolder) {
		this.selectedSharedCloudSafeFolder = selectedSharedCloudSafeFolder;
	}

	public void setDownloadFileName(String downloadFileName) {
		this.downloadFileName = downloadFileName;
	}

	public String getSearchFile() {
		return searchFile;
	}

	public void setSearchFile(String searchFile) {
		this.searchFile = searchFile;
		if (searchFile == null || searchFile.isEmpty()) {
			shouldRefreshCurrentFiles = true;
			return;
		} else {
			try {
				cloudSafeEntityFiles = cloudSafeLogic.getCloudSafeAllFileList(loggedInUser.getId(), searchFile + "%", 0, CloudSafeOwner.USER, true);

			} catch (Exception e) {
				logger.error("searching for Files or Folders : Could not finde anything..", e);
			}

		}
	}

	public void cutSelectedFilesOrFolders() {
		selectedFilesToCut.clear();
		if (listView == false) {
			selectedCloudSafeFiles = selectedCloudSafeEntity;
		}
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.isEmpty() == false) {
			for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
				if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(cloudSafeEntity.getName())) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableToMoveRecycleBin");
					return;
				} else {
					selectedFilesToCut.add(cloudSafeEntity);
				}
			}
			PrimeFaces.current().ajax().update("cloudSafeForm:nodeContextMenu");
		} else {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.noFilesSelected"));
		}
	}

	public void pasteSelectedFilesOrFolders() throws DcemApiException, Exception {
		if (selectedFilesToCut != null) {
			try {
				for (CloudSafeEntity selectedCloudSafeFile : selectedFilesToCut) {
					if (selectedCloudSafeFile.getOwner().equals(CloudSafeOwner.GROUP)) {
						JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.notPossibleToMoveFileOwnedByGroup");
						return;
					}
					if (!fileOrFolderExists(selectedCloudSafeFile.getName(), currentFolderId, false)) { // EG Use False
						if (selectedFolder != null) {
							if (selectedFolder.getId() == selectedCloudSafeFile.getId()) {
								JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.notAcceptedAction");
								return;
							}
							if (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD)) { // here in protected folder
								if (selectedCloudSafeFile.isOption(CloudSafeOptions.PWD)
										|| selectedCloudSafeFile.isFolder() && selectedCloudSafeFile.isOption(CloudSafeOptions.ENC)) {
									JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.notPossibleToMove");
									return;
								} else {
									cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, passwordToEncryptContent, currentFolderId, loggedInUser);
								}
							} else if (selectedFolder.isOption(CloudSafeOptions.ENC) && selectedCloudSafeFile.isOption(CloudSafeOptions.FPD)) {
								cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, loggedInUser);
							} else if (selectedCloudSafeFile.isOption(CloudSafeOptions.ENC) || selectedCloudSafeFile.isOption(CloudSafeOptions.PWD)) {
								cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, loggedInUser);
							}
						} else {
							cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, loggedInUser);
						}
						cloudSafeEntityFiles.remove(selectedCloudSafeFile);
					} else {
						if (currentFolderId == null) {
							JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.FILE_NAME_ALREADY_EXISTS_IN_ROOT", selectedCloudSafeFile.getName());
						} else {
							CloudSafeEntity cse = cloudSafeLogic.getCloudSafe(currentFolderId);
							JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.FILE_NAME_ALREADY_EXISTS", selectedCloudSafeFile.getName(),
									cse.getName());
						}
					}
				}
			} catch (DcemException ex) {
				logger.info("failed to move file", ex);
				JsfUtils.addErrorMessage(ex.getLocalizedMessage());
				selectedFilesToCut.clear();
				PrimeFaces.current().ajax().update("cloudSafeForm:nodeContextMenu");
				return;
			} catch (Exception e) {
				logger.info("failed to move file", e);
				selectedFilesToCut.clear();
				PrimeFaces.current().ajax().update("cloudSafeForm:nodeContextMenu");
				return;
			}
			selectedFilesToCut.clear();
			shouldRefreshCurrentFiles = true;
			PrimeFaces.current().ajax().update("cloudSafeForm:nodeContextMenu");
			PrimeFaces.current().ajax().update("cloudSafeForm:cloudSafeTable");
			PrimeFaces.current().ajax().update("cloudSafeForm:cloudSafeKacheln");

		}
	}

	public boolean isPasteDisabled() {
		if (selectedFilesToCut.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isMultipleFile() {
		return multipleFile;
	}

	public void setMultipleFile(boolean multipleFile) {
		this.multipleFile = multipleFile;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getPasswordToEncryptContent() {
		return passwordToEncryptContent;
	}

	public void setPasswordToEncryptContent(String passwordToEncryptContent) {
		this.passwordToEncryptContent = passwordToEncryptContent;
	}

	public List<CloudSafeShareEntity> getCurrentSelectedSharedCloudSafeFiles() {
		return currentSelectedSharedCloudSafeFiles;
	}

	public void setCurrentSelectedSharedCloudSafeFiles(List<CloudSafeShareEntity> currentSelectedSharedCloudSafeFiles) {
		this.currentSelectedSharedCloudSafeFiles = currentSelectedSharedCloudSafeFiles;
	}

	public boolean isDownloadSharedFileFPD() {
		return downloadSharedFileFPD;
	}

	public void setDownloadSharedFileFPD(boolean downloadSharedFileFPD) {
		this.downloadSharedFileFPD = downloadSharedFileFPD;
	}

	public String getFileIcon(CloudSafeEntity cloudSafeEntity) {
		String iconName;
		String fileName = cloudSafeEntity.getName();
		if (cloudSafeEntity.isFolder()) {
			if (DcemConstants.CLOUD_SAFE_RECYCLE_BIN.equals(fileName)) {
				iconName = DcemConstants.DEFAULT_BIN_ICON;
			} else if (listView == false && cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
				iconName = DcemConstants.DEFAULT_FOLDER_LOOK_ICON;
			} else {
				iconName = DcemConstants.DEFAULT_FOLDER_ICON;
			}
		} else {
			int ind = fileName.lastIndexOf('.');
			if (ind == -1) {
				iconName = DcemConstants.DEFAULT_FILE_ICON;
			}
			String fileExtension = fileName.substring(ind + 1);
			iconName = dcemApplication.getFileIconsMap().get(fileExtension.toLowerCase());
			if (iconName == null) {
				iconName = DcemConstants.DEFAULT_FILE_ICON;
			}
		}
		return iconName;
	}

	public void setSelectedCloudSafeFile(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity == null) {
			return;
		}
		int foundIndex = searchSelectedFile(cloudSafeEntity.getId());
		if (foundIndex == -1 && selectedFile == true) {
			selectedCloudSafeEntity.add(cloudSafeEntity);
		} else if (foundIndex != -1 && selectedCloudSafeEntity.isEmpty() == false && selectedFile == false) {
			selectedCloudSafeEntity.remove(foundIndex);
		}
		return;
	}

	private int searchSelectedFile(Integer selectedFileId) {
		if (selectedCloudSafeEntity.isEmpty()) {
			return -1;
		}
		for (int i = 0; i < selectedCloudSafeEntity.size(); i++) {
			if (selectedFileId == selectedCloudSafeEntity.get(i).getId()) {
				return i;
			}
		}
		return -1;
	}

	public boolean isSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(boolean selectedFile) {
		this.selectedFile = selectedFile;
	}

	public void changeselectedFileListener(ValueChangeEvent vce) {
		Integer questionId = (Integer) ((UIComponentBase) vce.getSource()).getAttributes().get("cloudSafeFileId");
		if (searchSelectedFile(questionId) == -1 && vce.getNewValue().equals(true)) {
			selectedFile = true;
			return;
		}
		selectedFile = false;
		return;
	}

	public boolean isListView() {
		return listView;
	}

	public void setListView(boolean listView) {
		selectedCloudSafeFiles = new ArrayList<CloudSafeEntity>();
		selectedCloudSafeEntity = new ArrayList<CloudSafeEntity>();
		this.listView = listView;
	}

	public CloudSafeEntity getCloudSafeEntityForShow() {
		return cloudSafeEntityForShow;
	}

	public void setCloudSafeEntityForShow(CloudSafeEntity cloudSafeEntityForShow) {
		this.cloudSafeEntityForShow = cloudSafeEntityForShow;
	}

	public boolean isFolder() {
		return folder;
	}

	public void setFolder(boolean folder) {
		this.folder = folder;
	}

	public String getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public boolean isDefineOwnerGroup() {
		return defineOwnerGroup;
	}

	public void setDefineOwnerGroup(boolean defineOwnerGroup) {
		this.defineOwnerGroup = defineOwnerGroup;
	}

	public boolean isUploadingSharedFile() {
		return uploadingSharedFile;
	}

	public List<DcemGroup> getAllUsersGroups() {
		return allUsersGroups;
	}

}
