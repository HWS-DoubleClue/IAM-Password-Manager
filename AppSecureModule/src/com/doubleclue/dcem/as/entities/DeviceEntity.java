package com.doubleclue.dcem.as.entities;

import java.util.Date;

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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.logic.DeviceStatus;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;

/**
 *  
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "as_device", uniqueConstraints = @UniqueConstraint(name = "UK_DEVICE_USER", columnNames = { "userId", "name" }), indexes = {
		@Index(name = "IDX_DEVICE_LAST_LOGIN", columnList = "lastLogin, dc_state"), @Index(name = "IDX_DEVICE_USER", columnList = "userId") })
// Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
// @Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
		@NamedQuery(name = DeviceEntity.GET_USER_DEVICES_IDS, query = "SELECT device.id FROM DeviceEntity device where device.user = ?1 and device.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled"),
		@NamedQuery(name = DeviceEntity.GET_USER_DEVICES_NAMES, query = "SELECT device.name FROM DeviceEntity device where device.user = ?1 and device.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled"),
		@NamedQuery(name = DeviceEntity.GET_DEVICE_BY_NAME, query = "SELECT device FROM DeviceEntity device where device.user = ?1 and device.name= ?2 and device.state = ?3"),
		@NamedQuery(name = DeviceEntity.GET_DEVICE_BY_NAME_USER, query = "SELECT device.name FROM DeviceEntity device where device.user = ?1 and device.name LIKE ?2"),
		@NamedQuery(name = DeviceEntity.GET_DEVICE_ON_BY_USER, query = "SELECT device FROM DeviceEntity device where device.user = ?1 and (device.status = com.doubleclue.dcem.as.logic.DeviceStatus.Online "
				+ "OR device.status = com.doubleclue.dcem.as.logic.DeviceStatus.OnlinePasswordLess) ORDER BY device.lastLoginTime DESC"),
		@NamedQuery(name = DeviceEntity.GET_ALL_USER_DEVICES, query = "SELECT device FROM DeviceEntity device where device.user = ?1"),
		@NamedQuery(name = DeviceEntity.GET_DEVICES_BY_USER, query = "SELECT NEW com.doubleclue.dcem.as.logic.DevicesUserDto(d.id, d.status, d.nodeId) FROM DeviceEntity d where d.user = ?1 and d.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled ORDER BY d.lastLoginTime DESC"),
		@NamedQuery(name = DeviceEntity.GET_DEVICES_OFF_BY_USER, query = "SELECT NEW com.doubleclue.dcem.as.logic.DevicesUserDtoOffline (dv.id, dv.udid, dv.offlineKey) FROM DeviceEntity dv where dv.user = ?1 and dv.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled ORDER BY dv.lastLoginTime DESC"),
		@NamedQuery(name = DeviceEntity.RESET_DEVICES_STATUS, query = "UPDATE DeviceEntity d SET d.status = com.doubleclue.dcem.as.logic.DeviceStatus.Offline WHERE d.nodeId = ?1 AND (d.status = com.doubleclue.dcem.as.logic.DeviceStatus.Online OR d.status = com.doubleclue.dcem.as.logic.DeviceStatus.Suspend)"),

		@NamedQuery(name = DeviceEntity.GET_DISTINCT_USER_COUNT, query = "SELECT COUNT(DISTINCT d.user) FROM DeviceEntity d WHERE d.lastLoginTime > ?1 AND d.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled"),
		@NamedQuery(name = DeviceEntity.GET_DEVICES_COUNT_FOR_USER, query = "SELECT COUNT(d) FROM DeviceEntity d WHERE d.user = ?1 AND d.state = com.doubleclue.dcem.as.logic.DeviceState.Enabled"),
		@NamedQuery(name = DeviceEntity.DELETE_USER_DEVICES, query = "DELETE FROM DeviceEntity rp where rp.user = ?1"), })
public class DeviceEntity extends EntityInterface {

	public static final String GET_USER_DEVICES_IDS = "DeviceEntity.devicesId";
	public static final String GET_USER_DEVICES_NAMES = "DeviceEntity.devicesName";
	public static final String GET_DEVICE_BY_NAME = "DeviceEntity.deviceByName";
	public static final String GET_DEVICE_BY_NAME_USER = "DeviceEntity.deviceByNameUser";
	public static final String RESET_DEVICES_STATUS = "DeviceEntity.resetdevicesStatus";
	public static final String GET_DEVICE_ON_BY_USER = "DeviceEntity.deviceOnByUser";
	public static final String GET_DEVICES_BY_USER = "DeviceEntity.devicesByUser";
	public static final String GET_DEVICES_OFF_BY_USER = "DeviceEntity.devicesOffByUser";
	public static final String GET_DISTINCT_USER_COUNT = "DeviceEntity.distinctUser";
	public static final String GET_DEVICES_COUNT_FOR_USER = "DeviceEntity.devicesCountForUser";
	public static final String DELETE_USER_DEVICES = "DeviceEntity.deleteUserDevices";
	public static final String GET_ALL_USER_DEVICES = "DeviceEntity.allUserDevices";;

	// private static Logger logger = LogManager.getLogger(AppDevice.class);

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreAppDevice", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "APP_DEVICE.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreAppDevice")
	private Integer id;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne
	@JoinColumn(nullable = false, name = "userId", foreignKey = @ForeignKey(name = "FK_APP_DEVICE_USER"), insertable = true, updatable = false)
	private DcemUser user;

	@DcemGui
	@Column(length = 64)
	private String name;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_state", nullable = false)
	@DcemGui
	DeviceState state;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	@DcemGui
	DeviceStatus status;

	@DcemGui(name = "platform", subClass = "clientType", dbMetaAttributeName = "asVersion")
	@Transient
	private AsVersionEntity asVersionPlatform; // this is only a dummy which returns asVersion

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_DEVICE_VERSION"), insertable = true, updatable = true)
	@DcemGui(name = "version", subClass = "versionStr")
	private AsVersionEntity asVersion;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "lastLogin", nullable = true)
	@DcemGui
	private Date lastLoginTime;

	@Column(length = 255)
	@DcemGui
	private String appOsVersion;

	@Column(length = 255, updatable = false)
	@DcemGui(visible = false)
	private String manufacture;

	@Column(length = 255)
	private String risks;

	@Column(length = 2)
	@DcemGui
	private String locale;

	@DcemGui
	private int retryCounter = 0;

	@Column(length = 255, updatable = false)
	private byte[] udid;

	@Column(length = 255, updatable = false)
	private byte[] deviceHash;

	private int offlineCounter = 0;

	@Column(name = "offlineKey", nullable = true, updatable = false)
	@Convert(converter = DbEncryptConverterBinary.class)
	private byte[] offlineKey;

	@Column(length = 255, updatable = false)
	@Convert(converter = DbEncryptConverterBinary.class)
	private byte[] deviceKey;

	@Column(length = 1024, updatable = false)
	private byte[] publicKey;

	// @ManyToOne (fetch = FetchType.LAZY)
	// @JoinColumn(name="nodeId", nullable = true, insertable = true, updatable = true)
	@DcemGui
	@Column(name = "nodeId")
	private Integer nodeId;

	public DeviceEntity() {
	}

	public Date getLastLoginTime() {
		return this.lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getAppOsVersion() {
		return appOsVersion;
	}

	public void setAppOsVersion(String appOsVersion) {
		this.appOsVersion = appOsVersion;
	}

	public String getRisks() {
		return risks;
	}

	public void setRisks(String risks) {
		this.risks = risks;
	}

	public DeviceState getState() {
		return state;
	}

	public void setState(DeviceState state) {
		this.state = state;
	}

	public byte[] getDeviceKey() {
		return deviceKey;
	}

	public void setDeviceKey(byte[] deviceKey) {
		this.deviceKey = deviceKey;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public int getRetryCounter() {
		return retryCounter;
	}

	public void setRetryCounter(int retryCounter) {
		this.retryCounter = retryCounter;
	}

	public byte[] getDeviceHash() {
		return deviceHash;
	}

	public void setDeviceHash(byte[] deviceHash) {
		this.deviceHash = deviceHash;
	}

	public int getOfflineCounter() {
		return offlineCounter;
	}

	public void setOfflineCounter(int offlineCounter) {
		this.offlineCounter = offlineCounter;
	}

	public byte[] getOfflineKey() {
		return offlineKey;
	}

	public void setOfflineKey(byte[] offlineKey) {
		this.offlineKey = offlineKey;
	}

	public AsVersionEntity getAsVersion() {
		return asVersion;
	}

	public void setAsVersion(AsVersionEntity asVersion) {
		this.asVersion = asVersion;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public AsVersionEntity getAsVersion1() { // dummy
		return asVersion;
	}

	public DeviceStatus getStatus() {
		return status;
	}

	public void setStatus(DeviceStatus status) {
		this.status = status;
	}

	public String getManufacture() {
		return manufacture;
	}

	public void setManufacture(String manufacture) {
		this.manufacture = manufacture;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLoginId() {
		return user.getLoginId();
	}

	public byte[] getUdid() {
		return udid;
	}

	public void setUdid(byte[] udid) {
		this.udid = udid;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String toString() {
		return user.getLoginId() + "-" + name;
	}

	// public DcemNode getDcemNode() {
	// return dcemNode;
	// }
	//
	// public void setDcemNode(DcemNode dcemNode) {
	// this.dcemNode = dcemNode;
	// }

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

	public AsVersionEntity getAsVersionPlatform() {
		return asVersion;
	}

	public void setAsVersionPlatform(AsVersionEntity asVersionPlatform) {
		this.asVersion = asVersionPlatform;
	}

	@Transient
	public String getDeviceType() {
		if (asVersion == null) {
			return "";
		}
		return asVersion.getClientType().name();
	}

	@Transient
	public boolean isOnline() {
		return status == DeviceStatus.Online;
	}

	@Transient
	public boolean isEnabled() {
		return state == DeviceState.Enabled;
	}

}