package com.doubleclue.dcem.radius.entities;

import com.doubleclue.dcem.radius.logic.RadiusReportAction;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-02-04T13:25:39.376+0100")
@StaticMetamodel(RadiusReportEntity.class)
public class RadiusReportEntity_ {
	public static volatile SingularAttribute<RadiusReportEntity, Integer> id;
	public static volatile SingularAttribute<RadiusReportEntity, Date> time;
	public static volatile SingularAttribute<RadiusReportEntity, String> nasClientName;
	public static volatile SingularAttribute<RadiusReportEntity, RadiusReportAction> action;
	public static volatile SingularAttribute<RadiusReportEntity, Boolean> error;
	public static volatile SingularAttribute<RadiusReportEntity, String> details;
}
