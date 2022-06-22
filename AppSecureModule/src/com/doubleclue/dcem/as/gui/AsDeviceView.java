package com.doubleclue.dcem.as.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.AsCloudSafeSubject;
import com.doubleclue.dcem.subjects.AsDeviceSubject;

@SuppressWarnings("serial")
@Named("asDeviceView")
@SessionScoped
public class AsDeviceView extends DcemView {

	@Inject
	private AsDeviceSubject appDeviceSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private AsDeviceDialog asDeviceDialog;

	@Inject
	private AsCloudSafeSubject cloudDataSubject;

	List<String> dialogs = new ArrayList<String>(1);

	@PostConstruct
	private void init() {

		asDeviceDialog.setParentView(this);

		subject = appDeviceSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle resourceBundleAs = JsfUtils.getBundle(AsModule.RESOURCE_NAME);

		addAutoViewAction(DcemConstants.ACTION_ENABLE, resourceBundle, asDeviceDialog, null);
		addAutoViewAction(DcemConstants.ACTION_DISABLE, resourceBundle, asDeviceDialog, null);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, asDeviceDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		
		addAutoViewAction(DcemConstants.ACTION_SHOW_PN_TOKEN, resourceBundleAs, asDeviceDialog, null);


//		ViewLink viewLink = new ViewLink(cloudDataSubject, "name", "device.name");
//		addAutoViewAction(AsConstants.ACTION_GOTO_CLOUDDATA, resourceBundleAs, null, null, viewLink);
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
