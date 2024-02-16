package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.subjects.TextResourceSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;

@Named("textResourceView")
@SessionScoped
public class TextResourceView extends DcemView {

	// @Inject
	// private TextResourceDialog textResourceDialog;

//	private static final Logger logger = LogManager.getLogger(TextResourceView.class);


	@Inject
	AuditingLogic auditingLogic;

	@Inject
	TextResourceSubject textResourceSubject;

	@Inject
	private TextResourceDialog textResourceDialog;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	protected void init() {
		// DcemModule dcemModule = viewNavigator.getActiveModule();
//		subject = textResourceSubject;
		reload();
		textResourceDialog.setParentView(this);

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, textResourceDialog, DcemConstants.TEXT_RESOURCE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_COPY, resourceBundle, textResourceDialog, DcemConstants.TEXT_RESOURCE_DIALOG);

		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, textResourceDialog, DcemConstants.TEXT_RESOURCE_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, textResourceDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_UPLOAD, resourceBundle, textResourceDialog, DcemConstants.TEXT_RESOURCE_UPLOAD_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DOWNLOAD, resourceBundle, textResourceDialog, DcemConstants.TEXT_RESOURCE_DOWNLOAD_DIALOG);


	}
	
	@Override
	public void reload() {
		DcemModule dcemModule = viewNavigator.getActiveModule();
		subject = dcemApplication.getSubjectByName(dcemModule.getId(), "TextResource");
	}




}
