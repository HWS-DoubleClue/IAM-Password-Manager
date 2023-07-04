package com.doubleclue.dcem.petshop.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;

@Entity
@Table(name="petshop_petorder")
public class PetOrderEntity extends EntityInterface {
	
	@Id
	@Column(name = "dc_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY )
	Integer id;
	
	
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(referencedColumnName = "dc_id", foreignKey = @ForeignKey(name = "FK_PET_ORDER"), name = "dc_name", nullable = false, insertable = true, updatable = true)
    @DcemGui(subClass = "name")
    private PetEntity pet;
    
    @DcemGui
    @Column (name="dc_date")
    Date date;   
	

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;		
	}

	public PetEntity getPet() {
		return pet;
	}

	public void setPet(PetEntity pet) {
		this.pet = pet;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	

}
