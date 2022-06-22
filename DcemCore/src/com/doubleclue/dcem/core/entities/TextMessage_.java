package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2016-05-10T17:06:25.560+0200")
@StaticMetamodel(TextMessage.class)
public class TextMessage_ {
	public static volatile SingularAttribute<TextMessage, Integer> id;
	public static volatile SingularAttribute<TextMessage, String> key;
	public static volatile SingularAttribute<TextMessage, String> value;
	public static volatile SingularAttribute<TextMessage, TextResourceBundle> textResourceBundle;
	public static volatile SingularAttribute<TextMessage, Integer> jpaVersion;
}
