package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name = "core_role", uniqueConstraints = @UniqueConstraint(name = "UK_ROLE_NAME", columnNames = { "dc_name" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({ @NamedQuery(name = DcemRole.GET_ROLE_BY_NAME, query = "select sr from DcemRole as sr where sr.name = ?1", hints = {
		@QueryHint(name = "org.hibernate.cacheable", value = "true"), @QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemRoleName") }),
		@NamedQuery(name = DcemRole.GET_ALL_NAMES, query = "SELECT sr.name FROM DcemRole AS sr ORDER BY sr.rank DESC", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemRoleAllNames") }),
		@NamedQuery(name = DcemRole.GET_ALL_ROLES, query = "SELECT sr FROM DcemRole AS sr ORDER BY sr.rank DESC", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemRoleAll") }),
		@NamedQuery(name = DcemRole.GET_ROLES_BELOW_RANK, query = "SELECT sr FROM DcemRole AS sr WHERE sr.rank <= ?1 ORDER BY sr.rank DESC")})

public class DcemRole extends EntityInterface implements Serializable {

	public final static String GET_ROLE_BY_NAME = "DcemRole.name";
	public final static String GET_ALL_ROLES = "DcemRole.all";
	public final static String GET_ALL_NAMES = "DcemRole.allNames";
	public final static String GET_ROLES_BELOW_RANK = "DcemRole.belowRank";

	private static final long serialVersionUID = 1L;
	// private static Logger logger = LogManager.getLogger(DcemRole.class);

	@Id
	@TableGenerator(name = "coreSeqStoreRole", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "ROLE.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreRole")
	@Column(name = "dc_id")
	private Integer id;

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	@Column(name = "dc_name", length = 64, nullable = false)
	@NotNullOrEmptyString(message = "{roleDialog.name}")
	@Size(min = 2, max = 64)
	@DcemGui
	private String name;

	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	private boolean systemRole;

	@DcemGui()
	private boolean disabled = false;

	@DcemGui
	@Column(name = "dc_rank", nullable = false)
	private int rank;

	@Version
	private int jpaVersion;

	@ManyToMany (fetch = FetchType.LAZY)
	@JoinTable(name = "core_role_core_action", joinColumns = @JoinColumn(name = "core_role_dc_id"), inverseJoinColumns = @JoinColumn(name = "actions_dc_id"), inverseForeignKey = @ForeignKey(name = "FK_ROLE_ACTION"))
	// @JoinColumn(name="dc_role", nullable = true, insertable = true, updatable = true)
	private Set<DcemAction> actions;

	public DcemRole() {
	}

	public DcemRole(String name, boolean systemRole, int rank) {
		super();
		this.name = name;
		this.systemRole = systemRole;
		this.rank = rank;
		this.disabled = false;
	}

	@JsonIgnore
	public int getid() {
		return this.id;
	}

	public void setid(int uid) {
		this.id = uid;
	}

	public int getJpaVersion() {
		return this.jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}

	public Set<DcemAction> getActions() {
		return actions;
	}

	public void setActions(Set<DcemAction> actions) {
		this.actions = actions;
	}

	@Transient
	public void updateActions(Set<DcemAction> actions) {
		if (this.actions == null) {
			this.actions = actions;
		}
		this.actions.clear();
		this.actions.addAll(actions);
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof DcemRole) {
			return this.id == ((DcemRole) object).id;
		}
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}