package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.system.logic.NodeState;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-08-28T15:41:27.040+0200")
@StaticMetamodel(DcemNode.class)
public class DcemNode_ {
	public static volatile SingularAttribute<DcemNode, Integer> id;
	public static volatile SingularAttribute<DcemNode, String> name;
	public static volatile SingularAttribute<DcemNode, NodeState> state;
	public static volatile SingularAttribute<DcemNode, Date> startedOn;
	public static volatile SingularAttribute<DcemNode, Date> wentDownOn;
}
