package quina.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import quina.exception.QuinaException;
import quina.jdbc.kind.Db2Kind;
import quina.jdbc.kind.H2Kind;
import quina.jdbc.kind.MariaDbKind;
import quina.jdbc.kind.MsSqlKind;
import quina.jdbc.kind.MySqlKind;
import quina.jdbc.kind.OracleKind;
import quina.jdbc.kind.PostgresSqlKind;
import quina.jdbc.kind.QuinaJDBCKind;
import quina.util.Alphabet;
import quina.util.BooleanUtil;
import quina.util.Flag;
import quina.util.NumberUtil;
import quina.util.StringUtil;
import quina.util.collection.IndexMap;

/**
 * QuinaJDBC接続定義.
 */
public class QuinaJDBCConfig {
	// kind定義.
	private QuinaJDBCKind kind = null;
	// 定義名.
	private String name = null;
	// 接続先URL
	private String url = null;
	// 認証ユーザー.
	private String user = null;
	// 認証パスワード.
	private String password = null;
	// 接続タイムアウト値.
	private Integer timeout = null;
	// 読み込み専用の接続.
	private boolean readOnly = false;
	// 自動コミット.
	private boolean autoCommit = false;
	// トランザクションレベル.
	private Integer transactionLevel = null;
	// ResultSetに対する１度の取得サイズ.
	private Integer fetchSize = null;
	// 接続プロパティ.
	private IndexMap<String, Object> properties = null;
	// 接続先のURLパラメータ.
	private String urlParams = "";
	// URLパラメータタイプ.
	private boolean urlType = false;
	
	// プーリングサイズ.
	private Integer poolingSize = null;
	// 最大コネクション数.
	private Integer maxConnection = null;
	// コネクションタイムアウト.
	private Integer connectionTimeout = null;
	
	// oracle の jdbc接続など、末尾に；を付けるとエラーになるものは[true].
	// oracleやderbyなど.
	private boolean notSemicolon = false;
	
	// fixFlag.
	private final Flag fixFlag = new Flag(false);
	
	// サポートKindList.
	private static final QuinaJDBCKind[] KIND_LIST = new QuinaJDBCKind[] {
		Db2Kind.value()
		,H2Kind.value()
		,MariaDbKind.value()
		,MsSqlKind.value()
		,MySqlKind.value()
		,OracleKind.value()
		,PostgresSqlKind.value()
	};
	
	// 指定URLに対する対象Kindを取得.
	private static final QuinaJDBCKind getUrlByKind(String url) {
		if(url == null ||
			(url = url.trim().toLowerCase()).length() == 0) {
			return null;
		}
		final int len = KIND_LIST.length;
		for(int i = 0; i < len; i ++) {
			if(KIND_LIST[i].isUrl(url)) {
				return KIND_LIST[i];
			}
		}
		return null;
	}
	
	/**
	 * コンフィグ情報を読み込む.
	 * @param name 定義名を設定します.
	 * @param conf 対象のコンフィグ定義を設定します.
	 * @return QuinaJDBCConfig kin情報が返却されます.
	 */
	public static final QuinaJDBCConfig create(
		String name, Map<String,Object> conf) {
		final QuinaJDBCConfig ret = create(
			name,
			StringUtil.parseString(conf.get("url")),
			null);
		return ret.setting(conf);
	}
	
	/**
	 * 直接設定.
	 * @param name Kind名を設定します.
	 * @param url 接続URLを設定します.
	 * @return QuinaJDBCConfig kin情報が返却されます.
	 */
	public static final QuinaJDBCConfig create(
		String name, String url) {
		return create(name, url, null);
	}
	
	/**
	 * 直接設定.
	 * @param name Kind名を設定します.
	 * @param url 接続URLを設定します.
	 * @return QuinaJDBCConfig kin情報が返却されます.
	 */
	public static final QuinaJDBCConfig create(
		String name, String url, QuinaJDBCKind kind) {
		final QuinaJDBCConfig ret = new QuinaJDBCConfig();
		name = name == null ? null : name.trim();
		url = url == null ? null : url.trim();
		ret.name = name;
		ret.url = url;
		ret.kind = kind != null ?
			kind : getUrlByKind(url);
		ret.check();
		ret.setNotSemicolon();
		return ret;
	}
	
	/**
	 * コンストラクタ.
	 */
	protected QuinaJDBCConfig() {
		
	}
	
	// 最低限の設定チェック.
	private final void check() {
		if(this.name == null || this.name.isEmpty()) {
			throw new QuinaJDBCException("define name is not set.");
		} else if(this.url == null || this.url.isEmpty()) {
			throw new QuinaJDBCException("jdbc url is not set.");
		} else if(kind == null) {
			throw new QuinaException(
				"The URL of the specified Database (\"" +
				url
				+ "\") is not supported. ");
		}
	}
	
	/**
	 * 定義をFixします
	 */
	public void fix() {
		// 今回の処理でFix完了の場合.
		if(!fixFlag.setToGetBefore(true)) {
			String key;
			// 利用禁止のURLパラメータを取得.
			String[] strAry = kind.notUrlParams();
			// チェックが必要な場合.
			if(strAry.length != 0 &&
				urlParams != null && !urlParams.isEmpty()) {
				String value;
				final int len = strAry.length;
				for(int i = 0; i < len; i += 2) {
					// key, valueを取得.
					key = StringUtil.urlEncode(strAry[i], "UTF8");
					value = StringUtil.urlEncode(strAry[i + 1], "UTF8");
					// チェック処理.
					if(QuinaProxyUtil.eqURLParamsToKeyValue(
						urlParams, key, value)) {
						throw new QuinaException(
							"URL parameters that cannot be specified " +
							"are set (key: " + strAry[i] + ", value: " +
							strAry[i + 1] + ") ");
					}
				}
			}
			strAry = null;
			
			// 利用禁止のPropertyを取得.
			Object[] objAry = kind.notProperty();
			// チェックが必要な場合.
			if(objAry.length != 0 &&
				properties != null && !properties.isEmpty()) {
				Object value;
				final int len = objAry.length;
				for(int i = 0; i < len; i += 2) {
					key = "" + objAry[i];
					// チェック処理.
					if(properties.containsKey(key)) {
						value = properties.get(key);
						if(value == objAry[i +1] ||
							(value != null && value.equals(objAry[i + 1]))) {
							throw new QuinaException(
								"Properties that cannot be specified " +
								"are set (key: " + objAry[i] + ", value: " +
								objAry[i+1] + ") ");
						}
					}
				}
			}
			objAry = null;
			
			// 存在しない場合定義するURLパラメータを取得.
			strAry = kind.addByNotExistUrlParams();
			// 処理が必要な場合.
			if(strAry.length != 0) {
				final int len = strAry.length;
				for(int i = 0; i < len; i += 2) {
					// keyを取得.
					key = StringUtil.urlEncode(strAry[i], "UTF8");
					// URLパラメータが存在しない場合.
					if(urlParams == null || urlParams.isEmpty()) {
						if(urlType) {
							urlParams = "?" + key + "=" +
								StringUtil.urlEncode(strAry[i + 1], "UTF8");
						} else {
							urlParams = ";" + key + "=" +
								StringUtil.urlEncode(strAry[i + 1], "UTF8");
						}
					// 指定キー情報が存在しない.
					} else if(!QuinaProxyUtil.eqURLParamsToKey(urlParams, key)) {
						if(urlType) {
							urlParams += "&" + key + "=" +
								StringUtil.urlEncode(strAry[i + 1], "UTF8");
						} else {
							urlParams += ";" + key + "=" +
								StringUtil.urlEncode(strAry[i + 1], "UTF8");
						}
					}
				}
			}
			strAry = null;
			
			// 存在しない場合定義するプロパティを取得.
			objAry = kind.notProperty();
			// 処理が必要な場合.
			if(objAry.length != 0) {
				final int len = objAry.length;
				for(int i = 0; i < len; i += 2) {
					key = "" + objAry[i];
					// プロパティ設定内容が定義されてないか存在しない場合.
					if(properties == null || properties.isEmpty()) {
						// 定義されてない場合は生成.
						if(properties == null) {
							properties = new IndexMap<String, Object>();
						}
						// 追加処理.
						properties.put(key, objAry[i + 1]);
					// 指定プロパティキーが存在しない場合.
					} else if(!properties.containsKey(key)) {
						// 追加処理.
						properties.put(key, objAry[i + 1]);
					}
				}
			}
			
			// Maxコネクションが設定されてる場合.
			int maxConn = this.getMaxConnection();
			if(maxConn != -1) {
				// プーリングサイズの１．２５倍以上
				// Maxコネクション数が設定されていない場合.
				int poolSize = (int)(
					(double)this.getPoolingSize() * 1.25d);
				if(poolSize > maxConn) {
					// プーリングサイズの１．２５倍を
					// Maxコネクション数とする.
					this.setMaxConnection(poolSize);
				}
			}
		}
	}
	
	/**
	 * Fixしているか取得.
	 * @return boolean trueの場合Fixしています.
	 */
	public boolean isFix() {
		return fixFlag.get();
	}
	
	// Fixしてる場合は例外.
	protected void checkFix() {
		if(fixFlag.get()) {
			throw new QuinaJDBCException("Already confirmed.");
		}
	}
	
	/**
	 * JDBCKindを取得.
	 * @return QuinaJDBCKindが返却されます.
	 */
	public QuinaJDBCKind getKind() {
		return kind;
	}

	/**
	 * 定義名を取得.
	 * @return String 定義名が返されます.
	 */
	public String getName() {
		return name;
	}

	/**
	 * URLを取得.
	 * @return String URLが返却されます.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * 認証ユーザ名を取得.
	 * @return String ユーザ名が返却されます.
	 */
	public String getUser() {
		return user;
	}

	/**
	 * 認証パスワードを取得.
	 * @return String パスワードが返却されます.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * 読み込み専用データベース.
	 * @return boolean [true]の場合は、読み込み専用データベースです.
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	
	/**
	 * AutoCommit設定か取得.
	 * @return boolean  true の場合AutoCommitが行われます.
	 */
	public boolean isAutoCommit() {
		return autoCommit;
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
	 * コネクションに初期セット.
	 * @param conn 対象のコネクションオブジェクトを設定します.
	 */
	protected void appendConnection(Connection conn) {
		appendTransactionLevel(conn);
	}
	
	/**
	 * ステートメントに初期セット.
	 * @param stmt 対象のStatemnetを設定します.
	 * @return Statement 引数のオブジェクトが返却されます.
	 */
	protected Statement appendStatement(Statement stmt) {
		appendTimeout(stmt);
		appendFetchSize(stmt);
		return stmt;
	}
	
	/**
	 * BusyTimeoutを設定.
	 * @param stmt 対象のStatemnetを設定します.
	 */
	protected void appendTimeout(Statement stmt) {
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
	protected void appendFetchSize(Statement stmt) {
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
	protected void appendTransactionLevel(Connection conn) {
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
	protected void appendProperty(Properties prop) {
		if (properties == null || properties.size() == 0) {
			return;
		}
		int len = properties.size();
		for(int i = 0; i < len; i ++) {
			prop.put(properties.getKey(i), properties.getValue(i));
		}
	}
	
	/**
	 * プーリングサイズを取得.
	 * @return Integer プーリングサイズが返却されます.
	 */
	public Integer getPoolingSize() {
		if(poolingSize == null) {
			return QuinaJDBCConstants.getPoolingSize();
		}
		return poolingSize;
	}
	
	/**
	 * MaxConnectionを取得.
	 * @return Integer MaxConnectionが返却されます.
	 */
	public Integer getMaxConnection() {
		if(maxConnection == null) {
			return QuinaJDBCConstants.getMaxConnection();
		}
		return maxConnection;
	}
	
	/**
	 * Maxコネクション数が設定されてるか取得.
	 * @return boolean trueの場合設定されています.
	 */
	public boolean isMaxConnection() {
		return getMaxConnection() != -1;
	}
	
	/**
	 * コネクションタイムアウト値を取得.
	 * @return Integer コネクションタイムアウト値が返却されます.
	 */
	public Integer getConnectionTimeout() {
		if(connectionTimeout ==null) {
			return QuinaJDBCConstants.getConnectionTimeout();
		}
		return connectionTimeout;
	}
	
	/**
	 * SQLの末端にセミコロンを付与させない場合は
	 * 「true」が返却されます.
	 * @return boolean trueの場合SQLの終端のセミコロンは不要です.
	 */
	public boolean isNotSemicolon() {
		return notSemicolon;
	}

	/**
	 * 認証ユーザー情報を設定.
	 * @param user 認証ユーザーを設定します.
	 * @param password 認証パスワードを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setUser(
		String user, String password) {
		checkFix();
		this.user = user;
		this.password = password;
		return this;
	}
	
	/**
	 * 認証ユーザー情報を設定.
	 * @param user 認証ユーザーを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setUser(String user) {
		checkFix();
		this.user = user;
		return this;
	}
	
	/**
	 * 認証パスワード情報を設定.
	 * @param password 認証パスワードを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setPassword(String password) {
		checkFix();
		this.password = password;
		return this;
	}
	
	/**
	 * 読み込みモードをセット.
	 * @param readOnly trueの場合読み込みモードで実行されます.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setReadOnly(boolean readOnly) {
		checkFix();
		this.readOnly = readOnly;
		return this;
	}
	
	/**
	 * オートコミットモードをセット.
	 * @param autoCommit trueの場合オートコミットです.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setAutoCommit(boolean autoCommit) {
		checkFix();
		this.autoCommit = autoCommit;
		return this;
	}
	
	/**
	 * BusyTimeoutを設定.
	 * @param timeout BusyTimeout値を設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setTimeout(Integer timeout) {
		checkFix();
		if(timeout == null || timeout <= 0) {
			timeout = null;
		}
		this.timeout = timeout;
		return this;
	}

	/**
	 * トランザクションレベルを設定.
	 * @param transactionLevel トランザクションレベルを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setTransactionLevel(Object transactionLevel) {
		checkFix();
		this.transactionLevel = transactionLevel(transactionLevel);
		return this;
	}

	/**
	 * フェッチサイズを設定.
	 * @param fetchSize フェッチサイズを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setFetchSize(Integer fetchSize) {
		checkFix();
		this.fetchSize = fetchSize;
		return this;
	}
	
	/**
	 * プロパティ群を設定.
	 * @param properties プロパティ群を設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setProperties(
		Map<String, Object> properties) {
		checkFix();
		if(properties == null || properties.size() == 0) {
			return this;
		}
		if(properties instanceof IndexMap) {
			this.properties = (IndexMap<String, Object>)properties;
		} else {
			this.properties = new IndexMap<String, Object>(properties);
		}
		return this;
	}

	/**
	 * URLパラメータを設定.
	 * @param urlParams Urlパラメータを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setUrlParams(String urlParams) {
		return setUrlParams(null, urlParams);
	}
	
	/**
	 * URLパラメータを設定.
	 * @param urlType urlParamsが Map 形式の場合
	 *                trueの場合 url?xxx=yyy&aaa=bbb で
	 *                falseの場合 url;xxx=yyy;aaa=bbb で
	 *                定義されます.
	 * @param urlParams Urlパラメータを設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	@SuppressWarnings("rawtypes")
	public QuinaJDBCConfig setUrlParams(Boolean urlType, Object urlParams) {
		checkFix();
		urlType = urlType == null ? checkUrlType(url) : urlType;
		if(urlParams == null) {
			this.urlParams = null;
		} else if(urlParams instanceof String) {
			this.urlParams = (String)urlParams;
			if(this.urlParams.isEmpty()) {
				this.urlParams = null;
			}
		} else if(urlParams instanceof Map) {
			this.urlParams = convertUrlParams((Map)urlParams, urlType);
		}
		this.urlType = urlType;
		return this;
	}

	/**
	 * プーリング管理数を設定.
	 * @param size プーリング数を設定します.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setPoolingSize(Integer size) {
		checkFix();
		if(size != null) {
			if(size < QuinaJDBCConstants.MIN_POOLING_SIZE) {
				size = QuinaJDBCConstants.MIN_POOLING_SIZE;
			} else if(size > QuinaJDBCConstants.MAX_POOLING_SIZE) {
				size = QuinaJDBCConstants.MAX_POOLING_SIZE;
			}
			this.poolingSize = size;
		} else {
			this.poolingSize = QuinaJDBCConstants.getPoolingSize();
		}
		return this;
	}
	
	/**
	 * 最大コネクション数を設定.
	 * @param conn 最大コネクション数を設定します.
	 *             0以下を設定した場合、無効に設定出来ます.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setMaxConnection(Integer conn) {
		checkFix();
		if(conn != null) {
			if(conn <= 0) {
				conn = -1;
			} else if(conn < QuinaJDBCConstants.MIN_MAX_CONNECTION) {
				conn = QuinaJDBCConstants.MIN_MAX_CONNECTION;
			} else if(conn > QuinaJDBCConstants.MAX_MAX_CONNECTION) {
				conn = QuinaJDBCConstants.MAX_MAX_CONNECTION;
			}
			this.maxConnection = conn;
		} else {
			this.maxConnection = QuinaJDBCConstants.getMaxConnection();
		}
		return this;
	}
	
	/**
	 * 最大コネクション数を設定.
	 * @param timeout 最大コネクション数を設定します.
	 *                0以下を設定した場合、無効に設定出来ます.
	 * @return QuinaJDBCConfig オブジェクトが返却されます.
	 */
	public QuinaJDBCConfig setConnectionTimeout(Integer timeout) {
		checkFix();
		if(timeout != null) {
			if(timeout <= 0) {
				timeout = -1;
			} else if(timeout < QuinaJDBCConstants.MIN_CONNECTION_TIMEOUT) {
				timeout = QuinaJDBCConstants.MIN_CONNECTION_TIMEOUT;
			} else if(timeout > QuinaJDBCConstants.MAX_CONNECTION_TIMEOUT) {
				timeout = QuinaJDBCConstants.MAX_CONNECTION_TIMEOUT;
			}
			this.connectionTimeout = timeout;
		} else {
			this.connectionTimeout =
				QuinaJDBCConstants.getConnectionTimeout();
		}
		return this;
	}


	// URLに合わせたSQL終端のセミコロン許可を判別してセット.
	protected QuinaJDBCConfig setNotSemicolon() {
		this.notSemicolon = checkNotSemicolon(this.url);
		return this;
	}
	
	// Mapから設定処理.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private QuinaJDBCConfig setting(Map<String, Object> conf) {
		Boolean f;
		Integer i;
		Map m;
		
		String user = StringUtil.parseString(conf.get("user"));
		String password = StringUtil.parseString(conf.get("password"));
		if(password == null || password.isEmpty()) {
			password = StringUtil.parseString(conf.get("passwd"));
		}
		this.setUser(user, password);
		
		f = BooleanUtil.parseBoolean(conf.get("readOnly"));
		if(f != null) {
			this.setReadOnly(f);
		}
		
		f = BooleanUtil.parseBoolean(conf.get("autoCommit"));
		if(f != null) {
			this.setAutoCommit(f);
		}
		
		this.setTimeout(NumberUtil.parseInt(conf.get("timeout")));
		
		this.setTransactionLevel(conf.get("transactionLevel"));
		i = NumberUtil.parseInt(conf.get("fetchSize"));
		if(i == null) {
			i = NumberUtil.parseInt(conf.get("fetch"));
		}
		this.setFetchSize(i);
		
		i = NumberUtil.parseInt(conf.get("poolingSize"));
		if(i == null) {
			i = NumberUtil.parseInt(conf.get("poolSize"));
		}
		this.setPoolingSize(i);
		
		i = NumberUtil.parseInt(conf.get("maxConnection"));
		if(i == null) {
			i = NumberUtil.parseInt(conf.get("maxConn"));
		}
		this.setMaxConnection(i);
		
		i = NumberUtil.parseInt(conf.get("connectionTimeout"));
		if(i == null) {
			i = NumberUtil.parseInt(conf.get("connTimeout"));
		}
		this.setConnectionTimeout(i);
		
		m = null;
		if(conf.get("params") instanceof Map) {
			m = new IndexMap((Map)conf.get("params"));
		} else if(conf.get("property") instanceof Map) {
			m = new IndexMap((Map)conf.get("property"));
		} else if(conf.get("properties") instanceof Map) {
			m = new IndexMap((Map)conf.get("properties"));
		}
		this.setProperties(m);
		this.setUrlParams(
			BooleanUtil.parseBoolean(conf.get("urlType")),
			conf.get("urlParams"));
		this.setNotSemicolon();
		return this;
	}
	
	
	// Map指定したURLパラメータを文字列変換.
	@SuppressWarnings("rawtypes")
	private static final String convertUrlParams(Map m, boolean urlType) {
		if(m == null || m.size() == 0) {
			return null;
		}
		Entry e;
		int cnt = 0;
		final StringBuilder ret = urlType ?
			new StringBuilder("?") : new StringBuilder(";");
		final String bcode = urlType ? "&" : ";";
		final Iterator itr = m.entrySet().iterator();
		while(itr.hasNext()) {
			e = (Entry)itr.next();
			if((cnt ++) != 0) {
				ret.append(bcode);
			}
			//ret.append(e.getKey()).append("=").append(e.getValue());
			ret.append(StringUtil.urlEncode("" + e.getKey(), "utf8"))
				.append("=")
				.append(StringUtil.urlEncode("" + e.getValue(), "utf8"));
		}
		return ret.toString();
	}
	
	// トランザクションレベルを取得.
	private static final Integer transactionLevel(Object o) {
		if(o == null) {
			return null;
		}
		// static int   TRANSACTION_NONE
		//              トランザクションがサポートされていないことを示す定数です。
		// static int   TRANSACTION_READ_COMMITTED
		//              ダーティ読込みは抑制され、繰返し不可の読み込みおよびファントム
		//              読込みが起こることを示す定数です。
		// static int   TRANSACTION_READ_UNCOMMITTED
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが
		//              起こることを示す定数です。
		// static int   TRANSACTION_REPEATABLE_READ
		//              ダーティ読み込みおよび繰返し不可の読込みは抑制され、ファントム
		///             読込みが起こることを示す定数です。
		// static int   TRANSACTION_SERIALIZABLE
		//              ダーティ読み込み、繰返し不可の読み込み、およびファントム読込みが
		//              抑制されることを示す定数です。
		
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
			if(Alphabet.eqArray(s, "none", "transaction_none",
				"transactionNone") != -1) {
				return Connection.TRANSACTION_NONE;
			}
			else if(Alphabet.eqArray(s, "read_committed", "readCommitted",
				"transaction_read_committed", "transactionReadCommitted") != -1) {
				return Connection.TRANSACTION_READ_COMMITTED;
			}
			else if(Alphabet.eqArray(s, "read_uncommitted", "readUncommitted",
				"transaction_read_uncommitted", "transactionReadUncommitted") != -1) {
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			}
			else if(Alphabet.eqArray(s, "repeatable_read", "repeatableRead",
				"transaction_repeatable_read", "transactionRepeatableRead") != -1) {
				return Connection.TRANSACTION_REPEATABLE_READ;
			}
			else if(Alphabet.eqArray(s, "serializable",
				"transaction_serializable", "transactionSerializable") != -1) {
				return Connection.TRANSACTION_SERIALIZABLE;
			}
		}
		return null;
	}
	
	// SQLの末端に「；」セミコロンを付けるとNGかチェック.
	private static final boolean checkNotSemicolon(String url) {
		// java 11 以降は derby は対象とならないのでQuinaでは対象としない.
		if(OracleKind.value().isUrl(url)) {
			return true;
		}
		return false;
	}
	
	// URL定義のパラメータ定義.
	private static final boolean checkUrlType(String url) {
		if(MsSqlKind.value().isUrl(url)
			|| H2Kind.value().isUrl(url)) {
			// ; でURLパラメータをセット.
			return false;
		}
		return true;
	}
	
}
