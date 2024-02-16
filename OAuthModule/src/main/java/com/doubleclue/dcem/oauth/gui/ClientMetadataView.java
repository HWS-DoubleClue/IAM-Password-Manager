package com.doubleclue.dcem.oauth.gui;

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
import com.doubleclue.dcem.oauth.logic.OAuthModule;
import com.doubleclue.dcem.oauth.logic.OAuthModuleConstants;
import com.doubleclue.dcem.oauth.subjects.ClientMetadataSubject;

@SuppressWarnings("serial")
@Named("clientMetadataView")
@SessionScoped
public class ClientMetadataView extends DcemView {

	@Inject
	private ClientMetadataSubject clientMetadataSubject;

	@Inject
	private ClientMetadataDialog clientMetadataDialog;

	@Inject
	private ConfigLogic configLogic;

	@PostConstruct
	private void init() {

		clientMetadataDialog.setParentView(this);
		subject = clientMetadataSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());
		ResourceBundle resourceBundleOAuth = JsfUtils.getBundle(OAuthModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, clientMetadataDialog, OAuthModuleConstants.DIALOG_CLIENT_METADATA);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, clientMetadataDialog, OAuthModuleConstants.DIALOG_CLIENT_METADATA);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, clientMetadataDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

		try {
			if (TenantIdResolver.isCurrentTenantMaster()
					&& configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.OPENN_ID_OAUTH).isEnabled() == false) {
				JsfUtils.addErrorMessage(resourceBundleOAuth.getString("clientMetadataView.error.oauthNotEnabled"));
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getMessage());
		} catch (NullPointerException e) {
			JsfUtils.addErrorMessage(resourceBundleOAuth.getString("clientMetadataView.error.oauthNotConfigured"));
		}
	}
}
