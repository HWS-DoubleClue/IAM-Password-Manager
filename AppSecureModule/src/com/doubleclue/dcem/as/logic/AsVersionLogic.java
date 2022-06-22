package com.doubleclue.dcem.as.logic;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppVersion;
import com.doubleclue.comm.thrift.ClientType;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.tasks.ReplicateVersionEntityTask;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;

@ApplicationScoped
public class AsVersionLogic {

	private static Logger logger = LogManager.getLogger(AsVersionLogic.class);

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	AsModule asModule;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	TaskExecutor taskExecutor;

	// private static Logger logger =
	// LogManager.getLogger(AsVersionLogic.class);

	@Inject
	EntityManager em;

	@DcemTransactional
	public void addUpdateVersion(AsVersionEntity versionEntity, DcemAction dcemAction) {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			versionEntity.setId(null);
			em.persist(versionEntity);
		} else {
			versionEntity = em.merge(versionEntity);
		}
		auditingLogic.addAudit(dcemAction, versionEntity.toString());
	}

	@DcemTransactional
	public void addVersion(AsVersionEntity versionEntity) {
		em.persist(versionEntity);
		replicateTenantVersion(versionEntity, versionEntity);
	}

	public List<AsVersionEntity> getVersions() {
		TypedQuery<AsVersionEntity> query = em.createNamedQuery(AsVersionEntity.GET_VERSIONS, AsVersionEntity.class);
		return query.getResultList();

	}

	public AsVersionEntity getVersion(AppVersion appVersion, ClientType clientType) {
		TypedQuery<AsVersionEntity> query = em.createNamedQuery(AsVersionEntity.GET_VERSION, AsVersionEntity.class);
		query.setParameter(1, appVersion.getName());
		query.setParameter(2, appVersion.getVersion());
		query.setParameter(3, clientType);
		query.setParameter(4, false);
		AsVersionEntity asVersion = null;
		try {
			asVersion = query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
		return asVersion;
	}

	private AsVersionEntity getUniqueVersion(String name, int version, ClientType clientType) {
		TypedQuery<AsVersionEntity> query = em.createNamedQuery(AsVersionEntity.GET_UNIQUE_VERSION, AsVersionEntity.class);
		query.setParameter(1, name);
		query.setParameter(2, version);
		query.setParameter(3, clientType);
		AsVersionEntity asVersion = null;
		try {
			asVersion = query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
		return asVersion;
	}

	// will be deprecated in future
	@DcemTransactional
	public void resetUser(DcemUser dcemUser) {
		Query query = em.createNamedQuery(AsVersionEntity.RESET_USER_VERSION);
		query.setParameter(1, dcemUser);
		query.executeUpdate();
	}

	@DcemTransactional
	public void replicateVersion(AsVersionEntity orgVersionEntity, AsVersionEntity modifiedVersionEntity) {
		AsVersionEntity versionEntity = getUniqueVersion(orgVersionEntity.getName(), orgVersionEntity.getVersion(), orgVersionEntity.getClientType());
		if (modifiedVersionEntity == null) {
			// delete
			if (versionEntity != null) {
				em.remove(versionEntity);
			}
		} else {
			if (versionEntity == null) {
				versionEntity = new AsVersionEntity();
				try {
					versionEntity = (AsVersionEntity) modifiedVersionEntity.clone();
					versionEntity.setId(null);
					em.persist(versionEntity);
				} catch (CloneNotSupportedException e) {
					logger.warn(e);
				}
			} else {
				versionEntity.setExpiresOn(modifiedVersionEntity.getExpiresOn());
				versionEntity.setDisabled(modifiedVersionEntity.isDisabled());
				versionEntity.setInformationUrl(modifiedVersionEntity.getInformationUrl());
				versionEntity.setName(modifiedVersionEntity.getName());
				versionEntity.setVersion(modifiedVersionEntity.getVersion());
				versionEntity.setVersionStr(modifiedVersionEntity.getVersionStr());
			}
		}
	}

	public void replicateTenantVersion(AsVersionEntity orgVersionEntity, AsVersionEntity modifiedVersionEntity) {
		Collection<TenantEntity> tenants = applicationBean.getTenantMap().values();
		int i = 0;
		for (TenantEntity tenantEntity : tenants) {
			if (tenantEntity.getId() == TenantIdResolver.getCurrentTenant().getId()) {
				continue;
			}
			taskExecutor.schedule(new ReplicateVersionEntityTask(orgVersionEntity, modifiedVersionEntity, tenantEntity), (100 + i), TimeUnit.MILLISECONDS);
			i += 2;
		}
	}

	@DcemTransactional
	public void addVersionsToTenant(List<AsVersionEntity> versions) {
		if (versions == null) {
			return;
		}
		for (AsVersionEntity versionEntity : versions) {
			versionEntity.setId(null);
			versionEntity.setUser(null);
			em.persist(versionEntity);
		}
	}

	public List<AsVersionEntity> getDetachedVersions() {
		List<AsVersionEntity> list = getVersions();
		for (AsVersionEntity asVersionEntity : list) {
			em.detach(asVersionEntity);
		}
		return list;
	}

	// @DcemTransactional
	// public void deleteVersionByUser(DcemUser dcemUser) {
	// TypedQuery<AsVersion> query = em.createNamedQuery(AsVersionEntity.GET_BY_USER);
	// query.setParameter(1, dcemUser);
	// query.executeUpdate();
	// }

}
