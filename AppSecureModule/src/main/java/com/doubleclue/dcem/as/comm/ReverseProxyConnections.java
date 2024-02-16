package com.doubleclue.dcem.as.comm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.logic.ReverseProxyConnection;
import com.doubleclue.dcem.as.logic.ReverseProxyStatus;
import com.doubleclue.dcem.core.cluster.DcemCluster;;

public class ReverseProxyConnections {
	
	private static Logger logger = LogManager.getLogger(ReverseProxyConnections.class);

	private static ConcurrentHashMap<String, ReverseProxyConnection> reverseProxyConnectionsMap = new ConcurrentHashMap<>();
	
	public static void add(AppSession appSession) {
		ReverseProxyConnection reverseProxyConnection = new ReverseProxyConnection(appSession);
		if (reverseProxyConnectionsMap.get(appSession.getDomainName().toLowerCase(Locale.ENGLISH)) != null) {
			logger.warn("Overwriting a DomainProxy: " + appSession.getDomainName());
		}
		reverseProxyConnectionsMap.put(appSession.getDomainName().toLowerCase(Locale.ENGLISH), reverseProxyConnection);
	}

	public static ReverseProxyConnection remove (String domainName) {
		return reverseProxyConnectionsMap.remove(domainName.toLowerCase(Locale.ENGLISH));
	}
	
	public static ReverseProxyConnection get (String domainName) {
		return reverseProxyConnectionsMap.get(domainName.toLowerCase(Locale.ENGLISH));
	}
	
	public static void removeSubSession(String domainName, String sessionId) {
		 ReverseProxyConnection rpc =  get (domainName);
		 if (rpc != null) {
			 rpc.removeSubSession(sessionId);
		 }
	}
	
	public static List<ReverseProxyStatus> getRpStatus() {
		List<ReverseProxyStatus> rpStatusList = new ArrayList<>();
		for (Entry<String, ReverseProxyConnection> entry : reverseProxyConnectionsMap.entrySet()) {
			ReverseProxyConnection rpConnection = entry.getValue();
			rpStatusList.add(new ReverseProxyStatus(entry.getKey(), new Date(rpConnection.getAppSession().getTimeStamp()),
					rpConnection.getSubSessions().size(), rpConnection.getAppSession().getWsSession().getRemoteAddress(), DcemCluster.getDcemCluster().getNodeName()));
		}
		return rpStatusList;
	}
	
	public static List<ReverseProxyStatus> getRpStatus(String proxyName) {
		List<ReverseProxyStatus> rpStatusList = new ArrayList<>();
		ReverseProxyConnection rpConnection = get(proxyName);
		if (rpConnection != null) {
			rpStatusList.add(new ReverseProxyStatus(proxyName, new Date(rpConnection.getAppSession().getTimeStamp()),
					rpConnection.getSubSessions().size(), rpConnection.getAppSession().getWsSession().getRemoteAddress(), DcemCluster.getDcemCluster().getNodeName()));
		}
		return rpStatusList;
	}
	
	public static int getConnectionCount(String dcemName) {
		ReverseProxyConnection rpConnection = get(dcemName);
		if (rpConnection == null) {
			return 0;
		}
		return rpConnection.getSubSessions().size();		
	}
	
	public static boolean isConnected (String dcemName) {
		ReverseProxyConnection rpConnection = get(dcemName);
		if (rpConnection == null) {
			return false;
		}
		return true;
		
	}

}
