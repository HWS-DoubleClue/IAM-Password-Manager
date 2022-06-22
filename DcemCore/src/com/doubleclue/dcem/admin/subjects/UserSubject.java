package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
@ApplicationScoped
public class UserSubject extends SubjectAbs {

	public UserSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_ADD, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction(DcemConstants.ACTION_EDIT, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction(DcemConstants.ACTION_DELETE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_OR_MORE));

		RawAction rawAction = new RawAction(DcemConstants.ACTION_RESET_PASSWORD,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK }, ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-key");
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.ACTION_MEMBER_OF, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-users");
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.ACTION_ENABLE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-check");
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{userDialog.actionEnableUser()}");
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_DISABLE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-minus");
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{userDialog.actionDisableUser()}");
		rawActions.add(rawAction);

		rawAction = new RawAction(DcemConstants.CREATE_ACTIVATION_CODE,
				new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK }, ActionSelection.ONE_OR_MORE);
		rawAction.setIcon("fa fa-qrcode");
		rawAction.setDependsOnModule(DcemConstants.AS_MODULE_ID);
		rawActions.add(rawAction);
		
		rawAction = new RawAction(DcemConstants.ACTION_RESET_STAY_LOGIN, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_HELPDESK },
				ActionSelection.ONE_ONLY);
		rawAction.setIcon("fa fa-reply");
		rawAction.setActionType(ActionType.EL_METHOD);
		rawAction.setElMethodExpression("#{userDialog.actionResetStayLogin()}");
		rawActions.add(rawAction);

		// rawAction = new RawAction (AsConst.ACTION_SEND_MESSAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN,
		// DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY);
		// rawAction.setIcon("messageIcon");
		// rawActions.add(rawAction);
		//
		// rawActions.add(new RawAction(AsConst.ACTION_GOTO_ACTIVATIONCODE,
		// new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN,
		// DcemConstants.SYSTEM_ROLE_HELPDESK },
		// ActionSelection.IGNORE, AsConst.ACTION_GOTO_ACTIVATIONCODE, ActionType.VIEW_LINK ));
		//
		// rawActions.add(new RawAction(AsConst.ACTION_GOTO_DEVICES,
		// new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN,
		// DcemConstants.SYSTEM_ROLE_HELPDESK },
		// ActionSelection.IGNORE, AsConst.ACTION_GOTO_DEVICES, ActionType.VIEW_LINK ));
		// rawActions.add(new RawAction(AsConst.ACTION_GOTO_CLOUDDATA,
		// new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN,
		// DcemConstants.SYSTEM_ROLE_HELPDESK },
		// ActionSelection.IGNORE, AsConst.ACTION_GOTO_CLOUDDATA, ActionType.VIEW_LINK ));

		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN }));

	}

	public String getModuleId() {
		return AdminModule.MODULE_ID;
	}

	public int getRank() {
		return 20;
	}

	public String getIconName() {
		return "fa fa-user";
	}

	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	public Class<?> getKlass() {
		return DcemUser.class;
	}

}
