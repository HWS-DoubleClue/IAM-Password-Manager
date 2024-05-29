package com.doubleclue.dcem.as.gui;

import java.time.LocalDateTime;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.AsActivationSubject;

@SuppressWarnings("serial")
@Named("asActivationView")
@SessionScoped
public class ActivationViewBean extends DcemView {


	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private AsModule asModule;

	@Inject
	private AsActivationDialogBean activationDialogBean;

	@Inject
	private AsActivationSubject activationSubject;

	String userLoginId;

	DcemUser dcemUser;
 
	@PostConstruct
	private void init() {

		subject = activationSubject;
		activationDialogBean.setParentView(this);

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle resourceBundleAs = JsfUtils.getBundle(AsModule.RESOURCE_NAME);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, activationDialogBean, AsConstants.ACTIVATION_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, activationDialogBean, AsConstants.ACTIVATION_DIALOG);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, activationDialogBean, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		addAutoViewAction(AsConstants.ACTION_SHOW_ACTIVATION_CODE, resourceBundleAs, activationDialogBean, AsConstants.SHOW_ACTIVATION_CODE_DIALOG_PATH);

		activationDialogBean.setParentView(this);
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemView#creatActionObject()
	 */
	public Object createActionObject() {
		if (this.subject.getKlass() == null) {
			return null;
		}
		int hours = asModule.getPreferences().getActivationCodeDefaultValidTill();
		ActivationCodeEntity activationCode = new ActivationCodeEntity();
		activationCode.setValidTill(LocalDateTime.now().plusHours(hours));
		actionObject = activationCode;
		return activationCode;

	}

}
