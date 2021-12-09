package quina.jdbc.io;

import quina.exception.QuinaException;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaDataSource;
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
	
	/**
	 * Read/Write用ステートメントコール実行.
	 * @param ds QuinaDataSourceを設定します.
	 * @param call JDBCI/Oコールインターフェイスを設定します.
	 * @return Object IoStatementCall.execute(ios)の処理結果を返却.
	 */
	public static final Object execute(
		QuinaDataSource ds, IoStatementCall call) {
		QuinaConnection conn = null;
		IoStatement ios = null;
		try {
			Object ret = null;
			// コネクション取得.
			conn = ds.getConnection();
			ios = conn.ioStatement();
			
			// JDBCI/Oコール実行.
			ret = call.execute(ios);
			
			// 終了処理.
			ios.commit();
			ios.close();
			ios = null;
			conn.close();
			conn = null;
			
			return ret;
		} catch(QuinaException qe) {
			try {
				ios.rollback();
			} catch(Exception ee) {}
			throw qe;
		} catch(Exception e) {
			try {
				ios.rollback();
			} catch(Exception ee) {}
			throw new QuinaException(e);
		} finally {
			if(ios != null) {
				try {
					ios.close();
				} catch(Exception ee) {}
			}
			if(conn != null) {
				try {
					conn.close();
				} catch(Exception ee) {}
			}
		}
	}
}
