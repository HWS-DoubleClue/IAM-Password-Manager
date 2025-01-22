package com.doubleclue.dcem.as.logic;

import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.jpa.DcemTransactional;

@ApplicationScoped
@Named("cloudSafeTagLogic")
public class CloudSafeTagLogic {

	@Inject
	EntityManager em;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@DcemTransactional
	public void addOrUpdateTagEntity(CloudSafeTagEntity cloudSafeTagEntity, DcemAction dcemAction) {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(cloudSafeTagEntity);
		} else {
			em.merge(cloudSafeTagEntity);
		}
	}

	@DcemTransactional
	public void addMultipleTags(List<CloudSafeTagEntity> tags) {
		for (CloudSafeTagEntity tag : tags) {
			em.persist(tag);
		}
	}

	public List<CloudSafeTagEntity> getAllTags() {
		TypedQuery<CloudSafeTagEntity> query = em.createNamedQuery(CloudSafeTagEntity.GET_ALL_TAGS, CloudSafeTagEntity.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void removeTags(Set<CloudSafeTagEntity> tags, DcemAction dcemAction) throws Exception {
		List<CloudSafeEntity> cloudSafeEntitys = cloudSafeLogic.getAllCloudsafesByTag(tags);
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntitys) {
			cloudSafeEntity.getTags().removeAll(tags);
			System.out.println(cloudSafeEntity.getTags());
		}
		for (CloudSafeTagEntity tag : tags) {
			tag = em.merge(tag);
			em.remove(tag);
		}
	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	public List<CloudSafeTagEntity> getAllTagsFromCloudSafe(CloudSafeEntity cloudSafeEntity) {
//		try {
//			TypedQuery<CloudSafeTagEntity> query = em.createNamedQuery(CloudSafeTagEntity.GET_ALL_TAGS_BY_CLOUDSAFE, CloudSafeTagEntity.class);
//			query.setParameter(1, cloudSafeEntity.getId());
//			return query.getResultList();
//		} catch (Exception e) {
//			return null;
//		}
//	}

	public CloudSafeTagEntity getTagById(int id) {
		return em.find(CloudSafeTagEntity.class, id);
	}

	public CloudSafeTagEntity getTagByName(String name) {
		TypedQuery<CloudSafeTagEntity> query = em.createNamedQuery(CloudSafeTagEntity.GET_TAG_BY_NAME, CloudSafeTagEntity.class);
		query.setParameter(1, name);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
		
	}

}
