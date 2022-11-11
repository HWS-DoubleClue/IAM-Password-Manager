package com.doubleclue.dcem.petshop.entities;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-06-14T20:32:50.740+0200")
@StaticMetamodel(PetOrderEntity.class)
public class PetOrderEntity_ {
	public static volatile SingularAttribute<PetOrderEntity, Integer> id;
	public static volatile SingularAttribute<PetOrderEntity, PetEntity> pet;
	public static volatile SingularAttribute<PetOrderEntity, Date> date;
}
