package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.logic.DomainType;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-10-19T11:08:08.984+0200")
@StaticMetamodel(DomainEntity.class)
public class DomainEntity_ {
	public static volatile SingularAttribute<DomainEntity, Integer> id;
	public static volatile SingularAttribute<DomainEntity, Integer> rank;
	public static volatile SingularAttribute<DomainEntity, String> name;
	public static volatile SingularAttribute<DomainEntity, DomainType> domainType;
	public static volatile SingularAttribute<DomainEntity, String> host;
	public static volatile SingularAttribute<DomainEntity, String> baseDN;
	public static volatile SingularAttribute<DomainEntity, String> searchAccount;
	public static volatile SingularAttribute<DomainEntity, String> password;
	public static volatile SingularAttribute<DomainEntity, String> filter;
	public static volatile SingularAttribute<DomainEntity, String> loginAttribute;
	public static volatile SingularAttribute<DomainEntity, String> firstNameAttribute;
	public static volatile SingularAttribute<DomainEntity, String> lastNameAttribute;
	public static volatile SingularAttribute<DomainEntity, String> mailAttribute;
	public static volatile SingularAttribute<DomainEntity, String> telephoneAttribute;
	public static volatile SingularAttribute<DomainEntity, String> mobileAttribute;
	public static volatile SingularAttribute<DomainEntity, String> mapEmailDomains;
	public static volatile SingularAttribute<DomainEntity, String> configJson;
	public static volatile SingularAttribute<DomainEntity, Boolean> enable;
	public static volatile SingularAttribute<DomainEntity, Integer> version;
}
