package com.doubleclue.dcem.as.comm;

import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryEvictedListener;

public class PendingMessageListener implements EntryEvictedListener<Long, PendingMsg> {

	

	@Override
	public void entryEvicted(EntryEvent<Long, PendingMsg> event) {
		WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
			asMessageHandler.evictedMessage(event.getOldValue());

		} catch (Exception e) {
			
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
