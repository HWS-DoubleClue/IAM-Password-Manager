/*
*
*
*
 */
package com.doubleclue.dcem.core.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.EnumStringAnnotation;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.logic.PredefinedFilter;

@SuppressWarnings("serial")
public class JpaLazyModel<T> extends LazyDataModel<T> {

	private static final Logger logger = LogManager.getLogger(JpaLazyModel.class);

	protected EntityManager entityManager;

	protected List<T> data;
	JpaSelectProducer<T> jpaSelectProducer;
	List<FilterProperty> preFilterProperties;
	List<FilterProperty> filterProperties;

	DcemView dcemView;
	Map<String, SortMeta> sortBy;
	Map<String, FilterMeta> filterBy;
	
	

	public JpaLazyModel(EntityManager entityManager, DcemView dcemView) {
		this.entityManager = entityManager;
		this.dcemView = dcemView;
		jpaSelectProducer = new JpaSelectProducer(entityManager, dcemView.getSubject().getKlass());
	}

	@Override
	public T getRowData(String rowKey) {
		if (data != null && rowKey != null && !rowKey.isEmpty()) {
			for (T t : data) {
				Object id = getId(t);
				if (id != null) {
					if (id.toString().equals(rowKey)) {
						return t;
					}
				} else {
					logger.info("JpaLazyModel.getRowData() No Object found for key: " + rowKey);
				}
			}
		}
		return null;
	}

	@Override
	public String getRowKey(T t) {
		if (getId(t) == null) {
			return "0";
		}
		return getId(t).toString();
	}

	// // TODO - implement using metadata
	protected Object getId(T t) {
		try {
			Method method = t.getClass().getMethod("getId");
			try {
				return method.invoke(t);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data2) {
		this.data = (List<T>) data2;
	}

	public List<T> load(int first, int pageSize) {
		return load (first, pageSize, sortBy, filterBy);  // getlastest sortby
	}
	
	@Override
	public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		// long start = System.currentTimeMillis();
		// System.out.println("JpaLazyModel.load() filterBy " + filterBy);
		this.sortBy = sortBy;
		this.filterBy = filterBy;
		if (dcemView.getPredefinedFilterId() > 0) {
			try {
				PredefinedFilter predefinedFilter = dcemView.getPredefinedFilter();
				int dataSize = predefinedFilter.executeCount(entityManager).intValue();
				setRowCount(dataSize);
				data = (List<T>) predefinedFilter.execute(entityManager, first, pageSize);
			} catch (Exception exp) {
				JsfUtils.addErrorMessage("Couldn't execute Predefined-Filter. Cause: " + exp.getMessage());
				data = null;
			}
			return data;
		} else {
			List<FilterOrder> filterOrders = null;
			if (filterProperties == null) {
				filterProperties = getMetaFilterProperties(filterBy, dcemView.getViewVariables());
				try {
					if (preFilterProperties != null) {
						filterProperties.addAll(preFilterProperties);
					}
				} catch (Exception exp) {
					logger.warn("jpaSelectProducer.createCountCriteriaQuery", exp);
					JsfUtils.addErrorMessage(exp.toString());
					data = null;
					return null;
				}
			}
			try {
				filterOrders = new ArrayList<FilterOrder>();
				if (sortBy != null) {
					for (SortMeta sortMeta : sortBy.values()) {
						ViewVariable viewVariable = getViewVariable(sortMeta.getField());
						if (viewVariable.getDcemGui() != null && VariableType.LIST.equals(viewVariable.getDcemGui().variableType())) {
							continue;
						}
						boolean descending;
						switch (sortMeta.getOrder()) {
						case ASCENDING:
							descending = false;
							break;
						case DESCENDING:
							descending = true;
							break;
						default:
							continue;
						}
						filterOrders.add(
								new FilterOrder(viewVariable.getAttributes(), viewVariable.getId(), descending, viewVariable.getFilterItem().getSortRank()));
					}
				}
				Collections.sort(filterOrders, new FilterOrderCompare());
				data = jpaSelectProducer.selectCriteriaQuery(filterOrders, filterProperties, first, pageSize, dcemView);
			} catch (DcemException exp) {
				logger.warn("jpaSelectProducer.selectCriteriaQuery", exp);
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
				data = null;
			} catch (Exception e) {
				logger.warn("jpaSelectProducer.selectCriteriaQuery", e);
				JsfUtils.addErrorMessage("Opps. Error did occur. Please have a look in the log file. " + e.toString());
			}
			filterProperties = null;
			return data;
		}
	}

	public int getPredefinedFilterCount() throws Exception {
		PredefinedFilter predefinedFilter = dcemView.getPredefinedFilter();
		return predefinedFilter.executeCount(entityManager).intValue();
	}

	private ViewVariable getViewVariable(String id) {
		for (ViewVariable viewVariable : dcemView.getViewVariables()) {
			if (viewVariable.getId().equals(id)) {
				return viewVariable;
			}
		}
		return null;
	}

	private List<FilterProperty> getMetaFilterProperties(Map<String, FilterMeta> filterBy, List<ViewVariable> variables) {
		List<FilterProperty> properties = new ArrayList<>();
		ViewVariable viewVariable;
		if (filterBy != null) {
			for (FilterMeta filterMeta : filterBy.values()) {
				viewVariable = null;
				for (ViewVariable viewVariable2 : variables) {
					if (viewVariable2.getId().equals(filterMeta.getField())) {
						viewVariable = viewVariable2;
						break;
					}
				}
				if (viewVariable == null) {
					continue;
				}
				FilterProperty filterProperty = getFilterProperty(viewVariable, filterMeta.getFilterValue());
				if (filterProperty != null) {
					properties.add(filterProperty);
				}
			}
		}
		return properties;
	}

	public List<T> getPredefinedFilterData(int offset, int pageSize) throws Exception {
		PredefinedFilter predefinedFilter = dcemView.getPredefinedFilter();
		return (List<T>) predefinedFilter.execute(entityManager, offset, pageSize);
	}

	private static FilterProperty getFilterProperty(ViewVariable viewVariable, Object filterValue) {
		FilterOperator filterOperator = FilterOperator.NONE;
		VariableType variableType = viewVariable.getVariableType();
		Object filterValueTo = viewVariable.getFilterToValue();
		switch (variableType) {
		case LIST:
		case STRING:
			if (filterValue == null) {
				if ((viewVariable.getFilterValue() == null) || ((String) viewVariable.getFilterValue()).isEmpty()) {
					return null;
				}
				if (viewVariable.getFilterOperator() == FilterOperator.NONE) {
					filterOperator = FilterOperator.LIKE;
				} else {
					filterOperator = viewVariable.getFilterOperator();
				}
				filterValue = viewVariable.getFilterValue();
			} else {
				if (((String) filterValue).isEmpty() == false) {
					filterOperator = FilterOperator.LIKE;
				}
			}
			break;
		case ENUM:
			filterOperator = FilterOperator.EQUALS;
			if (filterValue == null) {
				Class<Enum<?>> klass = viewVariable.getKlass();
				if (klass.getAnnotation(EnumStringAnnotation.class) != null) {
					variableType = VariableType.ENUM_STRING;
					Enum[] enums = klass.getEnumConstants();
					if (viewVariable.getFilterValue() != null) {
						int ind = Integer.parseInt((String) viewVariable.getFilterValue());
						filterValue = enums[ind];
					}
				} else {
					filterValue = viewVariable.getFilterValue();
				}
			}
			break;
		case BOOLEAN:
			if (filterValue == null) {
				filterValue = viewVariable.getFilterValue();
			}
			if (filterValue != null) {
				if (filterValue instanceof String) {
					filterValue = new Boolean((String) filterValue);
				}
				if (((Boolean) filterValue) == true) {
					filterOperator = FilterOperator.IS_TRUE;
				} else {
					filterOperator = FilterOperator.IS_FALSE;
				}
			}
			break;
		case NUMBER:
			if (filterValue == null) {
				filterValue = viewVariable.getFilterValue();
			}
			if (filterValue != null) {
				try {
					filterValue = Integer.parseInt((String) filterValue);
					filterOperator = FilterOperator.EQUALS;
				} catch (Exception e) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "autoview.invalidNumberFormat", viewVariable.getDisplayName());
				}
			}
			break;
		case DATE_TIME:
			if (filterValue != null) {
				List<?> list = (List<?>) filterValue;
				filterValue = list.get(0);
				filterValueTo = (list.get(1));
			} else {
				filterValue = viewVariable.getFilterValue();
			}
			filterOperator = FilterOperator.BETWEEN;
			break;
		case DATE:
			if (filterValue != null) {
				List<?> list = (List<?>) filterValue;
				if (list.get(0) instanceof LocalDate) {
					filterValue = ((LocalDate) list.get(0)).atStartOfDay();
					filterValueTo = (((LocalDate) list.get(1)).atTime(LocalTime.MAX));
				} else {
					filterValue = list.get(0);
					filterValueTo = (list.get(1));
				}
			} else {
				filterValue = viewVariable.getFilterValue();
			}
			filterOperator = FilterOperator.BETWEEN;
			break;
		default:
			if (viewVariable.getFilterValue() == null) {
				return null;
			}
			filterOperator = viewVariable.getFilterOperator();
			filterValue = viewVariable.getFilterValue();

		}
		return new FilterProperty(viewVariable.getAttributes(), filterValue, filterValueTo, variableType, filterOperator);

	}

	/**
	 * @return
	 * @throws DcemException
	 * @throws Exception
	 */
	public static List<FilterProperty> getFilterProperties(List<ViewVariable> variables) throws DcemException {

		List<FilterProperty> filterProperties = new LinkedList<FilterProperty>();
		for (ViewVariable viewVariable : variables) {
			if (viewVariable.getFilterItem() == null) {
				continue;
			}
			FilterProperty filterProperty = getFilterProperty(viewVariable, null);
			if (filterProperty != null) {
				filterProperties.add(filterProperty);
			}
		}
		return filterProperties;
	}

	public void addPreFilterProperties(FilterProperty filterProperty) {
		if (preFilterProperties == null) {
			preFilterProperties = new ArrayList<>(1);
			preFilterProperties.add(filterProperty);
		}
	}

	@Override
	public int count(Map<String, FilterMeta> filterBy) {
		try {
			filterProperties = getMetaFilterProperties(filterBy, dcemView.getViewVariables());
			if (preFilterProperties != null) {
				filterProperties.addAll(preFilterProperties);
			}
			int dataSize = (int) jpaSelectProducer.createCountCriteriaQuery(dcemView.getSubject().getKlass(), filterProperties, dcemView);
			setRowCount(dataSize);
			return dataSize;
		} catch (DcemException exp) {
			JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			return 0;
		} catch (Exception exp) {
			logger.warn("jpaSelectProducer.createCountCriteriaQuery", exp);
			JsfUtils.addErrorMessage(exp.toString() + "For " + dcemView.getSubject().getKlass());
			return 0;
		}
	}

}