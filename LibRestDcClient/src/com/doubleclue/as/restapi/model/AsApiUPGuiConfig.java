/*
 * DoubleClue REST API
 * DoubleClue URL http://yourhost:8001/dcem/restApi/as
 *
 * OpenAPI spec version: 1.5.0
 * Contact: emanuel.galea@hws-gruppe.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.doubleclue.as.restapi.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * AsApiUPGuiConfig
 */

public class AsApiUPGuiConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonProperty("enableCaptcha")
	private boolean enableCaptcha = false;

	@JsonProperty("enableAutoComplete")
	private boolean enableAutoComplete = false;

	@JsonProperty("title")
	private String title = null;

	/**
	 * The user login-Id. Null if token is not assigned
	 */
	public enum NotificationTypeEnum {
		NONE("NONE"),

		SMS_ONLY("SMS_ONLY"),

		EMAIL_ONLY("EMAIL_ONLY"),

		BOTH("BOTH");

		private String value;

		NotificationTypeEnum(String value) {
			this.value = value;
		}

		@JsonValue
		public String getValue() {
			return value;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static NotificationTypeEnum fromValue(String text) {
			for (NotificationTypeEnum b : NotificationTypeEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("notificationType")
	private NotificationTypeEnum notificationType = null;

	@JsonProperty("visibleViews")
	private Map<String, Boolean> visibleViews = null;

	@JsonProperty("twoFactorRequiredViews")
	private Map<String, Boolean> twoFactorRequiredViews = null;

	@JsonProperty("visibleActions")
	private Map<String, Boolean> visibleActions = null;

	@JsonProperty("twoFactorRequiredActions")
	private Map<String, Boolean> twoFactorRequiredActions = null;

	public AsApiUPGuiConfig enableCaptcha(boolean enableCaptcha) {
		this.enableCaptcha = enableCaptcha;
		return this;
	}

	public boolean isEnableCaptcha() {
		return enableCaptcha;
	}

	public void setEnableCaptcha(boolean enableCaptcha) {
		this.enableCaptcha = enableCaptcha;
	}

	public AsApiUPGuiConfig enableAutoComplete(boolean enableAutoComplete) {
		this.enableAutoComplete = enableAutoComplete;
		return this;
	}

	public boolean isEnableAutoComplete() {
		return enableAutoComplete;
	}

	public void setEnableAutoComplete(boolean enableAutoComplete) {
		this.enableAutoComplete = enableAutoComplete;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public AsApiUPGuiConfig notificationType(NotificationTypeEnum notificationType) {
		this.notificationType = notificationType;
		return this;
	}

	public NotificationTypeEnum getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationTypeEnum notificationType) {
		this.notificationType = notificationType;
	}

	public AsApiUPGuiConfig visibleViews(Map<String, Boolean> visibleViews) {
		this.visibleViews = visibleViews;
		return this;
	}

	public AsApiUPGuiConfig putVisibleViewsItem(String key, boolean visibleViewsItem) {
		if (this.visibleViews == null) {
			this.visibleViews = new HashMap<String, Boolean>();
		}
		this.visibleViews.put(key, visibleViewsItem);
		return this;
	}

	public Map<String, Boolean> getVisibleViews() {
		return visibleViews;
	}

	public void setVisibleViews(Map<String, Boolean> visibleViews) {
		this.visibleViews = visibleViews;
	}

	public AsApiUPGuiConfig twoFactorRequiredViews(Map<String, Boolean> twoFactorRequiredViews) {
		this.twoFactorRequiredViews = twoFactorRequiredViews;
		return this;
	}

	public AsApiUPGuiConfig putTwoFactorRequiredViewsItem(String key, boolean twoFactorRequiredViewsItem) {
		if (this.twoFactorRequiredViews == null) {
			this.twoFactorRequiredViews = new HashMap<String, Boolean>();
		}
		this.twoFactorRequiredViews.put(key, twoFactorRequiredViewsItem);
		return this;
	}

	public Map<String, Boolean> getTwoFactorRequiredViews() {
		return twoFactorRequiredViews;
	}

	public void setTwoFactorRequiredViews(Map<String, Boolean> twoFactorRequiredViews) {
		this.twoFactorRequiredViews = twoFactorRequiredViews;
	}

	public AsApiUPGuiConfig visibleActions(Map<String, Boolean> visibleActions) {
		this.visibleActions = visibleActions;
		return this;
	}

	public AsApiUPGuiConfig putVisibleActionsItem(String key, boolean visibleActionsItem) {
		if (this.visibleActions == null) {
			this.visibleActions = new HashMap<String, Boolean>();
		}
		this.visibleActions.put(key, visibleActionsItem);
		return this;
	}

	public Map<String, Boolean> getVisibleActions() {
		return visibleActions;
	}

	public void setVisibleActions(Map<String, Boolean> visibleActions) {
		this.visibleActions = visibleActions;
	}

	public AsApiUPGuiConfig twoFactorRequiredActions(Map<String, Boolean> twoFactorRequiredActions) {
		this.twoFactorRequiredActions = twoFactorRequiredActions;
		return this;
	}

	public AsApiUPGuiConfig putTwoFactorRequiredActionsItem(String key, boolean twoFactorRequiredActionsItem) {
		if (this.twoFactorRequiredActions == null) {
			this.twoFactorRequiredActions = new HashMap<String, Boolean>();
		}
		this.twoFactorRequiredActions.put(key, twoFactorRequiredActionsItem);
		return this;
	}

	public Map<String, Boolean> getTwoFactorRequiredActions() {
		return twoFactorRequiredActions;
	}

	public void setTwoFactorRequiredActions(Map<String, Boolean> twoFactorRequiredActions) {
		this.twoFactorRequiredActions = twoFactorRequiredActions;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AsApiUPGuiConfig asApiUPGuiConfig = (AsApiUPGuiConfig) o;
		return Objects.equals(this.enableCaptcha, asApiUPGuiConfig.enableCaptcha) && Objects.equals(this.title, asApiUPGuiConfig.title)
				&& Objects.equals(this.notificationType, asApiUPGuiConfig.notificationType) && Objects.equals(this.visibleViews, asApiUPGuiConfig.visibleViews)
				&& Objects.equals(this.twoFactorRequiredViews, asApiUPGuiConfig.twoFactorRequiredViews)
				&& Objects.equals(this.visibleActions, asApiUPGuiConfig.visibleActions)
				&& Objects.equals(this.twoFactorRequiredActions, asApiUPGuiConfig.twoFactorRequiredActions)
				&& Objects.equals(this.enableAutoComplete, asApiUPGuiConfig.enableAutoComplete);
	}

	@Override
	public int hashCode() {
		return Objects.hash(enableCaptcha, enableAutoComplete, title, notificationType, visibleViews, twoFactorRequiredViews, visibleActions,
				twoFactorRequiredActions);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AsApiUPGuiConfig {\n");
		sb.append("    enableCaptcha: ").append(toIndentedString(enableCaptcha)).append("\n");
		sb.append("    enableAutoComplete: ").append(toIndentedString(enableAutoComplete)).append("\n");
		sb.append("    title: ").append(toIndentedString(title)).append("\n");
		sb.append("    notificationType: ").append(toIndentedString(notificationType)).append("\n");
		sb.append("    visibleViews: ").append(toIndentedString(visibleViews)).append("\n");
		sb.append("    twoFactorRequiredViews: ").append(toIndentedString(twoFactorRequiredViews)).append("\n");
		sb.append("    visibleActions: ").append(toIndentedString(visibleActions)).append("\n");
		sb.append("    twoFactorRequiredActions: ").append(toIndentedString(twoFactorRequiredActions)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

}
