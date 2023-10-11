package com.doubleclue.dcem.core.entities;

import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.084+0200")
@StaticMetamodel(DcemStatistic.class)
public class DcemStatistic_ {
	public static volatile SingularAttribute<DcemStatistic, Integer> id;
	public static volatile SingularAttribute<DcemStatistic, LocalDateTime> timestamp;
	public static volatile SingularAttribute<DcemStatistic, DcemNode> dcemNode;
	public static volatile SingularAttribute<DcemStatistic, String> data;
}
