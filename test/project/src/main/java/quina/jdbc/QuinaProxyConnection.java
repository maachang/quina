package quina.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quina.annotation.proxy.ProxyField;
import quina.annotation.proxy.ProxyInitialSetting;
import quina.annotation.proxy.ProxyInjectMethod;
import quina.annotation.proxy.ProxyOverride;
import quina.annotation.proxy.ProxyScoped;
import quina.util.AtomicNumber64;
import quina.util.Flag;

/**
 * ProxyConnection.
 */
@ProxyScoped
public abstract class QuinaProxyConnection
	implements Connection {
	// 接続確認用SQL.
	private static final String ECHO_SQL = "select (1);";
	
	// 元のコネクション.
	@ProxyField
	protected Connection connection;
	
	// 最終プーリング時間.
	protected final AtomicNumber64 lastPoolingTime =
		new AtomicNumber64();
	
	// プーリング対象のデータソース.
	private QuinaDataSource dataSource;
	
	// プーリングしない場合.
	private boolean notPooling;
	
	// 廃棄フラグ.
	private final Flag destroyFlag = new Flag(false);
	
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
		this.dataSource.getDefine()
			.appendConnection(
				this.connection);
		
		// 初期設定.
		this.closeFlag.set(false);
	}
	
	/**
	 * QuinaJDBCDefineを取得.
	 * @return QuinaJDBCDefine defineが返却されます.
	 * @exception SQLException SQL例外.
	 */
	public QuinaJDBCConfig getDefine()
		throws SQLException {
		checkClose();
		return dataSource.getDefine();
	}
	
	/**
	 * データ破棄.
	 */
	public void destroy() {
		closeFlag.set(true);
		destroyFlag.set(true);
		lastPoolingTime.set(
			QuinaJDBCTimeoutThread.DESTROY_TIMEOUT);
		Connection c = connection;
		connection = null;
		dataSource = null;
		try {
			if(c != null) {
				c.close();
			}
		} catch(Exception e) {}
	}
	
	/**
	 * 既に破棄されているかチェック.
	 * @return boolean trueの場合破棄されています.
	 */
	public boolean isDestroy() {
		return destroyFlag.get();
	}
	
	/**
	 * 仮クローズを再オープン.
	 * @return boolean trueの場合、再オープンできました.
	 */
	protected boolean reOpen() {
		if(destroyFlag.get()) {
			return false;
		}
		// タイムアウト監視しない.
		lastPoolingTime.set(
			QuinaJDBCTimeoutThread.NONE_TIMEOUT);
		// 仮オープン.
		closeFlag.set(false);
		// アクセス可能かチェック.
		boolean ret = true;
		Statement stm = null;
		ResultSet rs = null;
		try {
			stm = connection.createStatement();
			rs = stm.executeQuery(
				dataSource.getDefine().getSQL(ECHO_SQL));
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
	 * 最後にプーリングした時間を取得.
	 * @return long 最後にプーリングした時間が返却されます.
	 */
	protected long getLastPoolingTime() {
		return lastPoolingTime.get();
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
	@Override
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
				// 仮クローズ.
				lastPoolingTime.set(
					System.currentTimeMillis());
				// プーリング管理にセット.
				dataSource.pushPooling(this);
			}
		}
	}
	
	@Override
	@ProxyOverride
	public boolean isClosed()
		throws java.sql.SQLException {
		if(closeFlag.get()) {
			return true;
		}
		return connection.isClosed();
	}
	
	/**
	 * SQLを整形.
	 * @param sql 対象のSQLを設定します.
	 * @return String 整形されたSQLが返却されます.
	 */
	protected String getSQL(String sql) {
		return dataSource.getDefine().getSQL(sql);
	}
	
	/**
	 * StatementにKind定義を反映.
	 * @param stmt 対象のStatementを設定します.
	 * @return Statement 反映されたStatementが返却されます.
	 */
	protected final Statement appendStatement(Statement stmt) {
		return dataSource.getDefine().appendStatement(stmt);
	}

	@Override
	@ProxyOverride
	public QuinaProxyStatement createStatement()
		throws SQLException {
		checkClose();
		return (QuinaProxyStatement)appendStatement(
			QuinaProxyUtil.getStatement(
				this, connection.createStatement())
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement(String sql)
		throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(getSQL(sql)))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyCallableStatement prepareCall(String sql)
		throws SQLException {
		checkClose();
		return (QuinaProxyCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(getSQL(sql)))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), resultSetType, resultSetConcurrency))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyCallableStatement prepareCall(
		String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {
		checkClose();
		return (QuinaProxyCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(
					getSQL(sql), resultSetType, resultSetConcurrency))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency,
		int resultSetHoldability) throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), resultSetType, resultSetConcurrency,
					resultSetHoldability))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyCallableStatement prepareCall(
			String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		checkClose();
		return (QuinaProxyCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(
					getSQL(sql), resultSetType, resultSetConcurrency,
					resultSetHoldability))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement(
		String sql, int autoGeneratedKeys)
		throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), autoGeneratedKeys))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement(
		String sql, int[] columnIndexes)
		throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), columnIndexes))
		);
	}

	@Override
	@ProxyOverride
	public QuinaProxyPreparedStatement prepareStatement
		(String sql, String[] columnNames)
		throws SQLException {
		checkClose();
		return (QuinaProxyPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), columnNames))
		);
	}
}

