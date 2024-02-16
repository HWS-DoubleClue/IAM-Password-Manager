package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * The persistent class for the KERNEL_AUDITING database table.
 * 
 */
@Entity
@Table(name = "core_auditing")

@NamedQueries({ @NamedQuery(name = Auditing.GET_AFTER, query = "SELECT rp FROM Auditing rp where rp.timestamp < ?1"),
		@NamedQuery(name = Auditing.DELETE_AFTER, query = "DELETE FROM Auditing rp where rp.timestamp < ?1"),
		@NamedQuery(name = Auditing.DELETE_BY_USER, query = "DELETE FROM Auditing rp where rp.dcemUser = ?1"), })
public class Auditing extends EntityInterface implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String GET_AFTER = "Auditing.getAfter";
	public static final String DELETE_AFTER = "Auditing.deleteAfter";
	public static final String DELETE_BY_USER = "Auditing.deleteByUser";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAudit", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "AUDIT.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAudit")
	private Integer id;

	@Column(name = "auditTimeStamp")
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	private LocalDateTime timestamp;

	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "audituserId", foreignKey = @ForeignKey(name = "FK_AUDITING_USER"), referencedColumnName = "dc_id", nullable = true, insertable = true, updatable = false)
	@DcemGui(subClass = "displayName")
	private DcemUser dcemUser;

	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "actionId", referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_AUDITING_ACTION"), nullable = true, insertable = true, updatable = true)
	@DcemGui(subClass = "moduleId", name = "module")
	private DcemAction dcemAction;

	@DcemGui(dbMetaAttributeName = "dcemAction", subClass = "subject", name = "subject")
	@Transient
	private DcemAction subject; // this is only a dummy which returns dcemAction

	@DcemGui(dbMetaAttributeName = "dcemAction", subClass = "action", name = "action")
	@Transient
	private DcemAction action; // this is only a dummy which returns dcemAction

	@Lob()
	@Column(nullable = true)
	@DcemGui(style = "longInput")
	private String details;

	public Auditing() {
		super();
	}

	public Auditing(DcemAction dcemAction, String details, DcemUser user) {
		timestamp = LocalDateTime.now();
		this.dcemAction = dcemAction;
		this.details = details;
		this.dcemUser = user;
	}

	public String getDetails() {
		return this.details;
	}

	public void setAuditDetails(String details) {
		this.details = details;
	}

	public LocalDateTime getAuditTimestamp() {
		return this.timestamp;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public DcemAction getDcemAction() {
		return dcemAction;
	}

	public void setDcemAction(DcemAction dcemAction) {
		this.dcemAction = dcemAction;
	}

	public DcemAction getSubject() {
		return dcemAction;
	}

	public void setSubject(DcemAction subject) {
		this.dcemAction = subject;
	}

	public DcemAction getAction() {
		return dcemAction;
	}

	public void setAction(DcemAction action) {
		this.dcemAction = action;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

}