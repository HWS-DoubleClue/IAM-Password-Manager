package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
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
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;
import com.doubleclue.dcem.core.jpa.DbEncryptConverter;
import com.doubleclue.dcem.core.logic.DomainType;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The persistent class for the user database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "core_ldap", uniqueConstraints = @UniqueConstraint(name = "UK_LDAP_NAME", columnNames = { "name" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQuery(name = DomainEntity.GET_ALL, query = "select ldap from DomainEntity as ldap WHERE ldap.enable = true ORDER BY ldap.rank ASC")
public class DomainEntity extends EntityInterface implements Serializable {

	public final static String GET_ALL = "LdapEntity.getAll";
	
	private static final Logger logger = LogManager.getLogger(DomainEntity.class);

	@Id
	@TableGenerator(name = "coreSeqStoreLdap", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "LDAP.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreLdap")
	@Column(name = "dc_id")
	private Integer id;

	@Column(name = "dc_rank")
	@DcemGui(styleClass = "shortInput", sortOrder = SortOrder.ASCENDING)
	private int rank;

	@Column(name = "name", length = 64, nullable = false)
	@NotNullOrEmptyString(message = "{ldapdialog.name}")
	@Size(min = 2, max = 64)
	@DcemGui(columnWidth = "120px")
	private String name;

	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	@DcemGui
	private DomainType domainType = DomainType.Active_Directory;

	@Column(length = 255, nullable = false)
	@NotNullOrEmptyString(message = "{ldapdialog.host}")
	@Size(min = 4, max = 256)
	@DcemGui(name = "URL / TenantID", styleClass = "xlongInput")
	private String host = "ldap://";


	@Column(length = 255, nullable = false)
	// @DcemGui(styleClass= "xlongInput")
	private String baseDN;

	@Column(length = 255, nullable = false)
	@DcemGui(styleClass = "xlongInput", name = "Search Account / ClientID")
	private String searchAccount;

	@Column(length = 255, nullable = false)
	@Size(min = 2, max = 255)
	@Convert(converter = DbEncryptConverter.class)
	@DcemGui(displayMode = DisplayModes.INPUT_ONLY, password = true, name = "Search-Account Password")
	private String password;

	@Column(length = 255, nullable = false)
	@DcemGui(styleClass = "xlongInput", displayMode = DisplayModes.INPUT_ONLY)
	@Size(min = 2, max = 255)
	private String filter = "(&(objectCategory=Person)(sAMAccountName=*))";

	@Column(length = 255, nullable = false)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	@Size(min = 2, max = 255)
	private String loginAttribute = "sAMAccountName";

	@Column(length = 255, nullable = false)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	@Size(min = 2, max = 255)
	private String firstNameAttribute = "givenName";

	@Column(length = 255, nullable = false)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	@Size(min = 2, max = 255)
	private String lastNameAttribute = "sn";

	@Column(length = 255, nullable = true)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	@Size(min = 2, max = 255)
	private String mailAttribute = "mail";

	@Column(length = 255, nullable = true)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	private String telephoneAttribute = "telephonenumber";

	@Column(length = 255, nullable = true)
	@DcemGui(styleClass = "mediumInput", displayMode = DisplayModes.INPUT_ONLY)
	private String mobileAttribute = "mobile";

//	@Column(name = "timeout")
//	@DcemGui(styleClass = "mediumInput")
//	private int timeoutInSec = 10;

	@Column(length = 255, nullable = true)
	@DcemGui(styleClass = "mediumInput")
	private String mapEmailDomains = "";
	
	@Column(length = 4096, nullable = true)
	private String configJson;
	
	@Transient
	private DomainConfig domainConfig;


	/*
	 * @Column(length = 4096, nullable = true, name = "azureAdConfig")
	 * 
	 * @Convert(converter = DbJsonConverterClass.class) private AzureAdConfig
	 * azureAdConfig;
	 */

	@DcemGui
	private boolean enable = true;

	@Version
	@Column(name = "dc_version")
	private int version;

	public DomainEntity() {
	}
	
	@Transient
	Set<String> setOfEmailDomains;

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Transient
	public String getTenantId() {
		return host;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public void setBaseDN(String baseDN) {
		this.baseDN = baseDN;
	}
	
	@Transient
	public String getClientId() {
		return searchAccount;
	}
	
	@Transient
	public String getClientSecret() {
		return password;
	}

	public String getSearchAccount() {
		return searchAccount;
	}

	public void setSearchAccount(String searchAccount) {
		this.searchAccount = searchAccount;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getLoginAttribute() {
		return loginAttribute;
	}

	public void setLoginAttribute(String loginAttribute) {
		this.loginAttribute = loginAttribute;
	}

	public String getFirstNameAttribute() {
		return firstNameAttribute;
	}

	public void setFirstNameAttribute(String firstNameAttribute) {
		this.firstNameAttribute = firstNameAttribute;
	}

	public String getLastNameAttribute() {
		return lastNameAttribute;
	}

	public void setLastNameAttribute(String lastNameAttribute) {
		this.lastNameAttribute = lastNameAttribute;
	}

	public String getMailAttribute() {
		return mailAttribute;
	}

	public void setMailAttribute(String mailAttribute) {
		this.mailAttribute = mailAttribute;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

//	public int getTimeoutInSec() {
//		return timeoutInSec;
//	}
//
//	public void setTimeoutInSec(int connectTimeoutInSeconds) {
//		this.timeoutInSec = connectTimeoutInSeconds;
//	}

	public String getTelephoneAttribute() {
		return telephoneAttribute;
	}

	public void setTelephoneAttribute(String telephoneAttribute) {
		this.telephoneAttribute = telephoneAttribute;
	}

	public String getMobileAttribute() {
		return mobileAttribute;
	}

	public void setMobileAttribute(String mobileAttribute) {
		this.mobileAttribute = mobileAttribute;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public String toString() {
		return name;
	}

	public DomainType getDomainType() {
		return domainType;
	}

	public void setDomainType(DomainType domainType) {
		this.domainType = domainType;
	}

	public String getMapEmailDomains() {
		return mapEmailDomains;
	}

	public void setMapEmailDomains(String mapEmailDomains) {
		this.mapEmailDomains = mapEmailDomains;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public Set<String> getSetOfEmailDomains() {
		return setOfEmailDomains;
	}

	public void setSetOfEmailDomains(Set<String> setOfEmailDomains) {
		this.setOfEmailDomains = setOfEmailDomains;
	}
	

	public DomainConfig getDomainConfig() {
		if (domainConfig == null) {
			domainConfig = new DomainConfig();
			if (configJson != null && configJson.isEmpty() == false) {
				try {
					TypeReference<DomainConfig> typeRef = new TypeReference<DomainConfig>() {
					};
					ObjectMapper objectMapper = new ObjectMapper();
					domainConfig = objectMapper.readValue(configJson, typeRef);
				} catch (Exception e) {
					logger.warn("Couldn't deserialize Domain Config for " + getName(), e);
				}
			}
		}
		return domainConfig;
	}

	public void serializeDomainConfig() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			configJson = objectMapper.writeValueAsString(domainConfig);
		} catch (Exception e) {
			logger.warn("Couldn't serialize SP IdP Settings", e);
		}
	}

	public String getConfigJson() {
		return configJson;
	}

	public void setConfigJson(String configJson) {
		this.configJson = configJson;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseDN == null) ? 0 : baseDN.hashCode());
		result = prime * result + ((configJson == null) ? 0 : configJson.hashCode());
		result = prime * result + ((domainType == null) ? 0 : domainType.hashCode());
		result = prime * result + (enable ? 1231 : 1237);
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + ((firstNameAttribute == null) ? 0 : firstNameAttribute.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastNameAttribute == null) ? 0 : lastNameAttribute.hashCode());
		result = prime * result + ((loginAttribute == null) ? 0 : loginAttribute.hashCode());
		result = prime * result + ((mailAttribute == null) ? 0 : mailAttribute.hashCode());
		result = prime * result + ((mapEmailDomains == null) ? 0 : mapEmailDomains.hashCode());
		result = prime * result + ((mobileAttribute == null) ? 0 : mobileAttribute.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + rank;
		result = prime * result + ((searchAccount == null) ? 0 : searchAccount.hashCode());
		result = prime * result + ((setOfEmailDomains == null) ? 0 : setOfEmailDomains.hashCode());
		result = prime * result + ((telephoneAttribute == null) ? 0 : telephoneAttribute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DomainEntity other = (DomainEntity) obj;
		if (baseDN == null) {
			if (other.baseDN != null)
				return false;
		} else if (!baseDN.equals(other.baseDN))
			return false;
		if (configJson == null) {
			if (other.configJson != null)
				return false;
		} else if (!configJson.equals(other.configJson))
			return false;
		if (domainType != other.domainType)
			return false;
		if (enable != other.enable)
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (firstNameAttribute == null) {
			if (other.firstNameAttribute != null)
				return false;
		} else if (!firstNameAttribute.equals(other.firstNameAttribute))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lastNameAttribute == null) {
			if (other.lastNameAttribute != null)
				return false;
		} else if (!lastNameAttribute.equals(other.lastNameAttribute))
			return false;
		if (loginAttribute == null) {
			if (other.loginAttribute != null)
				return false;
		} else if (!loginAttribute.equals(other.loginAttribute))
			return false;
		if (mailAttribute == null) {
			if (other.mailAttribute != null)
				return false;
		} else if (!mailAttribute.equals(other.mailAttribute))
			return false;
		if (mapEmailDomains == null) {
			if (other.mapEmailDomains != null)
				return false;
		} else if (!mapEmailDomains.equals(other.mapEmailDomains))
			return false;
		if (mobileAttribute == null) {
			if (other.mobileAttribute != null)
				return false;
		} else if (!mobileAttribute.equals(other.mobileAttribute))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (rank != other.rank)
			return false;
		if (searchAccount == null) {
			if (other.searchAccount != null)
				return false;
		} else if (!searchAccount.equals(other.searchAccount))
			return false;
		if (setOfEmailDomains == null) {
			if (other.setOfEmailDomains != null)
				return false;
		} else if (!setOfEmailDomains.equals(other.setOfEmailDomains))
			return false;
		if (telephoneAttribute == null) {
			if (other.telephoneAttribute != null)
				return false;
		} else if (!telephoneAttribute.equals(other.telephoneAttribute))
			return false;
		return true;
	}
}