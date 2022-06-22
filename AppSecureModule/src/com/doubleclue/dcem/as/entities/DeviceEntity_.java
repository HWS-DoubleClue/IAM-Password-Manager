package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.logic.DeviceStatus;
import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-12-06T22:31:53.960+0100")
@StaticMetamodel(DeviceEntity.class)
public class DeviceEntity_ {
	public static volatile SingularAttribute<DeviceEntity, Integer> id;
	public static volatile SingularAttribute<DeviceEntity, DcemUser> user;
	public static volatile SingularAttribute<DeviceEntity, String> name;
	public static volatile SingularAttribute<DeviceEntity, DeviceState> state;
	public static volatile SingularAttribute<DeviceEntity, DeviceStatus> status;
	public static volatile SingularAttribute<DeviceEntity, AsVersionEntity> asVersion;
	public static volatile SingularAttribute<DeviceEntity, Date> lastLoginTime;
	public static volatile SingularAttribute<DeviceEntity, String> appOsVersion;
	public static volatile SingularAttribute<DeviceEntity, String> manufacture;
	public static volatile SingularAttribute<DeviceEntity, String> risks;
	public static volatile SingularAttribute<DeviceEntity, String> locale;
	public static volatile SingularAttribute<DeviceEntity, Integer> retryCounter;
	public static volatile SingularAttribute<DeviceEntity, byte[]> udid;
	public static volatile SingularAttribute<DeviceEntity, byte[]> deviceHash;
	public static volatile SingularAttribute<DeviceEntity, Integer> offlineCounter;
	public static volatile SingularAttribute<DeviceEntity, byte[]> offlineKey;
	public static volatile SingularAttribute<DeviceEntity, byte[]> deviceKey;
	public static volatile SingularAttribute<DeviceEntity, byte[]> publicKey;
	public static volatile SingularAttribute<DeviceEntity, Integer> nodeId;
}
