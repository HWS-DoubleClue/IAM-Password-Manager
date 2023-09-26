package com.doubleclue.dcem.radius.entities;

import com.doubleclue.dcem.radius.logic.RadiusReportAction;
import java.time.LocalDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2023-09-26T14:39:52.482+0200")
@StaticMetamodel(RadiusReportEntity.class)
public class RadiusReportEntity_ {
	public static volatile SingularAttribute<RadiusReportEntity, Integer> id;
	public static volatile SingularAttribute<RadiusReportEntity, LocalDateTime> time;
	public static volatile SingularAttribute<RadiusReportEntity, String> nasClientName;
	public static volatile SingularAttribute<RadiusReportEntity, RadiusReportAction> action;
	public static volatile SingularAttribute<RadiusReportEntity, Boolean> error;
	public static volatile SingularAttribute<RadiusReportEntity, String> details;
}
