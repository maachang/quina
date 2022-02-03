package quina.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

import quina.compile.cdi.annotation.proxy.ProxyField;
import quina.compile.cdi.annotation.proxy.ProxyInitialSetting;
import quina.compile.cdi.annotation.proxy.ProxyInjectMethod;
import quina.compile.cdi.annotation.proxy.ProxyOverride;
import quina.compile.cdi.annotation.proxy.ProxyScoped;
import quina.util.Flag;

/**
 * QuinaProxyResultSet.
 */
@ProxyScoped
public abstract class QuinaResultSet
	implements ResultSet {
	
	/**
	 * 元のResultSet.
	 */
	@ProxyField
	protected ResultSet resultSet;
	
	/**
	 * クローズフラグ.
	 */
	protected final Flag closeFlag = new Flag(true);
	
	/**
	 * 呼び出し元のステートメント.
	 */
	private AbstractQuinaProxyStatement statement;
	
	/**
	 * 初期設定.
	 * @param statement ProxyStateentを設定します.
	 * @param resultSet 元のResultSetを設定します.
	 */
	@ProxyInitialSetting
	protected void setting(
		AbstractQuinaProxyStatement statement,
		ResultSet resultSet) {
		if(statement == null || resultSet == null) {
			throw new NullPointerException();
		}
		this.statement = statement;
		this.resultSet = resultSet;
		this.closeFlag.set(false);
	}
	
	@ProxyInjectMethod
	protected void checkClose() throws SQLException {
		statement.checkClose();
		if(resultSet == null) {
			throw new SQLException("The resultSet is closed.");
		}
	}
	
	@Override
	@ProxyOverride
	public void close() throws SQLException {
		if(closeFlag.setToGetBefore(true)) {
			return;
		}
		ResultSet rs = resultSet;
		resultSet = null;
		if(rs != null) {
			rs.close();
		}
	}
	
	@Override
	@ProxyOverride
	public boolean isClosed()
		throws java.sql.SQLException {
		if(closeFlag.get()) {
			return true;
		}
		return resultSet.isClosed();
	}
	
	@Override
	@ProxyOverride
	public AbstractQuinaProxyStatement getStatement()
		throws SQLException {
		return statement;
	}
}
