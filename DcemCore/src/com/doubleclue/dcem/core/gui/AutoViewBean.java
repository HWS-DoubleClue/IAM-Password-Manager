package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.lang.reflect.Method;
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
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
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

		ModulePreferences preferences = viewNavigator.getActiveModule().getModulePreferences();
		try {
			Method method = preferences.getClass().getMethod("getManualsLink", (Class[]) null);
			String methodLink = (String) method.invoke(preferences, (Object[]) null);
			if (methodLink != null && methodLink.startsWith("http")) {
				return methodLink;
			}
		} catch (Exception e) {
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
	
	public List<SortMeta> getSortBy() {
		return viewNavigator.getActiveView().getSortedBy();
	}
	
	public List<FilterMeta> getFilterBy() {
		return viewNavigator.getActiveView().getFilterBy();
	}

	public List<ViewVariable> getVisibleVariables() {
		return viewNavigator.getActiveView().getDisplayViewVariables();
	}

	public String getActionText(AutoViewAction autoViewAction) {
		return autoViewAction.getActionText();
	}

	public List<Object> getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(List<Object> selectedItems) {
		this.selectedItems = selectedItems;
	}

	public String preSelectionDialog() {
		// TODO
		return null;
	}

	
}
