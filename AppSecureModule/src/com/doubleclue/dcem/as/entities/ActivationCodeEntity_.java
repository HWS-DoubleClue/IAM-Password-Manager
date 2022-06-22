package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-12-06T22:31:53.916+0100")
@StaticMetamodel(ActivationCodeEntity.class)
public class ActivationCodeEntity_ {
	public static volatile SingularAttribute<ActivationCodeEntity, Integer> id;
	public static volatile SingularAttribute<ActivationCodeEntity, DcemUser> user;
	public static volatile SingularAttribute<ActivationCodeEntity, Date> createdOn;
	public static volatile SingularAttribute<ActivationCodeEntity, String> activationCode;
	public static volatile SingularAttribute<ActivationCodeEntity, Date> validTill;
	public static volatile SingularAttribute<ActivationCodeEntity, String> info;
}
