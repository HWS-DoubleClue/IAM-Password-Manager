package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-09-23T12:36:11.915+0200")
@StaticMetamodel(CloudSafeShareEntity.class)
public class CloudSafeShareEntity_ {
	public static volatile SingularAttribute<CloudSafeShareEntity, Integer> id;
	public static volatile SingularAttribute<CloudSafeShareEntity, DcemUser> user;
	public static volatile SingularAttribute<CloudSafeShareEntity, DcemGroup> group;
	public static volatile SingularAttribute<CloudSafeShareEntity, CloudSafeEntity> cloudSafe;
	public static volatile SingularAttribute<CloudSafeShareEntity, Boolean> writeAccess;
	public static volatile SingularAttribute<CloudSafeShareEntity, Boolean> restrictDownload;
}
