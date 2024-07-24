package com.doubleclue.dcem.core.entities;

import java.util.Comparator;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.NotNullOrEmptyString;

@NamedQueries({})

@Entity
@Table(name = "core_branch_location")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class BranchLocation extends EntityInterface implements Comparable<BranchLocation> {

	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@DcemGui
	@Column(length = 255, name = "dc_country", nullable = false)
	@NotNullOrEmptyString
	private String country;

	@DcemGui
	@Column(length = 255, name = "dc_state")
	private String countryState;

	@DcemGui
	@Column(length = 255, name = "dc_city", nullable = false)
	@NotNullOrEmptyString
	private String city;

	@DcemGui
	@Column(length = 32, name = "dc_zipcode")
	private String zipCode;

	@DcemGui
	@Column(length = 255, name = "dc_street")
	private String street;

	@DcemGui
	@Column(length = 32, name = "dc_street_nr")
	private String streetNumber;

	public BranchLocation() {
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		if (id != null) {
			this.id = id.intValue();
		} else {
			this.id = null;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, country, city);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (Hibernate.getClass(this) != Hibernate.getClass(obj))
			return false;
		BranchLocation other = (BranchLocation) obj;
		return Objects.equals(this.getId(), other.getId()) && Objects.equals(this.getCountry(), other.getCountry());
	}

	@Override
	public int compareTo(BranchLocation other) {
		int compareCountryResult = Objects.compare(this.getCountry(), other.getCountry(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareCountryResult != 0) {
			return compareCountryResult;
		}
		int compareCityResult = Objects.compare(this.getCity(), other.getCity(), Comparator.nullsFirst(Comparator.naturalOrder()));
		if (compareCityResult != 0) {
			return compareCityResult;
		}
		return Objects.compare(this.getId(), other.getId(), Comparator.nullsFirst(Comparator.naturalOrder()));
	}

	@Override
	public String toString() {
		return country + " - " + city;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getCountryState() {
		return countryState;
	}

	public void setCountryState(String countryState) {
		this.countryState = countryState;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}
}
