package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-10-05T16:44:03.982+0200")
@StaticMetamodel(DomainConfig.class)
public class DomainConfig_ {
	public static volatile SingularAttribute<DomainConfig, Boolean> remote;
	public static volatile SingularAttribute<DomainConfig, Boolean> verifyCertificate;
	public static volatile SingularAttribute<DomainConfig, String> authConnectorName;
	public static volatile SingularAttribute<DomainConfig, String> groupAttribute;
}
