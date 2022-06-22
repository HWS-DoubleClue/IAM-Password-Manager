package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-01-06T14:05:29.996+0100")
@StaticMetamodel(DcemRole.class)
public class DcemRole_ {
	public static volatile SingularAttribute<DcemRole, Integer> id;
	public static volatile SingularAttribute<DcemRole, String> name;
	public static volatile SingularAttribute<DcemRole, Boolean> systemRole;
	public static volatile SingularAttribute<DcemRole, Boolean> disabled;
	public static volatile SingularAttribute<DcemRole, Integer> rank;
	public static volatile SingularAttribute<DcemRole, Integer> jpaVersion;
	public static volatile SetAttribute<DcemRole, DcemAction> actions;
}
