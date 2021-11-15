package quina.jdbc.console;

/**
 * JDBCConsole定義.
 */
public class QuinaJDBCConsoleConstants {
	
	/**
	 * [HTTP-Header]JDBCコンソール用ログイン認証コード.
	 */
	public static final String LOGIN_AUTH_CODE_HEAEDER_NAME = "X-Jdbc-Console-Auth-Code";
	
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
	 * 最小のログインタイムアウト値.
	 * 120分.
	 */
	public static final long MAX_LOGIN_TIMEOUT = 7200000L;

}
