package com.doubleclue.utils;

import java.util.StringTokenizer;


public class ProductVersion {
	
	public final static String SVN_NUMBER = "SCM-Revision";
	
	public static final int VERSION_MAJOR_ABS = 0x3FF;
	public static final int VERSION_MINOR_ABS = 0x3FF;
	public static final int VERSION_RV_ABS = 0x3FF;
	public static final int VERSION_BITS = 10;
	public static final String VERSION_DELIMITER = ".";
	
	String appName;
	String state;
	
	int major;
	int minor;
	int service;
	
	String svnBuildNr;
	
	public ProductVersion(String appName, int version) {
		super();
		this.appName = appName;
		this.major = version >> (VERSION_BITS * 2);
		this.minor = (version >> (VERSION_BITS)) & VERSION_MINOR_ABS;
		this.service = version & VERSION_RV_ABS;
	}
	
	public ProductVersion(String appType, int major, int minor, int service) {
		super();
		this.appName = appType;
		this.major = major;
		this.minor = minor;
		this.service = service;
	}
	
	public ProductVersion(String appName, String maniFestVersion) throws Exception {
		this.appName = appName;
		int ind = maniFestVersion.lastIndexOf('-');
		if (ind > 0) {
			setState(maniFestVersion.substring(ind+1, maniFestVersion.length()));
			maniFestVersion = maniFestVersion.substring(0, ind);
		}
		
		StringTokenizer tokenizer = new StringTokenizer(maniFestVersion, VERSION_DELIMITER);

		if (tokenizer.countTokens() != 3) {
			throw new Exception( "there must be 3 versions");
		}

		try {
			major = Integer.parseInt(tokenizer.nextToken());
			if (major > VERSION_MAJOR_ABS || major < 0) {
				throw new Exception("Wrong major version");
			}
		} catch (NumberFormatException e) {
			throw new Exception("Wrong major version");
		}

		// check minor version
		try {
			minor =  Integer.parseInt(tokenizer.nextToken());
			if (minor > VERSION_MINOR_ABS || minor < 0) {
				throw new Exception("Wrong minor version");
			}
		} catch (NumberFormatException e) {
			throw new Exception("Wrong minor version");
		}

		// check minor minor version
		try {
			service = Integer.parseInt(tokenizer.nextToken());
			if (service > VERSION_RV_ABS || service < 0) {
				throw new Exception( "Wrong service version");
			}
		} catch (NumberFormatException e) {
			throw new Exception("Wrong service version");
		}
	}
	
	public ProductVersion() {
		// TODO Auto-generated constructor stub
	}

	public int getVersionInt() {
		return (major << (VERSION_BITS*2) ) + (minor << VERSION_BITS) + service;
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public int getMinor() {
		return minor;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getService() {
		return service;
	}

	public void setService(int service) {
		this.service = service;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getVersionStr() {
		StringBuffer sb = new StringBuffer();
		sb.append(major);
		sb.append(VERSION_DELIMITER);
		sb.append(minor);
		sb.append(VERSION_DELIMITER);
		sb.append(service);
		if (state != null && state.isEmpty() == false) {
			sb.append('-');
			sb.append(state);
		}
		return sb.toString();
	}
	
	public String getVersionStrWoState() {
		StringBuffer sb = new StringBuffer();
		sb.append(major);
		sb.append(VERSION_DELIMITER);
		sb.append(minor);
		sb.append(VERSION_DELIMITER);
		sb.append(service);
		return sb.toString();
	}

	
	public String getSvnBuildNr() {
		return svnBuildNr;
	}

	public void setSvnBuildNr(String svnBuildNr) {
		this.svnBuildNr = svnBuildNr;
	}

	@Override
	public String toString() {
		return "[appName=" + appName + ", state=" + state + ", major=" + major + ", minor=" + minor
				+ ", service=" + service + ", svnBuildNr=" + svnBuildNr + "]";
	}
	
	
	
		

}
