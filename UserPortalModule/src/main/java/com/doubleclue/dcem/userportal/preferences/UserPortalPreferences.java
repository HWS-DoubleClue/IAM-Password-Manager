package com.doubleclue.dcem.userportal.preferences;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcup.logic.ActionItem;
import com.doubleclue.dcup.logic.NotificationType;
import com.doubleclue.dcup.logic.ViewItem;
import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("serial")
@XmlType
@XmlRootElement(name = "userPortalPreferences")
public class UserPortalPreferences extends ModulePreferences {

	private boolean enableCaptcha = false;
	private boolean enableAutoComplete = true;
	private boolean enableLocalUserRegistration = false;

	private int urlTokenTimeout = 120; // minutes

	private Map<ViewItem, Boolean> visibleViews;
	private Map<ViewItem, Boolean> twoFactorRequiredViews;
	private Map<ActionItem, Boolean> visibleActions;
	private Map<ActionItem, Boolean> twoFactorRequiredActions;
	private NotificationType notificationType = NotificationType.BOTH;
	String tutorialUrl = "https://doubleclue.com/files/UserPortal_Manual.pdf";

	public UserPortalPreferences() {
		visibleViews = new HashMap<>();
		for (ViewItem item : ViewItem.values()) {
			if (item.name().contains("DEPRECATED")) {
				continue;
			}
			visibleViews.put(item, true);
		}
		visibleActions = new HashMap<>();
		for (ActionItem items : ActionItem.values()) {
			visibleActions.put(items, true);
		}
		twoFactorRequiredViews = new HashMap<>();
		for (ViewItem items : ViewItem.values()) {
			twoFactorRequiredViews.put(items, true);
		}
		twoFactorRequiredActions = new HashMap<>();
		for (ActionItem items : ActionItem.values()) {
			twoFactorRequiredActions.put(items, true);
		}
	}

	@JsonIgnore
	public boolean isTwoFactorRequiredView(ViewItem item) {
		Boolean value = twoFactorRequiredViews.get(item);
		return (value != null) ? value : false;
	}

	@JsonIgnore
	public boolean isTwoFactorRequiredAction(ActionItem item) {
		Boolean value = twoFactorRequiredActions.get(item);
		return (value != null) ? value : false;
	}

	@JsonIgnore
	public boolean isViewVisible(ViewItem item) {
		Boolean value = visibleViews.get(item);
		return (value != null) ? value : false;
	}

	@JsonIgnore
	public boolean isActionVisible(ActionItem item) {
		Boolean value = visibleActions.get(item);
		return (value != null) ? value : false;
	}

	@JsonIgnore
	public boolean isCreateAccountEnabled() {
		return enableLocalUserRegistration == true;
	}

	public boolean isEnableCaptcha() {
		return enableCaptcha;
	}

	public void setEnableCaptcha(boolean enableCaptcha) {
		this.enableCaptcha = enableCaptcha;
	}

	public Map<ViewItem, Boolean> getVisibleViews() {
		return visibleViews;
	}

	public void setVisibleViews(Map<ViewItem, Boolean> visibleViews) {
		this.visibleViews = visibleViews;

	}

	public Map<ViewItem, Boolean> getTwoFactorRequiredViews() {
		return twoFactorRequiredViews;
	}

	public void setTwoFactorRequiredViews(Map<ViewItem, Boolean> twoFactorRequiredViews) {
		this.twoFactorRequiredViews = twoFactorRequiredViews;
	}

	public Map<ActionItem, Boolean> getVisibleActions() {
		return visibleActions;
	}

	public void setVisibleActions(Map<ActionItem, Boolean> visibleActions) {
		this.visibleActions = visibleActions;
	}

	public Map<ActionItem, Boolean> getTwoFactorRequiredActions() {
		return twoFactorRequiredActions;
	}

	public void setTwoFactorRequiredActions(Map<ActionItem, Boolean> twoFactorRequiredActions) {
		this.twoFactorRequiredActions = twoFactorRequiredActions;
	}

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}

	public boolean isEnableLocalUserRegistration() {
		return enableLocalUserRegistration;
	}

	public void setEnableLocalUserRegistration(boolean enableLocalUserRegistration) {
		this.enableLocalUserRegistration = enableLocalUserRegistration;
	}

	public int getUrlTokenTimeout() {
		return urlTokenTimeout;
	}

	public void setUrlTokenTimeout(int urlTokenTimeout) {
		this.urlTokenTimeout = urlTokenTimeout;
	}

	public boolean isEnableAutoComplete() {
		return enableAutoComplete;
	}

	public void setEnableAutoComplete(boolean enableAutoComplete) {
		this.enableAutoComplete = enableAutoComplete;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		UserPortalPreferences thisPortalPreferences = (UserPortalPreferences) super.clone();
		Map<ViewItem, Boolean> clonedVisibleViews = new HashMap<ViewItem, Boolean>();
		for (ViewItem key : visibleViews.keySet()) {
			clonedVisibleViews.put(key, visibleViews.get(key));
		}
		Map<ActionItem, Boolean> clonedVisibleActions = new HashMap<ActionItem, Boolean>();
		for (ActionItem key : visibleActions.keySet()) {
			clonedVisibleActions.put(key, visibleActions.get(key));
		}
		thisPortalPreferences.setVisibleViews(clonedVisibleViews);
		thisPortalPreferences.setVisibleActions(clonedVisibleActions);
		return thisPortalPreferences;
	}

	public String getTutorialUrl() {
		return tutorialUrl;
	}

	public void setTutorialUrl(String tutorialUrl) {
		this.tutorialUrl = tutorialUrl;
	}
}
