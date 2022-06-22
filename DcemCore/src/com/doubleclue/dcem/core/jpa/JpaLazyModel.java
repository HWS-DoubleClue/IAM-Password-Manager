/*
*
*
*
 */
package com.doubleclue.dcem.core.jpa;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
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
import org.primefaces.model.SortOrder;

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

	DcemView dcemView;

	int previousOffset;
	int previousPageSize;

	public JpaLazyModel(EntityManager entityManager, DcemView dcemView) {
		this.entityManager = entityManager;
		this.dcemView = dcemView;
		jpaSelectProducer = new JpaSelectProducer(entityManager, dcemView.getSubject().getKlass());

	}

	// protected Class<T> getEntityClass() {
	// return entityClass;
	// }

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
		return getId(t).toString();
	}

	// // TODO - implement using metadata
	protected Object getId(T t) {
		try {
			Method method = t.getClass().getMethod("getId");
			try {
				return method.invoke(t);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
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

	// public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, FilterMeta> filterBy) {
	// public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {

	@Override
	public List<T> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
		// long start = System.currentTimeMillis();
		if (dcemView.isDirty() || (first != previousOffset) || (pageSize != previousPageSize) || data == null) {
			previousOffset = first;
			previousPageSize = pageSize;
			dcemView.setDirty(false);
			// System.out.println("JpaLazyModel.load() Execute " + offset + ", "
			// + pageSize);
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
				List<FilterProperty> filterProperties;

				try {
					filterProperties = getFilterProperties(dcemView.getViewVariables());
					if (preFilterProperties != null) {
						filterProperties.addAll(preFilterProperties);
					}

				} catch (Exception exp) {
					logger.warn("jpaSelectProducer.createCountCriteriaQuery", exp);
					JsfUtils.addErrorMessage(exp.toString());
					data = null;
					return null;
				}
				if (filterProperties == null) {
					return data;
				}

				try {
					filterOrders = new ArrayList<FilterOrder>();
					for (ViewVariable viewVariable : dcemView.getViewVariables()) {
						if (viewVariable.getFilterItem().sortOrder != SortOrder.UNSORTED) {
							boolean descending = false;
							if (viewVariable.getFilterItem().sortOrder == SortOrder.DESCENDING) {
								descending = true;
							}
							filterOrders.add(new FilterOrder(viewVariable.getAttributes(), viewVariable.getId(), descending,
									viewVariable.getFilterItem().getSortRank()));
							// break;
						}
						if (viewVariable.getDcemGui().sortRank() > 0) {
							filterOrders
									.add(new FilterOrder(viewVariable.getAttributes(), viewVariable.getId(), true, viewVariable.getFilterItem().getSortRank()));
						}
					}
					Collections.sort(filterOrders, new FilterOrderCompare());
					data = jpaSelectProducer.selectCriteriaQuery(filterOrders, filterProperties, first, pageSize);
				} catch (DcemException exp) {
					logger.warn("jpaSelectProducer.selectCriteriaQuery", exp);
					JsfUtils.addErrorMessage(exp.toString());
					data = null;
				} catch (Exception e) {
					logger.warn("jpaSelectProducer.selectCriteriaQuery", e);
					JsfUtils.addErrorMessage("Opps. Error did occur. Please have a look in the log file. " + e.toString());
				}
				// System.out.println("JpaLazyModel.load() RETURN Time: " +
				// (System.currentTimeMillis() - start));
				return data;
			}
		} else {
			return data;
		}
	}

	public int getPredefinedFilterCount() throws Exception {
		PredefinedFilter predefinedFilter = dcemView.getPredefinedFilter();
		return predefinedFilter.executeCount(entityManager).intValue();
	}

	public List<T> getPredefinedFilterData(int offset, int pageSize) throws Exception {
		PredefinedFilter predefinedFilter = dcemView.getPredefinedFilter();
		return (List<T>) predefinedFilter.execute(entityManager, offset, pageSize);
	}

	/**
	 * @return
	 * @throws DcemException
	 * @throws Exception
	 */
	public static List<FilterProperty> getFilterProperties(List<ViewVariable> variables) throws DcemException {

		List<FilterProperty> filterProperties = new LinkedList<FilterProperty>();
		Object filterValue = null;
		for (ViewVariable viewVariable : variables) {
			if (viewVariable.getFilterItem() == null) {
				continue;
			}
			FilterOperator filterOperator = FilterOperator.NONE;
			// if (filterOperator == null || filterOperator ==
			// FilterOperator.NONE) {
			// continue;
			// }
			VariableType variableType = viewVariable.getVariableType();
			switch (variableType) {
			case STRING:
				if ((viewVariable.getFilterValue() == null) || ((String) viewVariable.getFilterValue()).isEmpty()) {
					continue;
				}
				if (viewVariable.getFilterOperator() == FilterOperator.NONE) {
					filterOperator = FilterOperator.LIKE;
				} else {
					filterOperator = viewVariable.getFilterOperator();
				}
				filterValue = viewVariable.getFilterValue();
				break;
			case ENUM:
				Class<Enum<?>> klass = viewVariable.getKlass();
				filterOperator = viewVariable.getFilterItem().getFilterOperator();
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
				break;
			case BOOLEAN:
				Object object = viewVariable.getFilterValue();
				if (object != null) {
					if (object instanceof String) {
						Boolean value = new Boolean((String) object);
						viewVariable.setFilterValue(value);
					}
					Boolean booleanValue = (Boolean) viewVariable.getFilterValue();
					if (booleanValue != null) {
						if (booleanValue == true) {
							filterOperator = FilterOperator.IS_TRUE;
							filterValue = true;
						} else {
							filterOperator = FilterOperator.IS_FALSE;
							filterValue = false;
						}
					}
				}
				break;
			case NUMBER:
				if (viewVariable.getFilterValue() != null) {
					try {
						Integer value = Integer.parseInt((String) viewVariable.getFilterValue());
						filterOperator = FilterOperator.EQUALS;
						filterValue = value;
					} catch (Exception e) {
						JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "autoview.invalidNumberFormat", viewVariable.getDisplayName());
					}
				}
				// if (viewVariable.getFilterOperator() != FilterOperator.NONE) {
				// Integer value;
				// try {
				// if (viewVariable.getFilterValue() == null) {
				// filterOperator = FilterOperator.NONE;
				// } else {
				// String strValue = ((String) viewVariable.getFilterValue());
				// value = Integer.parseInt(strValue);
				// filterValue = value;
				// filterOperator = viewVariable.getFilterOperator();
				// }
				// } catch (Exception e) {
				// JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "autoview.invalidNumberFormat", viewVariable.getDisplayName());
				// }
				// } else {
				// filterOperator = viewVariable.getFilterOperator();
				// }
				break;
			case DATE:
				if (viewVariable.getFilterValue() != null) {
					filterValue = viewVariable.getFilterValue();
					filterOperator = FilterOperator.EQUALS;
				}
				break;
			default:
				if (viewVariable.getFilterValue() == null) {
					continue;
				}
				filterOperator = viewVariable.getFilterOperator();
				filterValue = viewVariable.getFilterValue();

			}

			// if (viewVariable.getVariableType() == VariableType.STRING && ())
			// {
			// JsfUtils.addWarningMessage(Constants.CORE_RESOURCE,
			// "autoview.missingvalue",
			// viewVariable.getDisplayName());
			// return null;
			// }

			filterProperties.add(new FilterProperty(viewVariable.getAttributes(), filterValue, viewVariable.getFilterToValue(), variableType, filterOperator));
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
			List<FilterProperty> filterProperties;
			filterProperties = getFilterProperties(dcemView.getViewVariables());
			if (preFilterProperties != null) {
				filterProperties.addAll(preFilterProperties);
			}
			int dataSize = (int) jpaSelectProducer.createCountCriteriaQuery(dcemView.getSubject().getKlass(), filterProperties);
			setRowCount(dataSize);
			return dataSize;
		} catch (Exception exp) {
			logger.warn("jpaSelectProducer.createCountCriteriaQuery", exp);
			JsfUtils.addErrorMessage(exp.toString());
			return 0;
		}
	}

}