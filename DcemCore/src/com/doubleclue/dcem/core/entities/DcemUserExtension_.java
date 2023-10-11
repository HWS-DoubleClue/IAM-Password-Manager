package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.098+0200")
@StaticMetamodel(DcemUserExtension.class)
public class DcemUserExtension_ {
	public static volatile SingularAttribute<DcemUserExtension, Integer> id;
	public static volatile SingularAttribute<DcemUserExtension, String> country;
	public static volatile SingularAttribute<DcemUserExtension, String> jobTitle;
	public static volatile SingularAttribute<DcemUserExtension, byte[]> photo;
	public static volatile SingularAttribute<DcemUserExtension, DepartmentEntity> department;
	public static volatile SingularAttribute<DcemUserExtension, String> timezoneString;
}
