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
}
