package com.doubleclue.dcem.core.config;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConnectionService implements Cloneable {

	private static ResourceBundle resourceBundle;

	public ConnectionServicesType connectionServicesType;
	public String name;
	public Integer port;
	public boolean secure;
	public boolean enabled;
	public Integer externalPort;

	public ConnectionServicesType sameAsConnectionServiceType;

	public ConnectionService() {
	}

	public ConnectionService(ConnectionServicesType connectionServicesType) {
		this.connectionServicesType = connectionServicesType;
		this.name = connectionServicesType.displayName;
		this.port = connectionServicesType.port;
		this.secure = connectionServicesType.secure;
		this.enabled = connectionServicesType.enabled;
		sameAsConnectionServiceType = connectionServicesType.sameAsConnectionServiceType;
	}

	public ConnectionService(ConnectionServicesType connectionServicesType, String name, int port, boolean secure, boolean enabled,
			ConnectionServicesType samAsConnectionServicesType) {
		this.connectionServicesType = connectionServicesType;
		this.name = name;
		this.port = port;
		this.secure = secure;
		this.enabled = enabled;
		this.sameAsConnectionServiceType = samAsConnectionServicesType;
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public ConnectionServicesType getConnectionServicesType() {
		return connectionServicesType;
	}

	public void setConnectionServicesType(ConnectionServicesType connectionServicesType) {
		this.connectionServicesType = connectionServicesType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getPort() {
		if (sameAsConnectionServiceType != null) {
			return null;
		}
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public boolean isSecure() {
		return secure;
	}

	@JsonIgnore
	public boolean isSecureInherit() {
		if (sameAsConnectionServiceType != null) {
			return sameAsConnectionServiceType.secure;
		}
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isEnabled() {
		return enabled;
	}

	@JsonIgnore
	public boolean isEnabledInherit() {
		if (sameAsConnectionServiceType != null) {
			return sameAsConnectionServiceType.enabled;
		}
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ConnectionServicesType getSameAsConnectionServiceType() {
		return sameAsConnectionServiceType;
	}

	public void setSameAsConnectionServiceType(ConnectionServicesType sameAsConnectionServiceType) {
		this.sameAsConnectionServiceType = sameAsConnectionServiceType;
	}

	@JsonIgnore
	public String getPortText() {
		return (sameAsConnectionServiceType != null) ? getSameAsText()
				: (port != null) ? Integer.toString(port) : getResourceBundle().getString("clusterConfig.connectionService.notSet");
	}

	@JsonIgnore
	public String getEnabledText() {
		return (sameAsConnectionServiceType != null) ? "”" : getBooleanText(enabled);
	}

	@JsonIgnore
	public String getSecureText() {
		return (sameAsConnectionServiceType != null) ? "”" : getBooleanText(secure);
	}

	@JsonIgnore
	private static ResourceBundle getResourceBundle() {
		if (resourceBundle == null) {
			resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		}
		return resourceBundle;
	}

	@JsonIgnore
	private String getSameAsText() {
		return MessageFormat.format(getResourceBundle().getString("clusterConfig.connectionService.sameAs"), sameAsConnectionServiceType.getDisplayName());
	}

	@JsonIgnore
	private static String getBooleanText(boolean b) {
		return getResourceBundle().getString("clusterConfig.connectionService." + (b ? "yes" : "no"));
	}

	@JsonIgnore
	public boolean isPortInUse() {
		int effectivePort = (sameAsConnectionServiceType != null) ? sameAsConnectionServiceType.getPort() : port;
		return ConfigLogic.portsInUse.contains(effectivePort);
	}

	public Integer getExternalPort() {
		return externalPort;
	}

	public void setExternalPort(Integer externalPort) {
		this.externalPort = externalPort;
	}

	@JsonIgnore
	public String getExternalPortAsString() {
		int portTemp = port;
		if (externalPort != null) {
			portTemp = getExternalPort();
		}
		if (portTemp != 443) {
			return ":" + portTemp;
		}
		return "";
	}
	
	@JsonIgnore
	@Override
	public String toString() {
		return "ConnectionService [connectionServicesType=" + connectionServicesType + ", name=" + name + ", port=" + port + ", secure=" + secure + ", enabled="
				+ enabled + ", externalPort=" + externalPort + ", sameAsConnectionServiceType=" + sameAsConnectionServiceType + "]";
	}
}
