package com.doubleclue.dcem.as.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
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
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.comm.thrift.ClientType;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.VersionValidator;

/**
 * The persistent class for the app_version database table.
 * 
 */
@Entity
@Table(name = "as_version", uniqueConstraints = @UniqueConstraint(name = "UK_VERSION_NAME_TYPE", columnNames = { "dc_name", "versionStr", "clientType" }))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
		@NamedQuery(name = AsVersionEntity.GET_VERSION, query = "SELECT vr FROM AsVersionEntity vr where vr.name = ?1 AND vr.version = ?2 "
				+ "AND vr.clientType = ?3 AND ((vr.disabled IS NULL AND ?4 = FALSE) OR (vr.disabled IS NOT NULL AND vr.disabled = ?4))"),
		@NamedQuery(name = AsVersionEntity.GET_VERSIONS, query = "SELECT vr FROM AsVersionEntity vr where vr.disabled = false "),
		@NamedQuery(name = AsVersionEntity.GET_UNIQUE_VERSION, query = "SELECT vr FROM AsVersionEntity vr where vr.name = ?1 AND vr.version = ?2 "
				+ "AND vr.clientType = ?3 "),
		@NamedQuery(name = AsVersionEntity.RESET_USER_VERSION, query = "UPDATE AsVersionEntity vr SET vr.user= null WHERE vr.user = ?1"),
		// @NamedQuery(name = AsVersionEntity.DELETE_BY_USER, query = "DELETE FROM AsVersionEntity vr WHERE vr.user = ?1"),
		// @NamedQuery(name = AsVersionEntity.GET_BY_USER, query = "SELECT vr FROM AsVersionEntity vr WHERE vr.user = ?1")

})
public class AsVersionEntity extends EntityInterface implements Cloneable {

	public final static String GET_VERSION = "AsVersion.getVersion";
	public final static String GET_UNIQUE_VERSION = "AsVersion.getUniqueVersion";
	public final static String RESET_USER_VERSION = "AsVersion.resetUserVersion";
	// public final static String DELETE_BY_USER = "AsVersion.deleteByUser";
	// public final static String GET_BY_USER = "AsVersion.getByUser";
	public static final String GET_VERSIONS = "AsVersion.versions";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAppVersion", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "APP_VERSION.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAppVersion")
	private Integer id;

	@Column(name = "dc_name", length = 128)
	@DcemGui(name = "appName")
	private String name;

	@Enumerated(EnumType.ORDINAL)
	@DcemGui
	private ClientType clientType;

	@Column(length = 128)
	@DcemGui(name = "versionNo")
	@VersionValidator
	private String versionStr;

	// @DcemGui(name = "regUser", subClass = "loginId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_VERSION_USER"), insertable = true, updatable = true)
	private DcemUser user;

	@DcemGui
	LocalDateTime expiresOn;

	@Column(length = 255)
	private String downloadUrl;

	@Column(length = 255)
	@DcemGui
	private String informationUrl;

	@Column(name = "as_version")
	private int version;

	@Column(name = "dc_disabled")
	@DcemGui
	private Boolean disabled = false;

	@Column(name = "testApp")
	@DcemGui
	private Boolean testApp = false;

	@Version
	private int jpaVersion;

	public String getDownloadUrl() {
		return this.downloadUrl;
	}

	public void setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public int getJpaVersion() {
		return this.jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public int getVersion() {
		return this.version;
	}

	public String getVersionStr() {
		return this.versionStr;
	}

	public void setVersionStr(String versionStr) {
		this.versionStr = versionStr;
	}

	public String toString() {
		return name + "-" + versionStr;
	}

	public String getInformationUrl() {
		return informationUrl;
	}

	public void setInformationUrl(String informationUrl) {
		this.informationUrl = informationUrl;
	}

	public String toStringFull() {
		return "AsmVersion [versionId=" + id + ",  appName=" + name + ", downloadUrl=" + (downloadUrl != null ? downloadUrl : "null") + ", informationUrl="
				+ (informationUrl != null ? informationUrl : "null") + ",  disabled=" + disabled + ", version=" + version + ", versionStr=" + versionStr
				+ ", asClinetType=" + (clientType != null ? clientType.name() : "null") + ", allowedToRegisterAppDigestUID";
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public boolean isDisabled() {
		if (disabled == null) {
			disabled = Boolean.FALSE;
		}
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public LocalDateTime getExpiresOn() {
		return expiresOn;
	}

	public void setExpiresOn(LocalDateTime expiresOn) {
		this.expiresOn = expiresOn;
	}

	@Transient
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public boolean isTestApp() {
		if (testApp == null) {
			testApp = false;
		}
		return testApp;
	}

	public void setTestApp(boolean testApp) {
		this.testApp = testApp;
	}

}