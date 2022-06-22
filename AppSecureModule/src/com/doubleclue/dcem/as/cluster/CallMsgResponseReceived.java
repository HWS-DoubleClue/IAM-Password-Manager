package com.doubleclue.dcem.as.cluster;

import java.io.Serializable;
import java.util.concurrent.Callable;

import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
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
public class CallMsgResponseReceived implements Callable<String>, Serializable {
	
	protected long msgId;
	String tenantName;

    public CallMsgResponseReceived(long msgId, String tenantName) {
        this.msgId = msgId;
        this.tenantName = tenantName;
    }

    @Override
    public String call() throws DcemException {
    	
    	
    	WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantEntity tenantEntity =  applicationBean.getTenant(tenantName);
			TenantIdResolver.setCurrentTenant(tenantEntity);
			
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			asMessageHandler.fireMsgResponseReceived(msgId);
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}    
        return null;
    }
}
