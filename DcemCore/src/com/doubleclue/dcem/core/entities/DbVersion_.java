package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2017-02-10T12:42:31.594+0100")
@StaticMetamodel(DbVersion.class)
public class DbVersion_ {
	public static volatile SingularAttribute<DbVersion, String> moduleId;
	public static volatile SingularAttribute<DbVersion, String> versionStr;
	public static volatile SingularAttribute<DbVersion, Integer> version;
}
