package quina.jdbc.io;

import java.io.IOException;

import quina.exception.QuinaException;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaPreparedStatement;
import quina.jdbc.QuinaResultSet;
import quina.util.collection.ObjectList;

/**
 * I/O用ステートメント.
 */
public class IoStatement
	extends AbstractIoStatement<IoStatement> {
	// query result list.
	protected ObjectList<DbResult> resultList = null;
	// sql buffer.
	protected StringBuilder sqlBuf = null;
	
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
	
	@Override
	public void close() throws IOException {
		if(resultList != null) {
			final ObjectList<DbResult> rsList = resultList;
			resultList = null;
			final int len = rsList.size();
			for(int i = 0; i < len; i ++) {
				try {
					rsList.get(i).close();
				} catch(Exception e) {}
			}
		}
		sqlBuf = null;
		super.close();
	}
	
	/**
	 * SQL文を設定.
	 * @param sql 対象のSQL文を設定します.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement sql(String sql) {
		checkClose();
		if(sql == null || (sql = sql.trim()).isEmpty()) {
			throw new QuinaException(
				"The specified SQL content is empty.");
		}
		if(sqlBuf == null) {
			sqlBuf = new StringBuilder();
		} else if(sqlBuf.length() != 0) {
			sqlBuf.append(" ");
		}
		sqlBuf.append(sql);
		return this;
	}
	
	/**
	 * 実行可能チェック.
	 */
	protected void checkExecute() {
		if(sqlBuf == null || sqlBuf.length() ==0) {
			throw new QuinaException(
				"The SQL statement to be executed is not set.");
		}
	}
	
	/**
	 * 実行用のSQLとパラメーターをクリア.
	 */
	protected final void clearSqlAndParmas() {
		super.clearParmas();
		sqlBuf = null;
	}
	
	/**
	 * 実行SQL文を取得.
	 * @return String sql文が返却されます.
	 */
	protected final String getExecuteSql() {
		return sqlBuf.toString();
	}
	
	/**
	 * 実行処理.
	 * @param query trueの場合Query実行を行います.
	 * @return Object trueの場合 DbResult が返却されます.
	 *                falseの場合 処理件数が返却されます.
	 */
	protected final Object executeStatement(boolean query) {
		// 実行SQLとパラメーターを取得.
		String sql = getExecuteSql();
		// QuinaPreparedStatementで処理.
		QuinaPreparedStatement ps = null;
		try {
			// QuinaPreparedStatementを取得.
			ps = prepareStatement(sql);
			
			// パラメータを反映.
			this.updateParams(ps);
			
			// query返却が必要な場合.
			if(query) {
				// DbResultを返却.
				return DbResult.create(
					(QuinaResultSet)ps.executeQuery(),
					this);
			} else {
				// 処理件数を返却.
				return ps.executeLargeUpdate();
			}
		} catch(QuinaException qe) {
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception e) {}
			}
			throw qe;
		} catch(Exception e) {
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception ee) {}
			}
			throw new QuinaException(e);
		}
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
		// DbResultを取得.
		Object o = executeStatement(true);
		// 登録されてたSQLとパラメータをクリア.
		clearSqlAndParmas();
		// DbResultを取得.
		DbResult rs = (DbResult)o;
		// DbResultを登録.
		if(resultList == null) {
			resultList = new ObjectList<DbResult>();
		}
		resultList.add(rs);
		return rs;
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
		// 書き込み処理系を実行.
		Object o = executeStatement(false);
		// 登録されてたSQLとパラメータをクリア.
		clearSqlAndParmas();
		// 処理結果の件数を返却.
		return (Long)o;
	}
	
	/**
	 * 更新実行.
	 * @param out out[0]に処理結果の件数が設定されます.
	 * @return IoStatement このオブジェクトが返却されます.
	 */
	public IoStatement executeUpdate(long[] out) {
		// 処理結果の件数をセット.
		if(out != null && out.length > 0) {
			out[0] = executeUpdate();
		} else {
			executeUpdate();
		}
		return this;
	}
}