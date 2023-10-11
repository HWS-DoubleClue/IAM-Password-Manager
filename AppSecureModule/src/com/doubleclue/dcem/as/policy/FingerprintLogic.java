package com.doubleclue.dcem.as.policy;

import java.time.LocalDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doubleclue.dcem.as.entities.FingerprintId;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.UserFingerprintEntity;
import com.doubleclue.dcem.core.jpa.DcemTransactional;

@ApplicationScoped
@Named("fingerprintLogic")
public class FingerprintLogic {

	@Inject
	EntityManager em;

	public boolean verifyFingerprint(Integer userId, Integer id, String fingerprint) {
		if (fingerprint == null) { 
			return false;
		}
		UserFingerprintEntity userFingerprintEntity = getFingerprint(userId, id);
		if (userFingerprintEntity == null) {
			return false;
		}
		if (fingerprint != null) {
			if (userFingerprintEntity.getFingerprint() == null) {
				return false;
			}
			if (LocalDateTime.now().isBefore(userFingerprintEntity.getTimestamp()) && userFingerprintEntity.getFingerprint().equals(fingerprint)) {
				return true;
			}
		}
		return false;
	}

	
	@DcemTransactional
	public void updateFingerprint(UserFingerprintEntity userFingerprintEntity) {
		UserFingerprintEntity existingUserFingerprintEntity = em.find(UserFingerprintEntity.class, userFingerprintEntity.getId());
		if (existingUserFingerprintEntity == null) { // add
			em.persist(userFingerprintEntity);
		} else {   // update
			existingUserFingerprintEntity.setTimestamp(userFingerprintEntity.getTimestamp());
			userFingerprintEntity.setFingerprint(existingUserFingerprintEntity.getFingerprint());
		}
	}
	
	
	public UserFingerprintEntity getFingerprint(FingerprintId fingerprintId) {
		return em.find(UserFingerprintEntity.class, fingerprintId);
	}
	
	
	

	@DcemTransactional
	public void deleteFingerPrint(Integer userId, PolicyAppEntity appEntity) {
		UserFingerprintEntity entity = em.find(UserFingerprintEntity.class, new FingerprintId(userId, appEntity));
		if (entity != null) {
			em.remove(entity);
		}
	}

	private UserFingerprintEntity getFingerprint(Integer userId, Integer appId) {
		return em.find(UserFingerprintEntity.class, new FingerprintId(userId, appId));
	}

	@DcemTransactional
	public void deleteExpiredFingerprints() {
		Query query = em.createNamedQuery(UserFingerprintEntity.DELETE_EXPIRED_FP);
		query.setParameter(1, LocalDateTime.now());
		query.executeUpdate();
	}

	@DcemTransactional
	public void deleteUserFingerprints(int userId) {
		Query query = em.createNamedQuery(UserFingerprintEntity.DELETE_USER_FP);
		query.setParameter(1, userId);
		query.executeUpdate();
	}

	@DcemTransactional
	public void deleteFingerPrint(Integer userId, Integer fingerprintIdForApp) {
		UserFingerprintEntity entity = em.find(UserFingerprintEntity.class, new FingerprintId(userId, fingerprintIdForApp));
		if (entity != null) {
			em.remove(entity);
		}

	}
}
