package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-01-22T11:19:06.447+0100")
@StaticMetamodel(CloudSafeLimitEntity.class)
public class CloudSafeLimitEntity_ {
	public static volatile SingularAttribute<CloudSafeLimitEntity, Integer> id;
	public static volatile SingularAttribute<CloudSafeLimitEntity, DcemUser> user;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Long> limit;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Long> used;
	public static volatile SingularAttribute<CloudSafeLimitEntity, LocalDateTime> expiryDate;
	public static volatile SingularAttribute<CloudSafeLimitEntity, Boolean> passwordSafeEnabled;
}
