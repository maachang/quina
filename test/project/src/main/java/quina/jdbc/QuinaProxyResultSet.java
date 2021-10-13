package quina.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyInjectMethod;
import quina.annotation.proxy.ProxyOverride;

/**
 * QuinaProxyResultSet.
 */
public abstract class QuinaProxyResultSet
	implements ResultSet {
	
	// 元のResultSet.
	@ProxyField
	protected ResultSet resultSet;
	
	// 呼び出し元のステートメント.
	private AbstractQuinaProxyStatement statement;
	
	@ProxyOverride
	public void close() throws SQLException {
		statement.checkClose();
		if(resultSet != null) {
			ResultSet rs = resultSet;
			resultSet = null;
			rs.close();
		}
	}
	
	@ProxyInjectMethod
	protected void checkClose() throws SQLException {
		statement.checkClose();
		if(resultSet == null) {
			throw new SQLException("The resultSet is closed.");
		}
	}
	
	@ProxyOverride
	public Statement getStatement() throws SQLException {
		return statement;
	}
}
