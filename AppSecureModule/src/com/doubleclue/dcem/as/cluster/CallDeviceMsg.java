package com.doubleclue.dcem.as.cluster;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

/**
 * 
 * This class is used from Cluster
 * @author Emanuel
 *
 */
@SuppressWarnings("serial")
public class CallDeviceMsg implements Callable<String>, Serializable {
	
	
	protected long msgId;

    public CallDeviceMsg(long msgId) {
        this.msgId = msgId;
    }

    @Override
    public String call() throws DcemException {
    	WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			asMessageHandler.sendMsgToClientFromCluster(msgId);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}    
        return null;
    }
}
