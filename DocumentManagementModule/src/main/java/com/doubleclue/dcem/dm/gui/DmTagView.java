package com.doubleclue.dcem.dm.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.GenericDcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.subjects.DmTagSubject;

@SuppressWarnings("serial")
@Named("dmTagView")
@SessionScoped
public class DmTagView extends DcemView {

	@Inject
	DmTagSubject dmTagSubject;

	@Inject
	DmTagDialog dmTagDialog;
	
	@Inject
	DmDocumentView dmDocumentView;

	@PostConstruct
	public void init() {
		subject = dmTagSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DocumentManagementModule.RESOURCE_NAME, operatorSessionBean.getLocale());

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, dmTagDialog, DmConstants.DM_TAG_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, dmTagDialog, DmConstants.DM_TAG_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, dmTagDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

		addAutoViewAction(DmConstants.SHOW_DOCUMENT_WITH_TAG, resourceBundle, null, null);
	}
	
	public void showDocumentsWithTag() {
		CloudSafeTagEntity cloudSafeTagEntity = (CloudSafeTagEntity) this.getActionObject();
		viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmDocumentView.getSubject().getViewName());
		dmDocumentView.showDocumentWithTag(cloudSafeTagEntity);
	}

}
