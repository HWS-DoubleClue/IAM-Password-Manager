package com.doubleclue.dcem.admin.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.entities.TenantBrandingEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
