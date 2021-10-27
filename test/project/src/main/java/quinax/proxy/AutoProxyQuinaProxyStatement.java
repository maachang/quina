package quinax.proxy;

/**
 * ProxyScoped ProxyClass automatically generated based on the
 * annotation definition class "quina.jdbc.QuinaProxyStatement".
 */
@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
public final class AutoProxyQuinaProxyStatement
	extends quina.jdbc.QuinaProxyStatement {
	
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
	public java.sql.ResultSet getGeneratedKeys()
		throws java.sql.SQLException {
		super.checkClose();
		return statement.getGeneratedKeys();
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
