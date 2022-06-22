import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hibernate.AnnotationException;
import org.hibernate.boot.MappingException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.core.jpa.DatabaseTypes;

public class CreateTables {

	public static void main(String[] args) {

		// PersistenceXmlParser parser = new PersistenceXmlParser(new ClassLoaderServiceImpl(),
		// PersistenceUnitTransactionType.RESOURCE_LOCAL);
		// List<ParsedPersistenceXmlDescriptor> allDescriptors = parser.doResolve(new HashMap<>());

		if (args.length < 1) {
			System.err.println("Need Workspace Location as first parameter");
			System.exit(-1);
		}

		String outputDir = args[0] + File.separator + "DcemCore" + File.separator + "etc" + File.separator + "db";
		System.out.println("Output Directory = " + outputDir);

		for (DatabaseTypes databaseType : DatabaseTypes.values()) {
			System.out.println("CreateTables.main() Database Type: " + databaseType);
			Enumeration<URL> persistenceRes = null;
			try {
				persistenceRes = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

			while (persistenceRes.hasMoreElements()) {
				URL persistenceFileUrl = persistenceRes.nextElement();
				String path = persistenceFileUrl.getPath();
				// if (path.contains("/bin/")) {
				// continue;
				// }

				System.out.println("CreateTables.main() Path=" + path);

				// int ind = path.indexOf("Module/");
				// if (ind == -1) {
				// continue;
				// }
				// int ind2 = path.lastIndexOf('/', ind);
				// if (ind2 == -1) {
				// continue;
				// }
				// String moduleName = path.substring(ind2 + 1, ind);
				// System.out.println("Creating Tables for " + moduleName);

				HashSet<String> classesMap = new HashSet<String>();

				DocumentBuilder builder = null;
				try {
					builder = factory.newDocumentBuilder();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// cfg.setProperty("hibernate.hbm2ddl.auto", "create");
				// cfg.setProperty("hibernate.dialect", databaseType.getHibernateDialect());
				// cfg.setProperty("hibernate.id.new_generator_mappings", "true");

				Map<String, String> settings = new HashMap<>();
				// settings.put("connection.driver_class", "com.mysql.jdbc.Driver");
				settings.put("hibernate.dialect", databaseType.getHibernateDialect());
				settings.put("hibernate.connection.url", "jdbc:derby:memory:myDB;create=true");
				// settings.put("hibernate.connection.username", "root");
				// settings.put("hibernate.connection.password", "");
				// settings.put("hibernate.id.new_generator_mappings", "true");
				// settings.put("hibernate.hbm2ddl.auto", "create");
				// settings.put("hibernate.connection.driver_class", databaseType.getDriver());
				// settings.put("javax.persistence.jdbc.driver", databaseType.getDriver());
				// settings.put("hibernate.connection.datasource", "xxxxxxxxxx");
				// settings.put("show_sql", "true");
				MetadataSources metadata = new MetadataSources(new StandardServiceRegistryBuilder().applySettings(settings).build());

				Document persistenceDocument = null;
				try {
					persistenceDocument = builder.parse(persistenceFileUrl.openStream());
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList result = null;
				NodeList module = null;
				try {
					result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);
					module = (NodeList) xPath.evaluate("/persistence/persistence-unit/@name", persistenceDocument, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					e.printStackTrace();
					continue;
				}

				if (result.getLength() == 0) {
					continue;
				}

				Node node = module.item(0);
				if (node == null) {
					System.out.println("ERROR:  No Module Defined in " + path);
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
						e.printStackTrace();
						continue;
					}
				}

				SchemaExport export = new SchemaExport();
				export.setDelimiter(";");
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
					export.setOutputFile(outputFile);
					export.setFormat(true);
					export.setHaltOnError(true);
								EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.SCRIPT);
					export.execute(targetTypes, SchemaExport.Action.CREATE, metadata.buildMetadata());
					System.out.println("CreateTables.main() exported for: " + outputFile);
				} catch (AnnotationException exp) {
					System.err.println("ERROR ERROR ERROR: Somthing is wrong with your annotation for Module: " + moduleName);
					System.out.println();
					exp.printStackTrace();
					System.exit(-1);
				} catch (MappingException exp) {
					System.err.println("ERROR ERROR ERROR: JPA Mapping Problem: " + moduleName);
					System.out.println();
					exp.printStackTrace();
					System.exit(-1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Tables created, now improving the sql scripts");
		File inputDirectory = new File(outputDir);
		File outputDirectory = new File(args[0] + File.separator + "DcemCore" + File.separator + "resources" + File.separator + "com/doubleclue/dcem/db");
		try {
			System.out.println("						Input directory:		" + inputDirectory.getPath());
			System.out.println("						Output directory:		" + outputDirectory.getPath());
			ConvertSqlFiles.convertSqlDirectories(inputDirectory, outputDirectory);
		} catch (Exception e) {
			System.out.println("Couldn't convert tables");
			e.printStackTrace();
			System.exit(-1);
		}
		System.exit(0);
	}

}
