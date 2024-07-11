package com.doubleclue.dcem.core.jpa;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CacheRegionStatistics;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.system.logic.SystemModule;

/**
 * Produces an entity manager from the given config file.
 * 
 * @author Emanuel Galea
 * 
 */
@ApplicationScoped
public class EntityManagerProducer {

	private static final Logger logger = LogManager.getLogger(EntityManagerProducer.class);

	@Inject
	SystemModule systemModule;

	private DatabaseTypes dbType;

	private EntityManagerFactory emf = null;

	boolean dbStatistics;
	HashMap<String, StatisticCounter> dbStatisticMap = new HashMap<String, StatisticCounter>();

	private static final String QUERY_FROM = "from ";

	// private EntityManagerFactory systemEmf = null;

	@PostConstruct
	public void init() {
		emf = DbFactoryProducer.getInstance().getEntityManagerFactory();
	}

	@SuppressWarnings("unused")
	@PreDestroy
	private void deinit() {
		logger.info("stopping DBMS handling");
		if (emf != null) {
			emf.close();
			if (logger.isDebugEnabled()) {
				logger.debug("entity manager factory closed");
			}
		}
	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}

	/**
	 * produce RequestScoped entity manager
	 * 
	 * @return
	 */
	@Produces
	@RequestScoped
	EntityManager produceEntityManager() { // InjectionPoint injectionPoint
		// if (logger.isDebugEnabled()) {
		// logger.debug("Create Tenant entity manager");
		// }
		EntityManager em = emf.createEntityManager();
		// System.out.println("EntityManagerProducer.produceEntityManager() " +
		// em);
		return em;
	}

	void disposeEntityManager(@Disposes EntityManager em) {
		// System.out.println("EntityManagerProducer.disposeEntityManager() " +
		// em);
		// if (logger.isTraceEnabled()) {
		// logger.trace("Destroying entity manager");
		// }
		try {
			if (em != null && em.isOpen()) {
				if (em.getTransaction() != null && em.getTransaction().isActive()) {
					logger.warn("Transaction is still active");
				}
				em.close();
			}
		} catch (Exception exp) {
			if (logger.isDebugEnabled()) {
				logger.debug("Destroying entity manager", exp);
			}
		}

	}

	/**
	 * this function should only be used by methods that must use an entity
	 * manager and are not called during a request. For Request(scoped) usage
	 * just usage normal injection!
	 * 
	 * currently the only place where this is used is for application startup
	 * functions
	 * 
	 * @return
	 */
	public EntityManager getUnmanagedEntitymanager() {
		return emf.createEntityManager();
	}

	/**
	 * used to just initialize the EMFs
	 */
	public void ping() {
		return;
	}

	/**
	 * parse all META-INF/persistance.xml's and add all entities to the
	 * persistence context
	 * 
	 * @param puConfiguration
	 * @throws IOException
	 */
	public static List<Class<?>> getPersistenceClasses() throws IOException {

		HashSet<String> classesMap = new HashSet<String>();

		Enumeration<URL> persistenceXMLres = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			while (persistenceXMLres.hasMoreElements()) {
				URL persistenceXMLURL = persistenceXMLres.nextElement();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document persistenceDocument = builder.parse(persistenceXMLURL.openStream());
				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);

				for (int i = 0; i < result.getLength(); i++) {
					String className = result.item(i).getNodeValue();
					classesMap.add(className);
				}

			}
			ArrayList<Class<?>> list = new ArrayList<Class<?>>(classesMap.size());
			Iterator<String> iterator = classesMap.iterator();
			Class<?> cls = null;
			;
			while (iterator.hasNext()) {
				String className = iterator.next();
				cls = Class.forName(className);
				list.add(cls);
			}

			return list;
		} catch (Exception exp) {
			logger.error("Please check Installation, couldn't read the persistence.xml of modules", exp);
			throw new IOException(exp);
		}
	}

	public DatabaseTypes getDbType() {
		return dbType;
	}

	public void close() {
		if (emf != null) {
			emf.close();
		}

	}

	synchronized public void enableDbStatistics(boolean enable) {
		if ((dbStatistics == false) && (enable == true)) {
			dbStatisticMap = new HashMap<String, StatisticCounter>();
		}
		dbStatistics = enable;
		SessionFactory sf = emf.unwrap(SessionFactory.class);
		sf.getStatistics().setStatisticsEnabled(enable);
		return;
	}

	synchronized public void clearDbStatistics() {
		SessionFactory sf = emf.unwrap(SessionFactory.class);
		for (StatisticCounter statisticCounter : dbStatisticMap.values()) {
			statisticCounter.reset();
		}
		sf.getStatistics().clear();
		return;
	}

	/**
	 * 
	 * 
	 * @param emf
	 */
	synchronized public Map<String, StatisticCounter> getQueryStatistics(EntityManagerFactory emf) {
		SessionFactory sf = emf.unwrap(SessionFactory.class);
		org.hibernate.stat.Statistics statistics = sf.getStatistics();
		StatisticCounter statisticsRecord;
		if (logger.isDebugEnabled()) {
			logger.debug("DB-Statistics: " + statistics.toString());
			statisticsRecord = new StatisticCounter();
			statisticsRecord.count = statistics.getSecondLevelCacheHitCount();
			dbStatisticMap.put("DB-Global SecondLevelHits", statisticsRecord);
			String[] regions = statistics.getSecondLevelCacheRegionNames();
			for (String region : regions) {
				try {
					CacheRegionStatistics regionStatistic = statistics.getDomainDataRegionStatistics(region);
					logger.debug("DB-SecondLayerCache: " + regionStatistic.toString());
					statisticsRecord = new StatisticCounter();
					statisticsRecord.setCount(regionStatistic.getHitCount());
					statisticsRecord.setAveTime(regionStatistic.getMissCount());
					dbStatisticMap.put("Hit-Missed: " + region.toString(), statisticsRecord);
				} catch (Exception exp) {

				}
			}
		}

		String[] entityNames = statistics.getEntityNames();
		for (String entity : entityNames) {
			String name = "ENTITY: -" + entity;
			org.hibernate.stat.EntityStatistics entityStatistics = statistics.getEntityStatistics(entity);
			if (entityStatistics.getLoadCount() != 0) {
				statisticsRecord = dbStatisticMap.get(name);
				if (statisticsRecord == null) {
					statisticsRecord = new StatisticCounter();
					dbStatisticMap.put(name, statisticsRecord);
				}
				statisticsRecord.count = entityStatistics.getLoadCount();
				if (entityStatistics.getCacheRegionName() != null) {
					statisticsRecord.aveTime = entityStatistics.getCacheHitCount();
					statisticsRecord.longestTime = entityStatistics.getCacheMissCount();
				} else {
					statisticsRecord.aveTime = -1;
					statisticsRecord.longestTime = -1;
				}
			}
		}

		String[] queries = statistics.getQueries();
		for (String query : queries) {
			String queryLow = query.toLowerCase();
			int startName = queryLow.indexOf(QUERY_FROM);
			if (startName >= 0) {
				startName += QUERY_FROM.length();
				int endName = queryLow.indexOf(' ', startName + QUERY_FROM.length());
				String name = "QUERY: " + query.substring(startName, endName);
				org.hibernate.stat.QueryStatistics queryStatistics = statistics.getQueryStatistics(query);
				statisticsRecord = dbStatisticMap.get(name);
				if (statisticsRecord == null) {
					statisticsRecord = new StatisticCounter();
					dbStatisticMap.put(name, statisticsRecord);
				}
				statisticsRecord.count = queryStatistics.getExecutionCount();
				statisticsRecord.aveTime = queryStatistics.getExecutionAvgTime();
				statisticsRecord.longestTime = queryStatistics.getExecutionMaxTime();
			}
		}
		return dbStatisticMap;
	}

}
