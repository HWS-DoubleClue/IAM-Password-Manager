package com.doubleclue.dcem.radius.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.radius.logic.RadiusConstants;
import com.doubleclue.dcem.radius.subjects.RadiusClientSubject;

@SuppressWarnings("serial")
@Named("radiusClientView")
@SessionScoped
public class RadiusClientView extends DcemView {

	@Inject
	private RadiusClientSubject radiusClientSubject;

	@Inject
	private RadiusClientDialog radiusClientDialog;

	@PostConstruct
	private void init() {

		radiusClientDialog.setParentView(this);
		subject = radiusClientSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, radiusClientDialog, RadiusConstants.CLIENT_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, radiusClientDialog, RadiusConstants.CLIENT_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, radiusClientDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

}
