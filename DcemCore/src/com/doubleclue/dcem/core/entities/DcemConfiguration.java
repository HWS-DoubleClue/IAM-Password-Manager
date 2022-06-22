package com.doubleclue.dcem.core.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
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

import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;

@Entity
@Table(name = "core_config",  uniqueConstraints= @UniqueConstraint(name="UK_CONFIG_NAME", columnNames={"moduleId", "dc_key"}))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = DcemConfiguration.MODULE_KEY, query = "select sc from DcemConfiguration as sc where sc.moduleId=:moduleId AND sc.key=:key", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query.DcemConfig.moduleKey") }),
		@NamedQuery(name = DcemConfiguration.KEY_ONLY, query = "select sc from DcemConfiguration as sc where sc.key=:key"),

})
public class DcemConfiguration extends EntityInterface {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String MODULE_KEY = "DcemConfig.moduleKey";
	public static final String KEY_ONLY = "DcemConfig.keyOnly";
	
	
	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreDcemConfig", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "SEM_CONFIG.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreDcemConfig")
	private Integer id;

	@Column(length = 64, nullable = true)
	protected String nodeId;

	@Column(length = 64, nullable = true)
	protected String moduleId;

	@Column(name = "dc_key", length = 128, nullable = false)
	protected String key;

//	@Lob
	@Column(name = "dc_value", nullable = false)
	@Convert(converter = DbEncryptConverterBinary.class)
	protected byte[] value;


	public DcemConfiguration() {

	}

	/**
	 * @param moduleId
	 * @param key
	 * @param value
	 * @param encrypted
	 */
	public DcemConfiguration(String moduleId, String key, byte[] value) {
		super();
		this.moduleId = moduleId;
		this.key = key;
		this.value = value;
		
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "DcemConfiguration [id=" + id + ", moduleId=" + moduleId + ", key=" + key + "]";
	}
	
}
