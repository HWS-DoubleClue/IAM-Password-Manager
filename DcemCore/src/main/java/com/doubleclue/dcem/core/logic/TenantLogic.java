package com.doubleclue.dcem.core.logic;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.tasks.CoreCreateChangeTenantCall;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;

@ApplicationScoped
@Named("tenantLogic")
public class TenantLogic implements ReloadClassInterface {

	private static Logger logger = LogManager.getLogger(TenantLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	RoleLogic roleLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	ActionLogic actionLogic;
	
	@Inject 
	OperatorSessionBean operatorSessionBean;

	static public TenantEntity getMasterTenant(LocalConfig localConfig) {
		return new TenantEntity(null, localConfig.getDatabase().getDatabaseName(), false, null, true, "master");
	}

	/**
	 * @param dcemOperator
	 * @param dcemAction
	 * @throws SQLException
	 * @throws IOException
	 */

	@DcemTransactional
	public void addOrUpdateTenant(TenantEntity tenantEntity, DcemAction dcemAction, String superAdminPassword, String superAdminPhone, String superAdminEmail,
			SupportedLanguage supportedLanguage, String loginId, String displayName, TimeZone timeZone, boolean audit) throws Exception {
		String auditInfo = null;
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			if (tenantEntity.getSchema() == null) {
				tenantEntity.setSchema(tenantEntity.getName());
			}
			if (getTenantByName(tenantEntity.getName()) != null) {
				throw new DcemException(DcemErrorCodes.TENANT_ALREADY_EXIST, tenantEntity.getName());
			}
			Future<?> future = taskExecutor.submit(new CoreCreateChangeTenantCall(tenantEntity, superAdminPassword, superAdminPhone, superAdminEmail,
					supportedLanguage, loginId, displayName, timeZone));
			try {
				Exception exp = (Exception) future.get();
				if (exp != null) {
					throw exp;
				}
			} catch (Exception exp) {
				logger.info("addOrUpdateTenant", exp);
				throw new DcemException(DcemErrorCodes.EXCEPTION, exp.getMessage());
			}
			em.persist(tenantEntity);
			auditInfo = tenantEntity.getName();
		} else {
			TenantEntity tenantEntityDb = em.find(TenantEntity.class, tenantEntity.getId());
			try {
				tenantEntityDb.setFullName(tenantEntity.getFullName());
				tenantEntityDb.setName(tenantEntity.getName());
				tenantEntityDb.setDisabled(tenantEntity.isDisabled());
			} catch (Exception exp) {
				logger.warn("Couldn't compare operator", exp);
				auditInfo = "ERROR: " + exp.getMessage();
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "", exp);
			}
		}
		if (audit) {
			auditingLogic.addAudit(dcemAction, tenantEntity);
		}
		return;
	}

	public TenantEntity getTenantByName(String tenantName) {
		TypedQuery<TenantEntity> query = em.createNamedQuery(TenantEntity.GET_TENANT, TenantEntity.class);
		query.setParameter(1, tenantName);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@DcemTransactional
	public void deleteTenant(TenantEntity tenantEntity) {
		tenantEntity = em.find(TenantEntity.class, tenantEntity.getId());
		em.remove(tenantEntity);
	}
	
	@DcemTransactional
	public void recoverSuperAdminAccess(TenantEntity tenantEntity, DcemAction dcemAction, String superAdminPassword) throws DcemException {
		Future<?> future = taskExecutor.submit(new CoreCreateChangeTenantCall(tenantEntity, superAdminPassword));
		try {
			Exception exp = (Exception) future.get();
			if (exp != null) {
				throw exp;
			}
		} catch (Exception exp) {
			logger.info(exp);
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "", exp);
		}
		AsModuleApi asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		if (asModuleApi != null) {
			try {
				asModuleApi.onRecoverSuperAdminAccess(tenantEntity);
			} catch (Exception exp) {
				logger.warn(exp);
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "", exp);
			}
		}
		auditingLogic.addAudit(dcemAction, tenantEntity.getName());
	}

	public List<TenantEntity> getAllTenants() {
		TypedQuery<TenantEntity> query = em.createNamedQuery(TenantEntity.GET_ALL, TenantEntity.class);
		return query.getResultList();
	}

	

	public TenantEntity getTenantById(int selectedTenantOptions) {
		TypedQuery<TenantEntity> query = em.createNamedQuery(TenantEntity.GET_TENANT_BY_ID, TenantEntity.class);
		query.setParameter(1, selectedTenantOptions);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@Override
	public void reload(String info) throws DcemException {
		applicationBean.updateInitializeTenantMap();
	}
}
