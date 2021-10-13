package quina.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyOverride;
import quina.annotation.proxy.ProxyScoped;

/**
 * QuinaProxyPreparedStatement.
 */
@ProxyScoped
public abstract class QuinaProxyPreparedStatement
	extends AbstractQuinaProxyStatement
	implements PreparedStatement {
	
	// 元のPreparedStatement.
	@ProxyField
	protected PreparedStatement statement;
	
	@Override
	protected Statement getStatement() {
		return statement;
	}
	
	@Override
	protected void setStatement(Statement s) {
		statement = (PreparedStatement)s;
	}

	@ProxyOverride
	public ResultSet getGeneratedKeys() throws SQLException {
		return QuinaProxyUtil.getResultSet(
			this, statement.getGeneratedKeys());
	}

	@ProxyOverride
	public ResultSet executeQuery() throws SQLException {
		return QuinaProxyUtil.getResultSet(
			this, statement.executeQuery());
	}
}
