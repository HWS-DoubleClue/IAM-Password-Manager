package com.doubleclue.portaldemo.utils;

import java.io.File;

import com.doubleclue.portaldemo.PortalDemoConfig;
import com.doubleclue.portaldemo.boot.LocalPathsPortalDemo;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigUtil {

	static public final String PORTAL_DIR = "PortalDemo";
	static public final String PORTAL_CONFIG_FILE = "PortalConfig.json";
	PortalDemoConfig portalDemoConfig = null;
	
	

	static File homeDir;

	public static File getConfigurationFile() throws Exception {
		return new File(LocalPathsPortalDemo.getDcemHomeDir(), PORTAL_CONFIG_FILE);
	}

	public static PortalDemoConfig getPortalDemoConfig() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		PortalDemoConfig portalDemoConfig;
		if (getConfigurationFile().exists()) {
			try {
				portalDemoConfig = objectMapper.readValue(getConfigurationFile(), PortalDemoConfig.class);
			} catch (Exception e) {
				throw new Exception(e);
			}
		} else {
			portalDemoConfig = new PortalDemoConfig();
			setPortalDemoConfig(portalDemoConfig);
		}
		return portalDemoConfig;
	}

	public static void setPortalDemoConfig(PortalDemoConfig portalDemoConfig) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			objectMapper.writeValue(getConfigurationFile(), portalDemoConfig);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}

}
