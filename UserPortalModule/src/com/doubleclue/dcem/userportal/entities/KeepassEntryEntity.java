package com.doubleclue.dcem.userportal.entities;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.doubleclue.dcem.userportal.logic.AppHubApplication;

/**
 * The persistent class for the application hub database table.
 * @author  Kenneth Ellul
 */
@Entity
@Table(name = "up_keepassentry")

public class KeepassEntryEntity  implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "dc_id")
	private String uuid;

	@Column(nullable = true, length = 10000, insertable = true, updatable = true)
	@Convert(converter = DbJsonConverterAppHub.class)
	private AppHubApplication application;

	@Column(name = "up_name", nullable = false, length = 255, insertable = true, updatable = true)
	private String name;
	
	
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name = "appEntity", foreignKey = @ForeignKey(name = "FK_KEEPASS_APP"), nullable = true, insertable = true, updatable = true)
	ApplicationHubEntity applicationEntity;

	public KeepassEntryEntity() {
	}
	
	public KeepassEntryEntity(String uuid, ApplicationHubEntity applicationHubEntity) {
		super();
		this.uuid = uuid;
		this.name = applicationHubEntity.getName();
		this.applicationEntity = applicationHubEntity;
	}

	public KeepassEntryEntity(String uuid, String name, AppHubApplication application) {
		super();
		this.uuid = uuid;
		this.name = name;
		this.application = application;
	}
	
	
	public AppHubApplication getApplication() {
		return application;
	}

	public void setApplication(AppHubApplication application) {
		this.application = application;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public ApplicationHubEntity getApplicationEntity() {
		return applicationEntity;
	}

	public void setApplicationEntity(ApplicationHubEntity applicationEntity) {
		this.applicationEntity = applicationEntity;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, uuid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeepassEntryEntity other = (KeepassEntryEntity) obj;
		return Objects.equals(name, other.name) && Objects.equals(uuid, other.uuid);
	}

	@Override
	public String toString() {
		return "KeepassEntryEntity [uuid=" + uuid + ", name=" + name;
	}
	
	

}