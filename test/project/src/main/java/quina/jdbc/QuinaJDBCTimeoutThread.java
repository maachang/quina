package quina.jdbc;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import quina.QuinaServiceThread;

/**
 * PoolingConnectionタイムアウト監視スレッド.
 */
class QuinaJDBCTimeoutThread
	extends QuinaServiceThread<QuinaConnection> {
	
	// 利用中を示すタイムアウト値.
	protected static final long NONE_TIMEOUT = -1L;
	
	// 破棄を示すタイムアウト値.
	protected static final long DESTROY_TIMEOUT = 0L;
	
	// タイムアウト値.
	private long timeout;
	
	// タイムアウト監視Queue.
	private Queue<QuinaConnection> queue =
		new ConcurrentLinkedQueue<QuinaConnection>();
	
	/**
	 * コンストラクタ.
	 * @param timeout タイムアウト値を設定します.
	 */
	public QuinaJDBCTimeoutThread(long timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * データ追加.
	 */
	@Override
	public void offer(QuinaConnection value) {
		queue.offer(value);
	}

	/**
	 * 1つの情報を取得.
	 */
	@Override
	protected QuinaConnection poll() {
		return queue.poll();
	}

	/**
	 * タイムアウト実行処理.
	 */
	@Override
	protected void executeCall(QuinaConnection conn)
		throws Throwable {
		long time;
		// 既に破棄されてる場合.
		if(conn == null || conn.isDestroy()) {
			// この内容は除外.
			conn = null;
			Thread.sleep(50L);
			return;
		// 利用中(time = NONE_TIMEOUT)の場合.
		} else if((time = conn.getLastPoolingTime()) == NONE_TIMEOUT) {
			Thread.sleep(50L);
			return;
		}
		try {
			// タイムアウトしている場合.
			if(time + timeout < System.currentTimeMillis()) {
				// 破棄して除外.
				conn.destroy();
				conn = null;
			}
		} finally {
			// タイムアウトしてない.
			if(conn != null) {
				// 今回評価した内容は終端に追加.
				queue.offer(conn);
			}
		}
		Thread.sleep(50L);
	}
	
	/**
	 * 後始末実行.
	 */
	protected void cleanUpCall() {
		QuinaConnection conn;
		while(true) {
			try {
				if((conn = queue.poll()) == null) {
					return;
				}
				conn.destroy();
			} catch(Exception e) {}
		}
	}
	
}
