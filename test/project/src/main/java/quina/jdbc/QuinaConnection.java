package quina.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import quina.compile.cdi.annotation.proxy.ProxyField;
import quina.compile.cdi.annotation.proxy.ProxyInitialSetting;
import quina.compile.cdi.annotation.proxy.ProxyInjectMethod;
import quina.compile.cdi.annotation.proxy.ProxyOverride;
import quina.compile.cdi.annotation.proxy.ProxyScoped;
import quina.jdbc.io.AbstractStatement;
import quina.jdbc.io.IoStatement;
import quina.jdbc.io.QueryStatement;
import quina.jdbc.io.WriteBatchStatement;
import quina.util.AtomicNumber64;
import quina.util.Flag;
import quina.util.collection.ObjectList;
import quina.worker.timeout.TimeoutElement;

/**
 * ProxyConnection.
 */
@ProxyScoped
public abstract class QuinaConnection
	implements Connection, TimeoutElement {
	// 接続確認用SQL.
	private static final String ECHO_SQL = "select (1);";
	
	// 利用中を示すタイムアウト値.
	protected static final long NONE_TIMEOUT = -1L;
	
	// 破棄を示すタイムアウト値.
	protected static final long DESTROY_TIMEOUT = 0L;
	
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
	
	// タイマー監視登録フラグ.
	private final Flag regTimeFlag = new Flag(false);
	
	// I/Oステートメント管理.
	private ObjectList<AbstractStatement<?>> ioStatementList = null;
	
	/**
	 * 初期設定.
	 * @param notPooling trueの場合、プーリングしません.
	 * @param dataSource QuinaDataSourceを設定します.
	 * @param connection 元のコネクションを設定します.
	 */
	@ProxyInitialSetting
	protected void setting(Boolean notPooling,
		QuinaDataSource dataSource, Connection connection) {
		if(notPooling == null || dataSource == null ||
			connection == null) {
			throw new NullPointerException();
		}
		// closeがtrueの場合.
		if(closeFlag.setToGetBefore(false)) {
			// 初期データセット.
			this.notPooling = notPooling;
			this.dataSource = dataSource;
			this.connection = connection;
			// コネクションの初期設定.
			this.dataSource.getConfig()
				.appendConnection(
					this.connection);
			// コネクションカウントを１UP.
			this.dataSource.incConnectionCount();
		}
	}
	
	/**
	 * QuinaJDBCDefineを取得.
	 * @return QuinaJDBCConfig defineが返却されます.
	 * @exception SQLException SQL例外.
	 */
	public QuinaJDBCConfig getConfig()
		throws SQLException {
		checkClose();
		return dataSource.getConfig();
	}
	
	// I/Oステートメントリストをクリア.
	protected void closeIoStatement() {
		if(ioStatementList != null) {
			final int len = ioStatementList.size();
			for(int i = 0; i < len; i ++) {
				try {
					ioStatementList.get(i).close();
				} catch(Exception e) {}
			}
			ioStatementList = null;
		}
	}
	
	/**
	 * データ破棄.
	 */
	public void destroy() {
		// 今回破棄される場合.
		if(!destroyFlag.setToGetBefore(true)) {
			// コネクションカウントを１デクリメント.
			dataSource.decConnectionCount();
		}
		closeFlag.set(true);
		lastPoolingTime.set(DESTROY_TIMEOUT);
		closeIoStatement();
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
		lastPoolingTime.set(NONE_TIMEOUT);
		// アクセス可能かチェック.
		boolean ret = true;
		Statement stm = null;
		ResultSet rs = null;
		try {
			stm = connection.createStatement();
			rs = stm.executeQuery(
				dataSource.getConfig().getSQL(ECHO_SQL));
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
		// 再オープン成功の場合.
		if(ret) {
			// この時点でタイムアウト監視などで
			// 廃棄された場合.
			if(destroyFlag.get()) {
				return false;
			}
			// オープン処理.
			closeFlag.set(false);
			return true;
		}
		return false;
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
	@Override
	public long getTime() {
		return lastPoolingTime.get();
	}
	
	// タイムアウト監視が登録されているか取得.
	protected boolean isRegTimeout() {
		return regTimeFlag.get();
	}
	
	/**
	 * タイムアウト監視登録.
	 * @return boolean trueの場合既に登録してます.
	 */
	@Override
	public boolean regTimeout() {
		return regTimeFlag.setToGetBefore(true);
	}
	
	/**
	 * タイムアウト監視登録解除.
	 * @return boolean trueの場合、登録状態を解除しました.
	 */
	protected boolean releaseTimeout() {
		return regTimeFlag.setToGetBefore(false);
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
			// 読み込み専用でない場合はコミットする.
			if(!connection.isReadOnly()) {
				try {
					connection.commit();
				} catch(SQLException se) {
					try {
						destroy();
					} catch(Exception e) {}
					throw se;
				}
			}
			// プーリングしない場合.
			if(notPooling) {
				// コネクション破棄.
				destroy();
			// プーリングする場合.
			} else {
				// IoStatementを破棄.
				closeIoStatement();
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
		return dataSource.getConfig().getSQL(sql);
	}
	
	/**
	 * StatementにKind定義を反映.
	 * @param stmt 対象のStatementを設定します.
	 * @return Statement 反映されたStatementが返却されます.
	 */
	protected final Statement appendStatement(Statement stmt) {
		return dataSource.getConfig().appendStatement(stmt);
	}

	@Override
	@ProxyOverride
	public QuinaStatement createStatement()
		throws SQLException {
		checkClose();
		return (QuinaStatement)appendStatement(
			QuinaProxyUtil.getStatement(
				this, connection.createStatement())
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaStatement createStatement(
		int resultSetType, int resultSetConcurrency)
		throws SQLException {
		checkClose();
		return (QuinaStatement)appendStatement(
			QuinaProxyUtil.getStatement(
				this, connection.createStatement(
				resultSetType, resultSetConcurrency))
		);
	}
	
	
	@Override
	@ProxyOverride
	public QuinaCallableStatement prepareCall(String sql)
		throws SQLException {
		checkClose();
		return (QuinaCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(getSQL(sql)))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaCallableStatement prepareCall(
		String sql, int resultSetType, int resultSetConcurrency)
		throws SQLException {
		checkClose();
		return (QuinaCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(
					getSQL(sql), resultSetType, resultSetConcurrency))
		);
	}

	@Override
	@ProxyOverride
	public QuinaCallableStatement prepareCall(
			String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		checkClose();
		return (QuinaCallableStatement)appendStatement(
			QuinaProxyUtil.getCallableStatement(
				this, connection.prepareCall(
					getSQL(sql), resultSetType, resultSetConcurrency,
					resultSetHoldability))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaStatement createStatement(
		int resultSetType, int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		checkClose();
		return (QuinaStatement)appendStatement(
			QuinaProxyUtil.getStatement(
				this, connection.createStatement(
				resultSetType, resultSetConcurrency,
				resultSetHoldability))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement(String sql)
		throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(getSQL(sql)))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), resultSetType, resultSetConcurrency))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement(
		String sql, int resultSetType, int resultSetConcurrency,
		int resultSetHoldability) throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), resultSetType, resultSetConcurrency,
					resultSetHoldability))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement(
		String sql, int autoGeneratedKeys)
		throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), autoGeneratedKeys))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement(
		String sql, int[] columnIndexes)
		throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), columnIndexes))
		);
	}
	
	@Override
	@ProxyOverride
	public QuinaPreparedStatement prepareStatement
		(String sql, String[] columnNames)
		throws SQLException {
		checkClose();
		return (QuinaPreparedStatement)appendStatement(
			QuinaProxyUtil.getPreparedStatement(
				this, connection.prepareStatement(
					getSQL(sql), columnNames))
		);
	}
	
	// I/Oステートメントを追加.
	private final void addIoStatement(AbstractStatement<?> ios) {
		if(ioStatementList == null) {
			ioStatementList =
				new ObjectList<AbstractStatement<?>>();
		}
		ioStatementList.add(ios);
	}
	
	/**
	 * 読み込み専用ステートメントを取得.
	 * @return QueryStatement 読み込み用ステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public QueryStatement queryStatement()
		throws SQLException {
		checkClose();
		QueryStatement ret = new QueryStatement(this);
		addIoStatement(ret);
		return ret;
	}

	
	/**
	 * I/Oステートメントを取得.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement()
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(this);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * I/Oステートメントを取得.
	 * @param resultSetType ResultSet定数.
	 * @param resultSetConcurrency ResultSet定数.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement(
		int resultSetType, int resultSetConcurrency)
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(
			this, resultSetType, resultSetConcurrency);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * I/Oステートメントを取得.
	 * @param resultSetType ResultSet定数.
	 * @param resultSetConcurrency ResultSet定数.
	 * @param resultSetHoldability ResultSet定数.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement(
		int resultSetType, int resultSetConcurrency,
		int resultSetHoldability)
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(
			this, resultSetType, resultSetConcurrency,
			resultSetHoldability);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * I/Oステートメントを取得.
	 * @param autoGeneratedKeys 自動生成キーを返すかどうかを示すフラグ.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement(int autoGeneratedKeys)
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(
			this, autoGeneratedKeys);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * I/Oステートメントを取得.
	 * @param columnIndexes 挿入された行から返される列を示す列インデックスの配列.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement(int[] columnIndexes)
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(
			this, (Object)columnIndexes);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * I/Oステートメントを取得.
	 * @param columnNames 挿入された行から返される列を示す列名の配列.
	 * @return IoStatement I/Oステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public IoStatement ioStatement(String[] columnNames)
		throws SQLException {
		checkClose();
		IoStatement ret = new IoStatement(
			this,(Object)columnNames);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * WriteBatchステートメントを取得.
	 * @param sql WriteBatch実行用のSQL文を設定します.
	 * @return WriteBatchStatement WriteBatchステートメントが返却されます.
	 * @throws SQLException SQL例外.
	 */
	public WriteBatchStatement writeBatchStatement(String sql)
		throws SQLException {
		checkClose();
		WriteBatchStatement ret =
			new WriteBatchStatement(this, sql);
		addIoStatement(ret);
		return ret;
	}
	
	/**
	 * WriteBatchステートメントを取得.
	 * Insert用のSQLを自動生成します.
	 * @param tableName テーブル名を設定します.
	 * @param columns カラム名群を設定します.
	 * @throws SQLException SQL例外.
	 */
	public WriteBatchStatement writeBatchStatement(
		String tableName, String... columns)
		throws SQLException {
		checkClose();
		WriteBatchStatement ret =
			new WriteBatchStatement(this, tableName, columns);
		addIoStatement(ret);
		return ret;
	}

}
