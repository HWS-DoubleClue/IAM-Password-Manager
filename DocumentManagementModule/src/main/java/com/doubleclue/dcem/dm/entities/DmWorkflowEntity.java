package com.doubleclue.dcem.dm.entities;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.dm.logic.WorkflowAction;
import com.doubleclue.dcem.dm.logic.WorkflowTrigger;

/**
 * The persistent class for the app_version database table.
 * 
 */
@Entity
@Table(name = "dm_workflow", uniqueConstraints = @UniqueConstraint(name = "UK_WORKLFOW_NAME", columnNames = { "dc_name", "cloudSafeEntity" }))
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

@NamedQueries({
		@NamedQuery(name = DmWorkflowEntity.DELETE_FOR_DOCUMENT, query = "DELETE from DmWorkflowEntity wf WHERE wf.cloudSafeEntity.id = ?1"),
		@NamedQuery(name = DmWorkflowEntity.GET_DOCUMENT_TRIGGER_LIST, query = "SELECT wf from DmWorkflowEntity wf WHERE (wf.cloudSafeEntity = ?1) AND workflowTrigger = ?2"),
		@NamedQuery(name = DmWorkflowEntity.GET_TIME_TRIGGER_LIST, query = "SELECT wf from DmWorkflowEntity wf WHERE"
				+ " (wf.workflowTrigger = com.doubleclue.dcem.dm.logic.WorkflowTrigger.OnDate AND wf.localDate = CURRENT_DATE) OR "
				+ " (wf.workflowTrigger = com.doubleclue.dcem.dm.logic.WorkflowTrigger.Periodically_Weekly AND wf.day = ?1) OR "
				+ " (wf.workflowTrigger = com.doubleclue.dcem.dm.logic.WorkflowTrigger.Periodically_Monthly AND wf.day = ?2 ) OR "
				+ " (wf.workflowTrigger = com.doubleclue.dcem.dm.logic.WorkflowTrigger.Periodically_Yearly AND wf.day = ?2 AND wf.month = ? 3)") })

public class DmWorkflowEntity extends EntityInterface implements Cloneable {

	public final static String GET_DOCUMENT_TRIGGER_LIST = "DmWorkflowEntity.docTriggerList";
	public final static String GET_TIME_TRIGGER_LIST = "DmWorkflowEntity.triggerList";
	public final static String DELETE_FOR_DOCUMENT = "DmWorkflowEntity.deleteForDocument";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui(name = "document", subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cloudSafeEntity", nullable = false, foreignKey = @ForeignKey(name = "FK_DM_WORKFLOW_CLOUDSAFE"), insertable = true, updatable = false)
	private CloudSafeEntity cloudSafeEntity;

	@Column(name = "dc_name", length = 128)
	@DcemGui(name = "name")
	private String name;

	@DcemGui(name = "user", subClass = "displayName")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false, foreignKey = @ForeignKey(name = "FK_DM_WORKFLOW_USER"), insertable = true, updatable = true)
	private DcemUser user;

	@DcemGui(name = "user 2", subClass = "displayName")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_DM_WORKFLOW_USER2"), insertable = true, updatable = true)
	private DcemUser user2;

	@DcemGui
	boolean groupMembers;

	@Enumerated(EnumType.ORDINAL)
	@DcemGui()
	private WorkflowTrigger workflowTrigger;

	@DcemGui()
	@Enumerated(EnumType.ORDINAL)
	private WorkflowAction workflowAction;

	int day = 0;

	@DcemGui(name = "Day")
	@Transient
	String dayText;

	int month = 1;

	@DcemGui(name = "month")
	@Transient
	String monthText;

	@DcemGui(name = "On Date")
	LocalDate localDate;

	@Column(length = 255)
	String description;
	
	@Transient
	CloudSafeEntity childCloudSafeEntity;

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			id = null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void setCloudSafeEntity(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;
	}

	public WorkflowTrigger getWorkflowTrigger() {
		return workflowTrigger;
	}

	public void setWorkflowTrigger(WorkflowTrigger workflowTrigger) {
		this.workflowTrigger = workflowTrigger;
	}

	public WorkflowAction getWorkflowAction() {
		return workflowAction;
	}

	public void setWorkflowAction(WorkflowAction workflowAction) {
		this.workflowAction = workflowAction;
	}

	public String getDayText() {
		if (day < 1) {
			return null;
		}
		if (workflowTrigger == WorkflowTrigger.Periodically_Weekly) {
			return DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale.getDefault());
		}
		return Integer.toString(day);
	}

	public String getMonthText() {
		if (month < 0) {
			return null;
		}
		String[] months = new DateFormatSymbols(Locale.getDefault()).getMonths();
		return months[month];
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public LocalDate getLocalDate() {
		return localDate;
	}

	public void setLocalDate(LocalDate localDate) {
		this.localDate = localDate;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public DcemUser getUser2() {
		return user2;
	}

	public void setUser2(DcemUser user2) {
		this.user2 = user2;
	}

	public boolean isGroupMembers() {
		return groupMembers;
	}

	public void setGroupMembers(boolean groupMembers) {
		this.groupMembers = groupMembers;
	}

	public CloudSafeEntity getChildCloudSafeEntity() {
		return childCloudSafeEntity;
	}

	public void setChildCloudSafeEntity(CloudSafeEntity childCloudSafeEntity) {
		this.childCloudSafeEntity = childCloudSafeEntity;
	}

}