package com.doubleclue.dcem.system.logic;

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.utils.SecureServerUtils;

@ApplicationScoped
@Named("keyStoreLogic")
public class KeyStoreLogic {

	private static final Logger logger = LogManager.getLogger(KeyStoreLogic.class);

	@Inject
	EntityManager em;
	
	@Inject
	AdminModule adminModule;

	@Inject
	NodeLogic nodeLogic;

	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void addKeystore(KeyStoreEntity entity) {
		em.persist(entity);
	}

	@DcemTransactional
	public KeyStoreEntity addReplaceKeystore(KeyStore keyStore, KeyStorePurpose purpose, String password, String nodeName, String ipAddress) throws Exception {
		DcemNode dcemNode = null;
		KeyStoreEntity preKeyStoreEntity = null;
		if (purpose == KeyStorePurpose.ROOT_CA || purpose == KeyStorePurpose.Saml_IdP_CA) {
			List<KeyStoreEntity> list = getKeyStoreByPurpose(purpose);
			if (list.isEmpty() == false) {
				preKeyStoreEntity = list.get(0);
			}
			if (preKeyStoreEntity != null) {
				preKeyStoreEntity.setNode(null);
			}
		} else {
			if (nodeName != null) {
				dcemNode = nodeLogic.getNodeByName(nodeName);
			}
			if (dcemNode == null) {
				throw new Exception("Node entity required");
			}
			preKeyStoreEntity = getKeyStoreByPurposeNode(purpose, dcemNode);
		}

		X509Certificate certificate = SecureServerUtils.getCertificateRefactorAlias(keyStore, purpose, password);

		if (preKeyStoreEntity == null) {
			preKeyStoreEntity = new KeyStoreEntity(purpose, SecureServerUtils.serializeKeyStore(keyStore, password), certificate.getSubjectDN().getName(),
					certificate.getNotAfter().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime(), password, dcemNode);
			preKeyStoreEntity.setIpAddress(ipAddress);
			em.persist(preKeyStoreEntity);
		} else {
			preKeyStoreEntity.setCn(certificate.getSubjectDN().getName());
			preKeyStoreEntity.setExpiresOn(certificate.getNotAfter().toInstant().atZone(TimeZone.getDefault().toZoneId()).toLocalDateTime());
			preKeyStoreEntity.setPassword(password);
			preKeyStoreEntity.setKeyStore(SecureServerUtils.serializeKeyStore(keyStore, password));
			preKeyStoreEntity.setIpAddress(ipAddress);
		}
		return preKeyStoreEntity;

	}

	public void setEntityManager(EntityManager entityManager) {
		em = entityManager;
	}

	/**
	 * @param purpose
	 * @param dcemNode
	 * @return
	 */
	public KeyStoreEntity getKeyStoreByPurposeNode(KeyStorePurpose purpose, DcemNode dcemNode) {
		TypedQuery<KeyStoreEntity> query = em.createNamedQuery(KeyStoreEntity.GET_BY_PURPOSE_NODE, KeyStoreEntity.class);
		query.setParameter(1, purpose);
		query.setParameter(2, dcemNode);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * @param purpose
	 * @param dcemNode
	 * @return
	 */
	public List<KeyStoreEntity> getKeyStoreByPurpose(KeyStorePurpose purpose) {
		TypedQuery<KeyStoreEntity> query = em.createNamedQuery(KeyStoreEntity.GET_BY_PURPOSE, KeyStoreEntity.class);
		query.setParameter(1, purpose);
		return query.getResultList();

	}

	public KeyStoreEntity getKeyStoreRoot() throws Exception {
		List<KeyStoreEntity> list = getKeyStoreByPurpose(KeyStorePurpose.ROOT_CA);
		if (list == null || list.isEmpty()) {
			throw new Exception("No root-CA found.");
		}
		return list.get(0);
	}

}
