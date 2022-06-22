package com.doubleclue.dcem.core.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Convert;
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
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.jpa.FilterItem;
import com.doubleclue.dcem.core.outofscope.DbJsonConverter;



/**
 * The persistent class for the user database table.
 * 
 */
@Entity
@Table(name="core_rolerestriction", uniqueConstraints= @UniqueConstraint(name="UK_ROLE_RESTRICTION", columnNames={"dc_role", "moduleId",  "viewName", "variableName"}))
@Cache (usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(name=RoleRestriction.GET_VIEW_RESTRICTIONS,
			query="select sr from RoleRestriction as sr where sr.dcemRole = ?1 AND sr.moduleId=?2 AND sr.viewName = ?3",
			hints={@QueryHint(name="org.hibernate.cacheable", value="true")})
	
})


public class RoleRestriction extends EntityInterface implements Serializable {
	
	public static final String GET_VIEW_RESTRICTIONS = "roleRestriction.View";

	private static final long serialVersionUID = 1L;
//	private static Logger logger = LogManager.getLogger(RoleRestriction.class);

	@Id
	@TableGenerator( name = "coreSeqStoreRoleRestriction", table = "core_seq", pkColumnName = "seq_name", pkColumnValue="ROLERESTRICTION.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "coreSeqStoreRoleRestriction" )
	@Column(name = "dc_id")
	private Integer id;	
	

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_RESTRICTION_ROLE"), name = "dc_role", nullable = false, insertable = true, updatable = true)
	private DcemRole dcemRole;
	
	@Size (min=3, max = 128)
	String moduleId;


	@Size (min=3, max = 128)
	String viewName;
	
	@Size (min=3, max = 128)
	String variableName;
	
	
	@Convert (converter = DbJsonConverter.class)
	@Column(length=1024)
	FilterItem filterItem;
	
	
	@Version
	private int jpaVersion;
	
	
	public RoleRestriction() {
    }
	
	
	public int getid() {
		return this.id;
	}

    
	public void setid(int uid) {
		this.id = uid;
	}


	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}
	
	public DcemRole getDcemRole() {
		return dcemRole;
	}

	public void setDcemRole(DcemRole dcemRole) {
		this.dcemRole = dcemRole;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public FilterItem getFilterItem() {
		return filterItem;
	}

	public void setFilterItem(FilterItem filterItem) {
		this.filterItem = filterItem;
	}

	public int getJpaVersion() {
		return this.jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}


	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}