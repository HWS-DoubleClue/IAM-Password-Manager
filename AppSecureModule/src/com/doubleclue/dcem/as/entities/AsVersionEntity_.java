package com.doubleclue.dcem.as.entities;

import com.doubleclue.comm.thrift.ClientType;
import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-08-16T07:59:43.002+0200")
@StaticMetamodel(AsVersionEntity.class)
public class AsVersionEntity_ {
	public static volatile SingularAttribute<AsVersionEntity, Integer> id;
	public static volatile SingularAttribute<AsVersionEntity, String> name;
	public static volatile SingularAttribute<AsVersionEntity, ClientType> clientType;
	public static volatile SingularAttribute<AsVersionEntity, String> versionStr;
	public static volatile SingularAttribute<AsVersionEntity, DcemUser> user;
	public static volatile SingularAttribute<AsVersionEntity, Date> expiresOn;
	public static volatile SingularAttribute<AsVersionEntity, String> downloadUrl;
	public static volatile SingularAttribute<AsVersionEntity, String> informationUrl;
	public static volatile SingularAttribute<AsVersionEntity, Integer> version;
	public static volatile SingularAttribute<AsVersionEntity, Boolean> disabled;
	public static volatile SingularAttribute<AsVersionEntity, Boolean> testApp;
	public static volatile SingularAttribute<AsVersionEntity, Integer> jpaVersion;
}
