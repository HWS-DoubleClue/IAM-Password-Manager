package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-12-06T22:31:53.966+0100")
@StaticMetamodel(FidoAuthenticatorEntity.class)
public class FidoAuthenticatorEntity_ {
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Integer> id;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, DcemUser> user;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, String> displayName;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, String> credentialId;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, byte[]> publicKey;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Boolean> passwordless;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Date> registeredOn;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Date> lastUsed;
}
