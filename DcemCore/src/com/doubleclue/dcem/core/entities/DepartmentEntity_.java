package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-03-07T11:16:48.576+0100")
@StaticMetamodel(DepartmentEntity.class)
public class DepartmentEntity_ {
	public static volatile SingularAttribute<DepartmentEntity, Long> id;
	public static volatile SingularAttribute<DepartmentEntity, DepartmentEntity> parentDepartment;
	public static volatile SingularAttribute<DepartmentEntity, String> name;
	public static volatile SingularAttribute<DepartmentEntity, String> abbriviation;
	public static volatile SingularAttribute<DepartmentEntity, DcemUser> headOf;
	public static volatile SingularAttribute<DepartmentEntity, DcemUser> deputy;
	public static volatile SingularAttribute<DepartmentEntity, String> description;
}
