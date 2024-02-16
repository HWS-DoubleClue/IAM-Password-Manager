package com.doubleclue.dcem.core.entities;

import java.util.List;

import javax.persistence.CascadeType;
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
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.logic.DcemLdapAttributes;
import com.doubleclue.dcem.core.utils.DisplayModes;

/**
 * The persistent class for user
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "core_group", uniqueConstraints = @UniqueConstraint(name = "UK_APP_GROUP", columnNames = { "dc_name" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

@NamedQueries({ @NamedQuery(name = DcemGroup.GET_MEMBER_COUNT, query = "SELECT COUNT(m) from DcemGroup g JOIN g.members m WHERE g.id = ?1"),
		// @NamedQuery(name = DcemGroup.GET_MAIN_GROUP_FROM_MEMBER, query = "SELECT gr.id from DcemGroup gr JOIN
		// gr.members us WHERE us.id = ?1 ORDER BY gr.weight DESC"),

		@NamedQuery(name = DcemGroup.GET_GROUPS_FROM_MEMBER, query = "SELECT gr.name from DcemGroup gr JOIN gr.members us WHERE us.id = ?1"),

		@NamedQuery(name = DcemGroup.GET_GROUP, query = "SELECT gr from DcemGroup gr WHERE gr.name = ?1", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query." + DcemGroup.GET_GROUP) }),
		@NamedQuery(name = DcemGroup.GET_USER_GROUPS, query = "SELECT g from DcemGroup g JOIN g.members m WHERE m.id = ?1"),
		// @NamedQuery(name = DcemGroup.GET_GROUP_FROM_DN, query = "SELECT g from DcemGroup g WHERE g.groupDn = ?1"),

		@NamedQuery(name = DcemGroup.GET_ALL, query = "SELECT g from DcemGroup g WHERE g.id > 0 ORDER BY g.name ASC"),
		@NamedQuery(name = DcemGroup.GET_GROUPS_BY_LDAP, query = "SELECT g FROM DcemGroup g WHERE g.domainEntity = ?1"),
		@NamedQuery(name = DcemGroup.GET_FILTERED_GROUPS_BY_LDAP, query = "SELECT group FROM DcemGroup group WHERE group.domainEntity = ?1 AND group.name LIKE ?2 ESCAPE "
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES),
		@NamedQuery(name = DcemGroup.GET_FILTERED_GROUPS, query = "SELECT g FROM DcemGroup g WHERE (g.name !='_GROUP_ROOT_') AND lower(g.name) LIKE lower(?1) ESCAPE"
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES + "ORDER BY g.name ASC")

})

public class DcemGroup extends EntityAbstract {

	public final static String GET_MEMBER_COUNT = "DcemGroup.memberCount";
	// public final static String GET_MAIN_GROUP_FROM_MEMBER = "DcemGroup.mainGroup";
	public static final String GET_ALL = "DcemGroup.getAll";
	public static final String GET_GROUP = "DcemGroup.getGroup";
	public static final String GET_USER_GROUPS = "DcemGroup.getUserGroups";
	// public static final String GET_GROUP_FROM_DN = "DcemGroup.getGroupFromDn";
	public static final String GET_GROUPS_FROM_MEMBER = "DcemGroup.getGroupsFromMember";
	public static final String GET_GROUPS_BY_LDAP = "DcemGroup.getGroupsByLdap";
	public final static String GET_FILTERED_GROUPS_BY_LDAP = "DcemGroup.getFilteredGroupsByDomain";
	public final static String GET_FILTERED_GROUPS = "DcemGroup.getFilteredGroups";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreCoreGroup", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "CORE_GROUP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreCoreGroup")
	private Integer id;

	@Column(name = "dc_name", length = 255, nullable = false)
	@Size(min = 2, max = 255)
	@DcemGui
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_GROUP_ROLE"), name = "dc_role", nullable = true, insertable = true, updatable = true)
	@DcemGui(subClass = "name", displayMode = DisplayModes.TABLE_ONLY)
	private DcemRole dcemRole;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_GROUP_LDAP"), name = "dc_ldap", nullable = true, insertable = true, updatable = true)
	private DomainEntity domainEntity;

	@Column(length = 255, nullable = true)
	@Size(max = 255)
	@DcemGui
	private String description;

	// @DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	// @Transient
	// private long memberCount = -1;

	// @DcemGui
	// @Column(name = "dc_weight", nullable = false)
	// int weight;

	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "core_ref_user_group", joinColumns = @JoinColumn(name = "group_id"), foreignKey = @ForeignKey(name = "FK_USER_GROUP"), inverseJoinColumns = @JoinColumn(name = "user_id"), inverseForeignKey = @ForeignKey(name = "FK_GROUP_USER"))
	private List<DcemUser> members;

	@Column(length = 255, nullable = true)
	@DcemGui(style = "font-size: xx-small")
	private String groupDn;

	public DcemGroup() {
	}

	public DcemGroup(String domainName, String name) {
		if (domainName == null || domainName.isEmpty()) {
			this.name = name;
		} else {
			this.name = domainName + DcemConstants.DOMAIN_SEPERATOR + name;
		}
	}

	public DcemGroup(DomainEntity domainEntity, String dn, String name) {
		if (domainEntity == null) {
			this.name = name;
		} else {
			this.name = domainEntity.getName() + DcemConstants.DOMAIN_SEPERATOR + name;
		}
		this.groupDn = dn;
		this.domainEntity = domainEntity;
	}

	public void ldapSync(DcemLdapAttributes attributes) {
		this.groupDn = attributes.getDn();
		return;
	}

	public String toString() {
		return name;
	}

	public String getShortName() {
		if (domainEntity == null) {
			return name;
		}
		return name.substring(domainEntity.getName().length() + 1);
	}

	@Transient
	public boolean isDomainGroup() {
		return domainEntity != null;
	}

	// @Transient
	// public long getMemberCount() {
	// if (memberCount == -1) {
	// GroupLogic groupLogic = CdiUtils.getReference(GroupLogic.class);
	// memberCount = groupLogic.getMemberCount(this);
	// }
	// return memberCount;
	// }

	// @Transient
	// public void setMemberCount(long memberCount) {
	// this.memberCount = memberCount;
	// }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getGroupDn() {
		return groupDn;
	}

	public void setGroupDn(String groupDn) {
		this.groupDn = groupDn;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}
/*
 *  ATTENTION THIS RETURNS ONLY LOCAL GROUP MEMBERS
 * 
 */
	public List<DcemUser> getMembers() {
		return members;
	}

	public void setMembers(List<DcemUser> members) {
		this.members = members;
	}

	public DomainEntity getDomainEntity() {
		return domainEntity;
	}

	public void setDomainEntity(DomainEntity domainEntity) {
		this.domainEntity = domainEntity;
	}

	public DcemRole getDcemRole() {
		return dcemRole;
	}

	public void setDcemRole(DcemRole dcemRole) {
		this.dcemRole = dcemRole;
	}
	
	public String getRawName () {
		if (domainEntity == null) {
			return name;
		}
		return name.substring(domainEntity.getName().length() + 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupDn == null) ? 0 : groupDn.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		DcemGroup other = (DcemGroup) obj;
	
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public String getRowStyle() {
		return null;
	}

}