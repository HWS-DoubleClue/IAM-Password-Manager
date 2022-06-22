package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.core.logic.UrlTokenType;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2022-02-18T17:14:53.360+0100")
@StaticMetamodel(UrlTokenEntity.class)
public class UrlTokenEntity_ {
	public static volatile SingularAttribute<UrlTokenEntity, String> urlToken;
	public static volatile SingularAttribute<UrlTokenEntity, Date> expiryDate;
	public static volatile SingularAttribute<UrlTokenEntity, String> objectIdentifier;
	public static volatile SingularAttribute<UrlTokenEntity, UrlTokenType> urlTokenType;
}
