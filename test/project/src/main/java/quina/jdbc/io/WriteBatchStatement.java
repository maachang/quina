package quina.jdbc.io;

import quina.exception.QuinaException;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaPreparedStatement;

/**
 * WriteBatch用Statement.
 */
public class WriteBatchStatement
	extends AbstractIoStatement<WriteBatchStatement> {
	
	// コンストラクタ.
	@SuppressWarnings("unused")
	private WriteBatchStatement() {}
	
	/**
	 * コンストラクタ.
	 * @param conn JDBCコネクションを設定します.
	 * @param sql 対象のSQL文を設定します.
	 * @param args 対象のPreparedStatementを取得する
	 *             パラメータを設定します.
	 */
	public WriteBatchStatement(
		QuinaConnection conn, String sql,Object... args) {
		if(sql == null || (sql = sql.trim()).isEmpty()) {
			throw new QuinaException(
				"The SQL statement to be executed is not set.");
		}
		try {
			init(conn, args);
			prepareStatement(sql);
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * 実行可能チェック.
	 */
	protected void checkExecute() {
		if(params == null || params.size() == 0) {
			throw new QuinaException(
				"The SQL paramsto be executed is not set.");
		}
	}
	
	/**
	 * １つの条件をBatch実行.
	 * @return WriteBatchStatement このオブジェクトが返却されます.
	 */
	public WriteBatchStatement add() {
		// クローズチェック.
		checkClose();
		// 実行可能チェック.
		checkExecute();
		QuinaPreparedStatement ps = null;
		try {
			// Batch実行対象のPreparedStatementを取得.
			ps = nowPreparedStatement();
			// パラメータを反映.
			this.updateParams(ps);
			// Batchセット.
			ps.addBatch();
			// パラメータクリア.
			clearParmas();
			return this;
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
	 * Batch実行された内容を反映.
	 * @return long[] add実行されたBatch実行結果が配列毎に返却されます.
	 */
	public long[] execute() {
		// クローズチェック.
		checkClose();
		QuinaPreparedStatement ps = null;
		try {
			// Batch実行対象のPreparedStatementを取得.
			ps = nowPreparedStatement();
			// Batch実行.
			return ps.executeLargeBatch();
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
	 * Batch実行された内容を反映.
	 * @param out Object[0]に対してlong[n]の各Batch実行件数が返却されます.
	 * @return WriteBatchStatement このオブジェクトが返却されます.
	 */
	public WriteBatchStatement execute(Object[] out) {
		if(out != null && out.length > 0) {
			out[0] = execute();
		} else {
			execute();
		}
		return this;
	}
}
