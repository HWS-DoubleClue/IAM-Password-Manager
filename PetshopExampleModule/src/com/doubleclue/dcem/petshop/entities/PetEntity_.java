package com.doubleclue.dcem.petshop.entities;

import com.doubleclue.dcem.petshop.logic.PetType;
import com.doubleclue.dcem.petshop.logic.SexEnum;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-11-07T15:48:15.774+0100")
@StaticMetamodel(PetEntity.class)
public class PetEntity_ {
	public static volatile SingularAttribute<PetEntity, Integer> id;
	public static volatile SingularAttribute<PetEntity, String> name;
	public static volatile SingularAttribute<PetEntity, PetType> petType;
	public static volatile SingularAttribute<PetEntity, Integer> age;
	public static volatile SingularAttribute<PetEntity, SexEnum> sex;
	public static volatile SingularAttribute<PetEntity, Integer> price;
	public static volatile SingularAttribute<PetEntity, Boolean> reserved;
	public static volatile SingularAttribute<PetEntity, Boolean> sold;
}
