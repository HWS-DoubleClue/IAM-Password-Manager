package com.doubleclue.dcem.radius.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.radius.logic.RadiusReportAction;

/**
 * The persistent class for logging
 * 
 * @author Emanuel Galea
 * 
 */
@Entity
@Table(name = "radius_report")
@NamedQueries({
		@NamedQuery(name = RadiusReportEntity.GET_TIMESTAMPS, query = "SELECT rr.time FROM RadiusReportEntity AS rr ORDER BY rr.time ASC"),
		@NamedQuery(name = RadiusReportEntity.GET_AFTER, query = "SELECT rp FROM RadiusReportEntity rp where rp.time < ?1"),
		@NamedQuery(name = RadiusReportEntity.DELETE_AFTER, query = "DELETE FROM RadiusReportEntity rp where rp.time < ?1") })

public class RadiusReportEntity extends EntityInterface {

	public static final String GET_TIMESTAMPS = "radiusReportEntity.getTimestamps";

	public static final String GET_AFTER = "radiusReportEntity.getAfter";
	public static final String DELETE_AFTER = "radiusReportEntity.deleteAfter";

	public RadiusReportEntity() {

	}

	public RadiusReportEntity(String nasClientName, RadiusReportAction action, String details) {
		super();
		this.nasClientName = nasClientName;
		this.action = action;
		this.details = details;
		time = new Date();
//		error = true;
	}

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreRadiusRep", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "RADIUS_REP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreRadiusRep")
	// @DcemGui (columnWidth="45px", displayMode=DisplayModes.INPUT_DISABLED)
	private Integer id;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "dc_time", nullable = false)
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	private Date time;

	@DcemGui
	@Column(length = 128, nullable = true)
	private String nasClientName;

	@Enumerated(EnumType.ORDINAL)
	@DcemGui
	private RadiusReportAction action;

//	@DcemGui
	boolean error;

	@Column(length = 1024, nullable = true)
	@DcemGui
	private String details;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public RadiusReportAction getAction() {
		return action;
	}

	public void setAction(RadiusReportAction action) {
		this.action = action;
	}

	public String getNasClientName() {
		return nasClientName;
	}

	public void setNasClientName(String nasClientName) {
		this.nasClientName = nasClientName;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	@Override
	public String toString() {
		return "RadiusReportEntity nasClientName=" + nasClientName + ", action=" + action + ", details=" + details
				+ "]";
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}

}