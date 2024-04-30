package com.doubleclue.dcem.admin.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;

@SuppressWarnings("serial")
@Named("roleDialog")
@SessionScoped
public class RoleDialogBean extends DcemDialog {

	@Inject
	RoleLogic roleLogic;

	@Inject
	JpaLogic jpaLogic;

	public RoleDialogBean() {
	}

	@Override
	public boolean actionOk() throws Exception {
		jpaLogic.addOrUpdateEntity((EntityInterface) getActionObject(), this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void actionConfirm() {
		roleLogic.deleteRoles(this.getAutoViewAction().getDcemAction(), autoViewBean.getSelectedItems());
	}
}
