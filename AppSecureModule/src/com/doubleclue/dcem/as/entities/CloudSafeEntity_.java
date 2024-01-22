package com.doubleclue.dcem.as.entities;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2024-01-22T11:19:06.435+0100")
@StaticMetamodel(CloudSafeEntity.class)
public class CloudSafeEntity_ {
	public static volatile SingularAttribute<CloudSafeEntity, Integer> id;
	public static volatile SingularAttribute<CloudSafeEntity, CloudSafeOwner> owner;
	public static volatile SingularAttribute<CloudSafeEntity, DcemUser> user;
	public static volatile SingularAttribute<CloudSafeEntity, DeviceEntity> device;
	public static volatile SingularAttribute<CloudSafeEntity, DcemGroup> group;
	public static volatile SingularAttribute<CloudSafeEntity, String> name;
	public static volatile SingularAttribute<CloudSafeEntity, Long> length;
	public static volatile SingularAttribute<CloudSafeEntity, CloudSafeEntity> parent;
	public static volatile SingularAttribute<CloudSafeEntity, DcemUser> lastModifiedUser;
	public static volatile SingularAttribute<CloudSafeEntity, LocalDateTime> lastModified;
	public static volatile SingularAttribute<CloudSafeEntity, LocalDateTime> discardAfter;
	public static volatile SingularAttribute<CloudSafeEntity, String> options;
	public static volatile SingularAttribute<CloudSafeEntity, byte[]> salt;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> isFolder;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> isGcm;
	public static volatile SingularAttribute<CloudSafeEntity, Boolean> recycled;
}
