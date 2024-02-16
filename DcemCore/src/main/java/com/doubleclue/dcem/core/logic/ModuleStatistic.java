package com.doubleclue.dcem.core.logic;

import java.io.Serializable;
import java.util.Map;

import com.doubleclue.dcem.core.jpa.StatisticCounter;

@SuppressWarnings("serial")
public class ModuleStatistic implements Serializable {

	public ModuleStatistic() {
	}

	ModuleStatistic(String id) {
		moduleId = id;
	}
	
	private String moduleId;
	Map<String, StatisticCounter> counters;
	Map<String, String> staticValues;
	Map<String, String> values;
	
	boolean isEmpty() {
		return counters == null && staticValues == null && values == null;
	}

	public String getModuleId() {
		return moduleId;
	}
	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}
	public Map<String, StatisticCounter> getCounters() {
		return counters;
	}
	public void setCounters(Map<String, StatisticCounter> counters) {
		this.counters = counters;
	}
	public Map<String, String> getStaticValues() {
		return staticValues;
	}
	public void setStaticValues(Map<String, String> staticValues) {
		this.staticValues = staticValues;
	}
	public Map<String, String> getValues() {
		return values;
	}
	public void setValues(Map<String, String> values) {
		this.values = values;
	}
}