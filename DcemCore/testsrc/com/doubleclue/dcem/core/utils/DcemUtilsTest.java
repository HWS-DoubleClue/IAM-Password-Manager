package com.doubleclue.dcem.core.utils;

import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.doubleclue.dcem.core.exceptions.DcemException;
public class DcemUtilsTest {

//	@Test
//	public void testGetManifestInformation() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertFieldToViewVariableFieldResourceBundleStringObject() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testConvertFieldToViewVariableFieldResourceBundleStringObjectArrayListOfSingularAttributeOfQQ() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testPopulateTable() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetHtmlInput() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEvalAsListString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEvalAsString() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testEvalAsObject() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCompareObjects() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetHttpBasicAuthentication() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetHierarchyList() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testResourceKeyToName() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testNameToResourceKey() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetViewVariableList() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testGetViewVariableFromId() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetDayBegin() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testSetDayEnd() {
//		fail("Not yet implemented");
//	}

	@Test
	public void testProcessTemplate() {
		String template1 = "FIRST PART #{product} bla #{version} LASTPART";
		String template2 = "FIRST PART #{product  LASTPART";
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("product" , "EnterpriseClient");
		String result = null;
		map.put("version" , "10.10");
		try {
			result = DcemUtils.processTemplate(template1, map);
		} catch (DcemException e) {
			Assertions.fail("Template1 failed");
		}
		Assertions.assertEquals(result, "FIRST PART EnterpriseClient bla 10.10 LASTPART");
		
		map.remove("version");
		try {
			result = DcemUtils.processTemplate(template1, map);
			Assertions.fail("Template1 should have thrown an exception");
		} catch (DcemException e) {
			
		}
				
		
		try {
			result = DcemUtils.processTemplate(template2, map);
		} catch (DcemException e) {
			return;
		}
		Assertions.fail("Bad Template should return an exception");
		
		
	}

}
