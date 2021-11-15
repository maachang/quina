package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * DB2用Kind.
 */
public class Db2Kind implements QuinaJDBCKind {
	// シングルトン.
	private static final Db2Kind SNGL = new Db2Kind();
	
	/**
	 * Db2Kindを取得.
	 * @return Db2Kind Db2Kindが返却されます.
	 */
	public static final Db2Kind value() {
		return SNGL;
	}
	
	// コンストラクタ.
	private Db2Kind() {}
	
	// Driverオブジェクト管理.
	protected final AtomicObject<java.sql.Driver> drivers =
		new AtomicObject<java.sql.Driver>();
	
	/**
	 * Driverオブジェクトを取得.
	 * @return java.sql.Driver Driverオブジェクトが返却されます.
	 */
	@Override
	public java.sql.Driver getDriver() {
		java.sql.Driver driver = drivers.get();
		if(driver == null) {
			Class<?> c;
			try {
				// 新しいDriver取得.
				c = Class.forName("com.ibm.db2.jcc.DB2Driver");
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
	public boolean isUrlByKind(String url) {
		if(url == null) {
			return false;
		}
		return Alphabet.startsWith(
			url, "jdbc:db2:", "jdbc:db2j:net:", "jdbc:ids:");
	}
}
