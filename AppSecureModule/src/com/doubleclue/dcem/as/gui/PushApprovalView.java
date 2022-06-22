package com.doubleclue.dcem.as.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.PushApprovalSubject;

@SuppressWarnings("serial")
@Named("pushApprovalView")
@SessionScoped
public class PushApprovalView extends DcemView {

	@Inject
	private PushApprovalSubject msgSubject;

	@Inject
	private MsgShowDialog msgShowDialog;
	
	@Inject
	private AsMessageDialog messageDialog;
	
	@Inject
	private PendingMsgDialog pendingMsgDialog;
	
	@Inject
	private PushNotificationDialog pushNotificationDialog;

	@PostConstruct
	private void init() {

		msgShowDialog.setParentView(this);

		subject = msgSubject;

		// ResourceBundle resourceBundle =
		// JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);
		addAutoViewAction(AsConstants.ACTION_DISPLAY_MSG, asResourceBundle, msgShowDialog, AsConstants.MSG_SHOW_DIALOG);
		addAutoViewAction(AsConstants.ACTION_SEND_MESSAGE, asResourceBundle, messageDialog, AsConstants.MESSAGE_DIALOG);
		addAutoViewAction(AsConstants.ACTION_PENDING_MESSAGES, asResourceBundle, pendingMsgDialog, AsConstants.PENDING_MSG_DIALOG);
		
		addAutoViewAction(DcemConstants.ACTION_PUSH_NOTIFICATION, asResourceBundle, pushNotificationDialog, AsConstants.PUSH_NOTIFICATION_DIALOG);

	}

	@Override
	public void reload() {
		// autoViewBean.reload();
	}

}
