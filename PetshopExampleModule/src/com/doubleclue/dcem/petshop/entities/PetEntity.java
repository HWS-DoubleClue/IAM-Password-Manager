package com.doubleclue.dcem.petshop.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.petshop.logic.PetType;
import com.doubleclue.dcem.petshop.logic.SexEnum;

@Entity
@Table(name="petshop_pet", uniqueConstraints = @UniqueConstraint(name = "UK_PET_NAME", columnNames = {"dc_name" }) )
public class PetEntity extends EntityInterface {
	
	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="petshop_petseq" )
	@SequenceGenerator(
		    name="petshop_petseq",
		    allocationSize=4
	)
	Integer id;
	
	@Column(name = "dc_name", nullable=false, length = 128)
	@DcemGui
	String name;
	
	@DcemGui
	@Column(nullable=false)
	PetType petType; 
	
	@DcemGui
	@Column(name = "dc_age", nullable=false)
	int age;
	
	@DcemGui
	@Column(name = "dc_sex", nullable=false)
	SexEnum sex;
	
	@DcemGui
	@Column(name = "dc_price", nullable=false)
	int price;
	
	@DcemGui
	@Column(name = "dc_reserved", nullable=false)
	boolean reserved;
	
	@DcemGui
	@Column(name = "dc_sold", nullable=false)
	boolean sold;

	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PetType getPetType() {
		return petType;
	}

	public void setPetType(PetType petType) {
		this.petType = petType;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public SexEnum getSex() {
		return sex;
	}

	public void setSex(SexEnum sex) {
		this.sex = sex;
	}

	

	public boolean isReserved() {
		return reserved;
	}

	public void setReserved(boolean reserved) {
		this.reserved = reserved;
	}

	

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;		
	}

	public boolean isSold() {
		return sold;
	}

	public void setSold(boolean sold) {
		this.sold = sold;
	}

}
