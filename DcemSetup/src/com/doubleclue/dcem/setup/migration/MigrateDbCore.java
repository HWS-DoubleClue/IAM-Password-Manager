package com.doubleclue.dcem.setup.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.MigrateDbI;

@ApplicationScoped
@Named("MigrateDbCore")
public class MigrateDbCore implements MigrateDbI {

	private static final Logger logger = LogManager.getLogger(MigrateDbCore.class);

	public void migrate(Connection connection, DatabaseTypes dbType, int from, int to, String parameter) throws SQLException {
		System.out.println("MigrateDbCore.migrate() " + parameter);
		switch (to) {
		case 2:
			migrateTo_2(connection);
			break;
		case 3:
			break;
		}
	}

	private void migrateTo_2(Connection connection) throws SQLException {
		logger.info("migrating core to DB-Version 2");
		PreparedStatement statement = connection.prepareStatement("SELECT ld.dc_id, ld.name FROM core_ldap ld ORDER BY dc_rank asc");
		ResultSet rs = statement.executeQuery();
		if (rs.next() == false) {
			logger.info("migrating core ready. No ldap found.");
			return;
		}
		int ldapId = rs.getInt(1);
		String ldapName = rs.getString(2);
		statement.close();

		statement = connection.prepareStatement("SELECT us.dc_id, us.loginId FROM core_user us WHERE us.ldapUser=true");
		rs = statement.executeQuery();
		PreparedStatement statement2 = connection.prepareStatement("UPDATE core_user SET dc_ldap = ?, loginId=? where dc_id=?");
		int userId;
		String loginId;
		while (rs.next()) {
			userId = rs.getInt(1);
			loginId = rs.getString(2);
			statement2.setInt(1, ldapId);
			statement2.setString(2, ldapName.toUpperCase() + DcemConstants.DOMAIN_SEPERATOR + loginId);
			statement2.setInt(3, userId);
			statement2.execute();
		}
		statement.close();
		/*
		 *  for the operators
		 */
		statement = connection.prepareStatement("SELECT op.dc_id, op.loginId FROM core_operator op WHERE op.ldapUser=true");
		rs = statement.executeQuery();
		statement2 = connection.prepareStatement("UPDATE core_operator SET dc_ldap = ?, loginId=? where dc_id=?");
		int opId;
		while (rs.next()) {
			opId = rs.getInt(1);
			loginId = rs.getString(2);
			statement2.setInt(1, ldapId);
			statement2.setString(2, ldapName.toUpperCase() + DcemConstants.DOMAIN_SEPERATOR + loginId);
			statement2.setInt(3, opId);
			statement2.execute();
		}
		statement.close();
	}

	@Override
	public void dbMethod(Connection connection, DatabaseTypes dbType, String parameter) {
	}
}
