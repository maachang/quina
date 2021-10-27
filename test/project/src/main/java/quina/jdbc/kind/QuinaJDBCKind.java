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
	 * 指定禁止URLパラメータ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] notUrlParams() {
		return new String[] {};
	}
	
	/**
	 * 指定禁止プロパティ群を取得.
	 * @return String[] key, value, ... で設定されます.
	 */
	default String[] notProperty() {
		return new String[] {};
	}


}
