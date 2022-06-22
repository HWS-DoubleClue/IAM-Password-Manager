package com.doubleclue.dcem.as.entities;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-09-28T14:55:09.987+0200")
@StaticMetamodel(CloudSafeEntity.class)
public class CloudSafeEntity_ {
	public static volatile SingularAttribute<CloudSafeEntity, Integer> id;
	public static volatile SingularAttribute<CloudSafeEntity, CloudSafeOwner> owner;
	public static volatile SingularAttribute<CloudSafeEntity, DcemUser> user;
	public static volatile SingularAttribute<CloudSafeEntity, DeviceEntity> device;
	public static volatile SingularAttribute<CloudSafeEntity, String> name;
	public static volatile SingularAttribute<CloudSafeEntity, Long> length;
	public static volatile SingularAttribute<CloudSafeEntity, CloudSafeEntity> parent;
	public static volatile SingularAttribute<CloudSafeEntity, DcemUser> lastModifiedUser;
	public static volatile SingularAttribute<CloudSafeEntity, Date> lastModified;
	public static volatile SingularAttribute<CloudSafeEntity, Date> discardAfter;
	public static volatile SingularAttribute<CloudSafeEntity, String> options;
	public static volatile SingularAttribute<CloudSafeEntity, byte[]> salt;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> isFolder;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> isGcm;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> recycled;
	public static volatile SingularAttribute<CloudSafeEntity, DcemGroup> group;
}
