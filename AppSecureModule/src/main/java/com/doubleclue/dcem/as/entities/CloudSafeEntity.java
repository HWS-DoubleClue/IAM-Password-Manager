package com.doubleclue.dcem.as.entities;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.persistence.CascadeType;
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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;


import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The persistent class for the Registered Version
 * 
 */
@Entity
@Table(name = "as_cloudsafe", uniqueConstraints = @UniqueConstraint(name = "UK_AS_CLOUDDATA", columnNames = { "dc_name", "owner", "user_dc_id", "device_dc_id",
		"dc_parent_id", "group_dc_id" }))
@NamedQueries({
		@NamedQuery(name = CloudSafeEntity.GET_GLOBAL_CLOUDDATA, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.GLOBAL AND c.name=?1"),
		@NamedQuery(name = CloudSafeEntity.GET_USER_CLOUDDATA, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.name=?1 AND c.user=?2 AND c.parent.id=?3"),
		@NamedQuery(name = CloudSafeEntity.GET_GROUP_CLOUDDATA, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.GROUP AND c.name=?1 AND c.group=?2 AND c.parent.id=?3"),
		@NamedQuery(name = CloudSafeEntity.GET_CLOUDSAFE_ROOT, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.name=?1 AND c.user=?2 AND c.parent = c.id"),
		@NamedQuery(name = CloudSafeEntity.GET_CLOUDSAFE_IN_RECYCLEBIN, query = "SELECT c FROM CloudSafeEntity c WHERE c.name=?1 AND c.user.loginId=?2 AND c.recycled=true"),
		@NamedQuery(name = CloudSafeEntity.GET_DEVICE_CLOUDDATA, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.DEVICE AND c.name=?1 AND c.device=?2"),
		@NamedQuery(name = CloudSafeEntity.GET_DEVICE_LIST, query = "SELECT cloudData FROM CloudSafeEntity cloudData WHERE cloudData.device=?1"),

		@NamedQuery(name = CloudSafeEntity.GET_DEVICES_CLOUDDATA_IN, query = "SELECT cloudSafe.id FROM CloudSafeEntity cloudSafe "
				+ "WHERE cloudSafe.owner=com.doubleclue.comm.thrift.CloudSafeOwner.DEVICE AND cloudSafe.name=?1 AND cloudSafe.device IN (?2)"),
		@NamedQuery(name = CloudSafeEntity.GET_OWNED_FILE_KEYS, query = "SELECT c FROM CloudSafeEntity c WHERE c.name LIKE ?1 AND (c.discardAfter IS NULL OR c.discardAfter > ?2) "
				+ "AND c.user=?3 AND c.owner=?4 AND (c.lastModified IS NULL OR c.lastModified>?5)"),
		@NamedQuery(name = CloudSafeEntity.GET_USER_FILE_LIST, query = "SELECT c FROM CloudSafeEntity c WHERE c.name LIKE ?1 AND (c.discardAfter IS NULL OR c.discardAfter > ?2) AND c.name != 'Recycle Bin' AND c.recycled = false "
				+ "AND ((c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.user=?3) OR (c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.GROUP AND c.group IN ?4)) AND (c.lastModified IS NULL OR c.lastModified>?5) AND c.isFolder=?6"),
		@NamedQuery(name = CloudSafeEntity.GET_EXPIRED_DATA, query = "SELECT c FROM CloudSafeEntity c WHERE c.discardAfter < ?1"),
		@NamedQuery(name = CloudSafeEntity.GET_ALL_USER_CLOUDSAFE_WITH_GROUP, query = "SELECT c FROM CloudSafeEntity c WHERE c.name!='_ROOT_' AND (c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.user=?1) OR (c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.GROUP AND c.group IN ?2) ORDER BY c.isFolder DESC, c.name ASC "),
		@NamedQuery(name = CloudSafeEntity.GET_ALL_USER_CLOUDSAFE, query = "SELECT c FROM CloudSafeEntity c WHERE c.name!='_ROOT_' AND (c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.user=?1) ORDER BY c.isFolder DESC, c.name ASC "),
		@NamedQuery(name = CloudSafeEntity.GET_USER_CLOUDSAFE_BY_NAME, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.user=?1 AND c.name=?2 AND c.parent=?2"),
		@NamedQuery(name = CloudSafeEntity.GET_USER_TOTAL_EXCLUDING_ENTITY, query = "SELECT SUM(c.length) FROM CloudSafeEntity c INNER JOIN c.user u WHERE u.id = ?1 AND c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND (c.discardAfter IS NULL OR c.discardAfter > ?2) AND c.id <> ?3 GROUP BY u.id"),
		@NamedQuery(name = CloudSafeEntity.DELETE_CLOUD_SAFE_BY_ID, query = "DELETE FROM CloudSafeEntity c WHERE c.id = ?1"),
		@NamedQuery(name = CloudSafeEntity.DELETE_CLOUD_SAFE_BY_OWNER_GROUP, query = "DELETE FROM CloudSafeEntity c WHERE c.group.id = ?1"),
		@NamedQuery(name = CloudSafeEntity.SELECT_CLOUD_SAFE_FOLDER_STRUCTURE, query = "SELECT NEW com.doubleclue.dcem.as.logic.CloudSafeDto(c.id, c.isFolder) FROM CloudSafeEntity c WHERE c.parent.id=?1 AND c.user=?2"),
		@NamedQuery(name = CloudSafeEntity.SET_FOLDER_NAME, query = "UPDATE CloudSafeEntity c SET c.name = ?1 WHERE c.id = ?2"),
		@NamedQuery(name = CloudSafeEntity.MOVE_ENTRY, query = "UPDATE CloudSafeEntity c SET c.options = ?3 , c.parent.id = ?1 WHERE c.id = ?2"),
		@NamedQuery(name = CloudSafeEntity.UPDATE_ENTRIES_TO_ROOT, query = "UPDATE CloudSafeEntity c SET c.parent = ?1 WHERE c.parent IS NULL"),
		@NamedQuery(name = CloudSafeEntity.UPDATE_ENTRIES_TO_ROOT_DEVICE, query = "UPDATE CloudSafeEntity c SET c.device = ?1 WHERE c.device IS NULL"),
		@NamedQuery(name = CloudSafeEntity.GET_CLOUDSAFE_BY_ID, query = "SELECT NEW com.doubleclue.dcem.as.logic.CloudSafeNameDto(cs.id, cs.name, cs.parent.id) FROM CloudSafeEntity cs WHERE cs.id=?1"),
		@NamedQuery(name = CloudSafeEntity.GET_CLOUDSAFE_BY_ID_AND_RECYCLE_STATE, query = "SELECT cs FROM CloudSafeEntity cs WHERE cs.id=?1 AND cs.recycled=?2"),
		@NamedQuery(name = CloudSafeEntity.GET_IDS, query = "SELECT c.id FROM CloudSafeEntity c"),
		@NamedQuery(name = CloudSafeEntity.GET_USER_CLOUDSAFE_DATA, query = "SELECT c FROM CloudSafeEntity c WHERE (c.parent.id=?1 AND c.name!='_ROOT_') AND ((c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.user=?2) OR (c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.GROUP AND c.group IN ?3)) ORDER BY c.isFolder DESC, c.name ASC"),
		@NamedQuery(name = CloudSafeEntity.GET_SINGLE_CLOUDSAFE_FILE, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.name=?1 AND c.parent.id=?2 AND c.isFolder=?3 AND c.user.id=?4"),
		@NamedQuery(name = CloudSafeEntity.GET_SINGLE_CLOUDSAFE_FILE_WITH_NULL_PARENT, query = "SELECT c FROM CloudSafeEntity c WHERE c.owner=com.doubleclue.comm.thrift.CloudSafeOwner.USER AND c.name=?1 AND c.parent.id=?2 AND c.isFolder=?3 AND c.user.id=?4"),
		@NamedQuery(name = CloudSafeEntity.UPDATE_LAST_MODIFY_STATE_BY_USER, query = "UPDATE CloudSafeEntity c SET c.lastModifiedUser = NULL WHERE c.lastModifiedUser = ?1"),

})
public class CloudSafeEntity extends EntityInterface implements Cloneable {

	public final static String GET_GLOBAL_CLOUDDATA = "CloudSafeEntity.getGlobalProperty";
	public final static String GET_USER_CLOUDDATA = "CloudSafeEntity.getUserProperty";
	public final static String GET_GROUP_CLOUDDATA = "CloudSafeEntity.getGroupProperty";
	public final static String GET_CLOUDSAFE_ROOT = "CloudSafeEntity.getRoot";
	public final static String GET_CLOUDSAFE_IN_RECYCLEBIN = "CloudSafeEntity.getCloudSafeInRecycleBin";
	public final static String GET_DEVICE_CLOUDDATA = "CloudSafeEntity.getDeviceProperty";
	public static final String GET_DEVICES_CLOUDDATA_IN = "CloudSafeEntity.getDevicePropertyValue";
	public static final String GET_OWNED_FILE_KEYS = "CloudSafeEntity.getOwnedFileKeys";
	public static final String GET_USER_FILE_LIST = "CloudSafeEntity.getUserFileList";
	public static final String GET_DEVICE_LIST = "CloudSafeEntity.getDeviceList";
	public static final String GET_EXPIRED_DATA = "CloudSafeEntity.getExpiredData";
	public static final String GET_ALL_USER_CLOUDSAFE_WITH_GROUP = "CloudSafeEntity.getAllUserCloudSafeWithGroup";
	public static final String GET_ALL_USER_CLOUDSAFE = "CloudSafeEntity.getAllUserCloudSafe";
	public static final String GET_USER_CLOUDSAFE_BY_NAME = "CloudSafeEntity.getUserCloudSafeByName";
	public static final String GET_USER_TOTAL_EXCLUDING_ENTITY = "CloudSafeEntity.getUserTotalExcludingEntity";
	public static final String DELETE_CLOUD_SAFE_BY_ID = "CloudSafeEntity.deleteCloudSafeById";
	public static final String DELETE_CLOUD_SAFE_BY_OWNER_GROUP = "CloudSafeEntity.deleteCloudSafeByGroupOwnerId";
	public static final String SELECT_CLOUD_SAFE_FOLDER_STRUCTURE = "CloudSafeEntity.selectCloudSafeFolderStructure";
	public static final String SET_FOLDER_NAME = "CloudSafeEntity.setFolderName";
	public static final String MOVE_ENTRY = "CloudSafeEntity.moveEntry";
	public static final String GET_CLOUDSAFE_BY_ID = "CloudSafeEntity.getParentById";
	public static final String GET_CLOUDSAFE_BY_ID_AND_RECYCLE_STATE = "CloudSafeEntity.getParentByIdAndRecycleState ";
	public static final String GET_IDS = "CloudSafeEntity.getIds";
	public static final String GET_USER_CLOUDSAFE_DATA = "CloudSafeEntity.getUserCloudsafeData";
	public static final String GET_SINGLE_CLOUDSAFE_FILE = "CloudSafeEntity.getSingleCloudsafeFile";
	public static final String GET_SINGLE_CLOUDSAFE_FILE_WITH_NULL_PARENT = "CloudSafeEntity.getSingleCloudsafeFileWithNullParent";
	public static final String UPDATE_ENTRIES_TO_ROOT = "CloudSafeEntity.updateEntriesToRoot";
	public static final String UPDATE_ENTRIES_TO_ROOT_DEVICE = "CloudSafeEntity.updateEntriesToRootDevice";
	public static final String UPDATE_LAST_MODIFY_STATE_BY_USER = "CloudSafeShareEntity.updateLastMdoifyByUser";

	public CloudSafeEntity() {
		super();
	}

	public CloudSafeEntity(CloudSafeOwner owner, DcemUser user, DeviceEntity device, String name, LocalDateTime discardAfter, String options, boolean isFolder,
			CloudSafeEntity parent, DcemUser lastModifiedUser) {
		super();
		this.owner = owner;
		this.user = user;
		this.device = device;
		this.name = name;
		this.discardAfter = discardAfter;
		this.options = options;
		this.isFolder = isFolder;
		this.parent = parent;
		this.lastModifiedUser = lastModifiedUser;
	}

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "asmSeqStoreCloudData", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "AS_CLOUDDATA.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "asmSeqStoreCloudData")
	private Integer id;

	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column
	@JsonIgnore
	CloudSafeOwner owner;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_USER"), nullable = false, insertable = true, updatable = true)
	@JsonIgnore
	private DcemUser user;

	@DcemGui(name = "device", subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_DEVICE"), nullable = false, insertable = true, updatable = false)
	@JsonIgnore
	private DeviceEntity device;

	@DcemGui(name = "group", subClass = "name")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_GROUP"), name = "group_dc_id", nullable = true, insertable = true, updatable = true)
	@JsonIgnore
	private DcemGroup group;

	@DcemGui
	@Column(name = "dc_name", length = 255, nullable = false)
	String name;
	
	@DcemGui
	@Column(name = "dc_info", length = 255, nullable = true)
	String info;

	@DcemGui
	@Column(name = "dc_length")
	long length;
	
	@DcemGui
	@Column(name = "lengthTexT")
	long lengthOfText;  // if tero this document has no Text Content
	
	@Nullable
	@DcemGui
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_AS_PARENT_ID"), name = "dc_parent_id", nullable = true, insertable = true, updatable = true)
	@JsonIgnore
	private CloudSafeEntity parent;
	
	@Enumerated (EnumType.ORDINAL)
	@DcemGui
	@Column (nullable = true)
	DcemMediaType dcemMediaType;
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "as_ref_cloudsafe_tag", joinColumns = @JoinColumn(name = "group_id"), foreignKey = @ForeignKey(name = "FK_USER_GROUP"), inverseJoinColumns = @JoinColumn(name = "user_id"), inverseForeignKey = @ForeignKey(name = "FK_GROUP_USER"))
	private List<CloudSafeTagEntity> tags;

	@DcemGui(name = "last_Modified_User", subClass = "loginId")
	@ManyToOne
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_USER_MODIFIED"), nullable = true, insertable = true, updatable = true)
	@JsonIgnore
	private DcemUser lastModifiedUser;
	
	

	// @DcemGui
	// boolean sign;

	@DcemGui
	LocalDateTime lastModified;

	@DcemGui
	@Column(nullable = true)
	LocalDateTime discardAfter;

	@JsonIgnore
	String options;

	// @Column(name = "dc_signature")
	// @Convert(converter = DbEncryptConverterBinary.class)
	// byte[] signature;

	@Column(name = "dc_salt", length = 32)
	@JsonIgnore
	byte[] salt;

	@DcemGui
	@Column(name = "dc_is_folder", nullable = false, updatable = false)
	boolean isFolder = false;

	@DcemGui
	@Column(name = "dc_gcm", nullable = false)
	@JsonIgnore
	Boolean isGcm = true;

	@DcemGui
	@Column(name = "recycled", nullable = false)
	@JsonIgnore
	boolean recycled = false;

	@Transient
	@JsonIgnore
	String loginId;

	@Transient
	@JsonIgnore
	String deviceName;

	@Transient
	@JsonIgnore
	String lengthString;

	@Transient
	@JsonIgnore
	boolean writeAccess = true;

	@Transient
	@JsonIgnore
	boolean restrictDownload;

	@Transient
	@JsonIgnore
	boolean newEntity = false;

	@Transient
	@JsonIgnore
	String path;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CloudSafeOwner getOwner() {
		return owner;
	}

	public void setOwner(CloudSafeOwner owner) {
		this.owner = owner;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public DeviceEntity getDevice() {
		return device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@Transient
	public String toString() { 
		StringBuffer sb = new StringBuffer();
		sb.append("ID=");
		sb.append(id);
		sb.append(", Name=");
		sb.append(name);
		return sb.toString();
	}

	@Transient
	@JsonIgnore
	public long getDiscardAfterAsLong() {
		if (discardAfter == null) {
			return 0;
		}
		return discardAfter.toEpochSecond(ZoneOffset.UTC) * 1000;
	}

	public LocalDateTime getDiscardAfter() {
		return discardAfter;
	}

	public void setDiscardAfter(LocalDateTime discardAfter) {
		this.discardAfter = discardAfter;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	public boolean isOption(CloudSafeOptions options) {
		if (this.options == null) {
			return false;
		}
		return this.options.contains(options.name());
	}
	
	public byte[] getSalt() {
		return salt;
	}

	public void setSalt(byte[] salt) {
		this.salt = salt;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getLengthString() {
		if (lengthString == null) {
			lengthString = DataUnit.getByteCountAsString(length);
		}
		return lengthString;
	}

	public boolean isWriteAccess() {
		return writeAccess;
	}

	public void setWriteAccess(boolean writeAccess) {
		this.writeAccess = writeAccess;
	}

	@Transient
	@JsonIgnore
	public String getLengthKb() {
		if (isFolder) {
			return "-";
		} else if (length > 0) {
			return String.valueOf((length + 1024 - 1) / 1024);
		} else {
			return "0";
		}

	}

	public CloudSafeEntity getParent() {
		return parent;
	}

	public void setParent(CloudSafeEntity parent) {
		this.parent = parent;
	}

	@JsonIgnore
	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public DcemUser getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(DcemUser lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public boolean isNewEntity() {
		return newEntity;
	}

	public void setNewEntity(boolean newEntity) {
		this.newEntity = newEntity;
	}

	public boolean isRecycled() {
		return recycled;
	}

	public void setRecycled(boolean isRecycled) {
		this.recycled = isRecycled;
	}

	@JsonIgnore
	public boolean isGcm() {
		if (isGcm == null) {
			isGcm = false;
		}
		return isGcm;
	}

	public void setGcm(boolean isGcm) {
		this.isGcm = isGcm;
	}

	public boolean isRestrictDownload() {
		return restrictDownload;
	}

	public void setRestrictDownload(boolean restrictDownload) {
		this.restrictDownload = restrictDownload;
	}

	public DcemGroup getGroup() {
		try {
			if (group != null) {
				group.getName();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return group;
	}

	public void setGroup(DcemGroup group) {
		this.group = group;
	}
	

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudSafeEntity other = (CloudSafeEntity) obj;
		return Objects.equals(id, other.id);
	}

	public boolean isFile() {
		return isFolder == false;
	}

	@JsonIgnore
	public String getCanonicalPath() {
		return name;
	}

	@Transient
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}
}