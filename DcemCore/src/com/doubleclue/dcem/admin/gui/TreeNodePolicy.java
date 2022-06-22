package com.doubleclue.dcem.admin.gui;

public class TreeNodePolicy {
	
	
	public TreeNodePolicy(String name, String policyName) {
		super();
		this.name = name;
		this.policyName = policyName;
	}
	
	
	String name;
	String policyName;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

}
