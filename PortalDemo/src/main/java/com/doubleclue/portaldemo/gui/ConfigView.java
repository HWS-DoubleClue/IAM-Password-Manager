package com.doubleclue.portaldemo.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.as.restapi.AsClientRestApi;
import com.doubleclue.portaldemo.AbstractPortalView;
import com.doubleclue.portaldemo.PortalDemoConfig;
import com.doubleclue.portaldemo.utils.ConfigUtil;
import com.doubleclue.portaldemo.utils.JsfUtils;

@SuppressWarnings("serial")
@Named("configView")
@SessionScoped
public class ConfigView extends AbstractPortalView {

	// @ManagedProperty(value = "#{portalSessionBean}")
	// private PortalSessionBean portalSessionBean;

	PortalDemoConfig config;

	public String actionSave() {
		ConfigUtil.setPortalDemoConfig(config);
		AsClientRestApi.getInstance().getApiClient().setConnectionConfig(config.getConnectionConfig());
		AsClientRestApi.initilize(config.getConnectionConfig());
		return "portalLogin.xhtml";
	}

	@Override
	public String getPath() {
		return "config.xhtml";
	}

	public PortalDemoConfig getConfig() {
		if (config == null) {
			try {
				config = ConfigUtil.getPortalDemoConfig();
			} catch (Exception e) {
				JsfUtils.addErrorMessage("Corrupted PortalDemoConfig.json");
			}
		}
		return config;
	}

	public void setConfig(PortalDemoConfig config) {
		this.config = config;
	}

	public String getConfigFile() {
		try {
			return ConfigUtil.getConfigurationFile().getAbsolutePath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
