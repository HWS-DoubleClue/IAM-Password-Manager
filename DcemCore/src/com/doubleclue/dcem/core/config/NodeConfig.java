package com.doubleclue.dcem.core.config;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class holding the Node configuration parameters.
 * 
 * @author Emanuel Galea
 *
 */
@XmlType
@XmlRootElement (name ="Node")
public class NodeConfig implements Cloneable, Serializable {

	private static final long serialVersionUID = 1L;
	
	@NotNull(message = "{node.bean.node.id.invalid}")
	@Size(min = 1, max = 20, message = "{node.bean.node.id.invalid}")
	private String nodeId;

	@NotNull(message = "{node.bean.networkadapter.ip.invalid}")
	private String networkAdapterIp;
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getNetworkAdapterIp() {
		return networkAdapterIp;
	}

	public void setNetworkAdapterIP(String networkAdapterIp) {
		this.networkAdapterIp = networkAdapterIp;
	}
}