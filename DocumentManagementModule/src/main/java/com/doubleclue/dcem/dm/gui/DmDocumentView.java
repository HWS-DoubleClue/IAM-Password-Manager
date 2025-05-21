package com.doubleclue.dcem.dm.gui;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.crypto.io.InvalidCipherTextIOException;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuModel;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.dm.logic.DmSolrLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.subjects.DmDocumentSubject;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.StringUtils;

@SuppressWarnings("serial")
@Named("dmDocumentView")
@SessionScoped
public class DmDocumentView extends DcemView {

	private static final String MY_DOUBLE_CLUE_FILE_ZIP = "MyDocuments.zip";
	private static final String DOWNLOAD_DLG = "downloadDlg";
	private static final String LS_VIEWNAME = "DmDocumentView";
	private final String COLUMN_TOGGLER = "CT-";

	@Inject
	private DmDocumentSubject dmDocumentEntitySubject;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	DmWorkflowView workflowView;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DmNewDocumentView dmNewDocumentView;

	@Inject
	CloudSafeTagLogic cloudSafeTagLogic;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	DocumentManagementModule documentManagementModule;

	@Inject
	SystemModule systemModule;

	@Inject
	DmSolrLogic dmSolrLogic;

	@Inject
	TaskExecutor taskExecutor;

	private CloudSafeEntity cloudSafeRoot;
	private CloudSafeEntity selectedFolder;
	private CloudSafeEntity moveToFolder;
	private List<CloudSafeEntity> selectedCloudSafeFiles = new ArrayList<CloudSafeEntity>();
	private List<CloudSafeEntity> selectedFilesToCut = new ArrayList<CloudSafeEntity>();
	private List<CloudSafeEntity> cloudSafeEntityFiles;
	private List<CloudSafeTagEntity> filterTags;

	private ResourceBundle resourceBundle;
	private DmDisplayMode displayMode = DmDisplayMode.LIST;
	private boolean shouldRefreshCurrentFiles;
	private boolean passwordProtected;
	private boolean editFolderProcess;
	private String downloadFileName;
	private String filePassword;
	private String selectedFileName;
	private String addFolderName;
	private Integer currentFolderId;
	private Integer documentId;

	private MenuModel breadCrumbModel;

	private Map<String, Boolean> columnFilterSettings;
	private List<CloudSafeTagEntity> selectedFilterTags;
	private int emailForGroup;
	private String selectedGroup;
	private String mailToken;
	private String lastmailTokenIdentifier;
	private LocalDateTime tokenExpiryDate;
	String searchTerm = "";
	private boolean searchResultMode = false;
	private boolean shareDocumentsMode = false;
	File downloadTempFile;

	@PostConstruct
	private void init() {
		subject = dmDocumentEntitySubject;
		columnFilterSettings = new HashMap<String, Boolean>();
		resourceBundle = JsfUtils.getBundle(DocumentManagementModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		breadCrumbModel = new DefaultMenuModel();
		cloudSafeRoot = cloudSafeLogic.getCloudSafeRoot();
		updateBreadCrumbModel(cloudSafeRoot);
		selectedFilterTags = new ArrayList<CloudSafeTagEntity>();
	}

	@Override
	public void reload() {
		shareDocumentsMode = false;
		onReload();
	}

	public void setShareDocumentsMode(boolean value) {
		shareDocumentsMode = value;
		updateBreadCrumbModel(cloudSafeRoot);
		onReload();
	}

	private void onReload() {
		shouldRefreshCurrentFiles = true;
		searchResultMode = false;
		filterTags = cloudSafeTagLogic.getAllTags();
		loadColumnFilter();
	}

	

	public List<SelectItem> getUserGroups() {
		List<DcemGroup> listGroups = operatorSessionBean.getUserGroups();
		List<SelectItem> listItems = new ArrayList<>(listGroups.size());
		for (DcemGroup dcemGroup : listGroups) {
			listItems.add(new SelectItem(dcemGroup.getId().toString(), dcemGroup.getName()));
		}
		return listItems;
	}

	private String getAllSharedDocumentsIds() throws DcemException {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		List<CloudSafeShareEntity> shareEntities = cloudSafeLogic.getUserCloudSafeShareEntities(operatorSessionBean.getDcemUser(), null);
		for (int i = 0; i < shareEntities.size(); i++) {
			if (i > 0 && i < shareEntities.size()) {
				sb.append(" OR ");
			}
			CloudSafeEntity cloudSafeEntity = shareEntities.get(i).getCloudSafe();
			sb.append(cloudSafeEntity.getId());
			if (cloudSafeEntity.isFolder()) {
				List<CloudSafeEntity> list = cloudSafeLogic.getByUserAndParentIdDocuments(cloudSafeEntity.getId(), cloudSafeEntity.getUser(),
						operatorSessionBean.getUserGroups());
				if (list.isEmpty() == false) {
					sb.append(" OR ");
					for (int k = 0; k < list.size(); k++) {
						if (k > 0 && k < list.size()) {
							sb.append(" OR ");
						}
						sb.append(list.get(k).getId());
					}
				}
			}
		}
		sb.append(')');
		return sb.toString();
	}

	public List<CloudSafeEntity> getCurrentFiles() {
		if (searchResultMode == true) {
			return cloudSafeEntityFiles;
		}
		// System.out.println("DmDocumentView.getCurrentFiles()");
		if (shouldRefreshCurrentFiles || cloudSafeEntityFiles == null) {
			try {
				shouldRefreshCurrentFiles = false;
				if (shareDocumentsMode == true) {
					if (selectedFolder.equals(cloudSafeRoot)) {
						List<CloudSafeShareEntity> shareEntities = cloudSafeLogic.getUserCloudSafeShareEntities(operatorSessionBean.getDcemUser(), null);
						cloudSafeEntityFiles = new ArrayList<CloudSafeEntity>(shareEntities.size());
						for (CloudSafeShareEntity entity : shareEntities) {
							CloudSafeEntity cloudSafeEntity = entity.getCloudSafe();
							cloudSafeEntity.setWriteAccess(entity.isWriteAccess());
							cloudSafeEntity.setRestrictDownload(entity.isRestrictDownload());
							cloudSafeEntityFiles.add(cloudSafeEntity);
						}
					} else {
						if (selectedFolder.getOwner() == CloudSafeOwner.USER) {
							cloudSafeEntityFiles = cloudSafeLogic.getByUserAndParentIdDocuments(selectedFolder.getId(), selectedFolder.getUser(),
									operatorSessionBean.getUserGroups());
							for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
								cloudSafeEntity.setWriteAccess(selectedFolder.isWriteAccess());
								cloudSafeEntity.setRestrictDownload(selectedFolder.isRestrictDownload());
							}
						}
					}
				} else {
					if (selectedFolder.getOwner() == CloudSafeOwner.GROUP) {
						cloudSafeEntityFiles = cloudSafeLogic.getByParentId(selectedFolder);
					} else {
						cloudSafeEntityFiles = getAsApiCloudSafeFiles(selectedFolder != null ? selectedFolder.getId() : cloudSafeRoot.getId(),
								operatorSessionBean.getDcemUser());
					}
				}
				for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
					if (selectedCloudSafeFiles.contains(cloudSafeEntity) == true) {
						cloudSafeEntity.setSelected(true);
					}
				}
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				logger.error(e.getMessage(), e);
				return new ArrayList<CloudSafeEntity>(0);
			}
		}
		return cloudSafeEntityFiles;
	}

	// used for tile and content view
	public List<CloudSafeEntity> getCurrentFolders() {
		getCurrentFiles(); // update if neccessary
		List<CloudSafeEntity> currentFolders = new ArrayList<>();
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
			if (cloudSafeEntity.isFolder()) {
				currentFolders.add(cloudSafeEntity);
			}
		}
		return currentFolders;
	}

	// used for tile and content view
	public List<CloudSafeEntity> getCurrentDocuments() {
		getCurrentFiles(); // update if neccessary
		List<CloudSafeEntity> currentDocuments = new ArrayList<>();
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
			if (cloudSafeEntity.isFolder() == false) {
				currentDocuments.add(cloudSafeEntity);
			}
		}
		return currentDocuments;
	}

	public void actionClearSearch() {
		searchTerm = "";
		searchResultMode = false;
		updateTable();
		selectedFilterTags.clear();
		shouldRefreshCurrentFiles = true;
	}

	public boolean isSearchText() {
		return selectedFilterTags.isEmpty() == false || (searchTerm.isEmpty() == false);
	}

	public String getTagsSafelyText(CloudSafeEntity entity) {
		SortedSet<CloudSafeTagEntity> set = cloudSafeLogic.getTagsSafely(entity);
		if (set == null) {
			return "";
		}
		String value = cloudSafeLogic.getTagsSafely(entity).toString();
		return value.substring(1, value.length() - 1);
	}

	public SortedSet<CloudSafeTagEntity> getTagsSafely(CloudSafeEntity entity) {
		return cloudSafeLogic.getTagsSafely(entity);
	}

	public void actionSearch() {
		if (isSearchText() == false) {
			shouldRefreshCurrentFiles = true;
			searchResultMode = false;
			updateTable();
			return;
		}
		if (searchTerm.equals("{IndexAll}")) {
			try {
				dmSolrLogic.indexUserDocument(cloudSafeRoot, operatorSessionBean.getDcemUser(), operatorSessionBean.getUserGroups());
			} catch (Exception e) {
				JsfUtils.addErrorMessage("Couldn't index all files. Cause: " + e.toString());
				return;
			}
			searchTerm = "";
			shouldRefreshCurrentFiles = true;
			searchResultMode = false;
			updateTable();
			JsfUtils.addInfoMessage("Your documents are indexed");
			return;
		}
		try {
			int maxResults = documentManagementModule.getPreferences().getMaxSearchResults();
			List<Integer> solrResults;
			if (shareDocumentsMode == true) {
				String idFilter = getAllSharedDocumentsIds();
				solrResults = dmSolrLogic.searchDocuments(selectedFilterTags, searchTerm, null, null, idFilter, maxResults);
			} else {
				solrResults = dmSolrLogic.searchDocuments(selectedFilterTags, searchTerm, operatorSessionBean.getDcemUser().getId(),
						operatorSessionBean.getUserGroups(), null, maxResults);
			}
			searchResultMode = true;
			updateTable();
			if (solrResults.isEmpty()) {
				cloudSafeEntityFiles.clear(); // Clear displayed documents
				return;
			}
			if (solrResults.size() >= maxResults) {
				JsfUtils.addWarnMessage(resourceBundle, "documentView.warning.searchLimit");
			}
			cloudSafeEntityFiles = cloudSafeLogic.getCloudSafeEntitiesByIds(solrResults);
			System.out.println("DmDocumentView.actionSearch() " + cloudSafeEntityFiles.size());
		} catch (DcemException e) {
			logger.error("Error occurred during search for term: {}", searchTerm, e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("Error occurred during search for term: {}", searchTerm, e);
			JsfUtils.addErrorMessage("Error performing search: " + e.getMessage());
		}
	}

	private List<CloudSafeEntity> getAsApiCloudSafeFiles(Integer parentId, DcemUser user) throws Exception {
		if (parentId != null && parentId == 0) {
			CloudSafeEntity root = cloudSafeLogic.getCloudSafeRoot();
			parentId = root.getId();
		}
		return cloudSafeLogic.getCloudSafeByUserAndParentId(parentId, user, operatorSessionBean.getUserGroups(), false);
	}

	public void actionParentFolder(CloudSafeEntity cloudSafeEntity) {
		searchResultMode = false;
		searchTerm = "";
		selectedFilterTags.clear();
		updateTable();
		actionClickFolder(cloudSafeEntity.getParent());
	}

	public void actionClickFolder() {
		CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(this.documentId);
		actionClickFolder(cloudSafeEntity);
	}

	public void actionClickFolder(CloudSafeEntity cloudSafeEntity) {
		if (selectedFilesToCut.contains(cloudSafeEntity)) {
			selectedFilesToCut.clear();
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.cannotPasteItemsHere");
			return;
		}
		try {
			updateBreadCrumbModel(cloudSafeEntity);
			shouldRefreshCurrentFiles = true;
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	private void updateBreadCrumbModel(CloudSafeEntity cloudSafeEntity) {
		try {
			if (cloudSafeEntity.isFolder() == true) {
				breadCrumbModel.getElements().clear();
				currentFolderId = cloudSafeEntity.getId();
				selectedFolder = cloudSafeEntity;
				if (shareDocumentsMode == true) { // do not show whole path
					addEntityToBreadCrumg(cloudSafeRoot);
					if (cloudSafeEntity.equals(cloudSafeRoot) == false) {
						addEntityToBreadCrumg(cloudSafeEntity);
					}
					return;
				}
				List<CloudSafeEntity> list = cloudSafeLogic.getPathList(cloudSafeEntity);
				list.addFirst(cloudSafeRoot);
				for (CloudSafeEntity entity : list) {
					addEntityToBreadCrumg(entity);
				}
			}
		} catch (Exception ex) {
			logger.info("Could not open folder: " + cloudSafeEntity.getName() + " User : " + operatorSessionBean.getDcemUser(), ex);
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.cantOpenFolderContactAdmin" + ex.toString());
		}
	}

	private void addEntityToBreadCrumg(CloudSafeEntity cloudSafeEntity) {
		String folderId = cloudSafeEntity.getId().toString();
		DefaultMenuItem menuItem = new DefaultMenuItem();
		menuItem.setValue(cloudSafeEntity.getName());
		menuItem.setCommand("#{dmDocumentView.breadCrumbAction(" + folderId + ")}");
		menuItem.setUpdate("documentForm");
		menuItem.setId(folderId);
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.GROUP) {
			menuItem.setIcon("fa fa-people-group");
		}
		breadCrumbModel.getElements().add(menuItem);
		return;
	}

	public String getFileIcon(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity.getDcemMediaType() != null) {
			return cloudSafeEntity.getDcemMediaType().getIconResourcePath();
		}
		String iconName;
		String fileName = cloudSafeEntity.getName();
		if (cloudSafeEntity.isFolder()) {
			if (cloudSafeEntity.isOption(CloudSafeOptions.PWD)) {
				iconName = DcemConstants.DEFAULT_FOLDER_LOOK_ICON;
			} else {
				iconName = "svg/" + DcemConstants.DEFAULT_FOLDER_ICON;
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

	public void onDropFile(DragDropEvent<?> event) {
		if (event.getData() == null) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.noFileSelected");
			return;
		}

		CloudSafeEntity droppedDocument = (CloudSafeEntity) event.getData();
		System.out.println("Dropped document: " + droppedDocument.getName());
		if ((selectedCloudSafeFiles == null || selectedCloudSafeFiles.size() == 0) && event.getData() == null) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.noFileSelected"); // RESOURCES
			return;
		}
		CloudSafeEntity selectedMoveEntry = (CloudSafeEntity) event.getData();
		String[] idTokens = event.getDropId().split(String.valueOf(UINamingContainer.getSeparatorChar(FacesContext.getCurrentInstance())));
		int rowIndex = Integer.parseInt(idTokens[idTokens.length - 2]);
		CloudSafeEntity moveTo = cloudSafeEntityFiles.get(rowIndex);
		if (selectedMoveEntry.getOwner().equals(CloudSafeOwner.GROUP)) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notPossibleToMoveFileOwnedByGroup");
			return;
		}
		if (selectedMoveEntry.isOption(CloudSafeOptions.ENC) && moveTo.isOption(CloudSafeOptions.PWD)) {
			JsfUtils.addWarningMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notPossibleToMoveIntoProtectedFolder");
		}
		if (selectedMoveEntry.isOption(CloudSafeOptions.PWD) && moveTo.isOption(CloudSafeOptions.ENC) == false) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notPossibleToMove");
			return;
		}
		if (moveTo.isFolder() == false) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.dropInFolder");
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

	public void onDownloadFiles(CloudSafeEntity cloudSafeEntity) {
		if (selectedCloudSafeFiles.contains(cloudSafeEntity) == false) {
			selectedCloudSafeFiles.add(cloudSafeEntity);
		}
		onDownloadFiles();
	}

	public void onDownloadFiles() {
		try {
			if (selectedCloudSafeFiles == null || selectedCloudSafeFiles.size() == 0) {
				JsfUtils.addErrorMessage(resourceBundle.getString("documentView.error.noFileSelected"));
				return;
			}
			if (selectedCloudSafeFiles.size() == 1 && selectedCloudSafeFiles.get(0).isFolder() == false) {
				if (selectedCloudSafeFiles.get(0).isRestrictDownload() == true) {
					JsfUtils.addErrorMessage(resourceBundle, "documentView.error.downloadProtectedFile");
					return;
				}
				setDownloadFileName(selectedCloudSafeFiles.get(0).getName());
			} else {
				for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) { // protection
					if (isDocumentProtected(cloudSafeEntity) == true) {
						JsfUtils.addErrorMessage(resourceBundle, "documentView.error.downloadProtectedFile");
						return;
					}
				}
				setDownloadFileName(MY_DOUBLE_CLUE_FILE_ZIP);
			}
			PrimeFaces.current().ajax().update("downloadForm");
			showDialog(DOWNLOAD_DLG);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error(e.getMessage(), e);
			return;
		}
		return;
	}

	private boolean isDocumentProtected(CloudSafeEntity cloudSafeEntity) throws Exception {
		boolean found = false;
		if (cloudSafeEntity.isOption(CloudSafeOptions.PWD) == true || cloudSafeEntity.isRestrictDownload() == true) {
			return true;
		}
		if (cloudSafeEntity.isFolder() == true) {
			List<CloudSafeEntity> childernSubFolder = getAsApiCloudSafeFiles(cloudSafeEntity.getId(), cloudSafeEntity.getUser());
			for (CloudSafeEntity child : childernSubFolder) {
				if (shareDocumentsMode == true) {
					if (child.isFolder() == true) {
						continue; // ignore nested folders
					} else {
						if (child.isOption(CloudSafeOptions.PWD) == true || child.isRestrictDownload() == true) {
							return true;
						}
					}
				} else {
					return isDocumentProtected(child);
				}
			}
			return found;
		}
		return found;
	}

	public void actionAddDocument(String mediaType) {
		try {
			DcemMediaType dcemMediaType = DcemMediaType.valueOf(mediaType);
			dmNewDocumentView.addDocument(dcemMediaType, selectedFolder);
		} catch (Exception e) {
			logger.error("Coundn't add new document", e);
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "error.addDocument"); // TODO RESOURCES
			return;
		}
		viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmNewDocumentView.getSubject().getViewName());
	}

	// public void uploadDragFileListener(FilesUploadEvent event) {
	// System.out.println("DmDocumentView.uploadDragFileListener() Count: " + event.getFiles().getSize());
	// // System.out.println("DmDocumentView.uploadFileListener() file: " + event.getFiles().getSize());
	// if (event.getFiles().getSize() > 1) {
	// for (UploadedFile uploadedFile : event.getFiles().getFiles()) {
	// System.out.println("DmDocumentView.uploadFileListener() " + uploadedFile.getFileName());
	// }
	// return;
	// }
	//
	// try {
	// dmNewDocumentView.uploadDocument(event.getFiles().getFiles().get(0), selectedFolder);
	// } catch (DcemException e) {
	// switch (e.getErrorCode()) {
	// case OCR_TESSERACT_NOT_CONFIGURED:
	// JsfUtils.addWarnMessage(e.getLocalizedMessage());
	// break;
	// case OCR_TESSERACT_ERROR:
	// JsfUtils.addErrorMessage(e.getLocalizedMessage());
	// return;
	// default:
	// throw new IllegalArgumentException("Unexpected value: " + e.getErrorCode());
	// }
	// } catch (Throwable e) {
	// logger.error("Couldn't upload file", e);
	// JsfUtils.addErrorMessage(e.toString());
	// return;
	// }
	// viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmNewDocumentView.getSubject().getViewName());
	// }

	public StreamedContent actionDownloadFile() {
		InputStream inputStream;
		try {
			if (selectedCloudSafeFiles.size() == 1 && selectedCloudSafeFiles.get(0).isFolder() == false) {
				char[] password = null;
				if (filePassword != null) {
					password = filePassword.toCharArray();
				}
				inputStream = cloudSafeLogic.getCloudSafeContentAsStream(selectedCloudSafeFiles.get(0), password, operatorSessionBean.getDcemUser());
				return DefaultStreamedContent.builder().contentType(selectedCloudSafeFiles.get(0).getDcemMediaType().getMediaType())
						.name(selectedCloudSafeFiles.get(0).getNameWithExtension()).stream(() -> inputStream).build();
			}
			// multiple files
			return actionDownloadMultipleFiles();

		} catch (InvalidCipherTextIOException ex) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.corruptedFile");
			logger.error("Could not download file or folder with name: " + downloadFileName);
			return null;
		} catch (DcemException e) {
			logger.error("Coundn't downlaod file " + downloadFileName, e);
			if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_READ_ERROR) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} else {
				JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.verifyPasswordForFolder");
			}
			return null;
		} catch (Throwable e) {
			logger.error("Coundn't downlaod files " + downloadFileName, e);
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.verifyPasswordForFolder");
			return null;
		}
	}

	private StreamedContent actionDownloadMultipleFiles() throws Exception {
		downloadTempFile = File.createTempFile("dcem-", "-cloudSafe");
		OutputStream outputStream = new FileOutputStream(downloadTempFile);
		ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
		zipOutputStream.setLevel(Deflater.BEST_SPEED);
		for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
			zipFoldersOrFiles("", zipOutputStream, outputStream, cloudSafeEntity);
		}
		zipOutputStream.close();
		outputStream.close();
		FileInputStream fileInputStream = new FileInputStream(downloadTempFile);
		return DefaultStreamedContent.builder().contentType("application/zip").name(MY_DOUBLE_CLUE_FILE_ZIP).stream(() -> fileInputStream).build();
	}

	private void zipFoldersOrFiles(String path, ZipOutputStream zipOutputStream, OutputStream output, CloudSafeEntity cloudSafeEntity) throws Exception {
		int length = -1;
		byte[] buffer = new byte[1024 * 16];
		InputStream inputStream;
		ZipEntry zipEntry;
		BufferedInputStream bis;
		if (cloudSafeEntity.isFolder() == true) {

			List<CloudSafeEntity> childernSubFolder = getAsApiCloudSafeFiles(cloudSafeEntity.getId(), cloudSafeEntity.getUser());
			if (path.isEmpty() == true) {
				path = cloudSafeEntity.getNameWithExtension() + "/";
			} else {
				path = path + "/";
			}
			zipEntry = new ZipEntry(path);
			zipOutputStream.putNextEntry(zipEntry);
			for (CloudSafeEntity childEntity : childernSubFolder) {
				if (shareDocumentsMode == false || childEntity.isFolder() == false) {
					zipFoldersOrFiles(path + childEntity.getNameWithExtension(), zipOutputStream, output, childEntity);
				}
			}

		} else {
			inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, operatorSessionBean.getDcemUser());
			bis = new BufferedInputStream(inputStream);
			if (path.equals("")) {
				zipEntry = new ZipEntry(cloudSafeEntity.getNameWithExtension());
			} else {
				zipEntry = new ZipEntry(path);
			}
			zipOutputStream.putNextEntry(zipEntry);
			while ((length = bis.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, length);
			}
		}
	}

	public void actionCloseDownloadFile() {
		if (isError() == false) {
			hideDialog(DOWNLOAD_DLG);
		}
		if (downloadTempFile != null) {
			downloadTempFile.delete();
			downloadTempFile = null;
		}
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
			JsfUtils.addErrorMessage(dcemException.getLocalizedMessage());
		}
		return true;
	}

	public void validateFileOrFolderName(String selectedFileOrFolderName) throws DcemException {
		if (StringUtils.isValidFileName(selectedFileOrFolderName) == false) {
			throw new DcemException(DcemErrorCodes.FILE_NAME_WITH_SPECIAL_CHARACTERS, selectedFileOrFolderName);
		}
	}

	public void actionTrashDocuments() {
		try {
			List<CloudSafeDto> deletedDbFiles = cloudSafeLogic.trashFiles(selectedCloudSafeFiles, operatorSessionBean.getDcemUser());
			try {
				dmSolrLogic.removeDocumentsIndex(deletedDbFiles);
			} catch (Exception solrException) {
				logger.warn(resourceBundle.getString("documentView.warn.partialSolrDeletion"), solrException);
				JsfUtils.addWarnMessage(JsfUtils.getStringSafely(resourceBundle, "documentView.warn.partialSolrDeletion"));
			}
		} catch (Exception e) {
			logger.warn("Couldn't delete files", e);
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.deleteFailed" + e.getLocalizedMessage());
			return;
		}
		cloudSafeEntityFiles.clear();
		selectedCloudSafeFiles.clear();
		shouldRefreshCurrentFiles = true;
		PrimeFaces.current().ajax().update("documentForm:storageInfo");
		hideDialog("confirmDlg");
	}

	public void onAddFolder() {
		setEditFolderProcess(false);
		filePassword = null;
		setAddFolderName(null);
		passwordProtected = false;
		PrimeFaces.current().executeScript("PF('processFolderDialog').show();");
		PrimeFaces.current().ajax().update("processFolderForm:processFolderDialog");
	}

	public void addFolder() {
		if (selectedFolder == null) {
			selectedFolder = new CloudSafeEntity(null, null, null, "MyFiles", null, null, true, null);
			selectedFolder.setId(0);
		}
		String folderName = getAddFolderName();
		if (folderName.trim().isEmpty()) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.missingName");
			return;
		}
		DcemUser currentUser = operatorSessionBean.getDcemUser();
		try {
			CloudSafeEntity cloudSafeEntity = null;
			validateFileOrFolderName(folderName);
			if (fileOrFolderExists(folderName, selectedFolder.getId(), true) == false) {
				if (selectedFolder.getId() == 0) {
					cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(cloudSafeRoot, currentUser, folderName, passwordProtected, true, filePassword);
				} else {
					if (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD)) {
						cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(selectedFolder, currentUser, folderName, passwordProtected, true, null);
					} else if (selectedFolder.isOption(CloudSafeOptions.ENC)) {
						cloudSafeEntity = cloudSafeLogic.createCloudSafeEntityFolder(selectedFolder, currentUser, folderName, passwordProtected, true,
								filePassword);
					}
				}
				cloudSafeLogic.addCloudSafeFolder(cloudSafeEntity);
			} else {
				JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.folderAlreadyExists");
				return;
			}
		} catch (DcemException exception) {
			logger.error(exception);
			JsfUtils.addErrorMessage(exception.getLocalizedMessage());
			return;
		} catch (Exception e) {
			logger.error("", e.getLocalizedMessage());
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return;
		}
		shouldRefreshCurrentFiles = true;
		PrimeFaces.current().executeScript("PF('processFolderDialog').hide();");
		PrimeFaces.current().ajax().update("documentForm");
	}

	private boolean fileOrFolderExists(String folderName, Integer parentId, boolean isFolder) throws DcemApiException, Exception {
		return cloudSafeLogic.getCloudSafeUserSingleResult(folderName, parentId, isFolder, operatorSessionBean.getDcemUser().getId(), false) != null;
	}

	public boolean isShareTo(CloudSafeEntity cloudSafeEntity) {
		try {
			StringBuilder sb = new StringBuilder();
			List<CloudSafeShareEntity> list = cloudSafeLogic.getSharedCloudSafeUsersAccess(cloudSafeEntity);
			if (list.isEmpty()) {
				return false;
			}
			for (CloudSafeShareEntity shareEntity : list) {
				if (sb.isEmpty() == false) {
					sb.append("; ");
				}
				sb.append(shareEntity.getOwnerName());
			}
			cloudSafeEntity.setSharedTo(sb.toString());
			return true;
		} catch (DcemException e) {
			logger.error("breadCrumbAction", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return false;
		}

	}

	public void breadCrumbAction(String cloudSafeId) {
		try {
			selectedFolder = cloudSafeLogic.getCloudSafe(Integer.valueOf(cloudSafeId));
			currentFolderId = selectedFolder.getId();
			updateBreadCrumbModel(selectedFolder);
			updateColumnFilter();
			shouldRefreshCurrentFiles = true;
			PrimeFaces.current().ajax().update("documentForm:nodeContextMenu");
			PrimeFaces.current().ajax().update("documentForm:documentBreadCrumbId");
		} catch (Exception e) {
			logger.error("breadCrumbAction", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void cutSelectedFilesOrFolders() {
		selectedFilesToCut.clear();
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.isEmpty() == false) {
			for (CloudSafeEntity cloudSafeEntity : selectedCloudSafeFiles) {
				selectedFilesToCut.add(cloudSafeEntity);
			}
			PrimeFaces.current().ajax().update("documentForm:nodeContextMenu");
		} else {
			JsfUtils.addErrorMessage(resourceBundle.getString("documentView.error.noFileSelected"));
		}
	}

	public void pasteSelectedFilesOrFolders() throws DcemApiException, Exception {
		if (selectedFilesToCut != null) {
			try {
				for (CloudSafeEntity selectedCloudSafeFile : selectedFilesToCut) {
					if (selectedCloudSafeFile.getOwner().equals(CloudSafeOwner.GROUP)) {
						JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notPossibleToMoveFileOwnedByGroup");
						return;
					}
					if (fileOrFolderExists(selectedCloudSafeFile.getName(), currentFolderId, false) == false) { // EG
																												// Use
																												// False
						if (selectedFolder != null) {
							if (selectedFolder.getId() == selectedCloudSafeFile.getId()) {
								JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notAcceptedAction");
								return;
							}
							if (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD)) {
								if (selectedCloudSafeFile.isOption(CloudSafeOptions.PWD)
										|| selectedCloudSafeFile.isFolder() && selectedCloudSafeFile.isOption(CloudSafeOptions.ENC)) {
									JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.notPossibleToMove");
									return;
								} else {
									cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, operatorSessionBean.getDcemUser());
								}
							} else if (selectedFolder.isOption(CloudSafeOptions.ENC) && selectedCloudSafeFile.isOption(CloudSafeOptions.FPD)) {
								cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, operatorSessionBean.getDcemUser());
							} else if (selectedCloudSafeFile.isOption(CloudSafeOptions.ENC) || selectedCloudSafeFile.isOption(CloudSafeOptions.PWD)) {
								cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, operatorSessionBean.getDcemUser());
							}
						} else {
							cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, currentFolderId, operatorSessionBean.getDcemUser());
						}
						cloudSafeEntityFiles.remove(selectedCloudSafeFile);
					} else {
						if (currentFolderId == null) {
							JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.fileAlreadyExists",
									selectedCloudSafeFile.getName());
						} else {
							CloudSafeEntity cse = cloudSafeLogic.getCloudSafe(currentFolderId);
							JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.fileAlreadyExists",
									selectedCloudSafeFile.getName(), cse.getName());
						}
					}
				}
			} catch (Exception e) {
				logger.error("failed to move file", e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				selectedFilesToCut.clear();
				PrimeFaces.current().ajax().update("documentForm:nodeContextMenu");
				return;
			}
			selectedFilesToCut.clear();
			shouldRefreshCurrentFiles = true;
			PrimeFaces.current().ajax().update("documentForm:nodeContextMenu");
			PrimeFaces.current().ajax().update("documentForm");

		}
	}

	public boolean isPasteDisabled() {
		if (selectedFilesToCut.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public void moveFileEntry() {
		if (selectedCloudSafeFiles == null || moveToFolder == null) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.cantMove");
			return;
		}
		try {
			for (CloudSafeEntity selectedCloudSafeFile : selectedCloudSafeFiles) {

				if (fileOrFolderExists(selectedCloudSafeFile.getName(), moveToFolder.getId(), false) == false) {
					cloudSafeLogic.moveCurrentEntry(selectedCloudSafeFile, null, moveToFolder.getId(), operatorSessionBean.getDcemUser());
					cloudSafeEntityFiles.remove(selectedCloudSafeFile);
				} else {
					JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.fileNameAlreadyExists",
							selectedCloudSafeFile.getName(), moveToFolder.getName());
				}
			}

		} catch (Exception e) {
			logger.error(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return;
		}
		selectedCloudSafeFiles.clear();
		PrimeFaces.current().executeScript("PF('moveEntryConfirmationDialog').hide();");
	}

	public void openEditDocument(CloudSafeEntity cloudSafeEntity) {
		selectedCloudSafeFiles.clear();
		selectedCloudSafeFiles.add(cloudSafeEntity);
		openEditDocument();
	}

	public void openEditDocument() {
		CloudSafeEntity cloudSafeEntity;
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.size() == 1) {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe(selectedCloudSafeFiles.get(0).getId()); // refresh Entity
			cloudSafeEntity.setWriteAccess(selectedCloudSafeFiles.get(0).isWriteAccess());

		} else {
			JsfUtils.addWarnMessage(resourceBundle.getString("documentView.message.selectOnlyOneFile"));
			return;
		}
		try {
			dmNewDocumentView.editDocument(cloudSafeEntity, cloudSafeEntityFiles);
			viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmNewDocumentView.getSubject().getViewName());
		} catch (DcemException e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (InvalidCipherTextIOException e) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "documentView.error.cantEncryptCorruptFile");
		} catch (Throwable e) {
			logger.error("Couldn't edit file " + cloudSafeEntity.getName(), e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}
	
	public void openWorkflow() {
		CloudSafeEntity cloudSafeEntity;
		if (selectedCloudSafeFiles != null && selectedCloudSafeFiles.size() == 1) {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe(selectedCloudSafeFiles.get(0).getId()); // refresh Entity
			cloudSafeEntity.setWriteAccess(selectedCloudSafeFiles.get(0).isWriteAccess());

		} else {
			JsfUtils.addWarnMessage(resourceBundle.getString("documentView.message.selectOnlyOneFile"));
			return;
		}
		openWorkflow(cloudSafeEntity);		
	}

	public void openWorkflow(CloudSafeEntity cloudSafeEntity) {
		workflowView.editWorkflows(cloudSafeEntity);
		viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + workflowView.getSubject().getViewName());
	}

	public StreamedContent getThumbnailStream(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity == null) {
			return null;
		}
		try {
			String contentType = "image/jpeg";
			byte[] thumbnail = cloudSafeEntity.getThumbnail();
			if (thumbnail == null) {
				DcemMediaType mediaType = cloudSafeEntity.getDcemMediaType();
				String resourcePath;
				if (mediaType != null) {
					resourcePath = mediaType.getIconResource();
				} else {
					resourcePath = DcemConstants.ICONS_16_PATH + getFileIcon(cloudSafeEntity);
				}
				if (resourcePath.endsWith(".svg")) {
					contentType = DcemMediaType.SVG.getMediaType();
				}
				final InputStream in = JsfUtils.class.getResourceAsStream(resourcePath);
				if (in == null) {
					logger.error("Couldn't find " + resourcePath);
					return null;
				}
				return DefaultStreamedContent.builder().contentType(contentType).stream(() -> in).build();
			} else {
				if (cloudSafeEntity.getDcemMediaType() == DcemMediaType.SVG) {
					contentType = DcemMediaType.SVG.getMediaType();
				}
				InputStream in = new ByteArrayInputStream(thumbnail);
				return DefaultStreamedContent.builder().contentType(contentType).stream(() -> in).build();
			}

		} catch (Throwable e) {
			logger.error("couldn't fetch thumbnail " + cloudSafeEntity.getName(), e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return null;
		}
	}

	public void onSelection(CloudSafeEntity cloudSafeEntity) {
		selectedCloudSafeFiles.remove(cloudSafeEntity);
		if (cloudSafeEntity.isSelected() == true) {
			selectedCloudSafeFiles.add(cloudSafeEntity);
		}
	}

	public void onColumnToggle(ToggleEvent event) {
		try {
			int columPosition = (Integer) event.getData();
			DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("documentForm").findComponent("documentTable");
			UIComponent columnComponent = dataTable.getChildren().get(columPosition);
			String columnId = columnComponent.getId();
			columnFilterSettings.put(columnId, event.getVisibility() == Visibility.VISIBLE);
			updateColumnFilter();
		} catch (Exception e) {
			logger.warn("Could not toggle column filter", e);
		}
	}

	private void loadColumnFilter() {
		columnFilterSettings = new HashMap<String, Boolean>();
		try {
			String value = operatorSessionBean.getLocalStorageUserSetting(COLUMN_TOGGLER + LS_VIEWNAME);
			if (value != null) {
				String[] columnVisibilties = value.split(", ");
				for (String columnVisibilty : columnVisibilties) {
					String[] keyValuePair = columnVisibilty.split("=");
					columnFilterSettings.put(keyValuePair[0], Boolean.valueOf(keyValuePair[1]));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				operatorSessionBean.removeLocalStorageUserSetting(COLUMN_TOGGLER + LS_VIEWNAME);
			} catch (Exception exp) {
			}
		}
	}

	public void actionEmailToken() {
		try {
			LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(documentManagementModule.getPreferences().getEmailTokenValidFor());
			StringBuilder sb = new StringBuilder(); // identifier
			sb.append(emailForGroup);
			sb.append('-');
			if (emailForGroup == 0) {
				sb.append(operatorSessionBean.getDcemUser().getId());
			} else {
				sb.append(selectedGroup);
			}
			sb.append('-');
			sb.append(selectedFolder.getId());
			if (tokenExpiryDate == null || tokenExpiryDate.isAfter(localDateTime) || sb.toString().equals(lastmailTokenIdentifier) == false) {
				tokenExpiryDate = localDateTime;
				lastmailTokenIdentifier = sb.toString();
				mailToken = urlTokenLogic.addMailUrlToken(DocumentManagementModule.MODULE_ID, tokenExpiryDate, lastmailTokenIdentifier);
			}
		} catch (DcemException e) {
			logger.warn("Could not create Email Token: ", e);
			JsfUtils.addErrorMessage("Could not create Email Token: " + e.toString());
		}
	}

	public boolean isMailToken() {
		return mailToken != null;
	}

	public String getEmailAddress() {
		return systemModule.getPreferences(TenantIdResolver.getMasterTenant()).geteMailReceiveAccount();
	}

	public boolean isEmailToken() {
		return documentManagementModule.getPreferences().getEmailTokenValidFor() > 0;
	}

	private void updateColumnFilter() {
		try {
			if (columnFilterSettings == null || columnFilterSettings.isEmpty()) {
				operatorSessionBean.removeLocalStorageUserSetting(COLUMN_TOGGLER + LS_VIEWNAME);
				return;
			}
			List<String> columnVisibilty = new ArrayList<String>();
			for (Map.Entry<String, Boolean> entry : columnFilterSettings.entrySet()) {
				columnVisibilty.add(String.format("%s=%s", entry.getKey(), entry.getValue()));
			}
			operatorSessionBean.setLocalStorageUserSetting(COLUMN_TOGGLER + LS_VIEWNAME, String.join(", ", columnVisibilty));
		} catch (Exception e) {
			logger.warn("Could not save role filter to local storage for user: " + operatorSessionBean.getDcemUser().getDisplayName(), e);
		}
	}

	public void setDisplayMode(String mode) {
		try {
			this.displayMode = DmDisplayMode.valueOf(mode.toUpperCase());
			shouldRefreshCurrentFiles = true;
			if (displayMode != null) {
				operatorSessionBean.setLocalStorageUserSetting("displayMode", displayMode.name());
			}
		} catch (Exception e) {
			logger.warn("Invalid display mode: " + mode, e);
			this.displayMode = DmDisplayMode.LIST;
		}
	}

	public DmDisplayMode getDisplayMode() {
		if (displayMode == null) {
			loadDisplayMode();
		}
		return displayMode;
	}

	private void loadDisplayMode() {
		try {
			String savedMode = operatorSessionBean.getLocalStorageUserSetting("displayMode");
			if (savedMode != null) {
				this.displayMode = DmDisplayMode.valueOf(savedMode.toUpperCase());
			} else {
				this.displayMode = DmDisplayMode.LIST;
			}
		} catch (Exception e) {
			logger.warn("Could not load display mode", e);
			this.displayMode = DmDisplayMode.LIST;
		}
	}

	private void saveDisplayMode() {
		try {
			if (displayMode != null) {
				operatorSessionBean.setLocalStorageUserSetting("displayMode", displayMode.name());
			}
		} catch (Exception e) {
			logger.warn("Could not save display mode", e);
		}
	}

	private void updateTable() {
		try {
			DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("documentForm").findComponent("documentTable");
			if (dataTable != null) {
				dataTable.resetColumns();
			}
		} catch (Exception e) {
			logger.debug(e);
		}
	}

	public String getOwnerName(CloudSafeEntity cloudSafeEntity) {
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.USER && cloudSafeEntity.getUser().getId() == operatorSessionBean.getDcemUser().getId()) {
			return null;
		}
		return cloudSafeEntity.getOwnerName();
	}

	public void showDocumentWithTag(CloudSafeTagEntity cloudSafeTagEntity) {
		selectedFilterTags = new ArrayList<CloudSafeTagEntity>();
		selectedFilterTags.add(cloudSafeTagEntity);
		searchTerm = "";
		actionSearch();
	}

	public String getPath(CloudSafeEntity cloudSafeEntity) {
		return cloudSafeLogic.getPath(cloudSafeEntity);
	}

	public boolean isVisibility(String columnId) {
		return columnFilterSettings.getOrDefault(columnId, true);
	}

	public boolean isLocationVisible() {
		return isVisibility("location") && (searchResultMode == true);
	}

	public List<CloudSafeEntity> getSelectedCloudSafeFiles() {
		return selectedCloudSafeFiles;
	}

	public void setSelectedCloudSafeFiles(List<CloudSafeEntity> selectedCloudSafeFiles) {
		this.selectedCloudSafeFiles = selectedCloudSafeFiles;
	}

	public CloudSafeEntity getMoveToFolder() {
		return moveToFolder;
	}

	public void setMoveToFolder(CloudSafeEntity moveToFolder) {
		this.moveToFolder = moveToFolder;
	}

	public String getDownloadFileName() {
		return downloadFileName;
	}

	public void setDownloadFileName(String downloadFileName) {
		this.downloadFileName = downloadFileName;
	}

	public String getFilePassword() {
		return filePassword;
	}

	public void setFilePassword(String filePassword) {
		this.filePassword = filePassword;
	}

	public MenuModel getBreadCrumbModel() {
		return breadCrumbModel;
	}

	public void setBreadCrumbModel(MenuModel breadCrumbModel) {
		this.breadCrumbModel = breadCrumbModel;
	}

	public String getSelectedFileName() {
		return selectedFileName;
	}

	public void setSelectedFileName(String selectedFileName) {
		this.selectedFileName = selectedFileName;
	}

	public boolean isEditFolderProcess() {
		return editFolderProcess;
	}

	public void setEditFolderProcess(boolean editFolderProcess) {
		this.editFolderProcess = editFolderProcess;
	}

	public String getAddFolderName() {
		return addFolderName;
	}

	public void setAddFolderName(String addFolderName) {
		this.addFolderName = addFolderName;
	}

	public Integer getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Integer documentId) {
		this.documentId = documentId;
	}

	public List<CloudSafeTagEntity> getFilterTags() {
		return filterTags;
	}

	public void setFilterTags(List<CloudSafeTagEntity> filterTags) {
		this.filterTags = filterTags;
	}

	public Map<String, Boolean> getColumnFilterSettings() {
		return columnFilterSettings;
	}

	public void setColumnFilterSettings(Map<String, Boolean> columnFilterSettings) {
		this.columnFilterSettings = columnFilterSettings;
	}

	public int getEmailForGroup() {
		return emailForGroup;
	}

	public void setEmailForGroup(int emailForGroup) {
		this.emailForGroup = emailForGroup;
	}

	public String getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(String selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public String getMailToken() {
		return mailToken;
	}

	public String getTokenExpiryDate() {
		return DcemUtils.formatDateTime(operatorSessionBean.getLocale(), tokenExpiryDate);
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		if (searchTerm != null) {
			this.searchTerm = searchTerm.trim();
		} else {
			this.searchTerm = "";
		}
	}

	public List<CloudSafeTagEntity> getSelectedFilterTags() {
		return selectedFilterTags;
	}

	public void setSelectedFilterTags(List<CloudSafeTagEntity> selectedFilterTags) {
		this.selectedFilterTags = selectedFilterTags;
	}

	public boolean isSearchResultMode() {
		// System.out.println("DmDocumentView.isSearchResultMode() " + searchResultMode);
		return searchResultMode;
	}

	public void setSearchResultMode(boolean searchResultMode) {
		this.searchResultMode = searchResultMode;
	}

	public boolean isShareDocumentsMode() {
		return shareDocumentsMode;
	}

	public boolean isFileWithPassword() {
		return selectedCloudSafeFiles.size() == 0 ? false : selectedCloudSafeFiles.get(0).isOption(CloudSafeOptions.PWD);
	}

	public CloudSafeEntity getSelectedFolder() {
		return selectedFolder;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public boolean isShouldRefreshCurrentFiles() {
		return shouldRefreshCurrentFiles;
	}

	public void setShouldRefreshCurrentFiles(boolean shouldRefreshCurrentFiles) {
		this.shouldRefreshCurrentFiles = shouldRefreshCurrentFiles;
	}

}
