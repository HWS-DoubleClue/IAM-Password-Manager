//package com.doubleclue.dcem.boot;
///**
// * 
// */
//
//import static org.junit.Assert.fail;
//
//import javax.inject.Inject;
//import javax.persistence.EntityManager;
//
//import org.jboss.arquillian.container.test.api.Deployment;
//import org.jboss.arquillian.junit.Arquillian;
//import org.jboss.shrinkwrap.api.ArchivePaths;
//import org.jboss.shrinkwrap.api.ShrinkWrap;
//import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
//import org.jboss.shrinkwrap.api.spec.JavaArchive;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import com.doubleclue.dcem.core.config.ClusterConfig;
//import com.doubleclue.dcem.core.config.LocalConfig;
//import com.doubleclue.dcem.core.config.LocalConfigProvider;
//import com.doubleclue.dcem.core.entities.DcemConfiguration;
//import com.doubleclue.dcem.core.exceptions.DcemException;
//import com.doubleclue.dcem.core.gui.DcemApplicationBean;
//import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
//import com.doubleclue.dcem.core.jpa.DcemTransactionInterceptor;
//import com.doubleclue.dcem.core.logic.ConfigLogic;
//import com.doubleclue.dcem.core.logic.module.DcemModule;
//import com.doubleclue.dcem.core.test.EmProducerTest;
//import com.doubleclue.dcem.core.utils.DcemUtils;
//import com.fasterxml.jackson.core.JsonProcessingException;
//
///**
// * @author Emanuel
// *
// */
//@RunWith(Arquillian.class)
//public class MainTest {
//
//	@Deployment
//	public static JavaArchive createTestArchive() {
//
//		return ShrinkWrap.create(JavaArchive.class, "test.jar").addPackages(true, "com.doubleclue.dcem.core", "com.doubleclue.dcem.as")
//				.addClasses(addDependencies(com.doubleclue.dcem.core.test.EmProducerMySqlTest.class))
//				.addAsManifestResource(new ByteArrayAsset(("<beans><interceptors><class>"
//						+ DcemTransactionInterceptor.class.getName() + "</class></interceptors></beans>").getBytes()),
//						ArchivePaths.create("beans.xml"));
//	}
//
//	public static Class<?>[] addDependencies(Class<? extends EmProducerTest> producerClass) {
//		return new Class<?>[] { producerClass, EntityManager.class, DcemModule.class,  
//				DcemApplicationBean.class, LocalConfigProvider.class
//
//		};
//	}
//
//	@Inject
//	ConfigLogic configLogic;
//
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@Before
//	public void setUp() throws Exception {
//		LocalConfig localConfig = LocalConfigProvider.readConfig();
//		DcemApplicationBean.jUnitTestMode = true;
//		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
//		try {
//			dbFactoryProducer.createEmp(localConfig);
//		} catch (Exception e) {
//			fail(e.toString());
//		}
//	}
//
//	/**
//	 * Test method for {@link com.doubleclue.dcem.as.comm.AppToServerHandler#disconnect()}.
//	 * @throws DcemException 
//	 * @throws JsonProcessingException 
//	 */
//	@Test
//	public void testMain() throws DcemException, JsonProcessingException {
//
//		ClusterConfig clusterConfig = new ClusterConfig();
//		clusterConfig.setDefault();
//		DcemConfiguration dcemConfiguration =configLogic.createClusterConfig(clusterConfig);
//		configLogic.setDcemConfiguration(dcemConfiguration);
//
//		ClusterConfig clusterConfig2 = configLogic.getClusterConfig();
//		if (clusterConfig2 == null) {
//			fail("no object returned");
//		}
//
//		try {
//			DcemUtils.compareObjects(clusterConfig, clusterConfig2);
//		} catch (Exception e) {
//			fail(e.getMessage());
//		}
//	}
//
//}
