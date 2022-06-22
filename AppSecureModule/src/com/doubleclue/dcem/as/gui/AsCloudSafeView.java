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
import com.doubleclue.dcem.subjects.AsCloudSafeSubject;

@SuppressWarnings("serial")
@Named("asCloudSafeView")
@SessionScoped
public class AsCloudSafeView extends DcemView {

	@Inject
	private AsCloudSafeSubject cloudSafeSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private AsCloudSafeShowFilesDialog showFilesDialog;

	@Inject
	private AsCloudSafeSetLimitsDialog setLimitsDialog;

	@PostConstruct
	private void init() {

		subject = cloudSafeSubject;

		showFilesDialog.setParentView(this);
		setLimitsDialog.setParentView(this);

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, setLimitsDialog, AsConstants.PATH_SET_CLOUD_SAFE_LIMITS);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, setLimitsDialog, AsConstants.PATH_SET_CLOUD_SAFE_LIMITS);
		addAutoViewAction(AsConstants.ACTION_SHOW_CLOUD_SAFE_FILES, asResourceBundle, showFilesDialog, AsConstants.PATH_SHOW_CLOUD_SAFE_FILES);
		addAutoViewAction(AsConstants.ACTION_SHOW_RECOVERY_KEY, asResourceBundle, showFilesDialog, AsConstants.RECOVERY_KEY_DIALOG);
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
