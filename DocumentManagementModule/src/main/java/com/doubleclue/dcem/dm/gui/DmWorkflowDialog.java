package com.doubleclue.dcem.dm.gui;

import java.text.DateFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity;
import com.doubleclue.dcem.dm.logic.DmWorkflowLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.logic.WorkflowAction;
import com.doubleclue.dcem.dm.logic.WorkflowTrigger;

@Named("dmWorkflowDialog")
@SessionScoped
public class DmWorkflowDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(DmWorkflowDialog.class);

	@Inject
	private DmWorkflowLogic dmWorkflowEntityLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DmDocumentView dmDocumentView;

	@Inject
	DmWorkflowView workflowView;

	DcemUser dcemUser;
	DcemUser dcemUser2;
	DmWorkflowEntity workflowEntity;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/*
	* This method is called on OK button
	* @return true to close the dialog 
	* 
	*/
	@Override
	public boolean actionOk() throws Exception {
		workflowEntity.setUser(dcemUser);
		switch (workflowEntity.getWorkflowTrigger()) {
		case Added:
		case Modify:
			workflowEntity.setDay(-1);
			workflowEntity.setMonth(-1);
			workflowEntity.setLocalDate(null);
			break;
		case OnDate:
			if (workflowEntity.getLocalDate() == null) {
				JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "error.dateRequired");
				return false;
			}
			workflowEntity.setDay(0);
			workflowEntity.setMonth(-1);
			break;
		case Periodically_Monthly:
			workflowEntity.setMonth(-1);
			break;
		}
		dmWorkflowEntityLogic.addOrUpdate(workflowEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}
	
	public boolean isOwnerGroup() {
		return workflowEntity.getCloudSafeEntity().getOwner() == CloudSafeOwner.GROUP;
	}

	public List<SelectItem> getTriggers() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (WorkflowTrigger trigger : WorkflowTrigger.values()) {
			list.add(new SelectItem(trigger.name(), JsfUtils.getStringSafely(dmDocumentView.getResourceBundle(), "triggers." + trigger.name())));
		}
		return list;
	}

	public List<SelectItem> getActions() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		for (WorkflowAction action : WorkflowAction.values()) {
			if (action == WorkflowAction.MoveToTrash && workflowEntity.getWorkflowTrigger() != WorkflowTrigger.OnDate) {
				continue;
			}
			list.add(new SelectItem(action.name(), JsfUtils.getStringSafely(dmDocumentView.getResourceBundle(), "actions." + action.name())));
		}
		return list;
	}

	public List<SelectItem> getDaysOfWeek() {
		return Stream.of(DayOfWeek.values()).map(d -> new SelectItem(d.getValue(), d.getDisplayName(TextStyle.FULL, operatorSessionBean.getLocale())))
				.collect(Collectors.toList());
	}

	public List<SelectItem> getDaysOfMonth() {
		int lastDay = getLastDayOfMonth();
		List<SelectItem> list = new ArrayList<SelectItem>(lastDay);
		for (int i = 1; i <= lastDay; i++) {
			list.add(new SelectItem(i));
		}
		return list;
	}

	public List<SelectItem> getMonths() {
		String[] months = new DateFormatSymbols(operatorSessionBean.getLocale()).getMonths();
		List<SelectItem> list = new ArrayList<SelectItem>(months.length + 1);
		for (int i = 0; i < months.length - 1; i++) {
			list.add(new SelectItem(i + 1, months[i]));
		}
		return list;
	}

	private int getLastDayOfMonth() {
		if (workflowEntity.getMonth() == -1) {
			workflowEntity.setMonth(1);
		}
		LocalDate localDate = LocalDate.now().withMonth(workflowEntity.getMonth()).with(TemporalAdjusters.lastDayOfMonth());
		return localDate.getDayOfMonth();
	}

	public boolean isRenderWeekly() {
		return workflowEntity.getWorkflowTrigger() == WorkflowTrigger.Periodically_Weekly;
	}

	public boolean isRenderMonthly() {
		return workflowEntity.getWorkflowTrigger() == WorkflowTrigger.Periodically_Monthly;
	}

	public boolean isRenderYearly() {
		return workflowEntity.getWorkflowTrigger() == WorkflowTrigger.Periodically_Yearly;
	}

	public boolean isRenderOnDate() {
		return workflowEntity.getWorkflowTrigger() == WorkflowTrigger.OnDate;
	}

	// public String getHeight() {
	// return "650";
	// }

	// public String getWidth() {
	// return "1000";
	// }

	public String getDocumentName() {
		return workflowEntity.getCloudSafeEntity().getName();
	}

	/*
	* This method is called before Dialog is opened
	* 
	*/
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		// String action = this.getAutoViewAction().getDcemAction().getAction();
		workflowEntity = (DmWorkflowEntity) this.getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT)) {

		} else {
			workflowEntity.setWorkflowTrigger(WorkflowTrigger.Modify);
			workflowEntity.setWorkflowAction(WorkflowAction.Email);
			workflowEntity.setCloudSafeEntity(workflowView.getCloudSafeEntity());
		}
		if (workflowEntity.getUser() == null) {
			workflowEntity.setUser(operatorSessionBean.getDcemUser());
		}
		dcemUser = workflowEntity.getUser();
		dcemUser2 = workflowEntity.getUser2();
	}

	public void leaving() {
		/*
		*  clear local variables
		*/
	}

	public void listenTrigger() {
		// System.out.println("DmWorkflowDialog.listenTrigger()");
	}

	public String getWorkflowTriggerName() {
		return workflowEntity.getWorkflowTrigger().name();
	}

	public void setWorkflowTriggerName(String text) {
		workflowEntity.setWorkflowTrigger(WorkflowTrigger.valueOf(text));
	}

	public String getWorkflowActionName() {
		return workflowEntity.getWorkflowAction().name();
	}

	public void setWorkflowActionName(String text) {
		workflowEntity.setWorkflowAction(WorkflowAction.valueOf(text));
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	public DcemUser getDcemUser2() {
		return dcemUser2;
	}

	public void setDcemUser2(DcemUser dcemUser2) {
		this.dcemUser2 = dcemUser2;
	}

	public DmWorkflowEntity getWorkflowEntity() {
		return workflowEntity;
	}

	public void setWorkflowEntity(DmWorkflowEntity workflowEntity) {
		this.workflowEntity = workflowEntity;
	}
}
