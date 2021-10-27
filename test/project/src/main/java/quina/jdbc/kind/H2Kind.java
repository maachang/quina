package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * H2用Kind.
 */
public class H2Kind implements QuinaJDBCKind {
	// シングルトン.
	private static final H2Kind SNGL = new H2Kind();
	
	/**
	 * H2Kindを取得.
	 * @return H2Kind H2Kindが返却されます.
	 */
	public static final H2Kind value() {
		return SNGL;
	}
	
	// コンストラクタ.
	private H2Kind() {}
	
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
				c = Class.forName("org.h2.Driver");
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
		return Alphabet.startsWith(url, "jdbc:h2:");
	}
}
