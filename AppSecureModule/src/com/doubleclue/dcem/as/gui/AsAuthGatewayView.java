package com.doubleclue.dcem.as.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.AsAuthGatewaySubject;

@SuppressWarnings("serial")
@Named("asAuthGatewayView")
@SessionScoped
public class AsAuthGatewayView extends DcemView {


	@Inject
	private AsAuthGatewaySubject authGatewaySubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private AuthGatewayDialog authAppDialog;
	
	@Inject
	ActiveAuthGatewayDialog activeAuthGatewayDialog;

	@PostConstruct
	private void init() {

		authAppDialog.setParentView(this);
		activeAuthGatewayDialog.setParentView(this);

		subject = authGatewaySubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		
		ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, authAppDialog, null);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, authAppDialog, null);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, authAppDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		
		addAutoViewAction(DcemConstants.ACTION_DOWNLOAD, resourceBundle, authAppDialog, null);
		
		addAutoViewAction(AsConstants.ACTION_ACTIVE_AUTH_GATEWAY, asResourceBundle, activeAuthGatewayDialog, AsConstants.ACTIVE_AUTH_GATEWAY_DIALOG);


//		addAutoViewAction(Constants.ACTION_SHOW, resourceBundle, templateDialog, AsConst.TEMPLATE_DIALOG);

	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

}
