package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-01-06T14:07:49.735+0100")
@StaticMetamodel(DomainConfig.class)
public class DomainConfig_ {
	public static volatile SingularAttribute<DomainConfig, Boolean> remote;
	public static volatile SingularAttribute<DomainConfig, Boolean> verifyCertificate;
	public static volatile SingularAttribute<DomainConfig, String> authConnectorName;
}
