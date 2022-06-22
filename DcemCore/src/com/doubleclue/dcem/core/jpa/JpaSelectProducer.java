/*
*
*
*
 */
package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.derby.tools.sysinfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.utils.DcemUtils;

import jersey.repackaged.com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class JpaSelectProducer<T> implements Serializable {

	private static final Logger logger = LogManager.getLogger(JpaSelectProducer.class);

	@Inject
	protected EntityManager entityManager;

	protected Long cachedRowCount;

	private Class<?> entityClass;

	DateFormat dateFomat = new SimpleDateFormat(DcemConstants.DAY_TIME_FORMAT);
	DateFormat dateDayFomat = new SimpleDateFormat(DcemConstants.DAY_FORMAT);

	public JpaSelectProducer(EntityManager entityManager, Class<?> entityClass) {
		super();
		this.entityManager = entityManager;
		this.entityClass = entityClass;
	}

	public long createCountCriteriaQuery(Class<?> entityClass, List<FilterProperty> filterProperties) throws DcemException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<?> root = criteriaQuery.from(entityClass);

		Predicate whereCond = getPredicates(criteriaBuilder, root, filterProperties);
		if (whereCond != null) {
			criteriaQuery.where(whereCond);
		}

		Expression<Long> count = criteriaBuilder.count(root);
		criteriaQuery.select(count);
		cachedRowCount = entityManager.createQuery(criteriaQuery).getSingleResult();
		return cachedRowCount;
	}

	/**
	 * @param entityClass
	 * @param methodProperties
	 * @param filterOrders
	 * @param firstResult
	 * @param maxResult
	 * @return
	 * @throws DcemException
	 */
	public List<T> selectCriteriaQuery(List<FilterOrder> filterOrders, List<FilterProperty> filterProperties, int firstResult, int maxResult)
			throws DcemException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<?> root = criteriaQuery.from(entityClass);

		if (filterOrders != null) {
			List<Order> orders = createOrders(criteriaBuilder, root, filterOrders);
			if (orders.isEmpty() == false) {
				criteriaQuery.orderBy(orders);
			}
		}
		if (filterProperties != null) {
			Predicate whereCond = getPredicates(criteriaBuilder, root, filterProperties);
			if (whereCond != null) {
				criteriaQuery.where(whereCond);
			}
		}
		@SuppressWarnings("unchecked")
		TypedQuery<T> query = (TypedQuery<T>) entityManager.createQuery(criteriaQuery);
		query.setFirstResult(firstResult);
		query.setMaxResults(maxResult);
		List<T> data = query.getResultList();
		return data;
	}

	/**
	 * @param criteriaBuilder
	 * @param root
	 * @param filterOrders
	 * @return
	 */
	protected List<Order> createOrders(CriteriaBuilder criteriaBuilder, Root<?> root, List<FilterOrder> filterOrders) {
		List<Order> orders = Lists.newArrayList();
		if (filterOrders != null && filterOrders.isEmpty() == false) {
			for (FilterOrder filterOrder : filterOrders) {

				int length = filterOrder.getAttributes().size();
				if (length == 0) {
					return orders;
				}
				SingularAttribute<?, ?> attribute = filterOrder.getAttributes().get(length - 1);

				From<?, ?> from = null;
				from = root;

				if (length > 1) {
					// search and create the Joins
					for (int i = 0; i < length - 1; i++) {
						SingularAttribute<?, ?> joinAttribute = filterOrder.getAttributes().get(i);
						from = getJoins(from.getJoins(), attribute.getDeclaringType().getJavaType(), joinAttribute);
						if (from == null) {
							from = root.join(joinAttribute.getName(), JoinType.LEFT);
						}
					}
				}
				Order jpaOrder;
				Expression<String> expression = from.<String> get(attribute.getName());

				if (filterOrder.isDesc()) {
					jpaOrder = criteriaBuilder.desc(expression);
				} else {
					jpaOrder = criteriaBuilder.asc(expression);
				}
				orders.add(jpaOrder);
			}
		}
		return orders;
	}

	protected Class<?> getEntityClass() {
		return entityClass;
	}

	/**
	 * @param root
	 * @param path
	 * @return
	 */
	Path<String> getJoins(Root<?> root, List<String> nameTree) {

		Join<Class<?>, Class<?>> join;
		Path<String> expression;
		if (nameTree.size() == 1) {
			expression = root.get(nameTree.get(0));
			return expression;
		}
		join = root.join(nameTree.get(0), JoinType.LEFT);
		int i = 1;
		for (i = 1; i < (nameTree.size() - 1); i++) {
			join = join.join(nameTree.get(i), JoinType.LEFT);
		}

		expression = join.get(nameTree.get(i));
		return expression;
	}

	/**
	 * find join identified by declaring type in a set of joins
	 * 
	 * @param rootJoins
	 * @param declaringType
	 *            the type to find in the joins
	 * @return
	 */
	private static From<?, ?> getJoins(Set<? extends Join<?, ?>> rootJoins, Class<?> declaringType, Attribute<?, ?> joinAttribute) {

		Set<? extends Join<?, ?>> tableJoins;
		Set<? extends Join<?, ?>> subJoins;

		Stack<Set<? extends Join<?, ?>>> stack = new Stack<Set<? extends Join<?, ?>>>();
		stack.push(rootJoins);
		while (!stack.isEmpty()) {
			tableJoins = stack.pop();
			for (Join<?, ?> join : tableJoins) {
				if (declaringType.isAssignableFrom(join.getJavaType())) {
					if (joinAttribute == null || join.getAttribute().equals(joinAttribute)) {
						return join;
					}
				}
				subJoins = join.getJoins();
				if (subJoins != null) {
					stack.push(subJoins);
				}
			}
		}
		return null;
	}

	/**
	 * @param cb
	 * @param root
	 * @param filterProperties
	 * @return
	 * @throws DcemException
	 */
	private Predicate getPredicates(CriteriaBuilder cb, Root<?> root, List<FilterProperty> filterProperties) throws DcemException {

		List<Predicate> predicates = new ArrayList<Predicate>();

		for (FilterProperty filterProperty : filterProperties) {
			if (filterProperty.filterOperator == null || filterProperty.filterOperator.equals(FilterOperator.NONE)) {
				continue;
			}
			if (filterProperty.filterOperator.valueRequired && ((filterProperty.getValue() == null))) {
				continue;
			}

			int length = filterProperty.getAttributes().size();
			SingularAttribute<?, ?> attribute = filterProperty.getAttributes().get(length - 1);

			From<?, ?> from = null;
			From<?, ?> preFrom = null;
			preFrom = root;

			if (length > 1) {
				// search and create the Joins
				for (int i = 0; i < length - 1; i++) {
					SingularAttribute<?, ?> joinAttribute = filterProperty.getAttributes().get(i);
					from = getJoins(preFrom.getJoins(), attribute.getDeclaringType().getJavaType(), joinAttribute);
					if (from == null) {
						preFrom = preFrom.join(joinAttribute.getName(), JoinType.LEFT);
					} else {
						preFrom = from;
					}
				}
			}

			switch (filterProperty.variableType) {

			case STRING: {
				if (filterProperty.getValue() == null) {
					continue;
				}
				Expression<String> expression = preFrom.<String> get(attribute.getName());
				switch (filterProperty.filterOperator) {
				default:
				case LIKE:
					predicates.add(cb.like(expression, "%" + (String) filterProperty.getValue() + "%", DcemConstants.JPA_ESCAPE_CHAR));
					break;
				case EQUALS:
					predicates.add(cb.like(expression, (String) filterProperty.getValue(), DcemConstants.JPA_ESCAPE_CHAR));
					break;
				case NOT_EQUALS:
					predicates.add(cb.notLike(expression, (String) filterProperty.getValue(), DcemConstants.JPA_ESCAPE_CHAR));
					break;

				}
			}
				break;

			case BOOLEAN:

				if (filterProperty.filterOperator.equals(FilterOperator.IS_TRUE)) {
					predicates.add(cb.equal(preFrom.<Boolean> get((SingularAttribute<Object, Boolean>) attribute), true));
				} else if (filterProperty.filterOperator.equals(FilterOperator.IS_FALSE)) {
					predicates.add(cb.equal(preFrom.<Boolean> get((SingularAttribute<Object, Boolean>) attribute), false));
				}
				break;

			case EPOCH_DATE: {
				// Expression<Long> expression = preFrom.<Long> get((SingularAttribute<Object, Long>) attribute);

				Expression<Integer> expression = preFrom.get(attribute.getName() + "Milli");
				if (filterProperty.getValue() == null) {
					continue;
				}
				Date date = DcemUtils.setDayBegin((Date) filterProperty.getValue());
				Number number = ((int) (date.getTime() / 1000));

				switch (filterProperty.getFilterOperator()) {
				case EQUALS:
					predicates.add(cb.equal(expression, number));
					break;
				case LESSER:
					predicates.add(cb.lt(expression, number));
					break;
				case GREATER:
					predicates.add(cb.gt(expression, number));
					break;
				case NOT_EQUALS:
					predicates.add(cb.notEqual(expression, number));
					break;
				case BETWEEN:
					if (filterProperty.getToValue() == null) {
						logger.warn("Filter-Operator is BETWEEN, but toValue is missing");
						break;
					}
					Date toDate = DcemUtils.setDayBegin((Date) filterProperty.getToValue());
					Number toNumber = ((int) (toDate.getTime() / 1000));
					predicates.add(cb.between(expression, (int) number, (int) toNumber));
					break;

				default:
					break;
				}

				break;
			}

			case ENUM: {
				Expression<Long> expression = preFrom.<Long> get((SingularAttribute<Object, Long>) attribute);
				String value = (String) filterProperty.getValue();

				if (value == null) {
					continue;
				}
				Number number = (Number) Integer.parseInt(value);
				predicates.add(cb.equal(expression, number));
				break;
			}

			case ENUM_STRING: {
				if (filterProperty.getValue() == null) {
					continue;
				}

				Expression<String> expression = preFrom.<String> get(attribute.getName());
				predicates.add(cb.equal(expression, filterProperty.getValue()));
				break;
			}

			case NUMBER: {
				Expression<Long> expression = preFrom.<Long> get((SingularAttribute<Object, Long>) attribute);
				if (filterProperty.getValue() == null) {
					continue;
				}
				Number number = (Number) filterProperty.getValue();

				switch (filterProperty.getFilterOperator()) {
				case EQUALS:
					predicates.add(cb.equal(expression, number));
					break;
				case LESSER:
					predicates.add(cb.lt(expression, number));
					break;
				case GREATER:
					predicates.add(cb.gt(expression, number));
					break;
				case NOT_EQUALS:
					predicates.add(cb.notEqual(expression, number));
					break;
				case BETWEEN:
					Number toNumber = (Number) filterProperty.getToValue();
					if (toNumber == null) {
						logger.warn("Filter-Operator is BETWEEN, but toValue is missing");
						break;
					}
					predicates.add(cb.between(expression, (long) number, (long) toNumber));
					break;

				default:
					break;
				}
			}
				break;

			case DATE: {
				Expression<Date> expressionDate = preFrom.<Date> get((SingularAttribute<Object, Date>) attribute);
				if (filterProperty.getValue() == null) {
					continue;
				}
				Date date;
				if (filterProperty.getValue() instanceof LocalDate) {
					Instant instant = ((LocalDate) filterProperty.getValue()).atStartOfDay(ZoneId.systemDefault()).toInstant();
					date = Date.from(instant);
				} else {
					List<LocalDate> dates = (List<LocalDate>) filterProperty.getValue();
					if (dates.size() > 1) {
						predicates.add(cb.between(expressionDate, DcemUtils.getDayBegin(dates.get(0)), DcemUtils.getDayEnd(dates.get(1))));
					}
					
					filterProperty.setValue(null);
				}
				// switch (filterProperty.getFilterOperator()) {
				// case EQUALS:
				// predicates.add(cb.between(expressionDate, DcemUtils.setDayBegin(date), DcemUtils.setDayEnd(date)));
				// break;
				// case LESSER:
				// predicates.add(cb.lessThan(expressionDate, DcemUtils.setDayBegin(date)));
				// break;
				// case GREATER:
				// predicates.add(cb.greaterThan(expressionDate, DcemUtils.setDayEnd(date)));
				// break;
				// case BETWEEN:
				// Date toDate = (Date) filterProperty.getToValue();
				// if (toDate == null) {
				// logger.warn("Filter-Operator is BETWEEN, but toValue is missing");
				// break;
				// }
				// predicates.add(cb.between(expressionDate, DcemUtils.setDayBegin(date), DcemUtils.setDayEnd(toDate)));
				// break;
				// default:
				// break;
				// }

				break;
			}

			case DATE_TIME: {
				Expression<Date> expressionDate = preFrom.<Date> get((SingularAttribute<Object, Date>) attribute);
				Date date = (Date) filterProperty.getValue();
				switch (filterProperty.getFilterOperator()) {

				default:
				case EQUALS:
					predicates.add(cb.equal(expressionDate, date));
					break;
				case LESSER:
					predicates.add(cb.lessThan(expressionDate, date));
					break;
				case GREATER:
					predicates.add(cb.greaterThan(expressionDate, date));
					break;

				}
				break;
			}

			default:
				throw new DcemException(DcemErrorCodes.UNSUPPORTED_FILTER, "Unsupported filter  : " + filterProperty);
			}

		}

		// if (predicates.size() == 0) {
		// throw new RuntimeException("Ther must be at lease one predicate");
		if (predicates.size() == 1) {
			return predicates.get(0);
		}
		// } else {
		return (cb.and(predicates.toArray(new Predicate[predicates.size()])));
		// }
	}

	public List<T> selectCriteriaQueryFilters(List<ApiFilterItem> filters, int firstResult, int maxResult) throws DcemException {
		List<ViewVariable> viewVariables = DcemUtils.getViewVariables(entityClass, null, "", null);
		for (ApiFilterItem apiFilterItem : filters) {
			ViewVariable viewVariable = getViewVariable(viewVariables, apiFilterItem.getName());
			if (viewVariable == null) {
				throw new DcemException(DcemErrorCodes.INVALID_FILTER_NAME,
						apiFilterItem.getName() + ", Possible Values: " + getViewVariableIds(viewVariables));
			}
			switch (viewVariable.getVariableType()) {
			case DATE:
				Date date = null;
				try {
					if (apiFilterItem.getValue().length() == 10) {
						date = dateDayFomat.parse(apiFilterItem.getValue());
					} else {
						date = dateFomat.parse(apiFilterItem.getValue());
					}
					viewVariable.setFilterValueOnly(date);
					break;
				} catch (ParseException e) {
					throw new DcemException(DcemErrorCodes.INVALID_DATE_FORMAT,
							apiFilterItem.getValue() + ". Format: " + DcemConstants.DAY_TIME_FORMAT + " OR " + DcemConstants.DAY_FORMAT, e);
				}
			default:
				viewVariable.setFilterValueOnly(apiFilterItem.getValue());
				break;
			}
			if (apiFilterItem.getOperator() == null) {
				throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "Invalid Operator");
			}
			viewVariable.setFilterOperatorOnly(FilterOperator.valueOf(apiFilterItem.getOperator().name()));
			if (apiFilterItem.getSortOrder() != null) {
				viewVariable.setFilterSortOrderOnly(SortOrder.valueOf(apiFilterItem.getSortOrder().name()));
			}
		}
		List<FilterProperty> filterProperties = null;
		try {
			filterProperties = JpaLazyModel.getFilterProperties(viewVariables);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		List<FilterOrder> filterOrders = new ArrayList<FilterOrder>();
		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.getFilterItem().sortOrder != SortOrder.UNSORTED) {
				boolean descending = false;
				if (viewVariable.getFilterItem().sortOrder == SortOrder.DESCENDING) {
					descending = true;
				}
				filterOrders.add(new FilterOrder(viewVariable.getAttributes(), viewVariable.getId(), descending, viewVariable.getFilterItem().getSortRank()));
				// break;
			}
			if (viewVariable.getDcemGui().sortRank() > 0) {
				filterOrders.add(new FilterOrder(viewVariable.getAttributes(), viewVariable.getId(), true, viewVariable.getFilterItem().getSortRank()));
			}
		}
		Collections.sort(filterOrders, new FilterOrderCompare());

		return selectCriteriaQuery(filterOrders, filterProperties, firstResult, maxResult);
	}

	private String getViewVariableIds(List<ViewVariable> viewVariables) {
		StringBuffer sb = new StringBuffer();
		for (ViewVariable viewVariable : viewVariables) {
			sb.append(viewVariable.getId());
			sb.append(' ');
		}
		return sb.toString();
	}

	private ViewVariable getViewVariable(List<ViewVariable> viewVariables, String name) {
		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.getId().equals(name)) {
				return viewVariable;
			}
		}
		return null;
	}

	// public List<T> selectCriteriaQueryFilters(List<AsApiFilterItem> filters, int firstResult, int maxResult)
	// throws DcemException {
	//
	// Class<?> metaClass = null;
	// try {
	// metaClass = Class.forName(entityClass.getName() + "_");
	// } catch (Exception e1) {
	// // logger.debug(e1);
	// }
	// List<FilterProperty> filterProperties = new LinkedList<>();
	// FilterProperty filterProperty;
	// ArrayList<SingularAttribute<?, ?>> attributes;
	// if (filters != null) {
	// for (AsApiFilterItem filter : filters) {
	// Field field = null;
	// try {
	// field = entityClass.getDeclaredField(filter.getName());
	// } catch (Exception e1) {
	// throw new DcemException(DcemErrorCodes.INVALID_FILTER_NAME, filter.getName());
	// }
	//
	// attributes = new ArrayList<>();
	//
	// try {
	// attributes.add((SingularAttribute<?, ?>) metaClass.getField(field.getName()).get(metaClass));
	// } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
	// | SecurityException e) {
	// throw new DcemException(DcemErrorCodes.GENERAL, e.getMessage());
	// }
	//
	// VariableType variableType = VariableType.UNKNOWN;
	// Class<?> cls = field.getType();
	// if ((cls.equals(Integer.class)) || (cls.equals(int.class))) {
	// variableType = VariableType.NUMBER;
	// } else if ((cls.equals(Long.class)) || (cls.equals(long.class))) {
	// variableType = VariableType.NUMBER;
	// } else if (cls.equals(String.class)) {
	// variableType = VariableType.STRING;
	// } else if ((cls.equals(Boolean.class)) || (cls.equals(boolean.class))) {
	// variableType = VariableType.BOOLEAN;
	// } else if ((cls.equals(Date.class)) || (cls.equals(Timestamp.class))
	// || (cls.equals(java.sql.Date.class))) {
	// variableType = VariableType.DATE;
	// } else if (cls.isEnum()) {
	// variableType = VariableType.ENUM;
	// } else {
	//
	// }
	// filterProperty = new FilterProperty(attributes, filter.getValue(), null, variableType,
	// FilterOperator.valueOf(filter.getOperator().name()));
	// filterProperties.add(filterProperty);
	// }
	// }
	// return selectCriteriaQuery(new LinkedList<FilterOrder>(), filterProperties, firstResult, maxResult);
	// }
}
