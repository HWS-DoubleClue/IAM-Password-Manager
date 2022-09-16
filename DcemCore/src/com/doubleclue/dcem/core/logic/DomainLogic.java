package com.doubleclue.dcem.core.logic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.directory.Attributes;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.ChangeUserDomanNameTask;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;

@ApplicationScoped
@Named("domainLogic")
public class DomainLogic implements ReloadClassInterface {

	private static final Logger logger = LogManager.getLogger(DomainLogic.class);

	public final static String DISTINGUISHED_NAME = "distinguishedName";
	public final static String AD_USER_ACCOUNT_CONTROL = "UserAccountControl";
	public final static String MEMBER_OF = "memberOf";
	public final static String MEMBER = "member";
	public final static String GROUP_NAME = "name";
	public static int PAGE_SIZE = 500;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	DcemReportingLogic reportingLogic;

	@PostConstruct
	public void init() {
		testDomainConnections(true);
	}

	// getDomianFromAddress

	public void reload() throws DcemException {
		// System.setProperty("com.sun.jndi.ldap.connect.pool.debug", "fine");
		// System.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", "1");
		// -Dcom.sun.jndi.ldap.connect.pool.debug=fine
		// -Dcom.sun.jndi.ldap.connect.pool.initsize=20
		// -Dcom.sun.jndi.ldap.connect.pool.timeout=10000
		LinkedHashMap<String, DomainApi> domains = getDomains();
		for (DomainApi domainApi : domains.values()) {
			domainApi.close();
		}
		domains.clear();
		DcemException exp = null;
		String ldapErrors = testDomainConnections(false);
		if (ldapErrors.isEmpty() == false) {
			throw new DcemException(DcemErrorCodes.LDAP_INITIALIZATION_FAILED, ldapErrors, exp);
		}
	}

	public String testDomainConnections(boolean displayWelcomeViewAlert) {
		List<DomainEntity> domainEntities = getDomainEntities();
		StringBuffer sb = new StringBuffer();
		LinkedHashMap<String, DomainApi> ldapDomains = getDomains();
		for (DomainApi domainApi : ldapDomains.values()) {
			domainApi.close();
		}
		DomainApi domainApi = null;
		for (DomainEntity detachedDomainEntity : domainEntities) {
			em.detach(detachedDomainEntity);
			switch (detachedDomainEntity.getDomainType()) {
			case Active_Directory:
				domainApi = new DomainLdap(detachedDomainEntity);
				break;
			case Azure_AD:
				domainApi = new DomainAzure(detachedDomainEntity);
				break;
			case Generic_LDAP:
				domainApi = new DomainLdap(detachedDomainEntity);
				break;
			}

			Set<String> emailSuffixs = new HashSet<>();
			detachedDomainEntity.setSetOfEmailDomains(emailSuffixs);
			if (detachedDomainEntity.getMapEmailDomains() != null) {
				String[] emails = detachedDomainEntity.getMapEmailDomains().split(";");
				for (String suffix : emails) {
					emailSuffixs.add(suffix.trim().toLowerCase());
				}
			}
			ldapDomains.put(detachedDomainEntity.getName().toLowerCase(), domainApi);
			try {
				domainApi.testConnection();
			} catch (DcemException e) {
				if (displayWelcomeViewAlert) {
					try {
						reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, e.getErrorCode(), DcemConstants.DOUBLECLUE, AlertSeverity.ERROR,
								true);
					} catch (Exception ex) {
						logger.debug(ex);
					}
				}

				logger.error("Couldn't initialize Domain : " + detachedDomainEntity.getName() + " Exception: " + e.toString());
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(detachedDomainEntity.getName() + ": " + e.toString());
			}
		}
		return sb.toString();
	}

	public void testDomainConnection(DomainEntity domainEntity) throws DcemException {
		DomainApi domainApi = null;
		switch (domainEntity.getDomainType()) {
		case Active_Directory:
			domainApi = new DomainLdap(domainEntity);
			break;
		case Azure_AD:
			domainApi = new DomainAzure(domainEntity);
			break;
		case Generic_LDAP:
			domainApi = new DomainLdap(domainEntity);
			break;
		}
		domainApi.testConnection();
	}

	@DcemTransactional
	public void addOrUpdateDcemLdap(DomainEntity ldapEntity, DcemAction dcemAction) {
		String changeInfo = null;
		ldapEntity.setName(ldapEntity.getName().toLowerCase());
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(DcemConstants.ACTION_COPY)) {
			ldapEntity.setId(null);
			ldapEntity.serializeDomainConfig();
			em.persist(ldapEntity);
			changeInfo = ldapEntity.getName();
		} else {
			DomainEntity oldEntity = getDomainEntityById(ldapEntity.getId());
			try {
				changeInfo = DcemUtils.compareObjects(oldEntity, ldapEntity);
			} catch (Exception exp) {
				logger.warn("Couldn't compare operator", exp);
				changeInfo = "ERROR: " + exp.getMessage();
			}
			String newName = ldapEntity.getName();
			if (!newName.equals(oldEntity.getName())) {
				updateDomainName(oldEntity, newName);
			}
			ldapEntity.serializeDomainConfig();
			ldapEntity = em.merge(ldapEntity);
		}
		auditingLogic.addAudit(dcemAction, changeInfo);
	}

	private void updateDomainName(DomainEntity ldap, String newName) {

		taskExecutor.execute(new ChangeUserDomanNameTask(ldap, ldap.getName(), newName, TenantIdResolver.getCurrentTenant()));

		List<DcemGroup> groups = groupLogic.getGroupsByLdap(ldap);
		for (DcemGroup group : groups) {
			group.setName(replaceLdapName(group.getName(), newName));
			em.merge(group);
		}
	}

	private String replaceLdapName(String text, String newName) {
		int index = text.indexOf(DcemConstants.DOMAIN_SEPERATOR);
		return index > -1 ? newName + text.substring(index) : text;
	}

	public DomainEntity getDomainEntityById(Integer id) {
		return em.find(DomainEntity.class, id);
	}

	public List<DomainEntity> getDomainEntities() {
		TypedQuery<DomainEntity> query = em.createNamedQuery(DomainEntity.GET_ALL, DomainEntity.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void deleteLdapEntity(DomainEntity ldapEntity) {
		ldapEntity = em.find(DomainEntity.class, ldapEntity.getId());
		em.remove(ldapEntity);
	}

	@DcemTransactional
	public void verifyDomainLogin(DcemUser dcemUser, byte[] password) throws DcemException {
		DomainApi domainApi = getDomainApi(dcemUser.getDomainEntity().getName());
		try {
			DcemUser synchUser = domainApi.verifyLogin(dcemUser, password);
			dcemUser.setMobileNumber(synchUser.getMobileNumber());
			dcemUser.setTelephoneNumber(synchUser.getTelephoneNumber());
			dcemUser.setDisplayName(synchUser.getDisplayName());
			dcemUser.setEmail(synchUser.getEmail());
			dcemUser.setUserDn(synchUser.getUserDn());
			dcemUser.setLoginId(synchUser.getLoginId());
			if (synchUser.getLanguage() != null) {
				dcemUser.setLanguage(synchUser.getLanguage());
			}
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_LOGIN_USER_FAILED) {
				logger.info(exp);
			}
			throw exp;
		}
	}

	public DomainApi getDomainApi(String domainName) throws DcemException {
		LinkedHashMap<String, DomainApi> domains = getDomains();
		DomainApi domainApi = domains.get(domainName.toLowerCase());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, "No Domain Entities configured for " + domainName);
		}
		return domainApi;
	}

	public DomainApi getDomainFromEmail(String fqUserId, DomainType domainType) throws DcemException {
		if (fqUserId == null) {
			return null;
		}
		int ind = fqUserId.indexOf("@");
		if (ind == -1) {
			return null;
		}
		String mailSuffix = fqUserId.substring(ind + 1).toLowerCase();
		for (DomainApi domainApi : getDomains().values()) {
			if (domainApi.getDomainEntity().getSetOfEmailDomains().contains(mailSuffix)) {
				if (domainType == null) {
					return domainApi;
				} else if (domainType == domainApi.getDomainEntity().getDomainType()) {
					return domainApi;
				}
			}
		}
		return null;
	}

	public List<String> getUserNames(String domainName, String userFilter) throws DcemException {
		if (domainName == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, null);
		}
		DomainApi domainApi = getDomains().get(domainName.toLowerCase());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, domainName);
		}
		return domainApi.getUserNames(userFilter);
	}

	public List<DcemUser> getUsers(String domainName, String tree, DcemGroup dcemGroup, String userFilter, int pageSize) throws DcemException {
		if (domainName == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, null);
		}
		DomainApi domainApi = getDomains().get(domainName.toLowerCase());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, domainName);
		}
		return domainApi.getUsers(tree, dcemGroup, userFilter, PAGE_SIZE);
	}
	
	public Map<String, Attributes> customSearch(String domainName, String baseDn, String filter, int pageSize) throws DcemException {
		if (domainName == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, null);
		}
		DomainApi domainApi = getDomains().get(domainName.toLowerCase());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, domainName);
		}
		return domainApi.customSearchAttributeMap(filter, baseDn, PAGE_SIZE);
	}
	
	public List<DcemGroup> getGroups(String domainName, String filter, int pageSize) throws DcemException {
		DomainApi domainApi = getDomainApi(domainName);
		return domainApi.getGroups(filter, pageSize);
	}

	public List<DcemGroup> getGroups(String domainName, String filter) throws DcemException {
		return getGroups(domainName, filter, PAGE_SIZE);
	}

	/**
	 * @param ldapdomain
	 * @param filter
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */

	@DcemTransactional
	public List<DcemGroup> getUserGroups(DcemUser dcemUser) throws DcemException {
		dcemUser = em.merge(dcemUser);
		DomainApi domainApi = getDomainApi(dcemUser.getDomainEntity().getName());
		return domainApi.getUserGroups(dcemUser, PAGE_SIZE);
	}

	/**
	 * @param dcemGroup
	 * @return
	 */
	@DcemTransactional
	public List<DcemUser> getMembersWithUpdate(DcemGroup dcemGroup) throws DcemException {
		em.merge(dcemGroup);
		DomainApi domainApi = getDomainApi(dcemGroup.getDomainEntity().getName());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.DOMAIN_DISABLED, dcemGroup.getDomainEntity().getName());
		}
		List<DcemUser> members = domainApi.getGroupMembers(dcemGroup, null);
		List<DcemUser> userList = new ArrayList<>(members.size());
		for (DcemUser member : members) {
			DcemUser user = userLogic.getUser(member.getLoginId());
			if (user != null) {
				userList.add(user);
			} else {
				userLogic.addOrUpdateUserWoAuditing(member);
				userList.add(member);
				logger.info("User imported: " + member.getLoginId());
			}
		}
		return userList;
	}

	public List<DcemUser> getMembers(DcemGroup dcemGroup) throws DcemException {
		DomainApi domainApi = getDomainApi(dcemGroup.getDomainEntity().getName());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.DOMAIN_DISABLED, dcemGroup.getDomainEntity().getName());
		}
		return domainApi.getGroupMembers(dcemGroup, null);
	}

	public DcemUser getUser(String ldapDomainName, String loginId) throws DcemException {
		DomainApi domainApi = getDomainApi(ldapDomainName);
		return domainApi.getUser(loginId);
	}

	public DcemUser getUserFromDomains(String loginId) throws DcemException {
		for (DomainApi domainApi : getDomains().values()) {
			try {
				return domainApi.getUser(loginId);
			} catch (Exception e) {

			}
		}
		return null;
	}

	public List<SelectItem> getDomainNames() {
		Iterator<String> domainNames = getDomains().keySet().iterator();
		List<SelectItem> list = new LinkedList<>();
		while (domainNames.hasNext()) {
			list.add(new SelectItem(domainNames.next()));
		}
		return list;
	}


	public LinkedHashMap<String, DomainApi> getDomains() {
		return adminModule.getAdminTenantData().getDomains();
	}

	public void updateUserPassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException {
		try {
			DomainApi domainEntity = getDomains().get(dcemUser.getDomainEntity().getName());
			domainEntity.updateUserPassword(dcemUser, currentPassword, newPassword);
		} catch (Exception e) {
			throw e;
		}
	}

	public List<String> getSelectedTree(String ldapDomainName, String treeFilter, int i) {
		DomainApi domainApi = getDomains().get(ldapDomainName);
		if (domainApi.getDomainEntity().getDomainType() == DomainType.Azure_AD) {
			return null;
		}
		try {
			return domainApi.getSelectedLdapTree(treeFilter, PAGE_SIZE);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public DomainType getDomainType(String domainName) throws DcemException {
		if (domainName == null || domainName.isEmpty()) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, domainName);
		}
		DomainApi domainApi = getDomains().get(domainName.toLowerCase());
		if (domainApi == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DOMAIN_NAME, domainName);
		}
		return domainApi.getDomainEntity().getDomainType();
	}

	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter, int pageSize) throws DcemException {
		DomainApi domainApi = getDomainApi(dcemUser.getDomainEntity().getName());
		return domainApi.getUserGroupNames(dcemUser, filter, pageSize);
	}

	/**
	 * @param loginId
	 * @return
	 */
	public DcemUser searchUserInDomains(String loginId) {
		LinkedHashMap<String, DomainApi> domains = getDomains();
		for (Entry<String, DomainApi> entry : domains.entrySet()) {
			try {
				DcemUser dcemUser = entry.getValue().getUser(loginId);
				if (dcemUser != null) {
					return dcemUser;
				}
			} catch (DcemException e) {
			}
		}
		return null;
	}

	public Map<String, String> getUserAttributes(DcemUser dcemUser, List<String> attributeList) throws DcemException {
		DomainApi domainApi = getDomainApi(dcemUser.getDomainEntity().getName());
		return domainApi.getUserAttributes(dcemUser, attributeList);
	}

	public void resetUserPassword(DcemUser dcemUser, String newPassword) throws DcemException {
		DomainApi domainEntity = getDomains().get(dcemUser.getDomainEntity().getName());
		domainEntity.resetUserPassword(dcemUser, newPassword);
	}

	public void changeUserPhotoProfile(DcemUser dcemUser, byte[] photo, String password) throws DcemException {
		DomainApi domainEntity = getDomains().get(dcemUser.getDomainEntity().getName());
		domainEntity.changeUserPhotoProfile(dcemUser, photo, password);
		if (domainEntity.getDomainEntity().getDomainType() == DomainType.Active_Directory) {
			DomainAzure domainAzure = (DomainAzure) getDomainFromEmail(dcemUser.getUserPrincipalName(), DomainType.Azure_AD);
			if (domainAzure != null) {
				domainAzure.changeUserPhotoProfile(dcemUser, photo, password);
			}
		}

	}

}
