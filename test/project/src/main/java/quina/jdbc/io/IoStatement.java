package quina.jdbc.io;

import java.io.IOException;
import java.sql.PreparedStatement;

import quina.exception.QuinaException;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaResultSet;

/**
 * I/O用ステートメント.
 */
public class IoStatement
	extends AbstractIoStatement<IoStatement> {
	// query result.
	protected DbResult result = null;
	// sql buffer.
	protected StringBuilder sqlBuf = null;
	
	// コンストラクタ.
	@SuppressWarnings("unused")
	private IoStatement() {}
	
	/**
	 * コンストラクタ.
	 * @param conn JDBCコネクションを設定します.
	 * @param args 対象のStatementを取得するパラメータを設定します.
	 */
	public IoStatement(
		QuinaConnection conn, Object... args) {
		init(conn, args);
	}
	
	@Override
	public void close() throws IOException {
		if(result != null) {
			DbResult rs = result;
			result = null;
			try {
				rs.close();
			} catch(Exception e) {}
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
		Object[] params = getExecuteParams();
		// PreparedStatementで処理.
		PreparedStatement ps = null;
		try {
			// PrepareStatementを取得.
			ps = (PreparedStatement)prepareStatement(sql);
			// パラメータが存在する場合.
			if(params != null && params.length > 0) {
				// PreparedStatementパラメータをセット.
				DbUtil.preParams(ps, ps.getParameterMetaData(), params);
			}
			// query返却が必要な場合.
			if(query) {
				// DbResultを返却.
				return DbResult.create(
					(QuinaResultSet)ps.executeQuery(),
					this);
			} else {
				// 処理件数を返却.
				long ret = ps.executeLargeUpdate();
				ps.close();
				return ret;
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
		checkClose();
		checkExecute();
		Object o = executeStatement(true);
		clearSqlAndParmas();
		this.result = (DbResult)o;
		return this.result;
	}
	
	/**
	 * 更新実行.
	 * @return long 処理結果の件数が返却されます.
	 */
	public long executeUpdate() {
		checkClose();
		checkExecute();
		Object o = executeStatement(false);
		clearSqlAndParmas();
		return (Long)o;
	}
}
