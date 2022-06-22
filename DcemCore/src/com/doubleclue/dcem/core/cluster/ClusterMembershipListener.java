package com.doubleclue.dcem.core.cluster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.NodeState;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class ClusterMembershipListener implements MembershipListener {

	private final static Logger logger = LogManager.getLogger(ClusterMembershipListener.class);

	public void memberAdded(MembershipEvent membershipEvent) {
		String nodeName = membershipEvent.getMember().getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE);
		if (nodeName == null) {
			nodeName = "Unknown Node Attribute, " + membershipEvent.getMember().toString();
		}
		logger.info("Added: " + nodeName);
		WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.NODE_JOINED, nodeName, AlertSeverity.OK, false);
		} catch (Exception e) {
			logger.debug("Error while adding alert for removing Cluster Membership", e);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

	public void memberRemoved(MembershipEvent membershipEvent) {

		String nodeName = membershipEvent.getMember().getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE);
		if (nodeName == null) {
			nodeName = "Unknown Node Attribute, " + membershipEvent.getMember().toString();
		}
		logger.warn("Cluster Node failed: " + nodeName);

		WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.NODE_FAILED, nodeName, AlertSeverity.ERROR, false);
		} catch (Exception e) {
			logger.debug("Error while adding alert for removing Cluster Membership", e);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}

		if (DcemCluster.getInstance().isClusterMaster()) {
			requestContext = null;
			try {
				requestContext = WeldContextUtils.activateRequestContext();
				NodeLogic nodeLogic = CdiUtils.getReference(NodeLogic.class);
				nodeLogic.setNodeState(nodeName, NodeState.Off);
			} catch (Exception e) {
				logger.warn("Cannot set node state", e);
			} finally {
				WeldContextUtils.deactivateRequestContext(requestContext);
			}
		}
	}

	public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
		System.err.println("Member attribute changed: " + memberAttributeEvent);
	}
}
