package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-03-23T09:51:24.918+0100")
@StaticMetamodel(DcemGroup.class)
public class DcemGroup_ {
	public static volatile SingularAttribute<DcemGroup, Integer> id;
	public static volatile SingularAttribute<DcemGroup, String> name;
	public static volatile SingularAttribute<DcemGroup, DcemRole> dcemRole;
	public static volatile SingularAttribute<DcemGroup, DomainEntity> domainEntity;
	public static volatile SingularAttribute<DcemGroup, String> description;
	public static volatile ListAttribute<DcemGroup, DcemUser> members;
	public static volatile SingularAttribute<DcemGroup, String> groupDn;
}
