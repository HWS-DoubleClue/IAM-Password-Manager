package com.doubleclue.dcem.as.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

@SuppressWarnings("serial")
@Entity
@Table(name = "as_cloudsafelimit")
@NamedQueries({
		@NamedQuery(name = CloudSafeLimitEntity.SET_LIMIT_FOR_USERS, query = "UPDATE CloudSafeLimitEntity b SET b.limit = ?1, b.expiryDate = ?2, b.passwordSafeEnabled = ?3 WHERE b.id IN (?4)"),
		@NamedQuery(name = CloudSafeLimitEntity.GET_LIMIT_REACHING_USERNAMES, query = "SELECT u.loginId FROM CloudSafeLimitEntity b INNER JOIN b.user u WHERE b.used >= b.limit"),
		@NamedQuery(name = CloudSafeLimitEntity.GET_TOTAL_USED, query = "SELECT SUM(b.used) FROM CloudSafeLimitEntity b"),
		@NamedQuery(name = CloudSafeLimitEntity.GET_EXISTING_USERS, query = "SELECT b.id FROM CloudSafeLimitEntity b WHERE b.id IN (?1)"),
		@NamedQuery(name = CloudSafeLimitEntity.DELETE_USER, query = "DELETE FROM CloudSafeLimitEntity limit WHERE limit.user=?1") })
public class CloudSafeLimitEntity extends EntityInterface implements Serializable {

	public static final String SET_LIMIT_FOR_USERS = "CloudSafeBillingEntity.setLimitForUsers";
	public static final String GET_LIMIT_REACHING_USERNAMES = "CloudSafeBillingEntity.getLimitBreachingUsernames";
	public static final String GET_TOTAL_USED = "CloudSafeBillingEntity.getTotalUsed";
	public static final String GET_EXISTING_USERS = "CloudSafeBillingEntity.getExistingUsers";
	public static final String DELETE_USER = "CloudSafeLimitEntity.deleteUser";

	@Id
	private Integer id;

	@DcemGui(subClass = "loginId")
	@OneToOne
	@JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "FK_CLOUDSAFE_LIMIT"), insertable = true, updatable = false)
	@MapsId
	private DcemUser user;

	@Column(name = "dc_limit", nullable = false)
	private long limit = 0;

	@Column(name = "dc_used", nullable = false)
	private long used = 0;

	@DcemGui(name = "Used")
	@Transient
	private String usedString;

	@DcemGui(name = "Limit")
	@Transient
	private String limitString;

	@Column(name = "expiry_date")
	@DcemGui
	private LocalDateTime expiryDate;

	@Column(name = "ps_enabled", nullable = false)
	@DcemGui
	private boolean passwordSafeEnabled = false;

	public CloudSafeLimitEntity() {
	}

	public CloudSafeLimitEntity(DcemUser user, long limit, long used, LocalDateTime expiryDate, boolean passwordSafeEnabled) {
		this.user = user;
		this.limit = limit;
		this.used = used;
		this.expiryDate = expiryDate;
		this.passwordSafeEnabled = passwordSafeEnabled;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = id.intValue();
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public long getLimit() {
		return limit;
	}

	public void setLimit(long limit) {
		this.limit = limit;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public String getUsedString() {
		if (usedString == null) {
			usedString = DataUnit.getByteCountAsString(used);
		}
		return usedString;
	}

	public void setUsedString(String usedString) {
		this.usedString = usedString;
	}

	public String getLimitString() {
		if (limitString == null) {
			limitString = DataUnit.getByteCountAsString(limit);
		}
		return limitString;
	}

	public void setLimitString(String limitString) {
		this.limitString = limitString;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isPasswordSafeEnabled() {
		return passwordSafeEnabled;
	}

	public void setPasswordSafeEnabled(boolean passwordSafeEnabled) {
		this.passwordSafeEnabled = passwordSafeEnabled;
	}
}
