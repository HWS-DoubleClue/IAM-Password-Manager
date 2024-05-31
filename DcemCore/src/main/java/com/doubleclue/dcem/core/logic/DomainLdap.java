package com.doubleclue.dcem.core.logic;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NoPermissionException;
import javax.naming.PartialResultException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InvalidSearchFilterException;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.sun.mail.imap.protocol.Item;

public class DomainLdap implements DomainApi {

	private static final Logger logger = LogManager.getLogger(DomainLdap.class);
	private static final String PROPERTY_DISTINGUISHED_NAME = "distinguishedName";
	private static final String AD_USER_ACCOUNT_CONTROL = "UserAccountControl";
	private static final String AD_USER_PRINCIAL_NAME = "UserPrincipalName";
	private static final String AD_USER_OBJECT_GUID = "objectGUID";
	private static final String AD_USER_OBJECT_SID = "objectsid";
	private static final String AD_USER_THUMBNAIL_PHOTO = "thumbnailPhoto";

	private static final String AD_USER_BINARY_OBJECTS = AD_USER_OBJECT_GUID + " " + AD_USER_OBJECT_SID;
	private static final String MEMBER = "member";
	private static final String GROUP_NAME = "name";
	private static final String CN = "cn";

	private static final String AD_ERROR_DATA = ", data ";
	private static final String AD_ERROR_USER_DISABLED = "533,";
	private static final String AD_ERROR_USER_MUST_RESET_PASSWORD = "773,";
	private static final String AD_ERROR_USER_ACCOUNT_LOCKED = "775,";
	private static final String AD_ERROR_USER_PASSWORD_EXPIRED = "532,";
	// private static final String AD_ERROR_INVALID_CREDENTIALS = "52e,";
	private static Hashtable<String, Object> ldapEnvironment;

	private String[] defaultUserReturnedAtts;
	private DomainEntity domainEntity;
	private LdapContext searchLdapContext;
	private boolean hasUserAccountControl;

	static {
		// String timeout = Integer.toString(ldapEntity.getTimeoutInSec() * 1000);
		System.setProperty("com.sun.jndi.ldap.connect.pool.maxsize", "20");
		System.setProperty("com.sun.jndi.ldap.connect.pool.timeout", "60000"); // 10 Minutes
		System.setProperty("com.sun.jndi.ldap.connect.pool.protocol", "plain ssl");
		System.setProperty("com.sun.jndi.ldap.connect.pool.initsize", "4");
		// System.setProperty("com.sun.jndi.ldap.connect.pool.debug", "all");

		ldapEnvironment = new Hashtable<String, Object>();
		ldapEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
		ldapEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		ldapEnvironment.put("com.sun.jndi.ldap.read.timeout", "30000");
		ldapEnvironment.put("com.sun.jndi.ldap.connect.timeout", "30000");
		ldapEnvironment.put("java.naming.ldap.attributes.binary", AD_USER_BINARY_OBJECTS);
		ldapEnvironment.put("java.naming.ldap.factory.socket", DcemSocketFactory.class.getName());
		ldapEnvironment.put("com.sun.jndi.ldap.connect.pool", "true");
	}

	public DomainLdap(DomainEntity domainEntity) {
		this.domainEntity = domainEntity;
		defaultUserReturnedAtts = new String[] { DomainLdap.PROPERTY_DISTINGUISHED_NAME, domainEntity.getMailAttribute(), domainEntity.getFirstNameAttribute(),
				domainEntity.getLastNameAttribute(), domainEntity.getTelephoneAttribute(), domainEntity.getMobileAttribute(), domainEntity.getLoginAttribute(),
				DcemConstants.LDAP_DISPLAY_ATTRIBUTE, AD_USER_PRINCIAL_NAME, AD_USER_OBJECT_GUID, DcemConstants.LDAP_PREFERRED_LANGUAGE };
	}

	@Override
	public void testConnection() throws DcemException {
		searchLdapContext = getSearchAccount();
		String searchDn = domainEntity.getSearchAccount();
		if (searchDn.indexOf('@') != -1) {
			String filter = "(&(" + AD_USER_PRINCIAL_NAME + "=" + domainEntity.getSearchAccount() + ")(objectClass=person))";
			Map<String, Attributes> attrMap = getSearchTry(null, filter, null, new String[] { PROPERTY_DISTINGUISHED_NAME }, 10);
			if (attrMap.values().iterator().hasNext()) {
				Attributes attributes = attrMap.values().iterator().next();
				try {
					searchDn = (String) attributes.get(PROPERTY_DISTINGUISHED_NAME).get();
				} catch (NamingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//
		Attributes attributes = getAttributes(searchDn, new String[] { AD_USER_ACCOUNT_CONTROL });
		if (attributes != null && attributes.size() > 0) {
			hasUserAccountControl = true;
		}
		return;
	}

	@Override
	public void close() {
		try {
			if (searchLdapContext != null) {
				searchLdapContext.close();
				searchLdapContext = null;
			}
		} catch (NamingException e) {
			logger.debug(e);
		}
		searchLdapContext = null;
	}

	public void testLdapConnection(DomainEntity ldapEntity) throws DcemException {
		getSearchAccount();
	}

	/**
	 * Returns {@link Item} with addition information in case of user exists and
	 * authenicated correctly, otherwise null
	 * 
	 * @param userAccount username
	 * @param password    password
	 * @return Item or null
	 * @throws Exception
	 * @throws NamingException
	 */
	@Override
	public DcemUser verifyLogin(DcemUser dcemUser, byte[] password) throws DcemException {
		DcemLdapAttributes dcemLdapAttributes = null;
		String dn = dcemUser.getUserDn();
		String userAccountName = dcemUser.getAccountName();
		if (dn == null || dcemUser.getImmutableId() == null) {
			Map<String, Attributes> map = getUsersAttributeMap(null, null, null, userAccountName, defaultUserReturnedAtts, DomainLogic.PAGE_SIZE);
			if (map.size() != 1) {
				throw new DcemException(DcemErrorCodes.LDAP_SEARCH_USER_FAILED, "User not found: " + dcemUser.getLoginId());
			}
			dcemLdapAttributes = getDcemLdapAttributes(map.values().iterator().next());
			dn = dcemLdapAttributes.getDn();
			dcemUser.ldapSync(dcemLdapAttributes);
			dcemUser.setDomainEntity(domainEntity);
		}
		try {
			login(dn, password, true);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION) {
				Map<String, Attributes> map = getUsersAttributeMap(null, null, null, userAccountName, defaultUserReturnedAtts, DomainLogic.PAGE_SIZE);
				if (map.size() != 1) {
					throw new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, "User not found: " + userAccountName, exp);
				}
				dcemLdapAttributes = getDcemLdapAttributes(map.values().iterator().next());
				if (dn.equals(dcemLdapAttributes.dn) == true) {
					throw exp;
				}
				login(dcemLdapAttributes.dn, password, true);
				dn = dcemLdapAttributes.dn;
			} else {
				throw exp;
			}
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, "generic exception for user: " + userAccountName, exp);
		}
		logger.debug("Login Successful");
		Map<String, Attributes> map = getUsersAttributeMap(null, null, dn, userAccountName, defaultUserReturnedAtts, DomainLogic.PAGE_SIZE);
		Attribute attribute = null;
		if (map.isEmpty() == false) {
			dcemLdapAttributes = getDcemLdapAttributes(map.values().iterator().next());
			attribute = map.values().iterator().next().get(domainEntity.getLoginAttribute());
		}

		String userLoginId = null;
		if (attribute != null) {
			try {
				userLoginId = ((String) attribute.get());
			} catch (NamingException e) {
			}
		}
		DcemUser dcemUser2 = null;
		if (userLoginId == null) {
			dcemUser2 = new DcemUser();
		} else {
			dcemUser2 = new DcemUser(domainEntity.getName(), userLoginId);
		}
		dcemUser2.ldapSync(dcemLdapAttributes);
		return dcemUser2;

	}

	/**
	 * @param detachedLdapEntity
	 * @param dn
	 * @param password
	 * @return
	 * @throws NamingException
	 * @throws DcemException
	 */
	LdapContext login(String dn, byte[] password, boolean closeAfter) throws DcemException {
		Hashtable<String, Object> envLogin = new Hashtable<String, Object>();
		envLogin.putAll(ldapEnvironment);
		envLogin.put(Context.SECURITY_PRINCIPAL, dn);
		envLogin.put(Context.SECURITY_CREDENTIALS, password);
		envLogin.put(Context.PROVIDER_URL, domainEntity.getHost());
		envLogin.put("com.sun.jndi.ldap.connect.pool", "false");
		DcemSocketFactory.threadLocalDomainEntity.set(domainEntity);
		LdapContext ctx = null;
		long startTime = System.currentTimeMillis();
		try {
			ctx = new InitialLdapContext(envLogin, null);
		} catch (CommunicationException exp3) {
			throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED, "User LDAP-Connection failed.", exp3);
		} catch (AuthenticationException exp) {
			throw convertAuthException(exp, dn);
		} catch (Exception e) {
			if (e.getMessage().contains("timeout")) {
				throw new DcemException(DcemErrorCodes.REQUEST_TIMED_OUT, dn, e);
			}
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_USER_FAILED, "Login user failed", e);
		} finally {
			logger.debug("LDAP - login for user " + dn + " took " + (System.currentTimeMillis() - startTime) + " milliseconds");
			if (closeAfter) {
				try {
					ctx.close();
				} catch (Exception e) {
				}
			}
		}
		return ctx;
	}

	LdapContext getSearchAccount() throws DcemException {
		if (domainEntity == null) {
			throw new DcemException(DcemErrorCodes.NO_DOMAIN_ENTITIES, null);
		}
		// com.sun.jndi.ldap.LdapPoolManager.
		DcemSocketFactory.threadLocalDomainEntity.set(domainEntity);
		if (searchLdapContext != null) {
			return searchLdapContext;
		}
		Hashtable<String, Object> env = new Hashtable<String, Object>();
		env.putAll(ldapEnvironment);
		env.put(Context.SECURITY_PRINCIPAL, domainEntity.getSearchAccount());
		env.put(Context.SECURITY_CREDENTIALS, domainEntity.getPassword());
		env.put(Context.PROVIDER_URL, domainEntity.getHost());
		// env.put(Context.REFERRAL, "follow");
		env.put("com.sun.jndi.ldap.connect.pool", "false");
		try {
			LdapContext ctx = new InitialLdapContext(env, null);
			searchLdapContext = ctx;
			return ctx;
		} catch (CommunicationException exp3) {
			throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED, "User LDAP-Connection failed.", exp3);
		} catch (AuthenticationException exp) {
			DcemException dcemException = convertAuthException(exp, "Search User");
			DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, dcemException.getErrorCode(),
					"LDAP/AD Search-User failed for '" + domainEntity.getSearchAccount() + "'", AlertSeverity.ERROR, false);
			throw dcemException;
		} catch (Exception exp) {
			logger.info("Create Search-User failed for: " + domainEntity.getSearchAccount() + " Exception: " + exp.toString());
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Search Account login failed.", exp);
		}

	}
	
	private Attributes getAttributes(String dn, String[] attributes) throws DcemException {
		return getAttributes(dn, attributes, "(objectclass=person)", SearchControls.OBJECT_SCOPE);
	}
	
	public Attributes getAttributes(String dn, String[] attributes, String searchFilter, int scope) throws DcemException {
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(scope);
		searchControls.setReturningAttributes(attributes);
		NamingEnumeration<SearchResult> results = null;
		SearchResult searchResult = null;
		try {
			results = getSearchAccount().search(dn, searchFilter, searchControls);
			while (results.hasMore()) {
				searchResult = results.nextElement();
				return searchResult.getAttributes();
			}
		} catch (CommunicationException ce) {
			close();
			try {
				results = getSearchAccount().search(dn, searchFilter, searchControls);
				while (results.hasMore()) {
					searchResult = results.nextElement();
					return searchResult.getAttributes();
				}
			} catch (NamingException exp) {
				throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "getAttributes.", exp);
			} catch (DcemException e) {
				throw e;
			}
		} catch (NameNotFoundException ce) {
			throw new DcemException(DcemErrorCodes.LDAP_NAME_NOT_FOUND, String.format("DN = %d, Filter = %d", dn, searchFilter), ce);
		} catch (Exception exp) {
			logger.debug(String.format("DN = %d, Filter = %d", dn, searchFilter), exp);
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Search Account login failed.", exp);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (NamingException e) {
				}
			}
		}
		return null;
	}

	@Override
	public List<String> getUserNames(String userFilter) throws DcemException {
		Map<String, Attributes> map = getUsersAttributeMap(null, null, null, userFilter, new String[] { domainEntity.getLoginAttribute() }, 50);
		List<String> userList = new ArrayList<>(map.size());
		for (Attributes attributes : map.values()) {
			try {
				userList.add((String) attributes.get(domainEntity.getLoginAttribute()).get());
			} catch (NamingException e) {
				logger.warn(e);
			}
		}
		return userList;
	}

	public Map<String, Attributes> getUsersAttributeMap(String tree, String groupDn, String baseDn, String user, String[] returnedAttributes, int pageSize)
			throws DcemException {

		StringBuffer sb = new StringBuffer();
		if (hasUserAccountControl) {
			sb.append("(&(objectclass=person)(UserAccountControl:1.2.840.113556.1.4.803:=512)" + "(!(UserAccountControl:1.2.840.113556.1.4.803:=2))");
		} else {
			sb.append("(&(objectclass=person)");
		}
		if (user != null && user.isEmpty() == false) {
			if (user.indexOf('@') != -1) {
				sb.append("(" + AD_USER_PRINCIAL_NAME + "=" + user + ")");
			} else {
				sb.append("(" + domainEntity.getLoginAttribute() + "=" + user + ")");
			}
		}
		if (groupDn != null) {
			sb.append("(memberOf=" + groupDn + ")");
		}
		sb.append(")");

		try {
			return getSearchTry(tree, sb.toString(), baseDn, returnedAttributes, pageSize);

		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED) {
				logger.info("LDAP-Connection failed, trying again");
				try {
					close();
				} catch (Exception e) {
					logger.info("Couldn't close LdapContext");
				}
				return getSearchTry(tree, sb.toString(), baseDn, returnedAttributes, pageSize);
			} else {
				logger.info("LDAP-Connection failed", exp);
				throw exp;
			}
		}

	}

	public DomainUsers getUsers(String tree, DcemGroup dcemGroup, String user, int pageSize) throws DcemException {

		StringBuffer sb = new StringBuffer();
		if (hasUserAccountControl) {
			sb.append("(&(objectclass=person)");
			// sb.append("(&(objectclass=person)(UserAccountControl:1.2.840.113556.1.4.803:=512)" + "(!(UserAccountControl:1.2.840.113556.1.4.803:=2))");
		} else {
			sb.append("(&(objectclass=person)");
		}
		if (user != null && user.isEmpty() == false) {
			sb.append("(" + domainEntity.getLoginAttribute() + "=" + user + ")");
		}
		if (dcemGroup != null && dcemGroup.getGroupDn() != null) {
			sb.append("(memberOf=" + dcemGroup.getGroupDn() + ")");
		}
		sb.append(")");
		Map<String, Attributes> map;
		try {
			map = getSearchTry(tree, sb.toString(), null, defaultUserReturnedAtts, pageSize);
			List<DcemUser> users = new ArrayList<>(map.size());
			if (map != null) {
				for (String key : map.keySet()) {
					try {
						Attributes attributes = map.get(key);
						DcemUser dcemUser = new DcemUser(domainEntity, key + domainEntity.getBaseDN(),
								attributes.get(domainEntity.getLoginAttribute()).get().toString());
						dcemUser.ldapSync(getDcemLdapAttributes(attributes));
						dcemUser.setDcemLdapAttributes(getDcemLdapAttributes(attributes));
						users.add(dcemUser);
					} catch (NamingException e) {
						logger.info(e);
					}
				}
			}
			return new DomainUsers(pageSize, false , users);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED) {
				logger.info("LDAP-Connection failed, trying again");
				try {
					close();
				} catch (Exception e) {
					logger.info("Couldn't close LdapContext");
				}
				map = getSearchTry(tree, sb.toString(), null, defaultUserReturnedAtts, pageSize);
				List<DcemUser> users = new ArrayList<>(map.size());
				if (map != null) {
					for (String key : map.keySet()) {
						try {
							Attributes attributes = map.get(key);
							DcemUser dcemUser = new DcemUser(domainEntity, key + domainEntity.getBaseDN(),
									attributes.get(domainEntity.getLoginAttribute()).get().toString());
							dcemUser.ldapSync(getDcemLdapAttributes(attributes));
							dcemUser.setDcemLdapAttributes(getDcemLdapAttributes(attributes));
							users.add(dcemUser);
						} catch (NamingException e) {
							logger.info(e);
						}
					}
				}
				new DomainUsers(pageSize, false, users);
			} else {
				throw exp;
			}
		}
		return null;

	}

	private Map<String, Attributes> getSearchTry(String tree, String searchFilter, String baseDn, String[] returnedAttributes, int pageSize)
			throws DcemException {
		LdapContext ldapContext = getSearchAccount();
		Map<String, Attributes> map = new TreeMap<>();
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		searchControls.setReturningAttributes(returnedAttributes);
		NamingEnumeration<SearchResult> results = null;
		SearchResult searchResult = null;
		try {
			Control[] ctls = new Control[] { new PagedResultsControl(pageSize, false) };
			ldapContext.setRequestControls(ctls);
		} catch (NamingException | IOException exp) {
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "PagedResultsControl failed.", exp);
		}
		if (baseDn == null) {
			baseDn = domainEntity.getBaseDN();
		}
		try {
			if (tree != null) {
				if (tree.endsWith(",") == true) {
					tree = tree.substring(0, tree.length() - 1);
				}
				StringBuffer sb = new StringBuffer();
				if (tree.isEmpty() == false) {
					NameParser parser = ldapContext.getNameParser("");
					Name dn = parser.parse(tree);
					Enumeration<String> enum1 = dn.getAll();
					while (enum1.hasMoreElements()) {
						sb.append(enum1.nextElement());
						if (sb.length() > 0) {
							sb.append(',');
						}
					}
				}
				results = ldapContext.search(sb.toString() + baseDn, searchFilter, searchControls);
			} else {
				results = ldapContext.search(baseDn, searchFilter, searchControls);
			}

			while (results.hasMore()) {
				searchResult = results.nextElement();
				map.put(searchResult.getName(), searchResult.getAttributes());
			}
		} catch (InvalidNameException | NameNotFoundException ne) {
			throw new DcemException(DcemErrorCodes.DOMAIN_INVALID_NAME, ne.getMessage());
		} catch (PartialResultException exp) {
			// throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED,
			// "LDAP Connection failed due to PartialResultException. Try to make the 'Base DN' more specific or user port 3269");
			// logger.info("PartialResultException got ignored");
		} catch (Throwable exp) {
			if (exp.getClass() == javax.naming.CommunicationException.class) {
				throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED, "LDAP Connection failed.", exp);
			}
			if (exp.getClass() == InvalidSearchFilterException.class) {
				throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Invalid Search Filter", exp);
			}
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Search Account login failed.", exp);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (NamingException e) {
				}
			}
		}
		return map;
	}

	// public List<String> getSelectedGroups(ActiveDomain ldapdomain, String filter,
	// int pageSize) throws DcemException,
	// NamingException {
	// Map<String, Attributes> map = getGroups(ldapdomain, "name", filter + "*",
	// pageSize);
	// List<String> groups = new LinkedList<>();
	// String name;
	// for (String key : map.keySet()) {
	// name = (String) map.get(key).get("name").get();
	// groups.add(name);
	// }
	// return groups;
	// }

	private DcemLdapAttributes getDcemLdapAttributes(Attributes attrs) {
		DcemLdapAttributes dcemLdapAttributes = new DcemLdapAttributes();
		try {
			Attribute attribute = attrs.get(PROPERTY_DISTINGUISHED_NAME);
			if (attribute != null) {
				dcemLdapAttributes.setDn((String) attribute.get());
			}
			attribute = attrs.get(domainEntity.getFirstNameAttribute());
			if (attribute != null) {
				dcemLdapAttributes.setFirstName((String) attribute.get());
			}
			attribute = attrs.get(domainEntity.getLastNameAttribute());
			if (attribute != null) {
				dcemLdapAttributes.setLastGivenName((String) attribute.get());
			}

			attribute = attrs.get(DcemConstants.LDAP_DISPLAY_ATTRIBUTE);
			if (attribute != null) {
				dcemLdapAttributes.setDisplayName((String) attribute.get());
			}

			attribute = attrs.get(domainEntity.getMailAttribute());
			if (attribute != null) {
				dcemLdapAttributes.setEmail((String) attribute.get());
			}
			attribute = attrs.get(domainEntity.getTelephoneAttribute());
			if (attribute != null) {
				dcemLdapAttributes.setTelephone((String) attribute.get());
			}
			attribute = attrs.get(domainEntity.getMobileAttribute());
			if (attribute != null) {
				dcemLdapAttributes.setMobile((String) attribute.get());
			}
			attribute = attrs.get(AD_USER_PRINCIAL_NAME);
			if (attribute != null) {
				dcemLdapAttributes.setUserPrincipalName((String) attribute.get());
			}
			attribute = attrs.get(AD_USER_OBJECT_GUID);
			if (attribute != null) {
				dcemLdapAttributes.setObjectGuid((byte[]) attribute.get());
			}
			attribute = attrs.get(AD_USER_THUMBNAIL_PHOTO);
			if (attribute != null) {
				dcemLdapAttributes.setPhoto((byte[]) attribute.get());
			}
			attribute = attrs.get(DcemConstants.LDAP_PREFERRED_LANGUAGE);
			if (attribute != null) {
				dcemLdapAttributes.setPreferredLanguage((String) attribute.get());
			}
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dcemLdapAttributes;
	}

	/**
	 * @param dcemGroup
	 * @return
	 */
	@Override
	public DomainUsers getGroupMembers(DcemGroup dcemGroup, String filter) throws DcemException {

		Map<String, Attributes> map = getUsersAttributeMap(null, dcemGroup.getGroupDn(), null, null, defaultUserReturnedAtts, DomainLogic.PAGE_SIZE);
		List<DcemUser> userList = new ArrayList<>(map.size());
		Attributes attributes;
		String loginId;
		for (String key : map.keySet()) {
			attributes = map.get(key);
			try {
				loginId = (String) attributes.get(dcemGroup.getDomainEntity().getLoginAttribute()).get();
			} catch (NamingException e) {
				logger.error(e);
				continue;
			}
			DcemUser dcemUser2 = new DcemUser(domainEntity.getName(), loginId);
			DcemLdapAttributes dcemLdapAttributes = getDcemLdapAttributes(attributes);
			dcemUser2.ldapSync(dcemLdapAttributes);
			dcemUser2.setDomainEntity(domainEntity);
			userList.add(dcemUser2);
		}
		return new DomainUsers(DomainLogic.PAGE_SIZE, false , userList);
	}

	@Override
	public Map<String, Attributes> customSearchAttributeMap(String tree, String searchFilter, String baseDn, String[] returnedAttributes, int pageSize)
			throws DcemException {
		return getSearchTry(null, searchFilter, baseDn, null, pageSize);
	}

	@Override
	public DcemUser getUser(String loginId) throws DcemException {
		Map<String, Attributes> map = getUsersAttributeMap(null, null, null, loginId, defaultUserReturnedAtts, DomainLogic.PAGE_SIZE);
		if (map.size() == 1) {
			try {
				Attributes attributes = map.values().iterator().next();
				DcemUser dcemUser = new DcemUser(domainEntity, attributes.get(PROPERTY_DISTINGUISHED_NAME).get().toString(),
						attributes.get(domainEntity.getLoginAttribute()).get().toString());
				dcemUser.ldapSync(getDcemLdapAttributes(attributes));
				return dcemUser;
			} catch (NamingException e) {
				logger.info(e);
			}
		}
		throw new DcemException(DcemErrorCodes.INVALID_USERID, loginId);
	}

	public List<String> getSelectedLdapTree(String tree, int pageSize) throws DcemException {
		String searchFilter = "(|(objectClass=container)(objectClass=organizationalUnit))";
		try {
			return getSearchTree(searchFilter, tree, pageSize);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED) {
				logger.info("LDAP-Connection failed, trying again");
				close();
				return getSearchTree(searchFilter, tree, pageSize);
			} else {
				throw exp;
			}
		}
	}

	private List<String> getSearchTree(String searchFilter, String tree, int pageSize) throws DcemException {
		LdapContext ldapContext = getSearchAccount();
		SearchControls searchControls = new SearchControls();
		searchControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
		// searchControls.setReturningAttributes(new String []);
		NamingEnumeration<SearchResult> results = null;
		SearchResult searchResult = null;
		try {
			Control[] ctls = new Control[] { new PagedResultsControl(pageSize, false) };
			ldapContext.setRequestControls(ctls);
		} catch (NamingException | IOException exp) {
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "PagedResultsControl failed.", exp);
		}
		List<String> list = new LinkedList<>();

		try {
			String tree2 = "";
			NameParser parser = ldapContext.getNameParser("");
			if (tree != null && tree.isEmpty() == false) {
				if (tree.endsWith(",") == true) {
					tree = tree.substring(0, tree.length() - 1);
				}
				if (tree.isEmpty() == false) {
					tree2 = reverseDn(parser, tree) + ",";
				}
			}

			results = ldapContext.search(tree2 + domainEntity.getBaseDN(), searchFilter, searchControls);
			while (results.hasMore()) {
				searchResult = results.nextElement();
				String foundName = reverseDn(parser, searchResult.getName());
				if (tree2.isEmpty()) {
					list.add(foundName);
				} else {
					list.add(tree + "," + foundName);
				}
			}
		} catch (InvalidNameException | NameNotFoundException ne) {
			throw new DcemException(DcemErrorCodes.DOMAIN_INVALID_NAME, ne.getMessage());
		} catch (Exception exp) {
			if (exp.getClass() == javax.naming.CommunicationException.class) {
				throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED, "LDAP Connection failed.", exp);
			}
			if (exp.getClass() == InvalidSearchFilterException.class) {
				throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Invalid Search Filter", exp);
			}
			throw new DcemException(DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED, "Search Account login failed.", exp);
		} finally {
			if (results != null) {
				try {
					results.close();
				} catch (NamingException e) {
				}
			}
		}
		return list;
	}

	private String reverseDn(NameParser parser, String tree) throws NamingException {
		Name dn = parser.parse(tree);
		Enumeration<String> enum1 = dn.getAll();
		StringBuffer sb = new StringBuffer();
		while (enum1.hasMoreElements()) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(enum1.nextElement());

		}
		return sb.toString();
	}

	@Override
	public void updateUserPassword(DcemUser dcemUser, String currentPassword, String newPassword) throws DcemException {
		boolean isSearchAccount = false;
		LdapContext ldapContext = null;
		try {
			ldapContext = login(dcemUser.getUserDn(), currentPassword.getBytes(DcemConstants.UTF_8), false);
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.USER_PASSWORD_EXPIRED || e.getErrorCode() == DcemErrorCodes.USER_MUST_RESET_PASSWORD) {
				ldapContext = getSearchAccount();
				isSearchAccount = true;
			} else {
				throw e;
			}
		}
		long startTime = System.currentTimeMillis();
		try {
			String newQuotedPassword = "\"" + newPassword + "\"";
			byte[] newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
			ModificationItem[] mods = new ModificationItem[1];
			mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("UnicodePwd", newUnicodePassword));
			ldapContext.modifyAttributes(dcemUser.getUserDn(), mods);
		} catch (NoPermissionException e) {
			logger.error("LDAP - exception while updating password", e);
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, dcemUser.getUserDn(), e);
		} catch (Exception e) {
			logger.error("LDAP - exception while updating password", e);
			if (e.getMessage().contains("timeout")) {
				throw new DcemException(DcemErrorCodes.REQUEST_TIMED_OUT, dcemUser.getUserDn(), e);
			}
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, dcemUser.getUserDn(), e);
		} finally {
			logger.debug("LDAP - update password for user " + dcemUser.getLoginId() + " took " + (System.currentTimeMillis() - startTime) + " milliseconds");
			if (ldapContext != null) {
				if (!isSearchAccount) {
					try {
						ldapContext.close();
					} catch (NamingException e) {
					}
				}
			}
		}
	}

	@Override
	public List<DcemGroup> getGroups(String filter, int pageSize) throws DcemException {
		if (domainEntity.getDomainType() == DomainType.Generic_LDAP) {
			return getLdapGroups("cn", filter, pageSize);
		}
		return getLdapGroups("name", filter, pageSize);
	}

	private List<DcemGroup> getLdapGroups(String filterName, String filter, int pageSize) throws DcemException {
		if (filter == null) {
			filter = "*";
		}
		filter = filter.replace("\\,", "\\\\2c");
		String searchFilter = "(&(objectClass=" + domainEntity.getDomainConfig().getGroupAttribute() + ")(" + filterName + "=" + filter + "))";
		String name = GROUP_NAME;
		if (domainEntity.getDomainType() == DomainType.Generic_LDAP) {
			name = CN;
		}
		String returnedAtts[] = { name, PROPERTY_DISTINGUISHED_NAME };
		Map<String, Attributes> map;
		try {
			map = getSearchTry(null, searchFilter, null, returnedAtts, pageSize);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED || exp.getErrorCode() == DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED) {
				logger.info("LDAP-Connection failed, trying again");
				close();
				map = getSearchTry(null, searchFilter, null, returnedAtts, pageSize);
			} else {
				throw exp;
			}
		}
		List<DcemGroup> groups = new ArrayList<>(map.size());
		if (map != null) {
			for (String dn : map.keySet()) {
				try {
					Attributes attributes = map.get(dn);
					if (domainEntity.getDomainType() == DomainType.Generic_LDAP) {
						groups.add(new DcemGroup(domainEntity, dn + "," + domainEntity.getBaseDN(), attributes.get(CN).get().toString()));
					} else {
						groups.add(new DcemGroup(domainEntity, dn + "," + domainEntity.getBaseDN(), attributes.get(GROUP_NAME).get().toString()));

					}
				} catch (NamingException e) {
					logger.info(e);
				}
			}
		}
		return groups;
	}

	@Override
	public HashSet<String> getUserGroupNames(DcemUser dcemUser, String filter, int pageSize) throws DcemException {
		HashSet<String> groups = getLdapUserGroupNames(dcemUser, filter, pageSize);
		if (groups.isEmpty()) {
			DcemUser dcemUser2 = getUser(dcemUser.getAccountName());
			if (dcemUser.getUserDn().equals(dcemUser2.getUserDn()) == false) {
				dcemUser.setUserDn(dcemUser2.getUserDn());
				groups = getUserGroupNames(dcemUser, filter, pageSize); // try again with new DN
			}
		}
		return groups;
	}

	private HashSet<String> getLdapUserGroupNames(DcemUser dcemUser, String filter, int pageSize) throws DcemException {
		// check if user has been moved around
		DcemUser dcemUser2 = getUser(dcemUser.getAccountName());
		if (dcemUser.getUserDn().equals(dcemUser2.getUserDn()) == false) {
			dcemUser.setUserDn(dcemUser2.getUserDn());
		}
		String searchFilter;
		if (filter == null || filter.isEmpty()) {
			searchFilter = "(&(objectClass=group)(member=" + dcemUser.getUserDn().replace("\\,", "\\\\2c") + "))";
		} else {
			searchFilter = "(&(objectClass=group)(member=" + dcemUser.getUserDn().replace("\\,", "\\\\2c") + ")(name=" + filter + "))";
		}
		String returnedAtts[] = { GROUP_NAME };
		Map<String, Attributes> map;
		try {
			map = getSearchTry(null, searchFilter, null, returnedAtts, pageSize);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.LDAP_CONNECTION_FAILED || exp.getErrorCode() == DcemErrorCodes.LDAP_LOGIN_SEARCH_ACCOUNT_FAILED) {
				logger.info("LDAP-Connection failed, trying again");
				close();
				map = getSearchTry(null, searchFilter, null, returnedAtts, pageSize);
			} else {
				throw exp;
			}
		}
		HashSet<String> groups = new HashSet<>(map.size());
		if (map != null) {
			for (Attributes attributes : map.values()) {
				try {
					groups.add(domainEntity.getName() + DcemConstants.DOMAIN_SEPERATOR + attributes.get("name").get().toString());
				} catch (NamingException e) {
					logger.info(e);
				}
			}
		}
		return groups;
	}

	@Override
	public List<DcemGroup> getUserGroups(DcemUser dcemUser, int pageSize) throws DcemException {
		List<DcemGroup> groups = getLdapGroups(MEMBER, dcemUser.getUserDn(), pageSize);
		if (groups.isEmpty()) {
			DcemUser dcemUser2 = getUser(dcemUser.getAccountName());
			if (dcemUser.getUserDn().equals(dcemUser2.getUserDn()) == false) {
				dcemUser.setUserDn(dcemUser2.getUserDn());
				groups = getLdapGroups(MEMBER, dcemUser.getUserDn(), pageSize); // try again with new DN
			}
		}
		return groups;
	}

	@Override
	public DomainEntity getDomainEntity() {
		return domainEntity;
	}

	@Override
	public Map<String, String> getUserAttributes(DcemUser dcemUser, List<String> attributeList) throws DcemException {
		String[] array = attributeList.toArray(new String[0]);
		Attributes attributes = getAttributes(dcemUser.getUserDn(), array);
		Map<String, String> resultList = new HashMap<>();
		NamingEnumeration<String> attrEnum = attributes.getIDs();
		try {
			String value;
			while (attrEnum.hasMore()) {
				Attribute attribute = attributes.get(attrEnum.next());
				String idLowerCase = attribute.getID().toLowerCase();
				if (idLowerCase.equals(AD_USER_OBJECT_SID)) {
					value = convertSidToStringSid((byte[]) attribute.get());
				} else {
					value = attribute.get().toString();
				}
				resultList.put(idLowerCase, value);
			}
		} catch (NamingException e) {
			logger.warn("Couldn' read Attributes", e);
		}
		return resultList;
	}

	private DcemException convertAuthException(Exception exp, String dn) throws DcemException {
		int ind = exp.getMessage().indexOf(AD_ERROR_DATA);
		if (ind != -1) {
			String msg = exp.getMessage().substring(ind + AD_ERROR_DATA.length());
			if (msg.startsWith(AD_ERROR_USER_DISABLED)) { // disabled account
				return new DcemException(DcemErrorCodes.USER_DISABLED, dn, exp);
			} else if (msg.startsWith(AD_ERROR_USER_MUST_RESET_PASSWORD)) {
				return new DcemException(DcemErrorCodes.USER_MUST_RESET_PASSWORD, dn, exp);
			} else if (msg.startsWith(AD_ERROR_USER_ACCOUNT_LOCKED)) {
				return new DcemException(DcemErrorCodes.USER_ACCOUNT_LOCKED, dn, exp);
			} else if (msg.startsWith(AD_ERROR_USER_PASSWORD_EXPIRED)) {
				return new DcemException(DcemErrorCodes.USER_PASSWORD_EXPIRED, dn, exp);
			}
		}
		return new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, "Wrong Authentication for " + dn, exp);
	}

	public static String convertSidToStringSid(byte[] sid) {
		int offset, size;
		// sid[0] is the Revision, we allow only version 1, because it's the
		// only that exists right now.
		if (sid[0] != 1) {
			throw new IllegalArgumentException("SID revision must be 1");
		}
		StringBuilder stringSidBuilder = new StringBuilder("S-1-");
		// The next byte specifies the numbers of sub authorities (number of
		// dashes minus two)
		int subAuthorityCount = sid[1] & 0xFF;
		// IdentifierAuthority (6 bytes starting from the second) (big endian)
		long identifierAuthority = 0;
		offset = 2;
		size = 6;
		for (int i = 0; i < size; i++) {
			identifierAuthority |= (long) (sid[offset + i] & 0xFF) << (8 * (size - 1 - i));
			// The & 0xFF is necessary because byte is signed in Java
		}
		if (identifierAuthority < Math.pow(2, 32)) {
			stringSidBuilder.append(Long.toString(identifierAuthority));
		} else {
			stringSidBuilder.append("0x").append(Long.toHexString(identifierAuthority).toUpperCase());
		}

		// Iterate all the SubAuthority (little-endian)
		offset = 8;
		size = 4; // 32-bits (4 bytes) for each SubAuthority
		for (int i = 0; i < subAuthorityCount; i++, offset += size) {
			long subAuthority = 0;
			for (int j = 0; j < size; j++) {
				subAuthority |= (long) (sid[offset + j] & 0xFF) << (8 * j);
				// The & 0xFF is necessary because byte is signed in Java
			}
			stringSidBuilder.append("-").append(subAuthority);
		}
		return stringSidBuilder.toString();
	}

	@Override
	public void resetUserPassword(DcemUser dcemUser, String newPassword) throws DcemException {
		LdapContext ldapContext = getSearchAccount();
		String newQuotedPassword = "\"" + newPassword + "\"";
		byte[] newUnicodePassword = null;
		try {
			newUnicodePassword = newQuotedPassword.getBytes("UTF-16LE");
		} catch (UnsupportedEncodingException e) {
			logger.warn("Unsupported Encoding", e);
			throw new DcemException(DcemErrorCodes.UNSUPPORTED_ENCODING, e.getLocalizedMessage());
		}
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute("UnicodePwd", newUnicodePassword));
		try {
			ldapContext.modifyAttributes(dcemUser.getUserDn(), mods);
		} catch (NamingException e) {
			logger.warn("Couldn' read Attributes", e);
			throw new DcemException(DcemErrorCodes.LDAP_CONNECTION_FAILED, e.getLocalizedMessage());
		}
	}

	@Override
	public byte[] getUserPhoto(DcemUser dcemUser) throws DcemException {
		try {
			byte[] photo = null;
			Attributes attributes = getAttributes(dcemUser.getUserDn(), new String[] { AD_USER_THUMBNAIL_PHOTO });
			Attribute attribute = attributes.get(AD_USER_THUMBNAIL_PHOTO);
			if (attribute != null) {
				photo = (byte[]) attribute.get();
				photo = DcemUtils.resizeImage(photo, DcemConstants.PHOTO_MAX);
			}
			return photo;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void changeUserPhotoProfile(DcemUser dcemUser, byte[] photo, String password) throws DcemException {
		LdapContext ldapContext = getSearchAccount();
		ModificationItem[] mods = new ModificationItem[1];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(AD_USER_THUMBNAIL_PHOTO, photo));
		try {
			ldapContext.modifyAttributes(dcemUser.getUserDn(), mods);
		} catch (NoPermissionException e) {
			logger.info("LDAP - exception while updating password", e);
			throw new DcemException(DcemErrorCodes.INSUFFICIENT_ACCESS_RIGHTS, dcemUser.getUserDn(), e);
		} catch (NamingException e) {
			logger.warn("Couldn' read Attributes", e);
			throw new DcemException(DcemErrorCodes.LDAP_FAILED_READ_ATTRIBUTE, e.getLocalizedMessage(), e);
		}
	}
}
