package com.doubleclue.dcem.userportal.entities;

import com.doubleclue.dcem.userportal.logic.AppHubApplication;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-03-16T10:14:19.872+0100")
@StaticMetamodel(ApplicationHubEntity.class)
public class ApplicationHubEntity_ {
	public static volatile SingularAttribute<ApplicationHubEntity, Integer> id;
	public static volatile SingularAttribute<ApplicationHubEntity, AppHubApplication> application;
	public static volatile SingularAttribute<ApplicationHubEntity, byte[]> logo;
	public static volatile SingularAttribute<ApplicationHubEntity, String> name;
}
