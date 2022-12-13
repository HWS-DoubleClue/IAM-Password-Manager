package com.doubleclue.dcem.userportal.entities;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.userportal.logic.AppHubApplication;
import com.doubleclue.dcem.userportal.logic.MyApplication;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * The persistent class for the application hub database table.
 * @author  Kenneth Ellul
 */
@Entity
@Table(name = "up_applicationhub", uniqueConstraints = @UniqueConstraint(name = "UK_APPHUB_NAME", columnNames = { "up_name" }))
@NamedQueries({ 
		@NamedQuery(name = ApplicationHubEntity.GET_APPLICATIONS_WITH_NAME, query = "SELECT ahe FROM ApplicationHubEntity ahe WHERE lower(ahe.name) LIKE ?1 ORDER BY ahe.name"),
		@NamedQuery(name = ApplicationHubEntity.GET_APPLICATION_BY_NAME, query = "SELECT ahe FROM ApplicationHubEntity ahe WHERE lower(ahe.name) = ?1"),
})

public class ApplicationHubEntity extends EntityInterface implements Serializable {

	private static final long serialVersionUID = 1L;
	public final static String GET_APPLICATIONS_WITH_NAME = "applicationHub.getApplicationsWithName";
	public final static String GET_APPLICATION_BY_NAME = "applicationHub.getAppByName";

	@Id
	@Column(name = "up_id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "up_apphubseq")
	@SequenceGenerator(name = "up_apphubseq", allocationSize = 1)
	private Integer id;

	@Column(nullable = false, length = 10000, updatable = true, insertable = true)
	@Convert(converter = DbJsonConverterAppHub.class)
	private AppHubApplication application;

	@Column(length = 42000, updatable = true, insertable = true)
	private byte[] logo;

	@Column(name = "up_name", nullable = false, length = 255, updatable = true, insertable = true)
	private String name;
	

	public ApplicationHubEntity() {
	}

	public ApplicationHubEntity(String name, AppHubApplication application) {
		super();
		this.name = name;
		this.application = application;
	}
	
	
	public ApplicationHubEntity(MyApplication myApplication) {
		this.name = myApplication.getName();
		this.application = myApplication.getApplication();
		this.logo = myApplication.getLogo();
	}

	public AppHubApplication getApplication() {
		return application;
	}

	public void setApplication(AppHubApplication application) {
		this.application = application;
	}

	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	@JsonIgnore
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}


	@Override
	public String toString() {
		return "ApplicationHubEntity [id=" + id + ", application=" + application + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((application == null) ? 0 : application.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ApplicationHubEntity other = (ApplicationHubEntity) obj;
		if (application == null) {
			if (other.application != null)
				return false;
		} else if (!application.equals(other.application))
			return false;
		if (id == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}