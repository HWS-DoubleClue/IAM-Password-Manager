package com.doubleclue.dcem.as.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-09-03T14:40:26.392+0200")
@StaticMetamodel(AuthGatewayEntity.class)
public class AuthGatewayEntity_ {
	public static volatile SingularAttribute<AuthGatewayEntity, Integer> id;
	public static volatile SingularAttribute<AuthGatewayEntity, String> name;
	public static volatile SingularAttribute<AuthGatewayEntity, Boolean> disabled;
	public static volatile SingularAttribute<AuthGatewayEntity, Integer> retryCounter;
	public static volatile SingularAttribute<AuthGatewayEntity, byte[]> sharedKey;
}
