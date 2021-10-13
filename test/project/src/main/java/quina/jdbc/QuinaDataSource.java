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

	/** JDBCKind. **/
	private QuinaJDBCKind kind;
	
	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();

	/**
	 * コンストラクタ.
	 * @param kind QuinaJDBCKindを設定します.
	 */
	public QuinaDataSource(QuinaJDBCKind kind) {
		this.kind = kind;
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
	
	// Poolingにセット.
	protected boolean pushPooling(QuinaProxyConnection conn) {
		if(destroyFlag.get() ||
			kind.getPoolingSize() < pooling.size()) {
			try {
				conn.destroy();
			} catch(Exception e) {}
			return false;
		}
		pooling.offer(conn);
		return true;
	}

	/**
	 * QuinaJDBCKindを取得.
	 * @return QuinaJDBCKindが返却されます.
	 */
	public QuinaJDBCKind getKind() {
		return kind;
	}
	
	/**
	 * 読み書きコネクションの取得.
	 * 
	 * @param kind   DbKindを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 * @exception SQLExceptino SQL例外.
	 */
	protected static final Connection _getConnection(
		QuinaJDBCKind kind, String url, String user, String passwd)
		throws SQLException {
		Connection ret;
		Properties p = new java.util.Properties();
		kind.setProperty(p);
		if (user == null || user.length() <= 0) {
			p.put("user", "");
			p.put("password", "");
			ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
		} else {
			p.put("user", user);
			p.put("password", passwd);
			ret = DriverManager.getConnection(url + kind.getUrlParams(), p);
		}
		ret.setReadOnly(false);
		ret.setAutoCommit(false);
		return ret;
	}
	
	// QuinaProxyConnectionを取得.
	private QuinaProxyConnection _getQuinaProxyConnection(
		boolean notProxy, String user, String passwd)
		throws SQLException {
		checkDestroy();
		Connection c = _getConnection(
			kind, kind.getUrl(), user, passwd);
		QuinaProxyConnection ret = QuinaProxyUtil.getConnection(
			notProxy, this, c);
		return ret;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		QuinaProxyConnection conn = pooling.poll();
		if(conn == null || !conn.reOpen()) {
			return _getQuinaProxyConnection(
				false, kind.getUser(), kind.getPassword());
		}
		return conn;
	}

	@Override
	public Connection getConnection(
		String username, String password) throws SQLException {
		return _getQuinaProxyConnection(
			true, kind.getUser(), kind.getPassword());
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
