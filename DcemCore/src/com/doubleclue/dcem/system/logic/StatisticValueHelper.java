package com.doubleclue.dcem.system.logic;

import java.io.Serializable;
import java.util.Date;

public class StatisticValueHelper implements Serializable {

	private Date timestamp;
	private String node;
	private String module;
	private String name;
	private String value;
	private boolean checked;

	public StatisticValueHelper(Date timestamp, String node, String module, String name, String value) {
		super();
		this.timestamp = timestamp;
		this.node = node;
		this.module = module;
		this.name = name;
		this.value = value;
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
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
