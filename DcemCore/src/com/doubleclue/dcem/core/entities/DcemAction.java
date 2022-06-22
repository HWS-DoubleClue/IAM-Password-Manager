package com.doubleclue.dcem.core.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.gui.DcemGui;

@SuppressWarnings("serial")
@Entity
@Table(name = "core_action", uniqueConstraints = @UniqueConstraint(name = "UK_SEM_ACTION", columnNames = { "moduleId", "subject", "action" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = DcemAction.GET_ACTION, query = "select sa from DcemAction as sa where sa.moduleId=?1 AND sa.subject=?2 AND sa.action=?3", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"), @QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemAction") }),
		@NamedQuery(name = DcemAction.GET_ALL, query = "select sa from DcemAction as sa ORDER BY moduleId", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemActionAll") }),
		@NamedQuery(name = DcemAction.GET_BY_SUBJECT, query = "SELECT sa FROM DcemAction sa WHERE sa.subject=?1") })
public class DcemAction extends EntityInterface implements Serializable {

	public final static String GET_ACTION = "DcemAction.getACtion";
	public final static String GET_ALL = "DcemAction.all";
	public final static String GET_BY_SUBJECT = "DcemAction.getBySubject";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreDcemAction", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "SEM_ACTION.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreDcemAction")
	// @DcemGui(columnWidth = "40px")
	private Integer id;

	@Column(length = 64, nullable = false)
	@DcemGui
	protected String moduleId;

	@Column(length = 128, nullable = false)
	@DcemGui
	protected String subject;

	@Column(length = 128, nullable = false)
	@DcemGui
	protected String action;

	public DcemAction() {

	}

	public DcemAction(SubjectAbs subject, String action) {
		super();
		this.action = action;
		this.moduleId = subject.getModuleId();
		this.subject = subject.getName();
	}

	public DcemAction(String moduleId, String subject, String action) {
		super();
		this.action = action;
		this.moduleId = moduleId;
		this.subject = subject;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((action == null) ? 0 : action.hashCode());
		result = prime * result + ((moduleId == null) ? 0 : moduleId.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DcemAction other = (DcemAction) obj;
		if (action == null) {
			if (other.action != null)
				return false;
		} else if (!action.equals(other.action))
			return false;
		if (moduleId == null) {
			if (other.moduleId != null)
				return false;
		} else if (!moduleId.equals(other.moduleId))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DcemAction [id=" + id + ", moduleId=" + moduleId + ", subject=" + subject + ", action=" + action + "]";
	}

}
