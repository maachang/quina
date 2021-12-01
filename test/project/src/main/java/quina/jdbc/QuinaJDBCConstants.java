package quina.jdbc;

import quina.util.AtomicNumber;
import quina.util.AtomicNumber64;

/**
 * QuinaJDBC定義.
 */
public class QuinaJDBCConstants {
	private QuinaJDBCConstants() {}
	
	/**
	 * デフォルトプーリングタイムアウト.
	 * 30秒.
	 */
	protected static final long DEF_POOLING_TIMEOUT = 30000L;
	
	/**
	 * 最小プーリングタイムアウト.
	 * 5秒.
	 */
	protected static final long MIN_POOLING_TIMEOUT = 5000L;
	
	/**
	 * 最大プーリングタイムアウト.
	 * 5分.
	 */
	protected static final long MAX_POOLING_TIMEOUT = 300000L;
	
	/**
	 * プーリングタイムアウト.
	 */
	protected static final AtomicNumber64 poolingTimeout =
		new AtomicNumber64(DEF_POOLING_TIMEOUT);
	
	/**
	 * プーリングタイムアウト値を設定.
	 * @param timeout プーリングタイムアウト値をミリ秒で設定します.
	 */
	public static final void setPoolingTimeout(long timeout) {
		if(MIN_POOLING_TIMEOUT > timeout) {
			timeout = MIN_POOLING_TIMEOUT;
		} else if(MAX_POOLING_TIMEOUT < timeout) {
			timeout = MAX_POOLING_TIMEOUT;
		}
		poolingTimeout.set(timeout);
	}
	
	/**
	 * プーリングタイムアウト値を取得.
	 * @return long プーリングタイムアウト値が返却されます.
	 */
	public static final long getPoolingTimeout() {
		return poolingTimeout.get();
	}

	
	/**
	 * デフォルトのプーリングサイズ.
	 */
	protected static final int DEF_POOLING_SIZE = 15;
	
	/**
	 * 最小のプーリングサイズ.
	 */
	protected static final int MIN_POOLING_SIZE = 5;
	
	/**
	 * 最大のプーリングサイズ.
	 */
	protected static final int MAX_POOLING_SIZE = 9999;
	
	/**
	 * プーリングサイズ.
	 */
	protected static final AtomicNumber poolingSize =
		new AtomicNumber(DEF_POOLING_SIZE);
	
	/**
	 * プーリングサイズを設定.
	 * @param size プーリングサイズ値を設定します.
	 */
	public static final void setPoolingSize(int size) {
		if(MIN_POOLING_SIZE > size) {
			size = MIN_POOLING_SIZE;
		} else if(MAX_POOLING_SIZE < size) {
			size = MAX_POOLING_SIZE;
		}
		poolingSize.set(size);
	}
	
	/**
	 * プーリングサイズを取得.
	 * @return int プーリングサイズが返却されます.
	 */
	public static final int getPoolingSize() {
		return poolingSize.get();
	}
	
	/**
	 * デフォルトのMaxコネクション数.
	 * なし.
	 */
	protected static final int DEF_MAX_CONNECTION = 30;
	
	/**
	 * 最小のMaxコネクション数.
	 */
	protected static final int MIN_MAX_CONNECTION = 5;

	/**
	 * 最大のMaxコネクション数.
	 */
	protected static final int MAX_MAX_CONNECTION = 9999;
	
	/**
	 * Maxコネクション数.
	 */
	protected static final AtomicNumber maxConnection =
		new AtomicNumber(DEF_MAX_CONNECTION);
	
	/**
	 * Maxコネクション数を設定.
	 * @param size Maxコネクション数を設定します.
	 *             0を指定した場合、Maxコネクション数は指定しません.
	 */
	public static final void setMaxConnection(int size) {
		if(size <= 0) {
			size = -1;
		} else if(MIN_MAX_CONNECTION > size) {
			size = MIN_MAX_CONNECTION;
		} else if(MAX_MAX_CONNECTION < size) {
			size = MAX_MAX_CONNECTION;
		}
		maxConnection.set(size);
	}
	
	/**
	 * Maxコネクション数を取得.
	 * @return int Maxコネクション数が返却されます.
	 */
	public static final int getMaxConnection() {
		return maxConnection.get();
	}
	
	/**
	 * デフォルトのコネクションタイムアウト.
	 * なし.
	 */
	protected static final int DEF_CONNECTION_TIMEOUT = 2500;
	
	/**
	 * 最小のコネクションタイムアウト.
	 */
	protected static final int MIN_CONNECTION_TIMEOUT = 500;

	/**
	 * 最大のコネクションタイムアウト.
	 */
	protected static final int MAX_CONNECTION_TIMEOUT = 60000;
	
	/**
	 * コネクションタイムアウト.
	 */
	protected static final AtomicNumber connectionTimeout =
		new AtomicNumber(DEF_MAX_CONNECTION);
	
	/**
	 * コネクションタイムアウト値を設定.
	 * @param timeout コネクションタイムアウト値を設定します.
	 *                0を指定した場合、コネクションタイムアウト値は設定されません.
	 */
	public static final void setConnectionTimeout(int timeout) {
		if(timeout <= 0) {
			timeout = -1;
		} else if(MIN_CONNECTION_TIMEOUT > timeout) {
			timeout = MIN_CONNECTION_TIMEOUT;
		} else if(MAX_CONNECTION_TIMEOUT < timeout) {
			timeout = MAX_CONNECTION_TIMEOUT;
		}
		connectionTimeout.set(timeout);
	}
	
	/**
	 * コネクションタイムアウト値を取得.
	 * @return int コネクションタイムアウト値が返却されます.
	 */
	public static final int getConnectionTimeout() {
		return connectionTimeout.get();
	}
}
