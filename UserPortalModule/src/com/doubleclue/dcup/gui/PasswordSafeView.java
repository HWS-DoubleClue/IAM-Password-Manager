package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.constraints.Length;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
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
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.userportal.entities.ApplicationHubEntity;
import com.doubleclue.dcem.userportal.entities.KeepassEntryEntity;
import com.doubleclue.dcem.userportal.logic.ActionTypeEnum;
import com.doubleclue.dcem.userportal.logic.AppHubAction;
import com.doubleclue.dcem.userportal.logic.AppHubApplication;
import com.doubleclue.dcem.userportal.logic.ApplicationSelectItem;
import com.doubleclue.dcem.userportal.logic.KeepassEntryLogic;
import com.doubleclue.dcem.userportal.logic.MyApplication;
import com.doubleclue.dcem.userportal.logic.UpAppHubLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalConstants;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcup.logic.DcupConstants;
import com.doubleclue.dcup.logic.PasswordSafeEntry;
import com.doubleclue.dcup.logic.UserPortalKeePassLogic;
import com.doubleclue.utils.TimeBasedPasscodeGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Binaries;
import de.slackspace.openkeepass.domain.BinariesBuilder;
import de.slackspace.openkeepass.domain.Binary;
import de.slackspace.openkeepass.domain.BinaryBuilder;
import de.slackspace.openkeepass.domain.CustomIcon;
import de.slackspace.openkeepass.domain.CustomIconBuilder;
import de.slackspace.openkeepass.domain.CustomIcons;
import de.slackspace.openkeepass.domain.CustomIconsBuilder;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.GroupBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.domain.Meta;
import de.slackspace.openkeepass.domain.MetaBuilder;
import de.slackspace.openkeepass.domain.Property;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;

@Named("passwordSafeView")
@SessionScoped
public class PasswordSafeView extends AbstractPortalView {

	private static final long serialVersionUID = 1L;

	private static String FULL_PAGE_UPDATE = "viewPart";

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	UpAppHubLogic upAppHubLogic;

	@Inject
	KeepassEntryLogic keepassEntryLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AsModule asModule;

	@Inject
	UserPortalKeePassLogic userPortalKeePassLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	public static final String FILE_EXTENSION = ".kdbx";
	private static String AUTHENTICATOR_KEY = "Authenticator Key";
	private static String MY_APPLICATION_NAME = "MyApplications";
	private final static String HTTPS = "https://";
	private final static String HTTP = "http";

	private List<ApplicationHubEntity> applications = null;
	private CloudSafeEntity cloudSafeEntity;
	protected KeePassFile keePassFile = null;
	List<PasswordSafeRecentFile> recentFiles = new ArrayList<PasswordSafeRecentFile>();
	private String masterPassword;

	private String psHistory;
	private boolean rememberPassword;
	private KeepassEntryEntity currentKeepassEntryEntity;
	private Group currentGroup;
	private String searchValue;
	private String selectedAddApp;

	private String authPasscodeValue;

	private String appNameValue;
	private String appUrlValue;
	private UploadedFile uploadedFileLogo;
	byte[] uploadedImage;
	private boolean editingEntry;
	private String pluginResponse;
	private String appUsernameValue;
	private String appPasswordValue;
	private String appNotesValue;

	private AttributeTypeEnum selectedActionSourceType = null;
	private ActionTypeEnum selectedActionType;
	private List<AppHubAction> selectedActions = new ArrayList<AppHubAction>();
	private AppHubAction selectedAction;
	private boolean editingAction;
	private static List<SelectItem> actionTypes = null;
	private static List<SelectItem> actionSourceTypes = null;
	private String sortedGroupName;
	private String groupSelectItem;

	private boolean editingProperty;
	@Length(min = 1, max = 64)
	private String customPropertyName;
	@Length(min = 1, max = 1024)
	private String customPropertyValue;
	private Property selectedProperty;
	private List<DcemUploadFile> uploadedFiles;
	private MyAttachment selectedAttachmentFile;
	private PasswordSafeEntry currentEntry;
	private String usernameValue;
	private String passwordValue;
	private String authCodeValue;
	private long selectedPasswordSafeFile;
	private UploadedFile uploadedFile;
	private UploadedFile previousUploadedFile;
	private String newFileName;
	private String uploadPassword;

	@Length(min = 1, max = 64)
	private String password;

	private CloudSafeEntity selectionKeePass;
	private CloudSafeEntity currentOpenPasswordSafe;
	private KeePassFile keePassFileTemp;
	boolean advanceRecording;
	List<SdkCloudSafe> cloudSafeList;
	List<SdkCloudSafe> cloudSafeSharedList;
	private SdkCloudSafe selectedFile;

	private Logger logger = LogManager.getLogger(PasswordSafeView.class);

	PasswordSafePages passwordSafePages = PasswordSafePages.CHOOSE_FILE;

	@PostConstruct
	public void init() {
		actionSourceTypes = null;
		actionTypes = null;
	}

	@Override
	public void onView() {
		cloudSafeList = null;
		cloudSafeSharedList = null;
		applications = null;
		currentEntry = null;
		if (getAvailableOwnedPasswordSafeFiles().isEmpty()) {
			passwordSafePages = PasswordSafePages.INFO;
			return;
		}

		if (selectedPasswordSafeFile > 0) {
			if (checkUserLoggedIn() == false) {
				passwordSafePages = PasswordSafePages.CHOOSE_FILE;
				selectedPasswordSafeFile = 0;
			} else {
				passwordSafePages = PasswordSafePages.FILE_PANEL;
			}
			return;
		}
		if (recentFiles.isEmpty()) {
			passwordSafePages = PasswordSafePages.CHOOSE_FILE;
		} else {
			PasswordSafeRecentFile passwordSafeRecentFile = recentFiles.get(0);
			selectedPasswordSafeFile = passwordSafeRecentFile.getId();
			if (selectedPasswordSafeFile == 0 || checkUserLoggedIn() == false) {
				passwordSafePages = PasswordSafePages.CHOOSE_FILE;
			} else {
				passwordSafePages = PasswordSafePages.FILE_PANEL;
			}
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
				List<CloudSafeShareEntity> sharedFiles = cloudSafeLogic.getUserCloudSafeShareEntities(portalSessionBean.getDcemUser(),
						cloudSafeEntity.getName());
				for (CloudSafeShareEntity cloudSafeShareEntity : sharedFiles) {
					if (cloudSafeShareEntity.getCloudSafe().getId() == cloudSafeEntity.getId()) {
						cloudSafeEntity.setWriteAccess(cloudSafeShareEntity.isWriteAccess());
					}
				}
				InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
				loadKeepassFile(inputStream, masterPassword);
				currentOpenPasswordSafe = cloudSafeEntity;
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

	@Override
	public String getName() {
		return "passwordSafeView";
	}

	public void appUrlValueValidate() {
		try {
			if (appUrlValue.toLowerCase().startsWith(HTTP) == false) {
				appUrlValue = HTTPS + appUrlValue;
			}
			uploadedImage = upAppHubLogic.appUrlValueValidate(appUrlValue, true);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.invalidUrl"));
			return;
		}
		PrimeFaces.current().ajax().update("addAppsForm:tabView:addAppUrlField");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:fileLogoImg");
	}

	public List<ApplicationSelectItem> getAvailableApplications() {
		List<ApplicationHubEntity> apps = null;
		try {
			apps = upAppHubLogic.getAllApplicationsByName(null);
		} catch (Exception e) {
			logger.warn("passwordSafeView.getAvailableApplications : Something went wrong.", e);
			JsfUtils.addErrorMessage("Something went wrong when trying to get applications.");
		}
		if (apps == null) {
			return new ArrayList<ApplicationSelectItem>();
		}
		List<ApplicationSelectItem> result = new ArrayList<ApplicationSelectItem>();
		for (ApplicationHubEntity applicationHubEntity : apps) {
			result.add(new ApplicationSelectItem(applicationHubEntity.getId(), applicationHubEntity.getName(), applicationHubEntity.getLogo()));
		}
		PrimeFaces.current().ajax().update("applicationHubForm");
		return result;
	}

	public void onAddGroup() {
		if (isKeepPassExists() == false) {
			showDialog("newKeePass");
			return;
		}
		sortedGroupName = null;
		PrimeFaces.current().ajax().update("sortedGroupForm");
		showDialog("sortedGroupDialog");
	}

	private void updateDashboardView() {
		PrimeFaces.current().ajax().update("applicationHubForm");
	}

	private boolean isKeepPassExists() {
		try {
			if (selectedPasswordSafeFile == 0) {
				return false;
			}
			if (cloudSafeLogic.getCloudSafe((int) selectedPasswordSafeFile) != null) {
				return true;
			}
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(e.toString());
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

	private void processValuesBySource(AppHubApplication appHubApplication) throws DcemException {
		for (AppHubAction appHubAction : appHubApplication.getActions()) {
			ActionTypeEnum actionTypeEnum = ActionTypeEnum.valueOf(appHubAction.getType());
			switch (actionTypeEnum) {
			case delay:
				appHubAction.setOutputValue(appHubAction.getValueParameter());
				break;
			case input:
				AttributeTypeEnum attributeTypeEnum = AttributeTypeEnum.valueOf(appHubAction.getValueSourceType());
				if (attributeTypeEnum == null || ActionTypeEnum.input.name().equals(appHubAction.getType()) == false) {
					continue;
				}
				switch (attributeTypeEnum) {
				case USER_INPUT:
					String[] props = appHubAction.getValueParameter().split(",");
					Property property = null;
					for (String prop : props) {
						property = currentEntry.getEntry().getPropertyByName(prop.trim());
						if (property != null) {
							if (property.getPropertyValue().getValue().isEmpty()) {
								property = null;
								continue;
							}
							appHubAction.setOutputValue(property.getPropertyValue().getValue());
							break;
						}
					}
					if (property == null) {
						throw new DcemException(DcemErrorCodes.INVALID_INPUT_USER_FIELD, appHubAction.getValueParameter());
					}
					break;
				case AUTHENTICATOR_PASSCODE:
					Property keePassProperty = currentEntry.getPropertyByName(AUTHENTICATOR_KEY);
					if (keePassProperty != null) {
						String key = keePassProperty.getPropertyValue().getValue();
						// It is possible to not configure authenticator in case website has authenticator disabled
						if (key.isEmpty()) {
							JsfUtils.addWarningMessage(UserPortalModule.RESOURCE_NAME, "appHub.warn.UPDATED_PROPERTIES", selectedPasswordSafeFile);
							break;
						}
						String passcode = generateTotpCode(key, null);
						if (passcode == null) {
							throw new DcemException(DcemErrorCodes.GENERATE_OTP_FAILED, appHubAction.getValueParameter());
						}
						appHubAction.setOutputValue(passcode);
					} else {
						throw new DcemException(DcemErrorCodes.NO_OTP_KEY_FOUND, null);
						// JsfUtils.addWarningMessage(UserPortalModule.RESOURCE_NAME, "appHub.warn.UPDATED_PROPERTIES", selectedPasswordSafeFile);
					}
					break;
				case STATIC_TEXT:
					appHubAction.setOutputValue(appHubAction.getValueParameter());
					break;
				default:
					DcemUser user = portalSessionBean.getDcemUser();
					List<ClaimAttribute> claimAttributes = authenticationLogic.getClaimAttributeValues(getClaimAttributes(appHubApplication), user, null, null);
					for (ClaimAttribute claimAttribute : claimAttributes) {
						if (appHubAction.getValueParameter().equals(claimAttribute.getName())) {
							appHubAction.setOutputValue(claimAttribute.getValue());
							break;
						}
					}
					break;
				}
			default:
				break;
			}
		}
		return;
	}

	public String generateTotpCode() {
		return generateTotpCode(null, currentEntry);
	}

	public String generateTotpCode(String key, PasswordSafeEntry entry) {
		if (entry != null && entry.getPropertyByName(AUTHENTICATOR_KEY) != null) {
			key = entry.getPropertyByName(AUTHENTICATOR_KEY).getValue();
		}
		key = key.replaceAll("([^a-zA-Z0-9]|\\s)+", "");
		try {
			Integer passcode = TimeBasedPasscodeGenerator.generatePasscode(BaseEncoding.base32().decode(key.toUpperCase()), null, 30 * 1000,
					TimeBasedPasscodeGenerator.TOTP_ALGORITHM_HMAC_SHA1);
			return String.format("%06d", passcode);
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_OTP_GENERATE");
			return null;
		}
	}

	public void actionCreateFirstFile() {
		cloudSafeList = null;
		searchValue = null;
		if (getAvailableOwnedPasswordSafeFiles().isEmpty()) {
			showDialog("newKeePass");
		} else {
			passwordSafePages = PasswordSafePages.CHOOSE_FILE;
		}
	}

	public boolean isAuthCodeVisible() {
		if (currentEntry == null) {
			return false;
		}
		return currentEntry.getPropertyByName(AUTHENTICATOR_KEY) != null;
	}

	public boolean isCollapsed(Group group) {
		Group recycleBinGroup = getRecycleBinGroup();
		if (group.getUuid().equals(recycleBinGroup.getUuid())) {
			// System.out.println("PasswordSafeView.isCollapsed() TRUE " + group.getName());
			return true;
		}
		if (group.getEntries().isEmpty()) {
			// System.out.println("PasswordSafeView.isCollapsed() TRUE " + group.getName());
			return true;
		}
		// System.out.println("PasswordSafeView.isCollapsed() FASLE " + group.getName());
		return false;
	}

	// private Entry getEntryFromKeepass(ApplicationHubEntity applicationHubEntity) {
	// Entry entryResult = null;
	// if (applicationHubEntity.getId() == null) {
	// for (Entry entry : keePassFile.getEntries()) {
	// if (entry.getTitle().equals(applicationHubEntity.getName())) {
	// entryResult = entry;
	// break;
	// }
	// }
	// } else {
	// UUID uuid = makeUuid(applicationHubEntity.getId());
	// for (Entry entry : keePassFile.getEntries()) {
	// if (entry.getUuid().equals(uuid)) {
	// entryResult = entry;
	// break;
	// }
	// }
	// }
	// return entryResult;
	// }

	public void onAddEntry(Group group) {
		if (isKeepPassExists() == false) {
			showDialog("newKeePass");
			return;
		}
		if (group == null) {
			group = keePassFile.getRoot().getGroups().get(0);
		}
		groupSelectItem = group.getUuid().toString();
		currentGroup = group;
		appNameValue = "";
		appNotesValue = "";
		appUrlValue = "";
		appUsernameValue = "";
		appPasswordValue = "";
		authPasscodeValue = null;
		editingEntry = false;
		currentEntry = new PasswordSafeEntry(new EntryBuilder().build());
		currentKeepassEntryEntity = new KeepassEntryEntity();
		currentKeepassEntryEntity.setUuid(currentEntry.getUuid().toString());
		PrimeFaces.current().ajax().update("addAppsForm");
		resetMultiViewTab();
		showDialog("addAppDlg");
	}

	private void resetMultiViewTab() {
		rememberPassword = false;
		uploadedFileLogo = null;
		uploadedImage = null;
		FacesContext context = FacesContext.getCurrentInstance();
		String viewId = context.getViewRoot().getViewId();
		PrimeFaces.current().multiViewState().clearAll(viewId, true);
	}

	public List<Group> getKeepassGroups() {
		// Organise groups to set recycle bin at the bottom
		if (keePassFile == null) {
			return null;
		}
		List<Group> groups = keePassFile.getGroups();
		Group recycleBin = getRycleBinGroup();
		if (recycleBin != null) {
			Collections.rotate(groups.subList(groups.indexOf(recycleBin), groups.size()), -1);
		}
		return groups;
	}

	public Group getRycleBinGroup() {
		UUID uuidBin = keePassFile.getMeta().getRecycleBinUuid();
		if (uuidBin != null) {
			return keePassFile.getGroupByUUID(uuidBin);
		}

		return null;
	}

	public List<SelectItem> getSelectedAppGroups() {
		List<SelectItem> options = new ArrayList<SelectItem>();
		if (keePassFile != null) {
			for (Group group : keePassFile.getGroups()) {
				if (group.getUuid().equals(getRecycleBinGroup().getUuid()) == false) {
					options.add(new SelectItem(group.getUuid().toString(), group.getName()));
				}
			}
		}
		return options;
	}

	public List<Entry> getGroupEntries(Group group) {
		if (searchValue == null || searchValue.isEmpty()) {
			return group.getEntries();
		}
		ArrayList<Entry> entries = new ArrayList<>();
		String searchLowerCase = searchValue.toLowerCase();
		for (Entry entry : group.getEntries()) {
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
		return entries;
	}

	public String getUploadMessage() {
		if (uploadedFile == null) {
			return "";
		}
		return MessageFormat.format(portalSessionBean.getResourceBundle().getString("message.confirmUploadFile"), uploadedFile.getFileName());
	}

	public String getCreateMessage() {
		String newFileName = this.newFileName + FILE_EXTENSION;
		if (this.newFileName == null) {
			return "";
		} else
			return MessageFormat.format(portalSessionBean.getResourceBundle().getString("message.confirmCreateFile"), newFileName);

	}

	public void onAddNewAction() {
		selectedActions.clear();
		clearActionValues();
		selectedAction = null;
		selectedActionType = ActionTypeEnum.input;
		selectedActionSourceType = AttributeTypeEnum.USER_INPUT;
		editingAction = false;
		PrimeFaces.current().ajax().update("actionForm");
		PrimeFaces.current().executeScript("PF('actionDialog').show();");
	}

	public void clearActionValues() {
		List<AppHubAction> selectedActions = new ArrayList<AppHubAction>();
		AppHubAction selectedAction = new AppHubAction();

		selectedAction.setIndex(currentKeepassEntryEntity.getApplication().getActions().size());
		selectedActions.add(selectedAction);
		setSelectedActions(selectedActions);
		PrimeFaces.current().ajax().update("editPanel");
	}

	private boolean checkForSavedMasterPassword(CloudSafeEntity cloudSafeEntity) {
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
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_PASSWORD");
					return false;
				}
			}
		}
		return false;
	}

	public void actionOpenFile(SdkCloudSafe file) {
		selectedFile = file;
		selectedPasswordSafeFile = file.getUniqueKey().getDbId();
		if (checkUserLoggedIn() == false) {
			showDialog("loginAppHubKeePass");
		} else {
			try {
				updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), masterPassword, false, null);
			} catch (Exception e) {
				logger.info("", e);
			}
			passwordSafePages = PasswordSafePages.FILE_PANEL;
		}
		PrimeFaces.current().ajax().update(FULL_PAGE_UPDATE);
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
		keePassFileTemp = null;
		cloudSafeEntity = null;
		passwordSafePages = PasswordSafePages.CHOOSE_FILE;
	}

	/**
	 * @param entity
	 */
	public void performLogin(Entry entry) {
		loadEntry(entry);
		AppHubApplication appHubApplication;
		if (currentKeepassEntryEntity.getApplicationEntity() == null) {
			appHubApplication = currentKeepassEntryEntity.getApplication();
		} else {
			appHubApplication = currentKeepassEntryEntity.getApplicationEntity().getApplication();
		}
		
		try {
			if ((entry.getUrl() == null || entry.getUrl().isEmpty() == true)) {
				JsfUtils.addWarningMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.missingUrl");
				return;
			}
			if (appHubApplication == null || appHubApplication.getActions() == null || appHubApplication.getActions().isEmpty() == true) {
				PrimeFaces.current().executeScript("var url ='" + entry.getUrl() + "' ;window.open(url, '_blank');");
				return;
			}	
			
			appHubApplication.setName(entry.getTitle());
			appHubApplication.setUrl(entry.getUrl());
			processValuesBySource(appHubApplication);
			try {
				PrimeFaces.current().executeScript("triggerAppHubLogin(" + appHubApplication.getApplicationJson() + ")");
			} catch (IOException e) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_TO_EXECUTE_PLUGIN");
				logger.warn("Browser Plugun login failed", e);
			}
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessageWithMessage());
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_PERFORM_LOGIN");
			return;
		}
	}

	public void addNewProperty() {
		customPropertyName = null;
		customPropertyValue = null;
		selectedProperty = null;
		editingProperty = false;
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').show();");
		PrimeFaces.current().ajax().update("addNewPropertyForm");
	}

	public void onShowEntry(Entry entry, Group group) {
		currentGroup = group;
		loadEntry(entry);
	}

	public void onEditEntry(Entry entry, Group group) {
		currentGroup = group;
		groupSelectItem = group.getUuid().toString();
		rememberPassword = false;
		editingEntry = true;
		resetMultiViewTab();
		loadEntry(entry);
		PrimeFaces.current().ajax().update("addAppsForm");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:fileLogoImg");
		PrimeFaces.current().ajax().update("addAuthenticatorKeyForm");
		showDialog("addAppDlg");
	}

	public void recordCustomApplication() {
		try {
			upAppHubLogic.appUrlValueValidate(appUrlValue, false);
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.invalidUrl");
			return;
		}
		AppHubApplication app = new AppHubApplication();
		app.setUrl(appUrlValue);
		currentKeepassEntryEntity.setApplication(app);
		try {
			if (advanceRecording == false) {
				PrimeFaces.current().executeScript("triggerAppHubCustomAppLogin(" + app.getApplicationJson() + ")");
			} else {
				PrimeFaces.current().executeScript("triggerAppHubAdminLogin(" + app.getApplicationJson() + ")");
			}
		} catch (Exception exp) {
			logger.warn(exp);
			JsfUtils.addErrorMessage("Cause: " + exp.getClass().getSimpleName() + " - " + exp.getMessage());
		}
	}

	public void actionExportApp() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			MyApplication myApplication = new MyApplication();
			myApplication.setName(currentEntry.getTitle());
			myApplication.setLogo(currentEntry.getEntry().getIconData());
			myApplication.setApplication(currentKeepassEntryEntity.getApplication());
			byte[] data = mapper.writeValueAsBytes(myApplication);
			JsfUtils.downloadFile("application/octet", myApplication.getName() + ".dcMyApp", data);
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Couldn't export the application, cause: " + e.toString());
			logger.warn("Could serialize application", e);
		}

	}

	public List<Property> getCustomProperties() {
		if (currentEntry == null) {
			return null;
		}
		sortProperties();
		return currentEntry.getEntry().getCustomProperties();
	}

	private void sortProperties() {
		List<Property> properties = currentEntry.getEntry().getProperties();
		Collections.sort(properties, new ComparatorCustomProperty());
	}

	public List<MyAttachment> getCurrentFiles() {
		if (currentEntry == null) {
			return null;
		}
		List<Attachment> attachments = currentEntry.getEntry().getAttachments();
		List<MyAttachment> myAttachments = new ArrayList<>(attachments.size());
		try {
			for (Attachment attachment : attachments) {
				myAttachments.add(new MyAttachment(attachment.getKey(), attachment.getData().length, attachment.getRef()));
			}
			return myAttachments;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public void actionFinishExecutePlugin() {
		try {
			int actionCount = 0;
			if (pluginResponse == null || pluginResponse.isEmpty()) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.PLUGIN_NO_RESPONSE");
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES , false);
				List<AppHubAction> importedActions = objectMapper.readValue(pluginResponse, new TypeReference<List<AppHubAction>>() {
				});
				for (AppHubAction appHubAction : importedActions) {
					appHubAction.setIndex(actionCount);
					actionCount++;
				}
				currentKeepassEntryEntity.getApplication().setActions(importedActions);
			}
		} catch (Exception e) {
			logger.info("Invalid return from plugin: " + pluginResponse, e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} finally {
			pluginResponse = null;
		}
	}

	private void saveAndUpdateFile() throws DcemException, Exception {
		byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
		if (content == null) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_WRITE_ERROR, cloudSafeEntity.getName());
		}
		loadKeepassFile(content, masterPassword);
	}

	public void setAuthenticatorKey() {
		if (authPasscodeValue == null || authPasscodeValue.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingAuthPasscode");
			return;
		}
		String passcode = generateTotpCode(authPasscodeValue, null);
		if (passcode == null) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.failedAuthPasscode");
			return;
		}
		for (Property property : currentEntry.getProperties()) {
			if (property.getKey().equals(AUTHENTICATOR_KEY)) {
				currentEntry.getProperties().remove(property);
				break;
			}
		}
		currentEntry.getProperties().add(new Property(AUTHENTICATOR_KEY, authPasscodeValue, false));
		showDialog("addAppDlg");
		hideDialog("addAuthenticatorKeyDialog");
	}

	public void onSetAuthenticator() {
		showDialog("addAuthenticatorKeyDialog");
	}

	private void updateEntryList(Group newSelectedGroup, Entry editedEntry) {
		int ind = 0;
		if (editingEntry == false || currentGroup.getUuid().equals(newSelectedGroup.getUuid())) {
			List<Entry> entriesList = newSelectedGroup.getEntries();
			boolean found = false;
			for (; ind < entriesList.size(); ind++) {
				if (entriesList.get(ind).getUuid().equals(editedEntry.getUuid())) {
					found = true;
					break;
				}
			}
			if (found == false) {
				entriesList.add(editedEntry);
			} else {
				entriesList.remove(ind);
				entriesList.add(ind, editedEntry);
			}
		} else { // Group move
			moveEntry(newSelectedGroup, editedEntry);
		}
		return;
	}

	public void actionNewFile() {
		String name = newFileName + FILE_EXTENSION;
		KeePassFile keePassFile_ = new KeePassFileBuilder(newFileName).build();
		Meta meta = new MetaBuilder(keePassFile_.getMeta()).historyMaxSize(0).historyMaxItems(0).build();
		keePassFile_ = new KeePassFileBuilder(newFileName).withMeta(meta).build();
		try {
			cloudSafeEntity = userPortalKeePassLogic.createCloudSafeEntity(name);
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile_, name, uploadPassword);
			if (content == null) {
				logger.warn("actionNewKeePass - failed to create entry");
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_TO_CREATE_ENTRY", selectedPasswordSafeFile);
				return;
			}
			password = uploadPassword;
			keePassFile = keePassFile_;
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
			currentOpenPasswordSafe = cloudSafeEntity;
			masterPassword = password;
			hideDialog("newKeePass");
			selectedPasswordSafeFile = cloudSafeEntity.getId();
			PrimeFaces.current().ajax().update(FULL_PAGE_UPDATE);
			if (passwordSafePages == PasswordSafePages.INFO) {
				passwordSafePages = PasswordSafePages.FILE_PANEL;
			}
			cloudSafeList = null;
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
		} catch (Throwable e) {
			logger.warn(e.getLocalizedMessage());
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + " - " + e.toString());
		}
	}

	public void actionUploadFile() {
		if (uploadedFile == null || uploadedFile.getContent().length == 0) {
			if (previousUploadedFile != null) {
				uploadedFile = previousUploadedFile;
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.NO_FILE_SELECTED");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
				return;
			}
		}
		if (uploadPassword == null || uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.MISSING_PASSWORD");
			PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			return;
		} else {
			uploadPassword = uploadPassword.trim();
			try (ByteArrayInputStream bais = new ByteArrayInputStream(uploadedFile.getContent());) {
				KeePassDatabase databaseTemp = KeePassDatabase.getInstance(bais);
				keePassFileTemp = databaseTemp.openDatabase(uploadPassword);
				if (fileExists(uploadedFile.getFileName()) == false) {
					uploadFile(uploadPassword, keePassFileTemp);
				} else {
					PrimeFaces.current().executeScript("PF('confirmUploadDialog').show();");
				}
				cloudSafeList = null;
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
				logger.warn("Unsopported operation Exception", e);
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.WRONG_FILE_FORMAT");
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (DcemException e) {
				logger.warn("Couldn’t upload to database", e);
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			} catch (Exception e) {
				logger.warn("Couldn’t upload to database", e);
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.SOMETHING_WENT_WRONG" + e.getLocalizedMessage());
				PrimeFaces.current().executeScript("PF('uploadDialog').show();");
			}
		}
	}

	public void actionShowInfo() {
		passwordSafePages = PasswordSafePages.INFO;
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
		if (passwordSafePages == PasswordSafePages.INFO) {
			newFileName = MY_APPLICATION_NAME;
		}

		if (newFileName == null || newFileName.isEmpty() || uploadPassword.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.EMPTY_FIELDS");
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
		if (masterPassword.isEmpty() == false) {
			try {
				cloudSafeEntity = cloudSafeLogic.getCloudSafe((int) selectedPasswordSafeFile);
				cloudSafeEntity.setWriteAccess(selectedFile.isWriteAccess());
				InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
				loadKeepassFile(inputStream, masterPassword);
				if (rememberPassword) {
					updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), masterPassword, false, null);
				} else {
					updatePsHistory(cloudSafeEntity.getId(), cloudSafeEntity.getName(), null, true, null);
				}
				passwordSafePages = PasswordSafePages.FILE_PANEL;
				PrimeFaces.current().ajax().update(FULL_PAGE_UPDATE);
				hideDialog("loginAppHubKeePass");
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
	}

	private boolean fileExists(String fileName) throws Exception {
		List<ApiFilterItem> filters = new LinkedList<>();
		filters.add(
				new ApiFilterItem("user.loginId", portalSessionBean.getUserName(), ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("name", fileName, ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		filters.add(new ApiFilterItem("recycled", "false", ApiFilterItem.SortOrderEnum.ASCENDING, ApiFilterItem.OperatorEnum.EQUALS));
		return cloudSafeLogic.queryCloudSafeFiles(filters, 0, 100).isEmpty() == false;
	}

	private void loadKeepassFile(byte[] content, String password_) throws DcemException {
		loadKeepassFile(new ByteArrayInputStream(content), password_);
	}

	private void loadKeepassFile(InputStream inputStream, String password_) throws DcemException {
		keePassFile = null;
		try {
			KeePassDatabase database = KeePassDatabase.getInstance(inputStream);
			keePassFile = database.openDatabase(password_);
		} catch (KeePassDatabaseUnreadableException exp) {
			if (exp.getCause() != null && exp.getCause() instanceof javax.crypto.BadPaddingException) {
				throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, " Please check your Master Password. ");
			}
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_LOAD_PASSWORDSAFE", selectedPasswordSafeFile);
			throw e;
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {

				logger.warn(e);
			}
		}
	}

	private void loadEntry(Entry entry) {
		currentEntry = new PasswordSafeEntry(entry);
		appNameValue = entry.getTitle();
		appUrlValue = entry.getUrl();
		appNotesValue = entry.getNotes();
		appPasswordValue = entry.getPassword();
		appUsernameValue = entry.getUsername();
		authPasscodeValue = null;
		currentKeepassEntryEntity = keepassEntryLogic.getKeepassEntry(entry.getUuid().toString());
		if (currentKeepassEntryEntity == null) {
			currentKeepassEntryEntity = new KeepassEntryEntity(entry.getUuid().toString(), entry.getTitle(), null);
		}
	}

	public void addGroup() {
		Group groups = keePassFile.getRoot().getGroups().get(0);
		for (Group group : groups.getGroups()) {
			if (group.getName().equalsIgnoreCase(sortedGroupName)) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.groupAlreadyExists");
				return;
			}
		}
		Group newGroup = new GroupBuilder(sortedGroupName).build();
		groups.getGroups().add(newGroup);
		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, masterPassword);
			updateDashboardView();
			hideDialog("sortedGroupDialog");
			sortedGroupName = null;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void onEditGroupName(Group group) {
		currentGroup = group;
		sortedGroupName = currentGroup.getName();
		PrimeFaces.current().ajax().update("editGroupNameForm");
	}

	public void editGroupName() {
		Group groups = keePassFile.getRoot().getGroups().get(0);
		for (Group group : groups.getGroups()) {
			if ((currentGroup.getUuid().equals(group.getUuid()) == false) && group.getName().equalsIgnoreCase(sortedGroupName)) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.groupAlreadyExists");
				return;
			}
		}
		try {
			Group editedGroup = new GroupBuilder(currentGroup).name(sortedGroupName).build();
			replaceGroup(currentGroup, editedGroup);

			// Collections.replaceAll(keePassFile.getRoot().getGroups(), currentGroup, editedGroup);

			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				// loadKeepassFile(byteArrayOutputStream.toByteArray(), password);
				return;
			}
			loadKeepassFile(content, masterPassword);
			hideDialog("editGroupNameDialog");
			updateDashboardView();
			currentGroup = null;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}

	}

	private void replaceGroup(Group previosGroup, Group newGrouop) {
		int i;
		Group parentGroup = keePassFile.getRoot().getGroups().get(0);
		if (previosGroup.getUuid().equals(parentGroup.getUuid())) { // check for root roup
			keePassFile.getRoot().getGroups().remove(0);
			keePassFile.getRoot().getGroups().add(newGrouop);
			return;
		}
		for (i = 0; i < parentGroup.getGroups().size(); i++) {
			if (parentGroup.getGroups().get(i).getUuid().equals(previosGroup.getUuid()) == true) {
				break;
			}
		}
		if (i < parentGroup.getGroups().size()) {
			parentGroup.getGroups().remove(i);
			parentGroup.getGroups().add(i, newGrouop);
		}
		return;
	}

	public void showPluginFirefoxUnavailableAlert() {
		showDialog("pluginFirefoxUnavailableDlg");
	}

	public void showPluginSafariUnavailableAlert() {
		showDialog("pluginSafariUnavailableDlg");
	}

	public void showPluginChromeUnavailableAlert() {
		showDialog("pluginChromeUnavailableDlg");
	}

	public void showPluginUnavailableAlert() {
		showDialog("pluginUnavailableDlg");
	}

	public void updatePsHistory(int id, String fileName, String password, boolean remove, String groupName) throws Exception {
		psHistory = userPortalKeePassLogic.updatePsHistory(id, fileName, password, remove, recentFiles, groupName);
		setRecentFiles(recentFiles);
		PrimeFaces.current().ajax().update("pmForm:psHistory");
		PrimeFaces.current().executeScript("localStorage.setItem('psHistory." + portalSessionBean.getDcemUser().getLoginId().replace("\\", "\\\\") + "' , '"
				+ userPortalKeePassLogic.escapeJson(psHistory) + "')");
	}

	public void actionCloseAddDialog() {
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("addAppDlg");
	}

	/**
	 * 
	 */
	public void actionAddEditEntry() {
		if (appNameValue == null || appNameValue.trim().isEmpty() == true) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingName");
			return;
		}
		appNameValue = appNameValue.trim();
		try {
			if (appUrlValue != null) {
				appUrlValue = appUrlValue.trim();
			}
			if (appUrlValue != null && appUrlValue.isEmpty() == false) {
				if (appUrlValue.toLowerCase().startsWith(HTTP) == false) {
					appUrlValue = HTTPS + appUrlValue;
				}
			}
			Group newSelectedGroup;
			if (groupSelectItem == null) {
				newSelectedGroup = keePassFile.getRoot().getGroups().get(0);
			} else {
				newSelectedGroup = keePassFile.getGroupByUUID(UUID.fromString(groupSelectItem));
			}
			for (Entry entry : newSelectedGroup.getEntries()) {
				if (entry.getTitle().equalsIgnoreCase(appNameValue) && entry.getUuid().equals(currentEntry.getUuid()) == false) {
					throw new DcemException(DcemErrorCodes.APP_NAME_EXISTS, appNameValue);
				}
			}
			currentEntry.setTitle(appNameValue);
			currentEntry.setUrl(appUrlValue);
			currentEntry.setUsername(appUsernameValue);
			currentEntry.setPassword(appPasswordValue);
			currentEntry.setNotes(appNotesValue);
			Entry addingEntry;
			if (uploadedImage != null) {
			//	uploadedImage.length;
				uploadedImage = DcemUtils.resizeImage(uploadedImage);
				UUID iconUuid = addCustomIconToKeepassFile(uploadedImage);
				addingEntry = new EntryBuilder(currentEntry.getEntry()).customIconUuid(iconUuid).build();
			} else {
				addingEntry = new EntryBuilder(currentEntry.getEntry()).build();
			}
			List<Attachment> attachments = addingEntry.getAttachments();
			int id = getNextBinaryId();
			for (int i = 0; i < attachments.size(); i++) {
				if (attachments.get(i).getRef() == -1) {
					attachments.set(i, new Attachment(attachments.get(i).getKey(), id, attachments.get(i).getData()));
					Binary binary = new BinaryBuilder().data(attachments.get(i).getData()).id(id).isCompressed(false).build();
					List<Binary> allBinary = keePassFile.getMeta().getBinaries().getBinaries();
					allBinary.add(binary);
					id++;
				}
			}
			boolean found;
			for (int i = 0; i < addingEntry.getAttachments().size(); i++) {
				found = false;
				for (int j = 0; j < attachments.size(); j++) {
					if (addingEntry.getAttachments().get(i).getRef() == (attachments.get(j).getRef())) {
						found = true;
						break;
					}
				}
				if (found == false) {
					List<Binary> allBinary = keePassFile.getMeta().getBinaries().getBinaries();
					int foundInd = -1;
					for (int ind = 0; ind < allBinary.size(); ind++) {
						if (allBinary.get(ind).getId() == currentEntry.getAttachments().get(i).getRef()) {
							foundInd = ind;
							break;
						}
					}
					if (foundInd != -1) {
						allBinary.remove(foundInd);
					}
				}
			}
			updateEntryList(newSelectedGroup, addingEntry);
			currentGroup = newSelectedGroup;
			currentKeepassEntryEntity.setName(appNameValue);
			keepassEntryLogic.updateEntry(currentKeepassEntryEntity);
			saveAndUpdateFile();
			PrimeFaces.current().ajax().update("applicationHubForm");
			hideDialog("addAppDlg");
			currentEntry = null;
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.APP_NAME_EXISTS) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.APP_NAME_EXISTS");
				return;
			}
			if (exp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				logger.info("customAppAlreadyExists", exp);
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.APP_NAME_EXISTS");
				return;
			}
			if (exp.getErrorCode() == DcemErrorCodes.IMAGE_TOO_BIG) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.imgExceedsSize");
				return;
			}
			logger.info(exp.toString(), exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
		} catch (Exception e) {
			logger.info("Coundn't add Application", e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_ADD_APPLICATION");
		}
	}

	private int getNextBinaryId() {
		int nextBinaryId = 0;
		if (keePassFile.getMeta().getBinaries() == null) {
			List<Binary> binaryList = new ArrayList<>();
			Binaries binaries = new BinariesBuilder().binaries(binaryList).build();
			Meta meta = new MetaBuilder(keePassFile.getMeta()).binaries(binaries).historyMaxSize(0).historyMaxItems(0).build();
			keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
			return nextBinaryId;
		} else {
			List<Binary> allBinary = keePassFile.getMeta().getBinaries().getBinaries();
			for (int i = 0; i < allBinary.size(); i++) {
				if (allBinary.get(i).getId() > nextBinaryId) {
					nextBinaryId = allBinary.get(i).getId();
				}
			}
			nextBinaryId++;
		}
		return nextBinaryId;
	}

	private UUID addCustomIconToKeepassFile(byte[] logo) {
		Meta meta;
		UUID iconUuid = UUID.randomUUID();
		CustomIcons customIcons;
		CustomIcon customIcon = new CustomIconBuilder().uuid(iconUuid).data(logo).build();
		if (keePassFile.getMeta().getCustomIcons() != null) {
			customIcons = new CustomIconsBuilder(keePassFile.getMeta().getCustomIcons()).addIcon(customIcon).build();
		} else {
			customIcons = new CustomIconsBuilder().addIcon(customIcon).build();
		}
		meta = new MetaBuilder(keePassFile.getMeta()).customIcons(customIcons).binaries(keePassFile.getMeta().getBinaries()).build();
		keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
		return iconUuid;
	}

	public void actionDeleteEntry(Entry entry) {
		try {
			if (isInRecyclingBin(entry)) {
				Group recycleBinGroup = getRecycleBinGroup();
				removeEntryFromGroup(recycleBinGroup, entry);
				keepassEntryLogic.deleteKeepassEntry(entry.getUuid().toString());
			} else {
				moveEntry(getRecycleBinGroup(), entry);
			}
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, masterPassword);
		} catch (Exception e) {
			logger.info("Couldn't delete Entry: " + entry.getTitle(), e);
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.FAILED_CLOSE_APPLICATION");
		}
	}

	public String getActionValueSource(AppHubAction appHubAction) {
		if (appHubAction == null || appHubAction.getValueSourceType() == null) {
			return null;
		}
		for (AttributeTypeEnum attributeTypeEnum : AttributeTypeEnum.values()) {
			if (attributeTypeEnum.name().equals(appHubAction.getValueSourceType())) {
				return attributeTypeEnum.getDisplayName();
			}
		}
		return null;
	}

	private void moveEntry(Group moveToGroup, Entry moveFromEntry) {
		Group group = getGroup(moveFromEntry);
		removeEntryFromGroup(group, moveFromEntry);
		moveToGroup.getEntries().add(moveFromEntry);
	}

	private void removeEntryFromGroup(Group group, Entry entry) {
		int foundIndex = -1;
		for (int i = 0; i < group.getEntries().size(); i++) {
			if (group.getEntries().get(i).getUuid().equals(entry.getUuid())) {
				foundIndex = i;
				break;
			}
		}
		if (foundIndex != -1) {
			group.getEntries().remove(foundIndex);
		}
	}

	private boolean isInRecyclingBin(Entry keePassEntry) {
		Group recycleBinGroup = getRecycleBinGroup();
		for (Entry entry : recycleBinGroup.getEntries()) {
			if (entry.getUuid().equals(keePassEntry.getUuid())) {
				return true;
			}
		}
		return false;
	}

	public Group getRecycleBinGroup() {
		UUID uuidBin = keePassFile.getMeta().getRecycleBinUuid();
		if (uuidBin == null || keePassFile.getGroupByUUID(uuidBin) == null) {
			Group recicleBinGroup = new GroupBuilder().name(portalSessionBean.getResourceBundle().getString("title.RecycleBin")).build();
			Meta meta = new MetaBuilder(keePassFile.getMeta()).recycleBinEnabled(true).recycleBinUuid(recicleBinGroup.getUuid()).historyMaxSize(0)
					.historyMaxItems(0).build();
			List<Group> groups = keePassFile.getRoot().getGroups().get(0).getGroups();
			groups.add(groups.size(), recicleBinGroup);
			keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
			return recicleBinGroup;
		} else {
			return keePassFile.getGroupByUUID(uuidBin);
		}
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

	public void uploadFileLogoListener(FileUploadEvent event) {
		try {
			uploadedImage =  DcemUtils.resizeImage(event.getFile().getContent());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		return;
	}

	public StreamedContent getEntryImage(Entry entry) {
		if (entry.getCustomIconUuid() == null || entry.getIconData() == null) {
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> this.getClass().getResourceAsStream(DcemConstants.DEFAULT_APP_ICON))
					.build();
		}
		return DefaultStreamedContent.builder().contentType("image/png").stream(() -> new ByteArrayInputStream(entry.getIconData())).build();
	}

	public StreamedContent getFileLogoImage() {
		if (uploadedImage != null) {
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> new ByteArrayInputStream(uploadedImage)).build();
		}
		if (currentEntry == null) {
			return null;
		}
		if (currentEntry.getIconData() == null || currentEntry.getCustomIconUuid() == null) {
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> this.getClass().getResourceAsStream(DcemConstants.DEFAULT_APP_ICON))
					.build();
		}
		return DefaultStreamedContent.builder().contentType("image/png").stream(() -> new ByteArrayInputStream(currentEntry.getIconData())).build();
	}

	public List<SelectItem> getActionTypes() {
		if (actionTypes == null) {
			actionTypes = new LinkedList<>();
			for (ActionTypeEnum actionTypeEnum : ActionTypeEnum.values()) {
				actionTypes.add(new SelectItem(actionTypeEnum.name(), actionTypeEnum.getDisplayName()));
			}
		}
		return actionTypes;
	}

	public boolean isActionWithSelector() {
		if (selectedActionType == null) {
			return false;
		}
		switch (selectedActionType) {
		case input:
		case button:
			return true;
		default:
			break;
		}
		return false;
	}

	public boolean isActionWithSourceValue() {
		boolean returnValue = false;
		if (selectedActionType == null) {
			return false;
		}
		if (selectedActionType == ActionTypeEnum.input) {
			returnValue = true;
		}
		return returnValue;
	}

	public void listenerChangeActionSourceType() {
	}

	public boolean isActionWithDelay() {
		return (selectedActionType != null && selectedActionType == ActionTypeEnum.delay);
	}

	public boolean isActionWithParameter() {
		boolean returnValue = false;
		if (selectedActions == null || selectedActions.size() != 1) {
			return returnValue;
		} else {
			selectedAction = selectedActions.get(0);
		}
		if (selectedActionType != null) {
			if (selectedActionType == ActionTypeEnum.input) {
				if (selectedActionSourceType == null) {
					selectedActionSourceType = AttributeTypeEnum.USER_INPUT;
				}
				return selectedActionSourceType.isValueRequired();
			}
		}
		return returnValue;
	}

	public void actionCustomProperty() {
		if (editingProperty == false) {
			List<Property> properties = currentEntry.getEntry().getProperties();
			for (Property property : properties) {
				if (property.getKey().equalsIgnoreCase(getCustomPropertyName())) {
					JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassPropertyAlreadyExist"));
					return;
				}
			}
			properties.add(new Property(customPropertyName, customPropertyValue, false));
		} else {
			List<Property> properties = currentEntry.getEntry().getProperties();
			int propertyInedx = properties.indexOf(selectedProperty);
			Property prob = new Property(customPropertyName, customPropertyValue, false);
			properties.set(propertyInedx, prob);
		}
		PrimeFaces.current().ajax().update("addAppsForm:tabView:customPropertiesTable");
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').hide();");
	}

	public void editProperty() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		editingProperty = true;
		setCustomPropertyName(selectedProperty.getKey());
		setCustomPropertyValue(selectedProperty.getValue());
		PrimeFaces.current().ajax().update("addNewPropertyForm");
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').show();");
	}

	public void deleteProperty() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		List<Property> properties = currentEntry.getEntry().getProperties();
		properties.remove(selectedProperty);
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').hide();");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:customPropertiesTable");
	}

	public void actionAddEditAction() {
		AppHubAction editedAction = selectedActions.get(0);
		switch (selectedActionType) {
		case delay:
			if (editedAction.getValueParameter() == null || isNumeric(editedAction.getValueParameter()) == false) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionDelayValue");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			if (isNumeric(editedAction.getValueParameter())) {
				editedAction.setValueParameter(this.selectedAction.getValueParameter());
			} else {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.numericValue");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
			}
			editedAction.setValueSourceType(null);
			editedAction.setSelector(null);
			break;
		case button:
			if (editedAction.getSelector() == null || editedAction.getSelector().isEmpty()) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionSelector");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			editedAction.setValueSourceType(null);
			editedAction.setValueParameter(null);
			break;
		case input:
			if (editedAction.getSelector() == null || editedAction.getSelector().isEmpty()) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionSelector");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			if (selectedActionSourceType != null) {
				editedAction.setValueSourceType(selectedActionSourceType.name());
			}
			if (isActionWithParameter() == true) {
				if (editedAction.getValueParameter() == null || editedAction.getValueParameter().isEmpty() == true) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionInputName");
					PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
					return;
				}
			} else {
				editedAction.setValueParameter(null);
			}
		}
		editedAction.setType(selectedActionType.name());
		if (editingAction == false) {
			currentKeepassEntryEntity.getApplication().getActions().add(editedAction);
		} 
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("actionDialog");
	}

	public void handleFileUpload(FileUploadEvent event) {
		List<Attachment> attachments = currentEntry.getEntry().getAttachments();
		attachments.add(new Attachment(event.getFile().getFileName(), -1, event.getFile().getContent()));
	}

	public void deleteAttachment() {
		if (selectedAttachmentFile == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectAttachment"));
			return;
		}
		List<Attachment> attachments = currentEntry.getAttachments();
		for (int i = 0; i < attachments.size(); i++) {
			if (attachments.get(i).getKey().equals(selectedAttachmentFile.getKey())) {
				attachments.remove(i);
				break;
			}
		}
		PrimeFaces.current().ajax().update("addAppsForm:tabView:attachmentsTable");
	}

	public void actionDownloadAttachment() {
		if (selectedAttachmentFile == null) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectAttachment"));
			return;
		}
		List<Attachment> attachments = currentEntry.getEntry().getAttachments();
		Attachment attachmentFound = null;
		for (Attachment attachment : attachments) {
			if (attachment.getRef() == selectedAttachmentFile.getRef()) {
				attachmentFound = attachment;
			}
		}
		try {
			JsfUtils.downloadFile("", attachmentFound.getKey(), attachmentFound.getData());
		} catch (IOException e) {
			JsfUtils.addErrorMessage(e.toString());
		}

	}

	private static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			Integer.parseInt(strNum.trim());
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public void deleteGroup(Group groupToDelete) {
		Group parentGroup = keePassFile.getRoot().getGroups().get(0);
		if (groupToDelete.equals(parentGroup)) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHub.error.cannotDeleteParentGroup");
			return;
		}
		// Put all entries in recycle bin before deleting group
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll(groupToDelete.getEntries());
		for (Entry selectItem : entries) {
			if (isInRecyclingBin(selectItem)) {
				continue;
			} else {
				moveEntry(getRecycleBinGroup(), selectItem);
			}
		}
		parentGroup.getGroups().remove(groupToDelete);
		try {
			byte[] content = userPortalKeePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			loadKeepassFile(content, masterPassword);
			PrimeFaces.current().ajax().update("applicationHubForm");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public List<SelectItem> getActionSourceTypes() {
		if (actionSourceTypes == null) {
			actionSourceTypes = new LinkedList<>();
			for (AttributeTypeEnum attributeTypeEnum : AttributeTypeEnum.values()) {
				if (attributeTypeEnum == AttributeTypeEnum.GROUPS) {
					break;
				}
				actionSourceTypes.add(new SelectItem(attributeTypeEnum.name(), attributeTypeEnum.getDisplayName()));
			}
		}
		return actionSourceTypes;
	}

	public void deleteAction() {
		if (selectedActions == null || selectedActions.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.selectAction");
			return;
		}
		for (AppHubAction action : selectedActions) {
			currentKeepassEntryEntity.getApplication().getActions().remove(action);
		}
		PrimeFaces.current().executeScript("PF('actionDialog').hide();");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
	}

	public void onEditAction() {
		if (selectedActions == null || selectedActions.isEmpty() == true) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.selectAction");
			return;
		}
		if (selectedActions.size() != 1) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.selectOnlyOneAction");
			return;
		}
		editingAction = true;
		selectedAction = selectedActions.get(0);
		selectedActionSourceType = null;
		ActionTypeEnum typeEnum = ActionTypeEnum.valueOf(selectedAction.getType());
		switch (typeEnum) {
		case input:
			selectedActionType = ActionTypeEnum.input;
			selectedActionSourceType = AttributeTypeEnum.valueOf(selectedAction.getValueSourceType());
			break;
		case button:
			selectedActionType = ActionTypeEnum.button;
			break;
		case delay:
			selectedActionType = ActionTypeEnum.delay;
			break;
		default:
			break;
		}
		PrimeFaces.current().ajax().update("actionForm");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		showDialog("actionDialog");
	}

	private List<ClaimAttribute> getClaimAttributes(AppHubApplication appHubApplication) {
		List<ClaimAttribute> result = new ArrayList<ClaimAttribute>();
		for (AppHubAction appHubAction : appHubApplication.getActions()) {
			if (("input").equals(appHubAction.getType())) {
				AttributeTypeEnum valueSource = AttributeTypeEnum.valueOf(appHubAction.getValueSourceType());
				result.add(new ClaimAttribute(appHubAction.getValueParameter(), valueSource, null));
			}
		}
		return result;
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
			cloudSafeSharedList = cloudSafeLogic.getCloudSafeSharedFileList(portalSessionBean.getDcemUser().getId(), "%" + AsConstants.EXTENSION_PASSWORD_SAFE,
					0, CloudSafeOwner.USER, AsConstants.LIB_VERION_2);
			return cloudSafeSharedList;
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
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
			return cloudSafeLogic.getCloudSafeFileList(portalSessionBean.getDcemUser().getId(), "%" + AsConstants.EXTENSION_PASSWORD_SAFE, 0,
					CloudSafeOwner.USER, withSharedFiles, AsConstants.LIB_VERION_2);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(e));
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

	public PasswordSafeEntry getCurrentEntry() {
		return currentEntry;
	}

	public boolean isWriteAccess() {
		if (cloudSafeEntity != null) {
			return cloudSafeEntity.isWriteAccess();
		} else {
			return true;
		}
	}

	public void actionSetPredefinedApp() {
		try {
			if (selectedAddApp != null) {
				ApplicationHubEntity applicationHubEntity = upAppHubLogic.getApplicationById(Integer.parseInt(selectedAddApp));
				appNameValue = applicationHubEntity.getName();
				appUrlValue = applicationHubEntity.getApplication().getUrl();
				currentKeepassEntryEntity = new KeepassEntryEntity(currentEntry.getUuid().toString(), applicationHubEntity);
				uploadedImage = applicationHubEntity.getLogo();
				PrimeFaces.current().ajax().update("addAppsForm");
				hideDialog("setPredefinedAppDialog");
				showDialog("addAppDlg");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public boolean isNoPredefinedApp() {
		if (currentKeepassEntryEntity == null) {
			return false;
		}
		return currentKeepassEntryEntity.getApplicationEntity() == null;
	}

	public void actionRemovePredefinedApp() {
		currentKeepassEntryEntity.setApplicationEntity(null);
		PrimeFaces.current().ajax().update("addAppsForm");
		hideDialog("setPredefinedAppDialog");
		showDialog("addAppDlg");
	}

	public String getPredefinedAppName() {
		if (currentKeepassEntryEntity == null) {
			return null;
		}
		if (currentKeepassEntryEntity.getApplicationEntity() == null) {
			return null;
		}
		return currentKeepassEntryEntity.getApplicationEntity().getName();
	}

	public void upAction() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		List<Property> properties = currentEntry.getEntry().getProperties();
		int propertyInedx = properties.indexOf(selectedProperty);
		if (propertyInedx == 0) {
			return;
		}
		Collections.swap(properties, propertyInedx, propertyInedx - 1);
	}

	public void downAction() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}

		List<Property> properties = currentEntry.getEntry().getProperties();
		int propertyInedx = properties.indexOf(selectedProperty);
		if (properties.size() == propertyInedx + 1) {
			return;
		}
		Collections.swap(properties, propertyInedx, propertyInedx + 1);
	}

	private void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}

	void hideDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').hide();");
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

	public String getAuthPasscodeValue() {
		return authPasscodeValue;
	}

	public void setAuthPasscodeValue(String authPasscodeValue) {
		this.authPasscodeValue = authPasscodeValue;
	}

	@Override
	public String getPath() {
		return "passwordSafeView.xhtml";
	}

	public boolean isRememberPassword() {
		return rememberPassword;
	}

	public void setRememberPassword(boolean rememberPassword) {
		this.rememberPassword = rememberPassword;
	}

	public String getPsHistory() {
		return psHistory;
	}

	public String getMasterPassword() {
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		if (searchValue != null) {
			this.searchValue = searchValue.trim();
		} else {
			this.searchValue = null;
		}
	}

	public String getSelectedAddApp() {
		return selectedAddApp;
	}

	public void setSelectedAddApp(String selectedAddAppId) {
		this.selectedAddApp = selectedAddAppId;
	}

	public List<AppHubAction> getCurrentAppActions() {
		if (currentKeepassEntryEntity == null) {
			return null;
		}
		if (currentKeepassEntryEntity.getApplication() == null) {
			currentKeepassEntryEntity.setApplication(new AppHubApplication());
		}
		if (currentKeepassEntryEntity.getApplicationEntity() != null) {
			return currentKeepassEntryEntity.getApplicationEntity().getApplication().actions;
		}
		return currentKeepassEntryEntity.getApplication().getActions();
	}

	public List<PasswordSafeRecentFile> getRecentFiles() {
		return recentFiles;
	}

	public void setRecentFiles(List<PasswordSafeRecentFile> recentFiles) {
		this.recentFiles = recentFiles;
	}

	public String getUserId() {
		return portalSessionBean.getDcemUser().getLoginId();
	}

	public String getAppNameValue() {
		return appNameValue;
	}

	public void setAppNameValue(String appNameValue) {
		this.appNameValue = appNameValue;
	}

	public String getAppUrlValue() {
		return appUrlValue;
	}

	public void setAppUrlValue(String appUrlValue) {
		this.appUrlValue = appUrlValue;
	}

	public boolean isEditingApplication() {
		return editingEntry;
	}

	public void setEditingApplication(boolean editingApplication) {
		this.editingEntry = editingApplication;
	}

	public String getPluginResponse() {
		return pluginResponse;
	}

	public void setPluginResponse(String pluginResponse) {
		this.pluginResponse = pluginResponse;
	}

	public AttributeTypeEnum getSelectedActionSourceType() {
		return selectedActionSourceType;
	}

	public void setSelectedActionSourceType(AttributeTypeEnum selectedActionSourceType) {
		this.selectedActionSourceType = selectedActionSourceType;
	}

	public ActionTypeEnum getSelectedActionType() {
		return selectedActionType;
	}

	public void setSelectedActionType(ActionTypeEnum selectedActionType) {
		this.selectedActionType = selectedActionType;
	}

	public List<AppHubAction> getSelectedActions() {
		return selectedActions;
	}

	public void setSelectedActions(List<AppHubAction> selectedActions) {
		this.selectedActions = selectedActions;
	}

	public AppHubAction getSelectedAction() {
		if (selectedActions != null && selectedActions.isEmpty() == false) {
			return selectedActions.get(0);
		}
		return null;
	}

	public String getCurrentOpenPasswordSafeName() {
		if (currentOpenPasswordSafe != null && passwordSafePages == PasswordSafePages.FILE_PANEL) {
			return Files.getNameWithoutExtension(currentOpenPasswordSafe.getName());
		}
		return null;
	}

	public void setSelectedAction(AppHubAction selectedAction) {
		this.selectedAction = selectedAction;
	}

	public boolean isEditingAction() {
		return editingAction;
	}

	public void setEditingAction(boolean editingAction) {
		this.editingAction = editingAction;
	}

	public String getAppUsernameValue() {
		return appUsernameValue;
	}

	public void setAppUsernameValue(String appUsernameValue) {
		this.appUsernameValue = appUsernameValue;
	}

	public String getAppPasswordValue() {
		return appPasswordValue;
	}

	public String getAppPasswordValueConfirm() {
		return appPasswordValue;
	}

	public void setAppPasswordValue(String appPasswordValue) {
		this.appPasswordValue = appPasswordValue;
	}

	public String getAppNotesValue() {
		return appNotesValue;
	}

	public void setAppNotesValue(String appNotesValue) {
		this.appNotesValue = appNotesValue;
	}

	public String getSortedGroupName() {
		return sortedGroupName;
	}

	public void setSortedGroupName(String sortedGroupName) {
		this.sortedGroupName = sortedGroupName;
	}

	public String getGroupSelectItem() {
		return groupSelectItem;
	}

	public void setGroupSelectItem(String groupSelectItem) {
		this.groupSelectItem = groupSelectItem;
	}

	public Property getSelectedProperty() {
		return selectedProperty;
	}

	public void setSelectedProperty(Property selectedProperty) {
		this.selectedProperty = selectedProperty;
	}

	public List<DcemUploadFile> getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(List<DcemUploadFile> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public MyAttachment getSelectedAttachmentFile() {
		return selectedAttachmentFile;
	}

	public void setSelectedAttachmentFile(MyAttachment selectedAttachmentFile) {
		this.selectedAttachmentFile = selectedAttachmentFile;
	}

	public boolean isEditingProperty() {
		return editingProperty;
	}

	public void setEditingProperty(boolean editingProperty) {
		this.editingProperty = editingProperty;
	}

	public String getCustomPropertyName() {
		return customPropertyName;
	}

	public void setCustomPropertyName(String customPropertyName) {
		this.customPropertyName = customPropertyName;
	}

	public String getCustomPropertyValue() {
		return customPropertyValue;
	}

	public void setCustomPropertyValue(String customPropertyValue) {
		this.customPropertyValue = customPropertyValue;
	}

	public static void setActionSourceTypes(List<SelectItem> actionSourceTypes) {
		PasswordSafeView.actionSourceTypes = actionSourceTypes;
	}

	public List<ApplicationHubEntity> getApplications() {
		return applications;
	}

	public void setApplications(List<ApplicationHubEntity> applications) {
		this.applications = applications;
	}

	public String getUsernameValue() {
		return usernameValue;
	}

	public void setUsernameValue(String usernameValue) {
		this.usernameValue = usernameValue;
	}

	public String getPasswordValue() {
		return passwordValue;
	}

	public void setPasswordValue(String passwordValue) {
		this.passwordValue = passwordValue;
	}

	public String getAuthCodeValue() {
		return authCodeValue;
	}

	public void setAuthCodeValue(String authCodeValue) {
		this.authCodeValue = authCodeValue;
	}

	public long getSelectedPasswordSafeFile() {
		return selectedPasswordSafeFile;
	}

	public void setSelectedPasswordSafeFile(long selectedPasswordSafeFile) {
		this.selectedPasswordSafeFile = selectedPasswordSafeFile;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public UploadedFile getPreviousUploadedFile() {
		return previousUploadedFile;
	}

	public void setPreviousUploadedFile(UploadedFile previousUploadedFile) {
		this.previousUploadedFile = previousUploadedFile;
	}

	public String getNewFileName() {
		return newFileName;
	}

	public void setNewFileName(String newFileName) {
		this.newFileName = newFileName;
	}

	public String getUploadPassword() {
		return uploadPassword;
	}

	public void setUploadPassword(String uploadPassword) {
		this.uploadPassword = uploadPassword;
	}

	public CloudSafeEntity getCurrentOpenPasswordSafe() {
		return currentOpenPasswordSafe;
	}

	public void setCurrentOpenPasswordSafe(CloudSafeEntity currentOpenPasswordSafe) {
		this.currentOpenPasswordSafe = currentOpenPasswordSafe;
	}

	public String getPage() {
		return passwordSafePages.getXhtmlPage();
	}

	public String getPageName() {
		return passwordSafePages.name();
	}

	public UploadedFile getUploadedFileLogo() {
		return uploadedFileLogo;
	}

	public void setUploadedFileLogo(UploadedFile uploadedFileLogo) {
		this.uploadedFileLogo = uploadedFileLogo;
	}

	public boolean isAdvanceRecording() {
		return advanceRecording;
	}

	public void setAdvanceRecording(boolean advanceRecording) {
		this.advanceRecording = advanceRecording;
	}

	public SdkCloudSafe getSelectedFile() {
		return selectedFile;
	}

	public void setSelectedFile(SdkCloudSafe selectedFile) {
		this.selectedFile = selectedFile;
	}

}