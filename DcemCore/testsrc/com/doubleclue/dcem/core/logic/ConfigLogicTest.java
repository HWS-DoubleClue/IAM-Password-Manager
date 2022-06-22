/**
 * 
 */
package com.doubleclue.dcem.core.logic;

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.internal.SessionImpl;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongycastle.util.Arrays;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DbEncryption;
import com.doubleclue.dcem.system.logic.SystemModule;

/**
 * @author emanuel.galea
 *
 */
@RunWith(Arquillian.class)
public class ConfigLogicTest extends AbstractArquillianTest {

	@Inject
	EntityManager em;

	@Inject
	ConfigLogic configLogic;

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.module.ConfigLogic#verifyDbKey()}.
	 */
	@Test
	public void testVerifyDbKey() {
		try {
			configLogic.getDbVerification();
		} catch (DcemException exp) {
			logger.error("Couldn't set the DB Verification Key", exp);
			fail(exp.toString());
		}

		Session session = em.unwrap(Session.class);
		
//		session.doWork(new Work() {
//            @Override
//            public void execute(Connection connection) throws SQLException {
//                //connection accessible here
//                System.out.println(connection.getMetaData().getDatabaseProductName());
//            }
//        });
		try {
			Connection conn =  ((SessionImpl)session).connection();
			
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT _value FROM semxx.core_config where _key='"
					+ DcemConstants.CONFIG_KEY_DB_VERIFICATION + "' and moduleId='" + SystemModule.MODULE_ID + "'");
			if (rs.next() == false) {
				logger.error("No DB Verification Key found");
				fail();
			}
			byte[] value = rs.getBytes(1);
			if (Arrays.areEqual(ConfigLogic.DB_VERIFICATION, value) == true) {
				logger.error("DB Verification Key is not encrypted at all");
				fail();
			}
			
			if (Arrays.areEqual(ConfigLogic.DB_VERIFICATION, DbEncryption.decryptSeed(value)) == false) {
				logger.error("DB Verification Key is dencrypted failed.");
				fail();
			}
			

		} catch (Exception exp) {
			logger.error("Couldn't set the DB Verification Key", exp);
			fail(exp.toString());
		}
		
	}

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#getClusterConfig()}.
	 */
	// @Test
	// public void testGetClusterConfig() {
	// fail("Not yet implemented");
	// }

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#getModulePreferences(java.lang.String, java.lang.Class)}.
	 */
	// @Test
	// public void testGetModulePreferences() {
	// fail("Not yet implemented");
	// }

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#setModulePreferences(java.lang.String, com.doubleclue.dcem.core.logic.module.ModulePreferences)}.
	 */
	// @Test
	// public void testSetModulePreferences() {
	// fail("Not yet implemented");
	// }

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#getDcemConfiguration(java.lang.String, java.lang.String)}.
	 */
	// @Test
	// public void testGetDcemConfiguration() {
	// fail("Not yet implemented");
	// }

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#setClusterConfig(com.doubleclue.dcem.core.config.ClusterConfig)}.
	 */
	// @Test
	// public void testSetClusterConfig() {
	// fail("Not yet implemented");
	// }

	/**
	 * Test method for {@link com.doubleclue.dcem.core.logic.ConfigLogic#setDcemConfiguration(com.doubleclue.dcem.core.entities.DcemConfiguration)}.
	 */
	// @Test
	// public void testSetDcemConfiguration() {
	// fail("Not yet implemented");
	// }

}
