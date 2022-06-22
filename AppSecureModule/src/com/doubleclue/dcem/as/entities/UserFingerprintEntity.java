package com.doubleclue.dcem.as.entities;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The persistent class for the KERNEL_AUDITING database table.
 * 
 */
@Entity
@Table(name = "as_userfingerprint")
@NamedQueries ({ 
@NamedQuery(name=UserFingerprintEntity.DELETE_USER_FP, query="DELETE FROM UserFingerprintEntity uf where uf.id.userId = ?1"),
@NamedQuery(name=UserFingerprintEntity.DELETE_EXPIRED_FP, query="DELETE FROM UserFingerprintEntity uf where uf.timestamp < ?1"),


})
public class UserFingerprintEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String DELETE_USER_FP = "UserFingerprintEntity.delteUserFp";
	public static final String DELETE_EXPIRED_FP = "UserFingerprintEntity.delteExpired";


	@EmbeddedId
	private FingerprintId id;

	@Column(name = "timeStamp")
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;

	String fingerprint;

	public UserFingerprintEntity() {
		super();
	}

	public UserFingerprintEntity(FingerprintId id, String fingerprint, Date timestamp) {
		super();
		this.id = id;
		this.fingerprint = fingerprint;
		this.timestamp = timestamp;
	}
	
	public UserFingerprintEntity(FingerprintId id, String fingerprint, int validForMinutes) {
		super();
		this.id = id;
		this.fingerprint = fingerprint;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, validForMinutes);	
		this.timestamp = calendar.getTime();
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public FingerprintId getId() {
		return id;
	}

	public void setId(FingerprintId id) {
		this.id = id;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	@Override
	public String toString() {
		return "UserFingerprintEntity [id=" + id + ", timestamp=" + timestamp + ", fingerprint=" + fingerprint + "]";
	}
}