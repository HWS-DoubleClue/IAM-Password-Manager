<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>com.doubleclue</groupId>
		<artifactId>DcemParent</artifactId>
		<version>${DcemVersion}</version>
		<relativePath>../DcemParent</relativePath>
	</parent>
	<artifactId>${ModuleFullName}</artifactId>
	<groupId>com.doubleclue.dcem.module</groupId>
	<dependencies>
		<dependency>
			<groupId>com.doubleclue.dcem.module</groupId>
			<artifactId>DcemCore</artifactId>
		</dependency>
		<dependency>
			<groupId>com.doubleclue.dcem.module</groupId>
			<artifactId>AppSecureModule</artifactId>
		</dependency>
	</dependencies>
	<build>
		<plugins>
		</plugins>
	</build>
</project>