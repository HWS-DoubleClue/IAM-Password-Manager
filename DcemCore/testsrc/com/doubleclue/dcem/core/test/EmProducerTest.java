package com.doubleclue.dcem.core.test;

import java.util.List;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Disposes;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.EntityManagerProducer;

/**
 * 
 * @author Emanuel Galea
 *
 */
public abstract class EmProducerTest {

	protected static Logger logger = LogManager.getLogger(EmProducerTest.class);
	
    protected static final String DATABASE_NAME = "database.name";
    
	protected EntityManagerFactory emf = null;
	protected EntityManagerFactory systemEmf = null;
	
	protected abstract Map<String, Object> getSetting();

	public abstract EntityManager produceMasterEntityManager();
//	public abstract com.kobil.ssms.kernel.logic.jpa.EntityManagerProducer produceEMP();

	protected EntityManagerProducer getEntityManagerProducer()
	{
		return new EntityManagerProducer() {

			@Override
			public EntityManager getUnmanagedEntitymanager() {

				if (emf == null) {
					emf = createMasterEntitymanagerFactory();
				}

				return emf.createEntityManager(); 
			}
		};
	}
	
	protected EntityManagerFactory createMasterEntitymanagerFactory() {
		
		Map<String, Object> settings = getSetting();
		settings.put("hibernate.generate_statistics", "false");
		settings.put("hibernate.jdbc.batch_size", "20");
		
		/**
		 * ehCache specific 
		 */
//		settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
//		settings.put("hibernate.cache.use_second_level_cache", "false");
		settings.put("format_sql", "false");
		settings.put("hibernate.cache.use_query_cache", "false");
		settings.put("hibernate.cache.region_prefix", "");

		return initEntityManagerFactory("ssms_system", settings);
	}
	
	protected EntityManagerFactory createEntitymanagerFactory(){
		String puName = "sem.core";
		
		Map<String, Object> settings = getSetting();
		settings.put("hibernate.generate_statistics", "false");
		settings.put("hibernate.jdbc.batch_size", "20");
		
		/**
		 * ehCache specific 
		 */
//		settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
//		settings.put("hibernate.cache.use_second_level_cache", "false");
		settings.put("format_sql", "false");
		settings.put("hibernate.cache.use_query_cache", "false");
		settings.put("hibernate.cache.region_prefix", "");
		
//		settings.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA.name());
//		settings.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER,
//				MultiTenantConnectionProviderImpl.class.getName());
//		settings.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, TenantIdResolverImpl.class.getName());

		return initEntityManagerFactory(puName, settings);
	}

	private EntityManagerFactory initEntityManagerFactory(String puName, Map<String, Object> settings) {

		logger.info("starting DBMS handling");
		
		List<Class<?>> entitties = null;
//		try {
//			entitties = EntityManagerHelper.getPersistenceClasses();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
		settings.put(org.hibernate.jpa.AvailableSettings.LOADED_CLASSES,  entitties);
		return Persistence.createEntityManagerFactory(puName, settings);
	}

	// This method is not invoked automatically and needs to be implemented in the subclass
	protected abstract void disposeEntityManager(@Disposes EntityManager em);

	/** This method is not invoked automatically, so it needs to be invoked from the subclass
	  * Implement disposeEntityManager and call super.disposeEM(em).
	  */
	protected void disposeEM(@Disposes EntityManager em) {
		if (em != null && em.isOpen()) {
			em.close();
		}
	}
	
	@PreDestroy
	public void deinit() {
		if (emf != null) {
			emf.close();
		}
	}
}
