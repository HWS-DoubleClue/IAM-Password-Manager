package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.ConnectException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.constraints.Length;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.comm.thrift.SdkCloudSafe;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.restapi.model.AsApiCloudSafeFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.PasswordSafeEntry;
import com.doubleclue.dcup.logic.UserPortalKeePassLogic;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Binaries;
import de.slackspace.openkeepass.domain.BinariesBuilder;
import de.slackspace.openkeepass.domain.Binary;
import de.slackspace.openkeepass.domain.BinaryBuilder;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.domain.Meta;
import de.slackspace.openkeepass.domain.MetaBuilder;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

@Named("keePassView")
@SessionScoped
public class KeePassView extends AbstractPortalView {

	private static final long serialVersionUID = 1L;

	public static final int ICON_ID_RECYCLE_BIN = 43;
	public static final int ICON_ID_ENTRY = 0;
	public static final int ICON_ID_GROUP_OPEN = 49;
	public static final int ICON_ID_GROUP_CLOSE = 48;

	public static final String fileExtension = ".kdbx";

	@Length(min = 1, max = 64)
	private String newFileName;
	private SdkCloudSafe selectedFile;
	@Length(min = 1, max = 64)
	private String password;
	private CloudSafeEntity selectionKeePass;
	private String uploadPassword;
	private String newPassword;
	private String psHistory;
	List<Entry> entries;
	List<PasswordSafeEntry> passwordSafeEntries;
	List<PasswordSafeEntry> selectedKeePassEntries;
	List<AsApiCloudSafeFile> asApiCloudSafeFileList;
	PasswordSafeEntry currentEntry;
	private TreeNode selectedNode;
	private TreeNode treeGroup;
	protected KeePassFile keePassFile = null;
	private KeePassFile keePassFileTemp;
	private String searchText;
	@Length(min = 1, max = 64)
	private String addGroupName;
	private UploadedFile uploadedFile;
	private UploadedFile previousUploadedFile;
	private boolean editEntryProcess;
	private boolean editGroupProcess;
	private boolean rememberPassword;
	private CloudSafeEntity cloudSafeEntity;
	List<PasswordSafeRecentFile> recentFiles = new ArrayList<PasswordSafeRecentFile>();
	private static final String USER_NAME = "UserName";
	private static final String NOTES = "Notes";
	private static final String URL = "URL";
	private static final String PASSWORD = "Password";
	private static final String TITLE = "Title";
	private static final List<String> PROPERTY_KEYS = new ArrayList<String>();
	private CloudSafeEntity currentOpenPasswordSafe;
	List<SdkCloudSafe> cloudSafeList;

	static {
		PROPERTY_KEYS.add(USER_NAME);
		PROPERTY_KEYS.add(NOTES);
		PROPERTY_KEYS.add(URL);
		PROPERTY_KEYS.add(PASSWORD);
		PROPERTY_KEYS.add(TITLE);
	}

	private Logger logger = LogManager.getLogger(KeePassView.class);

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AsModule asModule;

	@Inject
	KeePassEntryView keePassEntryView;

	@Inject
	UserPortalKeePassLogic userPortalKeePassLogic;

	@Inject
	PasswordSafeView passwordSafeView;

	@PostConstruct
	public void init() {
	}

	@Override
	public void onView() {
		try {
			cloudSafeList = null;
			if (selectionKeePass != null && selectionKeePass.getId() != null) {
				reloadFile();
			}
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_NOT_FOUND) {
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				actionCloseDatabase();
				return;
			} else {
				String msg = "Coudn't refresh current opened passwordSafe file. " + exp.getMessage();
				logger.warn(msg, exp);
				JsfUtils.addErrorMessage(msg);
			}
		}
	}

	private void reloadFile() throws DcemException {
		selectionKeePass = cloudSafeLogic.getCloudSafe(selectionKeePass.getId());
		cloudSafeEntity = selectionKeePass;
		InputStream fileContent = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, portalSessionBean.getDcemUser());
		loadKeepassFile(fileContent, password);
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
			recentFiles = new ArrayList<PasswordSafeRecentFile>();
			logger.info("Couln't parse passwordSafe History", e);
		}
	}

	private boolean fileExists(String fileName) throws Exception {
		List<ApiFilterItem> filters = new LinkedList<>();
		filters.add(
				new ApiFilterItem("user.loginId", portalSessionBean.getUserName(), ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("name", fileName, ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("recycled", "false", ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		if (cloudSafeLogic.queryCloudSafeFiles(filters, 0, 100).size() > 0) {
			return true;
		}
		return false;
	}

	public List<SdkCloudSafe> getCloudStorageNames() {
		try {
			if (cloudSafeList != null) {
				return cloudSafeList;
			} else {
				cloudSafeList = cloudSafeLogic.getCloudSafeFileList(portalSessionBean.getDcemUser().getId(), "%" + AsConstants.EXTENSION_PASSWORD_SAFE, 0,
						CloudSafeOwner.USER, true, AsConstants.LIB_VERION_2);
			}
			return cloudSafeList;
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
			return null;
		} catch (Exception e) {
			logger.warn("getCloudStorageNames", e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public void actionCloseDatabase() {
		keePassFile = null;
		selectedNode = null;
		treeGroup = null;
		entries = null;
		passwordSafeEntries = null;
		uploadPassword = null;
		password = null;
		selectionKeePass = null;
		currentOpenPasswordSafe = null;
	}

	private boolean isInRecyclingBin() {
		Group recycleBinGroup = getRycleBinGroup();
		TreeNode node = selectedNode;
		while (node.getData() != null) {
			if (((Group) node.getData()).equals(recycleBinGroup)) {
				return true;
			}
			node = node.getParent();
		}
		return false;
	}

	public Group getRycleBinGroup() {
		UUID uuidBin = keePassFile.getMeta().getRecycleBinUuid();
		if (uuidBin == null || keePassFile.getGroupByUUID(uuidBin) == null) {
			// Group recicleBinGroup = new
			// GroupBuilder().name(portalSessionBean.getResourceBundle().getString("title.RecycleBin")).iconId(43).build();
			Group recicleBinGroup = new GroupBuilder().name(portalSessionBean.getResourceBundle().getString("title.RecycleBin")).build();

			Meta meta = new MetaBuilder(keePassFile.getMeta()).recycleBinEnabled(true).recycleBinUuid(recicleBinGroup.getUuid()).historyMaxSize(0)
					.historyMaxItems(0).build();
			List<Group> groups = keePassFile.getRoot().getGroups().get(0).getGroups();
			groups.add(groups.size(), recicleBinGroup);
			keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
			// updateFileAndPage();
			return recicleBinGroup;
		} else {
			return keePassFile.getGroupByUUID(uuidBin);
		}
	}

	public void actionSearch() {
		if (keePassFile == null) {
			return;
		}
		if (searchText == null || searchText.isEmpty()) {
			entries = keePassFile.getEntries();
		} else {
			entries = new LinkedList<>();
			List<Entry> allEntries = keePassFile.getEntries();
			String searchLowerCase = searchText;
			searchLowerCase = searchLowerCase.toLowerCase();
			for (Entry entry : allEntries) {
				if (entry.getTitle() != null && entry.getTitle().toLowerCase().contains(searchLowerCase)) {
					entries.add(entry);
					continue;
				}
				if (entry.getUsername() != null && entry.getUsername().toLowerCase().contains(searchLowerCase)) {
					entries.add(entry);
					continue;
				}
				if (entry.getUrl() != null && entry.getUrl().toLowerCase().contains(searchLowerCase)) {
					entries.add(entry);
					continue;
				}
				if (entry.getNotes() != null && entry.getNotes().toLowerCase().contains(searchLowerCase)) {
					entries.add(entry);
					continue;
				}
			}
		}
		passwordSafeEntries = null;
	}

	public boolean isEditEntryProcess() {
		return editEntryProcess;
	}

	public void setEditEntryProcess(boolean editEntryProcess) {
		this.editEntryProcess = editEntryProcess;
	}

	public boolean isEditGroupProcess() {
		return editGroupProcess;
	}

	public void setEditGroupProcess(boolean editGroupProcess) {
		this.editGroupProcess = editGroupProcess;
	}

	public String getGroupOfEntry(PasswordSafeEntry passwordSafeEntry) {
		Group group = getGroup(passwordSafeEntry.getEntry());
		if (group == null) {
			return null;
		}
		return group.getName();
	}

	public Group getGroup(Entry entry) {
		if (entry == null) {
			return null;
		}
		for (Group group : keePassFile.getGroups()) {
			for (Entry entry2 : group.getEntries())
				if (entry2.getUuid().equals(entry.getUuid())) {
					return group;
				}
		}
		return null;
	}

	public void actionNewKeePass() {
		String name = newFileName + fileExtension;
		KeePassFile keePassFile_ = new KeePassFileBuilder(newFileName).build();
		Meta meta = new MetaBuilder(keePassFile_.getMeta()).historyMaxSize(0).historyMaxItems(0).build();
		keePassFile_ = new KeePassFileBuilder(newFileName).withMeta(meta).build();
		try {
			cloudSafeEntity = userPortalKeePassLogic.createCloudSafeEntity(name);

			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile_, name, uploadPassword);
			if (content == null) {
				return;
			}
			password = uploadPassword;
			keePassFile = keePassFile_;
			loadKeepassFile(content, uploadPassword);
			cloudSafeList = null;
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Throwable e) {
			logger.warn(e.getLocalizedMessage());
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + " - " + e.toString());
		}

		selectionKeePass = cloudSafeEntity;
		password = uploadPassword;
		uploadPassword = null;
		newFileName = null;
		try {
			if (rememberPassword) {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), password, false, null);
			} else {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), null, true, null);
			}
			currentOpenPasswordSafe = cloudSafeEntity;
		} catch (Exception e) {
			logger.warn(e);
		}
		PrimeFaces.current().executeScript("PF('newKeePass').hide();");
		reloadPage();
	}

	public void onNewKeePass() {
		if (newFileName.isEmpty() || uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.EMPTY_FIELDS");
		} else {
			String name = newFileName + fileExtension;
			try {
				if (fileExists(name)) {
					PrimeFaces.current().executeScript("PF('confirmCreateKeepassFileDialog').show();");
				} else {
					actionNewKeePass();
				}
			} catch (DcemApiException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				return;
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				return;
			}
		}
	}

	public void reloadPage() {
		ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		try {
			ec.redirect(((HttpServletRequest) ec.getRequest()).getRequestURI());
		} catch (java.io.IOException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + " - " + e.toString());
		}
	}

	public boolean isPasswordSafeFileSaved(SdkCloudSafe file) {
		for (PasswordSafeRecentFile recentFile : recentFiles) {
			if (recentFile.name.equals(file.getUniqueKey().getName()) && recentFile.encPassword != null) {
				if (file.getUniqueKey().getGroupName() == null) {
					if (recentFile.groupName == null) {
						return true;
					}
				} else if (file.getUniqueKey().getGroupName().equals(recentFile.groupName)) {
					return true;
				}
			}
		}
		return false;
	}

	public void openRecentFile(SdkCloudSafe file) throws DcemException {
		PasswordSafeRecentFile recentFile = null;
		for (PasswordSafeRecentFile recentFile2 : recentFiles) {
			if (recentFile2.name.equals(file.getUniqueKey().name)) {
				if (recentFile2.groupName == null) {
					if (file.getUniqueKey().getGroupName() == null) {
						recentFile = recentFile2;
						break;
					}
				} else if (recentFile2.groupName.equals(file.getUniqueKey().getGroupName())) {
					recentFile = recentFile2;
					break;
				}
			}
		}
		if (recentFile == null) {
			onOpenPasswordSafeFile();
			newFileName = file.getUniqueKey().name;
			selectedFile = file;
			return;
		}
		if (recentFile.encPassword == null) {
			onOpenPasswordSafeFile();
			newFileName = file.getUniqueKey().name;
			selectedFile = file;
			rememberPassword = false;
		} else {
			rememberPassword = true;
			newFileName = file.getUniqueKey().name;
			selectedFile = file;
			byte[] data = Base64.getDecoder().decode(recentFile.encPassword);
			try {
				data = SecureServerUtils.decryptDataSalt(asModule.getConnectionKeyArray(), data);
				uploadPassword = new String(data, DcemConstants.CHARSET_UTF8);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
				return;
			}
			actionKeePassOpen();
		}
	}

	public void actionKeePassOpen() {
		if (newFileName != null) {
			try {
				cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) selectedFile.getUniqueKey().getDbId());
				cloudSafeEntity.setWriteAccess(selectedFile.isWriteAccess());
				selectionKeePass = cloudSafeEntity;
				InputStream fileContent = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, portalSessionBean.getDcemUser());
				loadKeepassFile(fileContent, uploadPassword);
				password = uploadPassword;
				newFileName = null;
				uploadPassword = null;
				PrimeFaces current = PrimeFaces.current();
				current.executeScript("PF('openKeePass').hide();");
				if (rememberPassword) {
					updatePsHistory(cloudSafeEntity.getId(), selectedFile.getUniqueKey().getName(), password, false,
							selectedFile.getUniqueKey().getGroupName());
				} else {
					updatePsHistory(cloudSafeEntity.getId(), selectedFile.getUniqueKey().getName(), null, true, selectedFile.getUniqueKey().getGroupName());
				}
				currentOpenPasswordSafe = cloudSafeEntity;
				// reloadPage();
			} catch (KeePassDatabaseUnreadableException exp) {
				if (exp.getCause() != null && exp.getCause() instanceof javax.crypto.BadPaddingException) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
				} else {
					logger.info("User: " + portalSessionBean.getUserName() + " file: " + newFileName + " Exception: " + exp.toString(), exp);
					JsfUtils.addErrorMessage(exp.getMessage());
				}
			} catch (UnsupportedOperationException exp) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_FILE_FORMAT");
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			} catch (Exception e) {
				logger.info("actionKeePassOpen", e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} else {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_SELECTED_FILE");
		}
	}

	private void loadKeepassFile(byte[] content, String password_) {
		loadKeepassFile(new ByteArrayInputStream(content), password_);
	}

	private void loadKeepassFile(InputStream inputStream, String password_) {
		KeePassDatabase database = KeePassDatabase.getInstance(inputStream);
		keePassFile = database.openDatabase(password_);
		// try {
		// addCustomIcons();
		// } catch (Exception e1) {
		// logger.warn ("Couldn't add Custom Icons", e1);
		// }
		updateTreeGroup(keePassFile.getRoot(), null);
		treeGroup.setExpanded(true);
		if (selectedNode == null) {
			selectedNode = (TreeNode) treeGroup.getChildren().get(0);
		} else {

		}
		selectedNode.setSelected(true);
		Group group = (Group) selectedNode.getData();
		entries = group.getEntries();
		passwordSafeEntries = null;
	}

	public boolean isContentInTable() {
		if (treeGroup != null) {
			return true;
		}
		return false;
	}

	private void updateTreeGroup(Group rootGroup, TreeNode treeNode) {
		if (treeNode == null) {
			if (treeGroup == null) {
				treeGroup = new DefaultTreeNode(null, null);
			} else {
				treeGroup.getChildren().clear();
			}
			treeNode = treeGroup;
		}

		for (Group group : rootGroup.getGroups()) {
			TreeNode tree = new DefaultTreeNode(group, treeNode);
			if (selectedNode != null && ((Group) selectedNode.getData()).getUuid().equals(group.getUuid())) {
				selectedNode = tree;
			}
			tree.setExpanded(true);
			updateTreeGroup(group, tree);
		}
	}

	public StreamedContent getGroupIcon(Group group) {
		if (group.getIconData() == null) {
			return JsfUtils.getEmptyImage();
		}
		try {
			DefaultStreamedContent defaultStreamedContent = DefaultStreamedContent.builder().contentType("image/png")
					.stream(() -> getGroupIconInputStream(group)).build();
			// System.out.println("KeePassView.getGroupIcon() " + group.getName() + ", " +
			// group.getIconId());
			return defaultStreamedContent;
		} catch (Exception ex) {
			if (ex.getCause() instanceof ConnectException) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.CANNOT_RETRIEVE_ICON");
				return JsfUtils.getEmptyImage();
			}
			JsfUtils.addErrorMessage(ex.toString());
		}
		return JsfUtils.getEmptyImage();
	}

	public StreamedContent getEntryIcon(PasswordSafeEntry passwordSafeEntry) {
		Entry entry = passwordSafeEntry.getEntry();
		if (entry.getIconData() == null) {
			return JsfUtils.getEmptyImage();
		}
		try {
			DefaultStreamedContent defaultStreamedContent = DefaultStreamedContent.builder().contentType("image/png")
					.stream(() -> getEntryIconInputStream(entry)).build();
			// System.out.println("KeePassView.getEntryIcon() " + entry.getTitle() + ", " +
			// entry.getIconId());
			return defaultStreamedContent;
		} catch (Exception exp) {
			if (exp.getCause() instanceof ConnectException) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.CANNOT_RETRIEVE_ICON");
				return JsfUtils.getEmptyImage();
			}
			JsfUtils.addErrorMessage(exp.toString());
		}
		return JsfUtils.getEmptyImage();
	}

	protected void updateFileAndPage() {
		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, password);
			PrimeFaces.current().ajax().update("pmForm:keePassTable");
			PrimeFaces.current().ajax().update("pmForm:passwordSafeTree");

		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Throwable e) {
			logger.warn(e.getLocalizedMessage());
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + " - " + e.toString());
		}
	}

	private int getNextBinaryId() {
		int nextBinaryId = 0;
		if (getKeePassFile().getMeta().getBinaries() == null) {
			List<Binary> binaryList = new ArrayList<>();
			Binaries binaries = new BinariesBuilder().binaries(binaryList).build();
			Meta meta = new MetaBuilder(getKeePassFile().getMeta()).binaries(binaries).historyMaxSize(0).historyMaxItems(0).build();
			setKeePassFile(new KeePassFileBuilder(getKeePassFile()).withMeta(meta).build());
			return nextBinaryId;
		} else {
			List<Binary> allBinary = getKeePassFile().getMeta().getBinaries().getBinaries();
			for (int i = 0; i < allBinary.size(); i++) {
				if (allBinary.get(i).getId() > nextBinaryId) {
					nextBinaryId = allBinary.get(i).getId();
				}
			}
			nextBinaryId++;
		}
		return nextBinaryId;
	}

	public void addEntry() {
		if (currentEntry != null) {
			Group group = (Group) selectedNode.getData();
			List<Entry> list = group.getEntries();
			if (currentEntry.getEntry().getAttachments() == null) {
				return;
			} else {
				if (getKeePassFile().getMeta().getBinaries() == null) {
					List<Binary> binaryList = new ArrayList<>();
					Binaries binaries = new BinariesBuilder().binaries(binaryList).build();
					Meta meta = new MetaBuilder(getKeePassFile().getMeta()).binaries(binaries).historyMaxSize(0).historyMaxItems(0).build();
					setKeePassFile(new KeePassFileBuilder(getKeePassFile()).withMeta(meta).build());
				}
				List<Attachment> attachments = currentEntry.getEntry().getAttachments();
				int id = getNextBinaryId();
				for (int i = 0; i < attachments.size(); i++) {
					if (attachments.get(i).getRef() == -1) {
						attachments.set(i, new Attachment(attachments.get(i).getKey(), id, attachments.get(i).getData()));
						Binary binary = new BinaryBuilder().data(attachments.get(i).getData()).id(id).isCompressed(false).build();
						List<Binary> allBinary = getKeePassFile().getMeta().getBinaries().getBinaries();
						allBinary.add(binary);
						id++;
					}
				}
			}
			list.add(currentEntry.getEntry());
			try {
				byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
				if (content != null) {
					loadKeepassFile(content, password);
					PrimeFaces current = PrimeFaces.current();
					current.executeScript("PF('processEntryDialog').hide();");
				}
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	public void editEntry() {
		Entry initialEntry = selectedKeePassEntries.get(0).getEntry();
		Group group = getGroup(initialEntry);
		if (group == null) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.ENTRY_NOT_FOUND");
			return;
		}
		List<Entry> entriesList = group.getEntries();
		try {
			List<Attachment> attachments = currentEntry.getEntry().getAttachments();
			int id = getNextBinaryId();
			for (int i = 0; i < attachments.size(); i++) {
				if (attachments.get(i).getRef() == -1) {
					attachments.set(i, new Attachment(attachments.get(i).getKey(), id, attachments.get(i).getData()));
					Binary binary = new BinaryBuilder().data(attachments.get(i).getData()).id(id).isCompressed(false).build();
					List<Binary> allBinary = getKeePassFile().getMeta().getBinaries().getBinaries();
					allBinary.add(binary);
					id++;
				}
			}
			boolean found;
			for (int i = 0; i < initialEntry.getAttachments().size(); i++) {
				found = false;
				for (int j = 0; j < attachments.size(); j++) {
					if (initialEntry.getAttachments().get(i).getRef() == (attachments.get(j).getRef())) {
						found = true;
						break;
					}
				}
				if (found == false) {
					List<Binary> allBinary = getKeePassFile().getMeta().getBinaries().getBinaries();
					int foundInd = -1;
					for (int ind = 0; ind < allBinary.size(); ind++) {
						if (allBinary.get(ind).getId() == initialEntry.getAttachments().get(i).getRef()) {
							foundInd = ind;
							break;
						}
					}
					if (foundInd != -1) {
						allBinary.remove(foundInd);
					}
				}
			}
			replaceEntryList(entriesList, initialEntry, currentEntry.getEntry());
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, password);
			PrimeFaces current = PrimeFaces.current();
			current.executeScript("PF('processEntryDialog').hide();");
			PrimeFaces.current().ajax().update("pmForm:keePassTable");
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(exp.toString());
			logger.error("ADD", exp);
		}
		selectedKeePassEntries = null;
	}

	private boolean replaceEntryList(List<Entry> entriesList, Entry initialEntry, Entry editedEntry) {
		int ind = 0;
		for (; ind < entriesList.size(); ind++) {
			if (entriesList.get(ind).getUuid().equals(initialEntry.getUuid())) {
				break;
			}
		}
		if (ind < entriesList.size()) {
			entriesList.remove(ind);
			entriesList.add(ind, editedEntry);
			return true;
		}
		return false;
	}

	public void deleteEntry() {

		if (selectedKeePassEntries != null || selectedKeePassEntries.isEmpty() == false) {
			try {
				for (int i = 0; i < selectedKeePassEntries.size(); i++) {
					Group group = getGroup(selectedKeePassEntries.get(i).getEntry());
					if (isInRecyclingBin()) {
						group.getEntries().remove(selectedKeePassEntries.get(i).getEntry());
					} else {
						keePassEntryView.moveEntry(getRycleBinGroup(), selectedKeePassEntries.get(i).getEntry());
					}
				}
				byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
				if (content == null) {
					return;
				}
				loadKeepassFile(content, password);
				PrimeFaces current = PrimeFaces.current();
				current.executeScript("PF('deleteEntryDialog').hide();");
				current.executeScript("PF('recycleEntryDialog').hide();");
				PrimeFaces.current().ajax().update("pmForm:content");
			} catch (Exception exp) {
				JsfUtils.addErrorMessage(exp.toString());
				logger.error(exp);
			}
		} else {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_ENTRY_SELECTED");
		}

	}

	@Override
	public String getName() {
		return DcupViewEnum.keePassView.name();
	}

	@Override
	public String getPath() {
		return "keePassView.xhtml";
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public TreeNode getTreeGroup() {
		return treeGroup;
	}

	public void setTreeGroup(TreeNode treeGroup) {
		this.treeGroup = treeGroup;
	}

	public TreeNode getSelectedNode() {
		return selectedNode;
	}

	public void setSelectedNode(TreeNode selectedNode) {
		this.selectedNode = selectedNode;
	}

	public void actionOnNodeClick(Group group) {
		entries = group.getEntries();
		passwordSafeEntries = null;
		searchText = null;
	}

	public String getUploadMessage() {
		if (uploadedFile == null) {
			return "";
		}
		return MessageFormat.format(portalSessionBean.getResourceBundle().getString("message.confirmUploadFile"), uploadedFile.getFileName());
	}

	public String getCreateMessage() {
		String newFileName = this.newFileName + fileExtension;
		if (this.newFileName == null) {
			return "";
		} else
			return MessageFormat.format(portalSessionBean.getResourceBundle().getString("message.confirmCreateFile"), newFileName);

	}

	public void confirmUpload() {
		uploadFile(uploadPassword, keePassFileTemp);
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
			logger.warn(exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			return;
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		selectionKeePass = cloudSafeEntity;
		password = uploadPassword;
		updateTreeGroup(keePassFile.getRoot(), null);
		treeGroup.setExpanded(true);
		entries = keePassFile.getTopEntries();
		passwordSafeEntries = null;
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
	}

	public void actionUploadDatabase() {
		if (uploadedFile == null || uploadedFile.getContent().length == 0) {
			if (previousUploadedFile != null) {
				uploadedFile = previousUploadedFile;
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_FILE_SELECTED");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
				return;
			}
		}
		uploadPassword = uploadPassword.trim();

		if (uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.MISSING_PASSWORD");
			PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			return;
		} else {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(uploadedFile.getContent());) {
				KeePassDatabase databaseTemp = KeePassDatabase.getInstance(bais);
				keePassFileTemp = databaseTemp.openDatabase(uploadPassword);
				if (!fileExists(uploadedFile.getFileName())) {
					uploadFile(uploadPassword, keePassFileTemp);
				} else {
					PrimeFaces.current().executeScript("PF('confirmUploadDialog').show();");
				}
			} catch (KeePassDatabaseUnreadableException e) {
				e.printStackTrace();
				if (e.getCause() != null && e.getCause() instanceof javax.crypto.BadPaddingException) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
				} else {
					logger.info("User: " + portalSessionBean.getUserName() + " file: " + uploadedFile.getFileName() + " Exception: " + e.toString(), e);
					JsfUtils.addErrorMessage(e.getMessage());
				}
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (UnsupportedOperationException e) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_FILE_FORMAT");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (DcemException e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (Exception e) {
				logger.warn(e); // TODO
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			}
		}
	}

	public KeePassFile getKeePassFileTemp() {
		return keePassFileTemp;
	}

	public void setKeePassFileTemp(KeePassFile keePassFileTemp) {
		this.keePassFileTemp = keePassFileTemp;
	}

	public UploadedFile getPreviousUploadedFile() {
		return previousUploadedFile;
	}

	public void setPreviousUploadedFile(UploadedFile previousUploadedFile) {
		this.previousUploadedFile = previousUploadedFile;
	}

	public boolean isSearch() {
		return searchText != null && searchText.isEmpty() == false;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public List<PasswordSafeEntry> getSelectedKeePassEntries() {
		return selectedKeePassEntries;
	}

	public void setSelectedKeePassEntries(List<PasswordSafeEntry> selectedKeePassEntries) {
		this.selectedKeePassEntries = selectedKeePassEntries;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
		if (uploadedFile != null && uploadedFile.getContent().length > 0) {
			previousUploadedFile = uploadedFile;
		}
	}

	public CloudSafeEntity getSelectionKeePass() {
		return selectionKeePass;
	}

	public String getUploadPassword() {
		return uploadPassword;
	}

	public void setUploadPassword(String uploadPassword) {
		this.uploadPassword = uploadPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

	public String getAddGroupName() {
		return addGroupName;
	}

	public void setAddGroupName(String addGroupName) {
		this.addGroupName = addGroupName;
	}

	public void addGroup() {
		TreeNode parent;
		if (selectedNode == null) {
			parent = (TreeNode) treeGroup.getChildren().get(0);
		} else {
			parent = selectedNode;
		}
		Group parentGroup = (Group) parent.getData();
		List<Group> listOfChildren = parentGroup.getGroups();

		Group newGroup = new GroupBuilder(addGroupName).build();

		listOfChildren.add(newGroup);
		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, password);
			PrimeFaces current = PrimeFaces.current();
			current.executeScript("PF('processGroupDialog').hide();");
			PrimeFaces.current().ajax().update("pmForm:keePassTable");
			addGroupName = null;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void editGroup() {
		setEditGroupProcess(true);
		/**
		 * Edit the name of the Root group
		 */
		// ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		// KeePassDatabase.write(keePassFile, password, byteArrayOutputStream);

		if (selectedNode.getParent().getData() instanceof String) {
			TreeNode nodeToEdit = selectedNode;
			Group groupToEdit = (Group) nodeToEdit.getData();
			try {
				Field fieldName = groupToEdit.getClass().getDeclaredField("name");
				fieldName.setAccessible(true);
				fieldName.set(groupToEdit, addGroupName);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
				logger.warn(e1);
			}
		} else {
			TreeNode nodeToEdit = selectedNode;
			TreeNode parent = nodeToEdit.getParent();
			Group groupToEdit = (Group) nodeToEdit.getData();
			Group parentGroup = (Group) parent.getData();
			if (parentGroup == null) {
				Group editedGroup = new GroupBuilder(groupToEdit).name(addGroupName).build();
				Collections.replaceAll(keePassFile.getRoot().getGroups(), groupToEdit, editedGroup);
			} else {
				Group editedGroup = new GroupBuilder(groupToEdit).name(addGroupName).build();
				Collections.replaceAll(parentGroup.getGroups(), groupToEdit, editedGroup);
			}
		}
		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
			if (content == null) {
				// loadKeepassFile(byteArrayOutputStream.toByteArray(), password);
				return;
			}
			loadKeepassFile(content, password);
			PrimeFaces current = PrimeFaces.current();
			current.executeScript("PF('processGroupDialog').hide();");
			PrimeFaces.current().ajax().update("pmForm:keePassTable");
			addGroupName = null;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void deleteGroup() {

		TreeNode nodeToDelete = selectedNode;
		Group recyclingBin = getRycleBinGroup();
		TreeNode parent = nodeToDelete.getParent();

		// Cannot delete root keepass folder
		if (parent.getParent() == null && "root".equals(parent.getRowKey())) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.CANNOT_DELETE_ROOT");
			return;
		}

		Group parentGroup = (Group) parent.getData();
		Group groupToDelete = keePassFile.getGroupByUUID(((Group) nodeToDelete.getData()).getUuid());
		if (groupToDelete == null) {
			groupToDelete = ((Group) nodeToDelete.getData());
		}
		parentGroup = keePassFile.getGroupByUUID(parentGroup.getUuid());

		if (isInRecyclingBin()) {
			parentGroup.getGroups().remove(groupToDelete);
		} else {
			keePassEntryView.actionMoveGroupOnDelete(recyclingBin, groupToDelete, parentGroup);
		}
		updateTreeGroup(keePassFile.getRoot(), null);

		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, password);
			PrimeFaces current = PrimeFaces.current();
			current.executeScript("PF('recycleGroupDialog').hide();");
			current.executeScript("PF('deleteGroupDialog').hide();");
			PrimeFaces.current().ajax().update("pmForm:keePassTable");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		selectedNode = null;
	}

	public void onAddGroup() {
		setEditGroupProcess(false);
		addGroupName = null;
		PrimeFaces.current().executeScript("PF('processGroupDialog').show();");
		PrimeFaces.current().ajax().update("processGroupForm:processGroupDialog");
	}

	public void onEditGroup() {
		Group groupToEdit;
		if (selectedNode == null) {
			TreeNode rootNode = (TreeNode) treeGroup.getChildren().get(0);
			if (rootNode.getChildCount() < 1) {
				selectedNode = rootNode;
				setEditGroupProcess(true);
				addGroupName = ((Group) selectedNode.getData()).getName();
				PrimeFaces.current().executeScript("PF('processGroupDialog').show();");
				PrimeFaces.current().ajax().update("processGroupForm:processGroupDialog");
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_GROUP_SELECTED");
			}
		} else if (selectedNode.getParent().getData() instanceof String) {
			setEditGroupProcess(true);
			addGroupName = ((Group) selectedNode.getData()).getName();
			PrimeFaces.current().executeScript("PF('processGroupDialog').show();");
			PrimeFaces.current().ajax().update("processGroupForm:processGroupDialog");
		} else {
			if (isInRecyclingBin() == true) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.unableToRenameRecycleBin");
				return;
			}
			setEditGroupProcess(true);
			groupToEdit = (Group) selectedNode.getData();
			addGroupName = groupToEdit.getName();
			PrimeFaces.current().executeScript("PF('processGroupDialog').show();");
			PrimeFaces.current().ajax().update("processGroupForm:processGroupDialog");
		}
	}

	public void onDeleteGroup() {
		if (selectedNode == null) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_GROUP_SELECTED");
		} else if (selectedNode.getParent().getData() instanceof String) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.ROOT_SELECTED_TO_DELETE");
		} else {
			if (isInRecyclingBin()) {
				PrimeFaces.current().executeScript("PF('deleteGroupDialog').show();");
			} else {
				PrimeFaces.current().executeScript("PF('recycleGroupDialog').show();");
			}
		}
	}

	public void onAddEntry() {
		if (selectedNode == null) {
			TreeNode rootNode = (TreeNode) treeGroup.getChildren().get(0);
			if (rootNode.getChildCount() < 1) {
				selectedNode = rootNode;
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_GROUP_SELECTED");
				return;
			}
		}
		currentEntry = new PasswordSafeEntry(new EntryBuilder().build());
		setEditEntryProcess(false);
		PrimeFaces.current().executeScript("PF('processEntryDialog').show();");
		PrimeFaces.current().ajax().update("processEntryForm:processEntryDialog");
	}

	public void onEditEntry() {
		if (selectedKeePassEntries == null || selectedKeePassEntries.isEmpty() == true) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_ENTRY_SELECTED");
		} else if (selectedKeePassEntries.size() == 1) {
			Entry editEntry = selectedKeePassEntries.get(0).getEntry();
			setEditEntryProcess(true);
			currentEntry = new PasswordSafeEntry(new EntryBuilder(editEntry).build());
			PrimeFaces.current().executeScript("PF('processEntryDialog').show();");
			PrimeFaces.current().ajax().update("processEntryForm:processEntryDialog");
		} else {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.TO_MANY_SELECTED");
		}
	}

	public void onDeleteEntry() {
		// Group selectedGroup = (Group) selectedNode.getData();
		// if (selectedGroup.getEntries().isEmpty()) {
		// JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME,
		// "error.EMPTY_GROUP");
		// }
		if (selectedKeePassEntries == null || selectedKeePassEntries.isEmpty() == true) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_ENTRY_SELECTED");
		} else {
			if (isInRecyclingBin()) {
				PrimeFaces.current().executeScript("PF('deleteEntryDialog').show();");
			} else {
				PrimeFaces.current().executeScript("PF('recycleEntryDialog').show();");
			}
		}
	}

	public void changeKeepassFilePassword() {
		if ((!uploadPassword.equals(password)) && (!uploadPassword.isEmpty())) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.INVALID_FILE_PASSWORD");
		} else if (newPassword.equals(password)) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.OLD_PASSWORD_IDENTIC_TO_NEW");
		} else if (uploadPassword.isEmpty() || newPassword.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.EMPTY_FIELDS");
		} else {
			// change password!
			password = newPassword;
			try {
				byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, selectionKeePass.getName(), password);
				if (content == null) {
					return;
				}
				loadKeepassFile(content, password);
				if (rememberPassword) {
					updatePsHistory((int) selectedFile.getUniqueKey().dbId, selectedFile.getUniqueKey().getName(), password, false,
						selectedFile.getUniqueKey().getGroupName());
				} else {
					updatePsHistory((int) selectedFile.getUniqueKey().dbId, selectedFile.getUniqueKey().getName(), null, true,
							selectedFile.getUniqueKey().getGroupName());
				}
				PrimeFaces.current().executeScript("PF('changeFilePasswordDialog').hide();");
				JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("info.passwordChange"));
				PrimeFaces current = PrimeFaces.current();
				current.ajax().update("changePasswordForm:changeFilePasswordDialog");
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	public void onUploadKeepass() {
		uploadedFile = null;
		previousUploadedFile = null;
		// uploadPassword = null;

		PrimeFaces.current().ajax().update("uploadKeePassForm:uploadDialog");
		PrimeFaces.current().executeScript("PF('uploadDialog').show();");
	}

	public void onNewPasswordSafeFile() {
		newFileName = null;
		uploadPassword = null;
		PrimeFaces.current().ajax().update("newKeePassForm:newKeePass");
		PrimeFaces.current().executeScript("PF('newKeePass').show()");
	}

	public void onOpenPasswordSafeFile() {
		// uploadPassword = null;
		// newFileName = null;
		PrimeFaces.current().ajax().update("openKeePassForm:openKeePass");
		PrimeFaces.current().executeScript("PF('openKeePass').show()");
	}

	public List<PasswordSafeEntry> getPasswordSafeEntries() {
		if (passwordSafeEntries == null) {
			passwordSafeEntries = new ArrayList<PasswordSafeEntry>(entries.size());
			for (Entry entry : entries) {
				passwordSafeEntries.add(new PasswordSafeEntry(entry));
			}
		}
		return passwordSafeEntries;
	}

	public void setPasswordSafeEntries(List<PasswordSafeEntry> passwordSafeEntries) {
		this.passwordSafeEntries = passwordSafeEntries;
	}

	public void actionCloseDialogOpenKeepass() {
		PrimeFaces.current().executeScript("PF('openKeePass').hide()");
		reloadPage();
	}

	public void actionCloseDialogNewKeepass() {
		PrimeFaces.current().executeScript("PF('newKeePass').hide()");
		reloadPage();
	}

	public String getLatestFileName() {
		return null;
	}

	public String getPsHistory() {
		return psHistory;
	}

	public List<PasswordSafeRecentFile> getRecentFiles() {
		return recentFiles;
	}

	public void removeRecentFile(SdkCloudSafe file) {
		try {
			updatePsHistory((int) file.getUniqueKey().dbId, file.getUniqueKey().name, null, true, file.getUniqueKey().getGroupName());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void updatePsHistory(int id, String fileName, String password, boolean remove, String groupName) throws Exception {
		psHistory = userPortalKeePassLogic.updatePsHistory(id, fileName, password, remove, recentFiles, groupName);
		passwordSafeView.setRecentFiles(recentFiles);
		PrimeFaces.current().ajax().update("pmForm:psHistory");
		PrimeFaces.current().executeScript("localStorage.setItem('psHistory." + portalSessionBean.getDcemUser().getLoginId().replace("\\", "\\\\") + "' , '"
				+ userPortalKeePassLogic.escapeJson(psHistory) + "')");
	}

	public KeePassFile getKeePassFile() {
		return keePassFile;
	}

	public void setKeePassFile(KeePassFile keePassFile) {
		this.keePassFile = keePassFile;
	}

	public PasswordSafeEntry getCurrentEntry() {
		return currentEntry;
	}

	public void setCurrentEntry(PasswordSafeEntry currentEntry) {
		this.currentEntry = currentEntry;
	}

	private InputStream getGroupIconInputStream(Group group) {
		InputStream inputStream = null;
		if (group.getCustomIconUuid() == null) {
			inputStream = getIconInputStream(group.getIconId());
		}
		if (inputStream == null) {
			inputStream = new ByteArrayInputStream(group.getIconData());
		}
		return inputStream;
	}

	private InputStream getEntryIconInputStream(Entry entry) {
		InputStream inputStream = null;
		if (entry.getCustomIconUuid() == null) {
			inputStream = getIconInputStream(entry.getIconId());
		}
		if (inputStream == null) {
			inputStream = new ByteArrayInputStream(entry.getIconData());
		}
		return inputStream;
	}

	private InputStream getIconInputStream(int iconId) {
		InputStream inputStream = null;
		switch (iconId) {
		case ICON_ID_ENTRY:
			inputStream = this.getClass().getResourceAsStream("/passwordSafe/entry.png");
			break;
		case ICON_ID_GROUP_OPEN:
		case ICON_ID_GROUP_CLOSE:
			inputStream = this.getClass().getResourceAsStream("/passwordSafe/group.png");
			break;
		case ICON_ID_RECYCLE_BIN:
			inputStream = this.getClass().getResourceAsStream("/passwordSafe/recycleBin.png");
			break;
		}
		return inputStream;
	}

	public boolean isRememberPassword() {
		return rememberPassword;
	}

	public void setRememberPassword(boolean rememberPassword) {
		this.rememberPassword = rememberPassword;
	}

	public String getUserId() {
		return portalSessionBean.getDcemUser().getLoginId();
	}

	public String getGroupOwnerName(long dbId) {
		CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) dbId);
		if (cloudSafeEntity != null && cloudSafeEntity.getGroup() != null) {
			return cloudSafeEntity.getGroup().getName();
		}
		return null;
	}

	public CloudSafeEntity getCurrentOpenPasswordSafe() {
		return currentOpenPasswordSafe;
	}

	public void setCurrentOpenPasswordSafe(CloudSafeEntity currentOpenPasswordSafe) {
		this.currentOpenPasswordSafe = currentOpenPasswordSafe;
	}
}
