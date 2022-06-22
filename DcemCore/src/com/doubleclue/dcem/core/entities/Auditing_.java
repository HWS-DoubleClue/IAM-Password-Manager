package com.doubleclue.dcem.core.entities;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-07-24T10:41:09.758+0200")
@StaticMetamodel(Auditing.class)
public class Auditing_ {
	public static volatile SingularAttribute<Auditing, Integer> id;
	public static volatile SingularAttribute<Auditing, Date> timestamp;
	public static volatile SingularAttribute<Auditing, DcemUser> dcemUser;
	public static volatile SingularAttribute<Auditing, DcemAction> dcemAction;
	public static volatile SingularAttribute<Auditing, String> details;
}
