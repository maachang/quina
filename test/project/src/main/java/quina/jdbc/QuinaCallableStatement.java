package quina.jdbc;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyOverride;
import quina.annotation.proxy.ProxyScoped;

/**
 * QuinaProxyCallableStatement.
 */
@ProxyScoped
public abstract class QuinaCallableStatement
	extends AbstractQuinaProxyStatement
	implements CallableStatement {
	
	// 元のCallableStatement.
	@ProxyField
	protected CallableStatement statement;
	
	@Override
	protected Statement getStatement() {
		return statement;
	}
	
	@Override
	protected void setStatement(Statement s) {
		statement = (CallableStatement)s;
	}

	@Override
	@ProxyOverride
	public QuinaResultSet getGeneratedKeys() throws SQLException {
		return QuinaProxyUtil.getResultSet(
			this, statement.getGeneratedKeys());
	}

	@Override
	@ProxyOverride
	public QuinaResultSet executeQuery() throws SQLException {
		return QuinaProxyUtil.getResultSet(
			this, statement.executeQuery());
	}

}
