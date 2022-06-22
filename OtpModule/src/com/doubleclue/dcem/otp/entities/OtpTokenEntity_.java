package com.doubleclue.dcem.otp.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.otp.logic.OtpTypes;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-12-06T21:18:35.942+0100")
@StaticMetamodel(OtpTokenEntity.class)
public class OtpTokenEntity_ {
	public static volatile SingularAttribute<OtpTokenEntity, Integer> id;
	public static volatile SingularAttribute<OtpTokenEntity, OtpTypes> otpType;
	public static volatile SingularAttribute<OtpTokenEntity, String> serialNumber;
	public static volatile SingularAttribute<OtpTokenEntity, DcemUser> user;
	public static volatile SingularAttribute<OtpTokenEntity, Boolean> disabled;
	public static volatile SingularAttribute<OtpTokenEntity, String> info;
	public static volatile SingularAttribute<OtpTokenEntity, Integer> counter;
	public static volatile SingularAttribute<OtpTokenEntity, byte[]> secretKey;
}
