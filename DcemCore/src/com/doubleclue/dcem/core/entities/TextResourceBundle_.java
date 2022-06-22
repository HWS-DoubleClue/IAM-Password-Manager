package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2016-04-29T13:46:36.509+0200")
@StaticMetamodel(TextResourceBundle.class)
public class TextResourceBundle_ {
	public static volatile SingularAttribute<TextResourceBundle, Integer> id;
	public static volatile SingularAttribute<TextResourceBundle, String> locale;
	public static volatile SingularAttribute<TextResourceBundle, String> baseName;
	public static volatile ListAttribute<TextResourceBundle, TextMessage> messages;
	public static volatile SingularAttribute<TextResourceBundle, Integer> jpaVersion;
}
