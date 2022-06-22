package com.doubleclue.dcem.core.test;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;

/**
 * 
 * @author Emanuel Galea
 *
 */
public abstract class EmProducerMySqlTest extends EmProducerTest{

	@Inject
	LocalConfigProvider localConfigProvider;
	
	@Produces
	@RequestScoped
	//@PersistenceContext(unitName = "ssms_kernel")
	public EntityManager produceEntityManager() {
		
		logger.debug("produceEntityManager()");
		
		if (emf == null) {
			emf = createMasterEntitymanagerFactory();
		}
		return emf.createEntityManager();
	}
	
	
	
	@Produces
	public EntityManagerProducer produceEMP(){
		return getEntityManagerProducer();
	}

	protected void disposeEntityManager(@Disposes EntityManager em) {
		super.disposeEM(em);
	}

	protected Map<String, Object> getSetting() {

		UnitTestUtils.setSemInstallIfnotSet();
		UnitTestUtils.setSSMSHomeIfnotSet();
		
//		Properties databaseProperties = null;
//		try {
//			databaseProperties = UnitTestUtils.getDatabaseProperties(DatabaseTypes.MYSQL);
//		} catch (IOException e) {
//			throw new RuntimeException("Failed to load config file for mysql", e);
//		}

//		String schema = databaseProperties.getProperty(DATABASE_NAME);
		

		
		// settings
		Map<String, Object> settings = new HashMap<String, Object>();
		
//		String fullDbUrl = databaseProperties
//				.getProperty(DatabaseUtils.DATABASE_URL)
//				+ "/" + schema;

		settings.put(DbFactoryProducer.HIBERNATE_C3P0_CHECKOUT_TIMEOUT, Integer.toString(30000)); // milliseconds
		settings.put(DbFactoryProducer.HIBERNATE_C3P0_TIMEOUT, Integer.toString(600));
		settings.put(DbFactoryProducer.HIBERNATE_C3P0_ACQUIRE_RETRY_ATTEMPTS, Integer.toString(30));
		settings.put(DbFactoryProducer.HIBERNATE_C3P0_ACQUIRE_RETRY_DELAY, Integer.toString(1000));
		settings.put(DbFactoryProducer.HIBERNATE_C3P0_MIN_SIZE, Integer.toString(1));
		settings.put(DbFactoryProducer.HIBERNATE_C3P0_MAX_SIZE, Integer.toString(10));

//		settings.put("javax.persistence.jdbc.user", databaseProperties
//				.getProperty(DatabaseUtils.DATABASE_USER));
//		settings.put("javax.persistence.jdbc.password", databaseProperties
//				.getProperty(DatabaseUtils.DATABASE_PASS));
//		settings.put("javax.persistence.jdbc.url", fullDbUrl);

		settings.put("hibernate.dialect",
				"org.hibernate.dialect.MySQLInnoDBDialect");
		
		settings.put("hibernate.show_sql", "true");

		return settings;
	}
}
