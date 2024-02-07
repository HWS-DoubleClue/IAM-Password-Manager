package com.doubleclue.dcem.core.logic;
/**
 * 
 */

import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.doubleclue.dcem.core.DcemContextListener;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.DcemTransactionInterceptor;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.test.EmProducerTest;

/**
 * @author emanuel.galea
 *
 */
@RunWith(Arquillian.class)
public abstract class AbstractArquillianTest {
	
	static final Logger logger = LogManager.getLogger(AbstractArquillianTest.class);


	@Deployment
	public static JavaArchive createTestArchive() {

		return ShrinkWrap.create(JavaArchive.class, "test.jar")
				.addPackages(true, "com.doubleclue.dcem.core", "com.doubleclue.dcem.system", "com.doubleclue.dcem.licence")
				.addClasses(addDependencies(com.doubleclue.dcem.core.test.EmProducerMySqlTest.class))
				.addAsManifestResource(new ByteArrayAsset(("<beans><interceptors><class>"
						+ DcemTransactionInterceptor.class.getName() + "</class></interceptors></beans>").getBytes()),
						ArchivePaths.create("beans.xml"));
	}

	public static Class<?>[] addDependencies(Class<? extends EmProducerTest> producerClass) {
		return new Class<?>[] { producerClass, EntityManager.class, DcemModule.class, 
				DcemApplicationBean.class, LocalConfigProvider.class

		};
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
//		System.setProperty("SEM_HOME", "C:\\Users\\Emanuel\\Workspaces\\karasystems\\SemParent\\etc\\SEM_HOME_TEST");
		LocalConfig localConfig = LocalConfigProvider.readConfig();
		DcemApplicationBean.jUnitTestMode = true;
		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
		dbFactoryProducer.createEmp(localConfig);
		DcemContextListener contextListener = new DcemContextListener();
		contextListener.contextInitialized(null);
	}

}
