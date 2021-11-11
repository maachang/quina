package quina.jdbc.io.template;

import quina.jdbc.io.QueryResult;
import quina.util.collection.ObjectList;

/**
 * ベーステンプレート.
 */
public interface BaseTemplate<T> {
	
	/**
	 * クローズチェック.
	 */
	public void checkClose();
	
	/**
	 * 実行可能チェック.
	 */
	public void checkExecute();
	
	/**
	 * 実行用のパラメーターをクリア.
	 * @return T このオブジェクトが返却されます.
	 */
	public T clearParmas();
	
	/**
	 * 実行用のパラメータを直接設定.
	 * @param params 実行用のパラメータを直接設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T setParams(ObjectList<Object> params);
	
	/**
	 * 実行用のパラメータを取得.
	 * @return ObjectList<Object> 実行用のパラメータが返却されます.
	 */
	public ObjectList<Object> getParams();
	
	/**
	 * 実行用のSQL文をクリア.
	 * @return StringBuilder 空のStringBuilderが返却されます.
	 */
	public StringBuilder clearSql();
	
	/**
	 * 利用中のSQL文を取得.
	 * @return StringBuilder 利用中のSQL文が返却されます.
	 */
	public StringBuilder getSql();
	
	/**
	 * SQL用のバッファを設定.
	 * @param sqlBuf 対象のSQLバッファを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T setSql(StringBuilder sqlBuf);
	
	/**
	 * 実行用のSQL文とパラメータをクリア.
	 * @return T このオブジェクトが返却されます.
	 */
	public T clearSqlAndParmas();
	
	/**
	 * 実行処理.
	 * @param query trueの場合Query実行を行います.
	 * @return Object trueの場合 QueryResult が返却されます.
	 *                falseの場合 処理件数が返却されます.
	 */
	public Object executeStatement(boolean query);
	
	/**
	 * Insertで付与されたシーケンスID結果を取得.
	 * @return QueryResult シーケンスID結果が返却されます.
	 *                  nullの場合、取得出来ませんでした.
	 */
	public QueryResult getGeneratedKeys();
}
