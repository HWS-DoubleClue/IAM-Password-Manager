package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "core_statistic", indexes = {@Index (name = "statisticTimestamp", columnList = "dc_timestamp") })
@NamedQueries({
	
	@NamedQuery(name=DcemStatistic.GET_TIMESTAMPS,
	query="SELECT sr.timestamp FROM DcemStatistic AS sr ORDER BY sr.timestamp ASC"),
	
	@NamedQuery(name=DcemStatistic.GET_STATISTICS,
	query="select sr from DcemStatistic as sr where sr.dcemNode =?1 ORDER BY sr.timestamp ASC"),
	
	@NamedQuery(name=DcemStatistic.GET_STATISTICS_ALL,
	query="select sr from DcemStatistic as sr ORDER BY sr.timestamp ASC"),
	
 	@NamedQuery(name=DcemStatistic.GET_STATISTICS_FROM_TO,
	query="select sr from DcemStatistic as sr where sr.timestamp BETWEEN ?1 AND ?2 ORDER BY sr.timestamp ASC"),
	
	@NamedQuery(name=DcemStatistic.GET_STATISTICS_TIME,
	query="SELECT sr FROM DcemStatistic AS sr WHERE sr.timestamp = ?1"),

})
public class DcemStatistic extends EntityInterface implements Serializable {
	
	public final static String GET_TIMESTAMPS = "DcemStatisticTimestamps";
	public final static String GET_STATISTICS_TIME = "DcemStatisticValue";
	public final static String GET_STATISTICS = "DcemStatistics";
	public final static String GET_STATISTICS_ALL = "DcemStatisticsAll";
	public final static String GET_STATISTICS_FROM_TO = "DcemStatisticsFromTo";


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreDcemStatistic", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "SEM_STATISTIC.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreDcemStatistic")
	private Integer id;
	
	@Column(name="dc_timestamp", nullable=false )
	private LocalDateTime timestamp;
	
	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn(name="nodeId", nullable = true, foreignKey = @ForeignKey(name = "FK_APP_STATISTIC_NODE"), insertable = true, updatable = false)
	private DcemNode dcemNode;
	
	@Lob()
	@Column(name="dc_data", nullable=false )
	private String data;
	
	public Integer getId() {
		return id;
	}
	public void setId(Number id) {
		this.id = (Integer) id;
	}
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public DcemNode getDcemNode() {
		return dcemNode;
	}
	public void setDcemNode(DcemNode dcemNode) {
		this.dcemNode = dcemNode;
	}
}