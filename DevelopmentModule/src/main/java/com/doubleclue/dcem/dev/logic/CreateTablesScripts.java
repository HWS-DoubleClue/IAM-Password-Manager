package com.doubleclue.dcem.dev.logic;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import org.hibernate.AnnotationException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.core.entities.DependencyClasses;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.ConvertSqlFiles;

@ApplicationScoped
@Named("createTablesScripts")
public class CreateTablesScripts {

	private static Logger logger;

	public void createTables(String moduleDirectory, String moduleResources, DcemModule dcemModule) throws Exception {

//		Path currentRelativePath = Paths.get("");
//		String modulePath = currentRelativePath.toAbsolutePath().toString();
//		System.out.println("Current absolute path is: " + modulePath);
				
		
		String outputDir = moduleDirectory + File.separator + "target" + File.separator + "tables";
		File outputDirFile = new File(outputDir);
		try {
			FileUtils.deleteDirectory(new File(outputDir));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		outputDirFile.delete();

		System.out.println("Output Directory = " + outputDir);
		String persistencePath = moduleResources + "META-INF" + File.separator + "persistence.xml";
		File persistenceFile = new File(persistencePath);
		if (persistenceFile.exists() == false) {
			throw new Exception ("ERROR: 'persistence.xml' NOT FOUND In " + persistencePath);
		}
		System.out.println("CreateTables.main() Path=" + persistencePath);
		String moduleName = null;
		String persitensUnitName = "dcem." + dcemModule.getId();
		
		for (DatabaseTypes databaseType : DatabaseTypes.values()) {
			System.out.println("CreateTables.main() Database Type: " + databaseType);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			HashSet<String> classesMap = new HashSet<String>();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new Exception ("ERROR:Cannot create DocumentBuilder " + persistencePath, e);
			}

			Map<String, String> settings = new HashMap<>();
			// settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
			settings.put("hibernate.dialect", databaseType.getHibernateDialect());
			settings.put("hibernate.connection.url", "jdbc:derby:memory:myDB;create=true");
			MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(settings).build());
			Document persistenceDocument = null;
			try {
				persistenceDocument = builder.parse(new FileInputStream(persistencePath));
			} catch (Exception e) {
				throw new Exception ("ERROR: Parsing 'persistence.xml' " + persistencePath, e);
			}
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList result = null;
			NodeList module = null;
			try {
				module = (NodeList) xPath.evaluate("/persistence/persistence-unit/@name", persistenceDocument, XPathConstants.NODESET);
				result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);

			} catch (XPathExpressionException e) {
				throw new Exception("ERROR: Parsing 'persistence.xml' " + persistencePath);
			}

			if (result.getLength() == 0) {
				continue;
			}

			Node node = module.item(0);
			if (node == null) {
				System.out.println("ERROR:  No Module Defined in " + persistencePath);
				continue;
			}
			moduleName = module.item(0).getNodeValue();
			if (moduleName.equals(persitensUnitName) == false) {
				throw new Exception ("Incorrect persistence-unit: " + moduleName + ". It should be: " + persitensUnitName);
			}
			System.out.println("Module Name " + moduleName);
			for (int i = 0; i < result.getLength(); i++) {
				String className = result.item(i).getNodeValue();
				classesMap.add(className);
			}
			for (Class<?> klass : DependencyClasses.getCoreDependencyClasses()) {
				try {
					metadata.addAnnotatedClass(klass);
				} catch (Exception e) {
					throw new Exception("FATAL ERROR: Class not found: " + klass.getCanonicalName());
				}
			}

			Iterator<String> iterator = classesMap.iterator();
			while (iterator.hasNext()) {
				String className = iterator.next();
				try {
					metadata.addAnnotatedClass(Class.forName(className));
					// System.out.println("className " + className);
				} catch (ClassNotFoundException e) {
					throw new Exception("FATAL ERROR: Class not found: " + className);
				}
			}
			
			SchemaExport schemaExport = new SchemaExport();
			schemaExport.setDelimiter(";");
			File file = new File(outputDir + File.separatorChar + databaseType);
			if (file.exists() == false) {
				file.mkdirs();
			}
			try {
				String outputFile = file.getPath() + File.separatorChar + moduleName + "Tables.sql";
				File outfile = new File(outputFile);
				if (outfile.exists()) {
					outfile.delete();
				}
				schemaExport.setOutputFile(outputFile);
				schemaExport.setFormat(true);
				schemaExport.setHaltOnError(true);
				schemaExport.setManageNamespaces(true);
				EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);
				schemaExport.execute(targetTypes, SchemaExport.Action.CREATE, metadata.buildMetadata());
				System.out.println("CreateTables.main() exported");
			} catch (AnnotationException e) {
				throw new Exception("FATAL ERROR: JPA Annotation Exception: " + e.toString());
			} catch (Exception e) {
				throw new Exception("FATAL ERROR: UNKNOWN EXCEPTION: " + e.toString());
			}
		}
		System.out.println("Tables created, now improving the sql scripts");
		File inputDirectory = new File(outputDir);
 		File outputDirectory = new File(moduleResources + File.separator + "com/doubleclue/dcem/db");
		if (outputDirectory.exists() == false) {
			outputDirectory.mkdirs();
		}
		try {
			System.out.println("						Input directory:		" + inputDirectory.getPath());
			System.out.println("						Output directory:		" + outputDirectory.getPath());
			int ind2 = moduleName.indexOf('.');
			if (ind2 != -1) {
				moduleName = moduleName.substring(ind2 + 1);
			}
			ConvertSqlFiles.convertSqlDirectories(inputDirectory, outputDirectory, moduleName);
		} catch (Exception e) {
			throw new Exception("Couldn't convert tables");
		}
		return;
	}

}
