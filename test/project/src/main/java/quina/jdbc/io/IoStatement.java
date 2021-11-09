package quina.jdbc.io;

import java.util.Map;

import quina.jdbc.QuinaConnection;

/**
 * Read/Write用ステートメント.
 */
public class IoStatement
	extends AbstractIoStatement<IoStatement> {
	
	// コンストラクタ.
	@SuppressWarnings("unused")
	private IoStatement() {}
	
	/**
	 * コンストラクタ.
	 * @param conn JDBCコネクションを設定します.
	 * @param args 対象のPreparedStatementを取得する
	 *             パラメータを設定します.
	 */
	public IoStatement(
		QuinaConnection conn, Object... args) {
		init(conn, args);
	}

	/**
	 * Query実行.
	 * @return DbResult Query実行結果が返却されます.
	 */
	public DbResult executeQuery() {
		// クローズチェック.
		checkClose();
		// 実行可能かチェック.
		checkExecute();
		try {
			// DbResultを取得.
			return (DbResult)executeStatement(true);
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}
	
	/**
	 * 更新実行.
	 * @return long 処理結果の件数が返却されます.
	 */
	public long executeUpdate() {
		// クローズチェック.
		checkClose();
		// 実行可能かチェック.
		checkExecute();
		try {
			// 書き込み処理系を実行.
			Object o = executeStatement(false);
			// 処理結果の件数を返却.
			return (Long)o;
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}
	
	/**
	 * 更新実行.
	 * @param out out[0]に処理結果の件数が設定されます.
	 * @return DbResult 書き込みで発行された結果を返却します.
	 */
	public DbResult executeUpdate(long[] out) {
		// 処理結果の件数をセット.
		if(out != null && out.length > 0) {
			out[0] = executeUpdate();
		} else {
			executeUpdate();
		}
		return getGeneratedKeys();
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず、
	 *  params(columnName, value, columName, value ...)
	 * と設定します.
	 * @param tableName テーブル名を設定します.
	 * @return DbResult 書き込みで発行された結果を返却します.
	 */
	public DbResult insert(String tableName) {
		super.sqlBuf = new StringBuilder();
		DbUtil.createInsert(sqlBuf, tableName, params);
		return executeUpdate(null);
	}
	
	/**
	 * 新規データを挿入.
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return DbResult 書き込みで発行された結果を返却します.
	 */
	public DbResult insert(String tableName,
		Map<String, Object> values) {
		super.sqlBuf = new StringBuilder();
		DbUtil.createInsert(sqlBuf, params, tableName, values);
		return executeUpdate(null);
	}
	
	
}
