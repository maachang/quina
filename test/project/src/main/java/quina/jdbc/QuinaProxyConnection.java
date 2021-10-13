package quina.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyInitialSetting;
import quina.annotation.proxy.ProxyInjectMethod;
import quina.annotation.proxy.ProxyOverride;
import quina.annotation.proxy.ProxyScoped;
import quina.util.Flag;

/**
 * ProxyConnection.
 */
@ProxyScoped
public abstract class QuinaProxyConnection
	implements Connection {
	
	// 元のコネクション.
	@ProxyField
	protected Connection connection;
	
	// プーリング対象のデータソース.
	private QuinaDataSource dataSource;
	
	// プーリングしない場合.
	private boolean notPooling;
	
	// クローズフラグ.
	private final Flag closeFlag = new Flag(true);
	
	/**
	 * 初期設定.
	 * @param notPooling trueの場合、プーリングしません.
	 * @param dataSource QuinaDataSourceを設定します.
	 * @param connection 元のコネクションを設定します.
	 */
	@ProxyInitialSetting
	protected void setting(Boolean notPooling,
		QuinaDataSource dataSource,
		Connection connection) {
		if(notPooling == null || dataSource == null ||
			connection == null) {
			throw new NullPointerException();
		}
		this.notPooling = notPooling;
		this.dataSource = dataSource;
		this.connection = connection;
		// コネクションの初期設定.
		this.dataSource.getKind().setConnection(
			this.connection);
		
		// 初期設定.
		this.closeFlag.set(false);
	}
	
	/**
	 * QuinaJDBCKindを取得.
	 * @return QuinaJDBCKind kindが返却されます.
	 * @exception SQLException SQL例外.
	 */
	public QuinaJDBCKind getKind()
		throws SQLException {
		checkClose();
		return dataSource.getKind();
	}
	
	/**
	 * データ破棄.
	 * @exception SQLException SQL例外.
	 */
	public void destroy() throws SQLException {
		closeFlag.set(true);
		Connection c = connection;
		connection = null;
		dataSource = null;
		if(c != null) {
			c.close();
		}
	}
	
	/**
	 * 仮クローズを再オープン.
	 * @return boolean trueの場合、再オープンできました.
	 */
	protected boolean reOpen() {
		if(connection == null) {
			return false;
		}
		closeFlag.set(false);
		// アクセス可能かチェック.
		boolean ret = true;
		Statement stm = null;
		ResultSet rs = null;
		try {
			stm = connection.createStatement();
			rs = stm.executeQuery(
				dataSource.getKind().getSQL("select 1;"));
			rs.next();
			rs.close(); rs = null;
			stm.close(); stm = null;
		} catch(Exception e) {
			ret = false;
		} finally {
			if(rs != null) {
				try {
					rs.close();
				} catch(Exception e) {}
			}
			if(stm != null) {
				try {
					stm.close();
				} catch(Exception e) {}
			}
			if(!ret) {
				try {
					destroy();
				} catch(Exception e) {}
			}
		}
		return ret;
	}
	
	/**
	 * プーリングしないコネクション.
	 * @return boolean trueの場合プーリングしません.
	 */
	protected boolean isNotPooling() {
		return notPooling;
	}
	
	/**
	 * ProxyConnectionのメソッドに対して
	 * SQLException例外を出力するメソッドのチェック処理.
	 * @exception SQLException SQL例外.
	 */
	@ProxyInjectMethod
	protected void checkClose() throws SQLException {
		if(closeFlag.get()) {
			throw new SQLException("The connection is closed.");
		}
	}
	
	/**
	 * コネクションクローズ処理.
	 * @exception SQLException SQL例外.
	 */
	@ProxyOverride
	public void close() throws SQLException {
		// クローズ済みでない場合.
		if(!closeFlag.setToGetBefore(true)) {
			// プーリングしない場合.
			if(notPooling) {
				// コネクション破棄.
				destroy();
			// プーリングする場合.
			} else {
				// 物理的にクローズせずに
				// 仮にクローズする.
				dataSource.pushPooling(this);
			}
		}
	}
	
	/**
	 * SQLを整形.
	 * @param sql 対象のSQLを設定します.
	 * @return String 整形されたSQLが返却されます.
	 * @exception SQLException SQL例外.
	 */
	protected String getSQL(String sql)
		throws SQLException {
		checkClose();
		return dataSource.getKind().getSQL(sql);
	}

	@ProxyOverride
	public Statement createStatement() throws SQLException {
		checkClose();
		return QuinaProxyUtil.getStatement(
			this, connection.createStatement());
	}

	@ProxyOverride
	public PreparedStatement prepareStatement(String sql)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(getSQL(sql)));
	}

	@ProxyOverride
	public CallableStatement prepareCall(String sql)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(getSQL(sql)));
	}

	@ProxyOverride
	public PreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(
				getSQL(sql), resultSetType, resultSetConcurrency));
	}

	@ProxyOverride
	public CallableStatement prepareCall(
		String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getCallableStatement(
			this, connection.prepareCall(
				getSQL(sql), resultSetType, resultSetConcurrency));
	}

	@ProxyOverride
	public PreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency,
		int resultSetHoldability) throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(
				getSQL(sql), resultSetType, resultSetConcurrency,
				resultSetHoldability));
	}

	@ProxyOverride
	public CallableStatement prepareCall(
			String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		checkClose();
		return QuinaProxyUtil.getCallableStatement(
			this, connection.prepareCall(
				getSQL(sql), resultSetType, resultSetConcurrency,
				resultSetHoldability));
	}

	@ProxyOverride
	public PreparedStatement prepareStatement(
		String sql, int autoGeneratedKeys)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(
				getSQL(sql), autoGeneratedKeys));
	}

	@ProxyOverride
	public PreparedStatement prepareStatement(
		String sql, int[] columnIndexes)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(
				getSQL(sql), columnIndexes));
	}

	@ProxyOverride
	public PreparedStatement prepareStatement
		(String sql, String[] columnNames)
		throws SQLException {
		checkClose();
		return QuinaProxyUtil.getPreparedStatement(
			this, connection.prepareStatement(
				getSQL(sql), columnNames));
	}
}