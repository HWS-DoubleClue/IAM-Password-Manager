package com.doubleclue.dcem.core.entities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.IPhoto;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DbEncryptConverter;
import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.DcemLdapAttributes;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.utils.compare.DcemCompare;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;


/**
 * The persistent class for user
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "core_user", uniqueConstraints = { @UniqueConstraint(name = "UK_APP_USER", columnNames = { "loginId" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = DcemUser.GET_FILTER_LIST, query = "SELECT user FROM DcemUser user WHERE LOWER(user.loginId) LIKE LOWER(?1) ESCAPE "
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES),
		@NamedQuery(name = DcemUser.GET_USER_LOGIN_DOMAIN, query = "SELECT user FROM DcemUser user WHERE user.domainEntity = ?1 AND (user.loginId = ?2 OR user.userPrincipalName = ?2)"),
		@NamedQuery(name = DcemUser.GET_USER_LOGIN, query = "SELECT user FROM DcemUser user WHERE user.loginId = ?1 OR user.userPrincipalName = ?1"),
		@NamedQuery(name = DcemUser.GET_USER_LOGIN_SEARCH, query = "SELECT user FROM DcemUser user WHERE user.loginId = ?1 OR user.userPrincipalName = ?1 OR user.loginId LIKE ?2 OR user.userPrincipalName LIKE ?2 ESCAPE "
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES),
		@NamedQuery(name = DcemUser.GET_TOTAL_USER_COUNT, query = "SELECT COUNT(user) FROM DcemUser user WHERE user.disabled = false"),
		@NamedQuery(name = DcemUser.GET_USERS_LDAP_NAME, query = "SELECT user from DcemUser user where user.domainEntity = ?2 AND user.loginId like ?1 ESCAPE "
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES),
		@NamedQuery(name = DcemUser.GET_USER_BY_DISPLAYNAME, query = "SELECT user FROM DcemUser user WHERE LOWER(user.displayName) = ?1"),
		@NamedQuery(name = DcemUser.GET_USERS_DISPLAYNAME, query = "SELECT user.displayName FROM DcemUser user WHERE user.displayName LIKE LOWER(?1) ORDER BY user.displayName ASC"),

		@NamedQuery(name = DcemUser.GET_USERS_ROLE, query = "SELECT user from DcemUser user where user.disabled = false AND user.dcemRole IN (?1)"),
		// @NamedQuery(name = DcemUser.GET_USER_LOGIN_WITH_DOMAIN, query = "SELECT user
		// FROM DcemUser user where
		// user.loginId = ?1 AND user.ldapEntity=?2")
		@NamedQuery(name = DcemUser.GET_USERS, query = "SELECT u FROM DcemUser u WHERE u.id IN (?1)"),
		@NamedQuery(name = DcemUser.GET_DOMAIN_USERS, query = "SELECT u FROM DcemUser u WHERE u.domainEntity = ?1"),
		@NamedQuery(name = DcemUser.GET_USER_BY_DN, query = "SELECT u FROM DcemUser u WHERE u.userDn = ?1"),
		@NamedQuery(name = DcemUser.GET_USER_BY_UPN, query = "SELECT u FROM DcemUser u WHERE u.userPrincipalName = ?1"),
		@NamedQuery(name = DcemUser.GET_USER_LDAP, query = "SELECT user FROM DcemUser user WHERE user.loginId = ?1"),
		@NamedQuery(name = DcemUser.GET_FILTERED_USERS_BY_DOMAIN, query = "SELECT user FROM DcemUser user WHERE user.domainEntity = ?1 AND user.loginId LIKE ?2 ESCAPE "
				+ DcemConstants.JPA_ESCAPE_CHAR_QUOTES),

})
public class DcemUser extends EntityInterface implements Serializable, Cloneable, IPhoto {

	private static final long serialVersionUID = 1L;

	public final static String GET_FILTER_LIST = "DcemUser.filterList";
	public final static String GET_USER_LOGIN = "DcemUser.userLogin";
	public final static String GET_USER_LOGIN_DOMAIN = "DcemUser.userLoginDomain";

	public final static String GET_USER_LOGIN_SEARCH = "DcemUser.userLoginSearch";
	public final static String GET_USERS_BY_LDAP = "DcemUser.usersByLdap";
	public final static String GET_USERS_ROLE = "DcemUser.getUsersRole";
	public final static String GET_USERS = "DcemUser.getUsers";
	public final static String GET_USERS_LDAP_NAME = "DcemUser.getUsersFromLdap";
	public final static String GET_USER_LDAP = "DcemUser.getUserLdap";
	public final static String GET_FILTERED_USERS_BY_DOMAIN = "DcemUser.getFilteredUsersByDomain";
	public final static String GET_TOTAL_USER_COUNT = "getTotalUserCount";
	public final static String GET_USER_BY_DISPLAYNAME = "DcemUser.getUserByDisplay";
	public final static String GET_USERS_DISPLAYNAME = "DcemUser.getUsersDisplayName";
	public static final String GET_DOMAIN_USERS = "DcemUser.getDomainUsers";
	public static final String GET_USER_BY_DN = "DcemUser.userByDn";
	public static final String GET_USER_BY_UPN = "DcemUser.userByUpn";

	// public final static String GET_USER_LOGIN_WITH_DOMAIN =
	// "DcemUser.userLoginWithDomain";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreCoreUser", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "CORE_USER.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreCoreUser")
	@DcemGui (visible = false)
	private Integer id;
	
	@DcemGui (name= "Photo", subClass = "photo", variableType = VariableType.IMAGE)
	@Transient
	byte [] photo;
	
	
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
	@JoinColumn(referencedColumnName = "dc_userext_id", foreignKey = @ForeignKey(name = "FK_USER_EXTENSION"), name = "userext", nullable = true, insertable = true, updatable = true)
	@DcemCompare (ignore = true)  // comapred seperatly
	private DcemUserExtension dcemUserExt;

	@Column(length = 255, nullable = true)
	@Size(min = 2, max = 255)
	@DcemGui(style = "width: 25em")
	private String displayName;

	@Column(length = 255, nullable = false)
	@Size(min = 2, max = 255)
	@DcemGui
	private String loginId;

	@Column(length = 255, nullable = true)
	@Size(min = 2, max = 255)
	@DcemGui(name = "UserPrincipalName", visible = false)
	private String userPrincipalName; // This is used for Active Directory

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_USER_LDAP"), name = "dc_ldap", nullable = true, insertable = true, updatable = true)
	private DomainEntity domainEntity;

	@Column(length = 255, nullable = true)
	// EG: have to remove this as some active directory users have an invalid email
	// address.
	// @Pattern(regexp = "^$|^([\\w\\.\\-]+)@([\\w\\-]+)((\\.(\\w){0,30})+)$",
	// message = "invalid.email.pattern")
	@DcemGui
	@Size(max = 256)
	private String email;

	@Column(length = 255, nullable = true)
	@DcemGui(name = "privateEmail", visible = false)
	@Size(max = 256)
	private String privateEmail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE"), name = "dc_role", nullable = false, insertable = true, updatable = true)
	@DcemGui(subClass = "name", displayMode = DisplayModes.TABLE_ONLY)
	private DcemRole dcemRole;

	@DcemGui
	@Column(name = "dc_tel", nullable = true)
	@Size(max = 32)
	private String telephoneNumber;

	@DcemGui
	@Size(max = 32)
	private String mobileNumber;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "locale", nullable = true)
	private SupportedLanguage language;

	@DcemGui()
	boolean disabled;

	@DcemGui(displayMode = DisplayModes.TABLE_ONLY, visible = true)
	LocalDateTime acSuspendedTill; // if NUll then manually disabled.

	@DcemGui(displayMode = DisplayModes.TABLE_ONLY, visible = false)
	private int failActivations;

	@Column(length = 255, nullable = true)
	@DcemGui(visible = false, name = "Distinguish Name/ID")
	@DcemCompare (ignore = true)
	private String userDn;
	
	@DcemGui (name= "Country", subClass = "country", dbMetaAttributeName = "dcemUserExt")
	@Transient
	@DcemCompare (ignore = true)
	private DcemUserExtension dcemUserExtCountry;
	
	@DcemGui (name= "Department", subClass = "department", dbMetaAttributeName = "dcemUserExt", style = "white-space: nowrap")
	@Transient
	@DcemCompare (ignore = true)
	private DcemUserExtension dcemUserExtDepartment;
	
	@DcemGui (name= "JobTitle", subClass = "jobTitle", dbMetaAttributeName = "dcemUserExt")
	@Transient
	@DcemCompare (ignore = true)
	private DcemUserExtension dcemUserExtJobtitle;

	@Version
	@DcemCompare (ignore = true)
	private int jpaVersion;

	@Column(length = 128, nullable = true)
	@Convert(converter = DbEncryptConverterBinary.class)
	@DcemCompare (ignore = true)
	private byte[] hashPassword;

	@Column(length = 128, nullable = true)
	@Convert(converter = DbEncryptConverter.class)
	@DcemCompare (ignore = true)
	private String saveit;

	@DcemGui(displayMode = DisplayModes.TABLE_ONLY, styleClass = "mediumInput", visible = false)
	@DcemCompare (ignore = true)
	LocalDateTime lastLogin;

	@Size(max = 32)
	@Column(name = "prvMobile", length = 32, nullable = true)
	private String privateMobileNumber;

	@Column(length = 32, nullable = false)
	@DcemGui(displayMode = DisplayModes.NONE)
	@DcemCompare (ignore = true)
	byte[] hmac;

	@Column(length = 32, nullable = true, name = "dc_salt")
	@DcemGui(displayMode = DisplayModes.NONE)
	@DcemCompare (ignore = true)
	byte[] salt;

	@DcemGui(visible = false)
	private int passCounter = 0;

	@Column(length = 255, nullable = true)
	@DcemGui(displayMode = DisplayModes.NONE)
	@DcemCompare (ignore = true)
	byte[] objectGuid;

	@Transient
	private String tenantName;

	@Transient
	@DcemCompare (ignore = true)
	private String initialPassword;

	@Transient
	@DcemGui(visible = false)
	@DcemCompare (ignore = true)
	String immutableId;
	
	@Transient
	@DcemCompare (ignore = true)
	DcemLdapAttributes dcemLdapAttributes;

	private static final Logger logger = LogManager.getLogger(DcemUser.class);

	

	@PostLoad
	public void postLoad() throws Exception {
		byte[] array = calcHmac();
		if (Arrays.equals(array, hmac) == false) {
			setDisplayName("MANIPULATED :( " + displayName);
			setDisabled(true);
			WeldRequestContext requestContext = null;
			try {
				requestContext = WeldContextUtils.activateRequestContext();
				DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
				reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.USER_DB_MANIPULATION, "User: " + this.loginId,
						AlertSeverity.ERROR, false);
			} catch (Exception e) {
				logger.debug(e);
			} finally {
				WeldContextUtils.deactivateRequestContext(requestContext);
			}
		}
	}

	@PrePersist
	@PreUpdate
	public void preSave() {
		try {
			setHmac(calcHmac());
		} catch (Exception exp) {
			logger.error("Couldn't reproduce hamc for " + loginId, exp);
		}
	}

	private byte[] calcHmac() throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream outputStream = new DataOutputStream(baos);
		outputStream.writeInt(dcemRole.getid());
		outputStream.writeBoolean(isDisabled());
		outputStream.writeUTF(loginId);
		outputStream.writeInt(failActivations);
		if (hashPassword != null) {
			outputStream.write(hashPassword);
		}
		return SecureServerUtils.createMacSha1(baos.toByteArray());
	}

	public DcemUser() {

	}

	public DcemUser(String loginId, String email, String displayName, DcemRole dcemRole) {
		super();
		setLoginId(loginId);
		this.email = email;
		this.displayName = displayName;
		this.disabled = false;
		this.dcemRole = dcemRole;
	}

	public DcemUser(String loginId, String email, String privateEmail, String displayName, DcemRole dcemRole) {
		super();
		setLoginId(loginId);
		this.email = email;
		this.privateEmail = privateEmail;
		this.displayName = displayName;
		this.disabled = false;
		this.dcemRole = dcemRole;
	}

	public DcemUser(String loginId) {
		setLoginId(loginId);
	}

	public String getShortLoginId() {
		if (domainEntity == null) {
			return loginId;
		}
		return loginId.substring(domainEntity.getName().length() + 1);
	}

	public DcemUser(int id) {
		this.id = id;
	}

	public DcemUser(String domainName, String loginId) {
		setLoginId((domainName == null || domainName.isEmpty()) ? loginId : domainName + DcemConstants.DOMAIN_SEPERATOR + loginId);
		displayName = loginId;
	}

	public DcemUser(DomainEntity domainEntity, String dn, String name) {
		setLoginId(domainEntity.getName() + DcemConstants.DOMAIN_SEPERATOR + name);
		this.domainEntity = domainEntity;
		this.userDn = dn;
	}

	public boolean ldapSync(DcemLdapAttributes attributes) {
		if (attributes.getEmail() != null && attributes.getEmail().isEmpty() == false) {
			this.email = attributes.getEmail();
		}
		if (attributes.getDisplayName() != null && attributes.getDisplayName().isEmpty() == false) {
			this.displayName = attributes.getDisplayName();
		}
		this.userDn = attributes.getDn();
		if (attributes.getMobile() != null && attributes.getMobile().isEmpty() == false) {
			this.mobileNumber = attributes.getMobile();
		}
		if (attributes.getTelephone() != null && attributes.getTelephone().isEmpty() == false) {
			this.telephoneNumber = attributes.getTelephone();
		}
		userPrincipalName = attributes.getUserPrincipalName();
		if (userPrincipalName != null) {
			userPrincipalName = this.userPrincipalName.toLowerCase();
		}
		if (attributes.getObjectGuid() != null) {
			this.objectGuid = attributes.getObjectGuid();
		}
		if (attributes.getPreferredLanguage() != null) {
			Locale locale = Locale.forLanguageTag(attributes.getPreferredLanguage());
			this.language = SupportedLanguage.fromLocale(locale);
		}
		return true;
	}

	public void updateDomainAttributes (DcemLdapAttributes dcemLdapAttributes) {
		email = dcemLdapAttributes.getEmail();
		displayName = dcemLdapAttributes.getDisplayName();
		userDn = dcemLdapAttributes.getDn();
		mobileNumber = dcemLdapAttributes.getMobile();
		telephoneNumber = dcemLdapAttributes.getTelephone();
		userPrincipalName = dcemLdapAttributes.getUserPrincipalName();
		if (userPrincipalName != null) {
			userPrincipalName = userPrincipalName.toLowerCase();
		}
//		if (dcemLdapAttributes. != null) {
//			this.language = dcemLdapAttributes.getLanguage();
//		}
		objectGuid = dcemLdapAttributes.getObjectGuid();
		return;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String toString() {
		return getLoginId();
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId.toLowerCase();
	}

	public String getDisplayName() {
		if ((displayName == null || displayName.isEmpty()) && loginId != null) {
			displayName = getAccountName();
		}
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayNameOrLoginId() {
		if (displayName == null || displayName.isEmpty()) {
			return loginId;
		}
		return displayName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPrivateEmail() {
		return privateEmail;
	}

	public void setPrivateEmail(String privateEmail) {
		this.privateEmail = privateEmail;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public LocalDateTime getAcSuspendedTill() {
		return acSuspendedTill;
	}

	public void setAcSuspendedTill(LocalDateTime acSuspendedTill) {
		this.acSuspendedTill = acSuspendedTill;
	}

	public int getFailActivations() {
		return failActivations;
	}

	public void setFailActivations(int failActivations) {
		this.failActivations = failActivations;
	}

	public byte[] getHashPassword() {
		return hashPassword;
	}

	public void setHashPassword(byte[] hashPassword) {
		this.hashPassword = hashPassword;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public SupportedLanguage getLanguage() {
		return language;
	}

	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}

	public String getUserDn() {
		return userDn;
	}

	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

	public String getSaveit() {
		return saveit;
	}

	public void setSaveit(String saveit) {
		this.saveit = saveit;
	}

	public int getPassCounter() {
		return passCounter;
	}

	public void setPassCounter(int passCounter) {
		this.passCounter = passCounter;
	}

	public DomainEntity getDomainEntity() {
		return domainEntity;
	}

	public void setDomainEntity(DomainEntity ldapEntity) {
		this.domainEntity = ldapEntity;
	}

	@Transient
	public boolean isDomainUser() {
		return domainEntity != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (id == null) {
			if (((EntityInterface) obj).getId() != null)
				return false;
		} else if (!id.equals(((EntityInterface) obj).getId()))
			return false;
		return true;
	}

	public String getTelephoneNumber() {
		return telephoneNumber;
	}

	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}

	public String getPartMobileNumber() {
		if (privateMobileNumber != null && privateMobileNumber.length() > 4) {
			return "..." + privateMobileNumber.substring(privateMobileNumber.length() - 4);
		}
		return null;
	}

	public String getPrivateMobileNumber() {
		return privateMobileNumber;
	}

	public void setPrivateMobileNumber(String privateMobileNumber) {
		this.privateMobileNumber = privateMobileNumber;
	}

	public String getMobile() {
		if (mobileNumber != null && mobileNumber.length() > 1) {
			return mobileNumber;
		}
		if (privateMobileNumber != null && privateMobileNumber.length() > 1) {
			return privateMobileNumber;
		}
		return null;
	}

	public void synchUserAttributes(DcemUser synchUser) {
		email = synchUser.getEmail();
		privateEmail = synchUser.privateEmail;
		telephoneNumber = synchUser.getTelephoneNumber();
		mobileNumber = synchUser.getMobile();
		displayName = synchUser.getDisplayName();
		userPrincipalName = synchUser.getUserPrincipalName();
		if (userPrincipalName != null) {
			userPrincipalName = userPrincipalName.toLowerCase();
		}
	}

	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
	}

	public DcemRole getDcemRole() {
		return dcemRole;
	}

	public void setDcemRole(DcemRole dcemRole) {
		this.dcemRole = dcemRole;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

	public String getInitialPassword() {
		return initialPassword;
	}

	public void setInitialPassword(String initialPassword) {
		this.initialPassword = initialPassword;
	}

	public byte[] getHmac() {
		return hmac;
	}

	public void setHmac(byte[] hmac) {
		this.hmac = hmac;
	}

	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	@Transient
	public String getAccountName() {
		int ind = loginId.indexOf(DcemConstants.DOMAIN_SEPERATOR);
		if (ind == -1) {
			return loginId;
		}
		return loginId.substring(ind + 1);
	}

	public String getUserPrincipalName() {
		return userPrincipalName;
	}

	public void setUserPrincipalName(String userPrincipalName) {
		this.userPrincipalName = userPrincipalName;
	}

	public byte[] getObjectGuid() {
		return objectGuid;
	}

	public void setObjectGuid(byte[] objectGuid) {
		this.objectGuid = objectGuid;
	}

	public void setImmutableId(String immutableId) {
		this.immutableId = immutableId;
	}

	@Transient
	public String getImmutableId() {
		if (objectGuid != null && immutableId == null) {
			immutableId = Base64.getEncoder().encodeToString(objectGuid);
		}
		return immutableId;
	}

	private static String prefixZeros(int value) {
		if (value <= 0xF) {
			StringBuilder sb = new StringBuilder("0");
			sb.append(Integer.toHexString(value));
			return sb.toString();
		} else {
			return Integer.toHexString(value);
		}
	}

	@Transient
	public String getObjectGuidString() {
		if (objectGuid == null || objectGuid.length < 16) {
			return "";
		}
		StringBuilder displayStr = new StringBuilder();
		displayStr.append(prefixZeros((int) objectGuid[3] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[2] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[1] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[0] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGuid[5] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[4] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGuid[7] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[6] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGuid[8] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[9] & 0xFF));
		displayStr.append("-");
		displayStr.append(prefixZeros((int) objectGuid[10] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[11] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[12] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[13] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[14] & 0xFF));
		displayStr.append(prefixZeros((int) objectGuid[15] & 0xFF));
		return displayStr.toString();
	}

	@Override
	public String getRowStyle() {
		if (disabled) {
			return "rowWarningClass";
		} else if (acSuspendedTill != null) {
			return "rowFailureClass";
		}
		return super.getRowStyle();
	}

	public DcemUserExtension getDcemUserExt() {
		return dcemUserExt;
	}

	public void setDcemUserExt(DcemUserExtension dcemUserExt) {
		this.dcemUserExt = dcemUserExt;
	}

	public DcemLdapAttributes getDcemLdapAttributes() {
		return dcemLdapAttributes;
	}

	public void setDcemLdapAttributes(DcemLdapAttributes dcemLdapAttributes) {
		this.dcemLdapAttributes = dcemLdapAttributes;
	}

	
	@Transient
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public DcemUserExtension getDcemUserExtCountry() {
		return dcemUserExt;
	}

	public void setDcemUserExtCountry(DcemUserExtension dcemUserExtCountry) {
		this.dcemUserExt = dcemUserExtCountry;
	}

	public DcemUserExtension getDcemUserExtDepartment() {
		return dcemUserExt;
	}

	public void setDcemUserExtDepartment(DcemUserExtension dcemUserExtDepartment) {
		this.dcemUserExtDepartment = dcemUserExtDepartment;
	}

	public DcemUserExtension getDcemUserExtJobtitle() {
		return dcemUserExt;
	}

	public void setDcemUserExtJobtitle(DcemUserExtension dcemUserExtJobtitle) {
		this.dcemUserExtJobtitle = dcemUserExtJobtitle;
	}
	
	public String getDepartmentName() {
		if (dcemUserExt != null && dcemUserExt.getDepartment() != null) {
			return dcemUserExt.getDepartment().getName();
		}
		return null;
	}
	
	public DepartmentEntity getDepartment() {
		if (dcemUserExt != null && dcemUserExt.getDepartment() != null) {
			return dcemUserExt.getDepartment();
		}
		return null;
	}
	
	public String getJobTitle() {
		if (dcemUserExt != null) {
			return dcemUserExt.getJobTitle();
		}
		return null;
	}

	@Override
	public byte[] getPhoto() {
		if (photo != null) {
			return photo;
		}
		if (getDcemUserExt() == null) {
			return null;
		}
		photo = dcemUserExt.getPhoto();
		return photo;
	}
	
	public boolean isHeadOf() {
		return (dcemUserExt != null && dcemUserExt.getDepartment() != null
				&& Objects.equals(dcemUserExt.getDepartment().getHeadOf(), this));
	}

}