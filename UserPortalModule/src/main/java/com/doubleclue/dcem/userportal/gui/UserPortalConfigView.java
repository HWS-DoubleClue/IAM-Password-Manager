package com.doubleclue.dcem.userportal.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.userportal.logic.UserPortalModule;
import com.doubleclue.dcem.userportal.preferences.UserPortalPreferences;
import com.doubleclue.dcem.userportal.subjects.UserPortalConfigSubject;
import com.doubleclue.dcup.logic.ActionItem;
import com.doubleclue.dcup.logic.NotificationType;
import com.doubleclue.dcup.logic.ViewItem;
import com.doubleclue.utils.ResourceBundleUtf8Control;

@SuppressWarnings("serial")
@Named("userPortalConfigView")
@SessionScoped
public class UserPortalConfigView extends DcemView {

	@Inject
	private UserPortalConfigSubject userPortalConfigSubject;

	@Inject
	UserPortalModule userPortalModule;

	@Inject
	ConfigLogic configLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	AuditingLogic auditingLogic;

	private Logger logger = LogManager.getLogger(UserPortalConfigView.class);
	private ResourceBundle resourceBundle;

	private UserPortalPreferences modulePreferencesClone;
	private UserPortalPreferences modulePreferencesPrevious;

	private List<SelectItem> viewItemList;
	private List<SelectItem> actionItemList;
	private List<SelectItem> notificationTypeList;

	@PostConstruct
	public void init() {
		subject = userPortalConfigSubject;
		resourceBundle = ResourceBundle.getBundle(UserPortalModule.RESOURCE_NAME, operatorSessionBean.getLocale(), new ResourceBundleUtf8Control());
	}

	@Override
	public void reload() {
		try {
			UserPortalPreferences preferences = userPortalModule.getModulePreferences();
			modulePreferencesClone = (UserPortalPreferences) preferences.clone();
			modulePreferencesPrevious = (UserPortalPreferences) preferences.clone();

		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public void actionSave() {
		try {
			userPortalModule.preferencesValidation(modulePreferencesClone);
			try {
				String changeInfo;
				try {
					changeInfo = DcemUtils.compareObjects(modulePreferencesPrevious, modulePreferencesClone);
					DcemAction action = new DcemAction(viewNavigator.getActiveModule().getId(), "Preferences", DcemConstants.ACTION_SAVE);
					auditingLogic.addAudit(action, changeInfo);
				} catch (DcemException e) {
					logger.warn("Couldn't compare operator", e);
				}
				configLogic.setModulePreferencesInCluster(viewNavigator.getActiveModule().getId(), modulePreferencesPrevious, modulePreferencesClone);
				modulePreferencesPrevious = (UserPortalPreferences) modulePreferencesClone.clone();
				JsfUtils.addInformationMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.ok");
			} catch (Exception exp) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.error", exp.getMessage());
			}

		} catch (DcemException exp) {
			JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.warning", exp.toString());
		}
	}

	public UserPortalPreferences getUserPortalConfig() {
		return modulePreferencesClone;
	}

	public List<SelectItem> getViewItemList() {
		if (viewItemList == null) {
			viewItemList = new ArrayList<>();
			for (ViewItem value : ViewItem.values()) {
				viewItemList.add(new SelectItem(value, JsfUtils.getStringSafely(resourceBundle, value.name())));
			}
		}
		return viewItemList;
	}

	public List<SelectItem> getActionItemList() {
		if (actionItemList == null) {
			actionItemList = new ArrayList<>();
			for (ActionItem value : ActionItem.values()) {
				actionItemList.add(new SelectItem(value.name(), JsfUtils.getStringSafely(resourceBundle, value.name())));
			}
		}
		return actionItemList;
	}

	public List<SelectItem> getNotificationTypeList() {
		if (notificationTypeList == null) {
			notificationTypeList = new ArrayList<>();
			for (NotificationType notificationType : NotificationType.values()) {
				notificationTypeList.add(new SelectItem(notificationType.name(), JsfUtils.getStringSafely(resourceBundle, notificationType.name())));
			}
		}
		return notificationTypeList;
	}

	public List<ViewItem> getSelectedVisibleViews() {
		List<ViewItem> selectedVisibleViews = new LinkedList<>();
		for (Map.Entry<ViewItem, Boolean> entry : getUserPortalConfig().getVisibleViews().entrySet()) {
			if (entry.getValue()) {
				selectedVisibleViews.add(entry.getKey());
			}
		}
		return selectedVisibleViews;
	}

	public void setSelectedVisibleViews(List<ViewItem> selectedVisibleViews) {
		for (Map.Entry<ViewItem, Boolean> entry : getUserPortalConfig().getVisibleViews().entrySet()) {
			entry.setValue(selectedVisibleViews.contains(entry.getKey()));
		}
	}

	public String getSelectedNotificationType() {
		return getUserPortalConfig().getNotificationType().name();
	}

	public void setSelectedNotificationType(String selectedNotificationType) {
		getUserPortalConfig().setNotificationType(NotificationType.valueOf(selectedNotificationType));
	}

	
	public List<String> getSelectedVisibleActions() {
		List<String> selectedVisibleActions = new LinkedList<>();
		for (Map.Entry<ActionItem, Boolean> entry : getUserPortalConfig().getVisibleActions().entrySet()) {
			if (entry.getValue()) {
				selectedVisibleActions.add(entry.getKey().name());
			}
		}
		return selectedVisibleActions;
	}

	public void setSelectedVisibleActions(List<String> selectedVisibleActions) {
		for (Map.Entry<ActionItem, Boolean> entry : getUserPortalConfig().getVisibleActions().entrySet()) {
			if (entry.getKey() != null) {
				entry.setValue(selectedVisibleActions.contains(entry.getKey().name()));
			}
		}
	}

	public List<String> getSelectedTwoFactorRequiredActions() {
		List<String> selectedTwoFactorRequiredActions = new LinkedList<>();
		for (Map.Entry<ActionItem, Boolean> entry : getUserPortalConfig().getTwoFactorRequiredActions().entrySet()) {
			if (entry.getValue()) {
				selectedTwoFactorRequiredActions.add(entry.getKey().name());
			}
		}
		return selectedTwoFactorRequiredActions;
	}

	public void setSelectedTwoFactorRequiredActions(List<String> selectedTwoFactorRequiredActions) {
		for (Map.Entry<ActionItem, Boolean> entry : getUserPortalConfig().getTwoFactorRequiredActions().entrySet()) {
			if (entry.getKey() != null) {
				entry.setValue(selectedTwoFactorRequiredActions.contains(entry.getKey().name()));
			}
		}
	}
}
