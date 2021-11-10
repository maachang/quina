package quina.jdbc.io;

import quina.jdbc.QuinaConnection;

/**
 * 読み込み専用Statement.
 */
public class QueryStatement
	extends AbstractIoStatement<QueryStatement> {
		
	// コンストラクタ.
	@SuppressWarnings("unused")
	private QueryStatement() {}
	
	/**
	 * コンストラクタ.
	 * @param conn JDBCコネクションを設定します.
	 */
	public QueryStatement(QuinaConnection conn) {
		init(conn);
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
	 * Select用のSQL文を生成.
	 * 使い方は以下のように使います.
	 * 
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
	 * @return QueryStatement このオブジェクトが返却されます.
	 */
	public QueryStatement selectSQL(
		String tableName, String... columns) {
		clearParmas();
		super.sqlBuf = new StringBuilder();
		DbUtil.createSelectSQL(sqlBuf, tableName, columns);
		return this;
	}
}
