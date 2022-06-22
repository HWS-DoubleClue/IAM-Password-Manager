package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-06-23T12:43:01.481+0200")
@StaticMetamodel(CloudSafeLimitEntity.class)
public class CloudSafeLimitEntity_ {
	public static volatile SingularAttribute<CloudSafeLimitEntity, Integer> id;
	public static volatile SingularAttribute<CloudSafeLimitEntity, DcemUser> user;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Long> limit;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Long> used;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Date> expiryDate;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Boolean> passwordSafeEnabled;
}
