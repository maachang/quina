package quina.jdbc.kind;

import quina.jdbc.QuinaJDBCConfig;

public interface QuinaJDBCKind {
	/**
	 * Driverオブジェクトを取得.
	 * @return java.sql.Driver Driverオブジェクトが返却されます.
	 */
	public java.sql.Driver getDriver();
	
	/**
	 * 対象URLが対象のこのKindかチェック.
	 * @param url 対象のURLを設定します.
	 * @return boolean trueの場合一致しています.
	 */
	public boolean isUrlByKind(String url);
	
	/**
	 * 指定URLが組み込みモードかチェック.
	 * @param url String 組み込みモードの場合trueが返却されます.
	 * @return boolean trueの場合組み込みモードです.
	 *                 falseの場合サーバーモードです.
	 */
	default boolean isUrlByEmbedded(String url) {
		return false;
	}
	
	/**
	 * 存在しない場合に追加するURLパラメータ群を取得.
	 * @param embedded 組み込みモードの場合はtrueを設定します.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] addByNotExistUrlParams(boolean embedded) {
		return new String[] {};
	}
	
	/**
	 * 指定禁止URLパラメータ群を取得.
	 * @param embedded 組み込みモードの場合はtrueを設定します.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] notUrlParams(boolean embedded) {
		return new String[] {};
	}
	
	/**
	 * 存在しない場合に追加するプロパティ群を取得.
	 * @param embedded 組み込みモードの場合はtrueを設定します.
	 * @return Object[] key, value, ... で設定されます.
	 */
	default Object[] addByNotExistProperty(boolean embedded) {
		return new Object[] {};
	}
	
	/**
	 * 指定禁止プロパティ群を取得.
	 * @param embedded 組み込みモードの場合はtrueを設定します.
	 * @return Object[] key, value, ... で設定されます.
	 */
	default Object[] notProperty(boolean embedded) {
		return new Object[] {};
	}
	
	/**
	 * URLパスのパラメータに対してURLEncodeを有効にするか取得.
	 * @return boolean trueの場合有効です.
	 */
	default boolean isUrlParamsAsEncode() {
		return true;
	}
	
	/**
	 * QuinaJDBCConfigのFix完了時に呼び出されます.
	 * @param config QuinaJDBCConfigを設定します.
	 */
	default void fix(QuinaJDBCConfig config) {
		
	}
}
