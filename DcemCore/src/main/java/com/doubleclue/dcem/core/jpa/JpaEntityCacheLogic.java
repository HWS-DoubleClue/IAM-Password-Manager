package com.doubleclue.dcem.core.jpa;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.tasks.FlushCacheTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.hazelcast.core.IList;

@ApplicationScoped
public class JpaEntityCacheLogic {

	private static final Logger logger = LogManager.getLogger(JpaEntityCacheLogic.class);

	@Inject
	EntityManager em;

	@Inject
	TaskExecutor taskExecutor;

	ScheduledFuture<?> schedule;

	Map<String, JpaEntityCache<?>> caches = new HashMap<>();
	private static final int FLUSH_TIME_MINUTES = 15;  

	@PostConstruct
	public void init() {
		schedule = taskExecutor.scheduleAtFixedRate(new FlushCacheTask(null, null), FLUSH_TIME_MINUTES, FLUSH_TIME_MINUTES, TimeUnit.MINUTES);
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityInterface> void addNewCache(String cacheName, JpaEntityUpdater<T> updater) {
		String tenantCache = cacheName + AppSystemConstants.TENANT_SEPERATOR + TenantIdResolver.getCurrentTenantName();
		IList<T> iList = (IList<T>) DcemCluster.getInstance().getList(tenantCache);
		caches.put(tenantCache, new JpaEntityCache<T>(iList, updater));
	}

	@SuppressWarnings("unchecked")
	public <T extends EntityInterface> void addToCache(String cacheName, JpaEntityUpdater<T> updater, T entity) {
		String tenantCache = cacheName + AppSystemConstants.TENANT_SEPERATOR + TenantIdResolver.getCurrentTenantName();
		JpaEntityCache<T> jpaEntityCache = (JpaEntityCache<T>) caches.get(tenantCache);
		if (jpaEntityCache ==  null) {
			IList<T> iList = (IList<T>) DcemCluster.getInstance().getList(tenantCache);
			jpaEntityCache = new JpaEntityCache<T>(iList, updater);
			caches.put(tenantCache, jpaEntityCache);
		}
		jpaEntityCache.add(entity);
	}
	
	@DcemTransactional
	public void startFlushCache(String cacheName) {
		Callable<Exception> flushCacheTaskCall = new FlushCacheTask(cacheName, TenantIdResolver.getCurrentTenantName());
		Future<Exception> future = DcemCluster.getInstance().getExecutorService().submitToMember(flushCacheTaskCall,
				DcemCluster.getInstance().getClusterMaster());
		try {
			future.get();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@DcemTransactional
	public void flushCacheOnly(String cacheName) {
		String tenantCache = cacheName + AppSystemConstants.TENANT_SEPERATOR + TenantIdResolver.getCurrentTenantName();
		JpaEntityCache<?> ent = caches.get(tenantCache);
		if (ent != null) {
			ent.flush(em);
		}
	}

	@DcemTransactional
	public void flushAll() {
		int count;
		JpaEntityCache<? extends EntityInterface> cache;
		String tenant = AppSystemConstants.TENANT_SEPERATOR + TenantIdResolver.getCurrentTenantName();
		for (String cacheName : caches.keySet()) {
			if (cacheName.endsWith(tenant)) {
				cache = caches.get(cacheName);
				count = cache.flush(em);
				if (logger.isDebugEnabled() && count > 0) {
					logger.debug("Entity cache = " + cacheName + ", flushed: " + count);
				}
			}
		}
	}
}
