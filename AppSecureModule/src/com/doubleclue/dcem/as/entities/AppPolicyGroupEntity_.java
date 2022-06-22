package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemGroup;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-03-30T12:11:44.979+0200")
@StaticMetamodel(AppPolicyGroupEntity.class)
public class AppPolicyGroupEntity_ {
	public static volatile SingularAttribute<AppPolicyGroupEntity, Integer> id;
	public static volatile SingularAttribute<AppPolicyGroupEntity, PolicyAppEntity> policyAppEntity;
	public static volatile SingularAttribute<AppPolicyGroupEntity, PolicyEntity> policyEntity;
	public static volatile SingularAttribute<AppPolicyGroupEntity, DcemGroup> group;
	public static volatile SingularAttribute<AppPolicyGroupEntity, Integer> priority;
}
