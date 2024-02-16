package com.doubleclue.dcem.core.logic;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("groupLogic")
public class GroupLogic {

	private static Logger logger = LogManager.getLogger(GroupLogic.class);

	@Inject
	DomainLogic domainLogic;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	EntityManager em;

	@Inject
	AdminModule adminModule;

	@Inject
	DcemApplicationBean applicationBean;

	@DcemTransactional
	public void addOrUpdateGroup(DcemGroup group, DcemAction dcemAction, boolean withAudit) throws DcemException {
		if (StringUtils.isValidNameId(group.getName()) == false) {
			throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, group.getName());
		}
		String[] domainUser = group.getName().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
		if (domainUser.length > 1) {
			DomainApi domainApi = domainLogic.getDomainApi(domainUser[0]);
			group.setDomainEntity(domainApi.getDomainEntity());
		}
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			// New Template Set all other previos Templates inactive
			group.setId(null);
			em.persist(group);
		} else {
			em.merge(group);
		}
		if (withAudit) {
			auditingLogic.addAudit(dcemAction, group.toString());
		}
	}

	@DcemTransactional
	public void addMember(DcemGroup group, String loginId) throws DcemException {
		group = em.find(DcemGroup.class, group.getId());
		boolean exists = false;
		for (DcemUser user : group.getMembers()) {
			if (user.getLoginId().equals(loginId)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			DcemUser user = userLogic.getUser(loginId);
			if (user != null) {
				group.getMembers().add(user);
			} else {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, "User '" + loginId + "' does not exist.");
			}
		} else {
			throw new DcemException(DcemErrorCodes.MEMBER_EXISTS_ALREADY, "User '" + loginId + "' already exists in group '" + group.getName() + "'.");
		}
	}

	public List<DcemUser> getMembers(DcemGroup group) throws DcemException {
		group = em.find(DcemGroup.class, group.getId());
		List<DcemUser> members;
		if (group.isDomainGroup()) {
			members = domainLogic.getMembersWithUpdate(group);
		} else {
			members = group.getMembers();
		}
		return members;
	}

	@DcemTransactional
	public void removeMembers(DcemGroup group, List<DcemUser> selectedUser) {
		group = em.find(DcemGroup.class, group.getId());
		List<DcemUser> userList = group.getMembers();
		for (DcemUser dcemUser : selectedUser) {
			userList.remove(dcemUser);
		}

	}

	public long getMemberCount(DcemGroup dcemGroup) {
		if (dcemGroup.isDomainGroup()) {
			try {
				return domainLogic.getMembers(dcemGroup).getUsers().size();
			} catch (DcemException e) {
				return -1;
			}
		}
		try {
			TypedQuery<Long> query = em.createNamedQuery(DcemGroup.GET_MEMBER_COUNT, Long.class);
			query.setParameter(1, dcemGroup.getId());
			return query.getSingleResult();
		} catch (Throwable exp) {
			logger.info("Couldn't retrieve member count for group: " + dcemGroup, exp);
		}
		return -1;
	}

	// public Integer getUserGroupId(String userLoginId) {
	// try {
	// TypedQuery<Integer> query = em.createNamedQuery(DcemGroup.GET_MAIN_GROUP_FROM_MEMBER, Integer.class);
	// query.setParameter(1, userLoginId);
	// return query.getSingleResult();
	// } catch (NoResultException exp) {
	// return null;
	// } catch (Exception exp) {
	// logger.info("Couldn't retrieve member count for group: " + userLoginId, exp);
	// throw exp;
	// }
	// }

	public List<String> getUserGroupNames(Integer userId) {
		try {
			TypedQuery<String> query = em.createNamedQuery(DcemGroup.GET_GROUPS_FROM_MEMBER, String.class);
			query.setParameter(1, userId);
			return query.getResultList();
		} catch (Exception exp) {
			logger.info("Couldn't retrieve member count for group: " + userId, exp);
			throw exp;
		}
	}

	public List<DcemGroup> getAllUserGroups(DcemUser dcemUser) throws DcemException {
		return getAllUserGroups(dcemUser, true);
	}

	public List<DcemGroup> getAllUserGroups(DcemUser dcemUser, boolean onlyImported) throws DcemException {
		List<DcemGroup> listGroup = getUserLocalGroups(dcemUser);
		if (dcemUser.isDomainUser()) {
			List<DcemGroup> domainGroups = domainLogic.getUserGroups(dcemUser);
			List<DcemGroup> dbGroups = new ArrayList<DcemGroup>();
			for (DcemGroup dcemGroupDomain : domainGroups) {
				DcemGroup dcemGroupLocally = getGroup(dcemGroupDomain.getName());
				if (dcemGroupLocally != null) {
					dbGroups.add(dcemGroupLocally);
				} else if (onlyImported == false) {
					dbGroups.add(dcemGroupDomain);
				}
			}
			listGroup.addAll(dbGroups);
		}
		return listGroup;
	}

	private List<DcemGroup> getUserLocalGroups(DcemUser dcemUser) {
		TypedQuery<DcemGroup> query = em.createNamedQuery(DcemGroup.GET_USER_GROUPS, DcemGroup.class);
		query.setParameter(1, dcemUser.getId());
		return query.getResultList();
	}

	public List<DcemGroup> getAllGroups() {
		TypedQuery<DcemGroup> query = em.createNamedQuery(DcemGroup.GET_ALL, DcemGroup.class);
		return query.getResultList();
	}

	public DcemGroup getGroup(Integer groupId) {
		return em.find(DcemGroup.class, groupId);
	}

	public DcemGroup getGroup(String domainName, String name) throws DcemException {
		if (domainName == null || domainName.isEmpty()) {
			return getGroup(name);
		} else {
			return getGroup(domainName + DcemConstants.DOMAIN_SEPERATOR + name);
		}

	}

	public DcemGroup getGroup(String groupName) {
		try {
			// this is 2.nd layer cache
			TypedQuery<DcemGroup> query = em.createNamedQuery(DcemGroup.GET_GROUP, DcemGroup.class);
			query.setParameter(1, groupName);
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		} catch (Exception exp) {
			logger.info("Couldn't retrieve  group: " + groupName, exp);
			throw exp;
		}
	}

	@DcemTransactional
	public DcemGroup getRootGroup() throws DcemException {
		DcemGroup rootGroup = adminModule.getTenantData().getRootGroup();
		if (rootGroup == null) {
			rootGroup = getGroup(DcemConstants.GROUP_ROOT);
			if (rootGroup == null) {
				rootGroup = new DcemGroup();
				rootGroup.setName(DcemConstants.GROUP_ROOT);
				em.persist(rootGroup);
			}
			adminModule.getTenantData().setRootGroup(rootGroup);
		}
		return rootGroup;
	}

	@DcemTransactional
	public void addOrUpdateGroupWoAuditing(DcemGroup group) {

		if (group.getId() == null) {
			em.persist(group);
		} else {
			group = em.merge(group);
			em.flush();
		}
	}

	public List<DcemGroup> getGroupsByLdap(DomainEntity ldap) {
		TypedQuery<DcemGroup> query = em.createNamedQuery(DcemGroup.GET_GROUPS_BY_LDAP, DcemGroup.class);
		query.setParameter(1, ldap);
		return query.getResultList();
	}

	@DcemTransactional
	public void removeMemberFromAllGroups(DcemUser dcemUser) throws DcemException {
		List<DcemGroup> groups = getUserLocalGroups(dcemUser);
		List<DcemUser> users = new ArrayList<DcemUser>(1);
		users.add(dcemUser);
		for (DcemGroup group : groups) {
			removeMembers(group, users);
		}
	}

	@DcemTransactional
	public void deleteGroups(List<Object> list, DcemAction dcemAction) throws DcemException {
		DcemGroup dcemGroup;
		StringBuffer sb = new StringBuffer();

		for (Object object : list) {
			dcemGroup = (DcemGroup) object;
			dcemGroup = em.merge(dcemGroup);
			for (DcemModule module : applicationBean.getSortedModules()) {
				module.deleteGroupFromDb(dcemGroup);
			}
			dcemGroup.getMembers().clear();
			em.remove(dcemGroup);
			sb.append(dcemGroup.getId());
			sb.append(" ");
		}
		auditingLogic.addAudit(dcemAction, sb.toString());
	}

	public List<String> getCompleteGroupList(String name, int max) {
		TypedQuery<DcemGroup> query = em.createNamedQuery(DcemGroup.GET_FILTERED_GROUPS, DcemGroup.class);
		query.setParameter(1, "%" + name + "%");
		query.setMaxResults(max);

		try {
			List<DcemGroup> list = query.getResultList();
			List<String> nameList = new ArrayList<>(list.size());
			for (DcemGroup group : list) {
				nameList.add(group.getName());
			}
			return nameList;
		} catch (Throwable exp) {
			logger.warn("Couldn't retrieve groups.", exp);
			return null;
		}
	}
}
