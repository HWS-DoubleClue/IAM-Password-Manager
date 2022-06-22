package com.doubleclue.dcem.core.entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2018-01-07T22:04:06.406+0100")
@StaticMetamodel(DcemConfiguration.class)
public class DcemConfiguration_ {
	public static volatile SingularAttribute<DcemConfiguration, Integer> id;
	public static volatile SingularAttribute<DcemConfiguration, String> nodeId;
	public static volatile SingularAttribute<DcemConfiguration, String> moduleId;
	public static volatile SingularAttribute<DcemConfiguration, String> key;
	public static volatile SingularAttribute<DcemConfiguration, byte[]> value;
}
