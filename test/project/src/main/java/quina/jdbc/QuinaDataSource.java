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
	protected final Queue<QuinaConnection> pooling =
		new ConcurrentLinkedQueue<QuinaConnection>();

	/** JDBCConfig. **/
	private QuinaJDBCConfig config;
	
	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();

	/**
	 * コンストラクタ.
	 * @param config QuinaJDBCDefineを設定します.
	 */
	protected QuinaDataSource(QuinaJDBCConfig config) {
		this.config = config;
	}
	
	/**
	 * DataSourceを破棄.
	 */
	public void destroy() {
		if (!destroyFlag.setToGetBefore(true)) {
			// 保持しているコネクションを全て破棄.
			QuinaConnection conn;
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
	protected boolean pushPooling(
		QuinaConnection conn) {
		// 既に破棄されてるか、Pooling管理数を超えてる場合.
		if(destroyFlag.get() ||
			config.getPoolingSize() < pooling.size()) {
			// プーリングせずに廃棄.
			try {
				conn.destroy();
			} catch(Exception e) {}
			return false;
		}
		// プーリングにセット.
		pooling.offer(conn);
		return true;
	}

	/**
	 * QuinaJDBCConfigを取得.
	 * @return QuinaJDBCConfigが返却されます.
	 */
	public QuinaJDBCConfig getConfig() {
		return config;
	}
	
	/**
	 * JDBCコネクションの取得.
	 * 
	 * @param config DbDefineを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 * @exception SQLExceptino SQL例外.
	 */
	protected static final Connection _getSrcConnection(
		QuinaJDBCConfig config, String url, String user, String passwd)
		throws SQLException {
		Connection ret;
		Properties p = new java.util.Properties();
		config.appendProperty(p);
		if (user == null || user.isEmpty()) {
			p.put("user", "");
			p.put("password", "");
			ret = config.getKind().getDriver().connect(
				url + config.getUrlParams(), p);
		} else {
			p.put("user", user);
			p.put("password", passwd);
			ret = config.getKind().getDriver().connect(
				url + config.getUrlParams(), p);
		}
		ret.setReadOnly(config.isReadOnly());
		ret.setAutoCommit(config.isAutoCommit());
		return ret;
	}
	
	// QuinaProxyConnectionを取得.
	private QuinaConnection _getQuinaProxyConnection(
		boolean notProxy, String user, String passwd)
		throws SQLException {
		checkDestroy();
		Connection c = _getSrcConnection(
			config, config.getUrl(), user, passwd);
		QuinaConnection ret = QuinaProxyUtil.getConnection(
			notProxy, this, c);
		return ret;
	}
	
	@Override
	public QuinaConnection getConnection() throws SQLException {
		QuinaConnection conn;
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
			false, config.getUser(), config.getPassword());
	}

	@Override
	public QuinaConnection getConnection(
		String username, String password) throws SQLException {
		return _getQuinaProxyConnection(
			true, config.getUser(), config.getPassword());
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
