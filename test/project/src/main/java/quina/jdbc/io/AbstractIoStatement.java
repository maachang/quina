package quina.jdbc.io;

import java.io.IOException;

import quina.exception.QuinaException;
import quina.jdbc.QuinaPreparedStatement;
import quina.jdbc.QuinaResultSet;
import quina.util.collection.ObjectList;

/**
 * AbstractIoStatement.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractIoStatement<T>
	extends AbstractStatement<T> {
	// 利用したDbResultを管理..
	protected ObjectList<DbResult> resultList = null;
	// SQLBuffer.
	protected StringBuilder sqlBuf = null;
	// 現在利用中のPreparedStatement.
	protected QuinaPreparedStatement nowStatement = null;
	
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
		nowStatement = null;
		super.close();
	}
	
	/**
	 * SQL文を設定.
	 * @param sql 対象のSQL文を設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T sql(String sql) {
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
		return (T)this;
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
	protected void clearSqlAndParmas() {
		super.clearParmas();
		sqlBuf = null;
	}
	
	/**
	 * 実行SQL文を取得.
	 * @return String sql文が返却されます.
	 */
	protected String getExecuteSql() {
		return sqlBuf.toString();
	}
	
	/**
	 * 実行処理.
	 * @param query trueの場合Query実行を行います.
	 * @return Object trueの場合 DbResult が返却されます.
	 *                falseの場合 処理件数が返却されます.
	 */
	protected Object executeStatement(boolean query) {
		// 実行SQLとパラメーターを取得.
		String sql = getExecuteSql();
		// QuinaPreparedStatementで処理.
		QuinaPreparedStatement ps = null;
		DbResult ret = null;
		try {
			// QuinaPreparedStatementを取得.
			ps = prepareStatement(sql);
			
			// パラメータを反映.
			this.updateParams(ps);
			
			// 現在利用中のPreparedStatementをセット.
			nowStatement = ps;
			
			// query返却が必要な場合.
			if(query) {
				// DbResultを返却.
				ret = DbResult.create(
					(QuinaResultSet)ps.executeQuery(),
					this);
				// DbResultを登録.
				if(resultList == null) {
					resultList = new ObjectList<DbResult>();
				}
				resultList.add(ret);
				return ret;
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
			if(ret != null) {
				try {
					ret.close();
				} catch(Exception e) {}
			}
			throw qe;
		} catch(Exception e) {
			if(ps != null) {
				try {
					ps.close();
				} catch(Exception ee) {}
			}
			if(ret != null) {
				try {
					ret.close();
				} catch(Exception ee) {}
			}
			throw new QuinaException(e);
		}
	}
	
	/**
	 * Insertで付与されたシーケンスID結果を取得.
	 * @return DbResult シーケンスID結果が返却されます.
	 *                  nullの場合、取得出来ませんでした.
	 */
	protected DbResult getGeneratedKeys() {
		if(nowStatement == null || statementArgs == null ||
			statementArgs.length == 0) {
			return null;
		}
		DbResult ret = null;
		try {
			// DBResultを取得.
			ret = DbResult.create(
				nowStatement.getGeneratedKeys(),
				this);
			// DbResultを登録.
			if(resultList == null) {
				resultList = new ObjectList<DbResult>();
			}
			resultList.add(ret);
			return ret;
		} catch(QuinaException qe) {
			if(ret != null) {
				try {
					ret.close();
				} catch(Exception e) {}
			}
			throw qe;
		} catch(Exception e) {
			if(ret != null) {
				try {
					ret.close();
				} catch(Exception ee) {}
			}
			throw new QuinaException(e);
		}
	}
}
