package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayOutputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.parser.MediaType;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.DomainType;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;

@SuppressWarnings("serial")
@Named("domainDialog")
@SessionScoped
public class DomainDialogBean extends DcemDialog {

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	AutoDialogBean autoDialogBean;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	SystemModule systemModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	private static final Logger logger = LogManager.getLogger(DomainDialogBean.class);
	private String domainType = null;
	private String azureRedirectUrl = null;

	public DomainDialogBean() {
	}

//	public void actionDownloadCertificates() {
//		DomainEntity domainEntity = (DomainEntity) getActionObject();
//		String host = domainEntity.getHost();
//		int ind = host.indexOf(DcemConstants.LDAP_URL_TYPE);
//		if (ind == -1) {
//			JsfUtils.addErrorMessage("The URL must start with 'ldaps://'");
//			return;
//		}
//		host = host.substring(ind + DcemConstants.LDAP_URL_TYPE.length());
//		try {
//			X509Certificate[] chain = SecureServerUtils.getCertificates(host, 636, systemModule.getPreferences().getHttpProxyHost(),
//					systemModule.getPreferences().getHttpProxyPort());
//			ByteArrayOutputStream bos = SecureServerUtils.convertChainToPem(chain);
//			JsfUtils.downloadFile("text/html", "Ldap-AD-Certificates.pem", bos.toByteArray());
//		} catch (Exception exp) {
//			logger.warn("Couldn 't downlaod AD Certificates", exp);
//			JsfUtils.addErrorMessage(exp.toString());
//			return;
//		}
//	}

	@Override
	public boolean actionOk() throws Exception {
		DomainEntity domainEntity = (DomainEntity) getActionObject();
		DomainType domainTypeObj = DomainType.valueOf(domainType);
		boolean isAzure = domainTypeObj == DomainType.Azure_AD;
		domainEntity.setDomainType(domainTypeObj);
		if (domainEntity.isEnable()) {
			try {
				domainLogic.testDomainConnection(domainEntity);
			} catch (DcemException exp) {
				logger.info(exp);
				if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "ldap.connection.failed");
					JsfUtils.addWarnMessage(exp.toString());
				} else if (exp.getErrorCode() == DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "ldap.login.searchAccount.failed");
				} else if (exp.getErrorCode() == DcemErrorCodes.AZURE_DOMAIN_AUTHENTICATION_ERROR) {
					JsfUtils.addErrorMessage(exp.getMessage());
				} else {
					JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				}
				return false;
			} catch (Exception exp) {
				JsfUtils.addErrorMessage(exp.toString());
				return false;
			}
		}
		if (isAzure) {
			domainEntity.setBaseDN("");
		}
		domainLogic.addOrUpdateDcemLdap(domainEntity, this.getAutoViewAction().getDcemAction());
		Exception exception = DcemUtils.reloadTaskNodes(DomainLogic.class);
		if (exception != null) {
			throw exception;
		}
		return true;
	}

	@Override
	public void actionConfirm() {
		domainLogic.deleteLdapEntity((DomainEntity) getActionObject());
		return;
	}

	@Override
	public String getWidth() {
		return "840";
	}

	@Override
	public String getHeight() {
		return "760";
	}

	public List<SelectItem> getDomainTypes() {
		ArrayList<SelectItem> items = new ArrayList<>();
		items.add(new SelectItem(DomainType.Active_Directory, "Active-Directory"));
		items.add(new SelectItem(DomainType.Azure_AD, "Azure Active-Directory"));
		items.add(new SelectItem(DomainType.Generic_LDAP, "Generic LDAP"));
		return items;
	}

	public String getDomainType() {
		return domainType;
	}

	public void setDomainType(String domainType) {
		this.domainType = domainType;
	}

	public String getAzureRedirectUrl() {
		return azureRedirectUrl;
	}

	public List<String> getAuthConnectors() {
		AsModuleApi asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		return asModuleApi.getAuthConnectorNames();
	}

	public void changeType() {
		DomainEntity domainEntity = (DomainEntity) getActionObject();
		DomainType currentDomainType = DomainType.valueOf(domainType);
		switch (currentDomainType) {
		case Azure_AD:
			domainEntity.setHost("");
			break;
		default:
			domainEntity.setHost("ldap://");
			break;
		}
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		DomainEntity domainEntity = (DomainEntity) getActionObject();
		domainType = domainEntity.getDomainType().name();
		domainEntity.getDomainConfig();
		azureRedirectUrl = null;
	}

	public boolean isRenderAzureAuthPanel() {
		return azureRedirectUrl != null;
	}
}
