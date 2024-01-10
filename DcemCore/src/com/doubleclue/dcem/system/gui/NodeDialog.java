package com.doubleclue.dcem.system.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.servlets.HealthCheckServlet;
import com.doubleclue.dcem.core.tasks.ReloadTask;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.NodeState;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

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
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "node.error.empty");
			return false;
		}
		if (node.getId() == null) {
			node.setState(NodeState.Off);
		}
		jpaLogic.addOrUpdateEntity(node, this.getAutoViewAction().getDcemAction());
		return true;
	}

	public void actionStopHealthCheck() {
		updateStopHealthCheck(true);
	}

	public void actionStartHealthCheck() throws Exception {
		updateStopHealthCheck(false);
	}

	private void updateStopHealthCheck(Boolean stopHealthcheck) {
		DcemNode node = (DcemNode) this.getActionObject();
		Member member = DcemCluster.getDcemCluster().getMember(node);
		if (member == null) {
			JsfUtils.addErrorMessage("Node is not active.");
			return;
		}
		try {
			DcemUtils.reloadTaskNode(HealthCheckServlet.class, TenantIdResolver.getCurrentTenantName(), stopHealthcheck.toString(), member);
			if (stopHealthcheck == true) {
				JsfUtils.addInfoMessage("HealthCheck stopped succesful");
			} else {
				JsfUtils.addInfoMessage("HealthCheck restarted succesful");
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Couldn't stop HealthCheck: " + e.toString());
		}
	}

	@Override
	public void actionConfirm() {
		nodeLogic.delete((DcemNode) this.getActionObject());
		return;
	}

}
