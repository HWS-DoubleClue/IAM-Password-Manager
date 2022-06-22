package com.doubleclue.dcem.as.logic;

import java.io.Serializable;
import java.util.Date;

import com.doubleclue.dcem.core.cluster.DcemCluster;

@SuppressWarnings("serial")
public class ReverseProxyReport implements Serializable {
	
	/**
	 * 
	 */
	public static String RESULT_OK = "OK";
	public static String RESULT_ERROR = "ERROR";

	public ReverseProxyReport(String name, String action, String result, String information, String remoteAddress) {
		super();
		this.name = name;
		this.action = action;
		this.result = result;
		this.information = information;
		this.remoteAddress = remoteAddress;
		date = new Date();
		this.nodeId = DcemCluster.getInstance().getNodeName();
	}

	Date date;
	String name;
	String action;
	String result;
	String information;
	String remoteAddress;
	String nodeId;
	

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

}
