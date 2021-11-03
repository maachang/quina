package quina.jdbc;

import java.util.Queue;

import quina.QuinaServiceThread;
import quina.util.collection.IndexKeyValueList;

/**
 * PoolingConnectionタイムアウト監視スレッド.
 */
class QuinaJDBCTimeoutThread
	extends QuinaServiceThread<Object> {
	
	// 利用中を示すタイムアウト値.
	protected static final long NONE_TIMEOUT = -1L;
	
	// 破棄を示すタイムアウト値.
	protected static final long DESTROY_TIMEOUT = 0L;
	
	// タイムアウト値.
	private long timeout;
	
	// データソース管理.
	private IndexKeyValueList<String, QuinaDataSource> dataSources;
	
	/**
	 * コンストラクタ.
	 * @param dataSources dataSourceを管理するオブジェクトを設定します.
	 * @param timeout タイムアウト値を設定します.
	 */
	protected QuinaJDBCTimeoutThread(
		IndexKeyValueList<String, QuinaDataSource> dataSources,
		long timeout) {
		this.dataSources = dataSources;
		this.timeout = timeout;
	}
	
	/**
	 * データ追加.
	 */
	@Override
	public void offer(Object value) {
	}

	/**
	 * 1つの情報を取得.
	 */
	@Override
	protected QuinaConnection poll() {
		return null;
	}

	/**
	 * タイムアウト実行処理.
	 */
	@Override
	protected void executeCall(Object o)
		throws Throwable {
		Queue<QuinaConnection> q = null;
		QuinaDataSource ds;
		QuinaConnection conn = null;
		final int len = dataSources.size();
		int no = 0;
		int timeoutId = -1;
		while (!stopFlag) {
			// タイムアウトチェック対象のDataSourceの項番を取得.
			if(no >= len) {
				no = 0;
			}
			// 今回処理分のタイムアウトIDを取得.
			if(timeoutId >= 1000) {
				timeoutId = 0;
			} else {
				timeoutId ++;
			}
			// 処理対象のDataSourceを取得.
			ds = dataSources.valueAt(no ++);
			// Pooling対象のQueueを取得.
			q = ds.getPooling();
			// Queueに情報が存在しない場合.
			if(q.size() <= 0) {
				Thread.sleep(50L);
				continue;
			}
			// Queueに情報が存在する場合タイムアウトチェック.
			while(!stopFlag) {
				Thread.sleep(50L);
				try {
					conn = null;
					// 取得できなかった場合.
					if((conn = q.poll()) == null) {
						break;
					// 破棄されてない場合.
					} else if(!conn.isDestroy()) {
						// 今回チェック済みの条件を検出.
						if(conn.getTimeoutId() == timeoutId) {
							// プーリングセット.
							ds.pushPooling(conn);
							// このDataSourceの処理を終了.
							break;
						// タイムアウトしている場合.
						}
						if(conn.getLastPoolingTime() + timeout <
							System.currentTimeMillis()) {
							// 破棄して除外.
							conn.destroy();
						// タイムアウトしてない場合.
						} else {
							// 処理済みとしてTimeoutIdをセット.
							conn.setTimeoutId(timeoutId);
							// プーリングセット.
							ds.pushPooling(conn);
						}
					}
				} catch(Exception e ) {
					// エラーが発生した場合.
					if(conn != null) {
						// コネクションを破棄.
						try {
							conn.destroy();
						} catch(Exception ee) {}
						conn = null;
					}
				}
			}
		}
	}
}
