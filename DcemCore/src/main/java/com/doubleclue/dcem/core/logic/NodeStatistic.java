package com.doubleclue.dcem.core.logic;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NodeStatistic {
	
	public NodeStatistic(String nodeName) {
		this.nodeName = nodeName;
		moduleStatistices = new LinkedList<>();
	}
	
	String nodeName;
	
	@JsonProperty ("modules")
	List<ModuleStatistic> moduleStatistices;
	
	public void addModuleStatitics (ModuleStatistic moduleStatistic ) {
		moduleStatistices.add(moduleStatistic);
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public List<ModuleStatistic> getModuleStatistices() {
		return moduleStatistices;
	}

	public void setModuleStatistices(List<ModuleStatistic> moduleStatistices) {
		this.moduleStatistices = moduleStatistices;
	}
		

}
