package com.doubleclue.dcem.core.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.utils.compare.CompareException;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;

@ApplicationScoped
@Named("roleLogic")
public class RoleLogic {

	@Inject
	EntityManager em;

	@Inject
	ActionLogic actionLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	AuditingLogic auditingLogic;

	public List<String> dcemRoleNames() {
		return getDcemRoleNames();
	}

	public List<String> getDcemRoleNames() {
		TypedQuery<String> query = em.createNamedQuery(DcemRole.GET_ALL_NAMES, String.class);
		try {
			return query.getResultList();
		} catch (Throwable e) {
			return null;
		}
	}

	public DcemRole getDcemRole(String roleName) {
		Query query = em.createNamedQuery(DcemRole.GET_ROLE_BY_NAME);
		query.setParameter(1, roleName);
		try {
			return (DcemRole) query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public List<DcemRole> getAllDcemRoles() {
		TypedQuery<DcemRole> query = em.createNamedQuery(DcemRole.GET_ALL_ROLES, DcemRole.class);
		try {
			return query.getResultList();
		} catch (Throwable e) {
			return null;
		}
	}

	public List<DcemRole> getDcemRolesBelowRank(int rank) {
		TypedQuery<DcemRole> query = em.createNamedQuery(DcemRole.GET_ROLES_BELOW_RANK, DcemRole.class);
		query.setParameter(1, rank);
		try {
			return query.getResultList();
		} catch (Throwable e) {
			return null;
		}
	}

	@DcemTransactional
	public void addActionToRole(String role, DcemAction dcemAction) {
		DcemRole dcemRole = getDcemRole(role);
		List<DcemAction> actions = new ArrayList<>();
		actions.add(dcemAction);
		addActionsToRole(dcemRole, actions);
	}

	@DcemTransactional
	public void addActionsToRole(DcemRole dcemRole, List<DcemAction> actions) {
		if (dcemRole != null) {
			if (dcemRole.getActions() == null) {
				dcemRole.setActions(new HashSet<DcemAction>());
			}
			dcemRole.getActions().addAll(actions);
		}
	}
	
	@DcemTransactional
	public void saveAssignments(Map<Integer, ActionRoleAssignment> assignmentMap, DcemAction dcemAction) throws CompareException {
		List <DcemRole> dcemRoles = getDcemRolesBelowRank(operatorSessionBean.getDcemUser().getDcemRole().getRank());
		for (DcemRole role : dcemRoles) {
			Set<DcemAction> actions = new HashSet<>();
			for (Map.Entry<Integer, ActionRoleAssignment> entry : assignmentMap.entrySet()) {
				if (entry.getValue().getRoleAssigned().get(role.getid())) {
					actions.add(actionLogic.getDcemAction(entry.getKey()));
				}
			}
			DcemRole updatedRole = new DcemRole();
			CompareUtils.copyObject(role, updatedRole);
			updatedRole.setActions(actions);
			auditingLogic.addAudit(dcemAction,updatedRole);
			
			
			role.updateActions(actions);
		}
	}

	@DcemTransactional
	public void deleteRoles(DcemAction dcemAction, List<Object> actionObjects) {
		DcemRole dcemRole;
		StringBuffer sb = new StringBuffer();
		for (Object obj : actionObjects) {
			dcemRole = (DcemRole) em.merge(obj);
			em.remove(dcemRole);
			sb.append(dcemRole.toString());
			sb.append(", ");
		}
		auditingLogic.addAudit(dcemAction, sb.toString());
	}

	@DcemTransactional
	public void addActionToRoles(DcemAction dcemAction, RawAction rawAction) {
		if (rawAction.getNoPermissionForRole() == null) { // add to every role.
			addActionToRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN, dcemAction);
			addActionToRole(DcemConstants.SYSTEM_ROLE_ADMIN, dcemAction);
			addActionToRole(DcemConstants.SYSTEM_ROLE_HELPDESK, dcemAction);
			addActionToRole(DcemConstants.SYSTEM_ROLE_VIEWER, dcemAction);
		} else {
			for (String role : rawAction.getNoPermissionForRole()) {
				addActionToRole(role, dcemAction);
			}
		}
	}
	
	

	@DcemTransactional
	public DcemRole addRole(DcemRole role) {
		DcemRole dcemRoleExisting = getDcemRole(role.getName());
		if (dcemRoleExisting == null) {
			em.persist(role);
			return role;
		} else {
			return dcemRoleExisting;
		}
	}
}
