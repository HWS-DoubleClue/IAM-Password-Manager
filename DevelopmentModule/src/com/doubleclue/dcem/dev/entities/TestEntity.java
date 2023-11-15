package com.doubleclue.dcem.dev.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.doubleclue.dcem.dev.logic.DevObjectTypes;


/**
 * The persistent class for logging
 * @author Emanuel Galea
 * 
 */
//@Entity
@Table(name="dev_test" )
public class TestEntity extends EntityInterface {
	
	@Id
	@Column(name = "dc_id")
	@DcemGui (displayMode = DisplayModes.TABLE_ONLY)
    private Integer id;
	
    @Column(name = "dc_datetime", nullable=false)
    @DcemGui (sortOrder=SortOrder.DESCENDING )
	private LocalDateTime time;	
 
    @DcemGui
    @Column(length = 128, nullable = false)
    private String testUnit;
    
    @DcemGui
	@Column(length = 1024, nullable = true)
	private String details;
	
	@DcemGui
	boolean activate;
	
	@DcemGui
	DevObjectTypes devObjectTypes;
	

	@DcemGui
	@Column (name = "dc_localdate")
	LocalDate localDate;
	
	@DcemGui
	@Column (name = "dc_localTime")
	LocalTime localTime;
	
	@DcemGui
	@Column (name = "dc_number")
	int number;	
	

	public Number getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer)id;		
	}

	public LocalDateTime getTime() {
		return time;
	}

	public void setTime(LocalDateTime time) {
		this.time = time;
	}

	public String getTestUnit() {
		return testUnit;
	}

	public void setTestUnit(String testUnit) {
		this.testUnit = testUnit;
	}

	
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public DevObjectTypes getDevObjectTypes() {
		return devObjectTypes;
	}

	public void setDevObjectTypes(DevObjectTypes devObjectTypes) {
		this.devObjectTypes = devObjectTypes;
	}

	
	public LocalDate getLocalDate() {
		return localDate;
	}

	public void setLocalDate(LocalDate localDate) {
		this.localDate = localDate;
	}

	
	public boolean isActivate() {
		return activate;
	}

	public void setActivate(boolean activate) {
		this.activate = activate;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public LocalTime getLocalTime() {
		return localTime;
	}

	public void setLocalTime(LocalTime localTime) {
		this.localTime = localTime;
	}
	

	
	
}