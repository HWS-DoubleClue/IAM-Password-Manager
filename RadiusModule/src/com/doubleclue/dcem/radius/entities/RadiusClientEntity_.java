package com.doubleclue.dcem.radius.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-06-12T16:00:51.470+0200")
@StaticMetamodel(RadiusClientEntity.class)
public class RadiusClientEntity_ {
	public static volatile SingularAttribute<RadiusClientEntity, Integer> id;
	public static volatile SingularAttribute<RadiusClientEntity, String> name;
	public static volatile SingularAttribute<RadiusClientEntity, String> ipNumber;
	public static volatile SingularAttribute<RadiusClientEntity, String> sharedSecret;
	public static volatile SingularAttribute<RadiusClientEntity, Boolean> useChallenge;
	public static volatile SingularAttribute<RadiusClientEntity, Boolean> ignoreUsersPassword;
	public static volatile SingularAttribute<RadiusClientEntity, String> settingsJson;
}
