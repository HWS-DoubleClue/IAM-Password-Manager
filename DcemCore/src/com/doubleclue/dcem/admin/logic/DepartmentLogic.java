package com.doubleclue.dcem.admin.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DepartmentEntity;

@ApplicationScoped
@Named("departmentLogic")
public class DepartmentLogic {

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;

	private Logger logger = LogManager.getLogger(DepartmentLogic.class);

	public List<String> getCompleteDepartmentList(String name, int max) {
		TypedQuery<String> query = em.createNamedQuery(DepartmentEntity.GET_FILTER_LIST, String.class);
		query.setParameter(1, "%" + name + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public DepartmentEntity getDepartmentByName(String parentDepartmentName) {
		TypedQuery<DepartmentEntity> query = em.createNamedQuery(DepartmentEntity.GET_BY_NAME, DepartmentEntity.class);
		query.setParameter(1, parentDepartmentName);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
}
