package quina.jdbc.kind;

import quina.util.Alphabet;
import quina.util.AtomicObject;

/**
 * MsSql用Kind.
 */
public class MsSqlKind implements QuinaJDBCKind {
	// シングルトン.
	private static final MsSqlKind SNGL = new MsSqlKind();
	
	/**
	 * MsSqlKindを取得.
	 * @return MsSqlKind MsSqlKindが返却されます.
	 */
	public static final MsSqlKind value() {
		return SNGL;
	}
	
	// コンストラクタ.
	private MsSqlKind() {}
	
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
				c = Class.forName(
					"com.microsoft.sqlserver.jdbc.SQLServerDriver");
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
		return Alphabet.startsWith(url, "jdbc:sqlserver:");	}
}
