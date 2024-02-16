package com.doubleclue.dcem.core.jpa;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.derby.tools.sysinfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;

import com.mchange.v2.c3p0.impl.NewProxyConnection;
import com.microsoft.sqlserver.jdbc.SQLServerConnection;

import net.sf.log4jdbc.sql.jdbcapi.ConnectionSpy;

public class MultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider, ServiceRegistryAwareService {

	private static final long serialVersionUID = 4368575201221677384L;
	private static final Logger logger = LogManager.getLogger(MultiTenantConnectionProviderImpl.class);

	private C3P0ConnectionProvider connectionProvider = null;

	private String databaseSwitchSchemaCommand;

	static DatabaseTypes databaseTypes;
	static ConcurrentHashMap<Integer, String> schemaMap = new ConcurrentHashMap<>();

	@Override
	public void injectServices(ServiceRegistryImplementor serviceRegistry) {
		// Collection lSettings = (Collection) serviceRegistry.getService(ConfigurationService.class).getSettings();
		connectionProvider = new C3P0ConnectionProvider();
		connectionProvider.injectServices(serviceRegistry);
		connectionProvider.configure(serviceRegistry.getService(ConfigurationService.class).getSettings());

		databaseTypes = DbFactoryProducer.getInstance().getDbType();
		if (logger.isDebugEnabled()) {
			logger.debug("database type is: " + databaseTypes.name());
		}
		databaseSwitchSchemaCommand = databaseTypes.getSchemaSwitch();
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		return null;
	}

	@Override
	public Connection getAnyConnection() throws SQLException {
		return connectionProvider.getConnection();
	}

	@Override
	public Connection getConnection(String tenantIdentifier) throws SQLException {
		final Connection connection = getAnyConnection();
//		Object connectionObject;
//		String preSchema = null;
//		Field field;
	//	int hash = 0;
	//	if (databaseTypes == DatabaseTypes.POSTGRE || databaseTypes == DatabaseTypes.MSSQL) {
			connection.createStatement().execute(databaseSwitchSchemaCommand + tenantIdentifier);
//			if (logger.isTraceEnabled()) {
//				logger.trace(hash + " DB-Switch-Schema: '" + databaseSwitchSchemaCommand + tenantIdentifier);
//			}
//		} else {
//			try {
//				field = ((NewProxyConnection) connection).getClass().getDeclaredField("inner");
//				field.setAccessible(true);
//				connectionObject = field.get(((NewProxyConnection) connection));
//
//				if (DatabaseUtils.logForJdbc) {
//					@SuppressWarnings("resource")
//					ConnectionSpy connectionSpy = (ConnectionSpy) connectionObject;
//					connectionObject = connectionSpy.getRealConnection();
//				}
//				hash = connectionObject.hashCode();
//				preSchema = schemaMap.get(hash);
//			} catch (Exception e1) {
//				logger.warn(e1);
//			}
//			if (preSchema == null || preSchema.equals(tenantIdentifier) == false) {
//				connection.createStatement().execute(databaseSwitchSchemaCommand + tenantIdentifier);
//				schemaMap.put(hash, tenantIdentifier);
//				if (logger.isDebugEnabled()) {
//					logger.debug(hash + " DB-Switch-Schema: '" + databaseSwitchSchemaCommand + tenantIdentifier);
//				}
//			}
//			// else {
//			// logger.debug(hash + " No DB-Switch-Schema: " + preSchema);
//			// }
//			// logger.debug("Connection: " + connection);
//		}
		return connection;
	}

	@Override
	public void releaseAnyConnection(Connection connection) throws SQLException {
		connectionProvider.closeConnection(connection);
	}

	@Override
	public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
		releaseAnyConnection(connection);
	}

	@Override
	public boolean isUnwrappableAs(Class unwrapType) {
		return false;
	}

	@Override
	public boolean supportsAggressiveRelease() {
		return true;
	}

}
