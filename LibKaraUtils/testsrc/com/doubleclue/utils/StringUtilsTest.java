package com.doubleclue.utils;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class StringUtilsTest {

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	@Test
	public void substituteTemplate() {
		String template = "123456789 {{token1}} 123456789 {{token2}} abcde {{token3}} - {{token1}}"; 
		Map<String, String> map = new HashMap<>();
		map.put("token1", "data1");
		map.put("token2", "data222222222");
		map.put("token3", "data3");
		String result = StringUtils.substituteTemplate (template, map);
		if (result.equals("123456789 data1 123456789 data222222222 abcde data3 - data1") == true) {
			return;
		}
		fail("result ist not the same");
	
	}
	
	@Test
	public void wipeStringTest() {
		String secret = "ABCD1234567890";
		try {
			StringUtils.wipeString(secret);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("StringUtilsTest.wipeStringTest() " + secret);		
		
	}

}
