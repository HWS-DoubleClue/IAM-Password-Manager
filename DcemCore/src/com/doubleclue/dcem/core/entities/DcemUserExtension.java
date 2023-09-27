package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.util.TimeZone;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * The persistent class for user
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "core_userext")
@NamedQueries({
		@NamedQuery(name = DcemUserExtension.DELETE_USER_EXTENSION, query = "DELETE FROM DcemUserExtension ex where ex.id = ?1"), })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DcemUserExtension extends EntityInterface implements Serializable {

	public final static String DELETE_USER_EXTENSION = "DcemUserExtension.deleteUserExtension";
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "dc_userext_id")
	private Integer id;

	@DcemGui(name = "Country")
	@Column(length = 255, name = "dc_country", nullable = true, updatable = true, insertable = true)
	String country;

	@DcemGui(name = "Job Title")
	@Column(length = 128, name = "jobTitle", nullable = true, updatable = true, insertable = true)
	String jobTitle;

	@Column(length = 255, name = "dc_timezone", nullable = true, updatable = true, insertable = true)
	private String timezoneString;

	@DcemGui(name = "Photo")
	@Column(length = 8096 * 2, nullable = true)
	private byte[] photo;

	@DcemGui(name = "Department", subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_DEPARTMENT_USEREXT_ID"), name = "departmentid", nullable = true, insertable = true, updatable = true)
	private DepartmentEntity department;

	// @DcemGui(name = "Country")
	@Transient
	String countryDisplayName;

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public byte[] getPhoto() {
		return photo;
	}

	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public TimeZone getTimezone() {
		if (timezoneString == null) {
			return null;
		}
		return TimeZone.getTimeZone(timezoneString);
	}

	public void setTimezone(TimeZone timezone) {
		if (timezone == null) {
			this.timezoneString = null;
		} else {
			this.timezoneString = timezone.getID();
		}
	}

	public DepartmentEntity getDepartment() {
		return department;
	}

	public void setDepartment(DepartmentEntity department) {
		this.department = department;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", country=" + country + ", timezone=" + timezoneString + ", department=" + department
				+ "]";
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getTimezoneString() {
		return timezoneString;
	}

	public void setTimezoneString(String timezoneString) {
		this.timezoneString = timezoneString;
	}

//	public DcemUser getDcemUser() {
//		return dcemUser;
//	}
//
//	public void setDcemUser(DcemUser dcemUser) {
//		this.dcemUser = dcemUser;
//	}

}