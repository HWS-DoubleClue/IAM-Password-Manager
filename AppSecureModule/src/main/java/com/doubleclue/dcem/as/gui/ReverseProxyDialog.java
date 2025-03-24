package com.doubleclue.dcem.as.gui;

import java.io.ByteArrayInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.as.comm.client.ProxyCommClient;
import com.doubleclue.dcem.as.comm.client.ReverseProxyProperties;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.RpConfig;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.utils.KaraUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Named("reverseProxyDialog")
@SessionScoped
public class ReverseProxyDialog extends DcemDialog {

	@Inject
	AsModule asModule;

	@Inject
	ProxyCommClient proxyCommClient;

	@Inject
	ConfigLogic configLogic;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private UploadedFile reverseProxyFile;

	RpConfig rpConfig;

	DcemConfiguration dcemConfiguration;

	// ResourceBundle asResourceBundle;

	@PostConstruct
	private void init() {

	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) {
		try {
			dcemConfiguration = configLogic.getDcemConfiguration(asModule.getId(), AsConstants.RP_CONFIG_KEY);
			if (dcemConfiguration == null) {
				rpConfig = new RpConfig();
			} else {
				rpConfig = new ObjectMapper().readValue(dcemConfiguration.getValue(), RpConfig.class);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			rpConfig = new RpConfig();
		}
	}

	@Override
	public void leavingDialog() {
		rpConfig = null;
		dcemConfiguration = null;
		reverseProxyFile = null;
	}

	public boolean actionOk() throws Exception {
		configLogic.setDcemConfiguration(dcemConfiguration);
		asModule.updateReverseProxy(true, rpConfig);
		return true;
	}

	public void validateOk() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		if (rpConfig.isEnableRp() == false) {
			rpConfig.setDomainName(null);
			rpConfig.setPassword(null);
			rpConfig.setReverseProxyProperties(null);
			rpConfig.setSdkConfigContent(null);
			auditingLogic.addAudit(getAutoViewAction().getDcemAction(), AsConstants.RP_CONFIG_DISABLED);
		} else {
			if (rpConfig.getPassword() == null || rpConfig.getPassword().trim().isEmpty()) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.pwempty");
				return;
			}
			if (rpConfig.getReconnect() < 1) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.rclt");
				return;
			}

			if ((reverseProxyFile == null || reverseProxyFile.getSize() == 0) && rpConfig.getReverseProxyProperties() == null) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.dcemfile");
				return;
			}

			if (reverseProxyFile != null && reverseProxyFile.getSize() > 0) {

				ByteArrayInputStream baip = new ByteArrayInputStream(reverseProxyFile.getContent());
				ZipInputStream zipInputStream = new ZipInputStream(baip);
				ZipEntry zipEntry;
				byte[] sdkConfigContent = null;
				byte[] reverseProxyPropertiesContent = null;
				ReverseProxyProperties reverseProxyProperties = null;
				try {
					while ((zipEntry = zipInputStream.getNextEntry()) != null) {
						if (zipEntry.getName().equals(AppSystemConstants.SdkConfigFileName)) {
							sdkConfigContent = KaraUtils.readInputStream(zipInputStream);
						} else if (zipEntry.getName().equals(AsConstants.DCEM_REVERSE_PROXY_PROPERTY_FILE)) {
							reverseProxyPropertiesContent = KaraUtils.readInputStream(zipInputStream);
						}
					}
					zipInputStream.close();
					if (sdkConfigContent == null || reverseProxyPropertiesContent == null) {
						throw new Exception("Missing components");
					}

					try {
						reverseProxyProperties = objectMapper.readValue(reverseProxyPropertiesContent, ReverseProxyProperties.class);
					} catch (Exception e) {
						reverseProxyFile = null;
						JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.valErrRp");
						return;
					}
					rpConfig.setReverseProxyProperties(reverseProxyProperties);
					rpConfig.setDomainName(reverseProxyProperties.getDomainName());
				} catch (Exception e) {
					reverseProxyFile = null;
					JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.valErrRp");
					return;
				}

				try {
					KaraUtils.parseSdkConfig(sdkConfigContent); // make validation
				} catch (Exception e) {
					reverseProxyFile = null;
					JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "rpMessage.valErrSdk");
					return;
				}
				rpConfig.setSdkConfigContent(sdkConfigContent);
			}
			auditingLogic.addAudit(getAutoViewAction().getDcemAction(), AsConstants.RP_CONFIG_ENABLED);
		}
		if (dcemConfiguration == null) {
			dcemConfiguration = new DcemConfiguration();
			dcemConfiguration.setModuleId(asModule.getId());
			dcemConfiguration.setKey(AsConstants.RP_CONFIG_KEY);
		}
		dcemConfiguration.setValue(objectMapper.writeValueAsBytes(rpConfig));
		PrimeFaces.current().executeScript("PF('confirm').show();");
		return;
	}

	public void clear() {
		proxyCommClient.clearReportList();
	}

	public ProxyCommClient getProxyCommClient() {
		return proxyCommClient;
	}

	public void setProxyCommClient(ProxyCommClient proxyCommClient) {
		this.proxyCommClient = proxyCommClient;
	}

	public RpConfig getRpConfig() {
		return rpConfig;
	}

	public void setRpConfig(RpConfig rpConfig) {
		this.rpConfig = rpConfig;
	}

	public UploadedFile getReverseProxyFile() {
		return reverseProxyFile;
	}

	public void setReverseProxyFile(UploadedFile reverseProxyFile) {
		this.reverseProxyFile = reverseProxyFile;
	}

}
