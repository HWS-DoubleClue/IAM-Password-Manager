package com.doubleclue.dcem.saml.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.subjects.SpMetadataSubject;

@SuppressWarnings("serial")
@Named("spMetadataView")
@SessionScoped
public class SpMetadataView extends DcemView {

	@Inject
	private SpMetadataSubject spMetadataSubject;

	@Inject
	private SpMetadataDialog spMetadataDialog;

	@Inject
	private DownloadIdpMetadataDialog downloadIdpMetadataDialog;

	@Inject
	private ConfigLogic configLogic;

	@PostConstruct
	private void init() {

		spMetadataDialog.setParentView(this);
		subject = spMetadataSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle resourceBundleSaml = JsfUtils.getBundle(SamlModule.RESOURCE_NAME);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, spMetadataDialog, SamlConstants.DIALOG_SP_METADATA);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, spMetadataDialog, SamlConstants.DIALOG_SP_METADATA);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, spMetadataDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(SamlConstants.ACTION_DOWNLOAD_IDP_METADATA, resourceBundleSaml, downloadIdpMetadataDialog, SamlConstants.DIALOG_IDP_METADATA);

		try {
			if (TenantIdResolver.isCurrentTenantMaster()
					&& configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.SAML).isEnabled() == false) {
				JsfUtils.addErrorMessage(resourceBundleSaml.getString("spMetadataView.error.samlNotEnabled"));
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getMessage());
		} catch (NullPointerException e) {
			JsfUtils.addErrorMessage(resourceBundleSaml.getString("spMetadataView.error.samlNotConfigured"));
		}
	}
}
