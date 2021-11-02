package quinax.proxy;

/**
 * ProxyScoped ProxyClass automatically generated based on the
 * annotation definition class "quina.jdbc.QuinaProxyConnection".
 */
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public final class AutoProxyQuinaProxyConnection
	extends quina.jdbc.QuinaProxyConnection {
	
	/**
	 * Set the required parameters.
	 * @param args Set the parameters.
	 */
	public final void __initialSetting(quina.annotation.proxy.ProxySettingArgs args) {
		try {
			super.setting(
				(java.lang.Boolean)args.getArgs(0)
				,(quina.jdbc.QuinaDataSource)args.getArgs(1)
				,(java.sql.Connection)args.getArgs(2)
			);
		} catch(quina.exception.QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new quina.exception.QuinaException(e);
		}
	}
	
	@Override
	public void setReadOnly(
		boolean arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setReadOnly(arg0);
	}

	@Override
	public boolean isReadOnly()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.isReadOnly();
	}

	@Override
	public void abort(
		java.util.concurrent.Executor arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.abort(arg0);
	}

	@Override
	public void commit()
		throws java.sql.SQLException {
		super.checkClose();
		connection.commit();
	}

	@Override
	public boolean isValid(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.isValid(arg0);
	}

	@Override
	public java.sql.SQLWarning getWarnings()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getWarnings();
	}

	@Override
	public void clearWarnings()
		throws java.sql.SQLException {
		super.checkClose();
		connection.clearWarnings();
	}

	@Override
	public java.lang.String nativeSQL(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.nativeSQL(arg0);
	}

	@Override
	public void setAutoCommit(
		boolean arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setAutoCommit(arg0);
	}

	@Override
	public boolean getAutoCommit()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getAutoCommit();
	}

	@Override
	public void rollback()
		throws java.sql.SQLException {
		super.checkClose();
		connection.rollback();
	}

	@Override
	public void rollback(
		java.sql.Savepoint arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.rollback(arg0);
	}

	@Override
	public java.sql.DatabaseMetaData getMetaData()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getMetaData();
	}

	@Override
	public void setCatalog(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setCatalog(arg0);
	}

	@Override
	public java.lang.String getCatalog()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setTransactionIsolation(arg0);
	}

	@Override
	public int getTransactionIsolation()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getTransactionIsolation();
	}

	@Override
	public java.util.Map getTypeMap()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getTypeMap();
	}

	@Override
	public void setTypeMap(
		java.util.Map arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setTypeMap(arg0);
	}

	@Override
	public void setHoldability(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setHoldability(arg0);
	}

	@Override
	public int getHoldability()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getHoldability();
	}

	@Override
	public java.sql.Savepoint setSavepoint()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.setSavepoint();
	}

	@Override
	public java.sql.Savepoint setSavepoint(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.setSavepoint(arg0);
	}

	@Override
	public void releaseSavepoint(
		java.sql.Savepoint arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.releaseSavepoint(arg0);
	}

	@Override
	public java.sql.Clob createClob()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.createClob();
	}

	@Override
	public java.sql.Blob createBlob()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.createBlob();
	}

	@Override
	public java.sql.NClob createNClob()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.createNClob();
	}

	@Override
	public java.sql.SQLXML createSQLXML()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.createSQLXML();
	}

	@Override
	public void setClientInfo(
		java.util.Properties arg0
	) throws java.sql.SQLClientInfoException {
		connection.setClientInfo(arg0);
	}

	@Override
	public void setClientInfo(
		java.lang.String arg0
		,java.lang.String arg1
	) throws java.sql.SQLClientInfoException {
		connection.setClientInfo(arg0, arg1);
	}

	@Override
	public java.util.Properties getClientInfo()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getClientInfo();
	}

	@Override
	public java.lang.String getClientInfo(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.getClientInfo(arg0);
	}

	@Override
	public java.sql.Array createArrayOf(
		java.lang.String arg0
		,java.lang.Object[] arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.createArrayOf(arg0, arg1);
	}

	@Override
	public java.sql.Struct createStruct(
		java.lang.String arg0
		,java.lang.Object[] arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.createStruct(arg0, arg1);
	}

	@Override
	public void setSchema(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setSchema(arg0);
	}

	@Override
	public java.lang.String getSchema()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getSchema();
	}

	@Override
	public void setNetworkTimeout(
		java.util.concurrent.Executor arg0
		,int arg1
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setNetworkTimeout(arg0, arg1);
	}

	@Override
	public int getNetworkTimeout()
		throws java.sql.SQLException {
		super.checkClose();
		return connection.getNetworkTimeout();
	}

	@Override
	public void beginRequest()
		throws java.sql.SQLException {
		super.checkClose();
		connection.beginRequest();
	}

	@Override
	public void endRequest()
		throws java.sql.SQLException {
		super.checkClose();
		connection.endRequest();
	}

	@Override
	public boolean setShardingKeyIfValid(
		java.sql.ShardingKey arg0
		,java.sql.ShardingKey arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.setShardingKeyIfValid(arg0, arg1, arg2);
	}

	@Override
	public boolean setShardingKeyIfValid(
		java.sql.ShardingKey arg0
		,int arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.setShardingKeyIfValid(arg0, arg1);
	}

	@Override
	public void setShardingKey(
		java.sql.ShardingKey arg0
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setShardingKey(arg0);
	}

	@Override
	public void setShardingKey(
		java.sql.ShardingKey arg0
		,java.sql.ShardingKey arg1
	) throws java.sql.SQLException {
		super.checkClose();
		connection.setShardingKey(arg0, arg1);
	}

	@Override
	public java.lang.Object unwrap(
		java.lang.Class arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.unwrap(arg0);
	}

	@Override
	public boolean isWrapperFor(
		java.lang.Class arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return connection.isWrapperFor(arg0);
	}

}
