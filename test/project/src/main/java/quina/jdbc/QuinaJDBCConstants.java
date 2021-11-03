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

}
