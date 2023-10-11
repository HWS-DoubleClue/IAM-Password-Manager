package com.doubleclue.dcem.core.entities;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.067+0200")
@StaticMetamodel(Auditing.class)
public class Auditing_ {
	public static volatile SingularAttribute<Auditing, Integer> id;
	public static volatile SingularAttribute<Auditing, LocalDateTime> timestamp;
	public static volatile SingularAttribute<Auditing, DcemUser> dcemUser;
	public static volatile SingularAttribute<Auditing, DcemAction> dcemAction;
	public static volatile SingularAttribute<Auditing, String> details;
}
