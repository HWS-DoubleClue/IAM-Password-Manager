package com.doubleclue.dcem.as.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.AppPolicyGroupEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("policyLogic")
public class PolicyLogic implements ReloadClassInterface {

	private static final Logger logger = LogManager.getLogger(PolicyLogic.class);

	@Inject
	EntityManager em;

	@Inject
	DcemApplicationBean dcemApplication;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	FingerprintLogic fingerPrintLogic;

	@Inject
	AsModule asModule;

	ObjectMapper objectMapper = new ObjectMapper();

	// @DcemTransactional
	// public void addApplicationsWithoutPolicies() {
	// for (AuthApplication app : AuthApplication.values()) {
	// if (app.isWithPolicies() == false) {
	// PolicyAppEntity appEntity = new PolicyAppEntity(app, 0, null);
	// appEntity.setDisabled(true);
	// em.persist(appEntity);
	// }
	// }
	// return;
	// }
	@DcemTransactional
	public Map<AuthApplication, PolicyAppEntity> getMainPolicies() {
		TypedQuery<PolicyAppEntity> query = em.createNamedQuery(PolicyAppEntity.GET_ALL_MAIN_POLICY_APP, PolicyAppEntity.class);
		List<PolicyAppEntity> list = query.getResultList();
		Map<AuthApplication, PolicyAppEntity> map = new HashMap<>();
		for (PolicyAppEntity appEntity : list) {
			map.put(appEntity.getAuthApplication(), appEntity);
		}
		for (AuthApplication app : AuthApplication.values()) {
			if (app.isWithPolicies() == false && (map.get(app) == null)) {
				PolicyAppEntity appEntity = new PolicyAppEntity(app, 0, null);
				appEntity.setDisabled(true);
				em.persist(appEntity);
				map.put(appEntity.getAuthApplication(), appEntity);
			}
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@DcemTransactional
	public void syncPolicyAppEntity() {
		HashSet<PolicyAppEntity> policyApplications = new HashSet<PolicyAppEntity>();
		for (DcemModule module : dcemApplication.getSortedModules()) {
			List<PolicyAppEntity> list = (List<PolicyAppEntity>) module.getPolicyApplications();
			if (list != null) {
				policyApplications.addAll(list);
			}
		}

		List<PolicyAppEntity> appEntities = getAllApplications();
		boolean found = false;
		String changedName;
		PolicyAppEntity appEntityDb = null;
		for (PolicyAppEntity detachedAppEntity : policyApplications) {
			found = false;
			changedName = null;
			for (PolicyAppEntity appEntity : appEntities) {
				if (appEntity.equals(detachedAppEntity) == true) {
					if (appEntity.getSubName() != null && appEntity.getSubName().equals(detachedAppEntity.getSubName()) == false) {
						changedName = detachedAppEntity.getSubName();
					}
					found = true;
					appEntityDb = appEntity;
					break;

				}
			}
			if (found == false) {
				if (detachedAppEntity.isDisabled() == false) {
					PolicyAppEntity appEntity = getPolicyAppEntity(detachedAppEntity.getAuthApplication(), detachedAppEntity.getSubId());
					if (appEntity == null) {
						appEntity = new PolicyAppEntity(detachedAppEntity.getAuthApplication(), detachedAppEntity.getSubId(), detachedAppEntity.getSubName());
						appEntity.setDisabled(detachedAppEntity.isDisabled());
						em.persist(appEntity);
					} else {
						appEntity.setDisabled(false);
					}

				}

			}
			if (changedName != null) {
				appEntityDb.setSubName(changedName);
			}
		}

		for (PolicyAppEntity appEntity : appEntities) {
			if (policyApplications.contains(appEntity) == false) {
				List<AppPolicyGroupEntity> assigned = getPolicyGroups(appEntity);
				for (AppPolicyGroupEntity appPolicyGroupEntity : assigned) {
					em.remove(appPolicyGroupEntity);
				}
				appEntity.setDisabled(true); //
			}
		}
	}

	/**
	 * @param authApplication
	 * @param subId
	 * @param userLoginId
	 * @return
	 * @throws Exception
	 */
	public PolicyEntity getPolicy(AuthApplication authApplication, int subId, DcemUser dcemUser) throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		HashMap<PolicyAppEntity, Map<String, PolicyEntity>> appPoliciesTmp = tenantData.getApplicationPolicies().get();
		PolicyAppEntity policyAppEntityDetached = new PolicyAppEntity(authApplication, subId, null);
		Map<String, PolicyEntity> policyMap = appPoliciesTmp.get(policyAppEntityDetached);

		if (policyMap == null || policyMap.size() == 0) {
			PolicyAppEntity policyAppEntityDetachedSub = new PolicyAppEntity(authApplication, 0, null);
			policyMap = appPoliciesTmp.get(policyAppEntityDetachedSub);
		}
		if (policyMap == null) {
			if (logger.isDebugEnabled()) {
				logger.debug(
						"Policy for " + policyAppEntityDetached.toString() + (dcemUser != null ? ", User: " + dcemUser.getLoginId() : "") + " > GlobalPolicy");
			}
			return tenantData.getGlobalEntityPolicy();
		}

		// check if user is in a group
		PolicyEntity policyEntity = null;
		int restPolicies = 0;
		PolicyEntity appPolicyEntity = policyMap.get(0);
		if (appPolicyEntity != null) {
			restPolicies = 1;
		}
		if (dcemUser != null && policyMap.size() > restPolicies) {
			HashSet<String> userGroupNames = userLogic.getUserGroupNames(dcemUser, null);
			for (String groupName : policyMap.keySet()) {
				if (userGroupNames.contains(groupName)) {
					policyEntity = policyMap.get(groupName);
					break;
				}
			}
		}

		if (policyEntity == null) {
			policyEntity = appPolicyEntity; // this is application policy without group
			if (policyEntity == null) {
				// policyMap = appPoliciesTmp.get(new PolicyAppEntity(authApplication, 0,
				// null));
				policyEntity = policyMap.get("#"); // this is the Parent Application-Type
				if (policyEntity == null) {
					if (logger.isDebugEnabled()) {
						String loginId = dcemUser == null ? null : dcemUser.getLoginId();
						logger.debug("Policy for " + policyAppEntityDetached.toString() + ", User: " + loginId + " > GlobalPolicy");
					}
					return tenantData.getGlobalEntityPolicy();
				}
			}
		}
		if (logger.isDebugEnabled()) {
			String loginId = dcemUser == null ? null : dcemUser.getLoginId();
			logger.debug("Policy for " + policyAppEntityDetached.toString() + ", User: " + loginId + " > " + policyEntity.getName());
		}
		return policyEntity;
	}

	PolicyEntity getPolicyByName(String name) {
		TypedQuery<PolicyEntity> query = em.createNamedQuery(PolicyEntity.GET_POLICY_BY_NAME, PolicyEntity.class);
		query.setParameter(1, name);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@DcemTransactional
	public void deletePolicyEntity(String name) throws Exception {
		PolicyEntity policyEntity = getPolicyByName(name);
		if (policyEntity != null) {
			List<AppPolicyGroupEntity> appPolicyGroupList = getAppPolicyGroupsByPolicyEntity(policyEntity);
			List<PolicyAppEntity> policyAppEntityList = new ArrayList<PolicyAppEntity>();
			if (appPolicyGroupList != null) {
				for (AppPolicyGroupEntity appPolicyGroup : appPolicyGroupList) {
					policyAppEntityList.add(appPolicyGroup.getPolicyAppEntity());
					em.remove(appPolicyGroup);
				}
			}
			syncPolicyAppEntity();
			em.remove(policyEntity);
			for (PolicyAppEntity policyAppEntity : policyAppEntityList) {
				em.remove(policyAppEntity);
			}
		}
	}

	public List<AppPolicyGroupEntity> getAppPolicyGroupsByPolicyEntity(PolicyEntity policyEntity) {
		TypedQuery<AppPolicyGroupEntity> query = em.createNamedQuery(AppPolicyGroupEntity.GET_POLICIES_BY_POLICY_ENTITY, AppPolicyGroupEntity.class);
		query.setParameter(1, policyEntity);
		try {
			return query.getResultList();
		} catch (NoResultException e) {
			return null;
		}
	}

	@DcemTransactional
	public PolicyEntity createOrGetGlobalPolicy() throws DcemException {
		PolicyEntity globalPolicy = getPolicyByName(DcemConstants.GLOBAL_POLICY);
		if (globalPolicy == null) {
			globalPolicy = new PolicyEntity();
			globalPolicy.setName(DcemConstants.GLOBAL_POLICY);
			DcemPolicy dcemPolicy = new DcemPolicy();
			globalPolicy.setDcemPolicy(dcemPolicy);
			setGlobalPolicy(globalPolicy, true);
		}
		return globalPolicy;
	}

	@DcemTransactional
	public void setGlobalPolicy(PolicyEntity globalPolicy, boolean isAdding) throws DcemException {
		PolicyEntity newPolicy;
		if (isAdding) {
			newPolicy = new PolicyEntity();
			newPolicy.setName(globalPolicy.getName());
			newPolicy.setDcemPolicy(globalPolicy.getDcemPolicy());
		} else {
			newPolicy = globalPolicy;
		}
		addOrUpdatePolicy(newPolicy, new DcemAction(SystemModule.MODULE_ID, null, isAdding ? DcemConstants.ACTION_ADD : DcemConstants.ACTION_EDIT), false);
		if (asModule.getTenantData() != null) {
			asModule.getTenantData().setGlobalEntityPolicy(newPolicy);
		}
	}

	@DcemTransactional
	public PolicyEntity createOrGetManagementPolicy() throws DcemException {
		PolicyEntity mgtPolicy = getPolicyByName(DcemConstants.MANAGEMENT_POLICY);
		if (mgtPolicy == null) {
			mgtPolicy = new PolicyEntity();
			mgtPolicy.setName(DcemConstants.MANAGEMENT_POLICY);
			DcemPolicy dcemPolicy = new DcemPolicy();
			dcemPolicy.allowMethod(AuthMethod.PASSWORD, true);
			dcemPolicy.setDefaultPolicy(AuthMethod.PASSWORD);
			mgtPolicy.setDcemPolicy(dcemPolicy);
			setManagementPolicy(mgtPolicy, true);
		}
		return mgtPolicy;
	}

	@DcemTransactional
	public void setManagementPolicy(PolicyEntity mgtPolicy, boolean isAdding) throws DcemException {
		PolicyEntity newPolicy;
		if (isAdding) {
			newPolicy = new PolicyEntity();
			newPolicy.setName(mgtPolicy.getName());
			newPolicy.setDcemPolicy(mgtPolicy.getDcemPolicy());
		} else {
			newPolicy = mgtPolicy;
		}
		addOrUpdatePolicy(newPolicy, new DcemAction(SystemModule.MODULE_ID, null, isAdding ? DcemConstants.ACTION_ADD : DcemConstants.ACTION_EDIT), false);
		PolicyAppEntity policyAppEntity = getPolicyAppEntity(AuthApplication.DCEM, 0);
		if (policyAppEntity != null) {
			PolicyTreeItem policyTreeItem = new PolicyTreeItem(policyAppEntity, null);
			assignPolicy(policyTreeItem, 0, newPolicy.getId(), 0);
		}
	}

	public List<PolicyAppEntity> getAllApplications() {
		TypedQuery<PolicyAppEntity> query = em.createNamedQuery(PolicyAppEntity.GET_ALL_POLICY_APP, PolicyAppEntity.class);
		return query.getResultList();
	}

	/**
	 * @throws DcemException
	 */
	public void updatePolicyCache() throws DcemException {
		AsTenantData tenantData = asModule.getTenantData();
		List<PolicyAppEntity> appEntities = getAllApplications();
		List<PolicyAppEntity> policyAppEntities = new ArrayList<>(appEntities.size());
		policyAppEntities.addAll(appEntities);
		PolicyEntity globalEntityPolicy = getPolicyByName(DcemConstants.GLOBAL_POLICY);
		em.detach(globalEntityPolicy);
		try {
			globalEntityPolicy.serializeDcemPolicy();
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't deserialie the global policy", exp);
		}
		tenantData.setGlobalEntityPolicy(globalEntityPolicy);

		HashMap<PolicyAppEntity, Map<String, PolicyEntity>> applicationPoliciesTmp = new HashMap<>();
		HashMap<String, PolicyEntity> groupPolicyMap;

		List<PolicyAppEntity> appPolicies = getAllApplications();
		for (PolicyAppEntity appEntity : appPolicies) {
			em.detach(appEntity);
			List<AppPolicyGroupEntity> appGroupEntities = getPolicyGroups(appEntity);
			if (appGroupEntities.size() == 0) {
				applicationPoliciesTmp.put(appEntity, new LinkedHashMap<String, PolicyEntity>());
			} else {
				groupPolicyMap = new LinkedHashMap<>();
				for (AppPolicyGroupEntity appPolicyGroupEntity : appGroupEntities) {
					if (appPolicyGroupEntity.getPolicyEntity() == null) {
						continue;
					}
					try {
						appPolicyGroupEntity.getPolicyEntity().serializeDcemPolicy();
					} catch (Exception e) {
						throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't parse DcemPolicy", e);
					}

					if (appPolicyGroupEntity.getGroup() == null) {
						groupPolicyMap.put("#", appPolicyGroupEntity.getPolicyEntity());
					} else {
						groupPolicyMap.put(appPolicyGroupEntity.getGroup().getName(), appPolicyGroupEntity.getPolicyEntity());
					}
				}
				applicationPoliciesTmp.put(appEntity, groupPolicyMap);
			}
		}
		tenantData.setPolicyAppEntities(policyAppEntities);
		tenantData.getApplicationPolicies().set(applicationPoliciesTmp);
	}

	public List<AppPolicyGroupEntity> getPolicyGroups(PolicyAppEntity appEntity) {
		TypedQuery<AppPolicyGroupEntity> query = em.createNamedQuery(AppPolicyGroupEntity.GET_APP_POLICIES, AppPolicyGroupEntity.class);
		query.setParameter(1, appEntity);
		return query.getResultList();
	}

	public List<AuthMethod> getAuthMethods(AuthApplication authApplication, int subId, DcemUser user) throws DcemException {
		PolicyEntity policyEntity = getPolicy(authApplication, subId, user);
		return getAuthMethods(policyEntity, authApplication, subId, user, null);
	}

	/**
	 * @param authApplication
	 * @param subId
	 * @param userLoginId
	 * @param network
	 * @param fingerprint
	 * @return
	 * @throws DcemException
	 */
	public List<AuthMethod> getAuthMethods(PolicyEntity policyEntity, AuthApplication authApplication, int subId, DcemUser user, String sessionCookie) throws DcemException {

		DcemPolicy dcemPolicy = policyEntity.getDcemPolicy();
		if (dcemPolicy.isDenyAccess()) { // ignore rest of policy rules
			return new ArrayList<AuthMethod>();
		} else {
			if (user != null) {
				if (dcemPolicy.isRefrain2FaWithInTime() == true) {
					PolicyAppEntity appEntity = getDetachedPolicyApp(authApplication, subId);
					if (fingerPrintLogic.verifyFingerprint(user.getId(), appEntity.getId(), sessionCookie)) {
						List<AuthMethod> list = new ArrayList<>(1);
						list.add(AuthMethod.PASSWORD);
						return list;
					}
				}
			}
			return new ArrayList<AuthMethod>(dcemPolicy.getAllowedMethods());
		}
	}

	public boolean isNetworkPassThrough(DcemPolicy dcemPolicy, String network) throws DcemException {
		if (dcemPolicy.getIpRanges() != null && dcemPolicy.getIpRanges().isInRange(network)) {
			return true;
		}
		return false;
	}

	@DcemTransactional
	public PolicyAppEntity getDetachedPolicyApp(AuthApplication application, int subId) {
		AsTenantData tenantData = asModule.getTenantData();
		PolicyAppEntity appEntity = null;
		List<PolicyAppEntity> policyAppEntities = tenantData.getPolicyAppEntities();
		if (policyAppEntities != null) {
			for (PolicyAppEntity entity : policyAppEntities) {
				if (entity.getAuthApplication() == application && entity.getSubId() == subId) {
					appEntity = entity;
					break;
				}
			}
			if (appEntity == null) {
				try {
					reload(null);
					// search again
					policyAppEntities = tenantData.getPolicyAppEntities();
					for (PolicyAppEntity entity : policyAppEntities) {
						if (entity.getAuthApplication() == application && entity.getSubId() == subId) {
							appEntity = entity;
						}
					}
				} catch (DcemException e) {
					logger.warn("Could not find policy entity: app - " + application + ", subId - " + subId, e);
				}
			}
		}
		return appEntity;
	}

	@DcemTransactional
	public void addOrUpdatePolicy(PolicyEntity policyEntity, DcemAction dcemAction, boolean withAudit) throws DcemException {
		if (StringUtils.isValidNameId(policyEntity.getName()) == false) {
			throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, policyEntity.getName());
		}
		DcemPolicy dcemPolicy = policyEntity.getDcemPolicy();
		if (dcemPolicy.getDefaultPolicy() != null) {
			if (dcemPolicy.getAllowedMethods().contains(dcemPolicy.getDefaultPolicy()) == false) {
				dcemPolicy.getAllowedMethods().add(dcemPolicy.getDefaultPolicy());
			}
		}

		try {
			policyEntity.setJsonPolicy(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(policyEntity.getDcemPolicy()));
		} catch (JsonProcessingException e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't serialize Policy");
		}
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			// New Template Set all other previos Templates inactive
			policyEntity.setId(null);
			em.persist(policyEntity);
		} else {
			em.merge(policyEntity);
		}
		if (withAudit) {
			auditingLogic.addAudit(dcemAction, policyEntity.toString());
		}
	}

	private PolicyAppEntity getPolicyAppEntity(AuthApplication authApplication, int subId) {
		TypedQuery<PolicyAppEntity> query = em.createNamedQuery(PolicyAppEntity.GET_POLICY_APP, PolicyAppEntity.class);
		query.setParameter(1, authApplication);
		query.setParameter(2, subId);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public List<PolicyEntity> getPoliciesWithAssignmrents() {
		TypedQuery<PolicyEntity> query = em.createNamedQuery(PolicyEntity.GET_ALL_POLICY, PolicyEntity.class);
		List<PolicyEntity> policyEntities = query.getResultList();
		TypedQuery<AppPolicyGroupEntity> query2 = em.createNamedQuery(AppPolicyGroupEntity.GET_POLICIES, AppPolicyGroupEntity.class);
		List<AppPolicyGroupEntity> appPolicyGroupEntities;
		List<String> assignedTo;

		for (PolicyEntity policyEntity : policyEntities) {
			query2.setParameter(1, policyEntity);
			appPolicyGroupEntities = query2.getResultList();
			assignedTo = new ArrayList<>(appPolicyGroupEntities.size());

			for (AppPolicyGroupEntity appPolicyGroupEntity : appPolicyGroupEntities) {
				String groupName = "";
				if (appPolicyGroupEntity.getGroup() != null) {
					groupName = "/" + appPolicyGroupEntity.getGroup().getName();
				}
				assignedTo.add(appPolicyGroupEntity.getPolicyAppEntity().toString() + groupName);
			}
			policyEntity.setAssignedTo(assignedTo);
		}
		return policyEntities;
	}

	public AppPolicyGroupEntity getAppPolicyGroupEntity(PolicyEntity policy, PolicyAppEntity app) {
		TypedQuery<AppPolicyGroupEntity> query = em.createNamedQuery(AppPolicyGroupEntity.GET_BY_POLICY_AND_APP, AppPolicyGroupEntity.class);
		query.setParameter(1, policy);
		query.setParameter(2, app);
		List<AppPolicyGroupEntity> result = query.getResultList();
		return result.size() > 0 ? result.get(0) : null;
	}

	public List<SelectItem> getPolciesSelection() {
		TypedQuery<PolicyEntity> query = em.createNamedQuery(PolicyEntity.GET_ALL_POLICY, PolicyEntity.class);
		List<PolicyEntity> list = query.getResultList();
		List<SelectItem> selection = new ArrayList<>(list.size());
		for (PolicyEntity entity : list) {
			selection.add(new SelectItem(entity.getId(), entity.getName()));
		}
		return selection;
	}

	// public HashMap<PolicyAppEntity, HashMap<Integer, PolicyEntity>>
	// getPolicyTree() {
	// HashMap<PolicyAppEntity, HashMap<Integer, PolicyEntity>> appPoliciesTmp =
	// applicationPolicies.get();
	// return appPoliciesTmp;
	//
	// }

	@DcemTransactional
	public void assignPolicy(PolicyTreeItem pti, Integer groupId, Integer policyId, int priority) {
		AppPolicyGroupEntity appPolicyGroupEntity;
		DcemGroup group = null;
		if (groupId != 0) {
			group = groupLogic.getGroup(groupId);
		}
		PolicyEntity policyEntity = null;
		if (policyId != 0) {
			policyEntity = em.find(PolicyEntity.class, policyId);
		}

		PolicyAppEntity appEntity = em.merge(pti.getAppEntity());
		if (pti.policyGroupEntity != null) {
			appPolicyGroupEntity = em.merge(pti.policyGroupEntity);
		} else {
			appPolicyGroupEntity = getAppPolicyGroupEntity(policyEntity, appEntity);
			if (appPolicyGroupEntity == null) {
				appPolicyGroupEntity = new AppPolicyGroupEntity();
			}
		}

		appPolicyGroupEntity.setPriority(priority);
		appPolicyGroupEntity.setGroup(group);
		appPolicyGroupEntity.setPolicyEntity(policyEntity);
		appPolicyGroupEntity.setPolicyAppEntity(appEntity);

		if (appPolicyGroupEntity.getId() == null) {
			em.persist(appPolicyGroupEntity);
		} else {
			em.merge(appPolicyGroupEntity);
		}
	}

	public AppPolicyGroupEntity getPolicyGroupEntity(Integer id) {
		return em.find(AppPolicyGroupEntity.class, id);
	}

	@Override
	@DcemTransactional
	public void reload(String info) throws DcemException {
		try {
			syncPolicyAppEntity();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updatePolicyCache();
	}

	@DcemTransactional
	public void setBackdoorToManagementPolicy() {
		try {
			// create the policy in case it's lost
			PolicyEntity mgtPolicy = createOrGetManagementPolicy();
			DcemPolicy dcemPolicy = mgtPolicy.getDcemPolicy();
			// make password the default auth method to remove 2FA
			dcemPolicy.allowMethod(AuthMethod.PASSWORD, true);
			dcemPolicy.setDefaultPolicy(AuthMethod.PASSWORD);
			mgtPolicy.setDcemPolicy(dcemPolicy);
			// re-assign the policy
			setManagementPolicy(mgtPolicy, false);
		} catch (Exception e) {
			logger.warn(e);
		}
	}

	@DcemTransactional
	public void deletePolicesGroup(DcemGroup dcemGroup) {
		Query query = em.createNamedQuery(AppPolicyGroupEntity.DELETE_POLICIY_GROUP);
		query.setParameter(1, dcemGroup);
		query.executeUpdate();
	}

	public List<AppPolicyGroupEntity> getAppPolicyByGroupEntity(DcemGroup dcemGroup) {
		TypedQuery<AppPolicyGroupEntity> query = em.createNamedQuery(AppPolicyGroupEntity.GET_POLICIES_BY_GROUP, AppPolicyGroupEntity.class);
		query.setParameter(1, dcemGroup);
		List<AppPolicyGroupEntity> result = query.getResultList();
		return result;
	}
}
