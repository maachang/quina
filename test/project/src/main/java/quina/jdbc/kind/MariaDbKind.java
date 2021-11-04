package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * MarriaDb用Kind.
 */
public class MariaDbKind implements QuinaJDBCKind {
	// シングルトン.
	private static final MariaDbKind SNGL = new MariaDbKind();
	
	/**
	 * MariaDbKindを取得.
	 * @return MariaDbKind MariaDbKindが返却されます.
	 */
	public static final MariaDbKind value() {
		return SNGL;
	}
	
	// コンストラクタ.
	private MariaDbKind() {}
	
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
				c = Class.forName(
					"org.mariadb.jdbc.Driver");
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
		return Alphabet.startsWith(url, "jdbc:mariadb:");
	}

}