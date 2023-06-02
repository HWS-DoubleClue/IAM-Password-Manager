package com.doubleclue.dcem.core.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public interface JpaPredicate {
	
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root);

}
