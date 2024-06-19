/*
*
*
*
 */
package com.doubleclue.dcem.core.jpa;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
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
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.utils.DcemUtils;

import com.google.common.collect.Lists;

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

	public long createCountCriteriaQuery(Class<?> entityClass, List<FilterProperty> filterProperties, JpaPredicate jpaPredicate) throws DcemException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
		Root<?> root = criteriaQuery.from(entityClass);
		Predicate whereCond = getPredicates(criteriaBuilder, root, filterProperties, jpaPredicate.getPredicates(criteriaBuilder, root));
		if (whereCond != null) {
			criteriaQuery.where(whereCond);
			// criteriaQuery.where(whereCond).distinct(true);
		}
		// Expression<Long> count = criteriaBuilder.countDistinct(root);
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
	public List<T> selectCriteriaQuery(List<FilterOrder> filterOrders, List<FilterProperty> filterProperties, int firstResult, int maxResult,
			JpaPredicate jpaPredicate) throws DcemException {
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(entityClass);
		Root<?> root = criteriaQuery.from(entityClass);

		if (filterOrders != null) {
			List<Order> orders = createOrders(criteriaBuilder, root, filterOrders);
			if (orders.isEmpty() == false) {
				criteriaQuery.orderBy(orders);
			}
		}
		List<Predicate> prePredicates;
		if (jpaPredicate == null) {
			prePredicates = new ArrayList<>();
		} else {
			prePredicates = jpaPredicate.getPredicates(criteriaBuilder, root);
		}

		if (filterProperties != null) {
			Predicate whereCond = getPredicates(criteriaBuilder, root, filterProperties, prePredicates);
			if (whereCond != null) {
				criteriaQuery.where(whereCond);
			}
		}
		// criteriaQuery.distinct(true);
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
				Attribute<?, ?> attribute = filterOrder.getAttributes().get(length - 1);
				From<?, ?> from = null;
				From<?, ?> preFrom = root;
				if (length > 1) {
					// search and create the Joins
					for (int i = 0; i < length - 1; i++) {
						Attribute<?, ?> joinAttribute = filterOrder.getAttributes().get(i);
						from = getJoins(preFrom.getJoins(), attribute.getDeclaringType().getJavaType(), joinAttribute);
						if (from == null) {
							preFrom = preFrom.join(joinAttribute.getName(), JoinType.LEFT);
						} else {
							preFrom = from;
						}
					}
				}
				Order jpaOrder;
				Expression<String> expression = preFrom.<String> get(attribute.getName());
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
	 * @param predicates 
	 * @return
	 * @throws DcemException
	 */
	private Predicate getPredicates(CriteriaBuilder cb, Root<?> root, List<FilterProperty> filterProperties, List<Predicate> predicates) throws DcemException {

		for (FilterProperty filterProperty : filterProperties) {
			if (filterProperty.filterOperator == null || filterProperty.filterOperator.equals(FilterOperator.NONE)) {
				continue;
			}
			if (filterProperty.filterOperator.valueRequired && ((filterProperty.getValue() == null))) {
				continue;
			}
			int length = filterProperty.getAttributes().size();
			if (filterProperty.getAttributes() == null || filterProperty.getAttributes().size() == 0) {
				throw new DcemException(DcemErrorCodes.MISSING_META_DATA_ATRIBUTES, filterProperty.toString());
			}
			Attribute<?, ?> attribute = filterProperty.getAttributes().get(length - 1);
			From<?, ?> from = null;
			From<?, ?> preFrom = root;
			if (length > 1) {
				// search and create the Joins
				for (int i = 0; i < length - 1; i++) {
					Attribute<?, ?> joinAttribute = filterProperty.getAttributes().get(i);
					from = getJoins(preFrom.getJoins(), attribute.getDeclaringType().getJavaType(), joinAttribute);
					if (from == null) {
						preFrom = preFrom.join(joinAttribute.getName(), JoinType.LEFT);
					} else {
						preFrom = from;
					}
				}
			}
			switch (filterProperty.variableType) {

			case LIST:
				System.out.println("JpaSelectProducer.getPredicates()");
				break;
			case STRING: {
				if (filterProperty.getValue() == null) {
					continue;
				}
				Expression<String> expression = preFrom.<String> get(attribute.getName());
				switch (filterProperty.filterOperator) {
				default:
				case LIKE:
					if (((String) filterProperty.getValue()).contains("%")) {
						predicates.add(cb.like(expression, (String) filterProperty.getValue(), DcemConstants.JPA_ESCAPE_CHAR));
					} else {
						predicates.add(
								cb.like(cb.lower(expression), "%" + ((String) filterProperty.getValue()).toLowerCase() + "%", DcemConstants.JPA_ESCAPE_CHAR));
					}
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
				Expression<Integer> expression = preFrom.<Integer> get((SingularAttribute<Object, Integer>) attribute);
				String [] list = (String []) filterProperty.getValue();
				if (list == null || list.length == 0) {
					continue;
				}
				In<Integer> inClause = cb.in (expression);
				for (int i = 0; i < list.length; i++) {
					inClause.value((Integer) Integer.parseInt(list[i]));
				}
				predicates.add(inClause);
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
				if (filterProperty.getValue() == null) {
					continue;
				}
				if (attribute.getJavaType().getSimpleName().equals("LocalDate")) {
					LocalDate localDate = LocalDate.from((LocalDateTime) filterProperty.getValue());
					LocalDate localDateTo = LocalDate.from((LocalDateTime) filterProperty.getToValue());
					Expression<LocalDate> expressionLocalDate = preFrom.<LocalDate> get((SingularAttribute<Object, LocalDate>) attribute);
					predicates.add(cb.between(expressionLocalDate, localDate, localDateTo));
				} else {
					Expression<Date> expressionDate = preFrom.<Date> get((SingularAttribute<Object, Date>) attribute);
					predicates.add(cb.between(expressionDate, DcemUtils.convertToDate((LocalDateTime) filterProperty.getValue()),
							DcemUtils.convertToDate((LocalDateTime) filterProperty.getToValue())));
				}
			}
				break;
			case DATE_TIME: {
				LocalDateTime localDateTimeFrom = null;
				LocalDateTime localDateTimeTo = null;
				if (filterProperty.getValue() instanceof LocalDate) {
					localDateTimeFrom = ((LocalDate) filterProperty.getValue()).atStartOfDay();
					localDateTimeTo = ((LocalDate) filterProperty.getToValue()).atTime(23, 59, 59);
				} else {
					localDateTimeFrom = ((LocalDateTime) filterProperty.getValue());
					localDateTimeTo = ((LocalDateTime) filterProperty.getToValue());
				}
				Expression<LocalDateTime> expressionLocalDate = preFrom.<LocalDateTime> get((SingularAttribute<Object, LocalDateTime>) attribute);
				predicates.add(cb.between(expressionLocalDate, localDateTimeFrom, localDateTimeTo));
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

	public List<T> selectCriteriaQueryFilters(List<ApiFilterItem> filters, int firstResult, int maxResult, JpaPredicate jpaPredicate) throws DcemException {
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

		return selectCriteriaQuery(filterOrders, filterProperties, firstResult, maxResult, jpaPredicate);
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

}
