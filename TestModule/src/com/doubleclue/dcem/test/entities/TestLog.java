package com.doubleclue.dcem.test.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.test.logic.TestLogAction;


/**
 * The persistent class for logging
 * @author Emanuel Galea
 * 
 */
@Entity
@Table(name="test_log" )


public class TestLog extends EntityInterface {
	
	@Id
	@Column(name = "dc_id")
	@TableGenerator( name = "coreSeqStoreTestLog", table = "core_seq", pkColumnName = "seq_name", pkColumnValue="TEST_LOG.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "coreSeqStoreTestLog" )
//	@DcemGui (columnWidth="45px", displayMode=DisplayModes.INPUT_DISABLED)
    private Integer id;
	
    @Column(name = "dc_time", nullable=false)
    @DcemGui (sortOrder=SortOrder.DESCENDING )
	private LocalDateTime time;	
 
    @DcemGui
    @Column(length = 128, nullable = false)
    private String testUnit;
    
    @Enumerated(EnumType.ORDINAL)
    @DcemGui
    private TestLogAction action;
	
	@Column(length = 1024, nullable = true)
	private String details;

	public Number getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer)id;		
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getTestUnit() {
		return testUnit;
	}

	public void setTestUnit(String testUnit) {
		this.testUnit = testUnit;
	}

	public TestLogAction getAction() {
		return action;
	}

	public void setAction(TestLogAction action) {
		this.action = action;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
	

	
	
}