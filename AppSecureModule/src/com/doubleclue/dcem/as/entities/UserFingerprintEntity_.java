package com.doubleclue.dcem.as.entities;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-07-23T16:40:13.196+0200")
@StaticMetamodel(UserFingerprintEntity.class)
public class UserFingerprintEntity_ {
	public static volatile SingularAttribute<UserFingerprintEntity, FingerprintId> id;
	public static volatile SingularAttribute<UserFingerprintEntity, Date> timestamp;
	public static volatile SingularAttribute<UserFingerprintEntity, String> fingerprint;
}
