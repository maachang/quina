package quina.jdbc.kind;

public interface QuinaJDBCKind {
	/**
	 * Driverオブジェクトを取得.
	 * @return java.sql.Driver Driverオブジェクトが返却されます.
	 */
	public java.sql.Driver getDriver();
	
	/**
	 * 対象URLが対象のこのKindかチェック.
	 * @param url 対象のURLを設定します.
	 * @return trueの場合一致しています.
	 */
	public boolean isUrl(String url);
	
	/**
	 * 存在しない場合に追加するURLパラメータ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] addByNotExistUrlParams() {
		return new String[] {};
	}
	
	/**
	 * 指定禁止URLパラメータ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] notUrlParams() {
		return new String[] {};
	}
	
	/**
	 * 存在しない場合に追加するプロパティ群を取得.
	 * @return Object[] key, value, ... で設定されます.
	 */
	default Object[] addByNotExistProperty() {
		return new Object[] {};
	}
	
	/**
	 * 指定禁止プロパティ群を取得.
	 * @return Object[] key, value, ... で設定されます.
	 */
	default Object[] notProperty() {
		return new Object[] {};
	}


}
