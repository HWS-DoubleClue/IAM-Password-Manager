package com.doubleclue.dcem.ps.gui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
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
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.ps.entities.ApplicationHubEntity;
import com.doubleclue.dcem.ps.entities.KeepassEntryEntity;
import com.doubleclue.dcem.ps.logic.ActionTypeEnum;
import com.doubleclue.dcem.ps.logic.AppHubAction;
import com.doubleclue.dcem.ps.logic.AppHubApplication;
import com.doubleclue.dcem.ps.logic.ApplicationSelectItem;
import com.doubleclue.dcem.ps.logic.KeePassLogic;
import com.doubleclue.dcem.ps.logic.KeepassEntryLogic;
import com.doubleclue.dcem.ps.logic.MyApplication;
import com.doubleclue.dcem.ps.logic.PasswordSafeEntry;
import com.doubleclue.dcem.ps.logic.PasswordSafeModule;
import com.doubleclue.dcem.ps.logic.PmAppHubLogic;
import com.doubleclue.dcem.ps.subjects.PsFileSubject;
import com.doubleclue.utils.TimeBasedPasscodeGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.BaseEncoding;
import com.google.common.io.Files;

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

@Named("psFileView")
@SessionScoped
public class PsFileView extends DcemView {

	private static final long serialVersionUID = 1L;

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
	KeePassLogic keePassLogic;

	@Inject
	PsFileSubject psFileSubject;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	PsChooseFileView psChooseFileView;

	public static final String FILE_EXTENSION = ".kdbx";
	private static String AUTHENTICATOR_KEY = "Authenticator Key";
	private final static String HTTPS = "https://";
	private final static String HTTP = "http";

	private List<ApplicationHubEntity> applications = null;
	private CloudSafeEntity cloudSafeEntity;
	protected KeePassFile keePassFile = null;
	private String masterPassword;

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

	boolean advanceRecording;

	ResourceBundle resourceBundle;

	private Logger logger = LogManager.getLogger(PsFileView.class);

	@PostConstruct
	public void init() {
		this.setSubject(psFileSubject);
		resourceBundle = ResourceBundle.getBundle(PasswordSafeModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		actionSourceTypes = null;
		actionTypes = null;
	}

	@Override
	public void reload() {
		applications = null;
	}
	
	public void appUrlValueValidate() {
		try {
			if (appUrlValue.toLowerCase().startsWith(HTTP) == false) {
				appUrlValue = HTTPS + appUrlValue;
			}
			uploadedImage = upAppHubLogic.appUrlValueValidate(appUrlValue, true);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
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
		PrimeFaces.current().ajax().update("psFileForm:gridPanel");
		return result;
	}

	public void onAddGroup() {
		sortedGroupName = null;
		PrimeFaces.current().ajax().update("sortedGroupForm");
		showDialog("sortedGroupDialog");
	}

	private void updateDashboardView() {
		PrimeFaces.current().ajax().update("psFileForm:gridPanel");
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
							JsfUtils.addWarningMessage(PasswordSafeModule.RESOURCE_NAME, "appHub.warn.UPDATED_PROPERTIES", selectedPasswordSafeFile);
							break;
						}
						String passcode = generateTotpCode(key, null);
						if (passcode == null) {
							throw new DcemException(DcemErrorCodes.GENERATE_OTP_FAILED, appHubAction.getValueParameter());
						}
						appHubAction.setOutputValue(passcode);
					} else {
						throw new DcemException(DcemErrorCodes.NO_OTP_KEY_FOUND, null);
						// JsfUtils.addWarningMessage(resourceBundle, "appHub.warn.UPDATED_PROPERTIES", selectedPasswordSafeFile);
					}
					break;
				case STATIC_TEXT:
					appHubAction.setOutputValue(appHubAction.getValueParameter());
					break;
				default:
					DcemUser user = operatorSessionBean.getDcemUser();
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
			JsfUtils.addErrorMessage(resourceBundle, "appHub.error.FAILED_OTP_GENERATE");
			return null;
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
		// if (isKeepPassExists() == false) {
		// showDialog("newKeePass");
		// return;
		// }
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
	
	public void preRenderView() {
		if (keePassFile != null) {
			return;
		}
		List<PasswordSafeRecentFile> recentFiles = psChooseFileView.getRecentFiles();
		if (recentFiles.size() > 0) {
			PasswordSafeRecentFile passwordSafeRecentFile = recentFiles.get(0);
			int cloudSafeId = passwordSafeRecentFile.getId();
			cloudSafeEntity = cloudSafeLogic.getCloudSafe(cloudSafeId);
			if (cloudSafeEntity != null) {
				if (psChooseFileView.checkForSavedMasterPassword(cloudSafeEntity) == true) {
					masterPassword = psChooseFileView.getMasterPassword();
				}	
				InputStream inputStream;
				try {
					inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null);
					keePassFile = psChooseFileView.loadKeepassFile(inputStream, masterPassword);
					return;
				} catch (DcemException e) {
					logger.warn(e);
					viewNavigator
							.setActiveView(PasswordSafeModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + psChooseFileView.getSubject().getViewName());
					return;
				}
			}
		}
		viewNavigator.setActiveView(PasswordSafeModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + psChooseFileView.getSubject().getViewName());
		return;
	}

	public List<Group> getKeepassGroups() {
		// Organise groups to set recycle bin at the bottom
		if (keePassFile == null) {
			return new ArrayList<Group>(1);
		}
		Group recycleBin = getRycleBinGroup();
		List<Group> keePassGroups = keePassFile.getGroups();
		List<Group> groups = new ArrayList<Group>(keePassFile.getGroups().size());
		List<Group> emptyGroups = new ArrayList<Group>(keePassFile.getGroups().size());
		for (Group group : keePassGroups) {
			if (recycleBin != null && group.getUuid().equals(recycleBin.getUuid()) == true) {
				continue;
			}
			if (getGroupEntries(group).size() > 0) {
				groups.add(group);
			} else {
				emptyGroups.add(group);
			}
		}
		if (searchValue == null || searchValue.isEmpty()) {
			groups.addAll(emptyGroups);
			if (recycleBin != null) {
				groups.add(recycleBin);
			}
		} else {
			if (recycleBin != null && getGroupEntries(recycleBin).size() > 0) {
				groups.add(recycleBin);
			}
		}
		return groups;
	}

	public Group getRycleBinGroup() {
		if (keePassFile == null ) {
			return null;
		}
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
		return MessageFormat.format(resourceBundle.getString("message.confirmUploadFile"), uploadedFile.getFileName());
	}

	public String getCreateMessage() {
		String newFileName = this.newFileName + FILE_EXTENSION;
		if (this.newFileName == null) {
			return "";
		} else
			return MessageFormat.format(resourceBundle.getString("message.confirmCreateFile"), newFileName);

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
		keePassFile = null;
		masterPassword = null;
		viewNavigator.setActiveView(PasswordSafeModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + psChooseFileView.getSubject().getViewName());
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
				JsfUtils.addErrorMessage(resourceBundle, "appHub.error.missingUrl");
				return;
			}
			if (appHubApplication == null || appHubApplication.getActions() == null || appHubApplication.getActions().isEmpty() == true) {
				PrimeFaces.current().executeScript("openTab ('" + entry.getUrl() + "');");
				return;
			}

			appHubApplication.setName(entry.getTitle());
			appHubApplication.setUrl(entry.getUrl());
			processValuesBySource(appHubApplication);
			try {
				PrimeFaces.current().executeScript("triggerAppHubLogin(" + appHubApplication.getApplicationJson() + ")");
			} catch (IOException e) {
				JsfUtils.addErrorMessage(resourceBundle, "appHub.error.FAILED_TO_EXECUTE_PLUGIN");
				logger.warn("Browser Plugun login failed", e);
			}
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessageWithMessage());
		} catch (Exception e) {
			logger.warn(e.getLocalizedMessage(), e);
			JsfUtils.addErrorMessage(resourceBundle, "appHub.error.FAILED_PERFORM_LOGIN");
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
		PrimeFaces.current().ajax().update("showEntryForm");
		showDialog("showEntry");
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
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.invalidUrl");
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
				JsfUtils.addErrorMessage(resourceBundle, "error.PLUGIN_NO_RESPONSE");
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
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
		byte[] content = keePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
		if (content == null) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_WRITE_ERROR, cloudSafeEntity.getName());
		}
		keePassFile = psChooseFileView.loadKeepassFile(content, masterPassword);
	}

	public void setAuthenticatorKey() {
		if (authPasscodeValue == null || authPasscodeValue.isEmpty()) {
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingAuthPasscode");
			return;
		}
		String passcode = generateTotpCode(authPasscodeValue, null);
		if (passcode == null) {
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.failedAuthPasscode");
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

	public void actionShowInfo() {
		// passwordSafePages = PasswordSafePages.INFO;
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
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.groupAlreadyExists");
				return;
			}
		}
		Group newGroup = new GroupBuilder(sortedGroupName).build();
		groups.getGroups().add(newGroup);
		try {
			byte[] content = keePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			keePassFile = psChooseFileView.loadKeepassFile(content, masterPassword);
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
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.groupAlreadyExists");
				return;
			}
		}
		try {
			Group editedGroup = new GroupBuilder(currentGroup).name(sortedGroupName).build();
			replaceGroup(currentGroup, editedGroup);

			// Collections.replaceAll(keePassFile.getRoot().getGroups(), currentGroup, editedGroup);

			byte[] content = keePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				// loadKeepassFile(byteArrayOutputStream.toByteArray(), password);
				return;
			}
			keePassFile = psChooseFileView.loadKeepassFile(content, masterPassword);
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

	public void actionCloseAddDialog() {
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("addAppDlg");
	}

	/**
	 * 
	 */
	public void actionAddEditEntry() {
		if (appNameValue == null || appNameValue.trim().isEmpty() == true) {
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingName");
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
				// uploadedImage.length;
				uploadedImage = DcemUtils.resizeImage(uploadedImage, DcemConstants.IMAGE_MAX, "png");
//				Files.write(uploadedImage, new File("c:\\temp\\image.png"));				
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
			PrimeFaces.current().ajax().update("psFileForm:gridPanel");
			hideDialog("addAppDlg");
			currentEntry = null;
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.APP_NAME_EXISTS) {
				JsfUtils.addErrorMessage(resourceBundle, "appHub.error.APP_NAME_EXISTS");
				return;
			}
			if (exp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				logger.info("customAppAlreadyExists", exp);
				JsfUtils.addErrorMessage(resourceBundle, "appHub.error.APP_NAME_EXISTS");
				return;
			}
			if (exp.getErrorCode() == DcemErrorCodes.IMAGE_TOO_BIG) {
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.imgExceedsSize");
				return;
			}
			logger.info(exp.toString(), exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
		} catch (Exception e) {
			logger.info("Coundn't add Application", e);
			JsfUtils.addErrorMessage(resourceBundle, "appHub.error.FAILED_ADD_APPLICATION");
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

	@Override
	public String getDisplayName() {
		if (cloudSafeEntity == null) {
			return subject.getDisplayName();
		}
		return subject.getDisplayName() + ": " + cloudSafeEntity.getName();
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
			byte[] content = keePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			keePassFile = psChooseFileView.loadKeepassFile(content, masterPassword);
			PrimeFaces.current().ajax().update("psFileForm:gridPanel");
		} catch (Exception e) {
			logger.info("Couldn't delete Entry: " + entry.getTitle(), e);
			JsfUtils.addErrorMessage(resourceBundle, "appHub.error.FAILED_CLOSE_APPLICATION");
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
			Group recicleBinGroup = new GroupBuilder().name(resourceBundle.getString("title.RecycleBin")).build();
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
			uploadedImage = DcemUtils.resizeImage(event.getFile().getContent(), DcemConstants.IMAGE_MAX, "png");
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
					JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassPropertyAlreadyExist"));
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
			JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassSelectProperty"));
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
			JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassSelectProperty"));
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
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingActionDelayValue");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			if (isNumeric(editedAction.getValueParameter())) {
				editedAction.setValueParameter(this.selectedAction.getValueParameter());
			} else {
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.numericValue");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
			}
			editedAction.setValueSourceType(null);
			editedAction.setSelector(null);
			break;
		case button:
			if (editedAction.getSelector() == null || editedAction.getSelector().isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingActionSelector");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			editedAction.setValueSourceType(null);
			editedAction.setValueParameter(null);
			break;
		case input:
			if (editedAction.getSelector() == null || editedAction.getSelector().isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingActionSelector");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			if (selectedActionSourceType != null) {
				editedAction.setValueSourceType(selectedActionSourceType.name());
			}
			if (isActionWithParameter() == true) {
				if (editedAction.getValueParameter() == null || editedAction.getValueParameter().isEmpty() == true) {
					JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.missingActionInputName");
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
			JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassSelectAttachment"));
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
			JsfUtils.addErrorMessage(resourceBundle.getString("message.keePassSelectAttachment"));
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
			JsfUtils.addErrorMessage(resourceBundle, "appHub.error.cannotDeleteParentGroup");
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
			byte[] content = keePassLogic.saveDatabaseFile(cloudSafeEntity, keePassFile, cloudSafeEntity.getName(), masterPassword);
			if (content == null) {
				return;
			}
			keePassFile = psChooseFileView.loadKeepassFile(content, masterPassword);
			PrimeFaces.current().ajax().update("psFileForm:gridPanel");
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
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.selectAction");
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
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.selectAction");
			return;
		}
		if (selectedActions.size() != 1) {
			JsfUtils.addErrorMessage(resourceBundle, "appHubAdmin.error.selectOnlyOneAction");
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

	public void onUploadKeepass() {
		uploadedFile = null;
		previousUploadedFile = null;
		PrimeFaces.current().ajax().update("uploadFileForm:uploadFileDialog");
		PrimeFaces.current().executeScript("PF('uploadFileDialog').show();");
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

				hideDialog("setPredefinedAppDialog");
				showDialog("addAppDlg");
				PrimeFaces.current().ajax().update("addAppsForm");
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
			JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassSelectProperty"));
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
			JsfUtils.addInfoMessage(resourceBundle.getString("message.keePassSelectProperty"));
			return;
		}

		List<Property> properties = currentEntry.getEntry().getProperties();
		int propertyInedx = properties.indexOf(selectedProperty);
		if (properties.size() == propertyInedx + 1) {
			return;
		}
		Collections.swap(properties, propertyInedx, propertyInedx + 1);
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

	public boolean isRememberPassword() {
		return rememberPassword;
	}

	public boolean isRecycleBin(Group group) {
		return getRecycleBinGroup().getUuid().equals(group.getUuid());
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

	public String getUserId() {
		return operatorSessionBean.getDcemUser().getLoginId();
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
		// if (currentOpenPasswordSafe != null && passwordSafePages == PasswordSafePages.FILE_PANEL) {
		// return Files.getNameWithoutExtension(currentOpenPasswordSafe.getName());
		// }
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

//	public static void setActionSourceTypes(List<SelectItem> actionSourceTypes) {
//		this.actionSourceTypes = actionSourceTypes;
//	}

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

	public void setKeePassFile(KeePassFile keePassFile) {
		this.keePassFile = keePassFile;
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void setCloudSafeEntity(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;
	}

}