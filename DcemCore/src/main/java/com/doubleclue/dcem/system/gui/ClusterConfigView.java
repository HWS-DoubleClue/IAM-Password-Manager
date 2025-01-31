package com.doubleclue.dcem.system.gui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.subjects.ClusterConfigSubject;

@SuppressWarnings("serial")
@Named("clusterConfigView")
@SessionScoped
public class ClusterConfigView extends DcemView {

	private static final Logger logger = LogManager.getLogger(ClusterConfigView.class);

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private ConfigLogic configLogic;

	@Inject
	private ClusterConfigSubject clusterConfigSubject;

	@Inject
	AuditingLogic auditingLogic;

	private ClusterConfig config;
	private ConnectionService selectedService;
	private ConnectionService editSelectedService;

	private static final String WV_SERVICE_CONNECTION_DIALOG = "serviceConnectionDialog";

	@PostConstruct
	private void init() {
		subject = clusterConfigSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		addAutoViewAction(DcemConstants.ACTION_SAVE, resourceBundle, null, null);
	}

	@Override
	public void reload() {
		try {
			config = configLogic.getClusterConfig();
		} catch (DcemException exp) {
			logger.error("Couldn't read Cluster Configuration", exp);
			JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "clusterConfig.save.warning.read", exp.toString());
		}
		if (config == null) {
			config = new ClusterConfig();
			config.setDefault();
		}
		if (config.getDcemHostDomainName() == null) {
			try {
				URL url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
				config.setDcemHostDomainName(url.getHost());
			} catch (MalformedURLException e) {
				logger.error("Couldn't read Cluster Configuration", e);
			}
		}
		autoViewBean.reload();
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public void actionEdit() {
		if (selectedService == null) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "clusterConfig.selectRow");
		} else {
			try {
				editSelectedService = (ConnectionService) selectedService.clone();
				if (selectedService.getSameAsConnectionServiceType() != null) {
					setSameAs(selectedService.getSameAsConnectionServiceType().name());
				}
				showDialog(WV_SERVICE_CONNECTION_DIALOG);
			} catch (CloneNotSupportedException e) {
				JsfUtils.addErrorMessage("Couldn't Clone ConnectionService");
			}
		}
	}

	public void actionEditOk() {
		if (editSelectedService.sameAsConnectionServiceType == null) {
			if (editSelectedService.getPort() == null || editSelectedService.getPort() < 1) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "clusterConfig.enteredPort.error");
				return;
			}
		}

		selectedService.setPort(editSelectedService.getPort());
		selectedService.setExternalPort(editSelectedService.getExternalPort());
		selectedService.setSameAsConnectionServiceType(editSelectedService.getSameAsConnectionServiceType());
		selectedService.setEnabled(editSelectedService.isEnabled());
		selectedService.setSecure(editSelectedService.isSecure());
		hideDialog(WV_SERVICE_CONNECTION_DIALOG);
	}

	public String actionSave() {

		String redirectPort = config.getRedirectPort80();
		if (redirectPort != null) {
			redirectPort = redirectPort.trim();
			if (redirectPort.isEmpty() == false) {
				try {
					Integer.parseInt(redirectPort);
				} catch (Exception e) {
					JsfUtils.addErrorMessage("Invalid Redirect Port");
					return null;
				}
			}
			config.setRedirectPort80(redirectPort);
		}
		
		try {
			dcemApplication.validateDcemUrls(config.getDcemHostDomainName());
		} catch (Exception e1) {
			JsfUtils.addErrorMessage("Invalid URL Format");
			return null;
		}

		try {
			
			DcemConfiguration dcemConfiguration = configLogic.createClusterConfig(config);
			configLogic.setDcemConfiguration(dcemConfiguration);
			auditingLogic.addAudit(new DcemAction(subject, DcemConstants.ACTION_SAVE), config.toString());
			editSelectedService = null;
			selectedService = null;
			showDialog("restart");
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "clusterConfig.save.error", e.getMessage());
		} catch (Exception e) {
			logger.warn("Action Save Configuration", e);
			JsfUtils.addErrorMessage("Somethoing went wrong. Please see Logfiles." + e.toString());
		}
		return null;
	}

	public List<SelectItem> getSameAsServices() {
		List<SelectItem> list = new ArrayList<>();
		if (selectedService != null) {
			for (ConnectionService connectionService : getConnectionServices()) {
				if (connectionService.getConnectionServicesType() != selectedService.getConnectionServicesType()
						&& connectionService.getSameAsConnectionServiceType() == null) {
					list.add(new SelectItem(connectionService.getConnectionServicesType().name(),
							connectionService.getConnectionServicesType().displayName));
				}
			}
		}
		return list;
	}

	public boolean isPermissionSave() {
		return operatorSessionBean.isPermission(new DcemAction(subject, DcemConstants.ACTION_SAVE));
	}

	public ClusterConfig getConfig() {
		return config;
	}

	public void setConfig(ClusterConfig config) {
		this.config = config;
	}

	public List<ConnectionService> getConnectionServices() {
		return config.getConnectionServices();
	}

	public ConnectionService getSelectedService() {
		return selectedService;
	}

	public void setSelectedService(ConnectionService selectedService) {
		this.selectedService = selectedService;
	}

	public String getSameAs() {
		return editSelectedService != null && editSelectedService.getSameAsConnectionServiceType() != null
				? editSelectedService.getSameAsConnectionServiceType().name()
				: null;
	}

	public void setSameAs(String sameAs) {
		ConnectionService toCopy = null;
		if (isNullOrEmpty(sameAs)) {
			editSelectedService.setSameAsConnectionServiceType(null);
			toCopy = selectedService;
		} else {
			ConnectionServicesType type = ConnectionServicesType.valueOf(sameAs);
			editSelectedService.setSameAsConnectionServiceType(type);
			for (ConnectionService connectionService : getConnectionServices()) {
				if (connectionService.getConnectionServicesType() == type) {
					toCopy = connectionService;
					break;
				}
			}
		}
		if (toCopy != null) {
			editSelectedService.setEnabled(toCopy.isEnabled());
			editSelectedService.setPort(toCopy.getPort());
			editSelectedService.setSecure(toCopy.isSecure());
		}
	}

	public ConnectionService getEditSelectedService() {
		return editSelectedService;
	}

	public void setEditSelectedService(ConnectionService editSelectedService) {
		this.editSelectedService = editSelectedService;
	}

	public void leavingView() {
		editSelectedService = null;
		selectedService = null;
	}
}
