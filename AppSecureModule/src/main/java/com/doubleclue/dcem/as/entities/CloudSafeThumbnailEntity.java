package com.doubleclue.dcem.as.entities;

import java.sql.Blob;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.doubleclue.dcem.core.entities.EntityInterface;

/**
 * The persistent class for the Registered Version
 * 
 */
@Entity
@Table(name = "as_cloudsafethumbnail")

@NamedQueries({ @NamedQuery(name = CloudSafeThumbnailEntity.DELETE_ENTITY, query = "DELETE FROM CloudSafeContentEntity ac WHERE ac.id = ?1"),

})

public class CloudSafeThumbnailEntity extends EntityInterface {

	public final static String DELETE_ENTITY = "CloudSafeThumbnailEntity.delete";
	
	public CloudSafeThumbnailEntity() {
	}

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;


	@Lob	
	byte [] thumbnail;

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

	

}