package quinax.proxy;

/**
 * ProxyScoped ProxyClass automatically generated based on the
 * annotation definition class "quina.jdbc.QuinaProxyPreparedStatement".
 */
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public final class AutoProxyQuinaProxyPreparedStatement
	extends quina.jdbc.QuinaProxyPreparedStatement {
	
	/**
	 * Set the required parameters.
	 * @param args Set the parameters.
	 */
	public final void __initialSetting(quina.annotation.proxy.ProxySettingArgs args) {
		try {
			super.setting(
				(quina.jdbc.QuinaProxyConnection)args.getArgs(0)
				,(java.sql.Statement)args.getArgs(1)
			);
		} catch(quina.exception.QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new quina.exception.QuinaException(e);
		}
	}
	
	@Override
	public boolean execute()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.execute();
	}

	@Override
	public void setBoolean(
		int arg0
		,boolean arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBoolean(arg0, arg1);
	}

	@Override
	public void setByte(
		int arg0
		,byte arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setByte(arg0, arg1);
	}

	@Override
	public void setShort(
		int arg0
		,short arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setShort(arg0, arg1);
	}

	@Override
	public void setInt(
		int arg0
		,int arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setInt(arg0, arg1);
	}

	@Override
	public void setLong(
		int arg0
		,long arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setLong(arg0, arg1);
	}

	@Override
	public void setFloat(
		int arg0
		,float arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setFloat(arg0, arg1);
	}

	@Override
	public void setDouble(
		int arg0
		,double arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setDouble(arg0, arg1);
	}

	@Override
	public void setTimestamp(
		int arg0
		,java.sql.Timestamp arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setTimestamp(arg0, arg1);
	}

	@Override
	public void setTimestamp(
		int arg0
		,java.sql.Timestamp arg1
		,java.util.Calendar arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setTimestamp(arg0, arg1, arg2);
	}

	@Override
	public void setURL(
		int arg0
		,java.net.URL arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setURL(arg0, arg1);
	}

	@Override
	public void setArray(
		int arg0
		,java.sql.Array arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setArray(arg0, arg1);
	}

	@Override
	public void setTime(
		int arg0
		,java.sql.Time arg1
		,java.util.Calendar arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setTime(arg0, arg1, arg2);
	}

	@Override
	public void setTime(
		int arg0
		,java.sql.Time arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setTime(arg0, arg1);
	}

	@Override
	public void setDate(
		int arg0
		,java.sql.Date arg1
		,java.util.Calendar arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setDate(arg0, arg1, arg2);
	}

	@Override
	public void setDate(
		int arg0
		,java.sql.Date arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setDate(arg0, arg1);
	}

	@Override
	public long executeLargeUpdate()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeUpdate();
	}

	@Override
	public int executeUpdate()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.executeUpdate();
	}

	@Override
	public void addBatch()
		throws java.sql.SQLException {
		super.checkClose();
		statement.addBatch();
	}

	@Override
	public java.sql.ResultSetMetaData getMetaData()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getMetaData();
	}

	@Override
	public void setNull(
		int arg0
		,int arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNull(arg0, arg1);
	}

	@Override
	public void setNull(
		int arg0
		,int arg1
		,java.lang.String arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNull(arg0, arg1, arg2);
	}

	@Override
	public void setBigDecimal(
		int arg0
		,java.math.BigDecimal arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBigDecimal(arg0, arg1);
	}

	@Override
	public void setString(
		int arg0
		,java.lang.String arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setString(arg0, arg1);
	}

	@Override
	public void setBytes(
		int arg0
		,byte[] arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBytes(arg0, arg1);
	}

	@Override
	public void setAsciiStream(
		int arg0
		,java.io.InputStream arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setAsciiStream(arg0, arg1, arg2);
	}

	@Override
	public void setAsciiStream(
		int arg0
		,java.io.InputStream arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setAsciiStream(arg0, arg1);
	}

	@Override
	public void setAsciiStream(
		int arg0
		,java.io.InputStream arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setAsciiStream(arg0, arg1, arg2);
	}

	@Override
	public void setUnicodeStream(
		int arg0
		,java.io.InputStream arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setUnicodeStream(arg0, arg1, arg2);
	}

	@Override
	public void setBinaryStream(
		int arg0
		,java.io.InputStream arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBinaryStream(arg0, arg1, arg2);
	}

	@Override
	public void setBinaryStream(
		int arg0
		,java.io.InputStream arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBinaryStream(arg0, arg1, arg2);
	}

	@Override
	public void setBinaryStream(
		int arg0
		,java.io.InputStream arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBinaryStream(arg0, arg1);
	}

	@Override
	public void clearParameters()
		throws java.sql.SQLException {
		super.checkClose();
		statement.clearParameters();
	}

	@Override
	public void setObject(
		int arg0
		,java.lang.Object arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setObject(arg0, arg1);
	}

	@Override
	public void setObject(
		int arg0
		,java.lang.Object arg1
		,int arg2
		,int arg3
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setObject(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setObject(
		int arg0
		,java.lang.Object arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setObject(arg0, arg1, arg2);
	}

	@Override
	public void setObject(
		int arg0
		,java.lang.Object arg1
		,java.sql.SQLType arg2
		,int arg3
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setObject(arg0, arg1, arg2, arg3);
	}

	@Override
	public void setObject(
		int arg0
		,java.lang.Object arg1
		,java.sql.SQLType arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setObject(arg0, arg1, arg2);
	}

	@Override
	public void setCharacterStream(
		int arg0
		,java.io.Reader arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setCharacterStream(arg0, arg1, arg2);
	}

	@Override
	public void setCharacterStream(
		int arg0
		,java.io.Reader arg1
		,int arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setCharacterStream(arg0, arg1, arg2);
	}

	@Override
	public void setCharacterStream(
		int arg0
		,java.io.Reader arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setCharacterStream(arg0, arg1);
	}

	@Override
	public void setRef(
		int arg0
		,java.sql.Ref arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setRef(arg0, arg1);
	}

	@Override
	public void setBlob(
		int arg0
		,java.io.InputStream arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBlob(arg0, arg1);
	}

	@Override
	public void setBlob(
		int arg0
		,java.sql.Blob arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBlob(arg0, arg1);
	}

	@Override
	public void setBlob(
		int arg0
		,java.io.InputStream arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setBlob(arg0, arg1, arg2);
	}

	@Override
	public void setClob(
		int arg0
		,java.io.Reader arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setClob(arg0, arg1);
	}

	@Override
	public void setClob(
		int arg0
		,java.io.Reader arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setClob(arg0, arg1, arg2);
	}

	@Override
	public void setClob(
		int arg0
		,java.sql.Clob arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setClob(arg0, arg1);
	}

	@Override
	public java.sql.ParameterMetaData getParameterMetaData()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getParameterMetaData();
	}

	@Override
	public void setRowId(
		int arg0
		,java.sql.RowId arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setRowId(arg0, arg1);
	}

	@Override
	public void setNString(
		int arg0
		,java.lang.String arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNString(arg0, arg1);
	}

	@Override
	public void setNCharacterStream(
		int arg0
		,java.io.Reader arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNCharacterStream(arg0, arg1, arg2);
	}

	@Override
	public void setNCharacterStream(
		int arg0
		,java.io.Reader arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNCharacterStream(arg0, arg1);
	}

	@Override
	public void setNClob(
		int arg0
		,java.sql.NClob arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNClob(arg0, arg1);
	}

	@Override
	public void setNClob(
		int arg0
		,java.io.Reader arg1
		,long arg2
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNClob(arg0, arg1, arg2);
	}

	@Override
	public void setNClob(
		int arg0
		,java.io.Reader arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setNClob(arg0, arg1);
	}

	@Override
	public void setSQLXML(
		int arg0
		,java.sql.SQLXML arg1
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setSQLXML(arg0, arg1);
	}

	@Override
	public void cancel()
		throws java.sql.SQLException {
		super.checkClose();
		statement.cancel();
	}

	@Override
	public long getLargeMaxRows()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getLargeMaxRows();
	}

	@Override
	public long[] executeLargeBatch()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeBatch();
	}

	@Override
	public long executeLargeUpdate(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeUpdate(arg0);
	}

	@Override
	public long executeLargeUpdate(
		java.lang.String arg0
		,int arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeUpdate(arg0, arg1);
	}

	@Override
	public long executeLargeUpdate(
		java.lang.String arg0
		,int[] arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeUpdate(arg0, arg1);
	}

	@Override
	public long executeLargeUpdate(
		java.lang.String arg0
		,java.lang.String[] arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.executeLargeUpdate(arg0, arg1);
	}

	@Override
	public java.lang.String enquoteLiteral(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.enquoteLiteral(arg0);
	}

	@Override
	public java.lang.String enquoteIdentifier(
		java.lang.String arg0
		,boolean arg1
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.enquoteIdentifier(arg0, arg1);
	}

	@Override
	public boolean isSimpleIdentifier(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.isSimpleIdentifier(arg0);
	}

	@Override
	public java.lang.String enquoteNCharLiteral(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.enquoteNCharLiteral(arg0);
	}

	@Override
	public int getMaxFieldSize()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getMaxFieldSize();
	}

	@Override
	public void setMaxFieldSize(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setMaxFieldSize(arg0);
	}

	@Override
	public int getMaxRows()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getMaxRows();
	}

	@Override
	public void setMaxRows(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setMaxRows(arg0);
	}

	@Override
	public void setEscapeProcessing(
		boolean arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setEscapeProcessing(arg0);
	}

	@Override
	public int getQueryTimeout()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getQueryTimeout();
	}

	@Override
	public void setQueryTimeout(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setQueryTimeout(arg0);
	}

	@Override
	public java.sql.SQLWarning getWarnings()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getWarnings();
	}

	@Override
	public void clearWarnings()
		throws java.sql.SQLException {
		super.checkClose();
		statement.clearWarnings();
	}

	@Override
	public void setCursorName(
		java.lang.String arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setCursorName(arg0);
	}

	@Override
	public int getUpdateCount()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getUpdateCount();
	}

	@Override
	public boolean getMoreResults(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.getMoreResults(arg0);
	}

	@Override
	public boolean getMoreResults()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getMoreResults();
	}

	@Override
	public void setFetchDirection(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setFetchDirection(arg0);
	}

	@Override
	public int getFetchDirection()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getFetchDirection();
	}

	@Override
	public void setFetchSize(
		int arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setFetchSize(arg0);
	}

	@Override
	public int getFetchSize()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getFetchSize();
	}

	@Override
	public int getResultSetConcurrency()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getResultSetConcurrency();
	}

	@Override
	public int getResultSetType()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getResultSetType();
	}

	@Override
	public void clearBatch()
		throws java.sql.SQLException {
		super.checkClose();
		statement.clearBatch();
	}

	@Override
	public int[] executeBatch()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.executeBatch();
	}

	@Override
	public int getResultSetHoldability()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getResultSetHoldability();
	}

	@Override
	public void setPoolable(
		boolean arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setPoolable(arg0);
	}

	@Override
	public boolean isPoolable()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.isPoolable();
	}

	@Override
	public void closeOnCompletion()
		throws java.sql.SQLException {
		super.checkClose();
		statement.closeOnCompletion();
	}

	@Override
	public boolean isCloseOnCompletion()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.isCloseOnCompletion();
	}

	@Override
	public long getLargeUpdateCount()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getLargeUpdateCount();
	}

	@Override
	public void setLargeMaxRows(
		long arg0
	) throws java.sql.SQLException {
		super.checkClose();
		statement.setLargeMaxRows(arg0);
	}

	@Override
	public java.lang.Object unwrap(
		java.lang.Class arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.unwrap(arg0);
	}

	@Override
	public boolean isWrapperFor(
		java.lang.Class arg0
	) throws java.sql.SQLException {
		super.checkClose();
		return statement.isWrapperFor(arg0);
	}

}
