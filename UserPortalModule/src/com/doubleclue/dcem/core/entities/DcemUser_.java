package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.gui.SupportedLanguage;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-03-31T11:43:38.454+0200")
@StaticMetamodel(DcemUser.class)
public class DcemUser_ {
	public static volatile SingularAttribute<DcemUser, Integer> id;
	public static volatile SingularAttribute<DcemUser, DcemUserExtension> dcemUserExt;
	public static volatile SingularAttribute<DcemUser, String> displayName;
	public static volatile SingularAttribute<DcemUser, String> loginId;
	public static volatile SingularAttribute<DcemUser, String> userPrincipalName;
	public static volatile SingularAttribute<DcemUser, DomainEntity> domainEntity;
	public static volatile SingularAttribute<DcemUser, String> email;
	public static volatile SingularAttribute<DcemUser, String> privateEmail;
	public static volatile SingularAttribute<DcemUser, DcemRole> dcemRole;
	public static volatile SingularAttribute<DcemUser, String> telephoneNumber;
	public static volatile SingularAttribute<DcemUser, String> mobileNumber;
	public static volatile SingularAttribute<DcemUser, SupportedLanguage> language;
	public static volatile SingularAttribute<DcemUser, Boolean> disabled;
	public static volatile SingularAttribute<DcemUser, Date> acSuspendedTill;
	public static volatile SingularAttribute<DcemUser, Integer> failActivations;
	public static volatile SingularAttribute<DcemUser, String> userDn;
	public static volatile SingularAttribute<DcemUser, Integer> jpaVersion;
	public static volatile SingularAttribute<DcemUser, byte[]> hashPassword;
	public static volatile SingularAttribute<DcemUser, String> saveit;
	public static volatile SingularAttribute<DcemUser, Date> lastLogin;
	public static volatile SingularAttribute<DcemUser, String> privateMobileNumber;
	public static volatile SingularAttribute<DcemUser, byte[]> hmac;
	public static volatile SingularAttribute<DcemUser, byte[]> salt;
	public static volatile SingularAttribute<DcemUser, Integer> passCounter;
	public static volatile SingularAttribute<DcemUser, byte[]> objectGuid;
}
