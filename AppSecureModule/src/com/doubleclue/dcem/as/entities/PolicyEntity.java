package com.doubleclue.dcem.as.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.as.policy.DcemPolicy;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The persistent class for the KERNEL_AUDITING database table.
 * 
 */
@Entity
@Table(name = "as_policy", uniqueConstraints = @UniqueConstraint(name = "UK_POLICY_NAME", columnNames = { "dc_name" }))
@NamedQueries({
		// @NamedQuery(name = PolicyEntity.GET_ALL_POLICY,
		// query = "SELECT pe FROM PolicyEntity pe JOIN pe.applications ap WHERE ap.id = ?1"),

		@NamedQuery(name = PolicyEntity.GET_POLICY_BY_NAME, query = "SELECT pe FROM PolicyEntity pe WHERE pe.name = ?1"),

		@NamedQuery(name = PolicyEntity.GET_ALL_POLICY, query = "SELECT pe FROM PolicyEntity pe where pe.id > 0 ORDER BY pe.name"),
		//
		// @NamedQuery(name = DcemUser.GET_USER_LOGIN_WITH_DOMAIN, query = "SELECT user FROM DcemUser user where
		// user.loginId = ?1 AND user.ldapEntity=?2")
})

public class PolicyEntity extends EntityInterface implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LogManager.getLogger(PolicyEntity.class);

	public final static String GET_ALL_POLICY = "PolicyEntity.getAll";
	// public final static String GET_ALL_POLICY = "PolicyEntity.allPolicy";
	public final static String GET_POLICY_BY_NAME = "PolicyEntity.policyByName";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStorePolicy", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "POLICY.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStorePolicy")
	private Integer id;

	@Column(name = "dc_name")
	String name;

	@Column(length = 4096)
	String jsonPolicy;

	@Transient
	DcemPolicy dcemPolicy;

	@Transient
	List<String> assignedTo;

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DcemPolicy getDcemPolicy() {
		if (dcemPolicy == null) {
			serializeDcemPolicy();
		}
		return dcemPolicy;
	}

	public void serializeDcemPolicy() {
		try {
			this.dcemPolicy = new ObjectMapper().readValue(jsonPolicy, DcemPolicy.class);
			dcemPolicy.updateIpranges();
		} catch (Exception exp) {
			this.dcemPolicy = new DcemPolicy();
			WeldRequestContext requestContext = null;
			try {
				requestContext = WeldContextUtils.activateRequestContext();
				DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
				reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.READ_POLICY_ERROR, "Policy: " + this.name,
						AlertSeverity.ERROR, false, this.name);
			} catch (Exception e) {
				logger.error("Error while adding alert in serializeDcemPolicy", e);
			} finally {
				WeldContextUtils.deactivateRequestContext(requestContext);
			}
		}
	}

	public void setDcemPolicy(DcemPolicy dcemPolicy) {
		this.dcemPolicy = dcemPolicy;
	}

	@Override
	public String toString() {
		return name;
	}

	public String getJsonPolicy() {
		return jsonPolicy;
	}

	public void setJsonPolicy(String jsonPolicy) {
		this.jsonPolicy = jsonPolicy;
	}

	public List<String> getAssignedTo() {
		return assignedTo;
	}

	public void setAssignedTo(List<String> assignedTo) {
		this.assignedTo = assignedTo;
	}

}