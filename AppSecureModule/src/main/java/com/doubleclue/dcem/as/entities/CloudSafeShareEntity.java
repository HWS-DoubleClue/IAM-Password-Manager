package com.doubleclue.dcem.as.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
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

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * The persistent class for the Registered Version
 * 
 */
@Entity
@Table(name = "as_cloudsafeshare")
@NamedQueries({
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe=?1 AND (cloudShare.user=?2 OR cloudShare.group=?3)"),
		@NamedQuery(name = CloudSafeShareEntity.GET_USER_SHARE_FILES, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare LEFT JOIN cloudShare.cloudSafe cloudSafe "
				+ "WHERE cloudSafe.user<>?1 AND (cloudShare.user=?1 OR cloudShare.group IN (?2)) AND cloudSafe.name LIKE ?3 "
				+ "AND (cloudSafe.discardAfter IS NULL OR cloudSafe.discardAfter>?4) AND (cloudSafe.recycled = false)  ORDER BY cloudShare.cloudSafe.isFolder DESC, cloudShare.group ASC NULLS FIRST"),
		@NamedQuery(name = CloudSafeShareEntity.GET_CLOUD_SHARE_FILES, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare LEFT JOIN cloudShare.cloudSafe cloudSafe "
				+ "WHERE cloudSafe.user<>?1 AND (cloudShare.user=?1 OR cloudShare.group IN (?2)) AND cloudSafe = ?3 "
				+ "AND (cloudSafe.discardAfter IS NULL OR cloudSafe.discardAfter>?4) "),
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE_ACCESS, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe=?1"),
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE_DISCARDED, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe.discardAfter<?1"),
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE_USER, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe.user=?1 OR cloudShare.user=?2"),
		@NamedQuery(name = CloudSafeShareEntity.DELETE_SHARE_BY_CLOUD_DATA, query = "DELETE FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe.id=?1"),
//		@NamedQuery(name = CloudSafeShareEntity.GET_SHARED_FILE_KEYS, query = "SELECT cloudShare "
//				// "SELECT DISTINCT CONCAT(cloudSafe.user.loginId, '@', cloudSafe.name), cloudSafe.owner "
//				+ "FROM CloudSafeShareEntity cloudShare LEFT JOIN cloudShare.cloudSafe cloudSafe "
//				+ "WHERE cloudSafe.name LIKE ?1 AND (cloudSafe.discardAfter IS NULL OR cloudSafe.discardAfter > ?2) AND (cloudSafe.recycled = false)"
//				+ "AND cloudSafe.user<>?3 AND (cloudShare.user=?3 OR cloudShare.group IN (?4)) "
//				+ "AND cloudSafe.owner=?5 AND (cloudSafe.lastModified IS NULL OR cloudSafe.lastModified>?6)"),
		@NamedQuery(name = CloudSafeShareEntity.DELETE_SHARE_BY_USER, query = "DELETE FROM CloudSafeShareEntity cloudShare WHERE cloudShare.user=?1"),
		@NamedQuery(name = CloudSafeShareEntity.DELETE_SHARE_BY_GROUP, query = "DELETE FROM CloudSafeShareEntity cloudShare WHERE cloudShare.group=?1"),
		@NamedQuery(name = CloudSafeShareEntity.DELETE_CLOUD_SHARE_CONTENT, query = "DELETE FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe.id IN (?1)"),
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE_BY_ID, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.cloudSafe=?1 AND (cloudShare.user=?2 OR cloudShare.group IN (?3))"),
		@NamedQuery(name = CloudSafeShareEntity.GET_USER_SHARE_FILES_BY_PARENT_ID, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare LEFT JOIN cloudShare.cloudSafe cloudSafe "
				+ "WHERE cloudSafe.user<>?1 AND (cloudShare.user=?1 OR cloudShare.group IN (?2)) AND cloudSafe = ?3 "
				+ "AND (cloudSafe.discardAfter IS NULL OR cloudSafe.discardAfter>?4)  ORDER BY cloudShare.group ASC NULLS FIRST"),
		@NamedQuery(name = CloudSafeShareEntity.GET_SHARE_BY_SHARE_ID, query = "SELECT cloudShare FROM CloudSafeShareEntity cloudShare WHERE cloudShare.id=?1")

})

public class CloudSafeShareEntity extends EntityInterface {

	public final static String GET_SHARE = "CloudSafeShareEntity.getShare";
	public final static String GET_SHARE_BY_ID = "CloudSafeShareEntity.getShareById";
	public final static String GET_USER_SHARE_FILES = "CloudSafeShareEntity.getShareFiles";
	public static final String GET_SHARE_ACCESS = "CloudSafeShareEntity.getShareAccess";
	public static final String GET_SHARE_DISCARDED = "CloudSafeShareEntity.getShareDiscarded";
	public static final String GET_SHARE_USER = "CloudSafeShareEntity.getShareUser";
	public static final String DELETE_SHARE_BY_CLOUD_DATA = "CloudSafeShareEntity.deleteByCloudSafe";
//	public static final String GET_SHARED_FILE_KEYS = "CloudSafeShareEntity.getSharedFilenames";
	public static final String DELETE_CLOUD_SHARE_CONTENT = "CloudSafeShareEntity.deleteCloudShareContent";
	public final static String GET_USER_SHARE_FILES_BY_PARENT_ID = "CloudSafeShareEntity.getShareFilesByParentId";
	public final static String GET_SHARE_BY_SHARE_ID = "CloudSafeShareEntity.getShareFilesByShareId";
	public static final String DELETE_SHARE_BY_USER = "CloudSafeShareEntity.deleteShareByUser";
	public static final String DELETE_SHARE_BY_GROUP = "CloudSafeShareEntity.deleteShareByGroup";
	public static final String GET_CLOUD_SHARE_FILES = "CloudSafeShareEntity.getCloudShareFiles";

	public CloudSafeShareEntity() {
		super();
	}

	public CloudSafeShareEntity(CloudSafeEntity cloudSafe, DcemUser user, DcemGroup group, boolean writeAccess, boolean restrictDownload) {
		this.user = user;
		this.group = group;
		this.cloudSafe = cloudSafe;
		this.writeAccess = writeAccess;
		this.restrictDownload = restrictDownload;
	}

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "asmSeqStoreCloudSafeShare", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "AS_CLOUDDATASHARE.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "asmSeqStoreCloudSafeShare")
	private Integer id;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_CD_SHARE_USER"), nullable = true, insertable = true, updatable = false)
	private DcemUser user;

	@DcemGui(name = "group", subClass = "group")
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_CD_GROUP"), nullable = true, insertable = true, updatable = false)
	private DcemGroup group;

	@OnDelete(action = OnDeleteAction.CASCADE)
	@DcemGui(name = "Cloud Safe", subClass = "name")
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(foreignKey = @ForeignKey(name = "FK_AS_CD_SHARE"), nullable = true, insertable = true, updatable = false)
	private CloudSafeEntity cloudSafe;

	Boolean writeAccess;
	
	Boolean restrictDownload;

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = id.intValue();
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public DcemGroup getGroup() {
		try {
			if (group != null) {
				group.getName();
			}
		} catch (Exception exp) {
			return null;
		}
		return group;
	}

	public void setGroup(DcemGroup group) {
		this.group = group;
	}
	
	public String getOwnerName () {
		if (user != null) {
			return user.getDisplayName();
		}
		return group.getName();
	}

	public boolean isWriteAccess() {
		return writeAccess;
	}

	public void setWriteAccess(boolean writeAccess) {
		this.writeAccess = writeAccess;
	}

	public boolean isRestrictDownload() {
		if (restrictDownload == null) {
			restrictDownload = false;
		}
		return restrictDownload;
	}

	public void setRestrictDownload(boolean restrictDownload) {
		this.restrictDownload = restrictDownload;
	}

	public CloudSafeEntity getCloudSafe() {
		return cloudSafe;
	}

	public void setCloudSafe(CloudSafeEntity cloudSafe) {
		this.cloudSafe = cloudSafe;
	}
	
	@Override
	public String toString() {
		return "CloudSafeShareEntity [id=" + id + ", user=" + user + ", group=" + group + ", cloudSafe=" + cloudSafe + ", writeAccess=" + writeAccess
				+ ", restrictDownload=" + restrictDownload + "]";
	}
}