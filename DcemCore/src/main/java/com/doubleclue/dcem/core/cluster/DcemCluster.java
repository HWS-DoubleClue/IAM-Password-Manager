package com.doubleclue.dcem.core.cluster;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

public class DcemCluster {

	private final static Logger logger = LogManager.getLogger(DcemCluster.class);

	static DcemCluster dcemCluster = new DcemCluster();

	static public DcemCluster getInstance() {
		return dcemCluster;
	}
	
	private FlakeIdGenerator flakeIdGenerator;

	private String nodeName;
	private DcemNode dcemNode;
	private HazelcastInstance hazelcastInstance = null;
	private ClusterConfig clusterConfig = null;
	
	byte [] clusterKey;

	public Member startCluster(ClusterConfig clusterConfig, String nodeNameTmp) throws Exception {

		this.clusterConfig = clusterConfig;
		nodeName = nodeNameTmp;
		Config config = null;
		InputStream inputStream;
		File file = LocalPaths.getClusterConfig();
		if (file.exists()) {
			try {
				inputStream = new FileInputStream(file);
				logger.info("User Cluster Configuration File: " + file.getAbsolutePath());
			} catch (FileNotFoundException e) {
				throw new DcemException(DcemErrorCodes.CLUSTER_CONFIG_FILE_NOT_FOUND, e.getMessage());
			}
		} else {
			inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(DcemConstants.CLUSTER_CONFIG_PATH);
		}

		try {
			config = new XmlConfigBuilder(inputStream).build();
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CLUSTER_CONFIG_FILE_NOT_FOUND, e.getMessage());
		}
		String clusterGroupName = clusterConfig.getGivenName();
		if (clusterGroupName == null || clusterGroupName.isEmpty()) {
			clusterGroupName = clusterConfig.getName().substring(8);
		}
		config.getGroupConfig().setName(clusterGroupName);
		config.getGroupConfig().setPassword(clusterConfig.getPassword());
		logger.info("Cluster Configuration: " + config.toString());

		config.setInstanceName(DcemConstants.CLUSTER_INTANCE_NAME);
		// config.getCPSubsystemConfig().setCPMemberCount(3); // TODO not sure about the number
		hazelcastInstance = Hazelcast.newHazelcastInstance(config);
		Cluster cluster = hazelcastInstance.getCluster();
		cluster.addMembershipListener(new ClusterMembershipListener());

		Member member = cluster.getLocalMember();
		member.setStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE, nodeName);
		clusterKey = Arrays.copyOf (clusterConfig.getName().getBytes(DcemConstants.CHARSET_ISO_8859_1), 32);
		byte [] data = clusterConfig.getPassword().getBytes(DcemConstants.CHARSET_ISO_8859_1);
		System.arraycopy(data, 0, clusterKey, 16, data.length);
		flakeIdGenerator = hazelcastInstance.getFlakeIdGenerator("default");
		return member;
	}
	
	public static DcemCluster getDcemCluster() {
		return dcemCluster;
	}

	public ClusterConfig getClusterConfig() {
		return clusterConfig;
	}

	public Set<Member> getMembers() {
		Cluster cluster = hazelcastInstance.getCluster();
		Set<Member> members = cluster.getMembers();
		// for (Member member : members) {
		// Map<String, Object> map = member.getAttributes();
		// }
		return members;
	}

	public MultiMap<?, ?> getMultiMap(String name) {
		MultiMap<?, ?> map = hazelcastInstance.getMultiMap(name);
		return map;
	}

	public IMap<?, ?> getMap(String name) {
		IMap<?, ?> imap = hazelcastInstance.getMap(name);
		return imap;
	}

	public IList<?> getList(String name) {
		IList<?> list = hazelcastInstance.getList(name);
		return list;
	}

	public HazelcastInstance getHazelcastInstance() {
		return hazelcastInstance;
	}

	public FlakeIdGenerator getIdGenerator(String name) {
		return hazelcastInstance.getFlakeIdGenerator(name);
	}

	public boolean isFirstStartedNode() {
		Set<Member> set = getMembers();
		return (set.size() == 1);
	}

	public String getNodeName() {
		return nodeName;
	}

	public DcemNode getDcemNode() {
		return dcemNode;
	}

	public void setDcemNode(DcemNode dcemNode) {
		this.dcemNode = dcemNode;
	}

	public boolean isClusterMaster() {
		Set<Member> set = getMembers();
		Member firstMember = set.iterator().next();
		if (firstMember.getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE).equals(nodeName)) {
			return true;
		}
		return false;
	}
	
	public Member getClusterMaster() {
		Set<Member> set = getMembers();
		return set.iterator().next();
	}

	public IExecutorService getExecutorService() {
		return hazelcastInstance.getExecutorService("default");
	}

	/**
	 * @param node
	 * @return
	 */
	public Member getMember(DcemNode node) {
		Set<Member> members = getMembers();
		for (Member member : members) {
			if (member.getStringAttribute(DcemConstants.NODE_NAME_ATTRIBUTE).equals(node.getName())) {
				return member;
			}
		}
		return null;
	}

	public void setClusterConfig(ClusterConfig clusterConfig) {
		this.clusterConfig = clusterConfig;
	}

	public byte[] getClusterKey() {
		return clusterKey;
	}


	public long getGeneratedId() {
		return flakeIdGenerator.newId();
	}
}
