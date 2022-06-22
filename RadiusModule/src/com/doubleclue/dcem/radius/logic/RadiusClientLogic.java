package com.doubleclue.dcem.radius.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.radius.entities.RadiusClientEntity;

@SuppressWarnings("serial")
@Named("radiusClientLogic")
@ApplicationScoped
public class RadiusClientLogic implements Serializable {

	@Inject
	EntityManager em;

	public RadiusClientLogic() {
		super();
	}

	public RadiusClientEntity getRadiusClientByIp(String ipAddress) {
		TypedQuery<RadiusClientEntity> query = em.createNamedQuery(RadiusClientEntity.GET_CLIENT_BY_IPNUMBER,
				RadiusClientEntity.class);
		query.setParameter(1, ipAddress);
		try {
			return query.getSingleResult();
		} catch (PersistenceException exp) {
			return null;
		}
	}
	
	public RadiusClientEntity getRadiusClientById(int id) {
		return em.find(RadiusClientEntity.class, id);
	}
	
	public RadiusClientEntity getRadiusClientName(String name) {
		TypedQuery<RadiusClientEntity> query = em.createNamedQuery(RadiusClientEntity.GET_CLIENT_BY_NAME,
				RadiusClientEntity.class);
		query.setParameter(1, name);
		try {
			return query.getSingleResult();
		} catch (PersistenceException exp) {
			return null;
		}

	}
	
	public List<PolicyAppEntity> getApplicationIdentifiers() {
		List<RadiusClientEntity> list = getAllClients();
		List<PolicyAppEntity> appList = new ArrayList<>(list.size());
		appList.add(new PolicyAppEntity (AuthApplication.RADIUS, 0, null));
		for (RadiusClientEntity clientEntity: list) {
			appList.add(new PolicyAppEntity (AuthApplication.RADIUS, clientEntity.getId(), clientEntity.getName() ));
		}
		return appList;
	}
	
	public List<RadiusClientEntity> getAllClients() {
		TypedQuery<RadiusClientEntity> query = em.createNamedQuery(RadiusClientEntity.GET_ALL_CLIENTS,
				RadiusClientEntity.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void add(RadiusClientEntity clientEntity) {
		em.persist(clientEntity);

	}

}
