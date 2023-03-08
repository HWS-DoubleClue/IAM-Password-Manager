package com.doubleclue.dcem.setup.logic;

import java.io.File;
import java.net.URL;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.AnnotationException;
import org.hibernate.boot.MappingException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.utils.ConvertSqlFiles;

@ApplicationScoped
public class CreateDbUpdateScripts {

	final Logger logger = LogManager.getLogger(CreateDbUpdateScripts.class);

	public File createMigrationScripts(DatabaseConfig databaseConfig, TargetType targetType) throws Exception {

		String outputDirPath = LocalPaths.getDcemHomeDir() + File.separator + "DatabaseMigrationScripts";
		File outputDir = new File(outputDirPath);
		String outputDirPathTemp = LocalPaths.getDcemHomeDir() + File.separator + "dbTemp";
		logger.info("Output Directory = " + outputDirPath);

		Enumeration<URL> persistenceRes = null;
		persistenceRes = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		File outputDirTemp = new File(outputDirPathTemp, databaseConfig.getDatabaseType());
		if (outputDirTemp.exists() == false) {
			outputDirTemp.mkdirs();
		}
		while (persistenceRes.hasMoreElements()) {
			URL persistenceFileUrl = persistenceRes.nextElement();
			String path = persistenceFileUrl.getPath();
			logger.info("MigratingTables.main() Path=" + path);
			HashSet<String> classesMap = new HashSet<String>();

			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				logger.debug(e);
			}

			Map<String, String> settings = new HashMap<>();
			DatabaseTypes dbType;
			try {
				dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			} catch (IllegalArgumentException exp) {
				throw new DcemException(DcemErrorCodes.INIT_DATABASE, "Database Wrong DB Type", exp);
			}
			// settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
			settings.put("hibernate.dialect", dbType.getHibernateDialect());
			settings.put("hibernate.connection.url", databaseConfig.getJdbcUrl());
			String schemaName = databaseConfig.getSchemaName().trim();
			switch (dbType) {
			case MARIADB:
			case MYSQL:
				settings.put(Environment.DEFAULT_CATALOG, databaseConfig.getDatabaseName());
				break;
			case MSSQL:
				settings.put(Environment.DEFAULT_SCHEMA, schemaName);
				settings.put(Environment.DEFAULT_CATALOG, databaseConfig.getDatabaseName());
				break;
			case POSTGRE:
				settings.put(Environment.DEFAULT_SCHEMA, databaseConfig.getDatabaseName());
				break;
			default:
				break;			
			}
			if (dbType != DatabaseTypes.DERBY) {
				settings.put("hibernate.connection.username", databaseConfig.getAdminName());
				settings.put("hibernate.connection.password", databaseConfig.getAdminPassword());
			}
			// settings.put
			MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(settings).build());
			Document persistenceDocument = null;
			try {
				persistenceDocument = builder.parse(persistenceFileUrl.openStream());
			} catch (Exception e) {
				logger.debug(e);
				continue;
			}
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList result = null;
			NodeList module = null;
			try {
				result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);
				module = (NodeList) xPath.evaluate("/persistence/persistence-unit/@name", persistenceDocument, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				logger.debug(e);
				continue;
			}

			if (result.getLength() == 0) {
				continue;
			}
			Node node = module.item(0);
			if (node == null) {
				logger.debug("ERROR:  No Module Defined in " + path);
				continue;
			}
			String moduleName = module.item(0).getNodeValue();
			for (int i = 0; i < result.getLength(); i++) {
				String className = result.item(i).getNodeValue();
				classesMap.add(className);
			}

			Iterator<String> iterator = classesMap.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next();
				try {
					metadata.addAnnotatedClass(Class.forName(className));
				} catch (ClassNotFoundException e) {
					logger.debug(e);
					continue;
				}
			}

			SchemaUpdate schemaUpdate = new SchemaUpdate();
			schemaUpdate.setDelimiter(";");

			try {
				String outputSqlFile = outputDirTemp.getPath() + File.separatorChar + moduleName + "Tables.sql";
				File outfile = new File(outputSqlFile);
				if (outfile.exists()) {
					outfile.delete();
				}
				schemaUpdate.setOutputFile(outputSqlFile);
				schemaUpdate.setFormat(true);
				schemaUpdate.setHaltOnError(true);
				EnumSet<TargetType> targetTypes = EnumSet.of(targetType);
				schemaUpdate.execute(targetTypes, metadata.buildMetadata());
				metadata.getServiceRegistry().close();
			} catch (AnnotationException exp) {
				logger.error("ERROR: Somthing is wrong with your annotation for Module: " + moduleName, exp);
				throw exp;
			} catch (MappingException exp) {
				logger.error("ERROR: JPA Mapping Problem " + moduleName, exp);
				throw exp;
			} catch (Exception e) {
				throw e;
			}
		}
		if (targetType == TargetType.SCRIPT) {
			System.out.println("Tables created, now improving the sql scripts");
			File inputDirectory = new File(outputDirPathTemp);
			File outputDirectory = new File(outputDirPath);
			try {
				System.out.println("Input directory:		" + inputDirectory.getPath());
				System.out.println("Output directory:		" + outputDirectory.getPath());
				ConvertSqlFiles.convertSqlDirectories(inputDirectory, outputDirectory);
			} catch (Exception exp) {
				logger.error("Couldn't convert files ", exp);
				throw exp;
			}
		}
		return new File(outputDir, databaseConfig.getDatabaseType());
	}

}
