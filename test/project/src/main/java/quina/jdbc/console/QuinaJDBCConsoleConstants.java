package quina.jdbc.console;

/**
 * JDBCConsole定義.
 */
public class QuinaJDBCConsoleConstants {
	
	/**
	 * [HTTP-Header]JDBCコンソール用ログイン認証トークン.
	 */
	public static final String LOGIN_AUTH_TOKEN = "X-Jdbc-Console-Auth-Token";
	
	/**
	 * [HTTP-Header]JDBCコンソール用シグネチャー.
	 */
	public static final String LOGIN_SIGNETUER_KEY = "X-Jdbc-Console-Signeture";
	
	/**
	 * [パラメータ]JDBCコンソール用シグネチャー.
	 */
	public static final String LOGIN_SIGNETUER_PARAM = "jdbcConsoleSigneture";
	
	/**
	 * デフォルトのログインタイムアウト値.
	 * 30分.
	 */
	public static final long DEF_LOGIN_TIMEOUT = 1800000L;
	
	/**
	 * 最小のログインタイムアウト値.
	 * 5分.
	 */
	public static final long MIN_LOGIN_TIMEOUT = 300000L;
	
	/**
	 * 最大のログインタイムアウト値.
	 * 120分.
	 */
	public static final long MAX_LOGIN_TIMEOUT = 7200000L;
	
	/**
	 * デフォルトのクエリー取得最大件数.
	 */
	public static final int DEF_RESULT_QUERY_COUNT = 100;
	
	/**
	 * 最小のクエリー取得最大件数.
	 */
	public static final int MIN_RESULT_QUERY_COUNT = 50;
	
	/**
	 * 最大のクエリー取得最大件数.
	 */
	public static final int MAX_RESULT_QUERY_COUNT = 500;

}
