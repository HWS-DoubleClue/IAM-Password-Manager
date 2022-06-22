package com.doubleclue.dcem.as.policy;

import java.io.Serializable;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;

public class PolicyTransaction implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	DcemPolicy dcemPolicy;
	PolicyAppEntity policyAppEntity;
	String policyName;
	int userId;
		
	public PolicyTransaction(DcemPolicy dcemPolicy, String policyName, PolicyAppEntity policyAppEntity, int userId) {
		super();
		this.dcemPolicy = dcemPolicy;
		this.policyName = policyName;
		this.policyAppEntity = policyAppEntity;
		this.userId = userId;
	}
	
	
	public DcemPolicy getDcemPolicy() {
		return dcemPolicy;
	}
	public void setDcemPolicy(DcemPolicy dcemPolicy) {
		this.dcemPolicy = dcemPolicy;
	}
	public PolicyAppEntity getPolicyAppEntity() {
		return policyAppEntity;
	}
	public void setPolicyAppEntity(PolicyAppEntity policyAppEntity) {
		this.policyAppEntity = policyAppEntity;
	}


	public int getUserId() {
		return userId;
	}


	public void setUserId(int userId) {
		this.userId = userId;
	}


	public String getPolicyName() {
		return policyName;
	}


	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}
}
