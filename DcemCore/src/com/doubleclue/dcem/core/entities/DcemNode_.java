package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.system.logic.NodeState;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-10-11T10:54:25.076+0200")
@StaticMetamodel(DcemNode.class)
public class DcemNode_ {
	public static volatile SingularAttribute<DcemNode, Integer> id;
	public static volatile SingularAttribute<DcemNode, String> name;
	public static volatile SingularAttribute<DcemNode, NodeState> state;
	public static volatile SingularAttribute<DcemNode, LocalDateTime> startedOn;
	public static volatile SingularAttribute<DcemNode, LocalDateTime> wentDownOn;
}
