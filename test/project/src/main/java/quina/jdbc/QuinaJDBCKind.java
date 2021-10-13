package quina.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import quina.json.JsonOut;
import quina.util.BooleanUtil;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexMap;

/**
 * QuinaJDBCKind.
 */
public class QuinaJDBCKind {
	private String name = null;
	private String driver = null;
	private String url = null;
	private String user = null;
	private String password = null;
	private boolean readOnly = false;
	private Integer timeout = null;
	private Integer transactionLevel = null;
	private Integer fetchSize = null;
	private IndexMap<String, Object> params = null;
	
	private boolean urlType = true;
	private String urlParams = "";
	
	private Integer poolingSize = null;
	private Integer poolingTimeout = null;
	
	// oracle の jdbc接続など、末尾に；を付けるとエラーになるものは[true].
	// oracleやderbyなど.
	private boolean notSemicolon = false;
	
	/**
	 * コンストラクタ.
	 */
	protected QuinaJDBCKind() {
		
	}
	
	/**
	 * コンフィグ情報を読み込む.
	 * @param name
	 * @param conf
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final QuinaJDBCKind create(
		String name, Map<String,Object> conf) {
		QuinaJDBCKind ret = new QuinaJDBCKind();
		// name定義がされている場合は、こちらを優先する.
		ret.name = StringUtil.parseString(conf.get("name"));
		if(ret.name == null || ret.name.isEmpty()) {
			ret.name = name;
		}
		ret.driver = StringUtil.parseString(conf.get("driver"));
		ret.url = StringUtil.parseString(conf.get("url"));
		ret.user = StringUtil.parseString(conf.get("user"));
		ret.password = StringUtil.parseString(conf.get("password"));
		if(ret.password == null || ret.password.isEmpty()) {
			ret.password = StringUtil.parseString(conf.get("passwd"));
		}
		ret.readOnly = BooleanUtil.parseBoolean(conf.get("readOnly"));
		ret.timeout = NumberUtil.parseInt(conf.get("timeout"));
		ret.transactionLevel = transactionLevel(conf.get("transactionLevel"));
		if(ret.transactionLevel == null) {
			ret.transactionLevel = transactionLevel(conf.get("transaction"));
		}
		ret.fetchSize = NumberUtil.parseInt(conf.get("fetchSize"));
		if(ret.fetchSize == null) {
			ret.fetchSize = NumberUtil.parseInt(conf.get("fetch"));
		}
		ret.poolingSize = NumberUtil.parseInt(conf.get("poolingSize"));
		if(ret.poolingSize == null) {
			ret.poolingSize = NumberUtil.parseInt(conf.get("poolSize"));
		}
		ret.poolingTimeout = NumberUtil.parseInt(conf.get("poolingTimeout"));
		if(ret.poolingTimeout == null) {
			ret.poolingTimeout = NumberUtil.parseInt(conf.get("poolTimeout"));
		}
		if(conf.get("params") instanceof Map) {
			ret.params = new IndexMap((Map)conf.get("params"));
		}
		ret.urlType = BooleanUtil.parseBoolean(conf.get("urlType"));
		{
			Object o = conf.get("urlParams");
			if(o == null) {
				ret.urlParams = null;
			} else if(o instanceof Map) {
				ret.urlParams = convertUrlParams((Map)o, ret.urlType);
			} else if(o instanceof String) {
				ret.urlParams = (String)o;
				if(ret.urlParams.isEmpty()) {
					ret.urlParams = null;
				}
			}
		}
		ret.notSemicolon = checkNotSemicolon(ret.url);
		ret.check();
		return ret;
	}
	
	/**
	 * 直接設定.
	 * @param name
	 * @param driver
	 * @param url
	 * @param user
	 * @param password
	 * @param readOnly
	 * @param timeout
	 * @param transactionLevel
	 * @param fetchSize
	 * @param poolingSize
	 * @param poolingTimeout
	 * @param urlType
	 * @param urlParams
	 * @param params
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static final QuinaJDBCKind create(String name, String driver, String url,
		String user, String password, boolean readOnly, Integer timeout, Object transactionLevel,
		Integer fetchSize, Integer poolingSize, Integer poolingTimeout, boolean urlType,
		Object urlParams, Map<String, Object> params) {
		QuinaJDBCKind ret = new QuinaJDBCKind();
		ret.name = name;
		ret.driver = driver;
		ret.url = url;
		ret.user = user;
		ret.password = password;
		ret.readOnly = readOnly;
		ret.timeout = timeout;
		ret.transactionLevel = transactionLevel(transactionLevel);
		ret.fetchSize = fetchSize;
		ret.poolingSize = poolingSize;
		ret.poolingTimeout = poolingTimeout;
		ret.params = new IndexMap<String, Object>(params);
		ret.urlType = urlType;
		{
			Object o = urlParams;
			if(o == null) {
				ret.urlParams = null;
			} else if(o instanceof Map) {
				ret.urlParams = convertUrlParams((Map)o, ret.urlType);
			} else if(o instanceof String) {
				ret.urlParams = (String)o;
				if(ret.urlParams.isEmpty()) {
					ret.urlParams = null;
				}
			}
		}
		ret.notSemicolon = checkNotSemicolon(ret.url);
		ret.check();
		return ret;
	}
	
	/**
	 * プーリングなしのコネクション生成用のkindオブジェクトを生成.
	 * @param driver
	 * @param url
	 * @return
	 */
	public static final QuinaJDBCKind create(String driver, String url) {
		QuinaJDBCKind ret = new QuinaJDBCKind();
		ret.name = "notPooling-Connection";
		ret.driver = driver;
		ret.url = url;
		ret.params = new IndexMap<String, Object>();
		ret.notSemicolon = checkNotSemicolon(ret.url);

		ret.check();
		return ret;
	}
	
	// Map指定したURLパラメータを文字列変換.
	@SuppressWarnings("rawtypes")
	private static final String convertUrlParams(Map m, boolean urlType) {
		if(m == null || m.size() == 0) {
			return null;
		}
		Object k;
		final StringBuilder ret = urlType ?
			new StringBuilder("?") : new StringBuilder(";");
		final String bcode = urlType ? "&" : ";";
		final Iterator itr = m.keySet().iterator();
		while(itr.hasNext()) {
			k = itr.next();
			ret.append(k).append("=").append(m.get(k)).append(bcode);
		}
		return ret.toString();
	}
	
	// トランザクションレベルを取得.
	private static final Integer transactionLevel(Object o) {
		// static int   TRANSACTION_NONE
		//              トランザクションがサポートされていないことを示す定数です。
		// static int   TRANSACTION_READ_COMMITTED
		//              ダーティ読込みは抑制され、繰返し不可の読み込みおよびファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_READ_UNCOMMITTED
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_REPEATABLE_READ
		//              ダーティ読み込みおよび繰返し不可の読込みは抑制され、ファントム読込みが起こることを示す定数です。
		// static int   TRANSACTION_SERIALIZABLE
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが抑制されることを示す定数です。
		
		if(NumberUtil.isNumeric(o)) {
			Integer i = (Integer)NumberUtil.parseInt(o);
			if(i.equals(Connection.TRANSACTION_NONE) ||
					i.equals(Connection.TRANSACTION_READ_COMMITTED) ||
					i.equals(Connection.TRANSACTION_READ_UNCOMMITTED) ||
					i.equals(Connection.TRANSACTION_REPEATABLE_READ) ||
					i.equals(Connection.TRANSACTION_SERIALIZABLE)) {
				return i;
			}
		} else {
			String s = ("" + o).toLowerCase();
			if("none".equals(s) || "transaction_none".equals(s)) {
				return Connection.TRANSACTION_NONE;
			}
			if("read_committed".equals(s) || "transaction_read_committed".equals(s)) {
				return Connection.TRANSACTION_READ_COMMITTED;
			}
			if("read_uncommitted".equals(s) || "transaction_read_uncommitted".equals(s)) {
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			}
			if("repeatable_read".equals(s) || "transaction_repeatable_read".equals(s)) {
				return Connection.TRANSACTION_REPEATABLE_READ;
			}
			if("serializable".equals(s) || "transaction_serializable".equals(s)) {
				return Connection.TRANSACTION_SERIALIZABLE;
			}
		}
		return null;
	}
	
	// SQLの末端に「；」セミコロンを付けるとNGかチェック.
	private static final boolean checkNotSemicolon(String url) {
		final String u = url.toLowerCase();
		if(u.startsWith("jdbc:oracle:") ||
			u.startsWith("jdbc:derby:")) {
			return true;
		}
		return false;
	}
	
	// 設定チェック.
	private final void check() {
		if(this.name == null || this.name.isEmpty()) {
			throw new QuinaJDBCException("kind name is not set.");
		}
		if(this.driver == null || this.driver.isEmpty()) {
			throw new QuinaJDBCException("jdbc driver package name is not set.");
		}
		if(this.url == null || this.url.isEmpty()) {
			throw new QuinaJDBCException("jdbc url is not set.");
		}
	}

	/**
	 * kind名を取得.
	 * 
	 * @return String kind名が返されます.
	 */
	public String getName() {
		return name;
	}

	/**
	 * ドライバー名を取得.
	 * 
	 * @return String ドライバー名が返却されます.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * URLを取得.
	 * 
	 * @return String URLが返却されます.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * ユーザ名を取得.
	 * 
	 * @return String ユーザ名が返却されます.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * パスワードを取得.
	 * 
	 * @return String パスワードが返却されます.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 読み込み専用データベース.
	 * 
	 * @return boolean [true]の場合は、読み込み専用データベースです.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * URLパラメータを取得.
	 * 
	 * @return String URLパラメータが返却されます.
	 */
	public String getUrlParams() {
		if (urlParams == null || urlParams.isEmpty()) {
			return "";
		}
		return urlParams;
	}
	
	/**
	 * コネクションに初期セット.
	 * @param conn 対象のコネクションオブジェクトを設定します.
	 */
	protected void setConnection(Connection conn) {
		setTransactionLevel(conn);
	}
	
	/**
	 * ステートメントに初期セット.
	 * @param stmt 対象のStatemnetを設定します.
	 */
	protected void setStatement(Statement stmt) {
		setTimeout(stmt);
		setFetchSize(stmt);
	}
	
	/**
	 * SQL文でセミコロンが許可されてる場合は付与し
	 * 許可されていない場合は削除します.
	 * @param sql SQL文を設定します.
	 * @return String 処理可能なSQL文が返却されます.
	 */
	protected String getSQL(String sql) {
		sql = sql.trim();
		if(!notSemicolon) {
			if(!sql.endsWith(";")) {
				return sql + ";";
			}
		} else if(!sql.endsWith(";")) {
			return sql.substring(0, sql.length() - 1);
		}
		return sql;
	}

	/**
	 * BusyTimeoutを設定.
	 * @param stmt 対象のStatemnetを設定します.
	 */
	protected void setTimeout(Statement stmt) {
		if (timeout != null && timeout > 0) {
			try {
				// 秒単位で設定.
				stmt.setQueryTimeout(timeout);
			} catch (Exception e) {
				throw new QuinaJDBCException(e);
			}
		}
	}

	/**
	 * フェッチサイズを設定.
	 * @param stmt 対象のStatemnetを設定します.
	 */
	protected void setFetchSize(Statement stmt) {
		if (fetchSize != null && fetchSize > 0) {
			try {
				stmt.setFetchSize(fetchSize);
			} catch (Exception e) {
				throw new QuinaJDBCException(e);
			}
		}
	}

	/**
	 * 基本トランザクションレベルを設定.
	 * @param conn 対象のコネクションオブジェクトを設定します.
	 */
	protected void setTransactionLevel(Connection conn) {
		if (transactionLevel != null) {
			try {
				conn.setTransactionIsolation(
					transactionLevel);
			} catch (Exception e) {
				throw new QuinaJDBCException(e);
			}
		}
	}

	/**
	 * Property定義.
	 * @param prop 対象のプロパティを設定します.
	 */
	protected void setProperty(Properties prop) {
		if (params == null || params.size() == 0) {
			return;
		}
		int len = params.size();
		for(int i = 0; i < len; i ++) {
			prop.put(params.getKey(i), params.getValue(i));
		}
	}
	
	/**
	 * プーリングサイズを取得.
	 * @return
	 */
	public Integer getPoolingSize() {
		return poolingSize;
	}
	
	/**
	 * プーリングタイムアウトを取得.
	 * @return
	 */
	public Integer getPoolingTimeout() {
		return poolingTimeout;
	}
	
	/**
	 * SQLの末端にセミコロンを付与させない場合は
	 * 「true」が返却されます.
	 * @return
	 */
	public boolean isNotSemicolon() {
		return notSemicolon;
	}
	
	/**
	 * Kind設定内容をMapで取得.
	 * @return
	 */
	public Map<String, Object> getMap() {
		return new IndexMap<String, Object>(
			"name", name, "driver", driver, "url", url,
			"user", user, "password", password, "readOnly", readOnly,
			"urlParams", urlParams, "timeout", timeout,
			"transactionLevel", transactionLevel, "fetchSize", fetchSize,
			"params", new IndexMap<String, Object>(params), "poolingSize",
			poolingSize, "poolingTimeout", poolingTimeout, "notSemicolon", notSemicolon);
	}
	
	/**
	 * 文字列変換.
	 * @return
	 */
	@Override
	public String toString() {
		return JsonOut.toString(getMap());
	}
}
