package org.dayatang.mysql.jdbc;

import com.mysql.jdbc.LoadBalancedMySQLConnection;
import com.mysql.jdbc.NonRegisteringDriver;
import com.mysql.jdbc.ReplicationConnection;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;
import java.util.concurrent.Executor;

public class GeminiReplicationConnection extends ReplicationConnection {

	private static final Logger LOGGER = LoggerFactory.getLogger(GeminiReplicationConnection.class);

	public GeminiReplicationConnection(Properties masterProperties, Properties slaveProperties) throws SQLException {
		NonRegisteringDriver driver = new NonRegisteringDriver();

		StringBuilder masterUrl = new StringBuilder("jdbc:mysql://");
		StringBuilder slaveUrl = new StringBuilder("jdbc:mysql://");

		String masterHost = masterProperties.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY);
		if (masterHost != null) {
			masterUrl.append(masterHost);
		}

		String slaveHost = slaveProperties.getProperty(NonRegisteringDriver.HOST_PROPERTY_KEY);
		if (slaveHost != null) {
			slaveUrl.append(slaveHost);
		}

		String masterDb = masterProperties.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
		masterUrl.append("/");
		if (masterDb != null) {
			masterUrl.append(masterDb);
		}

		String slaveDb = slaveProperties.getProperty(NonRegisteringDriver.DBNAME_PROPERTY_KEY);
		slaveUrl.append("/");
		if (slaveDb != null) {
			slaveUrl.append(slaveDb);
		}

		slaveProperties.setProperty("roundRobinLoadBalance", "true");

		this.masterConnection = (LoadBalancedMySQLConnection) driver.connect(masterUrl.toString(), masterProperties);

		if (StringUtils.isBlank(slaveHost) && slaveUrl.toString().contains("///")) {
			info(" ----- the salveUrl contains the '///', that means there is no slaver, make slavesConnection = masterConnection --");
			slavesConnection = masterConnection;
		} else {
			this.slavesConnection = (LoadBalancedMySQLConnection) driver.connect(slaveUrl.toString(), slaveProperties);
			this.slavesConnection.setReadOnly(true);
		}

		this.currentConnection = this.masterConnection;
	}

	@Override
	public Clob createClob() throws SQLException {
		return null;
	}

	@Override
	public Blob createBlob() throws SQLException {
		return null;
	}

	@Override
	public NClob createNClob() throws SQLException {
		return null;
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		return null;
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		return false;
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {

	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {

	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		return null;
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		return null;
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		return null;
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		return null;
	}

	//For JDK 7 compatability
	public void setSchema(String schema) throws SQLException {
	}

	//For JDK 7 compatability
	public String getSchema() throws SQLException {
		return null;
	}

	//For JDK 7 compatability
	public void abort(Executor executor) throws SQLException {
	}

	//For JDK 7 compatability
	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
	}

	//For JDK 7 compatability
	public int getNetworkTimeout() throws SQLException {
		return 0;
	}

	private void info(String message, Object... params) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info(message, params);
		}
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}
}
