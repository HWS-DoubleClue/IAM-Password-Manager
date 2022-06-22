package com.doubleclue.dcem.as.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * 
 * The persistent class for the KERNEL_AUDITING database table.
 * 
 */
@Entity
@Table(name = "as_policy_app", uniqueConstraints = @UniqueConstraint(name = "UK_POLICY_APP", columnNames = { "authapp", "subId" }))
@NamedQueries({ @NamedQuery(name = PolicyAppEntity.GET_ALL_MAIN_POLICY_APP, query = "SELECT pe FROM PolicyAppEntity pe where pe.subId=0"),
		@NamedQuery(name = PolicyAppEntity.GET_ALL_POLICY_APP, query = "SELECT pe FROM PolicyAppEntity pe where pe.disabled = false ORDER BY pe.authApplication, pe.subName ASC NULLS FIRST"),
		@NamedQuery(name = PolicyAppEntity.GET_POLICY_APP, query = "SELECT pe FROM PolicyAppEntity pe where pe.authApplication = ?1 AND pe.subId=?2") })

public class PolicyAppEntity extends EntityInterface implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String GET_ALL_MAIN_POLICY_APP = "PolicyAppEntity.mainPolicyApp";
	public static final String GET_ALL_POLICY_APP = "PolicyAppEntity.getAll";
	public static final String GET_POLICY_APP = "PolicyAppEntity.getPolicyApp";

	public PolicyAppEntity() {
		super();
	}

	public PolicyAppEntity(AuthApplication authApplication, int subId, String subName) {
		super();
		this.authApplication = authApplication;
		this.subId = subId;
		this.subName = subName;
	}

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStorePolicyApp", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "POLICYAPP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStorePolicyApp")
	private Integer id;

	@Column(name = "authapp")
	@Enumerated(EnumType.STRING)
	@DcemGui
	AuthApplication authApplication;

	@Column(name = "subname")
	@DcemGui
	String subName;

	int subId;

	@Column(name = "dc_disabled")
	boolean disabled = false;

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public AuthApplication getAuthApplication() {
		return authApplication;
	}

	public void setAuthApplication(AuthApplication authApplication) {
		this.authApplication = authApplication;
	}

	public int getSubId() {
		return subId;
	}

	public void setSubId(int subId) {
		this.subId = subId;
	}

	@Override
	public int hashCode() {
		return subId * 37 + authApplication.ordinal();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if ((obj instanceof PolicyAppEntity) == false) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		return (this.authApplication == ((PolicyAppEntity) obj).authApplication) && this.subId == (((PolicyAppEntity) obj).subId);
	}

	@Override
	public String toString() {
		if (subName == null) {
			return authApplication.toString();
		}
		return authApplication.toString() + "-" + subName;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}