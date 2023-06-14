package com.doubleclue.dcem.core.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.DbEncryptConverter;
import com.doubleclue.dcem.core.utils.DisplayModes;

/**
 * The persistent class for the kernel_versions database table.
 * 
 */
@Entity
@Table(name = "sys_keystore")
@NamedQueries({ 
@NamedQuery(name = KeyStoreEntity.GET_BY_PURPOSE_NODE, query = "select ks from KeyStoreEntity as ks where ks.purpose = ?1 and ks.node=?2"),
@NamedQuery(name = KeyStoreEntity.GET_BY_PURPOSE, query = "select ks from KeyStoreEntity as ks where ks.purpose = ?1")
})

public class KeyStoreEntity extends EntityInterface {

	public final static String GET_BY_PURPOSE_NODE = "KeyStore.byPurposeNode";
	public final static String GET_BY_PURPOSE = "KeyStore.byPurpose";

	@Id
	@TableGenerator(name = "coreSeqStoreKeyStore", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "KEYSTORE.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreKeyStore")
	@Column(name = "dc_id")
	private Integer id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_KEYSTORE_NODE"), name = "dc_node", nullable = true, insertable = true, updatable = false)
	@DcemGui(subClass = "name", displayMode = DisplayModes.TABLE_ONLY, sortOrder=SortOrder.ASCENDING)
	private DcemNode node;

	@Column(name = "purpose", length = 32)
	@Enumerated(EnumType.ORDINAL)
	@DcemGui
	private KeyStorePurpose purpose;
	

	@DcemGui
	String cn;

	@DcemGui
	String ipAddress;

	@DcemGui
	@Temporal(TemporalType.TIMESTAMP)
	Date expiresOn;
	
//	@Lob
	@Column (length = 32*1024)
	private byte[] keyStore;

	boolean disabled = false;

	@Convert(converter = DbEncryptConverter.class)
	private String password;

	public KeyStoreEntity() {

	}

	public KeyStoreEntity(KeyStorePurpose purpose, byte[] keyStore, String cn, Date expiresOn, String password,
			DcemNode dcemNode) {
		super();
		this.purpose = purpose;
		this.keyStore = keyStore;
		this.cn = cn;
		this.expiresOn = expiresOn;
		this.password = password;
		this.node = dcemNode;
	}

	
	public KeyStorePurpose getPurpose() {
		return purpose;
	}

	public void setPurpose(KeyStorePurpose purpose) {
		this.purpose = purpose;
	}

	public DcemNode getNode() {
		return node;
	}

	public void setNode(DcemNode node) {
		this.node = node;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getCn() {
		return cn;
	}

	public void setCn(String cn) {
		this.cn = cn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(Date expiresOn) {
		this.expiresOn = expiresOn;
	}

	public byte[] getKeyStore() {
		return keyStore;
	}

	public void setKeyStore(byte[] keyStore) {
		this.keyStore = keyStore;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	@Override
	public String toString() {
		return "KeyStoreEntity [node=" + node + ", purpose=" + purpose + ", cn=" + cn + ", ipAddress=" + ipAddress + ", expiresOn=" + expiresOn + ", disabled="
				+ disabled + "]";
	}

	
	
	

}