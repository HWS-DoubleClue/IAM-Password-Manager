package com.doubleclue.dcem.as.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.EntityInterface;

/**
 * 
 * The persistent class for the KERNEL_AUDITING database table.
 * 
 */
@Entity
@Table(name = "as_app_policy_group")
@NamedQueries({
		@NamedQuery(name = AppPolicyGroupEntity.GET_APP_POLICIES, query = "SELECT apg FROM AppPolicyGroupEntity apg LEFT JOIN apg.group gr where apg.policyAppEntity = ?1 AND apg.policyEntity IS NOT NULL ORDER BY apg.priority DESC"),
		@NamedQuery(name = AppPolicyGroupEntity.GET_POLICIES, query = "SELECT apg FROM AppPolicyGroupEntity apg JOIN apg.policyAppEntity pae where apg.policyEntity = ?1 ORDER BY pae.authApplication"),
		@NamedQuery(name = AppPolicyGroupEntity.GET_BY_POLICY_AND_APP, query = "SELECT apg FROM AppPolicyGroupEntity apg WHERE apg.policyEntity = ?1 AND apg.policyAppEntity = ?2"),
		@NamedQuery(name = AppPolicyGroupEntity.GET_POLICIES_BY_GROUP, query = "SELECT apg FROM AppPolicyGroupEntity apg where apg.group =?1"),
		@NamedQuery(name = AppPolicyGroupEntity.GET_POLICIES_BY_POLICY_ENTITY, query = "SELECT apg FROM AppPolicyGroupEntity apg where apg.policyEntity =?1"),
		@NamedQuery(name = AppPolicyGroupEntity.DELETE_POLICIY_GROUP, query = "DELETE FROM AppPolicyGroupEntity apg WHERE apg.group=?1"), })

public class AppPolicyGroupEntity extends EntityInterface implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String GET_APP_POLICIES = "AppPolicyGroupEntity.allPolicies";
	public static final String GET_POLICIES = "AppPolicyGroupEntity.policies";
	public static final String GET_BY_POLICY_AND_APP = "AppPolicyGroupEntity.getByPolicyAndApp";
	public static final String GET_POLICIES_BY_GROUP = "AppPolicyGroupEntity.getAllPoliciesGroup";
	public static final String GET_POLICIES_BY_POLICY_ENTITY = "AppPolicyGroupEntity.getAllPoliciesByPolicy";
	public static final String DELETE_POLICIY_GROUP = "AppPolicyGroupEntity.deletePoliciyGroup";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAsAppPolicyGroup", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "AS_APP_POLICY_GROUP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAsAppPolicyGroup")
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_REF_APP_POLICY"), name = "policyApp_id", nullable = false, insertable = true, updatable = true)
	private PolicyAppEntity policyAppEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_REF_POLICY"), name = "policy_id", nullable = true, insertable = true, updatable = true)
	private PolicyEntity policyEntity;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_REF_GROUP"), name = "group_id", nullable = true, insertable = true, updatable = true)
	DcemGroup group;

	@Column(name = "dc_priority")
	int priority;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public PolicyAppEntity getPolicyAppEntity() {
		return policyAppEntity;
	}

	public void setPolicyAppEntity(PolicyAppEntity policyAppEntity) {
		this.policyAppEntity = policyAppEntity;
	}

	public DcemGroup getGroup() {
		return group;
	}

	public void setGroup(DcemGroup group) {
		this.group = group;
	}

	public PolicyEntity getPolicyEntity() {
		return policyEntity;
	}

	public void setPolicyEntity(PolicyEntity policyEntity) {
		this.policyEntity = policyEntity;
	}

	@Override
	public String toString() {
		return "AppPolicyGroupEntity [priority=" + priority + ", policyAppEntity=" + policyAppEntity + ", policyEntity=" + policyEntity + ", group=" + group
				+ "]";
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}