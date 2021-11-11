package quina.jdbc.io;

import quina.jdbc.QuinaConnection;
import quina.jdbc.io.template.ReadTemplate;
import quina.jdbc.io.template.WriteTemplate;

/**
 * Read/Write用ステートメント.
 */
public class IoStatement
	extends AbstractIoStatement<IoStatement>
	implements ReadTemplate<IoStatement>,
		WriteTemplate<IoStatement> {
	
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
	 * @return QueryResult Query実行結果が返却されます.
	 */
	@Override
	public QueryResult executeQuery() {
		return DbUtil.executeQuery(this);
	}
	
	/**
	 * 更新実行.
	 * @param out out out[0]にQueryResultが返却されます.
	 * @param outCount outCount[0]に処理結果の件数が設定されます.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	@Override
	public IoStatement executeUpdate(
		QueryResult[] out, long[] outCount) {
		if(out != null && out.length > 0) {
			out[0] = DbUtil.executeUpdate(outCount, this);
		} else {
			DbUtil.executeUpdate(outCount, this);
		}
		return this;
	}
}
