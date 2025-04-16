package com.doubleclue.dcem.ps.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.io.InvalidCipherTextIOException;
import org.hibernate.validator.constraints.Length;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.comm.thrift.SdkCloudSafe;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.ps.logic.KeePassLogic;
import com.doubleclue.dcem.ps.logic.KeepassEntryLogic;
import com.doubleclue.dcem.ps.logic.PasswordSafeModule;
import com.doubleclue.dcem.ps.logic.PmAppHubLogic;
import com.doubleclue.dcem.ps.subjects.PsChooseFileSubject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.domain.Meta;
import de.slackspace.openkeepass.domain.MetaBuilder;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

@Named("psChooseFileView")
@SessionScoped
public class PsChooseFileView extends DcemView {

	private static final long serialVersionUID = 1L;

	private static String FULL_PAGE_UPDATE = "viewPart";

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	PmAppHubLogic upAppHubLogic;

	@Inject
	KeepassEntryLogic keepassEntryLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AsModule asModule;

	@Inject
	KeePassLogic userPortalKeePassLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	PsChooseFileSubject chooseFileSubject;

	@Inject
	PsFileView psFileView;

	public static final String FILE_EXTENSION = ".kdbx";
	public static final String PS_HISTORY = "psHistory";

	private CloudSafeEntity cloudSafeEntity;
	List<PasswordSafeRecentFile> recentFiles = new ArrayList<PasswordSafeRecentFile>();
	private String masterPassword;

	private boolean rememberPassword;
	byte[] uploadedImage;

	@Length(min = 1, max = 64)
	private String customPropertyName;
	@Length(min = 1, max = 1024)

	private long selectedPasswordSafeFile;
	private UploadedFile uploadedFile;
	private UploadedFile previousUploadedFile;
	private String newFileName;
	private String uploadPassword;

	@Length(min = 1, max = 64)
	private String password;

	private CloudSafeEntity selectionKeePass;

	boolean advanceRecording;
	List<SdkCloudSafe> cloudSafeList;
	List<SdkCloudSafe> cloudSafeSharedList;
	private SdkCloudSafe selectedFile;

	private Logger logger = LogManager.getLogger(PsChooseFileView.class);
	ResourceBundle resourceBundle;

	@PostConstruct
	public void init() {
		this.setSubject(chooseFileSubject);
		resourceBundle = ResourceBundle.getBundle(PasswordSafeModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		String psHistory = operatorSessionBean.getLocalStorageUserSetting(PS_HISTORY);
		if (psHistory == null || psHistory.isBlank()) {
			return;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			TypeReference<LinkedList<PasswordSafeRecentFile>> typeRef = new TypeReference<LinkedList<PasswordSafeRecentFile>>() {
			};
			recentFiles = objectMapper.readValue(psHistory, typeRef);
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			return;
		}
	}

	@Override
	public void reload() {
		cloudSafeList = null;
		cloudSafeSharedList = null;

		List<SdkCloudSafe> list = getAvailableOwnedPasswordSafeFiles();
		if (list == null) {
			return;
		}
		if (list.isEmpty()) {
			// TODO redirect
			return;
		}

		if (selectedPasswordSafeFile > 0) {
			// TODO redirect
			return;
		}

	}

	private boolean checkUserLoggedIn() {
		try {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) selectedPasswordSafeFile);
			if (cloudSafeEntity == null) {
				return false;
			}
			if (checkForSavedMasterPassword(cloudSafeEntity) == true) {
				// Get details of file share if available
				List<CloudSafeShareEntity> sharedFiles = cloudSafeLogic.getUserCloudSafeShareEntities(operatorSessionBean.getDcemUser(),
						cloudSafeEntity.getName());
				for (CloudSafeShareEntity cloudSafeShareEntity : sharedFiles) {
					if (cloudSafeShareEntity.getCloudSafe().getId() == cloudSafeEntity.getId()) {
						cloudSafeEntity.setWriteAccess(cloudSafeShareEntity.isWriteAccess());
					}
				}
				return true;
			} else {
				return false;
			}
		} catch (DcemException e) {
			logger.warn(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			logger.warn(e.toString(), e);
		}
		return false;
	}

	public void setPsHistory(String psHistory) {
		if (psHistory == null || psHistory.isEmpty()) {
			recentFiles = new ArrayList<PasswordSafeRecentFile>();
			return;
		}
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			TypeReference<LinkedList<PasswordSafeRecentFile>> typeRef = new TypeReference<LinkedList<PasswordSafeRecentFile>>() {
			};
			recentFiles = objectMapper.readValue(psHistory, typeRef);
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			return;
		}
	}

	public void actionCreateFirstFile() {
		cloudSafeList = null;
		if (getAvailableOwnedPasswordSafeFiles().isEmpty()) {
			showDialog("newKeePass");
		}
	}

	public boolean checkForSavedMasterPassword(CloudSafeEntity cloudSafeEntity) {
		PasswordSafeRecentFile recentFile = null;
		if (recentFiles != null) {
			for (PasswordSafeRecentFile recentFile2 : recentFiles) {
				if (cloudSafeEntity.getName().equals(recentFile2.getName()) && recentFile2.getGroup() == null) {
					recentFile = recentFile2;
					break;
				}
			}
			if (recentFile != null && recentFile.encPassword != null) {
				byte[] data = Base64.getDecoder().decode(recentFile.encPassword);
				try {
					data = SecureServerUtils.decryptDataSalt(asModule.getConnectionKeyArray(), data);
					masterPassword = new String(data, DcemConstants.CHARSET_UTF8);
					return true;
				} catch (Exception e) {
					JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
					return false;
				}
			}
		}
		return false;
	}

	public StreamedContent actionDownloadFile(SdkCloudSafe sdkCloudSafe) {
		InputStream inputStream;
		try {
			char[] password = null;
			CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) sdkCloudSafe.getUniqueKey().dbId);
			inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, password, operatorSessionBean.getDcemUser());
			return DefaultStreamedContent.builder().contentType(DcemMediaType.KEEPASS.getMediaType())
					.name(cloudSafeEntity.getNameWithExtension()).stream(() -> inputStream).build();
		} catch (DcemException e) {
			logger.error("Coundn't downlaod file ", e);
			if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_READ_ERROR) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} else {
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "documentView.error.verifyPasswordForFolder");
			}
			return null;
		} catch (Throwable e) {
			logger.error("Coundn't downlaod files " , e);
			JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "documentView.error.verifyPasswordForFolder");
			return null;
		}
	}

	public void actionOpenFile(SdkCloudSafe sdkCloudSafe) {
		selectedFile = sdkCloudSafe;
		selectedPasswordSafeFile = sdkCloudSafe.getUniqueKey().getDbId();
		if (checkUserLoggedIn() == false) {
			showDialog("loginAppHubKeePass");
			return;
		} else {
			try {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), masterPassword, false, null);
			} catch (Exception e) {
				logger.info("", e);
			}
			// TODO
		}
		InputStream inputStream;
		try {
			inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
			psFileView.setKeePassFile(loadKeepassFile(inputStream, masterPassword));
			psFileView.setCloudSafeEntity(cloudSafeEntity);
			psFileView.setMasterPassword(masterPassword);
			viewNavigator.setActiveView(PasswordSafeModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + psFileView.getSubject().getViewName());
		} catch (DcemException e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.warn(e.toString(), e);
			return;
		}
	}

	public String getGroupOwnerName(long dbId) {
		CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) dbId);
		if (cloudSafeEntity != null && cloudSafeEntity.getGroup() != null) {
			return cloudSafeEntity.getGroup().getName();
		}
		return null;
	}

	public void actionToPasswordSafeSelection() {
		selectedPasswordSafeFile = 0;
		cloudSafeEntity = null;
	}

	public void actionNewFile() {
		String name = newFileName + FILE_EXTENSION;
		KeePassFile keePassFile_ = new KeePassFileBuilder(newFileName).build();
		Meta meta = new MetaBuilder(keePassFile_.getMeta()).historyMaxSize(0).historyMaxItems(0).build();
		keePassFile_ = new KeePassFileBuilder(newFileName).withMeta(meta).build();
		try {
			cloudSafeEntity = userPortalKeePassLogic.createCloudSafeEntity(name);
			cloudSafeEntity.setDcemMediaType(DcemMediaType.KEEPASS);
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile_, name, uploadPassword);
			if (content == null) {
				logger.warn("actionNewKeePass - failed to create entry");
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "appHub.error.FAILED_TO_CREATE_ENTRY", selectedPasswordSafeFile);
				return;
			}
			password = uploadPassword;
			// TODO keePassFile = keePassFile_;
			loadKeepassFile(content, uploadPassword);
			selectionKeePass = cloudSafeEntity;
			password = uploadPassword;
			uploadPassword = null;
			newFileName = null;
			if (rememberPassword) {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), password, false, null);
			} else {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), null, true, null);
			}
			// TODO currentOpenPasswordSafe = cloudSafeEntity;
			masterPassword = password;
			hideDialog("newKeePass");
			selectedPasswordSafeFile = cloudSafeEntity.getId();
			PrimeFaces.current().ajax().update(FULL_PAGE_UPDATE);
			// todo
			cloudSafeList = null;
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Throwable e) {
			logger.warn(e.getLocalizedMessage());
			JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + " - " + e.toString());
		}
	}

	public void actionUploadFile() {
		if (uploadedFile == null || uploadedFile.getContent().length == 0) {
			if (previousUploadedFile != null) {
				uploadedFile = previousUploadedFile;
			} else {
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.NO_FILE_SELECTED");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
				return;
			}
		}
		if (uploadPassword == null || uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.MISSING_PASSWORD");
			PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			return;
		} else {
			uploadPassword = uploadPassword.trim();
			try (ByteArrayInputStream bais = new ByteArrayInputStream(uploadedFile.getContent());) {
				KeePassDatabase databaseTemp = KeePassDatabase.getInstance(bais);
				KeePassFile keePassFileTemp = databaseTemp.openDatabase(uploadPassword);
				if (fileExists(uploadedFile.getFileName()) == false) {
					uploadFile(uploadPassword, keePassFileTemp);
				} else {
					PrimeFaces.current().executeScript("PF('confirmUploadDialog').show();");
				}
				cloudSafeList = null;
			} catch (KeePassDatabaseUnreadableException e) {
				e.printStackTrace();
				if (e.getCause() != null && e.getCause() instanceof javax.crypto.BadPaddingException) {
					JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
				} else {
					logger.info("User: " + operatorSessionBean.getDcemUser().getAccountName() + " file: " + uploadedFile.getFileName() + " Exception: "
							+ e.toString(), e);
					JsfUtils.addErrorMessage(e.getMessage());
				}
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (UnsupportedOperationException e) {
				logger.warn("Unsopported operation Exception", e);
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.WRONG_FILE_FORMAT");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (DcemException e) {
				logger.warn("Couldn’t upload to database", e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (Exception e) {
				logger.warn("Couldn’t upload to database", e);
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			}
		}
	}

	public void confirmUpload() {
		// TODO uploadFile(uploadPassword, keePassFileTemp);
		PrimeFaces.current().executeScript("PF('confirmUploadDialog').hide();");
	}

	private void uploadFile(String uploadPassword, KeePassFile keePassFileTemp) {
		try {
			cloudSafeEntity = userPortalKeePassLogic.createCloudSafeEntity(uploadedFile.getFileName());
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFileTemp, uploadedFile.getFileName(), uploadPassword);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, uploadPassword);
		} catch (DcemException exp) {
			logger.warn("upload File Failed to save the entry", exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			return;
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		selectionKeePass = cloudSafeEntity;
		password = uploadPassword;
		uploadPassword = null;
		previousUploadedFile = null;
		keePassFileTemp = null;
		try {
			if (rememberPassword) {
				updatePsHistory(selectionKeePass.getId(), selectionKeePass.getName(), password, false, null);
			} else {
				updatePsHistory(selectionKeePass.getId(), selectionKeePass.getName(), null, true, null);
			}
		} catch (Exception exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
		}

		PrimeFaces.current().ajax().update("applicationHubForm");
	}

	public void onNewFile() {
		if (newFileName == null || newFileName.isEmpty() || uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "error.EMPTY_FIELDS");
		} else {
			String name = newFileName + FILE_EXTENSION;
			try {
				if (fileExists(name)) {
					PrimeFaces.current().executeScript("PF('confirmCreateKeepassFileDialog').show();");
				} else {
					actionNewFile();
				}
			} catch (DcemApiException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				return;
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
				return;
			}
		}
	}

	public void actionOpenKeepassFile() {

		try {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) selectedPasswordSafeFile);
			// TODO currentOpenPasswordSafe = cloudSafeEntity;
			cloudSafeEntity.setWriteAccess(selectedFile.isWriteAccess());
			InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
			KeePassFile keepassFile = loadKeepassFile(inputStream, masterPassword);
			if (rememberPassword == true) {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), masterPassword, false, null);
			} else {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), null, true, null);
			}
			// passwordSafePages = PasswordSafePages.FILE_PANEL;
			psFileView.setKeePassFile(keepassFile);
			psFileView.setCloudSafeEntity(cloudSafeEntity);
			psFileView.setMasterPassword(masterPassword);
			viewNavigator.setActiveView(PasswordSafeModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + psFileView.getSubject().getViewName());
			// hideDialog("loginAppHubKeePass");
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.INVALID_PASSWORD) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} else {
				logger.warn(e.getLocalizedMessage(), e);
				JsfUtils.addErrorMessage(e.toString());
			}
			return;
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

	}

	private boolean fileExists(String fileName) throws Exception {
		List<ApiFilterItem> filters = new LinkedList<>();
		filters.add(new ApiFilterItem("user.loginId", operatorSessionBean.getDcemUser().getLoginId(), ApiFilterItem.SortOrderEnum.ASCENDING,
				ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("name", fileName, ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("recycled", "false", ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		return cloudSafeLogic.queryCloudSafeFiles(filters, 0, 100).isEmpty() == false;
	}

	public KeePassFile loadKeepassFile(byte[] content, String password_) throws DcemException {
		return loadKeepassFile(new ByteArrayInputStream(content), password_);
	}

	public KeePassFile loadKeepassFile(InputStream inputStream, String password_) throws DcemException {
		try {
			KeePassDatabase database = KeePassDatabase.getInstance(inputStream);
			if (database.getHeader().getCrsAlgorithm() == null) {
				JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "appHub.error.FAILED_LOAD_PASSWORDSAFE", selectedPasswordSafeFile);
				throw new DcemException(DcemErrorCodes.INVALID_KEEPASS_ALGORITHM, "Wrong KeePass algorithm.");
			}
			return database.openDatabase(password_);
		} catch (DcemException e) {
			throw e;
		} catch (KeePassDatabaseUnreadableException exp) {
			if (exp.getCause() != null && exp.getCause() instanceof javax.crypto.BadPaddingException) {
				throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, " Please check your Master Password. ");
			}
			throw exp;
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(PasswordSafeModule.RESOURCE_NAME, "appHub.error.FAILED_LOAD_PASSWORDSAFE", selectedPasswordSafeFile);
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.warn(e);
			}
		}
	}

	public void updatePsHistory(int id, String fileName, String password, boolean remove, String groupName) throws Exception {
		String psHistory = userPortalKeePassLogic.updatePsHistory(id, fileName, password, remove, recentFiles, groupName);
		setRecentFiles(recentFiles);
		operatorSessionBean.setLocalStorageUserSetting("psHistory", userPortalKeePassLogic.escapeJson(psHistory));
	}

	public void actionCloseAddDialog() {
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("addAppDlg");
	}

	public String getUploadMessage() {
		if (uploadedFile == null) {
			return "";
		}
		return MessageFormat.format(resourceBundle.getString("message.confirmUploadFile"), uploadedFile.getFileName());
	}

	public String getCreateMessage() {
		String newFileName = this.newFileName + FILE_EXTENSION;
		if (this.newFileName == null) {
			return "";
		} else
			return MessageFormat.format(resourceBundle.getString("message.confirmCreateFile"), newFileName);

	}

	public List<SdkCloudSafe> getAvailableOwnedPasswordSafeFiles() {
		if (cloudSafeList != null) {
			return cloudSafeList;
		}
		cloudSafeList = getCurrentPasswordSafeFiles(false);
		return cloudSafeList;
	}

	public List<SdkCloudSafe> getAvailableSharedPasswordSafeFiles() {
		if (cloudSafeSharedList != null) {
			return cloudSafeSharedList;
		}
		try {
			cloudSafeSharedList = cloudSafeLogic.getCloudSafeSharedFileList(operatorSessionBean.getDcemUser().getId(),
					"%" + AsConstants.EXTENSION_PASSWORD_SAFE, 0, CloudSafeOwner.USER, AsConstants.LIB_VERION_2);
			return cloudSafeSharedList;
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return null;
		} catch (Exception e) {
			logger.warn("getCurrentPasswordSafeFiles", e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public String getSharedByName(String sharedByFullName) {
		int index = sharedByFullName.indexOf(AsConstants.SHARE_BY_SEPERATOR);
		return sharedByFullName.substring(0, index);
	}

	public String getFileName(String sharedByFullName) {
		int index = sharedByFullName.indexOf(AsConstants.SHARE_BY_SEPERATOR);
		return sharedByFullName.substring(index + 1, sharedByFullName.length());
	}

	private List<SdkCloudSafe> getCurrentPasswordSafeFiles(boolean withSharedFiles) {

		try {
			return cloudSafeLogic.getCloudSafeFileList(operatorSessionBean.getDcemUser().getId(), "%" + AsConstants.EXTENSION_PASSWORD_SAFE, 0,
					CloudSafeOwner.USER, withSharedFiles, AsConstants.LIB_VERION_2);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return null;
		} catch (Exception e) {
			logger.warn("getCurrentPasswordSafeFiles", e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public void removeRecentFile(SdkCloudSafe file) {
		try {
			updatePsHistory((int) file.getUniqueKey().dbId, file.getUniqueKey().name, null, true, file.getUniqueKey().getGroupName());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void onUploadKeepass() {
		uploadedFile = null;
		previousUploadedFile = null;
		PrimeFaces.current().ajax().update("uploadFileForm:uploadFileDialog");
		PrimeFaces.current().executeScript("PF('uploadFileDialog').show();");
	}

	public void onNewPasswordSafeFile() {
		newFileName = null;
		uploadPassword = null;
		PrimeFaces.current().ajax().update("newKeePassForm:newKeePass");
		showDialog("newKeePass");
	}

	public void actionCloseDialognewMyAppFile() {
		hideDialog("newMyAppFile");
	}

	public void actionCloseDialogLoginAppHubKeePass() {
		PrimeFaces.current().executeScript("PF('newMyAppFile').hide()");
	}

	public void actionCloseDialogKeepassProperties() {
		PrimeFaces.current().executeScript("PF('keepassPropertiesDlg').hide()");
	}

	public boolean isRememberPassword() {
		return rememberPassword;
	}

	public void setRememberPassword(boolean rememberPassword) {
		this.rememberPassword = rememberPassword;
	}

	public String getMasterPassword() {
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}

	public UploadedFile getPreviousUploadedFile() {
		return previousUploadedFile;
	}

	public void setPreviousUploadedFile(UploadedFile previousUploadedFile) {
		this.previousUploadedFile = previousUploadedFile;
	}

	public String getUploadPassword() {
		return uploadPassword;
	}

	public void setUploadPassword(String uploadPassword) {
		this.uploadPassword = uploadPassword;
	}

	public List<PasswordSafeRecentFile> getRecentFiles() {
		return recentFiles;
	}

	public void setRecentFiles(List<PasswordSafeRecentFile> recentFiles) {
		this.recentFiles = recentFiles;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

}