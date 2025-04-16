package com.doubleclue.dcem.ps.logic;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.doubleclue.dcem.core.utils.JsonConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class AppHubApplication implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("u")
	public String url;

	@JsonProperty("n")
	public String name;

	@JsonProperty("a")
	public List<AppHubAction> actions = new ArrayList<>();

	public AppHubApplication(String url, String name, List<AppHubAction> actions) {
		super();
		this.url = url;
		this.name = name;
		this.actions = actions;
	}

	public AppHubApplication() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AppHubAction> getActions() {
		return actions;
	}

	public void setActions(List<AppHubAction> actions) {
		this.actions = actions;
	}

	@JsonIgnore
	public String getApplicationJson() throws JsonParseException, JsonMappingException, IOException {
		if (actions == null) {
			actions = new ArrayList<>();
		}
		return JsonConverter.serializeObject(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((actions == null) ? 0 : actions.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AppHubApplication other = (AppHubApplication) obj;
		if (actions == null) {
			if (other.actions != null)
				return false;
		} else if (!actions.equals(other.actions))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public void updateAction(AppHubAction action, AppHubAction oldAction) {
		int index = actions.indexOf(oldAction);
		actions.set(index, action);
	}
	
}