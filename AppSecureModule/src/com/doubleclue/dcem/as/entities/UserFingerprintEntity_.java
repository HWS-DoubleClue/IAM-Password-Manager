package com.doubleclue.dcem.as.entities;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-01-22T11:19:06.491+0100")
@StaticMetamodel(UserFingerprintEntity.class)
public class UserFingerprintEntity_ {
	public static volatile SingularAttribute<UserFingerprintEntity, FingerprintId> id;
	public static volatile SingularAttribute<UserFingerprintEntity, LocalDateTime> timestamp;
	public static volatile SingularAttribute<UserFingerprintEntity, String> fingerprint;
}
