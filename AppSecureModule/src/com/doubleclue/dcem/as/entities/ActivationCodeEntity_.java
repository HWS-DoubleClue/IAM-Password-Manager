package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-01-22T11:19:06.413+0100")
@StaticMetamodel(ActivationCodeEntity.class)
public class ActivationCodeEntity_ {
	public static volatile SingularAttribute<ActivationCodeEntity, Integer> id;
	public static volatile SingularAttribute<ActivationCodeEntity, DcemUser> user;
	public static volatile SingularAttribute<ActivationCodeEntity, LocalDateTime> createdOn;
	public static volatile SingularAttribute<ActivationCodeEntity, String> activationCode;
	public static volatile SingularAttribute<ActivationCodeEntity, LocalDateTime> validTill;
	public static volatile SingularAttribute<ActivationCodeEntity, String> info;
}
