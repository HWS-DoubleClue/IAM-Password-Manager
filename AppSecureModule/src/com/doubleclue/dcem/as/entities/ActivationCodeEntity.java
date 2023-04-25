package com.doubleclue.dcem.as.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.DbEncryptConverter;
import com.doubleclue.dcem.core.utils.DisplayModes;

/**
 * The persistent class for the activationcode database table.
 * @author Emanuel Galea
 */
@Entity
@Table(name = "as_activationcode")

@NamedQueries({
		// @NamedQuery(name = ActivationCodeEntity.GET_USER_ACTIVATION, query = "SELECT ac FROM ActivationCodeEntity ac where ac.user.loginId = ?1"),
		@NamedQuery(name = ActivationCodeEntity.DELETE_USER_ACTIVATION, query = "DELETE FROM ActivationCodeEntity ac where ac.user = ?1"),
		@NamedQuery(name = ActivationCodeEntity.VALID_CODES, query = "SELECT ac FROM ActivationCodeEntity ac where ac.user = ?1 AND ac.validTill > ?2"),
		@NamedQuery(name = ActivationCodeEntity.DELETE_EXPIRED, query = "DELETE FROM ActivationCodeEntity ac WHERE ac.validTill < ?1") })

public class ActivationCodeEntity extends EntityInterface {

	// public final static String GET_USER_ACTIVATION = "activationCode.getUserActivation";
	public final static String DELETE_USER_ACTIVATION = "activationCode.deleteUserActivation";

	public final static String VALID_CODES = "activationCode.validCodes";
	public final static String DELETE_EXPIRED = "activationCode.deleteExpired";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAppAc", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "APP_AC.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAppAc")
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, foreignKey = @ForeignKey(name = "FK_APP_AC_USER"), insertable = true, updatable = true)
	@DcemGui(subClass = "loginId", name = "user")
	private DcemUser user;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	private Date createdOn;

	@Column(nullable = false, length = 255)
	@Convert(converter = DbEncryptConverter.class)
	private String activationCode;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@DcemGui
	private Date validTill;

	@Column(nullable = true, length = 255)
	@DcemGui
	private String info;

	public ActivationCodeEntity() {
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public String toString() {
		return user.getLoginId() + " created: " + createdOn.toString();
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}

}