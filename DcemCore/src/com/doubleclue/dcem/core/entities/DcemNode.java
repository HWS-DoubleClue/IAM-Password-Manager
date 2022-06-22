package com.doubleclue.dcem.core.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.doubleclue.dcem.system.logic.NodeState;
import com.hazelcast.core.Member;

/**
 * The persistent class for the user database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "sys_node", uniqueConstraints = { @UniqueConstraint(name = "UK_NODE_NAME", columnNames = { "dc_name" }) })
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({ @NamedQuery(name = DcemNode.GET_NODE_BY_NAME, query = "SELECT node FROM DcemNode node where node.name = ?1"),
		@NamedQuery(name = DcemNode.GET_NODES, query = "SELECT node FROM DcemNode node ORDER BY node.name ASC",  hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = "query.GET_NODES") })

})
public class DcemNode extends EntityInterface implements Serializable {

	// private static Logger logger = LogManager.getLogger(DcemNode.class);

	public final static String GET_NODE_BY_NAME = "dcemNode.nodeByName";

	public static final String GET_NODES = "dcemNode.getNodes";

	@Id
	@TableGenerator(name = "coreSeqStoreNode", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "NODE.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreNode")
	@Column(name = "dc_id")
	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	private Integer id;

	@Column(name = "dc_name", length = 64, nullable = false, updatable = true)
	@DcemGui(style = "width: 20em")
	private String name;

	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	NodeState state;

	// @Column(name = "dc_address", length = 255, nullable = true)
	@Transient
	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	String address;

	@Temporal(TemporalType.TIMESTAMP)
	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	Date startedOn;

	@Temporal(TemporalType.TIMESTAMP)
	@DcemGui(displayMode = DisplayModes.TABLE_ONLY)
	Date wentDownOn;

	public String getAddress() {
		Set<Member> members = DcemCluster.getDcemCluster().getMembers();
		for (Member member : members) {
			String nodeName = member.getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE);
			if (this.getName().equals(nodeName)) {
				if (DcemCluster.getDcemCluster().getDcemNode().name.equals(this.getName()))  {
					return "This: " + member.getAddress().getHost();
				}
				return member.getAddress().getHost();
			}
		}
		return "NOT REACHABLE";
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public DcemNode() {

	}

	public DcemNode(String name, NodeState state, String address) {
		super();
		this.name = name;
		this.state = state;
		this.address = address;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Number uid) {
		this.id = (Integer) uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getStartedOn() {
		return startedOn;
	}

	public void setStartedOn(Date startedOn) {
		this.startedOn = startedOn;
	}

	public Date getWentDownOn() {
		return wentDownOn;
	}

	public void setWentDownOn(Date wentDownOn) {
		this.wentDownOn = wentDownOn;
	}

	public NodeState getState() {
		return state;
	}

	public void setState(NodeState state) {
		this.state = state;
	}

	
	@Override
	public String toString() {
		return name;
	}

}