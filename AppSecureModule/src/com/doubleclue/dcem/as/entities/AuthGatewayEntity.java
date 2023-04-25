package com.doubleclue.dcem.as.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;
import com.doubleclue.dcem.core.jpa.FilterOperator;

/**
 *  
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "as_authApp", uniqueConstraints = @UniqueConstraint(name = "UK_AUTHAPP_NAME", columnNames = { "dc_name" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({ @NamedQuery(name = AuthGatewayEntity.GET_GATEWAY, query = "SELECT aa FROM AuthGatewayEntity aa where aa.name = ?1 AND aa.disabled = ?2"),
		@NamedQuery(name = AuthGatewayEntity.GET_ALL_AUTHGATEWAYS, query = "SELECT aa FROM AuthGatewayEntity aa where aa.disabled = false ORDER BY aa.name")

})

public class AuthGatewayEntity extends EntityInterface {

	public final static String GET_GATEWAY = "AuthGateway.getAuthApp";

	public static final String GET_ALL_AUTHGATEWAYS = "AuthGateway.all";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAppAuthApp", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "APP_AUTHAPP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAppAuthApp")
	private Integer id;

	@DcemGui
	@Column(name = "dc_name", length = 64)
	@Size(min = 2, max = 64)
	private String name;

	@DcemGui()
	boolean disabled;

	@DcemGui
	@Min(0)
	@Max(20)
	private int retryCounter = 0;

	@Column(length = 32, updatable = false)
	@Convert(converter = DbEncryptConverterBinary.class)
	private byte[] sharedKey;

	public AuthGatewayEntity() {
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getRetryCounter() {
		return retryCounter;
	}

	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}

	public byte[] getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(byte[] sharedKey) {
		this.sharedKey = sharedKey;
	}

	@Override
	public String toString() {
		return "AuthAppEntity [name=" + name + ", disabled=" + disabled + ", retryCounter=" + retryCounter + "]";
	}

	public void addRetryCounter() {
		retryCounter++;
	}

}