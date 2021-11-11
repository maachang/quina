package quina.jdbc.io;

import quina.jdbc.QuinaConnection;
import quina.jdbc.io.template.ReadTemplate;

/**
 * 読み込み専用Statement.
 */
public class QueryStatement
	extends AbstractIoStatement<QueryStatement>
	implements ReadTemplate<QueryStatement> {
		
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
	 * @return QueryResult Query実行結果が返却されます.
	 */
	@Override
	public QueryResult executeQuery() {
		return DbUtil.executeQuery(this);
	}
}
