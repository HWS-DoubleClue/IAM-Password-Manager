package com.doubleclue.dcem.core.jpa;



public enum DatabaseTypes {
	
	MYSQL(
			3306,
			"MySQL Server",
			"jdbc:mysql",
			"org.mariadb.jdbc.Driver",
			"org.hibernate.dialect.MySQL5InnoDBDialect",
			false,
			"use ",
			"select 1",
			"CREATE DATABASE {{db.name}} DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;",
			"\\\\"),
	MARIADB(
			3306,
			"MariaDB",
			"jdbc:mysql",
			"org.mariadb.jdbc.Driver",
			"org.hibernate.dialect.MySQL5InnoDBDialect",
			false,
			"use ",
			"select 1",   
			"CREATE DATABASE {{db.name}} DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;",
			"\\\\"),
	MSSQL(
			1433,
			"MS SQL Server",
			"jdbc:sqlserver",
			"com.microsoft.sqlserver.jdbc.SQLServerDriver",
			"org.hibernate.dialect.SQLServer2008Dialect",
			false,
			"use ",
			"select 1",
			"CREATE DATABASE {{db.name}} collate Latin1_General_CI_AS;\nALTER DATABASE {{db.name}} SET READ_COMMITTED_SNAPSHOT ON;",
			"\\"),
//	DERBY(
//			1527,
//			"Embedded-Database",
//			"jdbc:derby",
//			"org.apache.derby.jdbc.EmbeddedDriver",
//			"org.hibernate.dialect.DerbyTenSevenDialect",
//			true,
//			null,
//			"select 1",
//			"CREATE DATABASE {{db.name}};",
//			"\\\\"),
	POSTGRE(
			5432,
			"PostgreSQL",
			"jdbc:postgresql",
			"org.postgresql.Driver",
			"org.hibernate.dialect.PostgreSQL95Dialect",
			false,
			"SET search_path TO ",
			"select 1",
			"CREATE SCHEMA {{db.name}}",
			"\\\\"
//			), 
//
//	ORACLE(
//			1521,
//			"Oracle DB Server",
//			"jdbc:oracle:thin",
//			"oracle.jdbc.driver.OracleDriver",
//			"org.hibernate.dialect.Oracle12cDialect",
//			false,
//			"ALTER SESSION SET CURRENT_SCHEMA = ",
//			"select 1 from sys.dual",
//			"CREATE USER {{db.name}} IDENTIFIED BY {{db.namepwd}};",
//			"\\\\"
			);
	
	private int defaultPort;
	private String displayName;
	private String protocol;
	private String driver;
	private String hibernateDialect;
	private boolean forTestOnly;
	private String schemaSwitch;
	private String testQuery;
	private String createSchema;
	private String dbBackslash;

	private DatabaseTypes(int defaultPort, String displayName, String protocol, String driver,
			String hibernateDialect, boolean forTestOnly, String schemaSwitch, String testQuery, String createSchema, String dbBackslash) {
		this.defaultPort = defaultPort;
		this.displayName = displayName;
		this.protocol = protocol;
		this.driver = driver;
		this.hibernateDialect = hibernateDialect;
		this.forTestOnly = forTestOnly;
		this.schemaSwitch = schemaSwitch;
		this.testQuery = testQuery;
		this.createSchema = createSchema;
		this.dbBackslash = dbBackslash;
	}

	
	public String getDbBackslash() {
		return dbBackslash;
	}


	public int getDefaultPort() {
		return defaultPort;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getDriver() {
		return driver;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isForTestOnly() {
		return forTestOnly;
	}

	public String getHibernateDialect() {
		return hibernateDialect;
	}

	
	public String getSchemaSwitch() {
		return schemaSwitch;
	}

	public String getTestQuery() {
		return testQuery;
	}

	public void setTestQuery(String testQuery) {
		this.testQuery = testQuery;
	}


	public String getCreateSchema() {
		return createSchema;
	}


	public void setCreateSchema(String createSchema) {
		this.createSchema = createSchema;
	}
}
