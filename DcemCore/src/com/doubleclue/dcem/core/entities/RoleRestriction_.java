package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.jpa.FilterItem;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-07-24T10:41:10.072+0200")
@StaticMetamodel(RoleRestriction.class)
public class RoleRestriction_ {
	public static volatile SingularAttribute<RoleRestriction, Integer> id;
	public static volatile SingularAttribute<RoleRestriction, DcemRole> dcemRole;
	public static volatile SingularAttribute<RoleRestriction, String> moduleId;
	public static volatile SingularAttribute<RoleRestriction, String> viewName;
	public static volatile SingularAttribute<RoleRestriction, String> variableName;
	public static volatile SingularAttribute<RoleRestriction, FilterItem> filterItem;
	public static volatile SingularAttribute<RoleRestriction, Integer> jpaVersion;
}
