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
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement executeUpdate() {
		return executeUpdate(null, null);
	}
	
	/**
	 * 更新実行.
	 * @param out out[0]に処理結果の件数が設定されます.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement executeUpdate(long[] out) {
		return executeUpdate(null, out);
	}
	
	/**
	 * 更新実行.
	 * @param out out out[0]にDbResultが返却されます.
	 * @param outCount outCount[0]に処理結果の件数が設定されます.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement executeUpdate(DbResult[] out, long[] outCount) {
		// クローズチェック.
		checkClose();
		// 実行可能かチェック.
		checkExecute();
		try {
			// 書き込み処理系を実行.
			if(outCount != null && outCount.length > 0) {
				outCount[0] = (Long)executeStatement(false);
			} else {
				executeStatement(false);
			}
			if(out != null && out.length > 0) {
				out[0] = getGeneratedKeys();
			}
			return this;
		} finally {
			// 登録されてたSQLとパラメータをクリア.
			clearSqlAndParmas();
		}
	}

	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.params("id", 100, "age", 25, "name", "hoge")
	 *     .insert("testTable")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param tableName テーブル名を設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement insert(String tableName) {
		return insert(null, tableName);
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.params("id", 100, "age", 25, "name", "hoge")
	 *     .insert(null, "testTable")
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param out [0]に書き込みで発行された結果を返却します.
	 * @param tableName テーブル名を設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement insert(DbResult[] out, String tableName) {
		super.sqlBuf = new StringBuilder();
		params = DbUtil.createInsert(sqlBuf, tableName, params);
		executeUpdate(out, null);
		return this;
	}
	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("id", 100);
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert("testTable", keyValues)
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return DbResult 書き込みで発行された結果を返却します.
	 */
	public IoStatement insert(
		String tableName, Map<String, Object> values) {
		return insert(null, tableName, values);
	}

	
	/**
	 * 新規データを挿入.
	 * この処理の場合sql()呼び出しはせず以下のように実装します.
	 * 
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("id", 100);
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.insert(null, "testTable", keyValues)
	 *     .commit();
	 * 
	 * これにより"testTable"に対してid=100, age=25, name=hoge
	 * 内容がInsertされます.
	 * @param out [0]に書き込みで発行された結果を返却します.
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return DbResult 書き込みで発行された結果を返却します.
	 */
	public IoStatement insert(DbResult[] out, String tableName,
		Map<String, Object> values) {
		sqlBuf = new StringBuilder();
		params = DbUtil.createInsert(sqlBuf, params, tableName, values);
		executeUpdate(out, null);
		return this;
	}
	
	/**
	 * Update用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * Map<String, Object> keyValues = new HashMap<>();
	 * keyValues.put("age", 25);
	 * keyValues.put("name", "hoge");
	 * stmt.updateSQL("testTable", keyValues)
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > updata testTable set age=25, name='hoge' where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement updateSQL(String tableName,
		Map<String, Object> values) {
		sqlBuf = new StringBuilder();
		params = DbUtil.createUpdate(sqlBuf, params, tableName, values);
		return this;
	}
	
	/**
	 * Update用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * stmt.updateSQL("testTable", "age", 25, "name", "hoge")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeUpdate();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > updata testTable set age=25, name='hoge' where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名,value=要素の
	 *               Mapを設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement updateSQL(String tableName, Object... data) {
		sqlBuf = new StringBuilder();
		params = DbUtil.createUpdateSQL(sqlBuf, params, tableName, data);
		return this;
	}
	
	/**
	 * Select用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
	 * IoStatement stmt = conn.ioStatement();
	 * DbResult res = stmt
	 *     .selectSQL("testTable", "age", "name")
	 *     .sql("where id=?")
	 *     .params(100);
	 *     .executeQuery();
	 * 
	 * 以下のようなSQL文が実行されます.
	 * > select age, name from testTable where id=100;
	 * 
	 * @param tableName テーブル名を設定します.
	 * @param values 追加対象のkey=カラム名, value=要素の
	 *               Mapを設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement selectSQL(String tableName, String... columns) {
		clearParmas();
		sqlBuf = new StringBuilder();
		DbUtil.createSelectSQL(sqlBuf, tableName, columns);
		return this;
	}
}
