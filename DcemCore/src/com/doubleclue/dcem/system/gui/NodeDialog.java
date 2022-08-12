package com.doubleclue.dcem.system.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.NodeState;
import com.doubleclue.dcem.system.logic.SystemModule;

@Named("nodeDialog")
@SessionScoped
public class NodeDialog extends DcemDialog {

	@Inject
	NodeLogic nodeLogic;

	@Inject
	SystemModule adminModule;

	@Inject
	AutoDialogBean autoDialogBean;
	
	@Inject
	AutoViewBean autoViewBean;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	JpaLogic jpaLogic;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public boolean actionOk() throws Exception {
		DcemNode node = (DcemNode) this.getActionObject();
		node.setName(node.getName().trim());
		if (node.getName().isEmpty()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME,"node.error.empty");
			return false;
		}
		if (node.getId() == null) {
			node.setState(NodeState.Off);
		}
		jpaLogic.addOrUpdateEntity(node, this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void actionConfirm() {
		nodeLogic.delete((DcemNode) this.getActionObject());
		return;
	}

	
	


}
