package com.doubleclue.dcem.core.entities;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2019-07-24T10:41:09.984+0200")
@StaticMetamodel(DcemStatistic.class)
public class DcemStatistic_ {
	public static volatile SingularAttribute<DcemStatistic, Integer> id;
	public static volatile SingularAttribute<DcemStatistic, Date> timestamp;
	public static volatile SingularAttribute<DcemStatistic, DcemNode> dcemNode;
	public static volatile SingularAttribute<DcemStatistic, String> data;
}
