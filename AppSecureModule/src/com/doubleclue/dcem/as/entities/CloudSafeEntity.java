package com.doubleclue.dcem.as.entities;

import java.util.Arrays;
import java.util.Date;

import javax.annotation.Nullable;
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
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

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

	public CloudSafeEntity(CloudSafeOwner owner, DcemUser user, DeviceEntity device, String name, Date discardAfter, String options, boolean isFolder,
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
	CloudSafeOwner owner;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_USER"), nullable = false, insertable = true, updatable = true)
	private DcemUser user;

	@DcemGui(name = "device", subClass = "name")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_DEVICE"), nullable = false, insertable = true, updatable = false)
	private DeviceEntity device;

	@DcemGui(name = "group", subClass = "name")
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_GROUP"), name = "group_dc_id", nullable = true, insertable = true, updatable = true)
	private DcemGroup group;

	@DcemGui
	@Column(name = "dc_name", length = 255, nullable = false)
	String name;

	@DcemGui
	@Column(name = "dc_length")
	long length;

	@Nullable
	@DcemGui
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_AS_PARENT_ID"), name = "dc_parent_id", nullable = true, insertable = true, updatable = true)
	private CloudSafeEntity parent;

	@DcemGui(name = "last_Modified_User", subClass = "loginId")
	@ManyToOne
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_PROP_USER_MODIFIED"), nullable = true, insertable = true, updatable = true)
	private DcemUser lastModifiedUser;

	// @DcemGui
	// boolean sign;

	@DcemGui
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified;

	@DcemGui
	@Temporal(TemporalType.TIMESTAMP)
	@Column(nullable = true)
	Date discardAfter;

	String options;

	// @Column(name = "dc_signature")
	// @Convert(converter = DbEncryptConverterBinary.class)
	// byte[] signature;

	@Column(name = "dc_salt", length = 32)
	byte[] salt;

	@DcemGui
	@Column(name = "dc_is_folder", nullable = false, updatable = false)
	boolean isFolder = false;

	@DcemGui
	@Column(name = "dc_gcm", nullable = false)
	Boolean isGcm = false;

	@DcemGui
	@Column(name = "recycled", nullable = false)
	boolean recycled = false;

	@Transient
	String loginId;

	@Transient
	String deviceName;

	@Transient
	String lengthString;

	@Transient
	boolean writeAccess = true;

	@Transient
	boolean restrictDownload;

	@Transient
	boolean newEntity = false;

	@Transient
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

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
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
	public String toString() { // TODO is this being used?
		StringBuffer sb = new StringBuffer();
		sb.append("Propery ID=");
		sb.append(id);
		sb.append(", Owner=");
		sb.append(owner.name());
		sb.append(", Name=");
		sb.append(name);
		return sb.toString();
	}

	@Transient
	public long getDiscardAfterAsLong() {
		if (discardAfter == null) {
			return 0;
		}
		return discardAfter.getTime();
	}

	public Date getDiscardAfter() {
		return discardAfter;
	}

	public void setDiscardAfter(Date discardAfter) {
		this.discardAfter = discardAfter;
	}

	public String getOptions() {
		return options;
	}

	public void setOptions(String options) {
		this.options = options;
	}

	// public void setContent(byte[] content) {
	// if (content != null && content.length == 0) {
	// content = null;
	// this.length = 0;
	// }
	// this.content = content;
	// if (content != null) {
	// length = content.length;
	// }
	// }
	//
	// @Transient
	// public void setContentText(String text) throws UnsupportedEncodingException {
	// this.content = text.getBytes(DcemConstants.CHARSET_UTF8);
	// length = content.length;
	//
	// }

	// @Transient
	// public String getContentText() {
	// if (content != null) {
	// try {
	// return new String(this.content, DcemConstants.CHARSET_UTF8);
	// } catch (UnsupportedEncodingException e) {
	// return new String(this.content);
	// }
	// } else {
	// return "";
	// }
	//
	// }

	public boolean isOption(CloudSafeOptions options) {
		if (this.options == null) {
			return false;
		}
		return this.options.contains(options.name());
	}

	// @Transient
	// public void setEncytedContent(byte[] contentx, char [] password) throws DcemException {
	// if (isOption(CloudDataOptions.PWD)) {
	// setSalt(RandomUtils.getRandom(8));
	// this.setContent(SecureServerUtils.cryptContentPass(contentx, password, false, salt));
	// } else if (isOption(CloudDataOptions.ENC)) {
	// this.setContent(DbEncryption.encryptSeed(contentx));
	// } else {
	// this.setContent(contentx);
	// }
	// length = contentx.length;
	// }
	//
	// @Transient
	// public byte[] getDecytedContent(char [] password) throws DcemException {
	// if (isOption(CloudDataOptions.PWD)) {
	// return (SecureServerUtils.cryptContentPass(content, password, true, salt));
	// } else if (isOption(CloudDataOptions.ENC)) {
	// return DbEncryption.decryptSeed(content);
	// } else {
	// return content;
	// }
	// }

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((device == null) ? 0 : device.hashCode());
		result = prime * result + ((deviceName == null) ? 0 : deviceName.hashCode());
		result = prime * result + ((discardAfter == null) ? 0 : discardAfter.hashCode());
		result = prime * result + ((group == null) ? 0 : group.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isFolder ? 1231 : 1237);
		result = prime * result + ((isGcm == null) ? 0 : isGcm.hashCode());
		result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + ((lastModifiedUser == null) ? 0 : lastModifiedUser.hashCode());
		result = prime * result + (int) (length ^ (length >>> 32));
		result = prime * result + ((lengthString == null) ? 0 : lengthString.hashCode());
		result = prime * result + ((loginId == null) ? 0 : loginId.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (newEntity ? 1231 : 1237);
		result = prime * result + ((options == null) ? 0 : options.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + (recycled ? 1231 : 1237);
		result = prime * result + (restrictDownload ? 1231 : 1237);
		result = prime * result + Arrays.hashCode(salt);
		result = prime * result + ((user == null) ? 0 : user.hashCode());
		result = prime * result + (writeAccess ? 1231 : 1237);
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
		CloudSafeEntity other = (CloudSafeEntity) obj;
		if (device == null) {
			if (other.device != null)
				return false;
		} else if (!device.equals(other.device))
			return false;
		if (deviceName == null) {
			if (other.deviceName != null)
				return false;
		} else if (!deviceName.equals(other.deviceName))
			return false;
		if (discardAfter == null) {
			if (other.discardAfter != null)
				return false;
		} else if (!discardAfter.equals(other.discardAfter))
			return false;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isFolder != other.isFolder)
			return false;
		if (isGcm == null) {
			if (other.isGcm != null)
				return false;
		} else if (!isGcm.equals(other.isGcm))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (lastModifiedUser == null) {
			if (other.lastModifiedUser != null)
				return false;
		} else if (!lastModifiedUser.equals(other.lastModifiedUser))
			return false;
		if (length != other.length)
			return false;
		if (lengthString == null) {
			if (other.lengthString != null)
				return false;
		} else if (!lengthString.equals(other.lengthString))
			return false;
		if (loginId == null) {
			if (other.loginId != null)
				return false;
		} else if (!loginId.equals(other.loginId))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (newEntity != other.newEntity)
			return false;
		if (options == null) {
			if (other.options != null)
				return false;
		} else if (!options.equals(other.options))
			return false;
		if (owner != other.owner)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (recycled != other.recycled)
			return false;
		if (restrictDownload != other.restrictDownload)
			return false;
		if (!Arrays.equals(salt, other.salt))
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		if (writeAccess != other.writeAccess)
			return false;
		return true;
	}

	public boolean isFile() {
		return isFolder == false;
	}

	public String getCanonicalPath() {
		// TODO Auto-generated method stub we implement this later
		return name;
	}

	@Transient
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
}