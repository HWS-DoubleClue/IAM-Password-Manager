package com.doubleclue.dcem.as.gui;

import java.util.ArrayList;
import java.util.List;
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
import com.doubleclue.dcem.subjects.AsVersionSubject;

@SuppressWarnings("serial")
@Named("asVersionView")
@SessionScoped
public class AsVersionView extends DcemView {



	@Inject
	private AsVersionSubject versionSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private VersionDialog versionDialog;
	
	@Inject
	private GenerateSdkConfigDialog generateSdkConfigDialog;

	

	List<String> dialogs = new ArrayList<String>(1);

	@PostConstruct
	private void init() {
		versionDialog.setParentView(this);
		subject = versionSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);
		addAutoViewAction( DcemConstants.ACTION_ADD,  resourceBundle, versionDialog, AsConstants.VERSION_DIALOG);
		addAutoViewAction( DcemConstants.ACTION_EDIT,  resourceBundle, versionDialog, AsConstants.VERSION_DIALOG);
		addAutoViewAction( DcemConstants.ACTION_DELETE,  resourceBundle, versionDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		
		addAutoViewAction( AsConstants.ACTION_GENERATE_SDK_CONFIG,  asResourceBundle, generateSdkConfigDialog, AsConstants.GENERATE_SDK_CONFIG_DIALOG);



	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

	
	
}
