package quina.jdbc;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import javax.sql.DataSource;

import quina.exception.QuinaException;
import quina.jdbc.kind.QuinaJDBCKind;
import quina.util.AtomicNumber;
import quina.util.Flag;

/**
 * QuinaJDBCDdataSource.
 */
public class QuinaDataSource implements DataSource {
	
	/** 登録番号. **/
	private int regNo;
	
	/** 登録名. **/
	private String regName;
	
	/** プーリングデータ格納用. **/
	private final Queue<QuinaConnection> pooling =
		new ConcurrentLinkedQueue<QuinaConnection>();
	
	/** JDBCService. **/
	private QuinaJDBCService service;
	
	/** JDBCConfig. **/
	private QuinaJDBCConfig config;
	
	/** オブジェクト破棄チェック. **/
	private final Flag destroyFlag = new Flag();
	
	/** 現在のコネクション数. **/
	private final AtomicNumber connectionCount =new AtomicNumber(0);
	
	/**
	 * コンストラクタ.
	 * @param regNo 登録番号を設定します.
	 * @param regName 登録名を設定します.
	 * @param service QuinaJDBCServiceを設定します.
	 * @param config QuinaJDBCDefineを設定します.
	 */
	protected QuinaDataSource(int regNo, String regName,
		QuinaJDBCService service, QuinaJDBCConfig config) {
		this.regNo = regNo;
		this.regName = regName;
		this.service = service;
		this.config = config;
	}
	
	/**
	 * DataSourceを破棄.
	 */
	protected void destroy() {
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
	protected boolean pushPooling(QuinaConnection conn) {
		// コネクションが存在しないか破棄されてる場合.
		if(conn == null || conn.isDestroy()) {
			return false;
		// Pooling管理数を超えてる場合.
		} else if(destroyFlag.get() ||
			config.getPoolingSize() < pooling.size()) {
			// プーリングせずに廃棄.
			try {
				conn.destroy();
			} catch(Exception e) {}
			return false;
		}
		// プーリングにセット.
		pooling.offer(conn);
		// タイムアウト監視セット.
		service.getTimeoutLoopElement().offer(conn);
		return true;
	}
	
	/**
	 * 登録番号を取得
	 * @return int 登録番号が返却されます.
	 */
	public int getNo() {
		return regNo;
	}
	
	/**
	 * 登録名を取得
	 * @return 登録名が返却されます.
	 */
	public String getName() {
		return regName;
	}
	
	/**
	 * デフォルトのデータソースか取得.
	 * @return boolean trueの場合デフォルトのデータソースです.
	 */
	public boolean isDefault() {
		return config.isDefaultDataSource();
	}
	
	/**
	 * Pooling管理オブジェクトを取得.
	 * @return Queue<QuinaConnection> Pooling管理オブジェクトが
	 *                                返却されます.
	 */
	protected Queue<QuinaConnection> getPooling() {
		return pooling;
	}
	
	/**
	 * コネクションカウントを１UP.
	 */
	protected void incConnectionCount() {
		connectionCount.inc();
	}
	
	/**
	 * コネクションカウントを１Down.
	 */
	protected void decConnectionCount() {
		connectionCount.dec();
	}
	
	/**
	 * コネクションカウントを取得.
	 * @return int 現在のコネクションカウントが返却されます.
	 */
	public int getConnectionCount() {
		return connectionCount.get();
	}

	/**
	 * QuinaJDBCConfigを取得.
	 * @return QuinaJDBCConfigが返却されます.
	 */
	public QuinaJDBCConfig getConfig() {
		return config;
	}
	
	/**
	 * コネクションを取得.
	 * @return QuinaConnection コネクションが返却されます.
	 * @exception SQLException SQL例外.
	 */
	@Override
	public QuinaConnection getConnection()
		throws SQLException {
		QuinaConnection conn;
		
		// コネクションタイムアウト値を取得.
		long timeout = -1L;
		if(config.getConnectionTimeout() != -1) {
			timeout = System.currentTimeMillis() +
				(long)config.getConnectionTimeout();
		}
		
		// 最大コネクション数を取得.
		final int maxConnection = config.getMaxConnection();
		
		// サービスが終了するまで実行.
		while(!service.isExit()) {
			
			// プーリング情報から取得.
			while((conn = pooling.poll()) != null) {
				// ReOpenが成功した場合.
				if(conn.reOpen()) {
					// プーリングオブジェクトを返却.
					return conn;
				}
			}
			
			// Maxコネクション数が設定されてる場合
			// 現状が最大コネクション数を上回ってる場合は
			// 一定時間待機してリトライ.
			if(maxConnection != -1 &&
				maxConnection <= connectionCount.get()) {
				// 一定期間待機.
				try {
					Thread.sleep(5L);
				} catch(Exception e) {}
				
				// コネクションタイムアウトを検知.
				if(timeout != -1L &&
					System.currentTimeMillis() > timeout) {
					throw new SQLException(
						"The connection has timed out.");
				}
				continue;
			}
			
			// プーリングコネクションを新規作成.
			return _getQuinaProxyConnection(
				false, config.getUser(), config.getPassword());
		}
		
		// コネクション取得失敗例外.
		throw new SQLException("Failed to get the connection.");
	}
	
	/**
	 * 直接JDBCからコネクションの取得.
	 * 
	 * @param config DbDefineを設定します.
	 * @param url    対象の接続先を設定します.
	 * @param user   対象のユーザ名を設定します.
	 * @param passwd 対象のパスワードを設定します.
	 * @return Connection コネクション情報が返却されます.
	 * @exception SQLExceptino SQL例外.
	 */
	protected static final Connection _getRawConnection(
		QuinaJDBCConfig config, String url, String user, String passwd)
		throws SQLException {
		Connection ret;
		//System.out.println("UrlParams: " + url + config.getUrlParams());
		Properties p = new java.util.Properties();
		config.appendProperty(p);
		if (user == null || user.isEmpty()) {
			p.put("user", "");
			p.put("password", "");
			ret = config.getKind().getDriver().connect(
				url + config.getUrlParams(), p);
		} else {
			p.put("user", user);
			if (passwd == null || passwd.isEmpty()) {
				p.put("password", "");
			} else {
				p.put("password", passwd);
			}
			final QuinaJDBCKind kind = config.getKind();
			if(kind == null) {
				throw new QuinaException("Failed to get kind: " + config.getName());
			}
			final Driver driver = kind.getDriver();
			if(driver == null) {
				throw new QuinaException("Failed to get driver: " + config.getName());
			}
			ret = driver.connect(
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
		Connection c = _getRawConnection(
			config, config.getUrl(), user, passwd);
		QuinaConnection ret = QuinaProxyUtil.getConnection(
			notProxy, this, c);
		return ret;
	}
	
	@Override
	public QuinaConnection getConnection(
		String username, String password) throws SQLException {
		// プーリングしないコネクションを生成.
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
