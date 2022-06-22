import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.AnnotationException;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.schema.TargetType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.app.DcemMain;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.logging.DcemLogLevel;
import com.doubleclue.dcem.core.logging.LogUtils;

public class CreateModuleTables {
	
	private static Logger logger;

	public static void main(String[] args) {

		// PersistenceXmlParser parser = new PersistenceXmlParser(new ClassLoaderServiceImpl(),
		// PersistenceUnitTransactionType.RESOURCE_LOCAL);
		// List<ParsedPersistenceXmlDescriptor> allDescriptors = parser.doResolve(new HashMap<>());
		
		DcemLogLevel dcemLogLevel = DcemLogLevel.INFO;
		LogUtils.initLog4j(null, null, dcemLogLevel, true);
		logger = LogManager.getLogger(DcemMain.class);

		Path currentRelativePath = Paths.get("");
		String modulePath = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current absolute path is: " + modulePath);

		if (args.length < 1) {
			System.err.println("Please specify the Modules Name in the command parameters");
		}
		int ind = modulePath.lastIndexOf(File.separator);
		modulePath = modulePath.substring(0, ind+1);
		modulePath += args[0];

		String outputDir = modulePath + File.separator + "target" + File.separator + "tables";
		System.out.println("Output Directory = " + outputDir);
		String persistencePath = modulePath + File.separator + "src" + File.separator + "META-INF" + File.separator + "persistence.xml";
		File persistenceFile = new File(persistencePath);
		if (persistenceFile.exists() == false) {
			System.err.println("ERROR: 'persistence.xml' NOT FOUND In " + persistencePath);
			System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
			System.exit(-1);
		}
		System.out.println("CreateTables.main() Path=" + persistencePath);
		for (DatabaseTypes databaseType : DatabaseTypes.values()) {
			System.out.println("CreateTables.main() Database Type: " + databaseType);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			HashSet<String> classesMap = new HashSet<String>();
			DocumentBuilder builder = null;
			try {
				builder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				System.err.println("ERROR:Cannot create DocumentBuilder " + persistencePath);
				System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
				System.exit(-1);
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
				System.err.println("ERROR: Parsing 'persistence.xml' " + persistencePath);
				System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
				System.exit(-1);
			}
			XPath xPath = XPathFactory.newInstance().newXPath();
			NodeList result = null;
			NodeList module = null;
			try {
				result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);
				module = (NodeList) xPath.evaluate("/persistence/persistence-unit/@name", persistenceDocument, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				System.err.println("ERROR: Parsing 'persistence.xml' " + persistencePath);
				System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
				System.exit(-1);
			}

			if (result.getLength() == 0) {
				continue;
			}

			Node node = module.item(0);
			if (node == null) {
				System.out.println("ERROR:  No Module Defined in " + persistencePath);
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
					System.out.println();
					System.err.println("FATAL ERROR: Class not found: " + className);
					e.printStackTrace();
					System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
					System.exit(1);
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
				System.out.println("CreateTables.main() exported");
			} catch (AnnotationException e) {
				System.out.println();
				System.err.println("FATAL ERROR: JPA Annotation Exception: " + e.toString());
				e.printStackTrace();
				System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
				System.exit(1);
			} catch (Exception e) {
				System.out.println();
				System.err.println("FATAL ERROR: UNKNOWN EXCEPTION: " + e.toString());
				e.printStackTrace();
				System.err.println("\n!!!!!!!!!!!!        CreateModuleTables EXIT with ERROR        !!!!!!!!!!!!!!!!!!!");
				System.exit(1);
			}
		}
		System.out.println("Tables created, now improving the sql scripts");
		File inputDirectory = new File(outputDir);
		File outputDirectory = new File(modulePath + File.separator + "resources" + File.separator + "DB-Tables");
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
