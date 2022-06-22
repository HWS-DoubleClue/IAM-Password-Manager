package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-11-13T09:42:25.637+0100")
@StaticMetamodel(TenantEntity.class)
public class TenantEntity_ {
	public static volatile SingularAttribute<TenantEntity, Integer> id;
	public static volatile SingularAttribute<TenantEntity, Boolean> master;
	public static volatile SingularAttribute<TenantEntity, String> name;
	public static volatile SingularAttribute<TenantEntity, String> fullName;
	public static volatile SingularAttribute<TenantEntity, String> schema;
	public static volatile SingularAttribute<TenantEntity, Boolean> disabled;
}
