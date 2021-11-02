package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * MySql用Kind.
 */
public class MySqlKind implements QuinaJDBCKind {
	// シングルトン.
	private static final MySqlKind SNGL = new MySqlKind();
	
	/**
	 * MySqlKindを取得.
	 * @return MySqlKind MySqlKindが返却されます.
	 */
	public static final MySqlKind value() {
		return SNGL;
	}
	
	private MySqlKind() {}
	
	// Driverオブジェクト管理.
	protected final AtomicObject<java.sql.Driver> drivers =
		new AtomicObject<java.sql.Driver>();
	
	/**
	 * Driverオブジェクトを取得.
	 * @return java.sql.Driver Driverオブジェクトが返却されます.
	 */
	public final java.sql.Driver getDriver() {
		java.sql.Driver driver = drivers.get();
		if(driver == null) {
			Class<?> c;
			try {
				// 新しいDriver取得.
				c = Class.forName("com.mysql.cj.jdbc.Driver");
				driver = (java.sql.Driver)c.getDeclaredConstructor()
					.newInstance();
				drivers.set(driver);
				return driver;
			} catch(Exception e) {
				try {
					// 古いドライバ取得.
					c = Class.forName("com.mysql.jdbc.Driver");
					driver = (java.sql.Driver)c.getDeclaredConstructor()
						.newInstance();
					drivers.set(driver);
					return driver;
				} catch(Exception ee) {
				}
			}
			return null;
		}
		return driver;
	}
	
	/**
	 * 対象URLが対象のこのKindかチェック.
	 * @param url 対象のURLを設定します.
	 * @return trueの場合一致しています.
	 */
	@Override
	public boolean isUrl(String url) {
		if(url == null) {
			return false;
		}
		return Alphabet.startsWith(url, "jdbc:mysql:");
	}
	
	// 存在しない場合に追加するURLパラメータ群.
	private static final String[] NOT_EXIST_URL_PARAMS =
		new String[] {
			// 文字コード.
			"characterEncoding", "utf8"
			// ParameterMetaDataの利用を有効にする.
			,"generateSimpleParameterMetadata", "true"
			// SSL接続を無効にする.
			,"useSSL", "false"
		};
	
	/**
	 * 存在しない場合に追加するURLパラメータ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	public String[] addByNotExistUrlParams() {
		return NOT_EXIST_URL_PARAMS;
	}
	
	// 指定禁止URLパラメータ群.
	private static final String[] NOT_URL_PARAMS =
		new String[] {
			// SSL接続がONの場合エラー.
			"useSSL", "true"
		};
	
	/**
	 * 指定禁止URLパラメータ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	@Override
	public String[] notUrlParams() {
		return NOT_URL_PARAMS;
	}
}
