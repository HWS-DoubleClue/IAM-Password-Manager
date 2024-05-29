package com.doubleclue.dcem.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.as.entities.MessageEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class PushApprovalSubject extends SubjectAbs {



	public PushApprovalSubject () {
//		rawActions.add (new RawAction (Constants.ACTION_ADD, new String [] {Constants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.CREATE_OBJECT));
//		rawActions.add (new RawAction (Constants.ACTION_EDIT, new String [] {Constants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY));

		RawAction rawAction = new RawAction (AsConstants.ACTION_DISPLAY_MSG, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}, ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-eye");
		rawActions.add(rawAction);
		
		rawAction = new RawAction(AsConstants.ACTION_SEND_MESSAGE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-solid fa-envelope");
		rawActions.add(rawAction);
		
		rawAction = new RawAction(AsConstants.ACTION_PENDING_MESSAGES,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN },
				ActionSelection.IGNORE);
		rawAction.setIcon("fa fa-hourglass-half");
		rawActions.add(rawAction);
		
		rawActions.add (new RawAction (DcemConstants.ACTION_PUSH_NOTIFICATION, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}));	
		
		rawActions.add (new RawAction (DcemConstants.ACTION_VIEW, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));	
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN}));	

	}



	public String getModuleId() {
		return AsModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 70;
	}	

	@Override
	public String getIconName() {
		return "fa fa-comments";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return MessageEntity.class;
	}

}
