package quina.jdbc.io;

import java.io.IOException;

import quina.exception.QuinaException;
import quina.jdbc.QuinaPreparedStatement;
import quina.jdbc.QuinaResultSet;
import quina.jdbc.io.template.BaseTemplate;
import quina.util.collection.ObjectList;

/**
 * AbstractIoStatement.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractIoStatement<T>
	extends AbstractStatement<T>
	implements BaseTemplate<T> {
	// 利用したQueryResultを管理..
	protected ObjectList<QueryResult> resultList = null;
	// SQLBuffer.
	protected StringBuilder sqlBuf = null;
	// 現在利用中のPreparedStatement.
	protected QuinaPreparedStatement nowStatement = null;
	
	@Override
	public void close() throws IOException {
		if(resultList != null) {
			final ObjectList<QueryResult> rsList = resultList;
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
	@Override
	public void checkExecute() {
		if(sqlBuf == null || sqlBuf.length() ==0) {
			throw new QuinaException(
				"The SQL statement to be executed is not set.");
		}
	}
	
	/**
	 * 実行用のパラメータを直接設定.
	 * @param params 実行用のパラメータを直接設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	@Override
	public T setParams(ObjectList<Object> params) {
		super.params = params;
		return (T)this;
	}
	
	/**
	 * 実行用のパラメータを取得.
	 * @return ObjectList<Object> 実行用のパラメータが返却されます.
	 */
	@Override
	public ObjectList<Object> getParams() {
		return super.params;
	}
	
	/**
	 * 実行用のSQL文をクリア.
	 * @return StringBuilder 空のStringBuilderが返却されます.
	 */
	@Override
	public StringBuilder clearSql() {
		sqlBuf = new StringBuilder();
		return sqlBuf;
	}
	
	/**
	 * 利用中のSQL文を取得.
	 * @return StringBuilder 利用中のSQL文が返却されます.
	 */
	@Override
	public StringBuilder getSql() {
		return sqlBuf;
	}
	
	/**
	 * SQL用のバッファを設定.
	 * @param sqlBuf 対象のSQLバッファを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	@Override
	public T setSql(StringBuilder sqlBuf) {
		if(sqlBuf == null) {
			sqlBuf = new StringBuilder();
		}
		this.sqlBuf = sqlBuf;
		return (T)this;
	}

	
	/**
	 * 実行用のSQLとパラメーターをクリア.
	 */
	@Override
	public T clearSqlAndParmas() {
		super.clearParmas();
		sqlBuf = null;
		return (T)this;
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
	 * @return Object trueの場合 QueryResult が返却されます.
	 *                falseの場合 処理件数が返却されます.
	 */
	public Object executeStatement(boolean query) {
		// 実行SQLとパラメーターを取得.
		String sql = getExecuteSql();
		// QuinaPreparedStatementで処理.
		QuinaPreparedStatement ps = null;
		QueryResult ret = null;
		try {
			// QuinaPreparedStatementを取得.
			ps = prepareStatement(sql);
			
			// パラメータを反映.
			this.updateParams(ps);
			
			//System.out.println("params: " + (params == null ? 0 : params.size())
			//	+ " sql: " + sql);
			
			// 現在利用中のPreparedStatementをセット.
			nowStatement = ps;
			
			// query返却が必要な場合.
			if(query) {
				// QueryResultを返却.
				ret = QueryResult.create(
					(QuinaResultSet)ps.executeQuery(),
					this);
				// QueryResultを登録.
				if(resultList == null) {
					resultList = new ObjectList<QueryResult>();
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
	 * @return QueryResult シーケンスID結果が返却されます.
	 *                  nullの場合、取得出来ませんでした.
	 */
	public QueryResult getGeneratedKeys() {
		if(nowStatement == null || statementArgs == null ||
			statementArgs.length == 0) {
			return null;
		}
		QueryResult ret = null;
		try {
			// QueryResultを取得.
			ret = QueryResult.create(
				nowStatement.getGeneratedKeys(),
				this);
			// QueryResultを登録.
			if(resultList == null) {
				resultList = new ObjectList<QueryResult>();
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
