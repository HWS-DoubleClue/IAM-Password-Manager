package com.doubleclue.dcem.userportal.gui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.userportal.entities.ApplicationHubEntity;
import com.doubleclue.dcem.userportal.logic.ActionTypeEnum;
import com.doubleclue.dcem.userportal.logic.AppHubAction;
import com.doubleclue.dcem.userportal.logic.AppHubApplication;
import com.doubleclue.dcem.userportal.logic.KeepassEntryLogic;
import com.doubleclue.dcem.userportal.logic.MyApplication;
import com.doubleclue.dcem.userportal.logic.UpAppHubLogic;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcem.userportal.subjects.ApplicationHubAdminSubject;
import com.doubleclue.dcup.gui.PasswordSafeRecentFile;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;

import de.slackspace.openkeepass.domain.KeePassFile;

@Named("applicationHubAdminView")
@SessionScoped
public class ApplicationHubAdminView extends DcemView {

	private static final long serialVersionUID = 1L;

	@Inject
	private ApplicationHubAdminSubject applicationHubAdminSubject;

	@Inject
	UpAppHubLogic upAppHubLogic;

	@Inject
	AsModule asModule;

	@Inject
	UserLogic userLogic;

	@Inject
	KeepassEntryLogic keepassEntryLogic;

	private List<ApplicationHubEntity> applications;
	protected KeePassFile keePassFile = null;
	List<PasswordSafeRecentFile> recentFiles;
	private String masterPassword;
	private String psHistory;
	private boolean savePassword;
	private ApplicationHubEntity currentApp;
	private String searchValue;
	private String importAppValue;
	private String appNameValue;
	private String appUrlValue = "https://";
	private String logoFileName;
	private UploadedFile fileLogo;
	private List<String> uploadedFiles;
	List<ApplicationHubEntity> uploadApplicationFilesList = new ArrayList<>();
	private String pluginResponse;
	private static List<SelectItem> actionSourceTypes = null;
	private AttributeTypeEnum selectedActionSourceType = null;

	private ActionTypeEnum selectedActionType;
	private List<AppHubAction> selectedActions = new ArrayList<AppHubAction>();
	private AppHubAction selectedAction;
	private boolean editingAction;
	private boolean editingApplication;
	private static List<SelectItem> actionTypes = null;
	private ApplicationHubEntity appNameExistsEntity;
	boolean replaceExisting;

	private Logger logger = LogManager.getLogger(ApplicationHubAdminView.class);

	@PostConstruct
	public void init() {
		subject = applicationHubAdminSubject;
		try {
			applications = upAppHubLogic.migrateApplications26();
		} catch (Exception e) {
			logger.error("Could not laod applications", e);
		}
		keepassEntryLogic.migrate26();
	}

	public void onView() {
		applications = null;
	}

	public StreamedContent getFileLogoImg() {
		if (currentApp == null || currentApp.getLogo() == null) {
			return DefaultStreamedContent.builder().contentType("image/png")
					.stream(() -> this.getClass().getResourceAsStream("/appHub/DC_Logo_transp_01.2.png")).build();
		}
		return DefaultStreamedContent.builder().contentType("image/png").stream(() -> new ByteArrayInputStream(currentApp.getLogo())).build();
	}

	public List<ApplicationHubEntity> getApplications() {
		if (applications == null) {
			try {
				applications = upAppHubLogic.getAllApplicationsByName(null);
			} catch (Exception e) {
				logger.error("Couldn't get Applications ", e);
			}
		}
		return applications;
	}

	public void appUrlValueValidate() {
		try {
			byte[] logo = upAppHubLogic.appUrlValueValidate(appUrlValue, true);
			currentApp.setLogo(logo);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
			return;
		}

		PrimeFaces.current().ajax().update("addAppsForm:tabView:addAppUrlField");
		PrimeFaces.current().ajax().update("addAppsForm:tabView:fileLogoImg");
	}

	public void setAppUrlValue(String appUrlValue) {
		try {
			this.appUrlValue = java.net.URLDecoder.decode(appUrlValue, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			logger.warn(e.getLocalizedMessage(), e);
			return;
		}
	}

	public void fileLogoListener(FileUploadEvent event) {
		currentApp.setLogo(event.getFile().getContent());
		logoFileName = FilenameUtils.getName(event.getFile().getFileName());
		return;
	}

	public List<SelectItem> getActionTypes() {
		if (actionTypes == null) {
			actionTypes = new LinkedList<>();
			for (ActionTypeEnum propertyEnum : ActionTypeEnum.values()) {
				actionTypes.add(new SelectItem(propertyEnum.name(), propertyEnum.getDisplayName()));
			}
		}
		return actionTypes;
	}

	public boolean isActionWithSelector() {
		boolean returnValue = false;
		if (selectedActionType == null) {
			return false;
		}
		switch (selectedActionType) {
		case input:
		case button:
			returnValue = true;
			break;
		default:
			break;
		}
		return returnValue;
	}

	public void clearActionValues() {
		List<AppHubAction> selectedActions = new ArrayList<AppHubAction>();
		AppHubAction selectedAction = new AppHubAction();
		selectedAction.setIndex(currentApp.getApplication().getActions().size());
		selectedActions.add(selectedAction);
		setSelectedActions(selectedActions);
	}

	public void setPsHistory(String psHistory) {
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

	public void addApplication() {
		currentApp = new ApplicationHubEntity();
		fileLogo = null;
		logoFileName = "";
		appNameValue = "";
		appUrlValue = "https://";
		editingApplication = false;
		currentApp.setApplication(new AppHubApplication(appNameValue, appUrlValue, new ArrayList<AppHubAction>()));

		PrimeFaces.current().ajax().update("addAppsForm");
		showDialog("addAppDlg");
	}

	public void importApplication() {
		currentApp = new ApplicationHubEntity();
		importAppValue = "";
		fileLogo = null;
		logoFileName = "";
		uploadedFiles = null;
		uploadApplicationFilesList = null;
		showDialog("importAppDlg");
	}

	public void handleFilesUpload(FileUploadEvent event) {
		if (uploadedFiles == null) {
			uploadedFiles = new ArrayList<>();
		}
		UploadedFile uploadedfile = event.getFile();
		if (uploadedfile != null) {
			// Check file extension
			int i = uploadedfile.getFileName().lastIndexOf('.');
			if (i == -1 || "dcMyApp".equals(uploadedfile.getFileName().substring(i + 1)) == false) {
				JsfUtils.addErrorMessage(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.invalidFileExt"));
				return;
			}
			try {
				String fileContents = new String(uploadedfile.getContent(), DcemConstants.CHARSET_UTF8);
				ObjectMapper mapper = new ObjectMapper();
				ApplicationHubEntity appHubEntity = mapper.readValue(fileContents, ApplicationHubEntity.class);

				if (appHubEntity.getLogo().length == 0 || appHubEntity.getLogo() == null) {
					InputStream x = this.getClass().getResourceAsStream("/appHub/DC_Logo_transp_01.2.png");
					appHubEntity.setLogo(ByteStreams.toByteArray(x));
				}

				if (uploadApplicationFilesList == null) {
					uploadApplicationFilesList = new ArrayList<>();
				}
				uploadApplicationFilesList.add(appHubEntity);
				uploadedFiles.add(appHubEntity.getName());
			} catch (JsonParseException exp) {
				logger.error("Upload Failed", exp);
				JsfUtils.addErrorMessage("Fiel is not compatible to DoubleClue format. " + exp.getMessage());
			} catch (Exception e) {
				logger.error("Upload Failed", e);
				JsfUtils.addErrorMessage("upps something went wrong: " + e.toString());
			}
		} else {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.fileNotFound"));
			showDialog("importAppDlg");
		}
	}

	// public void replaceDuplicateNameApplication() {
	// ApplicationHubEntity app = upAppHubLogic.getPredefinedApplication(appNameExistsEntity.getName());
	// if (app == null) {
	// return;
	// }
	// upAppHubLogic.deleteApplication(app);
	// if (uploadApplicationFilesList == null) {
	// uploadApplicationFilesList = new ArrayList<>();
	// }
	// uploadApplicationFilesList.add(appNameExistsEntity);
	// if (appNameExistsEvent.getFile().getFileName() != null) {
	// uploadedFiles.add(appNameExistsEvent.getFile().getFileName());
	// }
	// PrimeFaces.current().ajax().update("importAppsForm:importAppDlg");
	// hideDialog("renameOrReplaceDlg");
	// showDialog("importAppDlg");
	// }
	//
	// public void renameDuplicateNameApplication() {
	// String previousName = appNameExistsEntity.getName();
	// appNameExistsEntity.setName(previousName + " (New)");
	// if (uploadApplicationFilesList == null) {
	// uploadApplicationFilesList = new ArrayList<>();
	// }
	// uploadApplicationFilesList.add(appNameExistsEntity);
	// if (appNameExistsEvent.getFile().getFileName() != null) {
	// uploadedFiles.add(appNameExistsEvent.getFile().getFileName());
	// }
	// PrimeFaces.current().ajax().update("importAppsForm:importAppDlg");
	// hideDialog("renameOrReplaceDlg");
	// showDialog("importAppDlg");
	//
	// }

	public String getUploadedFiles() {
		try {
			if (uploadedFiles == null) {
				return null;
			}
			StringBuffer sb = new StringBuffer();
			for (String filename : uploadedFiles) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(filename);
			}
			return sb.toString();
		} catch (Exception e) {
			logger.warn("uploadApplicationFilesList", e);
			return e.toString();
		}
	}

	public void actionImportApps() {
		if (uploadedFiles == null || uploadedFiles.isEmpty()) {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.selectOneFileAtLeast"));
			return;
		}
		try {
			for (ApplicationHubEntity applicatioHubEntity : uploadApplicationFilesList) {
				ApplicationHubEntity applicatioHubEntityExisting = upAppHubLogic.getApplicationByName(applicatioHubEntity.getName());
				if (applicatioHubEntityExisting != null) {
					if (applicatioHubEntity.getLogo() != null) {
						applicatioHubEntity.setLogo(DcemUtils.resizeImage(applicatioHubEntity.getLogo(), DcemConstants.IMAGE_MAX));
					}
					if (replaceExisting == true) {
						applicatioHubEntityExisting.setLogo(applicatioHubEntity.getLogo());
						applicatioHubEntityExisting.setApplication(applicatioHubEntity.getApplication());
					} else {
						applicatioHubEntity.setName(applicatioHubEntity.getName() + " (New)");
						upAppHubLogic.updateApplication(applicatioHubEntity);
					}
				} else {
					upAppHubLogic.updateApplication(applicatioHubEntity);
				}
			}
			upAppHubLogic.migrateApplications26();
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessageWithMessage());
		} catch (Exception exp) {
			logger.warn("Couldn't upload a Valid meta data file", exp);
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.invalidFile"));
			showDialog("importAppDlg");
		}
		actionCloseImportDialog();
	}

	public void actionCloseImportDialog() {
		uploadedFiles = null;
		uploadApplicationFilesList = null;
		hideDialog("importAppDlg");
		refreshAppHub();
	}

	public void actionAddApplication() {

		try {

			if (appNameValue != null && appNameValue.trim().isEmpty() == false && appNameValue.equals(currentApp.getName()) == false) {
				currentApp.setName(appNameValue);
				currentApp.getApplication().setName(appNameValue);
			} else if (appNameValue == null || appNameValue.trim().isEmpty() == true) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingName");
				return;
			}
			if (appUrlValue.trim().isEmpty() || "http://".equals(appUrlValue)) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingUrl");
				return;
			} else {
				currentApp.getApplication().setUrl(appUrlValue);
			}
			if (currentApp.getApplication().getActions().isEmpty() == true) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActions");
				return;
			}

			if (currentApp.getLogo() != null) {
				try {
					currentApp.setLogo(DcemUtils.resizeImage(currentApp.getLogo(), DcemConstants.IMAGE_MAX));
				} catch (DcemException exp) {
					if (exp.getErrorCode() == DcemErrorCodes.IMAGE_TOO_BIG) {
						JsfUtils.addErrorMessage(exp.getLocalizedMessage());
						return;
					} else {
						logger.warn("Couldn't resize image", exp);
						JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.failedToResizeImg");
						return;
					}
				}
			}
			upAppHubLogic.updateApplication(currentApp);
			DcemUtils.reloadTaskNodes(UpAppHubLogic.class);
			hideDialog("addAppDlg");
			refreshAppHub();
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.warn("Something wrong.", e);
			JsfUtils.addErrorMessage(e.toString());
		}

	}

	public void deleteApplication(ApplicationHubEntity app) {
		try {
			upAppHubLogic.deleteApplication(app);
			refreshAppHub();
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public void editApplication(ApplicationHubEntity app) {
		try {
			currentApp = upAppHubLogic.getApplicationById(app.getId());
			appNameValue = app.getName();
			appUrlValue = app.getApplication().getUrl();
			editingApplication = true;
			PrimeFaces.current().ajax().update("addAppsForm");
			PrimeFaces.current().ajax().update("addAppsForm:tabView:fileLogoImg");
			showDialog("addAppDlg");
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void exportApplication(ApplicationHubEntity app) {
		try {
			currentApp = app;
			exportToFile(currentApp);
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void exportToFile(ApplicationHubEntity applicationHubEntity) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			byte[] data = mapper.writeValueAsBytes(new MyApplication(applicationHubEntity));
			JsfUtils.downloadFile("application/octet", applicationHubEntity.getName() + ".dcMyApp", data);
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Couldn't export the application, cause: " + e.toString());
			logger.warn("Could serialize application", e);
		}
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

	public void refreshAppHub() {
		try {
			applications = upAppHubLogic.getAllApplicationsByName(null);
		} catch (Exception e) {
			logger.error("Couldn't get applications. ", e);
		}
		PrimeFaces.current().ajax().update("applicationHubForm");
	}

	public void actionAddEditAction() {
		AppHubAction editedAction = selectedActions.get(0);
		switch (selectedActionType) {
		case delay:
			if (editedAction.getValueParameter() == null || getNumeric(editedAction.getValueParameter()) == -1) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionDelayValue");
				PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
				return;
			}
			if (getNumeric(editedAction.getValueParameter()) != -1) {
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
			editedAction.setValueParameter(null);
			editedAction.setValueSourceType(null);
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
				if (editedAction.getValueParameter() == null || editedAction.getValueParameter().isEmpty()) {
					JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingActionInputName");
					PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
					return;
				}
			}
		}
		editedAction.setType(selectedActionType.name());
		if (editingAction == false) {
			currentApp.getApplication().getActions().add(editedAction);
		}
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("actionDialog");
	}

	private static int getNumeric(String strNum) {
		if (strNum == null) {
			return -1;
		}
		try {
			return Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	public void filterApplications() {
		try {
			applications = upAppHubLogic.getAllApplicationsByName(searchValue);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}

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

	public void clearAction() {
		if (selectedActions == null || selectedActions.isEmpty()) {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.selectAction");
			return;
		}

		for (AppHubAction action : selectedActions) {
			currentApp.getApplication().getActions().remove(action);
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

	public void executePlugin() {
		ApplicationHubEntity entity = new ApplicationHubEntity();
		if (appUrlValue.trim().isEmpty() == false && "https://".equals(appUrlValue) == false) {
			try {
				upAppHubLogic.appUrlValueValidate(appUrlValue, false);
			} catch (Exception exp) {
				JsfUtils.addErrorMessage("Cause: " + exp.getClass().getSimpleName() + " - " + exp.getMessage());
				return;
			}
			AppHubApplication app = new AppHubApplication();
			app.setUrl(appUrlValue);
			entity.setApplication(app);
			try {
				PrimeFaces.current().executeScript("triggerAppHubAdminLogin(" + entity.getApplication().getApplicationJson() + ")");
			} catch (Exception e) {
				logger.warn(e);
			}
		} else {
			JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.missingUrl");
			return;
		}
	}

	public void actionFinishExecutePlugin() {
		try {
			int actionCount = 0;
			if (pluginResponse == null || pluginResponse.isEmpty()) {
				JsfUtils.addErrorMessage(UserPortalModule.RESOURCE_NAME, "error.PLUGIN_NO_RESPONSE");
			} else {
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				List<AppHubAction> importedActions = objectMapper.readValue(pluginResponse, new TypeReference<List<AppHubAction>>() {
				});
				for (AppHubAction appHubAction : importedActions) {
					appHubAction.setIndex(actionCount);
					actionCount++;
				}
				currentApp.getApplication().setActions(importedActions);
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} finally {
			pluginResponse = null;
		}
	}

	public List<SelectItem> getActionSourceTypes() {
		if (actionSourceTypes == null) {
			actionSourceTypes = new LinkedList<>();
			for (AttributeTypeEnum propertyEnum : AttributeTypeEnum.values()) {
				actionSourceTypes.add(new SelectItem(propertyEnum.name(), propertyEnum.getDisplayName()));
			}
		}
		return actionSourceTypes;
	}

	public void listenerChangeActionSourceType() {
		AppHubAction selectedAction;
		if (selectedActions != null && selectedActions.size() != 1) {
			return;
		} else {
			selectedAction = selectedActions.get(0);
		}
		selectedAction.setValueSourceType(selectedActionSourceType.name());
		if (selectedActionSourceType != AttributeTypeEnum.STATIC_TEXT)
			selectedAction.setOutputValue(selectedActionSourceType.getDisplayName());
	}

	public AttributeTypeEnum getSelectedActionSourceType() {
		return selectedActionSourceType;
	}

	public void setSelectedActionSourceType(AttributeTypeEnum selectedActionSourceType) {
		this.selectedActionSourceType = selectedActionSourceType;
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

	public boolean isActionWithValue() {
		boolean returnValue = false;
		AppHubAction selectedAction;
		if (selectedActions == null || selectedActions.size() != 1) {
			return returnValue;
		} else {
			selectedAction = selectedActions.get(0);
		}
		if (selectedActionType != null && selectedActions != null) {
			if (selectedActionType == ActionTypeEnum.input) {
				AttributeTypeEnum valueSource = AttributeTypeEnum.valueOf(selectedAction.getValueSourceType());
				if (selectedActions == null || valueSource == null) {
					return returnValue;
				}
				switch (valueSource) {
				case CLOUD_SAFE_USER:
				case STATIC_TEXT:
					selectedAction.setOutputValue("");
					returnValue = true;
					break;
				default:
					break;
				}
			} else if (selectedActionType == ActionTypeEnum.delay) {
				returnValue = true;
			}
		}
		return returnValue;
	}

	public void leavingView() {
		applications = null;
	}

	@Deprecated
	public String getApplicationPath() throws DcemException {
		String decodedURL = null;
		try {
			decodedURL = URLDecoder.decode(LocalPaths.getDcemInstallDir().toString(), StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			logger.warn("AppHubAdmin : Could not decode the URL.", e);
		}
		return decodedURL + File.separator + "myApplications";
	}

	public StreamedContent getAppImage(ApplicationHubEntity applicationHubEntity) {
		if (applicationHubEntity.getLogo() == null) {
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> this.getClass().getResourceAsStream(DcemConstants.DEFAULT_APP_ICON))
					.build();
		}
		return DefaultStreamedContent.builder().contentType("image/png").stream(() -> new ByteArrayInputStream(applicationHubEntity.getLogo())).build();

	}

	void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}

	void hideDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').hide();");
	}

	public void actionCloseAddDialog() {
		PrimeFaces.current().ajax().update("addAppsForm:tabView:actionsTable");
		hideDialog("addAppDlg");
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

	public String getLogoFileName() {
		return logoFileName;
	}

	public void setLogoFileName(String logoFileName) {
		this.logoFileName = logoFileName;
	}

	public UploadedFile getFileLogo() {
		return fileLogo;
	}

	public void setFileLogo(UploadedFile fileLogo) {
		this.fileLogo = fileLogo;
	}

	public String getLogoName() {
		return logoFileName;
	}

	public boolean isEditingAction() {
		return editingAction;
	}

	public void setEditingAction(boolean editingAction) {
		this.editingAction = editingAction;
	}

	public ApplicationHubEntity getCurrentApp() {
		return currentApp;
	}

	public List<AppHubAction> getCurrentAppActions() {
		if (currentApp == null || currentApp.getApplication() == null) {
			return null;
		}
		return currentApp.getApplication().getActions();
	}

	public String getActionValueSource(AppHubAction appHubAction) {
		if (appHubAction.type.equals(ActionTypeEnum.input.name())) {
			try {
				return AttributeTypeEnum.valueOf(appHubAction.getValueSourceType()).getDisplayName();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		return null;
	}

	public void setCurrentApp(ApplicationHubEntity currentApp) {
		this.currentApp = currentApp;
	}

	public String getPluginResponse() {
		return pluginResponse;
	}

	public void setPluginResponse(String pluginResponse) {
		this.pluginResponse = pluginResponse;
	}

	public boolean isSavePassword() {
		return savePassword;
	}

	public void setSavePassword(boolean savePassword) {
		this.savePassword = savePassword;
	}

	public String getPsHistory() {
		return psHistory;
	}

	public ActionTypeEnum getSelectedActionType() {
		return selectedActionType;
	}

	public void setSelectedActionType(ActionTypeEnum selectedActionType) {
		this.selectedActionType = selectedActionType;
	}

	public AppHubAction getSelectedAction() {
		if (selectedActions != null && selectedActions.isEmpty() == false) {
			return selectedActions.get(0);
		}
		return null;
	}

	public void setSelectedAction(AppHubAction selectedAction) {
		this.selectedAction = selectedAction;
	}

	public List<AppHubAction> getSelectedActions() {
		return selectedActions;
	}

	public void setSelectedActions(List<AppHubAction> selectedAction) {
		this.selectedActions = selectedAction;
	}

	public String getMasterPassword() {
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
	}

	public boolean isEditingApplication() {
		return editingApplication;
	}

	public void setEditingApplication(boolean editingApplication) {
		this.editingApplication = editingApplication;
	}

	public String getSearchValue() {
		return searchValue;
	}

	public void setSearchValue(String searchValue) {
		this.searchValue = searchValue;
	}

	public String getImportAppValue() {
		return importAppValue;
	}

	public void setImportAppValue(String importAppValue) {
		this.importAppValue = importAppValue;
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

	public ApplicationHubEntity getAppNameExistsEntity() {
		return appNameExistsEntity;
	}

	public void setAppNameExistsEntity(ApplicationHubEntity appNameExistsEntity) {
		this.appNameExistsEntity = appNameExistsEntity;
	}

	public boolean isReplaceExisting() {
		return replaceExisting;
	}

	public void setReplaceExisting(boolean replaceExisting) {
		this.replaceExisting = replaceExisting;
	}

}
