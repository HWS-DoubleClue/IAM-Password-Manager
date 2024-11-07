package com.doubleclue.dcem.as.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;


@Entity
@Table(name = "as_cloudsafe_tag")
public class CloudSafeTagEntity  extends EntityInterface implements Comparable<CloudSafeTagEntity> {

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Column (name = "dc_name", length = 255, nullable = false)
	String name;
	
	@DcemGui
	@Column (name = "dc_color", nullable = false, length = 64)
	String color;

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
}