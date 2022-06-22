package com.doubleclue.dcem.core.licence;

import static org.junit.Assert.fail;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactionInterceptor;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.test.EmProducerTest;

@RunWith(Arquillian.class)
public class LicenceLogicTest  {
	
	@Deployment
	public static JavaArchive createTestArchive() {

		return ShrinkWrap.create(JavaArchive.class, "test.jar").addPackages(true, "com.doubleclue.dcem.core", "com.doubleclue.dcem.core.licence")
				.addClasses(addDependencies(com.doubleclue.dcem.core.test.EmProducerMySqlTest.class))
				.addAsManifestResource(new ByteArrayAsset(("<beans><interceptors><class>"
						+ DcemTransactionInterceptor.class.getName() + "</class></interceptors></beans>").getBytes()),
						ArchivePaths.create("beans.xml"));
	}

	public static Class<?>[] addDependencies(Class<? extends EmProducerTest> producerClass) {
		return new Class<?>[] { producerClass, DcemModule.class, LicenceLogicInterface.class, 
				DcemApplicationBean.class, LocalConfigProvider.class

		};
	}
	
	@Inject
	LicenceLogicInterface licenceLogic;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
//		System.setProperty("DCEM_HOME", "C:\\Users\\Emanuel\\Workspaces\\karasystems\\DcemParent\\etc\\DCEM_HOME_TEST");
//		LocalConfig localConfig = LocalConfigProvider.readConfig();
//		DcemApplicationBean.jUnitTestMode = true;
//		AppWsConnection  appWsConnection= new AppWsConnection ();   // create the singleTon
//		DcemContextListener contextListener = new DcemContextListener();
//		contextListener.contextInitialized(null);
	}

	@Test
	public void testGetEncryptedLicence() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDecryptedLicenceByteArrayString() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetDecryptedLicenceByteArray() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetEncryptedLicenceAsString() throws DcemException {
		LicenceKeyContent licenceContent = licenceLogic.createTrialLicence(10, null);
		licenceContent.setClusterId("0123456789abcdef");
		licenceContent.setCustomerName("HWS");
		System.out.println("LicenceLogicTest.testGetEncryptedLicenceAsString() ");
		String licenceString = licenceLogic.getEncryptedLicenceAsString(licenceContent);
		System.out.println("LicenceLogicTest.testGetEncryptedLicenceAsString() " + licenceString);
		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetLicence() {
		
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAllLicencesFromCoreConfig() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testGetAllLicencesFromModules() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetLicenceLicenceContent() {
//		fail("Not yet implemented"); // TODO
	}

	@Test
	public void testSetLicenceString() {
//		fail("Not yet implemented"); // TODO
	}
	
	

}


