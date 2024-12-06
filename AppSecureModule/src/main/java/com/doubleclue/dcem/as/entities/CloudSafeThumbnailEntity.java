package com.doubleclue.dcem.as.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;

/**
 * The persistent class for the Registered Version
 * 
 */
@Entity
@Table(name = "as_cloudsafethumbnail")
@NamedQueries({
		@NamedQuery(name = CloudSafeThumbnailEntity.DELETE_CLOUD_SAFE_THUMBNAIL_BY_ID, query = "DELETE FROM CloudSafeThumbnailEntity c WHERE c.id = ?1"),
		@NamedQuery(name = CloudSafeThumbnailEntity.DELETE_CLOUD_SAFE_THUMBNAIL_BY_CLOUD_SAFE_ID, query = "DELETE FROM CloudSafeThumbnailEntity c WHERE c.cloudSafeEntity.id = ?1"), })
public class CloudSafeThumbnailEntity extends EntityInterface {

	public static final String DELETE_CLOUD_SAFE_THUMBNAIL_BY_ID = "CloudSafeThumbnailEntity.deleteCloudSafeThumbnailById";
	public static final String DELETE_CLOUD_SAFE_THUMBNAIL_BY_CLOUD_SAFE_ID = "CloudSafeThumbnailEntity.deleteCloudSafeThumbnailByCloudSafeId";

	public CloudSafeThumbnailEntity() {
	}

	@Id
	@Column(name = "dc_id")
	private Integer id;

	@MapsId
	@OneToOne(cascade = CascadeType.ALL, optional = true, fetch = FetchType.LAZY)
	@JoinColumn(name = "dc_id", foreignKey = @ForeignKey(name = "FK_CLOUDSAFE_THUMBNAIL"))
	private CloudSafeEntity cloudSafeEntity;

	@Lob
	@Convert(converter = DbEncryptConverterBinary.class)
	byte[] thumbnail;

	public CloudSafeThumbnailEntity(byte[] thumbnail2) {
		this.thumbnail = thumbnail2;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = id.intValue();
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void setCloudSafeEntity(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;
	}

}