package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.logic.UrlTokenType;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.110+0200")
@StaticMetamodel(UrlTokenEntity.class)
public class UrlTokenEntity_ {
	public static volatile SingularAttribute<UrlTokenEntity, String> urlToken;
	public static volatile SingularAttribute<UrlTokenEntity, LocalDateTime> expiryDate;
	public static volatile SingularAttribute<UrlTokenEntity, String> objectIdentifier;
	public static volatile SingularAttribute<UrlTokenEntity, UrlTokenType> urlTokenType;
}
