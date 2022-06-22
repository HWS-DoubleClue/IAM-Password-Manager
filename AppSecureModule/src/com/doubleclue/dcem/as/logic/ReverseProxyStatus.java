package com.doubleclue.dcem.as.logic;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class ReverseProxyStatus implements Serializable {

	
	String name;
	Date connectedSince;
	int clientConnections;
	String ipSource;
	String nodeId;

	
	public ReverseProxyStatus(String name, Date connectedSince, int clientConnections, String ipSource, String nodeId) {
		super();
		this.name = name;
		this.connectedSince = connectedSince;
		this.clientConnections = clientConnections;
		this.ipSource = ipSource;
		this.nodeId = nodeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getConnectedSince() {
		return connectedSince;
	}

	public void setConnectedSince(Date connectedSince) {
		this.connectedSince = connectedSince;
	}

	public int getClientConnections() {
		return clientConnections;
	}

	public void setClientConnections(int clientConnections) {
		this.clientConnections = clientConnections;
	}

	public String getIpSource() {
		return ipSource;
	}

	public void setIpSource(String ipSource) {
		this.ipSource = ipSource;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}


	

	

	
}
