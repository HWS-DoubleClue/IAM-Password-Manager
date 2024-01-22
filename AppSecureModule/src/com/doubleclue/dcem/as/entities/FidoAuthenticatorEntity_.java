package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemUser;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-01-22T11:19:06.468+0100")
@StaticMetamodel(FidoAuthenticatorEntity.class)
public class FidoAuthenticatorEntity_ {
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Integer> id;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, DcemUser> user;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, String> displayName;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, String> credentialId;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, byte[]> publicKey;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, Boolean> passwordless;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, LocalDateTime> registeredOn;
	public static volatile SingularAttribute<FidoAuthenticatorEntity, LocalDateTime> lastUsed;
}
