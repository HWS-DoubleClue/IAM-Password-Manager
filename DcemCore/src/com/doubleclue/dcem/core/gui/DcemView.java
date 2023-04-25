package com.doubleclue.dcem.core.gui;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.RoleRestriction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.JpaLazyModel;
import com.doubleclue.dcem.core.logic.ActionType;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.PredefinedFilter;
import com.doubleclue.dcem.core.logic.RoleRestrictionLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;

public abstract class DcemView implements Serializable {

	protected static final Logger logger = LogManager.getLogger(DcemView.class);
	/**
	 * 
	 */
	@Inject
	protected AutoViewBean autoViewBean;

	@Inject
	protected ViewNavigator viewNavigator;

	@Inject
	protected OperatorSessionBean operatorSessionBean;

	@Inject
	protected EntityManager em;

	@Inject
	private AutoDialogBean autoDialogBean;

	@Inject
	protected DcemApplicationBean dcemApplication;

	@Inject
	PredefinedFilterDialog predefinedFilterDialog;

	@Inject
	RoleRestrictionLogic roleRestrictionLogic;

	DcemAction viewDcemAction;

	protected SubjectAbs subject;

	protected JpaLazyModel<?> lazyModel;

	protected List<AutoViewAction> autoViewActions;

	List<DcemAction> viewDcemActions;
	
	DcemAction revealAction;
	DcemAction manageAction;

	protected List<PredefinedFilter> predefinedFilters;

	List<Object> selection;

	int predefinedFilterId;

	protected AutoViewAction predefinedFilterAction;

	List<ViewVariable> viewVariables;
	List<ViewVariable> displayViewVariables;

	private static final long serialVersionUID = -1394282981492456785L;

	private DcemDialog activeDialog = null;

	protected Object actionObject;
	protected Object actionSubObject;
	boolean dirty;
	protected int maxExport = 1000;

	public List<AutoViewAction> getViewActions() {
		return autoViewActions;
	}
	


	public void closeDialog() {
		if (activeDialog != null) {
			activeDialog.leavingDialog();
			activeDialog = null;
			PrimeFaces.current().dialog().closeDynamic(null);
		}
	}

	public DcemDialog getActiveDialog() {
		return activeDialog;
	}

	public String getDisplayName() {
		return subject.getDisplayName();
	}

	public void triggerAction(AutoViewAction autoViewAction) {

		if (autoViewAction == null) {
			return;
		}
		selection = autoViewBean.getSelectedItems();
		Object selectedObject = null;
		switch (autoViewAction.getRawAction().getActionSelection()) {
		case ONE_ONLY:
			if (selection == null || selection.size() == 0) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "SELECT_ROW", (Object[]) null);
				activeDialog = null;
				return;
			} else if (selection.size() > 1) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "SELECT_ROW", (Object[]) null);
				activeDialog = null;
				return;
			}
			selectedObject = selection.iterator().next();
			break;

		case ONE_OR_MORE:
			if (selection == null || selection.size() == 0) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "MULTIPLE_ROW_SELECTION", (Object[]) null);
				activeDialog = null;
				return;
			}
			break;

		case IGNORE:
		default:
			break;
		}

		ActionType actioType = autoViewAction.getRawAction().getActionType();
		if (actioType == ActionType.VIEW_LINK) {
			this.actionViewLink(autoViewAction);
			return;
		}

		if (actioType == ActionType.EL_METHOD) {
			try {
				setActionObject(selectedObject);
				if (autoViewAction.getDcemDialog() != null) {
					autoViewAction.getDcemDialog().setAutoViewAction(autoViewAction);
				}
				DcemUtils.evalAsObject(autoViewAction.getRawAction().getElMethodExpression());
			} catch (Exception exp) {
				logger.warn("EL Method Failed", exp);
				JsfUtils.addErrorMessage(exp.toString());
			}
			return;
		}
		if (actioType == ActionType.EXCEL_EXPORT_ALL) {
			try {
				setActionObject(selectedObject);
				excelExport(lazyModel, displayViewVariables, actioType);
				// DcemUtils.evalAsObject(autoViewAction.getRawAction().getElMethodExpression());
			} catch (Exception exp) {
				logger.warn("EL Method Failed", exp);
				JsfUtils.addErrorMessage(exp.toString());
			}
			return;
		}

		Object subObject = null;
		if (actioType == ActionType.CREATE_OBJECT) {
			selectedObject = viewNavigator.getActiveView().createActionObject();
		}
		subObject = selectedObject;
		activeDialog = autoViewAction.getDcemDialog();
		
		if (actioType == ActionType.DIALOG || actioType == ActionType.CREATE_OBJECT) {
			if (autoDialogBean != null) {
				setActionObject(selectedObject);
				setActionSubObject(subObject);
			}
			if (autoViewAction.getDcemDialog() != null) {
				autoViewAction.getDcemDialog().setActionObject(selectedObject);
				autoViewAction.getDcemDialog().setSubActionObject(subObject);
			}
			if (selectedObject != null && autoViewAction.xhtmlPage.endsWith(DcemConstants.AUTO_DIALOG_PATH)) {
				autoDialogBean.populatePanelGrid();
			}
		}
		autoViewAction.getDcemDialog().setAutoViewAction(autoViewAction);
		try {
			autoViewAction.getDcemDialog().show(this, autoViewAction);
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			return;
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(exp.toString());
			return;
		}
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		// options.put("width", 840);
		// options.put("height", 540);
		// options.put("contentWidth", "740");
		// options.put("contentHeight", "auto");
		options.put("position", "top");
		options.put("headerElement", "customheader");
		if (activeDialog.getHeight() != null) {
			options.put("height", activeDialog.getHeight());
		}
		if (activeDialog.getWidth() != null) {
			options.put("width", activeDialog.getWidth());
		}
		options.put("contentWidth", activeDialog.getWidth());
		options.put("responsive", true);
		// options.put("closable", false);
		PrimeFaces.current().dialog().openDynamic(DcemConstants.WEB_MGT_CONTEXT + autoViewAction.getXhtmlPage(), options, null);
		// RequestContext.getCurrentInstance().openDialog(DcemConstants.WEB_MGT_CONTEXT + autoViewAction.getXhtmlPage(),
		// options, null);
	}

	/**
	 * @throws Exception 
	 */
	private void actionViewLink(AutoViewAction autoViewAction) {

		ViewLink viewLink = autoViewAction.getViewLink();
		if (viewLink == null) {
			logger.warn("Missing ViewLink");
			return;
		}
		viewNavigator.setActiveView(viewLink.getDestSubject().getModuleId() + DcemConstants.MODULE_VIEW_SPLITTER + viewLink.getDestSubject().getViewName());
		List<Object> selection = autoViewBean.getSelectedItems();
		if (selection != null && selection.size() == 1) {
			DcemView newDcemView = viewNavigator.getActiveView();
			List<ViewVariable> destViewVariables = newDcemView.getViewVariables();
			ViewVariable originViewVariable = DcemUtils.getViewVariableFromId(viewVariables, viewLink.getOriginVariable());

			for (ViewVariable destViewVariable : destViewVariables) {
				if (destViewVariable.getId().equals(viewLink.destinationFilterName)) {
					if (originViewVariable != null) {
						try {
							PropertyDescriptor pd = new PropertyDescriptor(viewLink.getOriginVariable(), selection.get(0).getClass());
							Method getterMethod = pd.getReadMethod();
							destViewVariable.setFilterValue(getterMethod.invoke(selection.get(0)));
							// viewVariable.setFilterValue(selection.get(0).getClass().getDeclaredField(viewLink.destinationFilterName).get(selection.get(0)));
						} catch (Exception exp) {
							logger.warn("Missing destination view filter", exp);
						}
						// viewVariable.setFilterValue(((KeyGen) selection.get(0)).getBatchNo());
						destViewVariable.getFilterItem().setFilterOperator(FilterOperator.EQUALS);
					} else {

					}
				} else {
					destViewVariable.setFilterValue(null);
				}
			}

			ViewVariable destViewVariable = DcemUtils.getViewVariableFromId(destViewVariables, viewLink.destinationFilterName);
			if (destViewVariable == null) {
				logger.warn("Destination Variable-Link Invalide" + viewLink.destinationFilterName);
				return;
			}

		}
	}

	public List<AutoViewAction> getAutoViewActions() {
		return autoViewActions;
	}

	public void setAutoViewActions(List<AutoViewAction> autoViewActions) {
		this.autoViewActions = autoViewActions;
	}

	public SubjectAbs getSubject() {
		return subject;
	}

	public void setSubject(SubjectAbs subject) {
		this.subject = subject;
	}

	public void reload() {
		autoViewBean.reload();
	}

	public List<ViewVariable> getViewVariables() {
		if (viewVariables == null) {
			DcemModule dcemModule = dcemApplication.getModule(subject.getModuleId());
			ResourceBundle resourceBundle = JsfUtils.getBundle(dcemModule.getResourceName());
			try {
				updateViewVariableList(resourceBundle);
			} catch (DcemException e) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, e.getErrorCode().name());
			}
		}
		return viewVariables;
	}

	public Object getActionObject() {
		if (selection == null) {
			selection = autoViewBean.getSelectedItems();
		}
		if ((actionObject == null) && (selection != null) && (selection.isEmpty() == false)) {
			return selection.get(0);
		}
		return actionObject;
	}

	public void setActionObject(Object actionObject) {
		this.actionObject = actionObject;
	}

	public void addAutoViewAction(String actionName, ResourceBundle resourceBundle, DcemDialog dcemDialog, String xhtmlPage) {
		AutoViewAction autoViewAction = createAutoViewAction(actionName, resourceBundle, dcemDialog, xhtmlPage, null);
		if (autoViewAction != null) {
			autoViewActions.add(autoViewAction);
		}
	}

	/**
	 * @param actionName
	 * @param resourceBundle
	 * @param dcemDialog
	 * @param xhtmlPage
	 * @param viewLink
	 */
	public void addAutoViewAction(String actionName, ResourceBundle resourceBundle, DcemDialog dcemDialog, String xhtmlPage, ViewLink viewLink) {

		AutoViewAction autoViewAction = createAutoViewAction(actionName, resourceBundle, dcemDialog, xhtmlPage, viewLink);
		if (autoViewAction != null) {
			autoViewActions.add(autoViewAction);
		}
	}

	protected AutoViewAction createAutoViewAction(String actionName, ResourceBundle resourceBundle, DcemDialog dcemDialog, String xhtmlPage,
			ViewLink viewLink) {
		if (dcemDialog != null) {
			dcemDialog.setParentView(this);
		}
		// DcemAction dcemActionManage = subject.getDcemActionByName(DcemConstants.ACTION_MANAGE);
		DcemAction dcemActionManage = new DcemAction(subject.getModuleId(), subject.getName(), DcemConstants.ACTION_MANAGE);

		dcemActionManage = operatorSessionBean.getPermission(dcemActionManage);
		if (dcemActionManage == null) {
			dcemActionManage = new DcemAction(subject.getModuleId(), DcemConstants.EMPTY_SUBJECT_NAME, DcemConstants.ACTION_MANAGE);
			dcemActionManage = operatorSessionBean.getPermission(dcemActionManage);
		}
		// DcemAction dcemAction = subject.getDcemActionByName(actionName);

		DcemAction dcemActionPre = new DcemAction(subject.getModuleId(), subject.getName(), actionName);
		DcemAction dcemAction = operatorSessionBean.getPermission(dcemActionPre);
		if (dcemAction == null) {
			logger.info("No Action Found! " + dcemActionPre.toString());
			return null;
		}
		if (dcemActionManage != null || dcemAction != null) {
			if (autoViewActions == null) {
				autoViewActions = new LinkedList<AutoViewAction>();
			}
			return new AutoViewAction(dcemAction, dcemDialog, resourceBundle, subject.getRawAction(actionName), xhtmlPage, viewLink);
		}
		return null;
	}

	public void addPredefinedFilter(PredefinedFilter predefinedFilter) {
		if (predefinedFilters == null) {
			predefinedFilters = new ArrayList<PredefinedFilter>();
		}
		predefinedFilters.add(predefinedFilter);
	}

	public Object createActionObject() {
		if (subject.getKlass() == null) {
			return null;
		}

		try {
			actionObject = subject.getKlass().newInstance();
		} catch (InstantiationException | IllegalAccessException exp) {
			logger.warn("Couldn't create Subject Instance Class: " + subject.getKlass().getName(), exp);
		}
		return actionObject;

	}

	public LazyDataModel<?> getLazyModel() {
		if (lazyModel == null) {
			lazyModel = new JpaLazyModel<>(em, this);
		}
		return lazyModel;
	}

	public Object getActionSubObject() {
		return actionSubObject;
	}

	public void setActionSubObject(Object actionSubObject) {
		this.actionSubObject = actionSubObject;
	}

	public boolean isPredefinedFilters() {
		return predefinedFilters != null;
	}

	public int getPredefinedFilterId() {
		return predefinedFilterId;
	}

	public void setPredefinedFilterId(int predefinedFilterId) {
		this.predefinedFilterId = predefinedFilterId;
	}

	public List<SelectItem> getPredefinedFilterItems() {
		List<SelectItem> list = new ArrayList<SelectItem>(predefinedFilters.size() + 1);
		int ind = 0;
		list.add(new SelectItem(ind++, ""));
		for (PredefinedFilter predefinedFilter : predefinedFilters) {
			list.add(new SelectItem(ind++, predefinedFilter.getName()));
		}
		return list;
	}

	public void editPredefinedFilter() {
		PredefinedFilter predefinedFilter = predefinedFilters.get(predefinedFilterId - 1);
		setActionObject(predefinedFilter);
		activeDialog = predefinedFilterDialog;
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("modal", true);
		activeDialog.setAutoViewAction(predefinedFilterAction);
		autoDialogBean.populatePanelGrid();
		options.put("width", "950");
		// options.put("height", 540);
		options.put("contentWidth", "950");
		// options.put("contentHeight", "400");
		options.put("headerElement", "customheader");
		PrimeFaces.current().dialog().openDynamic(predefinedFilterAction.getXhtmlPage(), options, null);
		// RequestContext.getCurrentInstance().openDialog(predefinedFilterAction.getXhtmlPage(), options, null);
	}

	public String getPredefinedFilterTitle() {
		if (predefinedFilterId == 0) {
			return null;
		}
		return predefinedFilters.get(predefinedFilterId - 1).toString();
	}

	public PredefinedFilter getPredefinedFilter() {
		if (predefinedFilterId == 0) {
			return null;
		}
		return predefinedFilters.get(predefinedFilterId - 1);
	}

	public List<Object> getSelection() {
		return selection;
	}

	public void setSelection(List<Object> selection) {
		this.selection = selection;
	}

	void updateViewVariableList(ResourceBundle resourceBundle) throws DcemException {
		// This is currently not being used
		// List<RoleRestriction> restrictions = roleRestrictionLogic.getRestrictionsForView(operatorSessionBean.getDcemUser().getDcemRole(), subject);
		List<RoleRestriction> restrictions = null;

		viewVariables = new LinkedList<ViewVariable>();
		displayViewVariables = new LinkedList<ViewVariable>();
//		DcemAction dcemAction = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
//		boolean managed = operatorSessionBean.isPermission(dcemAction);
		if (subject != null && subject.getKlass() != null) {
			viewVariables = DcemUtils.getViewVariables(subject.getKlass(), resourceBundle, subject.getName(), restrictions);
			for (ViewVariable viewVariable : viewVariables) {
				if (viewVariable.isVisible()) {
					displayViewVariables.add(viewVariable);
					viewVariable.setVisible(viewVariable.getDcemGui().visible());
				}
			}
		}
		return;
	}

	public List<ViewVariable> getDisplayViewVariables() {
		if (displayViewVariables == null) {
			DcemModule dcemModule = dcemApplication.getModule(subject.getModuleId());
			ResourceBundle resourceBundle = JsfUtils.getBundle(dcemModule.getResourceName());
			try {
				updateViewVariableList(resourceBundle);
			} catch (DcemException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return displayViewVariables;
	}

	public ViewVariable getDisplayViewVariable(String id) {
		List<ViewVariable> list = getDisplayViewVariables();
		for (ViewVariable variable : list) {
			if (variable.getId().equals(id)) {
				return variable;
			}
		}
		return null;
	}

	public void setDisplayViewVariables(List<ViewVariable> displayViewVariables) {
		this.displayViewVariables = displayViewVariables;
	}

	public List<ViewVariable> getVisibleVariables() {
		return displayViewVariables;
	}

	public void leavingView() {

	}

	private void excelExport(LazyDataModel<?> lazyDataModel, List<ViewVariable> variables, ActionType actionType) {

		List<?> data = ((JpaLazyModel)lazyDataModel).load(0, maxExport);
		Workbook workbook = new XSSFWorkbook();
		CreationHelper createHelper = workbook.getCreationHelper();

		Sheet sheet = workbook.createSheet("Employee");
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.BLUE.getIndex());

		Font errorFont = workbook.createFont();
		errorFont.setColor(IndexedColors.RED.getIndex());

		// Create a CellStyle with the font
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		// Create a CellStyle with the font for ERROR
		CellStyle errorCellStyle = workbook.createCellStyle();
		errorCellStyle.setFont(errorFont);

		// Create a Row
		Row headerRow = sheet.createRow(0);
		int ind = 0;
		for (ind = 0; ind < variables.size(); ind++) {
			Cell cell = headerRow.createCell(ind);
			cell.setCellValue(variables.get(ind).displayName);
			cell.setCellStyle(headerCellStyle);
		}
		// Create Cell Style for formatting Date
		CellStyle dateCellStyle = workbook.createCellStyle();
		dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd-MM-yyyy"));

		int rowNum = 1;
		for (Object klassObject : data) {
			Row row = sheet.createRow(rowNum++);
			String value;
			int colIndex = 0;
			for (ViewVariable rawActions : variables) {
				value = rawActions.getRecordData(klassObject);
				Cell cell = row.createCell(colIndex++);
				if (value == "ERROR") {
					cell.setCellStyle(errorCellStyle);
				}
				cell.setCellValue(value);
			}
		}

		// Resize all columns to fit the content size
		for (ind = 0; ind < variables.size(); ind++) {
			sheet.autoSizeColumn(ind);
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
		response.setHeader("Content-Disposition", "attachment;filename=" + this.getDisplayName() + ".xlsx");
		// response.setContentLength(workbook.get);
		response.setContentType("application/ms-excel");
		try {
			workbook.write(response.getOutputStream());
			response.getOutputStream().flush();
			response.getOutputStream().close();
			facesContext.responseComplete();
		} catch (IOException ex) {
			logger.warn("Couldn't export file", ex);
		} finally {
			// Closing the workbook
			try {
				workbook.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public List<SortMeta> getSortedBy() {
		List<SortMeta> list = new LinkedList<SortMeta>();
		SortMeta sortMeta;
		for (ViewVariable viewVariable : viewVariables) {
			switch (viewVariable.getFilterSortOrder()) {
			case ASCENDING:
				sortMeta = SortMeta.builder().field(viewVariable.id).order(SortOrder.ASCENDING).build();
				list.add(sortMeta);
			case DESCENDING:
				sortMeta = SortMeta.builder().field(viewVariable.id).order(SortOrder.DESCENDING).build();
				list.add(sortMeta);
			default:
			}
		}
		return list;
	}

	public List<FilterMeta> getFilterBy() {
		List<FilterMeta> list = new LinkedList<FilterMeta>();
		FilterMeta filterMeta = null;
		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.getFilterItem().getFilterOperator() != FilterOperator.NONE &&  viewVariable.getFilterValue() != null) {
				if (viewVariable.getFilterToValue() != null) {
					List<Object> listValues = new ArrayList<>();
					listValues.add(viewVariable.getFilterValue());
					listValues.add(viewVariable.getFilterToValue());
					filterMeta = FilterMeta.builder().field(viewVariable.getId()).filterValue(listValues).build();
				} else {
					filterMeta = FilterMeta.builder().field(viewVariable.getId()).filterValue(viewVariable.getFilterValue()).build();
				}
				list.add(filterMeta);
			}
		}
		return list;
	}



	public DcemAction getRevealAction() {
		if (revealAction == null) {
			revealAction = new DcemAction(subject, DcemConstants.ACTION_REVEAL);
		}
		return revealAction;
	}



	public DcemAction getManageAction() {
		if (manageAction == null) {
			manageAction = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
		}
		return manageAction;
	}

}
