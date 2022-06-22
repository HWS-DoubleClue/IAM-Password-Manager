package com.doubleclue.dcem.as.entities;

import java.sql.Blob;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * The persistent class for the Registered Version
 * 
 */
@Entity
@Table(name = "as_cloudsafecontent")

@NamedQueries({ @NamedQuery(name = CloudSafeContentEntity.DELETE_ENTITY, query = "DELETE FROM CloudSafeContentEntity ac WHERE ac.id = ?1"),

})

public class CloudSafeContentEntity extends EntityInterface {

	public final static String DELETE_ENTITY = "CloudSafeContentEntity.delete";

	@Id
	@Column(name = "cloudDataEntity_dc_id")
	private Integer id;

	@Lob
	Blob content;

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = id.intValue();
	}

	public Blob getContent() {
		return content;
	}

	public void setContent(Blob content) {
		this.content = content;
	}

}