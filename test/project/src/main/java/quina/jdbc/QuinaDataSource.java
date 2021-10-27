package quina.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.sql.DataSource;

import quina.util.Flag;

/**
 * QuinaJDBCDdataSource.
 */
public class QuinaDataSource implements DataSource {
	
	/** プーリングデータ格納用. **/
	protected final Queue<QuinaProxyConnection> pooling =
		new ConcurrentLinkedQueue<QuinaProxyConnection>();

	/** JDBCDefine. **/
	private QuinaJDBCConfig define;
	
	/** タイムアウト監視スレッド. **/
	private QuinaJDBCTimeoutThread timeoutThread;
	
	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();

	/**
	 * コンストラクタ.
	 * @param define QuinaJDBCDefineを設定します.
	 */
	protected QuinaDataSource(QuinaJDBCConfig define) {
		this.define = define;
	}
	
	/**
	 * DataSourceを破棄.
	 */
	public void destroy() {
		if (!destroyFlag.setToGetBefore(true)) {
			// 保持しているコネクションを全て破棄.
			QuinaProxyConnection conn;
			if (pooling.size() > 0) {
				while ((conn = pooling.poll()) != null) {
					try {
						conn.destroy();
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	/**
	 * DateSourceが破棄されてるかチェック.
	 * @return boolean trueの場合は破棄されています.
	 */
	public boolean isDestroy() {
		return destroyFlag.get();
	}
	
	// 破棄チェック.
	protected void checkDestroy() throws SQLException {
		if(destroyFlag.get()) {
			throw new SQLException(
				"The target data source has already been destroyed.");
		}
	}
	
	/**
	 * タイムアウト監視スレッドを設定.
	 * @param timeoutThread タイムアウトスレッドを設定します.
	 */
	protected void setTimeoutThread(QuinaJDBCTimeoutThread timeoutThread) {
		this.timeoutThread = timeoutThread;
	}
	
	// Poolingにセット.
	protected boolean pushPooling(
		QuinaProxyConnection conn) {
		if(destroyFlag.get() ||
			define.getPoolingSize() < pooling.size()) {
			try {
				conn.destroy();
			} catch(Exception e) {}
			return false;
		}
		// プーリングにセット.
		pooling.offer(conn);
		// タイムアウト監視セット.
		timeoutThread.offer(conn);
		return true;
	}

	/**
	 * QuinaJDBCDefineを取得.
	 * @return QuinaJDBCDefineが返却されます.
	 */
	public QuinaJDBCConfig getDefine() {
		return define;
	}
	
	/**
	 * JDBCコネクションの取得.
	 * 
	 * @param define DbDefineを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 * @exception SQLExceptino SQL例外.
	 */
	protected static final Connection _getSrcConnection(
		QuinaJDBCConfig define, String url, String user, String passwd)
		throws SQLException {
		Connection ret;
		Properties p = new java.util.Properties();
		define.appendProperty(p);
		if (user == null || user.isEmpty()) {
			p.put("user", "");
			p.put("password", "");
			ret = define.getKind().getDriver().connect(
				url + define.getUrlParams(), p);
		} else {
			p.put("user", user);
			p.put("password", passwd);
			ret = define.getKind().getDriver().connect(
				url + define.getUrlParams(), p);
		}
		ret.setReadOnly(define.isReadOnly());
		ret.setAutoCommit(define.isAutoCommit());
		return ret;
	}
	
	// QuinaProxyConnectionを取得.
	private QuinaProxyConnection _getQuinaProxyConnection(
		boolean notProxy, String user, String passwd)
		throws SQLException {
		checkDestroy();
		Connection c = _getSrcConnection(
			define, define.getUrl(), user, passwd);
		QuinaProxyConnection ret = QuinaProxyUtil.getConnection(
			notProxy, this, c);
		return ret;
	}
	
	@Override
	public QuinaProxyConnection getConnection() throws SQLException {
		QuinaProxyConnection conn;
		// プーリング情報から取得.
		while((conn = pooling.poll()) != null) {
			// ReOpenが成功した場合.
			if(conn.reOpen()) {
				// プーリングオブジェクトを返却.
				return conn;
			}
		}
		// 新規コネクションで取得.
		return _getQuinaProxyConnection(
			false, define.getUser(), define.getPassword());
	}

	@Override
	public QuinaProxyConnection getConnection(
		String username, String password) throws SQLException {
		return _getQuinaProxyConnection(
			true, define.getUser(), define.getPassword());
	}

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		checkDestroy();
		return DriverManager.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		checkDestroy();
		DriverManager.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		checkDestroy();
		DriverManager.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		checkDestroy();
		return DriverManager.getLoginTimeout();
	}
	
	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return null;
	}

	@Override
	public boolean isWrapperFor(Class<?> c) throws SQLException {
		checkDestroy();
		return QuinaDataSource.class.equals(c);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T unwrap(Class<T> c) throws SQLException {
		checkDestroy();
		if (isWrapperFor(c)) {
			return (T) this;
		}
		throw new SQLException(
			"The unmatched class \"" + c + "\"");
	}
}
