package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;

@Named("autoView")
@SessionScoped
public class AutoViewBean implements Serializable {

	// private static final Logger logger = LogManager.getLogger(AutoViewBean.class);

	@Inject
	ViewNavigator viewNavigator;
	
	@Inject
	OperatorSessionBean sessionBean;

	FacesContext context;

	private static final long serialVersionUID = 3733919546663290317L;

	private static Logger logger = LogManager.getLogger(AutoViewBean.class);

	transient ResourceBundle resourceBundle;
	private Map<String, String> filterValues = new HashMap<String, String>();
	private List<Object> selectedItems;
	private String sortProperty;

	public AutoViewBean() {
	}

	public String getId() {
		return viewNavigator.getActiveView().getSubject().getName();
	}

	@PostConstruct
	public void init() {
		String resourceName = viewNavigator.getActiveModule().getResourceName();
		resourceBundle = JsfUtils.getBundle(resourceName);
	}

	public void switchView() {
		String resourceName = viewNavigator.getActiveModule().getResourceName();
		resourceBundle = JsfUtils.getBundle(resourceName);
		selectedItems = null;
		filterValues.clear();
		sortProperty = null;
	}

	public void reload() {
		// loadVariables();
	}

	public String getTitle() {
		if (viewNavigator.getActiveView() == null) {
			return null;
		}
		return viewNavigator.getActiveView().getDisplayName();
	}

	public String getIcon() {
		if (viewNavigator.getActiveView() == null) {
			return null;
		}
		String icon = viewNavigator.getActiveView().getSubject().getIconName();
		if (icon.startsWith("fa")) {
			return icon;
		}
		return "/pictures/icons/24x24/" + viewNavigator.getActiveView().getSubject().getIconName();
	}

	public String getHelpResource() {
		if (viewNavigator.getActiveView() == null) {
			return null;
		}
		String manualsPath;
		String page;
		
		switch (sessionBean.getDcemUser().getLanguage()) {
		case German:
			manualsPath = DcemConstants.MANUALS_URL_LOCATION + Locale.GERMAN.toString().toUpperCase() + DcemConstants.PDF_EXT;
			break;
		default:
			manualsPath = DcemConstants.MANUALS_URL_LOCATION + Locale.ENGLISH.toString().toUpperCase() + DcemConstants.PDF_EXT;
			break;
		}
		page = JsfUtils.getStringSafely(resourceBundle,
				viewNavigator.getActiveView().getSubject().getModuleId() + "." + viewNavigator.getActiveView().getSubject().getName() + ".DOCU_PAGE");

		if (page.startsWith(JsfUtils.NO_RESOURCE_FOUND)) {
			return manualsPath;
		}
		return manualsPath + "#page=" + page;
	}

	public SubjectAbs getSubject() {
		if (viewNavigator.getActiveView() == null) {
			return null;
		}
		return viewNavigator.getActiveView().getSubject();
	}

	// public String getIcon24() {
	// if (viewNavigator.getActiveView() == null) {
	// return null;
	// }
	// return "/pictures/icons/24x24/" + viewNavigator.getActiveView().getSubject().getIconName();
	// }

	public class FieldType {
		String subClass;
		Class<?> clsType;
	}

	public Map<String, String> getFilterValues() {
		return filterValues;
	}

	public void setFilterValues(Map<String, String> filterValues) {
		this.filterValues = filterValues;
	}

	public String getSortProperty() {
		return sortProperty;
	}

	public void setSortProperty(String sortProperty) {
		this.sortProperty = sortProperty;
	}

	public String getTableStyle() {
		return viewNavigator.getActiveView().getSubject().getTableStyle();
	}

	public LazyDataModel<?> getLazyModel() {
		return viewNavigator.getActiveView().getLazyModel();
	}

	public void filterChange(AjaxBehaviorEvent vce) {
		System.out.println("AutoViewBean.filtetChange()");

	}

	public int getCount() {
		return viewNavigator.getActiveView().getLazyModel().getRowCount();
	}

	public List<ViewVariable> getVisibleVariables() {
		return viewNavigator.getActiveView().getDisplayViewVariables();
	}

	public String getActionText(AutoViewAction autoViewAction) {
		return autoViewAction.getActionText();
	}

	// public Collection<Object> getSelection() {
	// return selection;
	// }

	public List<Object> getSelectedItems() {
		// System.out.println("AutoViewBean.getSelectedItems() " + selectedItems);
		return selectedItems;
	}

	public void setSelectedItems(List<Object> selectedItems) {
		// System.out.println("AutoViewBean.setSelectedItems() " + selectedItems);
		this.selectedItems = selectedItems;
	}

	public String getSortIcon(String item) {
		String icon = null;
		ViewVariable viewVariable = DcemUtils.getViewVariableFromId(getVisibleVariables(), item);
		switch (viewVariable.getFilterItem().getSortOrder()) {
		case ASCENDING:
			icon = "/pictures/icons/16x16/sort_down.png";
			break;
		case DESCENDING:
			icon = "/pictures/icons/16x16/sort_up.png";
			break;
		case UNSORTED:
			icon = "/pictures/icons/16x16/sort_up_down2.png";
			break;
		}
		return icon;
	}

	public void sortVariable(String item) {
		List<ViewVariable> viewVariables = getVisibleVariables();
		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.id.equals(item)) {
				viewNavigator.getActiveView().setDirty(true);
				switch (viewVariable.getFilterItem().getSortOrder()) {
				case ASCENDING:
					viewVariable.getFilterItem().setSortOrder(SortOrder.DESCENDING);
					break;
				case DESCENDING:
					viewVariable.getFilterItem().setSortOrder(SortOrder.UNSORTED);
					break;
				case UNSORTED:
					viewVariable.getFilterItem().setSortOrder(SortOrder.ASCENDING);
					break;
				}
			} else {
				viewVariable.getFilterItem().setSortOrder(SortOrder.UNSORTED);
			}
		}
		return;
	}

	public String preSelectionDialog() {
		// TODO
		return null;
	}
}
