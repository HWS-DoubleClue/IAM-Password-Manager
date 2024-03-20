package com.doubleclue.dcem.core.logic;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.entities.DcemUser_;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
public class UserLogic {

	private static Logger logger = LogManager.getLogger(UserLogic.class);

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	EntityManager em;

	@Inject
	AdminModule adminModule;

	@Inject
	GroupLogic groupLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	RoleLogic roleLogic;

	@Inject
	UrlTokenLogic urlTokenLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	UserSubject userSubject;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	DepartmentLogic departmentLogic;

	AsModuleApi asModuleApi;

	@PostConstruct
	public void init() {
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
	}

	@DcemTransactional
	public DcemUser addOrUpdateUser(DcemUser user, DcemAction dcemAction, boolean withAudit, boolean numericPassword,
			int passwordLength, boolean savePassword) throws DcemException {
		if (StringUtils.isValidNameId(user.getLoginId()) == false) {
			throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, user.getLoginId());
		}
		if (user.getLanguage() == null) {
			user.setLanguage(adminModule.getPreferences().getUserDefaultLanguage());
		}
		String[] domainUser = getDomainUser(user.getLoginId());
		if (domainUser.length > 1) {
			DcemUser domainDcemUser = domainLogic.getUser(domainUser[0], domainUser[1]);
			user.setDomainEntity(domainDcemUser.getDomainEntity());
			user.setMobileNumber(domainDcemUser.getMobileNumber());
			user.setTelephoneNumber(domainDcemUser.getTelephoneNumber());
			user.setEmail(domainDcemUser.getEmail());
			user.setUserDn(domainDcemUser.getUserDn());
			user.setDisplayName(domainDcemUser.getDisplayName());
		}
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			licenceLogic.checkForLicence(null, true);
			// New Template Set all other previos Templates inactive
			user.setId(null);
			user.setFailActivations(0);
			user.setAcSuspendedTill(null);
			String initialPassword = user.getInitialPassword();
			if (initialPassword == null || initialPassword.isEmpty()) {
				if (numericPassword) {
					initialPassword = RandomUtils.generateRandomNumberString(passwordLength);
				} else {
					initialPassword = RandomUtils.generateRandomAlphaLowercaseNumericString(passwordLength);
				}
				user.setInitialPassword(initialPassword);
			}
			if (adminModule.getPreferences().isSaveUserPasswords() == true || savePassword == true) {
				user.setSaveit(user.getInitialPassword());
			} else {
				user.setSaveit(null);
			}
			try {
				user.setSalt(RandomUtils.getRandom(8));
				user.setHashPassword(KaraUtils.getSha1WithSalt(user.getSalt(),
						user.getInitialPassword().getBytes(DcemConstants.CHARSET_UTF8)));
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.EXCEPTION, "sha1 failed", e);
			}
			if (user.getEmail() == null) {
				user.setEmail("dummy@dummy.de");
			}
			if (user.getDcemRole() == null) {
				DcemRole dcemRole = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER);
				user.setDcemRole(dcemRole);
			}
			em.persist(user);
		} else { // EDITING
			if (withAudit) {
				try {
					DcemUser preUser = getUser(user.getId());
					modifiedUser(preUser, user, dcemAction);
				} catch (Exception exp) {
					logger.warn("Couldn't compare operator", exp);
				}
			}
			String initialPin = user.getInitialPassword();
			if (initialPin != null && initialPin.isEmpty() == false) {
				try {
					user.setSalt(RandomUtils.getRandom(8));
					user.setHashPassword(KaraUtils.getSha1WithSalt(user.getSalt(),
							user.getInitialPassword().getBytes(DcemConstants.CHARSET_UTF8)));
				} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
					throw new DcemException(DcemErrorCodes.EXCEPTION, "sha1 failed", e);
				}

				if (adminModule.getPreferences().isSaveUserPasswords() == true || user.getSaveit() != null) {
					user.setSaveit(initialPin);
				} else {
					user.setSaveit(null);
				}
			}
			user = em.merge(user);
		}
		if (withAudit) {
			auditingLogic.addAudit(dcemAction, user.toString());
		}
		return user;
	}

	String[] getDomainUser(String loginId) {
		return loginId.split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
	}

	public List<String> getCompleteUsersDisplayName(String name, int max) throws Exception {
		TypedQuery<String> query = em.createNamedQuery(DcemUser.GET_USERS_DISPLAYNAME, String.class);
		query.setParameter(1, "%" + name.toLowerCase() + "%");
		query.setMaxResults(max);
		return query.getResultList();
	}

	public DcemUser getUserByDisplayName(String name) throws Exception {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_BY_DISPLAYNAME, DcemUser.class);
		query.setParameter(1, name.toLowerCase());
		query.setMaxResults(1);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	/**
	 * @param userLoginId
	 * @param password
	 * @return
	 * @throws DcemException
	 */
	public DcemUser verifyUser(String userLoginId, byte[] password) throws DcemException {
		DcemUser dcemUser = null;
		if (userLoginId.indexOf(DcemConstants.DOMAIN_SEPERATOR_REGEX) != -1) {
			String[] domainUser = userLoginId.split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
			if (domainUser.length > 1) {
				domainLogic.getDomainApi(domainUser[0]);
			} else {
				dcemUser = domainLogic.getUserFromDomains(userLoginId);
				if (dcemUser == null) {
					throw new DcemException(DcemErrorCodes.INVALID_USERID, domainUser[0]);
				}
			}

			try {
				domainLogic.verifyDomainLogin(dcemUser, password);
			} catch (DcemException e) {
				if (e.getErrorCode() == DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION) {
					throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, dcemUser.getLoginId());
				} else {
					throw e;
				}
			}
		} else {
			dcemUser = new DcemUser(userLoginId);
		}
		dcemUser.setDcemRole(roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER));
		return dcemUser;
	}

	public DcemUser getUser(String domainName, String loginId) throws DcemException {
		if (domainName == null || domainName.isEmpty()) {
			return getUser(loginId);
		} else {
			return getDistinctUser(domainName + DcemConstants.DOMAIN_SEPERATOR + loginId);
		}
	}

	public DcemUser getUser(String loginId) throws DcemException {
		if (loginId == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, "null");
		}
		if (loginId.startsWith(DcemConstants.LOCAL_DOMAIN)) {
			loginId = loginId.substring(DcemConstants.LOCAL_DOMAIN.length());
			return getDistinctUser(loginId);
		}

		DomainApi domainApi = domainLogic.getDomainFromEmail(loginId, null);
		if (domainApi != null) {
			return getDistinctUserDomain(loginId, domainApi.getDomainEntity());
		}

		int ind = loginId.indexOf(DcemConstants.DOMAIN_SEPERATOR);
		String loginIdFilter = null;
		if (domainLogic.getDomains().isEmpty() == false
				&& adminModule.getPreferences().isEnableUserDomainSearch() == true) {
			switch (ind) {
			case -1:
				loginIdFilter = "%" + DbFactoryProducer.getDbType().getDbBackslash() + loginId;
				break;
			case 0:
				loginId = loginId.substring(1);
				break;
			default:
				break;
			}
		}
		if (loginIdFilter != null) {
			TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_LOGIN_SEARCH, DcemUser.class);
			query.setParameter(1, loginId.toLowerCase());
			query.setParameter(2, loginIdFilter.toLowerCase());
			List<DcemUser> list = query.getResultList();
			switch (list.size()) {
			case 0:
				return null;
			case 1:
				return list.get(0);
			default:
				throw new DcemException(DcemErrorCodes.NO_DISTINCT_USER_NAME, loginId);
			}
		} else {
			return getDistinctUser(loginId);
		}
	}

	public DcemUser getDistinctUser(String loginId) throws DcemException {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_LOGIN, DcemUser.class);
		query.setParameter(1, loginId.toLowerCase());
		// query.setParameter(2, loginId.toLowerCase());
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	private DcemUser getDistinctUserDomain(String loginId, DomainEntity domainEntity) throws DcemException {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_LOGIN_DOMAIN, DcemUser.class);
		query.setParameter(1, domainEntity);
		query.setParameter(2, loginId.toLowerCase());
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public List<DcemUser> getUsers(List<Integer> loginIds) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USERS, DcemUser.class);
		query.setParameter(1, loginIds);
		return query.getResultList();
	}

	@DcemTransactional
	public void addOrUpdateUserWoAuditing(DcemUser user) throws DcemException {
		String initialPassword = user.getInitialPassword();
		byte[] salt = null;
		byte[] hashPassword = null;
		if (user.getLanguage() == null) {
			user.setLanguage(adminModule.getPreferences().getUserDefaultLanguage());
		}

		if (initialPassword != null && !initialPassword.isEmpty()) {
			try {
				salt = RandomUtils.getRandom(8);
				hashPassword = (KaraUtils.getSha1WithSalt(salt,
						user.getInitialPassword().getBytes(DcemConstants.CHARSET_UTF8)));
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.EXCEPTION, "sha1 failed", e);
			}
		}
		if (user.getDcemRole() == null) {
			DcemRole dcemRole = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER);
			user.setDcemRole(dcemRole);
		}
		if (user.getId() == null) {
			licenceLogic.checkForLicence(null, true);
			if (salt != null && user.isDomainUser() == false) {
				user.setSalt(salt);
				user.setHashPassword(hashPassword);
			}
			em.persist(user);
			logger.info("User added: " + user.getLoginId());
		} else {
			user = em.merge(user);
			if (user.isDomainUser() == false && salt != null) {
				user.setSalt(salt);
				user.setHashPassword(hashPassword);
			}
		}
	}

	@DcemTransactional
	public void updateExtention(DcemUser dcemUser, byte[] photo, DcemLdapAttributes dcemLdapAttributes)
			throws DcemException {

		DcemUserExtension dcemUserExtension = em.find(DcemUserExtension.class, dcemUser.getId());
		if (dcemUserExtension == null) {
			dcemUserExtension = new DcemUserExtension();
			dcemUserExtension.setId(dcemUser.getId());
			dcemUserExtension.setPhoto(photo);
			em.persist(dcemUserExtension);
		} else {
			dcemUserExtension.setPhoto(photo);
		}
		updateUserExtensionAttributes(dcemUserExtension, dcemLdapAttributes);
		if (dcemUser.getDcemUserExt() == null) {
			dcemUser.setDcemUserExt(dcemUserExtension);
			em.merge(dcemUser);
		}
		return;
	}

	private void updateUserExtensionAttributes(DcemUserExtension dcemUserExtension,
			DcemLdapAttributes dcemLdapAttributes) throws DcemException {
		if (dcemLdapAttributes.country != null) {
			dcemUserExtension.setCountry(dcemLdapAttributes.country);
		}
		if (dcemLdapAttributes.jobTitle != null) {
			dcemUserExtension.setJobTitle(dcemLdapAttributes.jobTitle);
		}
		if (dcemLdapAttributes.department != null) {
			DcemUser headOf = null;
			if (dcemLdapAttributes.getManagerId() != null) {
				headOf = getUserByDn(dcemLdapAttributes.getManagerId());
			}
			if (headOf == null) {
				headOf = domainLogic.getUserFromDomains(dcemLdapAttributes.getManagerId());
				if (headOf != null) {
					DcemUser dcemHeadOf = getUserByUpn(headOf.getUserPrincipalName());
					if (dcemHeadOf == null) {
						addOrUpdateUserWoAuditing(headOf);
					} else {
						headOf = dcemHeadOf;
					}

				}
			}
			DepartmentEntity departmentEntity = departmentLogic.getDepartmentByName(dcemLdapAttributes.department);
			if (departmentEntity == null) {
				departmentEntity = new DepartmentEntity();
				departmentEntity.setName(dcemLdapAttributes.department);
				if (headOf != null) {
					departmentEntity.setHeadOf(headOf);
				}
				em.persist(departmentEntity);
				dcemUserExtension.setDepartment(departmentEntity);
			} else {
				dcemUserExtension.setDepartment(departmentEntity);
				if (headOf != null) {
					departmentEntity.setHeadOf(headOf);
				}
			}

		}
	}

	private DcemUser getUserByDn(String dn) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_BY_DN, DcemUser.class);
		query.setParameter(1, dn);
		List<DcemUser> list = query.getResultList();
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	private DcemUser getUserByUpn(String upn) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_BY_UPN, DcemUser.class);
		query.setParameter(1, upn);
		List<DcemUser> list = query.getResultList();
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	public DcemUser getUser(int id) {
		DcemUser user = em.find(DcemUser.class, id);
		return user;
	}

	public List<String> getCompleteUserList(String name, int max) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_FILTER_LIST, DcemUser.class);
		query.setParameter(1, "%" + name + "%");
		query.setMaxResults(max);

		try {
			List<DcemUser> list = query.getResultList();
			List<String> nameList = new ArrayList<>(list.size());
			for (DcemUser user : list) {
				nameList.add(user.getLoginId());
			}
			Collections.sort(nameList);
			return nameList;
		} catch (Throwable exp) {
			logger.warn("Couldn't retrieve products.", exp);
			return null;
		}
	}

	public List<String> getCompleteUserListByDomain(String name, int max, DomainEntity domain) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_FILTERED_USERS_BY_DOMAIN, DcemUser.class);
		query.setParameter(1, domain);
		query.setParameter(2, name.toLowerCase() + "%");
		query.setMaxResults(max);

		try {
			List<DcemUser> list = query.getResultList();
			List<String> nameList = new ArrayList<>(list.size());
			for (DcemUser user : list) {
				nameList.add(user.getLoginId());
			}
			return nameList;
		} catch (Throwable exp) {
			logger.warn("Couldn't retrieve users.", exp);
			return null;
		}
	}

	/**
	 * @param user
	 * @return
	 */
	public int isUserEnabled(DcemUser user) {
		if (user.isDisabled()) {
			return 1;
		}
		LocalDateTime now = LocalDateTime.now();
		if (user.getAcSuspendedTill() == null) {
			return 0;
		}
		if (now.compareTo(user.getAcSuspendedTill()) > 0) {
			user.setAcSuspendedTill(null);
			user.setPassCounter(0);
			return 0;
		} else {
			return 2;
		}
	}

	@DcemTransactional
	public int incFailActivations(DcemUser user, int maxFailedActivations, int retryActivationDelayinMinutes) {
		int fails = user.getFailActivations();
		if (fails >= maxFailedActivations) {
			int tempDisabled = retryActivationDelayinMinutes;
			user.setAcSuspendedTill(LocalDateTime.now().plusMinutes(retryActivationDelayinMinutes));
			return tempDisabled;
		} else {
			user.setFailActivations(fails + 1);
		}
		return 0;
	}

	@DcemTransactional
	public void activationOk(DcemUser user) throws DcemException {
		user.setInitialPassword(null);
		user.setFailActivations(0);
	}

	@DcemTransactional
	public void enableUser(DcemUser user, DcemAction action) throws DcemException {
		licenceLogic.checkForLicence(null, false);
		enableUserWoAuditing(user);
		auditingLogic.addAudit(action, user.toString());
	}

	@DcemTransactional
	public void disableUser(DcemUser user, DcemAction action) throws DcemException {
		user = em.merge(user);
		user.setDisabled(true);
		asModuleApi.killUserDevices(user);
		auditingLogic.addAudit(action, user.toString());
	}

	@DcemTransactional
	public void enableUserWoAuditing(DcemUser user) throws DcemException {
		try {
			user = em.merge(user);
		} catch (OptimisticLockException e) {
			user = getUser(user.getId());
		}
		user.setDisabled(false);
		user.setFailActivations(0);
		user.setAcSuspendedTill(null);
		user.setPassCounter(0);
		if (asModuleApi != null) {
			asModuleApi.resetStayLogin(user);
		}
	}

	private void modifiedUser(DcemUser preUser, DcemUser newUser, DcemAction dcemAction) {
		if (asModuleApi != null) {
			asModuleApi.modifiedUser(preUser, newUser);
			String changeInfo = "";
			try {
				changeInfo = DcemUtils.compareObjects(preUser, newUser);
			} catch (DcemException e) {
				logger.warn("Couldn't compare operator", e);
			}
			auditingLogic.addAudit(dcemAction, changeInfo);
		}
	}

	public void verifyUserPassword(DcemUser dcemUser, byte[] password) throws DcemException {
		if (dcemUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, null);
		}
		if (dcemUser.isDisabled()) {
			throw new DcemException(DcemErrorCodes.USER_DISABLED, dcemUser.getLoginId());
		}
		if (isUserEnabled(dcemUser) == 2) { // user is suspended
			throw new DcemException(DcemErrorCodes.USER_PASSWORD_MAX_RETRIES, dcemUser.getLoginId());
		}

		if (password != null) {
			if (dcemUser.isDomainUser()) {
				try {
					domainLogic.verifyDomainLogin(dcemUser, password);
				} catch (DcemException e) {
					if (e.getErrorCode() == DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION) {
						throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, dcemUser.getLoginId());
					} else {
						throw e;
					}
				}
			} else {
				byte[] passwordHash;
				int rc = dcemUser.getPassCounter();
				if (rc >= adminModule.getPreferences().getPasswordMaxRetryCounter()) {
					if (dcemUser.getAcSuspendedTill() == null) {
						suspendUser(dcemUser);
					}
					throw new DcemException(DcemErrorCodes.USER_PASSWORD_MAX_RETRIES, dcemUser.getLoginId());
				}
				try {
					passwordHash = KaraUtils.getSha1WithSalt(dcemUser.getSalt(), password);
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, dcemUser.getLoginId());
				}
				if (Arrays.equals(passwordHash, dcemUser.getHashPassword()) == false) {
					throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, dcemUser.getLoginId());
				}
			}
			// if no exception was fired, the password has been validated
		}
	}

	@DcemTransactional
	public void suspendUser(DcemUser dcemUser) {
		if (dcemUser != null) {
			int suspendedDuration = adminModule.getPreferences().getSuspendUserDuration();
			dcemUser.setAcSuspendedTill(LocalDateTime.now().plusMinutes(suspendedDuration));
			asModuleApi.killUserDevices(dcemUser);
		}
	}

	@DcemTransactional
	public void addPasswordCounter(DcemUser dcemUser) throws DcemException {
		if (dcemUser == null) {
			return;
		}
		int rc = dcemUser.getPassCounter();
		rc++;
		dcemUser.setPassCounter(rc);
	}

	@DcemTransactional
	public void resetPasswordCounter(DcemUser dcemUser) {
		dcemUser.setPassCounter(0);
	}

	@DcemTransactional
	public void setPasswordCounter(int userId, int count) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DcemUser> updateCriteria = cb.createCriteriaUpdate(DcemUser.class);
		Root<DcemUser> root = updateCriteria.from(DcemUser.class);
		updateCriteria.set(root.get(DcemUser_.passCounter), count);
		updateCriteria.where(cb.equal(root.get("id"), userId));
		em.createQuery(updateCriteria).executeUpdate();
	}

	@DcemTransactional
	public void setUserLanguage(DcemUser user, SupportedLanguage language) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DcemUser> updateCriteria = cb.createCriteriaUpdate(DcemUser.class);
		Root<DcemUser> root = updateCriteria.from(DcemUser.class);
		updateCriteria.set(root.get(DcemUser_.language.getName()), language);
		updateCriteria.where(cb.equal(root.get("id"), user.getId()));
		em.createQuery(updateCriteria).executeUpdate();
	}

	@DcemTransactional
	public void setUserLogin(DcemUser user) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DcemUser> updateCriteria = cb.createCriteriaUpdate(DcemUser.class);
		Root<DcemUser> root = updateCriteria.from(DcemUser.class);
		updateCriteria.set(root.get(DcemUser_.lastLogin.getName()), LocalDateTime.now());
		updateCriteria.set(root.get(DcemUser_.passCounter.getName()), 0);
		updateCriteria.where(cb.equal(root.get("id"), user.getId()));
		em.createQuery(updateCriteria).executeUpdate();
	}

	// THIS IS VERY DANGEROUS TO USE WITH OUT BATCHES
	// public List<DcemUser> getUsersByLdap(LdapEntity ldap) {
	// TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USERS_BY_LDAP,
	// DcemUser.class);
	// query.setParameter(1, ldap);
	// return query.getResultList();
	// }

	@DcemTransactional
	public void deleteUsers(List<Object> list, DcemAction dcemAction) throws DcemException {
		DcemUser dcemUser;
		StringBuffer sb = new StringBuffer();
		for (Object object : list) {
			dcemUser = (DcemUser) object;
			if (dcemUser.getId() == operatorSessionBean.getDcemUser().getId()) {
				throw new DcemException(DcemErrorCodes.CANNOT_DELETE_YOURSELF, null);
			}
			dcemUser = em.merge(dcemUser);
			for (DcemModule module : applicationBean.getSortedModules()) {
				module.deleteUserFromDbPre(dcemUser);
			}
			for (DcemModule module : applicationBean.getSortedModules()) {
				module.deleteUserFromDb(dcemUser);
			}
			String objectIdentifier = Integer.toString(dcemUser.getId());
			urlTokenLogic.deleteUserUrlTokens(objectIdentifier);
			auditingLogic.deleteAllAuditsForUser(dcemUser);
			groupLogic.removeMemberFromAllGroups(dcemUser);
			DcemUserExtension dcemUserExtension = dcemUser.getDcemUserExt();
			if (dcemUserExtension != null) {
				em.remove(dcemUserExtension);
			}
			em.remove(dcemUser);
			deleteUserExtension(dcemUser.getId());
			sb.append(dcemUser.getLoginId());
			sb.append(" ");
		}
		auditingLogic.addAudit(dcemAction, sb.toString());
	}

	@DcemTransactional
	public void setUserSalt(DcemUser dcemUser) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DcemUser> updateCriteria = cb.createCriteriaUpdate(DcemUser.class);
		Root<DcemUser> root = updateCriteria.from(DcemUser.class);
		updateCriteria.set(root.get(DcemUser_.salt), dcemUser.getSalt());
		updateCriteria.where(cb.equal(root.get("id"), dcemUser.getId()));
		em.createQuery(updateCriteria).executeUpdate();
	}

	@DcemTransactional
	public int replaceUsersLdapName(DomainEntity ldap, String previousName, String newName) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USERS_LDAP_NAME, DcemUser.class);
		query.setParameter(1, previousName + DcemConstants.DOMAIN_SEPERATOR + "%");
		query.setParameter(2, ldap);
		query.setMaxResults(1000);
		List<DcemUser> list = query.getResultList();
		int index = previousName.length();
		for (DcemUser user : list) {
			user.setLoginId(newName + user.getLoginId().substring(index));
		}
		logger.debug("Users renamed: " + list.size());
		return list.size();
	}

	@DcemTransactional
	public void setMailPasswordMobile(DcemUser dcemUser, String email, String newPassword, String mobileNumber)
			throws DcemException {
		if (email != null) {
			dcemUser.setEmail(email);
		}
		if (mobileNumber != null) {
			dcemUser.setMobileNumber(mobileNumber);
		}
		if (newPassword != null) {
			setPassword(dcemUser, newPassword);
		}
		return;
	}

	@DcemTransactional
	public void setPassword(DcemUser dcemUser, String newPassword) throws DcemException {
		if (dcemUser == null || dcemUser.getLoginId() == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, null);
		}
		if (em.contains(dcemUser) == false) {
			dcemUser = em.find(DcemUser.class, dcemUser.getId());
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, null);
			}
		}

		try {
			if (dcemUser.isDomainUser()) {
				domainLogic.resetUserPassword(dcemUser, newPassword);// still TODO
			} else {
				dcemUser.setSalt(RandomUtils.getRandom(8));
				dcemUser.setHashPassword(KaraUtils.getSha1WithSalt(dcemUser.getSalt(),
						newPassword.getBytes(DcemConstants.CHARSET_UTF8)));
			}
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.EXCEPTION, "sha1 failed", e);
		}
	}

	public List<DcemUser> getAdminOperators() {
		DcemRole superAdminRole = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN);
		DcemRole adminRole = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_ADMIN);
		return getUsersFromRoles(Arrays.asList(superAdminRole, adminRole));
	}

	public List<DcemUser> getUsersFromRoles(List<DcemRole> roles) {

		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USERS_ROLE, DcemUser.class);
		query.setParameter(1, roles);
		return query.getResultList();
	}

	@DcemTransactional
	public void changePassword(String userLoginId, String currentPassword, String newPassword) throws DcemException {
		DcemUser dcemUser = getUser(userLoginId);
		if (dcemUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, "User Login ID: " + userLoginId);
		}
		changePassword(dcemUser, currentPassword, newPassword);
	}
	

	@DcemTransactional
	public void changePassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException {
		if (dcemUser.isDomainUser()) {
			domainLogic.updateUserPassword(dcemUser, currentPassword, newPassword);
		} else {
			verifyUserPassword(dcemUser, currentPassword.getBytes(DcemConstants.UTF_8));
			if (newPassword == null || newPassword.length() < 4) {
				throw new DcemException(DcemErrorCodes.INVALID_PASSWORD, dcemUser.getLoginId());
			}
			setPassword(dcemUser, newPassword);
		}
	}

	@DcemTransactional
	public void createUserPasswordHash(DcemUser dcemUser) {
		dcemUser.setHashPassword(RandomUtils.getRandom(16));
		return;
	}

	@DcemTransactional
	public String registerUser(DcemUser dcemUser, String url, int timeout) throws DcemException {
		addOrUpdateUser(dcemUser, new DcemAction(userSubject, DcemConstants.ACTION_ADD), true, false,
				adminModule.getPreferences().getUserPasswordLength(), false);
		UrlTokenEntity urlTokenEntity = urlTokenLogic.addUrlTokenToDb(UrlTokenType.VerifyEmail, timeout, null,
				dcemUser.getId().toString());
		urlTokenLogic.sendUrlTokenByEmail(dcemUser, url, urlTokenEntity);
		String recoveryKey = null;
		if (asModuleApi != null) {
			recoveryKey = RandomUtils.generateRandomAlphaNumericString(20);
			asModuleApi.setUserCloudSafe(DcemConstants.RECOVERY_KEY, CloudSafeOptions.ENC.name(), null, dcemUser, false,
					null, recoveryKey.getBytes(DcemConstants.UTF_8));
		}
		return recoveryKey;
	}

	public String getUserGroupNamesAsString(DcemUser dcemUser, String filter) throws DcemException {
		HashSet<String> userGroupNames = getUserGroupNames(dcemUser, filter);
		StringBuilder sb = new StringBuilder();
		for (String name : userGroupNames) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(name);
		}
		return sb.toString();
	}

	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter) throws DcemException {
		HashSet<String> userGroupNames;
		List<String> groupNames = groupLogic.getUserGroupNames(dcemUser.getId());
		if (dcemUser.isDomainUser()) {
			userGroupNames = domainLogic.getUserGroupNames(dcemUser, filter, 500);
			userGroupNames.addAll(groupNames);
		} else {
			userGroupNames = new HashSet<>();
			userGroupNames.addAll(groupNames);
		}
		return userGroupNames;
	}

	public DcemUser getLdapIdOfUser(String userLogin) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_USER_LDAP, DcemUser.class);
		query.setParameter(1, userLogin);
		return query.getSingleResult();
	}

	public void resetStayLogin(DcemUser dcemUser, DcemAction dcemAction) throws DcemException {
		if (asModuleApi != null) {
			asModuleApi.resetStayLogin(dcemUser);
		}
	}

	public int getTotalUserCount() {
		TypedQuery<Long> query = em.createNamedQuery(DcemUser.GET_TOTAL_USER_COUNT, Long.class);
		return query.getSingleResult().intValue();
	}

	public int deleteUserExtension(Integer id) {
		Query query = em.createNamedQuery(DcemUserExtension.DELETE_USER_EXTENSION);
		query.setParameter(1, id);
		return query.executeUpdate();
	}

	public DcemUserExtension getDcemUserExtension(DcemUser dcemUser) {
		return em.find(DcemUserExtension.class, dcemUser.getId());
	}

	public DcemUser getSuperAdmin() {
		return adminModule.getAdminTenantData().getSuperAdmin();
	}

	@DcemTransactional
	public void updateDcemUserExtension(DcemUser dcemUser, DcemUserExtension dcemUserExtension) {
		DcemUserExtension dcemUserExtensionDb = em.find(DcemUserExtension.class, dcemUser.getId());
		if (dcemUserExtensionDb == null) {
			dcemUserExtension.setId(dcemUser.getId());
			em.persist(dcemUserExtension);
			dcemUser.setDcemUserExt(dcemUserExtension);
			em.merge(dcemUser);
		} else {
			if (dcemUserExtension.getPhoto() != null) {
				dcemUserExtensionDb.setPhoto(dcemUserExtension.getPhoto());
			}
			dcemUserExtensionDb.setCountry(dcemUserExtension.getCountry());
			dcemUserExtensionDb.setTimezone(dcemUserExtension.getTimezone());
			dcemUserExtensionDb.setDepartment(dcemUserExtension.getDepartment());
			dcemUserExtensionDb.setJobTitle(dcemUserExtension.getJobTitle());
			if (dcemUser.getDcemUserExt() == null) {
				dcemUser = em.find(DcemUser.class, dcemUser.getId());
				dcemUser.setDcemUserExt(dcemUserExtensionDb);
			}
		}
	}

	public List<DcemUser> getAllDomainUsers(DomainEntity azureDomainEntity) {
		TypedQuery<DcemUser> query = em.createNamedQuery(DcemUser.GET_DOMAIN_USERS, DcemUser.class);
		query.setParameter(1, azureDomainEntity);
		return query.getResultList();
	}
	
	public TimeZone getTimeZone (DcemUser dcemUser) {
		TimeZone timeZone;
		DcemUserExtension dcemUserExt = getDcemUserExtension(dcemUser);
		if (dcemUserExt != null && dcemUserExt.getTimezone() != null) {
			timeZone = dcemUserExt.getTimezone();
		} else {
			timeZone = adminModule.getTimezone();
		}
		return timeZone;
	}
	
	public LocalDateTime getUserZonedTime (LocalDateTime localDateTime, DcemUser dcemUser) {
		TimeZone timeZone = getTimeZone(dcemUser);
		if (TimeZone.getDefault().equals(timeZone)) {
			return localDateTime;
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, TimeZone.getDefault().toZoneId());
		return zonedDateTime.withZoneSameInstant(timeZone.toZoneId()).toLocalDateTime();
	}
	
	public LocalDateTime getDefaultZonedTime(LocalDateTime localDateTime, DcemUser dcemUser) {
		TimeZone timeZone = getTimeZone(dcemUser);
		if (TimeZone.getDefault().equals(timeZone)) {
			return localDateTime;
		}
		ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, timeZone.toZoneId());
		return zonedDateTime.withZoneSameInstant(TimeZone.getDefault().toZoneId()).toLocalDateTime();
	}
	
	@DcemTransactional
	public void updateUserProfile(DcemUser clonedUser, DcemUserExtension dcemUserExtension) throws Exception {
		DcemUser dcemUser = getUser(clonedUser.getId());
		String changeInfo;
		try {
			changeInfo = DcemUtils.compareObjects(clonedUser, dcemUser);
		} catch (DcemException e) {
			changeInfo = e.toString();
		}
		dcemUser.setLoginId(clonedUser.getLoginId());
		dcemUser.setDisplayName(clonedUser.getDisplayName());
		dcemUser.setEmail(clonedUser.getEmail());
		dcemUser.setPrivateEmail(clonedUser.getPrivateEmail());
		dcemUser.setTelephoneNumber(clonedUser.getTelephoneNumber());
		dcemUser.setMobileNumber(clonedUser.getMobile());
		dcemUser.setPrivateMobileNumber(clonedUser.getPrivateMobileNumber());
		dcemUser.setLanguage(clonedUser.getLanguage());
		DcemUserExtension dcemUserExtensionDb = em.find(DcemUserExtension.class, dcemUser.getId());
		if (dcemUserExtensionDb == null) {
			dcemUserExtension.setId(dcemUser.getId());
			em.persist(dcemUserExtension);
			dcemUser.setDcemUserExt(dcemUserExtension);
		} else {
			if (dcemUserExtension.getPhoto() != null) {
				dcemUserExtensionDb.setPhoto(dcemUserExtension.getPhoto());
			}
			dcemUserExtensionDb.setCountry(dcemUserExtension.getCountry());
			dcemUserExtensionDb.setTimezone(dcemUserExtension.getTimezone());
		}
		// userLogic.updateDcemUserExtension(dcemUser, dcemUserExtension);
		if (changeInfo != null || changeInfo.isEmpty() == false) {
			auditingLogic.addAudit(new DcemAction(userSubject, DcemConstants.ACTION_EDIT), dcemUser, changeInfo);
		}
	}

}
