package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * PostgresSql用Kind.
 */
public class PostgresSqlKind implements QuinaJDBCKind {
	// シングルトン.
	private static final PostgresSqlKind SNGL = new PostgresSqlKind();
	
	/**
	 * PostgresSqlKindを取得.
	 * @return PostgresSqlKind PostgresSqlKindが返却されます.
	 */
	public static final PostgresSqlKind value() {
		return SNGL;
	}
	
	// コンストラクタ.
	private PostgresSqlKind() {}
	
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
				c = Class.forName("org.postgresql.Driver");
				driver = (java.sql.Driver)c.getDeclaredConstructor()
					.newInstance();
				drivers.set(driver);
				return driver;
			} catch(Exception e) {
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
		return Alphabet.startsWith(url, "jdbc:postgresql:");
	}

}
