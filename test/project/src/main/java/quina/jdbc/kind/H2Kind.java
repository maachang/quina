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
	@Override
	public java.sql.Driver getDriver() {
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
	public boolean isUrlByKind(String url) {
		if(url == null) {
			return false;
		}
		return Alphabet.startsWith(url, "jdbc:h2:");
	}
	
	/**
	 * 指定URLが組み込みモードかチェック.
	 * @param url String 組み込みモードの場合trueが返却されます.
	 * @return boolean trueの場合組み込みモードです.
	 *                 falseの場合サーバーモードです.
	 */
	@Override
	public boolean isUrlByEmbedded(String url) {
		return Alphabet.indexOf(url, ":tcp:") == -1;
	}
	
	/**
	 * URLパスのパラメータに対してURLEncodeを有効にするか取得.
	 * @return boolean trueの場合有効です.
	 */
	@Override
	public boolean isUrlParamsAsEncode() {
		return false;
	}
	
	// 存在しない場合に追加する組み込みモード用URLパラメータ群.
	private static final String[] NOT_EXIST_URL_PARAMS_BY_EMBEDDED =
		new String[] {
			// MVCCモード.
			"MVCC", "TRUE"
			// VM終了時にConnectionClose.
			,"DB_CLOSE_ON_EXIT", "TRUE"
			// ファイルが存在しない場合作成.
			,"IFEXISTS", "FALSE"
			// 通常ロック(READ COMMITTED).
			,"LOCK_MODE", "3"
				// 0はロックがなし.
				// 1はPageStoreエンジンのテーブルレベルのロック.
				// 2はPageStoreエンジンのGCテーブルロック.
				// 3はテーブルレベルのロック.
			// トランザクションログを有効.
			,"LOG", "2"
				// 0: トランザクションログ無効 / FileDescriptor.sync 無効.
				// 1: トランザクションログ有効 / FileDescriptor.sync 無効.
				// 2: トランザクションログ有効 / FileDescriptor.sync 有効.
			// ロールバック有効.
			,"UNDO_LOG", "1"
				// 0: ロールバックを無効, 1: ロールバック有効.
		};
	
	// 存在しない場合に追加するサーバーモード用URLパラメータ群.
	private static final String[] NOT_EXIST_URL_PARAMS_BY_SERVER =
		new String[] {
			// 通常ロック(READ COMMITTED).
			"LOCK_MODE", "3"
				// 0はロックがなし.
				// 1はPageStoreエンジンのテーブルレベルのロック.
				// 2はPageStoreエンジンのGCテーブルロック.
				// 3はテーブルレベルのロック.
			// トランザクションログを有効.
			,"LOG", "2"
				// 0: トランザクションログ無効 / FileDescriptor.sync 無効.
				// 1: トランザクションログ有効 / FileDescriptor.sync 無効.
				// 2: トランザクションログ有効 / FileDescriptor.sync 有効.
			// ロールバック有効.
			,"UNDO_LOG", "1"
				// 0: ロールバックを無効, 1: ロールバック有効.
		};
	
	/**
	 * 存在しない場合に追加するURLパラメータ群を取得.
	 * @param embedded 組み込みモードの場合はtrueを設定します.
	 * @return String[] key, value, ... で設定されます.
	 */
	@Override
	public String[] addByNotExistUrlParams(boolean embedded) {
		// 組み込みモードの場合.
		if(embedded) {
			return NOT_EXIST_URL_PARAMS_BY_EMBEDDED;
		}
		// サーバーモードの場合.
		return NOT_EXIST_URL_PARAMS_BY_SERVER;
	}

}
