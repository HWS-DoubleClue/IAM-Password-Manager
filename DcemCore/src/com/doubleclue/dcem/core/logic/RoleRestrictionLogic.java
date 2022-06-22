package com.doubleclue.dcem.core.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.RoleRestriction;

@ApplicationScoped
public class RoleRestrictionLogic {
	
	@Inject
	EntityManager em;
	
	public List<RoleRestriction> getRestrictionsForView (DcemRole dcemRole, SubjectAbs subject) {
		List<RoleRestriction> roleRestrictions;
		TypedQuery<RoleRestriction> query = em.createNamedQuery(RoleRestriction.GET_VIEW_RESTRICTIONS, RoleRestriction.class);
		query.setParameter(1, dcemRole);
		query.setParameter(2, subject.getModuleId());
		query.setParameter(3, subject.getName());
		try {
			roleRestrictions =  query.getResultList();
			return roleRestrictions;
		} catch (Throwable e) {
			return null;
		}		
	}
	
	

}
