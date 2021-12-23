package quina.jdbc.io;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import quina.exception.QuinaException;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaPreparedStatement;
import quina.util.Flag;
import quina.util.NumberUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;

/**
 * AbstractStatement.
 */
@SuppressWarnings("unchecked")
public abstract class AbstractStatement<T>
	implements Closeable {
	// jdbc connection.
	protected QuinaConnection conn = null;
	// jdbc statement List.
	protected IndexKeyValueList<String, QuinaPreparedStatement> stmtList =
		new IndexKeyValueList<String, QuinaPreparedStatement>();
	protected QuinaPreparedStatement nowPs;
	// prepared statement params.
	protected ObjectList<Object> params = null;
	// statement生成パラメータ.
	protected Object[] statementArgs = null;
	// クローズフラグ.
	protected final Flag closeFlag = new Flag();
	
	/**
	 * 初期化処理.
	 * @param conn JDBCコネクションを設定します.
	 * @param args Statement用のパラメータを設定します.
	 */
	protected void init(QuinaConnection conn, Object... args) {
		try {
			if(conn.isClosed() || conn.isDestroy()) {
				throw new QuinaException(
					"The target JDBC connection has already " +
					"been destroyed.");
			}
			this.conn = conn;
			this.statementArgs = args;
		} catch(QuinaException qe) {
			throw qe;
		} catch(Exception e) {
			throw new QuinaException(e);
		}
	}
	
	@Override
	public void close() throws IOException {
		if(closeFlag.setToGetBefore(true)) {
			return;
		}
		if(stmtList != null) {
			IndexKeyValueList
				<String, QuinaPreparedStatement>
					list = stmtList;
			stmtList = null;
			final int len = list.size();
			for(int i = 0; i < len; i ++) {
				try {
					list.valueAt(i).close();
				} catch(Exception e) {}
			}
		}
		conn = null;
		params = null;
		statementArgs = null;
	}
	
	/**
	 * 既にクローズしているかチェック.
	 * @return boolean trueの場合クローズしています.
	 */
	public boolean isClosed() {
		// クローズされていない場合.
		if(!closeFlag.get()) {
			try {
				// コネクションがクローズか破棄されてる場合.
				if(conn.isClosed() || conn.isDestroy()) {
					try {
						this.close();
					} catch(Exception e) {}
					return true;
				}
			} catch(Exception ee) {
				// 例外発生した場合は
				try {
					// このコネクションをクローズ.
					this.close();
				} catch(Exception e) {}
				try {
					// コネクションを破棄.
					conn.destroy();
				} catch(Exception e) {}
				return true;
			}
		}
		return false;
	}
	
	/**
	 * クローズチェック.
	 */
	public void checkClose() {
		if(isClosed()) {
			throw new QuinaException(
				"The I/O Statement is closed.");
		}
	}
	
	/**
	 * コネクションオブジェクトを取得.
	 * @return
	 */
	public QuinaConnection getConnection() {
		checkClose();
		return conn;
	}
	
	/**
	 * コミット処理.
	 * @return T このオブジェクトが返却されます.
	 */
	public T commit() {
		checkClose();
		try {
			conn.commit();
		} catch(SQLException e) {
			throw new QuinaException(e);
		}
		return (T)this;
	}
	
	/**
	 * ロールバック処理.
	 * @return T このオブジェクトが返却されます.
	 */
	public T rollback() {
		checkClose();
		try {
			conn.rollback();
		} catch(SQLException e) {
			throw new QuinaException(e);
		}
		return (T)this;
	}
	
	/**
	 * パラメーターを設定.
	 * @param args 対象のパラメータを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T params(Object... args) {
		checkClose();
		if(args != null && args.length > 0) {
			if(params == null) {
				params = new ObjectList<Object>();
			}
			final int len = args.length;
			for(int i = 0; i < len; i ++) {
				params.add(args[i]);
			}
		}
		return (T)this;
	}
	
	/**
	 * パラメーターを設定.
	 * @param args 対象のパラメータを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T params(ObjectList<?> args) {
		checkClose();
		if(args != null && args.size() > 0) {
			if(params == null) {
				params = new ObjectList<Object>();
			}
			final int len = args.size();
			for(int i = 0; i < len; i ++) {
				params.add(args.get(i));
			}
		}
		return (T)this;
	}
	
	/**
	 * パラメーターを設定.
	 * @param args 対象のパラメータを設定します.
	 * @return T このオブジェクトが返却されます.
	 */
	public T params(List<?> args) {
		checkClose();
		if(args != null && args.size() > 0) {
			if(params == null) {
				params = new ObjectList<Object>();
			}
			final int len = args.size();
			for(int i = 0; i < len; i ++) {
				params.add(args.get(i));
			}
		}
		return (T)this;
	}
	
	/**
	 * 実行用のパラメーターをクリア.
	 * @return T このオブジェクトが返却されます.
	 */
	public final T clearParmas() {
		params = null;
		return (T)this;
	}
	
	/**
	 * 実行パラメータを取得.
	 * @return Object[] 実行パラメータが返却されます.
	 */
	protected Object[] getExecuteParams() {
		if(params == null || params.size() == 0) {
			return null;
		}
		return params.toArray();
	}
	
	/**
	 * PreparedStatementを取得.
	 * @param sql SQL文を設定します.
	 * @return QuinaPreparedStatement QuinaPreparedStatementが
	 *                                返却されます.
	 * @throws Exception 例外.
	 */
	protected QuinaPreparedStatement prepareStatement(String sql)
		throws Exception {
		sql = sql.trim();
		String key = null;
		QuinaPreparedStatement ret = null;
		final int len = statementArgs == null ?
			0 : statementArgs.length;
		if(len ==1 && statementArgs[0] instanceof Integer) {
			// パラメータを取得.
			Integer autoGeneratedKeys = (Integer)statementArgs[0];
			// 登録キーを作成.
			key = new StringBuilder("1@")
				.append(autoGeneratedKeys)
				.append("/")
				.append(sql)
				.toString();
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(sql, autoGeneratedKeys);
		} else if(len == 1 && statementArgs[0] instanceof int[]) {
			// パラメータを取得.
			int[] columnIndexes = (int[])statementArgs[0];
			// 登録キーを作成.
			int pLen = columnIndexes.length;
			StringBuilder buf = new StringBuilder("2@");
			for(int i = 0; i < pLen; i ++) {
				buf.append(columnIndexes[i])
					.append("/");
			}
			key = buf.append(sql).toString();
			buf = null;
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(sql, columnIndexes);
		} else if(len ==1 && statementArgs[0] instanceof String[]) {
			// パラメータを取得.
			String[] columnNames = (String[])statementArgs[0];
			// 登録キーを作成.
			int pLen = columnNames.length;
			StringBuilder buf = new StringBuilder("3@");
			for(int i = 0; i < pLen; i ++) {
				buf.append(columnNames[i])
					.append("/");
			}
			key = buf.append(sql).toString();
			buf = null;
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(sql, columnNames);
		} else if(len == 2) {
			// パラメータを取得.
			Integer resultSetType = NumberUtil.parseInt(statementArgs[0]);
			Integer resultSetConcurrency = NumberUtil.parseInt(statementArgs[1]);
			// 登録キーを作成.
			key = new StringBuilder("4@")
				.append(resultSetType)
				.append("/")
				.append(resultSetConcurrency)
				.append("/")
				.append(sql)
				.toString();
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(
				sql, resultSetType, resultSetConcurrency);
		} else if(len == 3) {
			// パラメータを取得.
			Integer resultSetType = NumberUtil.parseInt(statementArgs[0]);
			Integer resultSetConcurrency = NumberUtil.parseInt(statementArgs[1]);
			Integer resultSetHoldability = NumberUtil.parseInt(statementArgs[2]);
			// 登録キーを作成.
			key = new StringBuilder("5@")
				.append(resultSetType)
				.append("/")
				.append(resultSetConcurrency)
				.append("/")
				.append(resultSetHoldability)
				.append("/")
				.append(sql)
				.toString();
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(
				sql, resultSetType, resultSetConcurrency,
				resultSetHoldability);
		} else {
			// 登録キーを作成.
			key = new StringBuilder("0@/")
				.append(sql)
				.toString();
			// 以前に登録された内容があれば、それを再利用.
			if((ret = stmtList.get(key)) != null) {
				// 現在対象のPreparedStatementをセット.
				nowPs = ret;
				return ret;
			}
			// 新しく作成.
			ret = conn.prepareStatement(sql);
		}
		// 登録処理.
		this.stmtList.put(key, ret);
		// 現在対象のPreparedStatementをセット.
		nowPs = ret;
		return ret;
	}
	
	/**
	 * 現在有効なPreparedStatementを取得します.
	 * @return QuinaPreparedStatement
	 *         現在有効なPreparedStatementが返却されます.
	 */
	protected QuinaPreparedStatement nowPreparedStatement() {
		return nowPs;
	}
	
	/**
	 * 指定PreparedStatementに対してパラメータを反映.
	 * @param ps 対象のQuinaPreparedStatementを設定します.
	 * @return QuinaPreparedStatement QuinaPreparedStatementが
	 *                                返却されます.
	 */
	protected QuinaPreparedStatement updateParams(
		QuinaPreparedStatement ps) {
		Object[] params = getExecuteParams();
		try {
			// パラメータが存在する場合.
			if(params != null && params.length > 0) {
				// PreparedStatementパラメータをセット.
				DbUtil.preParams(
					ps, ps.getParameterMetaData(), params);
			}
			return ps;
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

}
