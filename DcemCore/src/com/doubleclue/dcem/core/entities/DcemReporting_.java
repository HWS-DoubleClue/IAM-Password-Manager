package com.doubleclue.dcem.core.entities;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.ReportAction;
import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2021-01-04T17:13:38.734+0100")
@StaticMetamodel(DcemReporting.class)
public class DcemReporting_ {
	public static volatile SingularAttribute<DcemReporting, Long> id;
	public static volatile SingularAttribute<DcemReporting, Date> time;
	public static volatile SingularAttribute<DcemReporting, AlertSeverity> severity;
	public static volatile SingularAttribute<DcemReporting, String> source;
	public static volatile SingularAttribute<DcemReporting, ReportAction> action;
	public static volatile SingularAttribute<DcemReporting, String> errorCode;
	public static volatile SingularAttribute<DcemReporting, DcemUser> user;
	public static volatile SingularAttribute<DcemReporting, String> location;
	public static volatile SingularAttribute<DcemReporting, String> info;
	public static volatile SingularAttribute<DcemReporting, Boolean> showOnDashboard;
}
