package com.doubleclue.portaldemo.boot;

import java.util.regex.Pattern;

import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;

public class DcemJarScanFilter implements JarScanFilter {
	
	String pattern = "(^bin.*|^jsf.*|^dcem.*|PortalDemo.*|)";

	@Override
	public boolean check(JarScanType jarScanType, String jarName) {
//		if (jarScanType == JarScanType.TLD) {
//			return false;
//		}
		
		if (Pattern.matches(pattern, jarName)) {
			System.out.println("JAR Match: "  + jarName + " Type: " + jarScanType);
			return true;
		}
//		System.out.println("JAR Do NOT Match: "  + jarName);		
		return false;
	}

}
