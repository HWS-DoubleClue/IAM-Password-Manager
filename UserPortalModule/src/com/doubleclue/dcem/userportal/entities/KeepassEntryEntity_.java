package com.doubleclue.dcem.userportal.entities;

import com.doubleclue.dcem.userportal.logic.AppHubApplication;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-03-16T09:17:47.135+0100")
@StaticMetamodel(KeepassEntryEntity.class)
public class KeepassEntryEntity_ {
	public static volatile SingularAttribute<KeepassEntryEntity, String> uuid;
	public static volatile SingularAttribute<KeepassEntryEntity, AppHubApplication> application;
	public static volatile SingularAttribute<KeepassEntryEntity, String> name;
	public static volatile SingularAttribute<KeepassEntryEntity, ApplicationHubEntity> applicationEntity;
}
