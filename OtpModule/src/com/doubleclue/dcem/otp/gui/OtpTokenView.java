package com.doubleclue.dcem.otp.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.otp.logic.OtpConstants;
import com.doubleclue.dcem.otp.subjects.OtpTokenSubject;

@Named("otpTokenView")
@SessionScoped
public class OtpTokenView extends DcemView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private OtpTokenSubject otpTokenSubject;

//	@Inject
//	private GenericDcemDialog genericDcemDialog;
	
	@Inject
	private OtpTokenDialog otpTokenDialog;

	@PostConstruct
	private void init() {

		otpTokenDialog.setParentView(this);
		subject = otpTokenSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);

		addAutoViewAction(DcemConstants.ACTION_IMPORT, resourceBundle, otpTokenDialog, OtpConstants.IMPORT_TOKENS_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, otpTokenDialog, OtpConstants.EDIT_TOKENS_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, otpTokenDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

	}

}
