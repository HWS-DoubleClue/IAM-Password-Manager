package com.doubleclue.dcem.system.logic;

import java.io.Serializable;
import java.util.Date;

import com.doubleclue.dcem.core.jpa.StatisticCounter;

public class StatisticCounterHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date timestamp;
	private String node;
	private String module;
	private String name;
	private StatisticCounter counter;
	private boolean checked;

	public StatisticCounterHelper(Date timestamp, String node, String module, String name, StatisticCounter counter) {
		super();
		this.timestamp = timestamp;
		this.node = node;
		this.module = module;
		this.name = name;
		this.counter = counter;
	}
	
	public Date getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
	public String getNode() {
		return node;
	}
	public void setNode(String node) {
		this.node = node;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public StatisticCounter getCounter() {
		return counter;
	}
	public void setCounter(StatisticCounter counter) {
		this.counter = counter;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}