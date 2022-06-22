package com.doubleclue.dcem.as.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.AsFidoAuthenticatorSubject;

@Named("asFidoAuthenticatorView")
@SessionScoped
public class AsFidoAuthenticatorView extends DcemView {

	private static final long serialVersionUID = 1L;

	@Inject
	AsFidoAuthenticatorSubject fidoSubject;

	@Inject
	AsFidoAuthenticatorDialog fidoDialog;

	@PostConstruct
	private void init() {
		subject = fidoSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, fidoDialog, AsConstants.FIDO_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, fidoDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
