package com.doubleclue.dcem.core.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Pattern;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.gui.DcemGui;

@Entity
@Table(name = "sys_tenant", uniqueConstraints = { @UniqueConstraint(name = "UK_TENANT_NAME", columnNames = {"dc_name"}), @UniqueConstraint(name = "UK_TENANT_SCHEMA", columnNames = {"dc_schema"})  })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries ({ 
	@NamedQuery(name = TenantEntity.GET_ALL, query = "select tn from TenantEntity as tn WHERE tn.disabled = false"),
	@NamedQuery(name = TenantEntity.GET_TENANT, query = "select tn from TenantEntity as tn WHERE tn.name = ?1"),
	@NamedQuery(name = TenantEntity.GET_TENANT_BY_ID, query = "select tn from TenantEntity as tn WHERE tn.id = ?1")

})
public class TenantEntity extends EntityInterface implements Serializable  {
	
	public static final String GET_ALL = "tenantEntity.getAll";
	public static final String GET_TENANT = "tenantEntity.getTenant";
	public static final String GET_TENANT_BY_ID = "tenantEntity.getTenantById";
	
	/*
	 *  ATTENTION
	 *  The column names are also used in JdbcUtils 
	 * 
	 * 
	 */
	

	@Id
	@TableGenerator(name = "coreSeqStoreTenant", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "TENANT.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreTenant")
	@Column(name = "dc_id")
	private Integer id;
	
	@Column(name = "dc_master", updatable = false)
	private boolean master;

	@Column(name = "dc_name", length = 32, nullable = false, updatable = true)
	@DcemGui
	@Pattern (regexp = "^[a-zA-Z][0-9a-zA-Z-]*$", message = "{tenant.config.name.invalid}"  )
	private String name;
		
	@Column(name = "dc_fullname", length = 255, updatable = true)
	@DcemGui
	private String fullName;
	
	@Column(name = "dc_schema", length=32, updatable = false)
	@Pattern (regexp = "^[a-zA-Z][0-9a-zA-Z_-]*$", message = "{tenant.config.dbname.invalid}" )
	@DcemGui
	private String schema;

	@Column(name = "dc_disabled", updatable = true)
	@DcemGui
	private boolean disabled;
		
	public TenantEntity() {
		
	}	
	
	// This is used in JdBC Utils
	public TenantEntity(Integer id, String schema, boolean disabled,  String fullName,  boolean master, String name) {
		super();
		this.id = id;
		this.schema = schema;
		this.master = master;
		this.name = name;
		this.fullName = fullName;
		this.disabled = disabled;
	}
	
	public TenantEntity(String name) {
		super();
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public boolean isEnabled() {
		return disabled;
	}

	public void setEnabled(boolean enabled) {
		this.disabled = enabled;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;		
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		TenantEntity other = (TenantEntity) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public String toString () {
		return name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
