package com.doubleclue.dcem.core.entities;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.primefaces.model.SortOrder;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

/**
 * The persistent class for the app_version database table.
 * 
 */
@Entity
@Table(name = "core_reporting")
@NamedQueries({ @NamedQuery(name = DcemReporting.GET_AFTER, query = "SELECT rp FROM DcemReporting rp where rp.localDateTime < ?1"),
		@NamedQuery(name = DcemReporting.DELETE_AFTER, query = "DELETE FROM DcemReporting rp where rp.localDateTime < ?1"),
		@NamedQuery(name = DcemReporting.DELETE_USER_REPORTS, query = "DELETE FROM DcemReporting rp where rp.user = ?1"),
		@NamedQuery(name = DcemReporting.GET_ALL_REPORTS_COUNT, query = "SELECT COUNT(rp) FROM DcemReporting rp where rp.localDateTime >= ?1 AND rp.localDateTime <= ?2 AND rp.action >= ?3 AND rp.action <= ?4 AND rp.errorCode IS NULL"),
		@NamedQuery(name = DcemReporting.GET_REPORTS_COUNT, query = "SELECT COUNT(rp) FROM DcemReporting rp where rp.localDateTime >= ?1 AND rp.localDateTime <= ?2 AND rp.action = ?3"),
		@NamedQuery(name = DcemReporting.GET_ALL_DASHBOARD_REPORTS, query = "SELECT rp FROM DcemReporting rp where rp.showOnDashboard = true"),
		@NamedQuery(name = DcemReporting.CLOSE_DASHBOARD_REPORT, query = "UPDATE DcemReporting rp SET rp.showOnDashboard = false where rp.id = ?1"),
		@NamedQuery(name = DcemReporting.GET_DASHBOARD_REPORT, query = "select rp FROM DcemReporting rp where rp.showOnDashboard = true AND rp.source = ?1 AND rp.severity = ?2 AND rp.errorCode = ?3 AND (?4 is null or rp.info = ?4)"),
		@NamedQuery(name = DcemReporting.GET_ALL_AUTH_METHODS_COUNT, query = "SELECT NEW com.doubleclue.dcem.core.logic.AuthMethodsActivityDto(rp.action, COUNT(rp)) FROM DcemReporting rp where rp.localDateTime >= ?1 AND rp.localDateTime <= ?2 AND rp.action >= ?3 AND rp.action <= ?4 AND rp.errorCode IS NULL GROUP BY rp.action") })

public class DcemReporting extends EntityInterface implements DataSerializable {

	public final static String GET_AFTER = "DcemReporting.getAfter";
	public static final String DELETE_USER_REPORTS = "DcemReporting.deleteUsrRpts";
	public static final String DELETE_AFTER = "DcemReporting.deleteAfter";
	public static final String GET_ALL_REPORTS_COUNT = "DcemReporting.getAllReportsCount";
	public static final String GET_REPORTS_COUNT = "DcemReporting.getReportsCount";
	public static final String GET_ALL_AUTH_METHODS_COUNT = "DcemReporting.getAllAuthMethodsCount";
	public static final String GET_ALL_DASHBOARD_REPORTS = "DcemReporting.getAllDashboardReports";
	public static final String CLOSE_DASHBOARD_REPORT = "DcemReporting.closeDashboardReport";
	public static final String GET_DASHBOARD_REPORT = "DcemReporting.getDashboardReport";

	public DcemReporting() {
		super();
	}

	public DcemReporting(String source, ReportAction action, DcemUser user, String errorCode, String location, String info, AlertSeverity severity,
			boolean showOnDashboard) {
		super();
		localDateTime = LocalDateTime.now();
		this.source = source;
		this.action = action;
		this.user = user;
		this.errorCode = errorCode;
		this.location = location;
		this.severity = severity;
		this.showOnDashboard = showOnDashboard;
		setInfo(info);
	}

	public DcemReporting(String source, ReportAction action, DcemUser user, String errorCode, String location, String info, AlertSeverity severity) {
		super();
		localDateTime = LocalDateTime.now();
		this.source = source;
		this.action = action;
		this.user = user;
		this.errorCode = errorCode;
		this.location = location;
		this.severity = severity;
		this.showOnDashboard = false;
		setInfo(info);
	}

	public DcemReporting(ReportAction action, DcemUser user, AppErrorCodes errorcode, String location, String info, AlertSeverity severity,
			boolean showOnDashboard) {
		super();
		localDateTime = LocalDateTime.now();
		this.action = action;
		this.user = user;
		if (errorcode != null) {
			this.errorCode = errorcode.name();
		}
		this.location = location;
		setInfo(info);
	}

	public DcemReporting(ReportAction action, DcemUser user, AppErrorCodes errorcode, String location, String info) {
		super();
		localDateTime = LocalDateTime.now();
		this.action = action;
		this.user = user;
		if (errorcode != null) {
			this.errorCode = errorcode.name();
		}
		this.location = location;
		this.severity = AlertSeverity.OK;
		this.showOnDashboard = false;
		setInfo(info);
	}

	@Id
	@Column(name = "dc_id")
	private Long id;

	@Column(name = "dc_time", nullable = false)
	@DcemGui(name="Time", sortOrder = SortOrder.DESCENDING)
	private LocalDateTime localDateTime;

	@Enumerated(EnumType.ORDINAL)
	@DcemGui
	@Column(nullable = false)
	private AlertSeverity severity;

	@DcemGui
	@Column(name = "dc_source")
	private String source;

	@Enumerated(EnumType.ORDINAL)
	@DcemGui
	private ReportAction action;

	@Column(name = "errorCode", nullable = true)
	@DcemGui
	private String errorCode;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_REPORT_USER"), insertable = true, updatable = true)
	private DcemUser user;

	@DcemGui(name = "location")
	@Column(name = "dc_loc")
	private String location;

	@DcemGui(name = "information")
	@Column(name = "info")
	private String info;

	@Column(name = "show_on_dashboard", nullable = false)
	@DcemGui(name = "On Dashboard", visible = false)
	private boolean showOnDashboard;

	

	public ReportAction getAction() {
		return action;
	}

	public void setAction(ReportAction action) {
		this.action = action;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		if (info != null && info.length() > 255) {
			info = info.substring(0, 254);
		}
		this.info = info;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "DcemReporting [time=" + localDateTime + ", action=" + action + ",  user=" + user + ", errorCode=" + errorCode + ", info=" + info + "]";
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Long) id;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getWelcomViewAlertSeverity() {
		switch (severity) {
		case OK:
			return "INFO";
		case WARNING:
			return "WARN";
		case FAILURE:
		case ERROR:
			return "ERROR";

		default:
			return "INFO";
		}
	}

	public AlertSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(AlertSeverity severity) {
		this.severity = severity;
	}

	public boolean isShowOnDashboard() {
		return showOnDashboard;
	}

	public void setShowOnDashboard(boolean showOnDashboard) {
		this.showOnDashboard = showOnDashboard;
	}

	@Override
	public void writeData(ObjectDataOutput out) throws IOException {
		out.writeLong(id);
		out.writeLong(localDateTime.toEpochSecond(ZoneOffset.UTC));
		out.writeInt(user == null ? -1 : user.getId());
		out.writeInt(action.ordinal());
		out.writeUTF(errorCode);
		if (location == null) {
			location = "";
		}
		out.writeUTF(location);
		out.writeUTF(info);
		out.writeUTF(source);
		out.writeInt(severity.ordinal());
		out.writeBoolean(showOnDashboard);
	}

	@Override
	public void readData(ObjectDataInput in) throws IOException {
		id = in.readLong();
		localDateTime = LocalDateTime.ofEpochSecond(in.readLong(), 0, ZoneOffset.UTC); 
		int userId = in.readInt();
		if (userId == -1) {
			user = null;
		} else {
			user = new DcemUser();
			user.setId(userId);
		}
		action = ReportAction.values()[in.readInt()];
		errorCode = in.readUTF();
		location = in.readUTF();
		info = in.readUTF();
		source = in.readUTF();
		severity = AlertSeverity.values()[in.readInt()];
		showOnDashboard = in.readBoolean();
	}

	public String getAlertDisplayString() {
		return severity + " : " + info + "<br>ErrorCode: " + errorCode + "<br>Date: " + localDateTime;
	}

	@Override
	public String getRowStyle() {
		if (severity.equals(AlertSeverity.ERROR)) {
			return "rowErrorClass";
		} else if (severity.equals(AlertSeverity.FAILURE)) {
			return "rowFailurreClass";
		} else if (severity.equals(AlertSeverity.WARNING)) {
			return "rowWarningClass";
		}
		return null;
	}

	public LocalDateTime getLocalDateTime() {
		return localDateTime;
	}

	public void setLocalDateTime(LocalDateTime localDateTime) {
		this.localDateTime = localDateTime;
	}
}