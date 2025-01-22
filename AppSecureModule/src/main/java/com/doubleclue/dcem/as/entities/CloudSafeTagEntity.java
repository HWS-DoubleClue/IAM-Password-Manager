package com.doubleclue.dcem.as.entities;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.DcemUtils;


@NamedQueries({ @NamedQuery(name = CloudSafeTagEntity.GET_ALL_TAGS, query = "SELECT ct FROM CloudSafeTagEntity ct "),
//		@NamedQuery(name = CloudSafeTagEntity.GET_ALL_TAGS_BY_CLOUDSAFE, query = "Select c FROM CloudSafeTagEntity c " + "JOIN c.cloudSafes cs "
//				+ "WHERE cs.id = ?1"),
		@NamedQuery(name = CloudSafeTagEntity.GET_TAG_BY_NAME, query = "Select c FROM CloudSafeTagEntity c WHERE c.name = ?1"),

})

@Entity
@Table(name = "as_cloudsafe_tag", uniqueConstraints = @UniqueConstraint(name = "UK_DM_TAG_NAME", columnNames = { "dc_name"}))
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CloudSafeTagEntity extends EntityInterface implements Comparable<CloudSafeTagEntity> {

	public static final String GET_ALL_TAGS = "CloudSafeTagEntity.GetAllTags";
//	public static final String GET_ALL_TAGS_BY_CLOUDSAFE = "GetAllTagsByCloudsafe";
	public static final String GET_TAG_BY_NAME = "CloudSafeTagEntity.GetByName";

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Column(name = "dc_name", length = 255, nullable = false)
	String name;

	@DcemGui
	@Column(name = "dc_color", nullable = false, length = 64)
	String color;

	@ManyToMany(mappedBy = "tags")
	private List<CloudSafeEntity> cloudSafes;

	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			id = null;
		}
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	@Override
	public int compareTo(CloudSafeTagEntity cloudSafeTagEntity) {
		return name.compareTo(cloudSafeTagEntity.getName());
	}

	@Override
	public String toString() {
		return name;
	}

	public String getForegroundColor() {
		return DcemUtils.decideFontColor(getColor());
	}

	@Override
	public int hashCode() {
		return Objects.hash(color, id, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudSafeTagEntity other = (CloudSafeTagEntity) obj;
		return Objects.equals(color, other.color) && Objects.equals(id, other.id) && Objects.equals(name, other.name);
	}

}