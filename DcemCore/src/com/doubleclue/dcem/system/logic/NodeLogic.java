package com.doubleclue.dcem.system.logic;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.jpa.DcemTransactional;

@ApplicationScoped
@Named("nodeLogic")
public class NodeLogic {

	@Inject
	private EntityManager em;
	
	@DcemTransactional
	public void addAutoDcemNode(DcemNode dcemNode) {
		dcemNode.setStartedOn(new Date());
		dcemNode.setState(NodeState.Active);
		em.persist(dcemNode);
	}
	
	@DcemTransactional
	public void delete(DcemNode dcemNode) {
		dcemNode = em.merge(dcemNode);
		em.remove(dcemNode);
	}
	
	@DcemTransactional
	public void started(DcemNode dcemNode) {
		dcemNode.setStartedOn(new Date());
		dcemNode.setState(NodeState.Active);
	}
	
	public DcemNode getNodeById(int id) {
		return em.find(DcemNode.class, id);
	}
	
	@DcemTransactional
	public void addDcemNode(DcemNode dcemNode) {
		em.persist(dcemNode);
	}

	public DcemNode getNodeByName(String name) {
		TypedQuery<DcemNode> query = em.createNamedQuery(DcemNode.GET_NODE_BY_NAME, DcemNode.class);
		query.setParameter(1, name);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public void setEntityManager(EntityManager em) {
		this.em = em;
	}

	public List<DcemNode> getNodes() {
		TypedQuery<DcemNode> query = em.createNamedQuery(DcemNode.GET_NODES, DcemNode.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void setNodeState(String name, NodeState state) {
		DcemNode node = getNodeByName(name);
		node.setState(state);
		if (state == NodeState.Off) {
			node.setWentDownOn(new Date());
		}
		if (state == NodeState.Active) {
			node.setStartedOn(new Date());
		}
	}
}
