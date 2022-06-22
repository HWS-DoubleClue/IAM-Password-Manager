package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.config.KeyStorePurpose;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-07-24T10:41:10.058+0200")
@StaticMetamodel(KeyStoreEntity.class)
public class KeyStoreEntity_ {
	public static volatile SingularAttribute<KeyStoreEntity, Integer> id;
	public static volatile SingularAttribute<KeyStoreEntity, DcemNode> node;
	public static volatile SingularAttribute<KeyStoreEntity, KeyStorePurpose> purpose;
	public static volatile SingularAttribute<KeyStoreEntity, String> cn;
	public static volatile SingularAttribute<KeyStoreEntity, String> ipAddress;
	public static volatile SingularAttribute<KeyStoreEntity, Date> expiresOn;
	public static volatile SingularAttribute<KeyStoreEntity, byte[]> keyStore;
	public static volatile SingularAttribute<KeyStoreEntity, Boolean> disabled;
	public static volatile SingularAttribute<KeyStoreEntity, String> password;
}
