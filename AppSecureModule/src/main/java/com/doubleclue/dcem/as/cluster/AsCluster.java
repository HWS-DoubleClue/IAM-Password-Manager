package com.doubleclue.dcem.as.cluster;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.inject.Inject;

import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

public class AsCluster {
	
	@Inject
	NodeLogic nodeLogic;
	
	@Inject
	DcemApplicationBean applicationBean;
	
	DcemCluster dcemCluster = DcemCluster.getInstance();


	class MemberDevice {
		Member member;
		Integer deviceId;
	}
	
	public String msgToDevice (DcemNode node, long msgId) throws DcemException  {
		Member member = dcemCluster.getMember(node);
		if (member == null) {
			throw new DcemException (DcemErrorCodes.NODE_NAME_NOT_FOUND, node.getName());
		}
		
		IExecutorService executorService = dcemCluster.getExecutorService();
		Callable<String> callDeviceMsg = new CallDeviceMsg(msgId);
		Future<String> future = executorService.submitToMember(callDeviceMsg, member);
		try {
			return future.get();
		} catch (Exception e) {
			if (e.getCause() instanceof DcemException) {
				throw (DcemException)e.getCause();
			}
			throw new DcemException (DcemErrorCodes.UNEXPECTED_ERROR, null, e);
		}
	}
	
	public String msgResponseReceived (PendingMsg pendingMsg, TenantEntity tenantEntity) throws DcemException  {
		DcemNode dcemNode = applicationBean.getDcemNodeById(pendingMsg.getNotifyNodeOnResponse());
		Member member = dcemCluster.getMember(dcemNode);
		if (member == null) {
			throw new DcemException (DcemErrorCodes.NODE_NAME_NOT_FOUND, dcemNode.getName());
		}
		
		IExecutorService executorService = dcemCluster.getExecutorService();
		Callable<String> callMsgResponse = new CallMsgResponseReceived(pendingMsg.getId(), tenantEntity.getName());
		Future<String> future = executorService.submitToMember(callMsgResponse, member);
		try {
			return future.get();
		} catch (Exception e) {
			if (e.getCause() instanceof DcemException) {
				throw (DcemException)e.getCause();
			}
			throw new DcemException (DcemErrorCodes.UNEXPECTED_ERROR, null, e);
		}
	}
	
	

}
