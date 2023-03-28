package com.doubleclue.dcem.core.entities;

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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * The persistent class for the app_version database table.
 * 
 */
@Entity
@Table(name = "core_department", uniqueConstraints = { @UniqueConstraint(name = "UK_DEPARTMENT_NAME", columnNames = { "dc_name" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = DepartmentEntity.GET_FILTER_LIST, query = "SELECT dt.name FROM DepartmentEntity dt WHERE LOWER(dt.name) LIKE LOWER(?1) ORDER BY dt.name"),
		@NamedQuery(name = DepartmentEntity.GET_BY_NAME, query = "SELECT dt FROM DepartmentEntity dt WHERE LOWER(dt.name) = LOWER(?1)"),
		@NamedQuery(name = DepartmentEntity.GET_BY_HEAD_OF, query = "SELECT dt FROM DepartmentEntity dt WHERE dt.headOf = ?1 OR dt.deputy = ?1") })

public class DepartmentEntity extends EntityInterface {

	public final static String GET_FILTER_LIST = "department.filterList";
	public static final String GET_BY_NAME = "department.getByName";
	public static final String GET_BY_HEAD_OF = "department.getByHeadOf";

	public DepartmentEntity() {
		super();
	}

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@DcemGui(name = "Parent", sortOrder = SortOrder.ASCENDING, sortRank = 0)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_DEPARTMENT_PARENT_ID"), name = "dc_parent_id", nullable = true, insertable = true, updatable = true)
	private DepartmentEntity parentDepartment;

	@Column(name = "dc_name", nullable = false)
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	private String name;

	@DcemGui
	private String abbriviation;

	@DcemGui(name = "HeadOf", subClass = "displayName")
	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_DEPARTMENT_USER"), insertable = true, updatable = true)
	private DcemUser headOf;

	@DcemGui(name = "Deputy", subClass = "displayName")
	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_DEPARTMENT_USER_DEPUTY"), insertable = true, updatable = true)
	private DcemUser deputy;

	@Column(name = "dc_desc")
	private String description;

	@Override
	public Number getId() {
		return this.id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Long) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbbriviation() {
		return abbriviation;
	}

	public void setAbbriviation(String abbriviation) {
		this.abbriviation = abbriviation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DcemUser getHeadOf() {
		return headOf;
	}

	public void setHeadOf(DcemUser headOf) {
		this.headOf = headOf;
	}

	public DepartmentEntity getParentDepartment() {
		return parentDepartment;
	}

	public void setParentDepartment(DepartmentEntity parentDepartment) {
		this.parentDepartment = parentDepartment;
	}

	public DcemUser getDeputy() {
		return deputy;
	}

	public void setDeputy(DcemUser deputy) {
		this.deputy = deputy;
	}

	@Override
	public String toString() {
		return name;
	}

}