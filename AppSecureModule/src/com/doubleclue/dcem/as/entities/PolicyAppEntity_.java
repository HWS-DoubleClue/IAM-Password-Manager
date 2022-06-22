package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.as.AuthApplication;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-11-06T08:52:48.564+0100")
@StaticMetamodel(PolicyAppEntity.class)
public class PolicyAppEntity_ {
	public static volatile SingularAttribute<PolicyAppEntity, Integer> id;
	public static volatile SingularAttribute<PolicyAppEntity, AuthApplication> authApplication;
	public static volatile SingularAttribute<PolicyAppEntity, String> subName;
	public static volatile SingularAttribute<PolicyAppEntity, Integer> subId;
	public static volatile SingularAttribute<PolicyAppEntity, Boolean> disabled;
}
