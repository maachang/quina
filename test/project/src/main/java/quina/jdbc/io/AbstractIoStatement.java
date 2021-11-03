package quina.jdbc.io;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

import quina.exception.QuinaException;
import quina.jdbc.AbstractQuinaProxyStatement;
import quina.jdbc.QuinaConnection;
import quina.jdbc.QuinaResultSet;
import quina.util.Flag;
import quina.util.NumberUtil;
import quina.util.collection.IndexKeyValueList;
import quina.util.collection.ObjectList;

/**
 * AbstractIoStatement.
 */
public abstract class AbstractIoStatement<T>
	implements Closeable {
	// jdbc connection.
	protected QuinaConnection conn = null;
	// jdbc statement List.
	protected IndexKeyValueList<String, AbstractQuinaProxyStatement> stmtList =
		new IndexKeyValueList<String, AbstractQuinaProxyStatement>();
	// resultSet.
	protected QuinaResultSet rs = null;
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
					"The target JDBC connection has already been destroyed.");
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
			IndexKeyValueList<String, AbstractQuinaProxyStatement>
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
	
	// クローズチェック.
	protected void checkClose() {
		if(isClosed()) {
			throw new QuinaException(
				"The I/O Statement is closed.");
		}
	}
	
	/**
	 * コミット処理.
	 */
	public void commit() {
		checkClose();
		try {
			conn.commit();
		} catch(SQLException e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * ロールバック処理.
	 */
	public void rollback() {
		checkClose();
		try {
			conn.rollback();
		} catch(SQLException e) {
			throw new QuinaException(e);
		}
	}
	
	/**
	 * パラメーターを設定.
	 * @param args 対象のパラメータを設定します.
	 * @return AbstractIoStatement<T> このオブジェクトが返却されます.
	 */
	public AbstractIoStatement<T> params(Object... args) {
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
		return this;
	}
	
	/**
	 * 実行用のパラメーターをクリア.
	 */
	protected final void clearParmas() {
		params = null;
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
	 * @return AbstractQuinaProxyStatement PreparedStatementが返却されます.
	 * @throws Exception 例外.
	 */
	protected AbstractQuinaProxyStatement prepareStatement(String sql)
		throws Exception {
		// 指定SQLのStatementを取得.
		AbstractQuinaProxyStatement ret = stmtList.get(sql);
		if(ret != null) {
			return ret;
		}
		// 新しいStatementを取得.
		final int len = statementArgs == null ?
			0 : statementArgs.length;
		if(len ==1 && statementArgs[0] instanceof Integer) {
			Integer autoGeneratedKeys = (Integer)statementArgs[0];
			ret = conn.prepareStatement(sql, autoGeneratedKeys);
		} else if(len ==1 && statementArgs[0] instanceof int[]) {
			int[] columnIndexes = (int[])statementArgs[0];
			ret = conn.prepareStatement(sql, columnIndexes);
		} else if(len ==1 && statementArgs[0] instanceof String[]) {
			String[] columnNames = (String[])statementArgs[0];
			ret = conn.prepareStatement(sql, columnNames);
		} else if(len == 2) {
			Integer resultSetType = NumberUtil.parseInt(statementArgs[0]);
			Integer resultSetConcurrency = NumberUtil.parseInt(statementArgs[1]);
			ret = conn.prepareStatement(
				sql, resultSetType, resultSetConcurrency);
		} else if(len == 3) {
			Integer resultSetType = NumberUtil.parseInt(statementArgs[0]);
			Integer resultSetConcurrency = NumberUtil.parseInt(statementArgs[1]);
			Integer resultSetHoldability = NumberUtil.parseInt(statementArgs[2]);
			ret = conn.prepareStatement(
					sql, resultSetType, resultSetConcurrency, resultSetHoldability);
		} else {
			ret = conn.prepareStatement(sql);
		}
		// リストにセット.
		this.stmtList.put(sql, ret);
		return ret;
	}
}
